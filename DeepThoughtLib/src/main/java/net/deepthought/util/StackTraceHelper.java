package net.deepthought.util;

import com.sun.javafx.stage.StageHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ganymed on 28/11/14.
 */
public class StackTraceHelper {

  private final static Logger log = LoggerFactory.getLogger(StageHelper.class);


  public static String GetStackTrace() {
    String stackTraceString = "";

    try {
      StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

      for (int i = 2; i < stackTrace.length; i++) {
        stackTraceString += System.lineSeparator() + stackTrace[i].toString();
      }
    } catch(Exception ex) {
      log.error("Could not get StackTrace", ex);
    }

    return stackTraceString;
  }
}
