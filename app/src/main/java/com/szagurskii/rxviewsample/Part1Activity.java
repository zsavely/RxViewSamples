package com.szagurskii.rxviewsample;

import android.app.Activity;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

import com.jakewharton.rxbinding.widget.RxTextView;

import java.util.concurrent.TimeUnit;

import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public final class Part1Activity extends Activity {
  private static final String TAG = Part1Activity.class.getSimpleName();

  private EditText editText;
  private TextView content;
  private TextView count;

  /** A subscription which should be unsubscribed in onStop(). */
  private Subscription subscription;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_part1);

    editText = (EditText) findViewById(R.id.edittext);
    content = (TextView) findViewById(R.id.content);
    count = (TextView) findViewById(R.id.count);

    subscription = RxTextView.textChanges(editText)
        .debounce(500, TimeUnit.MILLISECONDS)
        .observeOn(Schedulers.computation())
        .filter(new Func1<CharSequence, Boolean>() {
          @Override public Boolean call(CharSequence charSequence) {
            SystemClock.sleep(1000); // Simulate the heavy stuff.
            return charSequence.length() != 0;
          }
        })
        .subscribeOn(AndroidSchedulers.mainThread())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Observer<CharSequence>() {
          //@formatter:off
          @Override public void onCompleted() { }
          @Override public void onError(Throwable e) {
            Log.d(TAG, "Error.", e);
          }
          //@formatter:on
          @Override public void onNext(CharSequence charSequence) {
            content.setText(charSequence);
            count.setText(String.valueOf(charSequence.length()));
          }
        });
  }

  @Override protected void onStop() {
    super.onStop();

    if (!subscription.isUnsubscribed()) {
      subscription.unsubscribe();
    }
  }

  @Override protected void onDestroy() {
    super.onDestroy();

    editText = null;
    content = null;
    count = null;
  }
}
