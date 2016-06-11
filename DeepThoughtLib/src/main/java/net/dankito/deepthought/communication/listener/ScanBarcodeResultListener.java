package net.dankito.deepthought.communication.listener;

import net.dankito.deepthought.communication.messages.request.RequestWithAsynchronousResponse;
import net.dankito.deepthought.communication.messages.response.ScanBarcodeResultResponse;

/**
 * Created by ganymed on 21/11/15.
 */
public interface ScanBarcodeResultListener extends AsynchronousResponseListener<RequestWithAsynchronousResponse, ScanBarcodeResultResponse> {

}
