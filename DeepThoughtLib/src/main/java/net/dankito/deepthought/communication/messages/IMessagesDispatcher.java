package net.dankito.deepthought.communication.messages;

import net.dankito.deepthought.communication.messages.request.MultipartRequest;
import net.dankito.deepthought.communication.messages.request.Request;
import net.dankito.deepthought.communication.messages.response.Response;
import net.dankito.deepthought.communication.CommunicatorResponseListener;

/**
 * Created by ganymed on 20/11/15.
 */
public interface IMessagesDispatcher {
      void sendMessageAsync(String address, Request request, CommunicatorResponseListener listener);

  void sendMessageAsync(String address, Request request, Class<? extends Response> responseClass, CommunicatorResponseListener listener);

  void sendMultipartMessageAsync(String address, MultipartRequest request, CommunicatorResponseListener listener);

  void sendMultipartMessageAsync(String address, MultipartRequest request, Class<? extends Response> responseClass, CommunicatorResponseListener listener);
}
