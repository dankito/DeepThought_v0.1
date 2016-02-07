package net.deepthought.communication.messages;

import net.deepthought.communication.CommunicatorResponseListener;
import net.deepthought.communication.Constants;
import net.deepthought.communication.messages.request.MultipartRequest;
import net.deepthought.communication.messages.request.Request;
import net.deepthought.communication.messages.response.Response;
import net.deepthought.data.persistence.deserializer.DeserializationResult;
import net.deepthought.data.persistence.json.JsonIoJsonHelper;
import net.deepthought.data.persistence.serializer.SerializationResult;
import net.deepthought.util.IThreadPool;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by ganymed on 20/11/15.
 */
public class MessagesDispatcher implements IMessagesDispatcher {

  private final static Logger log = LoggerFactory.getLogger(MessagesDispatcher.class);


  protected IThreadPool threadPool;


  public MessagesDispatcher(IThreadPool threadPool) {
    this.threadPool = threadPool;
  }


  @Override
  public void sendMessageAsync(String address, Request request, CommunicatorResponseListener listener) {
    sendMessageAsync(address, request, Response.class, listener);
  }

  @Override
  public void sendMessageAsync(final String address, final Request request, final Class<? extends Response> responseClass, final CommunicatorResponseListener listener) {
    threadPool.runTaskAsync(new Runnable() {
      @Override
      public void run() {
        Response response = sendMessage(address, request, responseClass);
        if (listener != null)
          listener.responseReceived(response);
      }
    });
  }

  protected Response sendMessage(String address, Request request, Class<? extends Response> responseClass) {
    try {
      HttpEntity postEntity = createPostBody(request);

      return sendMessage(address, postEntity, responseClass);
    } catch(Exception ex) {
      log.error("Could not send message to address " + address + " for Request " + request, ex);
    }

    return null;
  }

  protected HttpEntity createPostBody(Request request) throws Exception {
    SerializationResult result = JsonIoJsonHelper.generateJsonString(request);
    if (result.successful() == false) {
      log.error("Could not generate Json from Request", result.getError());
      throw new Exception("Could not serialize Request " + request + " to Json", result.getError());
    }

    StringEntity postEntity = new StringEntity(result.getSerializationResult(), Constants.MessagesCharsetName);
    postEntity.setContentType(Constants.JsonMimeType);

    return postEntity;
  }


  @Override
  public void sendMultipartMessageAsync(String address, MultipartRequest request, CommunicatorResponseListener listener) {
    sendMultipartMessageAsync(address, request, Response.class, listener);
  }

  @Override
  public void sendMultipartMessageAsync(final String address, final MultipartRequest request, final Class<? extends Response> responseClass, final CommunicatorResponseListener listener) {
    threadPool.runTaskAsync(new Runnable() {
      @Override
      public void run() {
        Response response = sendMultipartMessage(address, request, responseClass);
        if (listener != null)
          listener.responseReceived(response);
      }
    });
  }

  protected Response sendMultipartMessage(String address, MultipartRequest request, Class<? extends Response> responseClass) {
    try {
      HttpEntity postEntity = createMultipartPostBody(request);

      sendMessage(address, postEntity, responseClass);
    } catch(Exception ex) {
      log.error("Could not send message to address " + address + " for Request " + request, ex);
    }

    return null;
  }

  protected HttpEntity createMultipartPostBody(MultipartRequest request) throws Exception {
    MultipartEntity postEntity = new MultipartEntity();

    for(MultipartPart part : request.getParts()) {
      if(part.getType() == MultipartType.Text) {
        addTextualPart(postEntity, part);
      }
      else if(part.getType() == MultipartType.Binary) {
        postEntity.addPart(part.getPartName(), new ByteArrayBody((byte[])part.getData(), "ToDo-SetFileName.tmp"));
      }
    }

    return postEntity;
  }

  protected void addTextualPart(MultipartEntity postEntity, MultipartPart part) throws Exception {
    String text = null;
    if(part.getData() instanceof String)
      text = (String)part.getData();
    else {
      SerializationResult result = JsonIoJsonHelper.generateJsonString(part.getData());
      if (result.successful() == false) {
        log.error("Could not generate Json from Request", result.getError());
        throw new Exception("Could not serialize Multipart data " + part.getData() + " to Json", result.getError());
      }
      text = result.getSerializationResult();
    }

    postEntity.addPart(part.getPartName(), new StringBody(text, Constants.MessagesCharset));
  }


  protected Response sendMessage(String address, HttpEntity postEntity, Class<? extends Response> responseClass) throws IOException {
    DefaultHttpClient httpClient = new DefaultHttpClient();
    HttpPost postRequest = new HttpPost(address);
    postRequest.setEntity(postEntity);

    HttpResponse response = httpClient.execute(postRequest);
    HttpEntity entity = response.getEntity();
    log.debug("Request Handled for url " + address + " ?: " + response.getStatusLine());

    String responseString = EntityUtils.toString(entity);
    httpClient.getConnectionManager().shutdown();

    return deserializeResponse(responseString, responseClass);
  }

  protected Response deserializeResponse(String responseString, Class<? extends Response> responseClass) {
    DeserializationResult deserializationResult = JsonIoJsonHelper.parseJsonString(responseString, responseClass);
    if(deserializationResult.successful())
      return (Response)deserializationResult.getResult();

    return null;
  }

}
