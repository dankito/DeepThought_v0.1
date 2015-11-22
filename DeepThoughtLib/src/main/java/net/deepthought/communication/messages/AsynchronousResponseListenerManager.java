package net.deepthought.communication.messages;

import net.deepthought.communication.messages.request.RequestWithAsynchronousResponse;
import net.deepthought.communication.listener.AsynchronousResponseListener;

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


  public RequestWithAsynchronousResponse getRequestWithAsynchronousResponseForMessageId(int messageId) {
    for(RequestWithAsynchronousResponse request : asynchronousResponseListeners.keySet()) {
      if(request.getMessageId() == messageId) {
        return request;
      }
    }

    return null;
  }

  public <TRequest extends RequestWithAsynchronousResponse, TResponse> AsynchronousResponseListener<TRequest, TResponse> getListenerForMessageId(int messageId) {
    RequestWithAsynchronousResponse request = getRequestWithAsynchronousResponseForMessageId(messageId);
    if(request != null) {
      return asynchronousResponseListeners.get(request);
    }

    return null;
  }

  public <TRequest extends RequestWithAsynchronousResponse, TResponse> AsynchronousResponseListener<TRequest, TResponse> getAndRemoveListenerForMessageId(int messageId) {
    RequestWithAsynchronousResponse request = getRequestWithAsynchronousResponseForMessageId(messageId);
    if(request != null) {
      AsynchronousResponseListener<TRequest, TResponse> listener = asynchronousResponseListeners.get(request);
      removeListenerForRequest(request);
      return listener;
    }

    return null;
  }

  public void removeListenerForMessageId(int messageId) {
    RequestWithAsynchronousResponse request = getRequestWithAsynchronousResponseForMessageId(messageId);
    if(request != null) {
      removeListenerForRequest(request);
    }
  }

  public void removeListenerForRequest(RequestWithAsynchronousResponse request) {
    asynchronousResponseListeners.remove(request);
  }


  public int getRegisteredListenersCount() {
    return asynchronousResponseListeners.size();
  }

}
