package net.deepthought.communication.messages;

import net.deepthought.communication.CommunicatorResponseListener;

/**
 * Created by ganymed on 20/11/15.
 */
public interface IMessagesDispatcher {
      void sendMessageAsync(String address, net.deepthought.communication.messages.request.Request request, CommunicatorResponseListener listener);

  void sendMessageAsync(String address, net.deepthought.communication.messages.request.Request request, Class<? extends net.deepthought.communication.messages.response.Response> responseClass, CommunicatorResponseListener listener);

  void sendMultipartMessageAsync(String address, net.deepthought.communication.messages.request.MultipartRequest request, CommunicatorResponseListener listener);

  void sendMultipartMessageAsync(String address, net.deepthought.communication.messages.request.MultipartRequest request, Class<? extends net.deepthought
      .communication.messages.response.Response> responseClass, CommunicatorResponseListener listener);
}
