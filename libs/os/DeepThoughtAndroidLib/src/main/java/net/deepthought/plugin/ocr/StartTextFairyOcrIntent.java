package net.deepthought.plugin.ocr;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.net.Uri;

import net.deepthought.communication.model.DoOcrConfiguration;
import net.deepthought.communication.model.OcrSource;
import net.deepthought.data.model.FileLink;
import net.deepthought.util.file.FileUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Created by ganymed on 06/02/16.
 */
public class StartTextFairyOcrIntent extends Intent {

  private static final Logger log = LoggerFactory.getLogger(StartTextFairyOcrIntent.class);


  public StartTextFairyOcrIntent(ResolveInfo resolveInfo, DoOcrConfiguration configuration) {
    ActivityInfo activityInfo = resolveInfo.activityInfo;
    setComponent(new ComponentName(activityInfo.packageName, activityInfo.name));
    setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

    setConfiguration(configuration);
  }

  protected void setConfiguration(DoOcrConfiguration configuration) {
    if(configuration.getSource() == OcrSource.CaptureImage) {
      putExtra(Constants.INTENT_KEY_RECOGNITION_SOURCE, Constants.RECOGNITION_SOURCE_CAPTURE_IMAGE);
    }
    else if(configuration.getSource() == OcrSource.ChoseImageFromGallery) {
      putExtra(Constants.INTENT_KEY_RECOGNITION_SOURCE, Constants.RECOGNITION_SOURCE_GET_FROM_GALLERY);
    }
    else if(configuration.getSource() == OcrSource.RecognizeFromUri) {
      setImageUri(configuration);
    }
    else {
      putExtra(Constants.INTENT_KEY_RECOGNITION_SOURCE, Constants.RECOGNITION_SOURCE_ASK_USER);
    }

    putExtra(Constants.INTENT_KEY_SHOW_SETTINGS_UI, configuration.showSettingsUi());

  }

  protected void setImageUri(DoOcrConfiguration configuration) {
    Uri imageUri = Uri.parse(configuration.getImageUri());
    if(imageUri == null && configuration.getImageToRecognize() != null) {
      imageUri = saveImageToTempFile(configuration);
    }

    if(imageUri != null) {
      putExtra(Constants.INTENT_KEY_RECOGNITION_SOURCE, Constants.RECOGNITION_SOURCE_RECOGNIZE_FROM_URI);
      putExtra(Constants.INTENT_KEY_IMAGE_TO_RECOGNIZE_URI, imageUri.toString());
    }
    else {
      // TODO: what if imageUri is null? We should cancel the process right here
    }
  }

  protected Uri saveImageToTempFile(DoOcrConfiguration configuration) {
    if(configuration.getImageToRecognize() != null) {
      try {
        File tempFile = File.createTempFile("image_for_ocr_", ".image");
        tempFile.deleteOnExit();
        FileUtils.writeToFile(configuration.getImageToRecognize(), new FileLink(tempFile.getAbsolutePath()));

        return Uri.fromFile(tempFile);
      } catch (Exception ex) {
        log.error("Could not write ImageData of length " + configuration.getImageToRecognize().length + " to temp file", ex);
      }
    }

    return null;
  }

}
