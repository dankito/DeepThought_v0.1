package net.deepthought.data.contentextractor.ocr;

import net.deepthought.data.contentextractor.ClipboardContent;
import net.deepthought.data.contentextractor.ContentExtractOption;
import net.deepthought.data.contentextractor.CreateEntryListener;
import net.deepthought.util.Localization;
import net.deepthought.util.file.FileUtils;

import java.io.File;

import javafx.scene.image.Image;

/**
 * Created by ganymed on 18/08/15.
 */
public abstract class OcrContentExtractorBase implements IOcrContentExtractor {

  @Override
  public String getName() {
    return Localization.getLocalizedString("ocr.content.extractor");
  }

  @Override
  public boolean canCreateEntryFromUrl(String url) {
    return FileUtils.isImageFile(FileUtils.getMimeType(url));
  }

  @Override
  public void createEntryFromUrlAsync(final String url, final CreateEntryListener listener) {
    new Thread(new Runnable() {
      @Override
      public void run() {
        createEntryFromUrl(url, listener);
      }
    }).start();
  }

  protected void createEntryFromUrl(String url, CreateEntryListener listener) {

  }

  @Override
  public ContentExtractOption canCreateEntryFromClipboardContent(ClipboardContent clipboardContent) {
    if(clipboardContent.hasImage()) {
      Image image = clipboardContent.getImage();
      return new ContentExtractOption(this, image, true);
    }

    if(clipboardContent.hasFiles()) {
      for(File file : clipboardContent.getFiles()) { // TODO: what about other files if one of the first files already succeed?
        if(canCreateEntryFromUrl(file.getAbsolutePath()))
          return new ContentExtractOption(this, file.getAbsolutePath(), true);
      }
    }

    if(clipboardContent.hasUrl()) {
      if (canCreateEntryFromUrl(clipboardContent.getUrl()))
        return new ContentExtractOption(this, clipboardContent.getUrl(), true);
    }

    if(clipboardContent.hasString()) {
      if (canCreateEntryFromUrl(clipboardContent.getString()))
        return new ContentExtractOption(this, clipboardContent.getString(), true);
    }

    return ContentExtractOption.CanNotExtractContent;
  }

  @Override
  public void createEntryFromClipboardContentAsync(final ContentExtractOption contentExtractOption, final CreateEntryListener listener) {
    new Thread(new Runnable() {
      @Override
      public void run() {
        createEntryFromClipboardContent(contentExtractOption, listener);
      }
    }).start();
  }

  protected void createEntryFromClipboardContent(ContentExtractOption contentExtractOption, CreateEntryListener listener) {

  }


  @Override
  public boolean canCaptureImage() {
    return false;
  }

  @Override
  public void captureImagesAndRecognizeTextAsync(final RecognizeTextListener listener) {
    new Thread(new Runnable() {
      @Override
      public void run() {
        captureImagesAndRecognizeText(listener);
      }
    }).start();
  }

  protected void captureImagesAndRecognizeText(RecognizeTextListener listener) {

  }

}
