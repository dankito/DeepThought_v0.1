package com.j256.ormlite.jpa;

/**
 * Created by ganymed on 05/03/15.
 */
public class StringHelper {

  public static boolean stringNotNullOrEmpty(String value) {
    return (value != null && value.length() > 0);
  }

}
