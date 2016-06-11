package net.dankito.deepthought.communication.listener;

import net.dankito.deepthought.communication.messages.request.RequestWithAsynchronousResponse;

/**
 * Created by ganymed on 20/11/15.
 */
public interface AsynchronousResponseListener<TRequest extends RequestWithAsynchronousResponse, TResponse> {

  void responseReceived(TRequest request, TResponse response);

}
