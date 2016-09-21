package net.dankito.deepthought.plugin.ocr;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;

import net.dankito.deepthought.communication.model.DoOcrConfiguration;
import net.dankito.deepthought.data.contentextractor.ContentExtractOptions;
import net.dankito.deepthought.data.contentextractor.ocr.OcrContentExtractorBase;
import net.dankito.deepthought.data.contentextractor.ocr.RecognizeTextListener;

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
  public ContentExtractOptions createExtractOptionsForUrl(String url) {
    return null;
  }

  @Override
  public boolean canCaptureImage() {
    return resolveInfo != null && context != null;
  }

  @Override
  protected void recognizeText(DoOcrConfiguration configuration, RecognizeTextListener listener) {
    try {
      OcrResultBroadcastReceiver ocrResultBroadcastReceiver = new OcrResultBroadcastReceiver(context, listener);

      Intent intent = new StartTextFairyOcrIntent(context, resolveInfo, configuration);

      context.startActivity(intent);
    } catch(Exception ex) {
      log.error("Could not start OcrContentExtractor plugin", ex);
    }
  }

}
