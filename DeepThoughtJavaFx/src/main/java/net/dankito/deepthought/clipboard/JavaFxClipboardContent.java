package net.dankito.deepthought.clipboard;

import net.dankito.deepthought.clipboard.ClipboardContent;

import java.io.File;
import java.util.List;

import javafx.scene.image.Image;
import javafx.scene.input.Clipboard;

/**
 * Created by ganymed on 24/04/15.
 */
public class JavaFxClipboardContent implements ClipboardContent {

  protected Clipboard clipboard;

  public JavaFxClipboardContent(Clipboard clipboard) {
    this.clipboard = clipboard;
  }


  @Override
  public boolean hasString() {
    return clipboard.hasString();
  }

  @Override
  public String getString() {
    return clipboard.getString();
  }

  @Override
  public boolean hasUrl() {
    return clipboard.hasUrl();
  }

  @Override
  public String getUrl() {
    return clipboard.getUrl();
  }

  @Override
  public boolean hasHtml() {
    return clipboard.hasHtml();
  }

  @Override
  public String getHtml() {
    return clipboard.getHtml();
  }

  @Override
  public boolean hasRtf() {
    return clipboard.hasRtf();
  }

  @Override
  public String getRtf() {
    return clipboard.getRtf();
  }

  @Override
  public boolean hasImage() {
    return clipboard.hasImage();
  }

  @Override
  public Image getImage() {
    return clipboard.getImage();
  }

  @Override
  public boolean hasFiles() {
    return clipboard.hasFiles();
  }

  @Override
  public List<File> getFiles() {
    return clipboard.getFiles();
  }

}
