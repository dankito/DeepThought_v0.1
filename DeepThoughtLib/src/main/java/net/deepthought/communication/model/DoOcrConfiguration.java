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


  protected byte[] imageToRecognize = null;

  protected String imageUri = null;

  protected boolean showSettingsUi = true;

  protected boolean showMessageOnRemoteDeviceWhenProcessingDone = false;



  public DoOcrConfiguration(byte[] imageToRecognize) {
    this(imageToRecognize, false);
  }

  public DoOcrConfiguration(byte[] imageToRecognize, boolean showSettingsUi) {
    this(imageToRecognize, showSettingsUi, false);
  }

  public DoOcrConfiguration(byte[] imageToRecognize, boolean showSettingsUi, boolean showMessageOnRemoteDeviceWhenProcessingDone) {
    this.imageToRecognize = imageToRecognize;
    this.showSettingsUi = showSettingsUi;
    this.showMessageOnRemoteDeviceWhenProcessingDone = showMessageOnRemoteDeviceWhenProcessingDone;
  }

  public DoOcrConfiguration(File imageToRecognize, boolean showSettingsUi) throws IOException {
    this(imageToRecognize, showSettingsUi, false);
  }

  public DoOcrConfiguration(File imageToRecognize, boolean showSettingsUi, boolean showMessageOnRemoteDeviceWhenProcessingDone) throws IOException {
    this(readImageFile(imageToRecognize), showSettingsUi, showMessageOnRemoteDeviceWhenProcessingDone);
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
    byte[] backup = imageToRecognize;
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

  public boolean showMessageOnRemoteDeviceWhenProcessingDone() {
    return showMessageOnRemoteDeviceWhenProcessingDone;
  }


  private static byte[] readImageFile(File imageToRecognize) throws IOException {
    return FileUtils.readFile(imageToRecognize);
  }

}
