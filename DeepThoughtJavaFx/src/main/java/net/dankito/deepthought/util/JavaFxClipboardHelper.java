package net.dankito.deepthought.util;

import net.dankito.deepthought.clipboard.IClipboardHelper;

import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

/**
 * Created by ganymed on 23/07/15.
 */
public class JavaFxClipboardHelper implements IClipboardHelper {

  public void copyStringToClipboard(String clipboardContent) {
    final ClipboardContent content = new ClipboardContent();
    content.putString(clipboardContent);
    Clipboard.getSystemClipboard().setContent(content);
  }

  @Override
  public void copyUrlToClipboard(String url) {
    final ClipboardContent content = new ClipboardContent();
    content.putString(url); // also copy URL as Plain Text
    content.putUrl(url);
    Clipboard.getSystemClipboard().setContent(content);
  }

}
