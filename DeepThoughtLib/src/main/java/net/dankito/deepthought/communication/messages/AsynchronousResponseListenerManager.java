package net.dankito.deepthought.communication.messages;

import net.dankito.deepthought.communication.messages.request.RequestWithAsynchronousResponse;
import net.dankito.deepthought.communication.listener.AsynchronousResponseListener;

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

  public boolean removeListenerForMessageId(int messageId) {
    RequestWithAsynchronousResponse request = getRequestWithAsynchronousResponseForMessageId(messageId);
    if(request != null) {
      return removeListenerForRequest(request);
    }

    return false;
  }

  public boolean removeListenerForRequest(RequestWithAsynchronousResponse request) {
    return asynchronousResponseListeners.remove(request) != null;
  }


  public int getRegisteredListenersCount() {
    return asynchronousResponseListeners.size();
  }

}
