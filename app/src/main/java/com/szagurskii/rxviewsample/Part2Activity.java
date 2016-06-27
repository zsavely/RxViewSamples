package com.szagurskii.rxviewsample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import com.jakewharton.rxbinding.view.RxView;
import com.jakewharton.rxbinding.widget.RxTextView;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func3;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

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

    Subscription subscriptionEditTexts = Observable.combineLatest(rxTextView(email), rxTextView(username), rxTextView(password),
        new Func3<CharSequence, CharSequence, CharSequence, Boolean>() {
          @Override public Boolean call(CharSequence email, CharSequence username, CharSequence password) {
            boolean emailValid = isEmailValid(email.toString());
            boolean usernameValid = isUsernameValid(username.toString());
            boolean passwordValid = isPasswordValid(password.toString());

            return emailValid && usernameValid && passwordValid;
          }
        })
        .subscribeOn(Schedulers.computation())
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

    email = null;
    username = null;
    password = null;
    button = null;
  }

  /** Returns an Observable that subscribes to TextView but observes the result on the computation thread. */
  private static Observable<CharSequence> rxTextView(EditText editText) {
    return RxTextView.textChanges(editText)
        .observeOn(Schedulers.computation())
        .debounce(new Func1<CharSequence, Observable<Long>>() {
          @Override public Observable<Long> call(CharSequence charSequence) {
            // Do not debounce if the CharSequence is empty
            // because in this way we can disable the button simultaneously.
            if (charSequence.length() == 0) return Observable.just(0L);

            // Otherwise, wait for 500 millis.
            return Observable.timer(500, TimeUnit.MILLISECONDS);
          }
        })
        .subscribeOn(AndroidSchedulers.mainThread());
  }

  /** A simple function to validate e-mail address. */
  private static boolean isEmailValid(@NonNull String email) {
    return !email.isEmpty() && email.contains("@") && email.contains(".");
  }

  /** A simple function to validate username. */
  private static boolean isUsernameValid(@NonNull String username) {
    return username.length() > 5;
  }

  /**
   * {@code (?=.*\d)} is for one digit from 0-9.
   * <br>
   * {@code (?=.*[a-z])} is for one lowercase character.
   * <br>
   * {@code (?=.*[A-Z])} is for one uppercase character.
   * <br>
   * {@code .} match anything with previous condition checking.
   * <br>
   * {@code {6,20}} length is more than 6 characters and less or equal to 20.
   *
   * @see <a href="http://goo.gl/OGKn03">Information about regular expressions.</a>
   */
  private static final String PASSWORD_PATTERN = "((?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{6,20})";

  /**
   * <p>
   * This method is used for validating the password complexity.
   * </p>
   *
   * @return {@code true} if the password is complex enough.
   */
  private static boolean isPasswordValid(@NonNull String password) {
    return password.matches(PASSWORD_PATTERN);
  }
}
