package com.szagurskii.rxviewsample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import com.jakewharton.rxbinding.view.RxView;
import com.jakewharton.rxbinding.widget.RxTextView;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public final class Part2Activity extends Activity {
  private static final String TAG = Part2Activity.class.getSimpleName();
  private static final String KEY_BUTTON_ENABLED = "key_button_enabled";

  private final Random random = new Random();

  private EditText username;
  private EditText password;
  private Button button;

  /** A subscription which should be unsubscribed in onStop(). */
  private CompositeSubscription compositeSubscription = new CompositeSubscription();

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_part2);

    username = (EditText) findViewById(R.id.username);
    password = (EditText) findViewById(R.id.password);
    button = (Button) findViewById(R.id.login);

    Subscription subscriptionEditTexts = Observable.combineLatest(rxTextView(username), rxTextView(password),
        new Func2<CharSequence, CharSequence, Boolean>() {
          @Override public Boolean call(CharSequence login, CharSequence password) {
            if (login.length() != 0 && password.length() != 0) {
              // Suppose you do some hard work with CharSequence.
              SystemClock.sleep(random.nextInt(1000));
            }
            return login.toString().trim().length() > 0 && password.length() > 0;
          }
        })
        .subscribeOn(Schedulers.computation())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Observer<Boolean>() {
          @Override public void onCompleted() {
            Log.d(TAG, "onCompleted()");
          }

          @Override public void onError(Throwable e) {
            Log.d(TAG, "onError()", e);
          }

          @Override public void onNext(Boolean enabled) {
            Log.d(TAG, String.format("onNext(enabled) -> %1$s", enabled));
            button.setEnabled(enabled);
          }
        });

    Subscription subscriptionButton = RxView.clicks(button)
        .subscribeOn(AndroidSchedulers.mainThread())
        .subscribe(new Action1<Void>() {
          @Override public void call(Void aVoid) {
            Intent intent = new Intent(Part2Activity.this, Part1Activity.class);
            startActivity(intent);
          }
        });

    compositeSubscription.add(subscriptionEditTexts);
    compositeSubscription.add(subscriptionButton);
  }

  @Override protected void onStop() {
    super.onStop();

    if (!compositeSubscription.isUnsubscribed()) {
      compositeSubscription.unsubscribe();
    }
  }

  @Override protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);

    // Save button state. You can also do that by extending the Button class.
    outState.putBoolean(KEY_BUTTON_ENABLED, button.isEnabled());
  }

  @Override protected void onRestoreInstanceState(Bundle savedInstanceState) {
    super.onRestoreInstanceState(savedInstanceState);

    // Restore button state.
    button.setEnabled(savedInstanceState.getBoolean(KEY_BUTTON_ENABLED, false));
  }

  @Override protected void onDestroy() {
    super.onDestroy();

    username = null;
    password = null;
    button = null;
  }

  /** Returns an Observable that subscribes to TextView but observes the result on computation thread. */
  private static Observable<CharSequence> rxTextView(EditText editText) {
    return RxTextView.textChanges(editText)
        .observeOn(Schedulers.computation())
        .debounce(new Func1<CharSequence, Observable<Long>>() {
          @Override public Observable<Long> call(CharSequence charSequence) {
            // Do not debounce if the CharSequence is empty.
            if (charSequence.length() == 0) return Observable.just(0L);

            // Otherwise, wait for 500 millis.
            return Observable.timer(500, TimeUnit.MILLISECONDS);
          }
        })
        .subscribeOn(AndroidSchedulers.mainThread());
  }
}
