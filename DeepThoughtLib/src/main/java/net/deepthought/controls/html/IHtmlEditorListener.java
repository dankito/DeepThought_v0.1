package net.deepthought.controls.html;

import net.deepthought.data.html.ImageElementData;

/**
 * Created by ganymed on 28/08/15.
 */
public interface IHtmlEditorListener {

  void editorHasLoaded(HtmlEditor editor);

  /**
   * <p> Called each time Html in CKEditor has changed.</p>
   *  <b><p>Important: Do not execute any expensive functions in listener method.</p>
   *  <p>This method is called indirectly from JavaScript code. Doing expensive functions can slow down even machines with 8 (virtual) cores.</p></b>
   * @param updatedHtmlCode The updated Html code
   */
  void htmlCodeUpdated();

  /**
   * <p>
   *   Called if after changing HTML the Undo Button is so often pressed that all changes have been undone -> original set HTML has been restored.
   * </p>
   */
  void htmlCodeHasBeenReset();

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

}
