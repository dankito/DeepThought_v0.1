package net.deepthought.communication.listener;

import net.deepthought.communication.messages.request.DoOcrOnImageRequest;
import net.deepthought.communication.messages.request.RequestWithAsynchronousResponse;
import net.deepthought.communication.messages.request.StopRequestWithAsynchronousResponse;

/**
 * Created by ganymed on 23/08/15.
 */
public interface CaptureImageOrDoOcrListener {

  void captureImage(RequestWithAsynchronousResponse request);

  void captureImageAndDoOcr(RequestWithAsynchronousResponse request);

  void doOcrOnImage(DoOcrOnImageRequest request);

  void scanBarcode(RequestWithAsynchronousResponse request);

  void stopCaptureImageOrDoOcr(StopRequestWithAsynchronousResponse request);
}
