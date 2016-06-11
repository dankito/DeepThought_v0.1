package net.dankito.deepthought.util;

/**
 * Created by ganymed on 04/02/15.
 */
public class StringUtils {

  public static boolean isNullOrEmpty(String string) {
    return string == null || /*string.isEmpty() */ string.trim().length() == 0; // unbelievable, Android 2.2 cannot handle string.isEmpty()
  }

  public static boolean isNotNullOrEmpty(String string) {
    return isNullOrEmpty(string) == false;
  }

  public static int getNumberOfOccurrences(String textToSearchFor, String textToSearchIn) {
    int numberOfOccurrences = 0;
    int indexOfOccurrence = -2;

    while((indexOfOccurrence = textToSearchIn.indexOf(textToSearchFor, indexOfOccurrence + 1)) >= 0) {
      numberOfOccurrences++;
    }

    return numberOfOccurrences;
  }
}
