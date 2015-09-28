package net.deepthought.controls.html;

import net.deepthought.data.html.ImageElementData;

/**
 * Created by ganymed on 28/08/15.
 */
public interface IHtmlEditorListener {

  void editorHasLoaded(HtmlEditor editor);

  void htmlCodeUpdated(String newHtmlCode);

  /**
   * If a custom Command handling is desired, handle the Command and return true.
   * If false is return default Command handling is executed.
   * @return
   */
  boolean handleCommand(HtmlEditor editor, HtmEditorCommand command);

  /**
   * If a custom Command Double Click handling is desired, return true.
   * If false is return default Double Click handling is executed.
   * @return
   */
  boolean elementDoubleClicked(HtmlEditor editor, ImageElementData elementData);

  void imageAdded(ImageElementData addedImage);

  void imageHasBeenDeleted(ImageElementData deletedImage, boolean isStillInAnotherInstanceOnHtml);

}
