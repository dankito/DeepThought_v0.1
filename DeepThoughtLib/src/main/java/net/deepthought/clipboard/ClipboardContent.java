package net.deepthought.clipboard;

import java.io.File;
import java.util.List;

import javafx.scene.image.Image;

/**
 * Created by ganymed on 24/04/15.
 */
public interface ClipboardContent {

  boolean hasString();

  String getString();

  boolean hasUrl();

  String getUrl();

  boolean hasHtml();

  String getHtml();

  boolean hasRtf();

  String getRtf();

  boolean hasImage();

  Image getImage();

  boolean hasFiles();

  List<File> getFiles();

}
