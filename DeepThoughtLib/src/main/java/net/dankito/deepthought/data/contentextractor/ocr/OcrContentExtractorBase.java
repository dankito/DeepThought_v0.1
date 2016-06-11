package net.dankito.deepthought.data.contentextractor.ocr;

import net.dankito.deepthought.communication.model.DoOcrConfiguration;
import net.dankito.deepthought.util.file.FileUtils;
import net.dankito.deepthought.Application;
import net.dankito.deepthought.plugin.IPlugin;
import net.dankito.deepthought.util.localization.Localization;

/**
 * Created by ganymed on 18/08/15.
 */
public abstract class OcrContentExtractorBase implements IOcrContentExtractor, IPlugin {

  @Override
  public int getSupportedPluginSystemVersion() {
    return 1;
  }

  @Override
  public String getName() {
    return Localization.getLocalizedString("ocr.content.extractor");
  }

  @Override
  public String getPluginVersion() {
    return "0.1";
  }

  @Override
  public boolean canCreateEntryFromUrl(String url) {
    return FileUtils.isImageFile(FileUtils.getMimeType(url));
  }


  @Override
  public void recognizeTextAsync(final DoOcrConfiguration configuration, final RecognizeTextListener listener) {
    Application.getThreadPool().runTaskAsync(new Runnable() {
      @Override
      public void run() {
        recognizeText(configuration, listener);
      }
    });
  }

  protected abstract void recognizeText(DoOcrConfiguration configuration, RecognizeTextListener listener);


  @Override
  public boolean canCaptureImage() {
    return false;
  }

}
