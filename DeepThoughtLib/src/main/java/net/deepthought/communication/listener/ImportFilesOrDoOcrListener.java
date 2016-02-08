package net.deepthought.communication.listener;

import net.deepthought.communication.messages.request.DoOcrRequest;
import net.deepthought.communication.messages.request.ImportFilesRequest;
import net.deepthought.communication.messages.request.RequestWithAsynchronousResponse;
import net.deepthought.communication.messages.request.StopRequestWithAsynchronousResponse;

/**
 * Created by ganymed on 23/08/15.
 */
public interface ImportFilesOrDoOcrListener {

  void importFiles(ImportFilesRequest request);

  void doOcr(DoOcrRequest request);

  void scanBarcode(RequestWithAsynchronousResponse request);

  void stopCaptureImageOrDoOcr(StopRequestWithAsynchronousResponse request);
}
