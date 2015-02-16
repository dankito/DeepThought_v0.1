package net.deepthought.util;

/**
 * Created by ganymed on 04/02/15.
 */
public class StringUtils {

  public static boolean isNullOrEmpty(String string) {
    return string == null || string.isEmpty();
  }

  public static boolean isNotNullOrEmpty(String string) {
    return isNullOrEmpty(string) == false;
  }

}
