package net.deepthought.communication.listener;

import net.deepthought.data.contentextractor.ocr.CaptureImageResult;
import net.deepthought.data.contentextractor.ocr.TextRecognitionResult;

/**
 * Created by ganymed on 23/08/15.
 */
public interface CaptureImageOrDoOcrResponseListener {

  void captureImageResult(CaptureImageResult captureImageResult);

  void ocrResult(TextRecognitionResult ocrResult);

}
