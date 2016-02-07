package net.deepthought.communication.listener;

import net.deepthought.communication.messages.request.RequestWithAsynchronousResponse;
import net.deepthought.communication.messages.response.ScanBarcodeResultResponse;

/**
 * Created by ganymed on 21/11/15.
 */
public interface ScanBarcodeResultListener extends AsynchronousResponseListener<RequestWithAsynchronousResponse, ScanBarcodeResultResponse> {

}
