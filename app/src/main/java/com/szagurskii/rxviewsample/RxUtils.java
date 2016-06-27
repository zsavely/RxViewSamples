package com.szagurskii.rxviewsample;

import android.widget.EditText;

import com.jakewharton.rxbinding.widget.RxTextView;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * @author Savelii Zagurskii
 */
public class RxUtils {
  /** Returns an Observable that subscribes to TextView but observes the result on the computation thread. */
  static Observable<CharSequence> rxTextView(EditText editText) {
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
}
