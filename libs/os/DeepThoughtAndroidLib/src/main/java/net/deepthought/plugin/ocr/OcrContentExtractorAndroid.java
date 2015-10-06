package net.deepthought.plugin.ocr;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.net.Uri;

import net.deepthought.communication.model.DoOcrConfiguration;
import net.deepthought.data.contentextractor.ClipboardContent;
import net.deepthought.data.contentextractor.ContentExtractOption;
import net.deepthought.data.contentextractor.CreateEntryListener;
import net.deepthought.data.contentextractor.ocr.OcrContentExtractorBase;
import net.deepthought.data.contentextractor.ocr.RecognizeTextListener;
import net.deepthought.data.model.FileLink;
import net.deepthought.util.file.FileUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Created by ganymed on 18/08/15.
 */
public class OcrContentExtractorAndroid extends OcrContentExtractorBase {

  public final static String INTENT_KEY_CAPTURE_IMAGE = "CaptureImage";

  public final static String INTENT_KEY_IMAGE_TO_RECOGNIZE_URI = "ImageUri";

  public final static String INTENT_KEY_SHOW_SETTINGS_UI = "ShowSettingsUi";

  public final static String INTENT_KEY_SHOW_MESSAGE_ON_REMOTE_DEVICE_WHEN_PROCESSING_DONE = "ShowMessageOnRemoteDeviceWhenProcessingDone";


  private final static Logger log = LoggerFactory.getLogger(OcrContentExtractorAndroid.class);


  protected Context context;

  protected ResolveInfo resolveInfo;


  public OcrContentExtractorAndroid(Context context, ResolveInfo pluginResolveInfo) {
    this.context = context;
    this.resolveInfo = pluginResolveInfo;
  }


  @Override
  public int getSupportedPluginSystemVersion() {
    return 1;
  }

  @Override
  public String getPluginVersion() {
    return "0.1";
  }


  @Override
  public boolean canCreateEntryFromUrl(String url) {
    return false;
  }

  @Override
  public void createEntryFromUrl(String url, CreateEntryListener listener) {

  }

  @Override
  public ContentExtractOption canCreateEntryFromClipboardContent(ClipboardContent clipboardContent) {
    return ContentExtractOption.CanNotExtractContent;
  }

  @Override
  public void createEntryFromClipboardContent(ContentExtractOption contentExtractOption, CreateEntryListener listener) {

  }

  @Override
  public boolean canCaptureImage() {
    return resolveInfo != null && context != null;
  }

  @Override
  protected void captureImagesAndRecognizeText(RecognizeTextListener listener) {
    try {
      Intent intent = createIntentToStartOcrPlugin(true, true, listener);

      context.startActivity(intent);
    } catch(Exception ex) {
      log.error("Could not start OcrContentExtractor plugin", ex);
    }
  }

  @Override
  protected void recognizeText(DoOcrConfiguration configuration, RecognizeTextListener listener) {
    try {
      Intent intent = createIntentToStartOcrPlugin(true, configuration.showSettingsUi(), listener);
      intent.putExtra(INTENT_KEY_SHOW_MESSAGE_ON_REMOTE_DEVICE_WHEN_PROCESSING_DONE, configuration.showMessageOnRemoteDeviceWhenProcessingDone());

      Uri imageUri = Uri.parse(configuration.getImageUri());
      if(imageUri == null && configuration.getImageToRecognize() != null)
        imageUri = saveImageToTempFile(configuration);

      if(imageUri != null)
        intent.putExtra(INTENT_KEY_IMAGE_TO_RECOGNIZE_URI, imageUri.toString());

      context.startActivity(intent);
    } catch(Exception ex) {
      log.error("Could not start OcrContentExtractor plugin", ex);
    }
  }

  protected Intent createIntentToStartOcrPlugin(boolean captureImage, boolean showSettingsUi, RecognizeTextListener listener) {
    OcrResultBroadcastReceiver ocrResultBroadcastReceiver = new OcrResultBroadcastReceiver(context, listener);

    ActivityInfo activityInfo = resolveInfo.activityInfo;
    Intent intent = new Intent();
    intent.setComponent(new ComponentName(activityInfo.packageName, activityInfo.name));
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

    intent.putExtra(INTENT_KEY_CAPTURE_IMAGE, captureImage);
    intent.putExtra(INTENT_KEY_SHOW_SETTINGS_UI, showSettingsUi);

    return intent;
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
