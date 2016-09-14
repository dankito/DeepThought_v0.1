package net.dankito.deepthought.communication;

import net.dankito.deepthought.communication.model.DoOcrConfiguration;
import net.dankito.deepthought.communication.listener.AskForDeviceRegistrationResultListener;
import net.dankito.deepthought.communication.listener.ImportFilesResultListener;
import net.dankito.deepthought.communication.listener.OcrResultListener;
import net.dankito.deepthought.communication.listener.ResponseListener;
import net.dankito.deepthought.communication.listener.ScanBarcodeResultListener;
import net.dankito.deepthought.communication.messages.AsynchronousResponseListenerManager;
import net.dankito.deepthought.communication.messages.IMessagesDispatcher;
import net.dankito.deepthought.communication.messages.request.AskForDeviceRegistrationRequest;
import net.dankito.deepthought.communication.messages.request.DoOcrRequest;
import net.dankito.deepthought.communication.messages.request.GenericRequest;
import net.dankito.deepthought.communication.messages.request.ImportFilesRequest;
import net.dankito.deepthought.communication.messages.request.Request;
import net.dankito.deepthought.communication.messages.request.RequestWithAsynchronousResponse;
import net.dankito.deepthought.communication.messages.response.AskForDeviceRegistrationResponse;
import net.dankito.deepthought.communication.messages.response.ImportFilesResultResponse;
import net.dankito.deepthought.communication.messages.response.OcrResultResponse;
import net.dankito.deepthought.communication.messages.response.Response;
import net.dankito.deepthought.communication.messages.response.ResponseCode;
import net.dankito.deepthought.communication.messages.response.ScanBarcodeResult;
import net.dankito.deepthought.communication.messages.response.ScanBarcodeResultResponse;
import net.dankito.deepthought.communication.model.ConnectedDevice;
import net.dankito.deepthought.communication.model.HostInfo;
import net.dankito.deepthought.communication.model.ImportFilesConfiguration;
import net.dankito.deepthought.communication.registration.IRegisteredDevicesManager;
import net.dankito.deepthought.data.contentextractor.ocr.ImportFilesResult;
import net.dankito.deepthought.data.contentextractor.ocr.TextRecognitionResult;
import net.dankito.deepthought.data.model.Device;
import net.dankito.deepthought.data.model.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ganymed on 20/08/15.
 */
public class Communicator {

  private final static Logger log = LoggerFactory.getLogger(Communicator.class);


  protected IMessagesDispatcher dispatcher = null;

  protected AsynchronousResponseListenerManager listenerManager = null;

  protected int messageReceiverPort;

  protected ConnectorMessagesCreator connectorMessagesCreator;

  protected IRegisteredDevicesManager registeredDevicesManager;


  public Communicator(CommunicatorConfig config) {
    this.dispatcher = config.getDispatcher();
    this.listenerManager = config.getListenerManager();
    this.messageReceiverPort = config.getMessageReceiverPort();
    this.connectorMessagesCreator = config.getConnectorMessagesCreator();
    this.registeredDevicesManager = config.getRegisteredDevicesManager();
  }


  public AskForDeviceRegistrationRequest askForDeviceRegistration(HostInfo serverInfo, User loggedOnUser, Device localDevice, final AskForDeviceRegistrationResultListener listener) {
    String address = Addresses.getAskForDeviceRegistrationAddress(serverInfo.getAddress(), serverInfo.getMessagesPort());

    final AskForDeviceRegistrationRequest request = createAskForDeviceRegistrationRequest(loggedOnUser, localDevice);
    listenerManager.addListenerForResponse(request, listener);

    dispatcher.sendMessageAsync(address, request, new CommunicatorResponseListener() {
      @Override
      public void responseReceived(Response communicatorResponse) {
        // user choice isn't send directly as response anymore as a) asking user is an asynchronous operation which cannot be await synchronously and b) connection may timeout when waiting
        // TODO: check if Response contains an error and notify user accordingly
        dispatchResponse(request, communicatorResponse);
      }
    });

    return request;
  }

  protected AskForDeviceRegistrationRequest createAskForDeviceRegistrationRequest(User loggedOnUser, Device localDevice) {
    return new AskForDeviceRegistrationRequest(loggedOnUser, localDevice, getIpAddressToSendResponseTo(), getMessageReceiverPort());
  }

  public void respondToAskForDeviceRegistrationRequest(final AskForDeviceRegistrationRequest request, final AskForDeviceRegistrationResponse response, final ResponseListener listener) {
    String address = Addresses.getAskForDeviceRegistrationResponseAddress(request.getAddress(), request.getPort());
    response.setRequestMessageId(request.getMessageId());

    dispatcher.sendMessageAsync(address, response, new CommunicatorResponseListener() {
      @Override
      public void responseReceived(Response communicatorResponse) {
        dispatchResponse(response, communicatorResponse, listener);

        // TODO: in order to keep Communicator generic try to avoid reference to IRegisteredDevicesManager
        if (response.allowsRegistration() && communicatorResponse.getResponseCode() == ResponseCode.Ok) {
          registeredDevicesManager.registerDevice(request, response.getUseSendersUserInformation());
        }
      }
    });
  }

  public void notifyRemoteWeHaveConnected(HostInfo connectedDevice) {
    notifyRemoteWeHaveConnected(connectedDevice, getLocalHostInfo());
  }

  public void notifyRemoteWeHaveConnected(HostInfo connectedDevice, ConnectedDevice localHost) {
    String address = Addresses.getNotifyRemoteWeHaveConnectedAddress(connectedDevice.getAddress(), connectedDevice.getMessagesPort());
    final Request request = new GenericRequest<ConnectedDevice>(localHost);

    dispatcher.sendMessageAsync(address, request, new CommunicatorResponseListener() {
      @Override
      public void responseReceived(Response communicatorResponse) {
        dispatchResponse(request, communicatorResponse);
      }
    });
  }

  public void acknowledgeWeHaveConnected(HostInfo connectedDevice) {
    acknowledgeWeHaveConnected(connectedDevice, getLocalHostInfo());
  }

  public void acknowledgeWeHaveConnected(HostInfo connectedDevice, ConnectedDevice localHost) {
    String address = Addresses.getAcknowledgeWeHaveConnectedAddress(connectedDevice.getAddress(), connectedDevice.getMessagesPort());
    final Request request = new GenericRequest<ConnectedDevice>(localHost);

    dispatcher.sendMessageAsync(address, request, new CommunicatorResponseListener() {
      @Override
      public void responseReceived(Response communicatorResponse) {
        dispatchResponse(request, communicatorResponse);
      }
    });
  }

  public void notifyRemoteWeAreGoingToDisconnect(HostInfo connectedDevice) {
    notifyRemoteWeAreGoingToDisconnect(connectedDevice, getLocalHostInfo());
  }

  public void notifyRemoteWeAreGoingToDisconnect(HostInfo connectedDevice, ConnectedDevice localHost) {
    String address = Addresses.getNotifyRemoteWeAreGoingToDisconnectAddress(connectedDevice.getAddress(), connectedDevice.getMessagesPort());
    final Request request = new GenericRequest<ConnectedDevice>(localHost);

    dispatcher.sendMessageAsync(address, request, new CommunicatorResponseListener() {
      @Override
      public void responseReceived(Response communicatorResponse) {
        dispatchResponse(request, communicatorResponse);
      }
    });
  }

  public void sendHeartbeat(ConnectedDevice connectedDevice, final ResponseListener listener) {
    sendHeartbeat(connectedDevice, getLocalHostInfo(), listener);
  }

  public void sendHeartbeat(ConnectedDevice connectedDevice, ConnectedDevice localHost, final ResponseListener listener) {
    String address = Addresses.getHeartbeatAddress(connectedDevice.getAddress(), connectedDevice.getMessagesPort());
    final Request request = new GenericRequest<ConnectedDevice>(localHost);

    dispatcher.sendMessageAsync(address, request, new CommunicatorResponseListener() {
      @Override
      public void responseReceived(Response communicatorResponse) {
        dispatchResponse(request, communicatorResponse, listener);
      }
    });
  }


  public ImportFilesRequest startImportFiles(ConnectedDevice deviceToDoTheJob, ImportFilesConfiguration configuration, ImportFilesResultListener listener) {
    String address = Addresses.getStartImportFilesAddress(deviceToDoTheJob.getAddress(), deviceToDoTheJob.getMessagesPort());
    final ImportFilesRequest request = new ImportFilesRequest(getIpAddressToSendResponseTo(), getMessageReceiverPort(), configuration);

    listenerManager.addListenerForResponse(request, listener);

    dispatcher.sendMultipartMessageAsync(address, request, new CommunicatorResponseListener() {
      @Override
      public void responseReceived(Response communicatorResponse) {
        dispatchResponse(request, communicatorResponse); // TODO: if an error occurred inform caller
      }
    });

    return request;
  }

//  public void stopImportFiles(int messageId, ConnectedDevice deviceToDoTheJob, final ResponseListener listener) {
//    if(listenerManager.removeListenerForMessageId(messageId) == false) {
//      log.error("stopCaptureImage() has been called but there was no Listener registered for MessageId " + messageId);
//    }
//
//    String address = Addresses.getStopCaptureImageAddress(deviceToDoTheJob.getAddress(), deviceToDoTheJob.getMessagesPort());
//    final StopRequestWithAsynchronousResponse stopRequest = new StopRequestWithAsynchronousResponse(messageId);
//
//    dispatcher.sendMessageAsync(address, stopRequest, new CommunicatorResponseListener() {
//      @Override
//      public void responseReceived(Response communicatorResponse) {
//        dispatchResponse(stopRequest, communicatorResponse, listener);
//      }
//    });
//  }

  public void respondToImportFilesRequest(RequestWithAsynchronousResponse request, ImportFilesResult result, final ResponseListener listener) {
    String address = Addresses.getImportFilesResultAddress(request.getAddress(), request.getPort());
    final ImportFilesResultResponse response = new ImportFilesResultResponse(result, request.getMessageId());

    dispatcher.sendMultipartMessageAsync(address, response, new CommunicatorResponseListener() {
      @Override
      public void responseReceived(Response communicatorResponse) {
        dispatchResponse(response, communicatorResponse, listener);
      }
    });
  }


  public DoOcrRequest startDoOcr(ConnectedDevice deviceToDoTheJob, DoOcrConfiguration configuration, final OcrResultListener listener) {
    String address = Addresses.getDoOcrOnImageAddress(deviceToDoTheJob.getAddress(), deviceToDoTheJob.getMessagesPort());
    final DoOcrRequest request = new DoOcrRequest(getIpAddressToSendResponseTo(), getMessageReceiverPort(), configuration);

    listenerManager.addListenerForResponse(request, listener);

    dispatcher.sendMultipartMessageAsync(address, request, new CommunicatorResponseListener() {
      @Override
      public void responseReceived(Response communicatorResponse) {
        dispatchResponse(request, communicatorResponse); // TODO: if an error occurred inform caller
      }
    });

    return request;
  }

//  public void stopDoOcr(int messageId, ConnectedDevice deviceToDoTheJob, final ResponseListener listener) {
//    if(listenerManager.removeListenerForMessageId(messageId) == false) {
//      log.error("stopCaptureImageOrDoOcr() has been called but there was no Listener registered for MessageId " + messageId);
//    }
//
//    String address = Addresses.getStopCaptureImageAndDoOcrAddress(deviceToDoTheJob.getAddress(), deviceToDoTheJob.getMessagesPort());
//    final StopRequestWithAsynchronousResponse stopRequest = new StopRequestWithAsynchronousResponse(messageId);
//
//    dispatcher.sendMessageAsync(address, stopRequest, new CommunicatorResponseListener() {
//      @Override
//      public void responseReceived(Response communicatorResponse) {
//        dispatchResponse(stopRequest, communicatorResponse, listener);
//      }
//    });
//  }


  public void respondToDoOcrRequest(final DoOcrRequest request, final TextRecognitionResult ocrResult, final ResponseListener listener) {
    String address = Addresses.getOcrResultAddress(request.getAddress(), request.getPort());
    final OcrResultResponse response = new OcrResultResponse(ocrResult, request.getMessageId());

    dispatcher.sendMessageAsync(address, response, new CommunicatorResponseListener() {
      @Override
      public void responseReceived(Response communicatorResponse) {
        dispatchResponse(response, communicatorResponse, listener);
      }
    });
  }



  public RequestWithAsynchronousResponse startScanBarcode(ConnectedDevice deviceToDoTheJob, ScanBarcodeResultListener listener) {
    String address = Addresses.getStartScanBarcodeAddress(deviceToDoTheJob.getAddress(), deviceToDoTheJob.getMessagesPort());
    final RequestWithAsynchronousResponse request = new RequestWithAsynchronousResponse(getIpAddressToSendResponseTo(), getMessageReceiverPort());

    listenerManager.addListenerForResponse(request, listener);

    dispatcher.sendMessageAsync(address, request, new CommunicatorResponseListener() {
      @Override
      public void responseReceived(Response communicatorResponse) {
        dispatchResponse(request, communicatorResponse); // TODO: if an error occurred inform caller
      }
    });

    return request;
  }

  public void respondToScanBarcodeRequest(RequestWithAsynchronousResponse request, ScanBarcodeResult result, final ResponseListener listener) {
    String address = Addresses.getScanBarcodeResultAddress(request.getAddress(), request.getPort());
    final ScanBarcodeResultResponse response = new ScanBarcodeResultResponse(result, request.getMessageId());

    dispatcher.sendMessageAsync(address, response, new CommunicatorResponseListener() {
      @Override
      public void responseReceived(Response communicatorResponse) {
        dispatchResponse(response, communicatorResponse, listener);
      }
    });
  }


  protected void dispatchResponse(Request request, Response response) {
    dispatchResponse(request, response, null);
  }

  protected void dispatchResponse(Request request, Response response, ResponseListener listener) {
    if(listener != null) {
      try {
        listener.responseReceived(request, response);
      } catch (Exception ex2) { log.error("An error occurred calling method's ResponseListener (so the error certainly is in the listener method)", ex2); }
    }
  }


  protected String getIpAddressToSendResponseTo() {
    return NetworkHelper.getIPAddressString(true);
  }

  protected int getMessageReceiverPort() {
    return messageReceiverPort;
  }

  protected void setMessageReceiverPort(int messageReceiverPort) {
    this.messageReceiverPort = messageReceiverPort;
  }

  protected ConnectedDevice getLocalHostInfo() {
    return connectorMessagesCreator.getLocalHostDevice();
  }

}
