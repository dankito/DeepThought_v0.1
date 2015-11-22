package net.deepthought.communication.listener;

import net.deepthought.communication.messages.request.RequestWithAsynchronousResponse;
import net.deepthought.data.contentextractor.ocr.CaptureImageResult;

/**
 * Created by ganymed on 21/11/15.
 */
public interface CaptureImageResultListener extends  AsynchronousResponseListener<RequestWithAsynchronousResponse, CaptureImageResult> {

}
