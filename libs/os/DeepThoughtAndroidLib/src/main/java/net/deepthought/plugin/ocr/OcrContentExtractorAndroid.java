package net.deepthought.plugin.ocr;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;

import net.deepthought.data.contentextractor.ocr.OcrContentExtractorBase;
import net.deepthought.data.contentextractor.ocr.RecognizeTextListener;
import net.deepthought.data.contentextractor.ClipboardContent;
import net.deepthought.data.contentextractor.ContentExtractOption;
import net.deepthought.data.contentextractor.CreateEntryListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ganymed on 18/08/15.
 */
public class OcrContentExtractorAndroid extends OcrContentExtractorBase {

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
      OcrResultBroadcastReceiver ocrResultBroadcastReceiver = new OcrResultBroadcastReceiver(context, listener);

      ActivityInfo activityInfo = resolveInfo.activityInfo;
      Intent intent = new Intent();
      intent.setComponent(new ComponentName(activityInfo.packageName, activityInfo.name));
      intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      context.startActivity(intent);
    } catch(Exception ex) {
      log.error("Could not start OcrContentExtractor plugin", ex);
    }
  }
}
