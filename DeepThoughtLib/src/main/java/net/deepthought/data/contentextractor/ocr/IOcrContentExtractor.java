package net.deepthought.data.contentextractor.ocr;

import net.deepthought.communication.model.CaptureImageOrDoOcrConfiguration;
import net.deepthought.data.contentextractor.IContentExtractor;

/**
 * Created by ganymed on 25/04/15.
 */
public interface IOcrContentExtractor extends IContentExtractor {

  void recognizeTextAsync(CaptureImageOrDoOcrConfiguration configuration, RecognizeTextListener listener);

  boolean canCaptureImage();

  void captureImagesAndRecognizeTextAsync(RecognizeTextListener listener);

}
