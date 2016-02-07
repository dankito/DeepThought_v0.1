package net.deepthought.communication.listener;

import net.deepthought.communication.messages.request.DoOcrRequest;
import net.deepthought.communication.messages.response.OcrResultResponse;

/**
 * Created by ganymed on 21/11/15.
 */
public interface OcrResultListener extends AsynchronousResponseListener<DoOcrRequest, OcrResultResponse> {

}
