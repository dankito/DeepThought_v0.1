package net.dankito.deepthought.util;

import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

/**
 * Created by ganymed on 23/07/15.
 */
public class ClipboardHelper {

  public static void copyStringToClipboard(String clipboardContent) {
    final ClipboardContent content = new ClipboardContent();
    content.putString(clipboardContent);
    Clipboard.getSystemClipboard().setContent(content);
  }
}
