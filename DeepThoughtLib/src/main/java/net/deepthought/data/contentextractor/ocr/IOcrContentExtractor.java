package net.deepthought.data.contentextractor.ocr;

import net.deepthought.communication.model.DoOcrConfiguration;
import net.deepthought.data.contentextractor.IContentExtractor;

/**
 * Created by ganymed on 25/04/15.
 */
public interface IOcrContentExtractor extends IContentExtractor {

  void recognizeTextAsync(DoOcrConfiguration configuration, RecognizeTextListener listener);

  boolean canCaptureImage();

}
