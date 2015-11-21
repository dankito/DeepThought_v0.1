package net.deepthought.communication.messages;

import net.deepthought.communication.messages.request.RequestWithAsynchronousResponse;
import net.deepthought.communication.messages.response.AsynchronousResponseListener;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ganymed on 20/11/15.
 */
public class AsynchronousResponseListenerManager {

  protected Map<RequestWithAsynchronousResponse, AsynchronousResponseListener> asynchronousResponseListeners = new HashMap<>();


  public <TRequest extends RequestWithAsynchronousResponse, TResponse> void addListenerForResponse(RequestWithAsynchronousResponse request, AsynchronousResponseListener<TRequest, TResponse> listener) {
    asynchronousResponseListeners.put(request, listener);
  }

  public <TRequest extends RequestWithAsynchronousResponse, TResponse> AsynchronousResponseListener<TRequest, TResponse> getListenerForMessageId(int messageId) {
    RequestWithAsynchronousResponse request = findRequestWithAsynchronousResponseForMessageId(messageId);
    if(request != null) {
      return asynchronousResponseListeners.get(request);
    }

    return null;
  }

  public <TRequest extends RequestWithAsynchronousResponse, TResponse> AsynchronousResponseListener<TRequest, TResponse> getAndRemoveListenerForMessageId(int messageId) {
    RequestWithAsynchronousResponse request = findRequestWithAsynchronousResponseForMessageId(messageId);
    if(request != null) {
      AsynchronousResponseListener<TRequest, TResponse> listener = asynchronousResponseListeners.get(request);
      removeListenerForRequest(request);
      return listener;
    }

    return null;
  }

  public void removeListenerForMessageId(int messageId) {
    RequestWithAsynchronousResponse request = findRequestWithAsynchronousResponseForMessageId(messageId);
    if(request != null) {
      removeListenerForRequest(request);
    }
  }

  public void removeListenerForRequest(RequestWithAsynchronousResponse request) {
    asynchronousResponseListeners.remove(request);
  }

  protected RequestWithAsynchronousResponse findRequestWithAsynchronousResponseForMessageId(int messageId) {
    for(RequestWithAsynchronousResponse request : asynchronousResponseListeners.keySet()) {
      if(request.getMessageId() == messageId) {
        return request;
      }
    }

    return null;
  }

}
