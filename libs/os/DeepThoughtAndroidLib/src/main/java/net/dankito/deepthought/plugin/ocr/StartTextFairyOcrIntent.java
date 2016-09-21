package net.dankito.deepthought.plugin.ocr;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.net.Uri;

import net.dankito.deepthought.communication.model.DoOcrConfiguration;
import net.dankito.deepthought.communication.model.OcrSource;
import net.dankito.deepthought.data.model.FileLink;
import net.dankito.deepthought.util.file.FileUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Created by ganymed on 06/02/16.
 */
public class StartTextFairyOcrIntent extends Intent {

  private static final Logger log = LoggerFactory.getLogger(StartTextFairyOcrIntent.class);


  public StartTextFairyOcrIntent(Context context, ResolveInfo resolveInfo, DoOcrConfiguration configuration) {
    ActivityInfo activityInfo = resolveInfo.activityInfo;
    setComponent(new ComponentName(activityInfo.packageName, activityInfo.name));
    setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

    putExtra(Constants.CALLING_APPLICATION_PACKAGE_NAME_EXTRA_NAME, context.getPackageName());

    setConfiguration(configuration);
  }

  protected void setConfiguration(DoOcrConfiguration configuration) {
    if(configuration.getSource() == OcrSource.CaptureImage) {
      putExtra(Constants.OCR_SOURCE_EXTRA_NAME, Constants.OCR_SOURCE_CAPTURE_IMAGE);
    }
    else if(configuration.getSource() == OcrSource.SelectAnExistingImageOnDevice) {
      putExtra(Constants.OCR_SOURCE_EXTRA_NAME, Constants.OCR_SOURCE_SELECT_AN_EXISTING_IMAGE_FROM_DEVICE);
    }
    else if(configuration.getSource() == OcrSource.RecognizeFromUri) {
      setImageUri(configuration);
    }
    else {
      putExtra(Constants.OCR_SOURCE_EXTRA_NAME, Constants.OCR_SOURCE_ASK_USER);
    }

    putExtra(Constants.SHOW_SETTINGS_UI_EXTRA_NAME, configuration.showSettingsUi());

  }

  protected void setImageUri(DoOcrConfiguration configuration) {
    Uri imageUri = Uri.parse(configuration.getImageUri());
    if(imageUri == null && configuration.getImageToRecognize() != null) {
      imageUri = saveImageToTempFile(configuration);
    }

    if(imageUri != null) {
      putExtra(Constants.OCR_SOURCE_EXTRA_NAME, Constants.OCR_SOURCE_RECOGNIZE_FROM_URI);
      putExtra(Constants.IMAGE_TO_RECOGNIZE_URI_EXTRA_NAME, imageUri.toString());
    }
    else {
      // TODO: what if imageUri is null? We should cancel the process right here
    }
  }

  protected Uri saveImageToTempFile(DoOcrConfiguration configuration) {
    if(configuration.getImageToRecognize() != null) {
      try {
        String fileExtension = ".jpg";
        if(configuration.getImageUri() != null) {
          fileExtension = FileUtils.getFileExtension(configuration.getImageUri());
        }

        File tempFile = File.createTempFile("image_for_ocr_", fileExtension);
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
