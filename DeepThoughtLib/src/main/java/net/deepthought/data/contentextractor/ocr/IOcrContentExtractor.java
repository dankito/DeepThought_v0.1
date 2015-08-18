package net.deepthought.data.contentextractor.ocr;

import net.deepthought.data.contentextractor.IContentExtractor;

/**
 * Created by ganymed on 25/04/15.
 */
public interface IOcrContentExtractor extends IContentExtractor {

  public boolean canCaptureImage();

  public void captureImagesAndRecognizeTextAsync(RecognizeTextListener listener);

}
