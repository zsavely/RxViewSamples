package com.szagurskii.rxviewsample;

import android.support.annotation.NonNull;

/**
 * @author Savelii Zagurskii
 */
public class Utils {
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
  static boolean isPasswordValid(@NonNull String password) {
    return password.matches(PASSWORD_PATTERN);
  }

  /** A simple function to validate username. */
  static boolean isUsernameValid(@NonNull String username) {
    return username.length() > 5;
  }

  /** A simple function to validate e-mail address. */
  static boolean isEmailValid(@NonNull String email) {
    return !email.isEmpty() && email.contains("@") && email.contains(".");
  }
}
