package net.deepthought.communication.listener;

import net.deepthought.communication.messages.request.RequestWithAsynchronousResponse;
import net.deepthought.communication.messages.response.OcrResultResponse;

/**
 * Created by ganymed on 21/11/15.
 */
public interface CaptureImageAndDoOcrResultListener extends  AsynchronousResponseListener<RequestWithAsynchronousResponse, OcrResultResponse> {

}
