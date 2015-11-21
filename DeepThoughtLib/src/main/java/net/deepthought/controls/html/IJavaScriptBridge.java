package net.deepthought.controls.html;

/**
 * Public methods for communicating from CKEditor's JavaScript with Java code.
 *
 * Created by ganymed on 26/09/15.
 */
public interface IJavaScriptBridge {

  void ckEditorLoaded();

  void htmlChanged();

  void htmlHasBeenReset();


  boolean elementClicked(String element, int button, int clickX, int clickY);

  boolean elementDoubleClicked(String element);

  boolean beforeCommandExecution(String commandName);

}
