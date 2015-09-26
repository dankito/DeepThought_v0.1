package net.deepthought.controls.html;

/**
 * Public methods for communicating from CKEditor's JavaScript with Java code.
 *
 * Created by ganymed on 26/09/15.
 */
public interface IJavaScriptBridge {

  void loaded();

  void htmlChanged(String newHtmlCode);

}
