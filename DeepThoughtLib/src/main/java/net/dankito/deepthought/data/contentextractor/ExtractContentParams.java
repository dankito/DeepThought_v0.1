package net.dankito.deepthought.data.contentextractor;

import java.net.URL;

/**
 * Created by ganymed on 15/01/15.
 */
public class ExtractContentParams {

  protected URL url;

  protected ExtractContentListener listener;


  public ExtractContentParams(URL url) {
    this.url = url;
  }

  public ExtractContentParams(URL url, ExtractContentListener listener) {
    this(url);
    this.listener = listener;
  }


  @Override
  public String toString() {
    return url.toString();
  }

}
