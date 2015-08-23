package net.deepthought.communication.listener;

import net.deepthought.communication.messages.CaptureImageOrDoOcrRequest;
import net.deepthought.communication.messages.StopCaptureImageOrDoOcrRequest;

/**
 * Created by ganymed on 23/08/15.
 */
public interface CaptureImageOrDoOcrListener {

  void startCaptureImageOrDoOcr(CaptureImageOrDoOcrRequest request);

  void stopCaptureImageOrDoOcr(StopCaptureImageOrDoOcrRequest request);
}
