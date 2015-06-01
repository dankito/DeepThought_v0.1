package net.deepthought.data.contentextractor;

import java.io.File;
import java.util.List;

import javafx.scene.image.Image;

/**
 * Created by ganymed on 24/04/15.
 */
public interface ClipboardContent {

  public boolean hasString();

  public String getString();

  public boolean hasUrl();

  public String getUrl();

  public boolean hasHtml();

  public String getHtml();

  public boolean hasRtf();

  public String getRtf();

  public boolean hasImage();

  public Image getImage();

  public boolean hasFiles();

  public List<File> getFiles();

}
