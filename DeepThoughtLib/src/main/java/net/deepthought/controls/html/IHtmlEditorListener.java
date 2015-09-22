package net.deepthought.controls.html;

/**
 * Created by ganymed on 28/08/15.
 */
public interface IHtmlEditorListener {

  void htmlCodeUpdated(String newHtmlCode);

  /**
   * If a custom Command handling is desired, handle the Command and return true.
   * If false is return default Command handling is executed.
   * @return
   */
  boolean handleCommand(HtmlEditor editor, HtmEditorCommand command);

  void imageHasBeenDeleted(String imageId, String imageUrl);

}
