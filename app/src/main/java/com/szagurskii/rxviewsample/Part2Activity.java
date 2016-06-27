package com.szagurskii.rxviewsample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import com.jakewharton.rxbinding.view.RxView;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func3;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import static com.szagurskii.rxviewsample.RxUtils.rxTextView;
import static com.szagurskii.rxviewsample.Utils.isEmailValid;
import static com.szagurskii.rxviewsample.Utils.isPasswordValid;
import static com.szagurskii.rxviewsample.Utils.isUsernameValid;

public final class Part2Activity extends Activity {
  private static final String TAG = Part2Activity.class.getSimpleName();
  private static final String KEY_BUTTON_ENABLED = "key_button_enabled";

  /** A CompositeSubscription which holds all created subscriptions and should be unsubscribed in onStop(). */
  private final CompositeSubscription compositeSubscription = new CompositeSubscription();

  private EditText email;
  private EditText username;
  private EditText password;
  private Button button;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_part2);

    email = (EditText) findViewById(R.id.email);
    username = (EditText) findViewById(R.id.username);
    password = (EditText) findViewById(R.id.password);
    button = (Button) findViewById(R.id.proceed);

    // Create a Subscription for EditTexts.
    Subscription subscriptionEditTexts = Observable.combineLatest(rxTextView(email), rxTextView(username), rxTextView(password),
        new Func3<CharSequence, CharSequence, CharSequence, Boolean>() {
          // This will happen in the background because
          // all Observables here are already observing on Schedulers.computation().
          @Override public Boolean call(CharSequence email, CharSequence username, CharSequence password) {
            boolean emailValid = isEmailValid(email.toString());
            boolean usernameValid = isUsernameValid(username.toString());
            boolean passwordValid = isPasswordValid(password.toString());

            return emailValid && usernameValid && passwordValid;
          }
        })
        // This doesn't affect much but
        // we can subscribe in the background, so why not?
        .subscribeOn(Schedulers.computation())
        // Change the observation thread to the main thread
        // in order to change the button state.
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Observer<Boolean>() {
          //@formatter:off
          @Override public void onCompleted() {
            Log.d(TAG, "onCompleted()");
          }
          @Override public void onError(Throwable e) {
            Log.d(TAG, "onError()", e);
          }
          //@formatter:on
          @Override public void onNext(Boolean enabled) {
            Log.d(TAG, String.format("onNext(enabled) -> %1$s", enabled));
            button.setEnabled(enabled);
          }
        });

    // Create a Subscription for button clicks.
    Subscription subscriptionButton = RxView.clicks(button)
        .subscribeOn(AndroidSchedulers.mainThread())
        .subscribe(new Action1<Void>() {
          @Override public void call(Void aVoid) {
            Intent intent = new Intent(Part2Activity.this, Part1Activity.class);
            startActivity(intent);
          }
        });

    // Add all created subscriptions to the CompositeSubscription.
    compositeSubscription.add(subscriptionEditTexts);
    compositeSubscription.add(subscriptionButton);
  }

  @Override protected void onStop() {
    super.onStop();

    // Unsubscribe from all added subscriptions.
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

    // Release view references.
    email = null;
    username = null;
    password = null;
    button = null;
  }
}
