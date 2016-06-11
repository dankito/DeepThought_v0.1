package net.dankito.deepthought.data.contentextractor.ocr;

import net.dankito.deepthought.communication.model.DoOcrConfiguration;
import net.dankito.deepthought.data.contentextractor.IContentExtractor;

/**
 * Created by ganymed on 25/04/15.
 */
public interface IOcrContentExtractor extends IContentExtractor {

  void recognizeTextAsync(DoOcrConfiguration configuration, RecognizeTextListener listener);

  boolean canCaptureImage();

}
