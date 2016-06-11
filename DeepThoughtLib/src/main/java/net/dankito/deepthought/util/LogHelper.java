package net.dankito.deepthought.util;

/**
 * Created by ganymed on 29/09/15.
 */
public class LogHelper {

  public static String createTimeElapsedString(long millisecondsElapsed) {
    return (millisecondsElapsed / 1000) + "." + String.format("%03d", millisecondsElapsed).substring(0, 3);
  }

}
