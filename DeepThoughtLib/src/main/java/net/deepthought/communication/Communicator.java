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
import net.deepthought.communication.messages.GenericRequest;
import net.deepthought.communication.messages.OcrResultResponse;
import net.deepthought.communication.messages.Request;
import net.deepthought.communication.messages.Response;
import net.deepthought.communication.messages.ResponseValue;
import net.deepthought.communication.messages.StopCaptureImageOrDoOcrRequest;
import net.deepthought.communication.model.ConnectedDevice;
import net.deepthought.communication.model.HostInfo;
import net.deepthought.data.contentextractor.ocr.TextRecognitionResult;
import net.deepthought.data.model.User;
import net.deepthought.data.persistence.deserializer.DeserializationResult;
import net.deepthought.data.persistence.json.JsonIoJsonHelper;
import net.deepthought.data.persistence.serializer.SerializationResult;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

  protected Map<CaptureImageOrDoOcrRequest, CaptureImageOrDoOcrResponseListener> captureImageOrDoOcrListeners = new HashMap<>();


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

  public void startCaptureImage(ConnectedDevice deviceToDoTheJob, CaptureImageOrDoOcrResponseListener listener) {
    startCaptureImageAndDoOcr(deviceToDoTheJob, true, false, listener);
  }

  public void startCaptureImageAndDoOcr(ConnectedDevice deviceToDoTheJob, CaptureImageOrDoOcrResponseListener listener) {
    startCaptureImageAndDoOcr(deviceToDoTheJob, true, true, listener);
  }

  protected void startCaptureImageAndDoOcr(ConnectedDevice deviceToDoTheJob, boolean captureImage, boolean doOcr, final CaptureImageOrDoOcrResponseListener listener) {
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

  public void sendCapturedImage(final CaptureImageOrDoOcrRequest request, final byte[] imageBytes, final ResponseListener listener) {
//    String address = Addresses.getOcrResultAddress(request.getAddress(), request.getPort());
//    final OcrResultResponse response = new OcrResultResponse(ocrResult, request.getMessageId());
//
//    sendMessageAsync(address, response, new CommunicatorResponseListener() {
//      @Override
//      public void responseReceived(Response communicatorResponse) {
//        dispatchResponse(response, communicatorResponse, listener);
//      }
//    });
  }

  public void stopCaptureImage(CaptureImageOrDoOcrResponseListener listenerToUnset /*important as it otherwise would cause memory leaks*/, final ResponseListener listener) {
    stopCaptureImageAndDoOcr(listenerToUnset, listener);
  }

  public void stopCaptureImageAndDoOcr(CaptureImageOrDoOcrResponseListener listenerToUnset /*important as it otherwise would cause memory leaks*/, final ResponseListener listener) {
    CaptureImageOrDoOcrRequest captureRequest = findCaptureImageOrDoOcrRequestForListener(listenerToUnset);
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

  protected CaptureImageOrDoOcrRequest findCaptureImageOrDoOcrRequestForListener(CaptureImageOrDoOcrResponseListener listenerToUnset) {
    CaptureImageOrDoOcrRequest request = null;
    for(Map.Entry<CaptureImageOrDoOcrRequest, CaptureImageOrDoOcrResponseListener> entry : captureImageOrDoOcrListeners.entrySet()) {
      if(entry.getValue() == listenerToUnset) {
        request = entry.getKey();
        captureImageOrDoOcrListeners.remove(request);
        break;
      }
    }
    return request;
  }


  protected void sendMessageAsync(String address, Request request, CommunicatorResponseListener listener) {
    sendMessageAsync(address, request, Response.class, listener);
  }

  protected void sendMessageAsync(final String address, final Request request, final Class<? extends Response> responseClass, final CommunicatorResponseListener listener) {
    new Thread(new Runnable() {
      @Override
      public void run() {
        Response response = sendMessage(address, request, responseClass);
        if(listener != null)
          listener.responseReceived(response);
      }
    }).start();
  }

  protected Response sendMessage(String address, Request request, Class<? extends Response> responseClass) {
    try {
      HttpClient httpClient = new DefaultHttpClient();

      HttpPost postRequest = new HttpPost(address);
      SerializationResult result = JsonIoJsonHelper.generateJsonString(request);
      if (result.successful() == false) {
        log.error("Could not generate Json from Request", result.getError()); // TODO: what to do in this case?
      }

      StringEntity postEntity = new StringEntity(result.getSerializationResult(), Constants.MessagesCharsetName);
      postEntity.setContentType(Constants.JsonMimeType);
      postRequest.setEntity(postEntity);

      HttpResponse response = httpClient.execute(postRequest);
      HttpEntity entity = response.getEntity();
      log.debug("Request Handled for url " + address + " ?: " + response.getStatusLine());

      String responseString = EntityUtils.toString(entity);
      httpClient.getConnectionManager().shutdown();

      return deserializeResponse(responseString, responseClass);
    } catch(Exception ex) {
      log.error("Could not send message to address " + address + " for Request " + request, ex);
    }

    return null;
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
    public void startCaptureImageOrDoOcr(CaptureImageOrDoOcrRequest request) {

    }

    @Override
    public void ocrResult(OcrResultResponse response) {
      Integer messageId = response.getMessageId();

      for(CaptureImageOrDoOcrRequest request : captureImageOrDoOcrListeners.keySet()) {
        if(messageId.equals(request.getMessageId())) {
          CaptureImageOrDoOcrResponseListener listener = captureImageOrDoOcrListeners.get(request);
          listener.ocrResult(response.getTextRecognitionResult());

          break;
        }
      }
    }

    @Override
    public void stopCaptureImageOrDoOcr(StopCaptureImageOrDoOcrRequest request) {

    }
  };

}
