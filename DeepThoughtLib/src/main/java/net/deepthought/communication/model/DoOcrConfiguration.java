package net.deepthought.communication.model;

import net.deepthought.util.file.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * Created by ganymed on 04/10/15.
 */
public class DoOcrConfiguration {

  protected byte[] imageToRecognize = null;

  protected String imageUri = null;

  protected boolean showSettingsUi = true;

  protected boolean showMessageOnRemoteDeviceWhenProcessingDone = false;



  public DoOcrConfiguration(byte[] imageToRecognize, boolean showSettingsUi) {
    this(imageToRecognize, showSettingsUi, false);
  }

  public DoOcrConfiguration(byte[] imageToRecognize, boolean showSettingsUi, boolean showMessageOnRemoteDeviceWhenProcessingDone) {
    this.imageToRecognize = imageToRecognize;
    this.showSettingsUi = showSettingsUi;
    this.showMessageOnRemoteDeviceWhenProcessingDone = showMessageOnRemoteDeviceWhenProcessingDone;
  }


  public byte[] getImageToRecognize() {
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

  public byte[] readBytesFromImageUri() throws IOException {
    if(imageToRecognize == null && imageUri != null)
      imageToRecognize = FileUtils.readFile(new File(imageUri));

    return imageToRecognize;
  }

  public boolean showSettingsUi() {
    return showSettingsUi;
  }

  public boolean showMessageOnRemoteDeviceWhenProcessingDone() {
    return showMessageOnRemoteDeviceWhenProcessingDone;
  }

}
