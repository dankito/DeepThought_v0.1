package net.deepthought.communication;

import net.deepthought.Application;
import net.deepthought.communication.listener.AskForDeviceRegistrationListener;
import net.deepthought.communication.listener.CaptureImageOrDoOcrResponseListener;
import net.deepthought.communication.listener.CommunicatorListener;
import net.deepthought.communication.listener.MessagesReceiverListener;
import net.deepthought.communication.listener.ResponseListener;
import net.deepthought.communication.messages.AskForDeviceRegistrationRequest;
import net.deepthought.communication.messages.AskForDeviceRegistrationResponseMessage;
import net.deepthought.communication.messages.CaptureImageOrDoOcrRequest;
import net.deepthought.communication.messages.CaptureImageResultResponse;
import net.deepthought.communication.messages.GenericRequest;
import net.deepthought.communication.messages.MultipartPart;
import net.deepthought.communication.messages.MultipartRequest;
import net.deepthought.communication.messages.MultipartType;
import net.deepthought.communication.messages.OcrResultResponse;
import net.deepthought.communication.messages.Request;
import net.deepthought.communication.messages.RequestWithAsynchronousResponse;
import net.deepthought.communication.messages.Response;
import net.deepthought.communication.messages.ResponseValue;
import net.deepthought.communication.messages.StopCaptureImageOrDoOcrRequest;
import net.deepthought.communication.model.ConnectedDevice;
import net.deepthought.communication.model.DoOcrConfiguration;
import net.deepthought.communication.model.HostInfo;
import net.deepthought.data.contentextractor.ocr.CaptureImageResult;
import net.deepthought.data.contentextractor.ocr.TextRecognitionResult;
import net.deepthought.data.model.User;
import net.deepthought.data.persistence.deserializer.DeserializationResult;
import net.deepthought.data.persistence.json.JsonIoJsonHelper;
import net.deepthought.data.persistence.serializer.SerializationResult;
import net.deepthought.util.Localization;
import net.deepthought.util.file.FileUtils;

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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ganymed on 20/08/15.
 */
public class Communicator {

  private final static Logger log = LoggerFactory.getLogger(Communicator.class);


  protected IDeepThoughtsConnector connector;

  protected CommunicatorListener communicatorListener;

  protected Map<AskForDeviceRegistrationRequest, AskForDeviceRegistrationListener> askForDeviceRegistrationListeners = new HashMap<>();

  protected Map<RequestWithAsynchronousResponse, CaptureImageOrDoOcrResponseListener> captureImageOrDoOcrListeners = new HashMap<>();


  public Communicator(IDeepThoughtsConnector connector, CommunicatorListener communicatorListener) {
    this.connector = connector;
    this.communicatorListener = communicatorListener;

    connector.addMessagesReceiverListener(messagesReceiverListener);
  }


  public void askForDeviceRegistration(HostInfo serverInfo, final AskForDeviceRegistrationListener listener) {
    String address = Addresses.getAskForDeviceRegistrationAddress(serverInfo.getIpAddress(), serverInfo.getPort());

    User user = Application.getLoggedOnUser();
    final AskForDeviceRegistrationRequest request = AskForDeviceRegistrationRequest.fromUserAndDevice(user, Application.getApplication().getLocalDevice());
    if(listener != null)
      askForDeviceRegistrationListeners.put(request, listener);

    sendMessageAsync(address, request, new CommunicatorResponseListener() {
      @Override
      public void responseReceived(Response communicatorResponse) {
        // user choice isn't send directly as response anymore as a) asking user is an asynchronous operation which cannot be await synchronously and b) connection may timeout when waiting
        // TODO: check if Response contains an error and notify user accordingly
        dispatchResponse(request, communicatorResponse);
      }
    });
  }

  public void sendAskForDeviceRegistrationResponse(final AskForDeviceRegistrationRequest request, final AskForDeviceRegistrationResponseMessage response, final ResponseListener listener) {
    String address = Addresses.getSendAskForDeviceRegistrationResponseAddress(request.getAddress(), request.getPort());
    response.setRequestMessageId(request.getMessageId());

    sendMessageAsync(address, response, new CommunicatorResponseListener() {
      @Override
      public void responseReceived(Response communicatorResponse) {
        dispatchResponse(response, communicatorResponse, listener);

        if(response.allowsRegistration() && communicatorResponse.getResponseValue() == ResponseValue.Ok)
          communicatorListener.serverAllowedDeviceRegistration(request, response);
      }
    });
  }

  public void notifyRemoteWeHaveConnected(ConnectedDevice connectedDevice) {
    String address = Addresses.getNotifyRemoteWeHaveConnectedAddress(connectedDevice.getAddress(), connectedDevice.getMessagesPort());
    ConnectedDevice self = ConnectedDevice.createSelfInstance();
    final Request request = new GenericRequest<ConnectedDevice>(self);

    sendMessageAsync(address, request, new CommunicatorResponseListener() {
      @Override
      public void responseReceived(Response communicatorResponse) {
        dispatchResponse(request, communicatorResponse);
      }
    });
  }

  public void sendHeartbeat(ConnectedDevice connectedDevice, final ResponseListener listener) {
    String address = Addresses.getHeartbeatAddress(connectedDevice.getAddress(), connectedDevice.getMessagesPort());
    ConnectedDevice self = ConnectedDevice.createSelfInstance();
    final Request request = new GenericRequest<ConnectedDevice>(self);

    sendMessageAsync(address, request, new CommunicatorResponseListener() {
      @Override
      public void responseReceived(Response communicatorResponse) {
        dispatchResponse(request, communicatorResponse, listener);
      }
    });
  }


  public void startCaptureImage(ConnectedDevice deviceToDoTheJob, CaptureImageOrDoOcrResponseListener listener) {
    startCaptureImageOrDoOcr(deviceToDoTheJob, true, false, listener);
  }

  public void startCaptureImageAndDoOcr(ConnectedDevice deviceToDoTheJob, CaptureImageOrDoOcrResponseListener listener) {
    startCaptureImageOrDoOcr(deviceToDoTheJob, true, true, listener);
  }

  protected void startCaptureImageOrDoOcr(ConnectedDevice deviceToDoTheJob, boolean captureImage, boolean doOcr, final CaptureImageOrDoOcrResponseListener listener) {
    String address = Addresses.getStartCaptureImageAndDoOcrAddress(deviceToDoTheJob.getAddress(), deviceToDoTheJob.getMessagesPort());
    final CaptureImageOrDoOcrRequest request = new CaptureImageOrDoOcrRequest(NetworkHelper.getIPAddressString(true), connector.getMessageReceiverPort(), captureImage, doOcr);

    if(listener != null)
      captureImageOrDoOcrListeners.put(request, listener);

    sendMessageAsync(address, request, new CommunicatorResponseListener() {
      @Override
      public void responseReceived(Response communicatorResponse) {
        dispatchResponse(request, communicatorResponse); // TODO: if an error occurred inform caller
      }
    });
  }


  public void startDoOcr(ConnectedDevice deviceToDoTheJob, File imageToRecognize, boolean showSettingsUi, boolean showMessageOnRemoteDeviceWhenProcessingDone, CaptureImageOrDoOcrResponseListener listener) {
    try {
      byte[] imageData = FileUtils.readFile(imageToRecognize);
      startDoOcr(deviceToDoTheJob, imageData, showSettingsUi, showMessageOnRemoteDeviceWhenProcessingDone, listener);
    } catch(Exception ex) {
      log.error("Could not read Image file " + imageToRecognize.getAbsolutePath(), ex);
      if(listener != null)
        listener.ocrResult(TextRecognitionResult.createErrorOccurredResult(Localization.getLocalizedString("could.not.read.file", imageToRecognize.getAbsolutePath(), ex.getLocalizedMessage())));
    }
  }

  public void startDoOcr(ConnectedDevice deviceToDoTheJob, byte[] imageToRecognize, boolean showSettingsUi, boolean showMessageOnRemoteDeviceWhenProcessingDone, CaptureImageOrDoOcrResponseListener listener) {
    startDoOcr(deviceToDoTheJob, new DoOcrConfiguration(imageToRecognize, showSettingsUi, showMessageOnRemoteDeviceWhenProcessingDone), listener);
  }

  public void startDoOcr(ConnectedDevice deviceToDoTheJob, DoOcrConfiguration configuration, final CaptureImageOrDoOcrResponseListener listener) {
    String address = Addresses.getStartCaptureImageAndDoOcrAddress(deviceToDoTheJob.getAddress(), deviceToDoTheJob.getMessagesPort());

    byte[] imageData = configuration.getAndResetImageToRecognize();
    final MultipartRequest request = new MultipartRequest(NetworkHelper.getIPAddressString(true), connector.getMessageReceiverPort(), new MultipartPart[] {
                                          new MultipartPart<DoOcrConfiguration>(ConnectorMessagesCreator.DoOcrMultipartKeyConfiguration, MultipartType.Text, configuration),
                                          new MultipartPart<byte[]>(ConnectorMessagesCreator.DoOcrMultipartKeyImage, MultipartType.Binary, imageData) });

    if(listener != null)
      captureImageOrDoOcrListeners.put(request, listener);

    sendMultipartMessageAsync(address, request, new CommunicatorResponseListener() {
      @Override
      public void responseReceived(Response communicatorResponse) {
        dispatchResponse(request, communicatorResponse); // TODO: if an error occurred inform caller
      }
    });
  }


  public void sendCaptureImageResult(final CaptureImageOrDoOcrRequest request, final byte[] imageData, final ResponseListener listener) {
    String address = Addresses.getCaptureImageResultAddress(request.getAddress(), request.getPort());
    CaptureImageResult result = new CaptureImageResult(imageData != null && imageData.length > 0);

    final MultipartRequest multiPartResponse = new MultipartRequest(request.getMessageId(), NetworkHelper.getIPAddressString(true), connector.getMessageReceiverPort(), new MultipartPart[] {
        new MultipartPart<CaptureImageResult>(ConnectorMessagesCreator.CaptureImageResultMultipartKeyResponse, MultipartType.Text, result),
        new MultipartPart<byte[]>(ConnectorMessagesCreator.CaptureImageResultMultipartKeyImage, MultipartType.Binary, imageData) });

    sendMultipartMessageAsync(address, multiPartResponse, new CommunicatorResponseListener() {
      @Override
      public void responseReceived(Response communicatorResponse) {
        dispatchResponse(multiPartResponse, communicatorResponse, listener);
      }
    });
  }

  public void sendOcrResult(final CaptureImageOrDoOcrRequest request, final TextRecognitionResult ocrResult, final ResponseListener listener) {
    String address = Addresses.getOcrResultAddress(request.getAddress(), request.getPort());
    final OcrResultResponse response = new OcrResultResponse(ocrResult, request.getMessageId());

    sendMessageAsync(address, response, new CommunicatorResponseListener() {
      @Override
      public void responseReceived(Response communicatorResponse) {
        dispatchResponse(response, communicatorResponse, listener);
      }
    });
  }

  public void stopCaptureImage(CaptureImageOrDoOcrResponseListener listenerToUnset /*important as it otherwise would cause memory leaks*/, final ResponseListener listener) {
    stopCaptureImageAndDoOcr(listenerToUnset, listener);
  }

  public void stopCaptureImageAndDoOcr(CaptureImageOrDoOcrResponseListener listenerToUnset /*important as it otherwise would cause memory leaks*/, final ResponseListener listener) {
    RequestWithAsynchronousResponse captureRequest = findCaptureImageOrDoOcrRequestForListener(listenerToUnset);
    removeFromCaptureImageOrDoOcrListenersMap(captureRequest);
    if(captureRequest == null) {
      log.error("stopCaptureImageOrDoOcr() has been called but no CaptureImageOrDoOcrRequest has been found for listenerToUnset");
      return;
    }

    String address = Addresses.getStopCaptureImageAndDoOcrAddress(captureRequest.getAddress(), captureRequest.getPort());
    final StopCaptureImageOrDoOcrRequest stopRequest = new StopCaptureImageOrDoOcrRequest(captureRequest.getMessageId());

    sendMessageAsync(address, stopRequest, new CommunicatorResponseListener() {
      @Override
      public void responseReceived(Response communicatorResponse) {
        dispatchResponse(stopRequest, communicatorResponse, listener);
      }
    });
  }

  protected RequestWithAsynchronousResponse findCaptureImageOrDoOcrRequestForListener(CaptureImageOrDoOcrResponseListener listenerToUnset) {
    RequestWithAsynchronousResponse request = null;
    for(Map.Entry<RequestWithAsynchronousResponse, CaptureImageOrDoOcrResponseListener> entry : captureImageOrDoOcrListeners.entrySet()) {
      if(entry.getValue() == listenerToUnset) {
        request = entry.getKey();
        break;
      }
    }
    return request;
  }


  protected void sendMessageAsync(String address, Request request, CommunicatorResponseListener listener) {
    sendMessageAsync(address, request, Response.class, listener);
  }

  protected void sendMessageAsync(final String address, final Request request, final Class<? extends Response> responseClass, final CommunicatorResponseListener listener) {
    Application.getThreadPool().runTaskAsync(new Runnable() {
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


  protected void sendMultipartMessageAsync(String address, MultipartRequest request, CommunicatorResponseListener listener) {
    sendMultipartMessageAsync(address, request, Response.class, listener);
  }

  protected void sendMultipartMessageAsync(final String address, final MultipartRequest request, final Class<? extends Response> responseClass, final CommunicatorResponseListener listener) {
    Application.getThreadPool().runTaskAsync(new Runnable() {
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

  protected void dispatchResponse(Request request, Response response) {
    dispatchResponse(request, response, null);
  }

  protected void dispatchResponse(Request request, Response response, ResponseListener listener) {
    try {
      communicatorListener.responseReceived(request, response);
    } catch(Exception ex) { log.error("An error occurred calling Communicator's communicatorListener (so the error certainly is in the listener method)", ex); }

    if(listener != null) {
      try {
        listener.responseReceived(request, response);
      } catch (Exception ex2) { log.error("An error occurred calling method's ResponseListener (so the error certainly is in the listener method)", ex2); }
    }
  }


  protected MessagesReceiverListener messagesReceiverListener = new MessagesReceiverListener() {

    @Override
    public void registerDeviceRequestRetrieved(AskForDeviceRegistrationRequest request) {

    }

    @Override
    public void askForDeviceRegistrationResponseReceived(AskForDeviceRegistrationResponseMessage message) {
      Integer messageId = message.getRequestMessageId();

      for(AskForDeviceRegistrationRequest request : askForDeviceRegistrationListeners.keySet()) {
        if(messageId.equals(request.getMessageId())) {
          AskForDeviceRegistrationListener listener = askForDeviceRegistrationListeners.get(request);
          listener.serverResponded(message);

          askForDeviceRegistrationListeners.remove(request);
          break;
        }
      }
    }

    @Override
    public void notifyRegisteredDeviceConnected(ConnectedDevice connectedDevice) {

    }

    @Override
    public void deviceIsStillConnected(ConnectedDevice connectedDevice) {

    }

    @Override
    public void startCaptureImageOrDoOcr(CaptureImageOrDoOcrRequest request) {

    }

    @Override
    public void captureImageResult(CaptureImageResultResponse response) {
      Integer messageId = response.getMessageId();

      for(RequestWithAsynchronousResponse request : captureImageOrDoOcrListeners.keySet()) {
        if(messageId.equals(request.getMessageId())) {
          CaptureImageOrDoOcrResponseListener listener = captureImageOrDoOcrListeners.get(request);
          listener.captureImageResult(response.getResult());

          if(response.getResult().isDone())
            removeFromCaptureImageOrDoOcrListenersMap(request);
          break;
        }
      }
    }

    @Override
    public void ocrResult(OcrResultResponse response) {
      Integer messageId = response.getMessageId();

      for(RequestWithAsynchronousResponse request : captureImageOrDoOcrListeners.keySet()) {
        if(messageId.equals(request.getMessageId())) {
          CaptureImageOrDoOcrResponseListener listener = captureImageOrDoOcrListeners.get(request);
          listener.ocrResult(response.getTextRecognitionResult());

          if(response.getTextRecognitionResult().isDone())
            removeFromCaptureImageOrDoOcrListenersMap(request);
          break;
        }
      }
    }

    @Override
    public void stopCaptureImageOrDoOcr(StopCaptureImageOrDoOcrRequest request) {

    }
  };

  protected void removeFromCaptureImageOrDoOcrListenersMap(RequestWithAsynchronousResponse request) {
    captureImageOrDoOcrListeners.remove(request);
  }

}
