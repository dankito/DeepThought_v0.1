package net.deepthought.communication.model;

import net.deepthought.util.file.FileUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * Created by ganymed on 04/10/15.
 */
public class DoOcrConfiguration {

  private final static Logger log = LoggerFactory.getLogger(DoOcrConfiguration.class);


  protected OcrSource source = OcrSource.AskUser;

  protected byte[] imageToRecognize = null;

  protected String imageUri = null;

  protected boolean showSettingsUi = true;


  public DoOcrConfiguration(OcrSource source) {
    this.source = source;
  }


  public DoOcrConfiguration(byte[] imageToRecognize) {
    this(imageToRecognize, false);
  }

  public DoOcrConfiguration(byte[] imageToRecognize, boolean showSettingsUi) {
    this(OcrSource.RecognizeFromUri);
    this.imageToRecognize = imageToRecognize;
    this.showSettingsUi = showSettingsUi;
  }

  public DoOcrConfiguration(File imageToRecognize, boolean showSettingsUi) throws IOException {
    this(readImageFile(imageToRecognize), showSettingsUi);

    try { setImageUri(imageToRecognize.toURI().toString()); } catch(Exception ex) { }
  }


  public OcrSource getSource() {
    return source;
  }

  public boolean hasImageToRecognize() {
    return this.imageToRecognize != null;
  }

  public void setImageToRecognize(File imageToRecognize) throws IOException {
    this.imageToRecognize = readImageFile(imageToRecognize);
  }

  public byte[] getImageToRecognize() {
    if(imageToRecognize == null && imageUri != null) {
      try {
        imageToRecognize = FileUtils.readFile(new File(imageUri));
      } catch(Exception ex) { log.error("Could not read Image to Recognize from Uri " + imageUri, ex); }
    }
    return imageToRecognize;
  }

  public byte[] getAndResetImageToRecognize() {
    byte[] backup = getImageToRecognize();
    this.imageToRecognize = null;
    return backup;
  }

  public String getImageUri() {
    return imageUri;
  }

  public void setImageUri(String imageUri) {
    this.imageUri = imageUri;
  }

  public boolean showSettingsUi() {
    return showSettingsUi;
  }


  private static byte[] readImageFile(File imageToRecognize) throws IOException {
    return FileUtils.readFile(imageToRecognize);
  }

}
