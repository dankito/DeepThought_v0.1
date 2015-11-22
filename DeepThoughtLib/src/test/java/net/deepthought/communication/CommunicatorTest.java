package net.deepthought.communication;

import net.deepthought.Application;
import net.deepthought.communication.listener.AskForDeviceRegistrationListener;
import net.deepthought.communication.listener.CaptureImageAndDoOcrResultListener;
import net.deepthought.communication.listener.CaptureImageOrDoOcrListener;
import net.deepthought.communication.listener.CaptureImageResultListener;
import net.deepthought.communication.listener.DoOcrOnImageResultListener;
import net.deepthought.communication.listener.MessagesReceiverListener;
import net.deepthought.communication.messages.DeepThoughtMessagesReceiverConfig;
import net.deepthought.communication.messages.MessagesReceiver;
import net.deepthought.communication.messages.request.AskForDeviceRegistrationRequest;
import net.deepthought.communication.messages.request.CaptureImageOrDoOcrRequest;
import net.deepthought.communication.messages.request.DoOcrOnImageRequest;
import net.deepthought.communication.messages.request.GenericRequest;
import net.deepthought.communication.messages.request.Request;
import net.deepthought.communication.messages.request.RequestWithAsynchronousResponse;
import net.deepthought.communication.messages.request.StopRequestWithAsynchronousResponse;
import net.deepthought.communication.messages.response.AskForDeviceRegistrationResponseMessage;
import net.deepthought.communication.messages.response.CaptureImageResultResponse;
import net.deepthought.communication.messages.response.OcrResultResponse;
import net.deepthought.communication.model.ConnectedDevice;
import net.deepthought.communication.model.DoOcrConfiguration;
import net.deepthought.communication.model.HostInfo;
import net.deepthought.communication.registration.UserDeviceRegistrationRequestListener;
import net.deepthought.data.contentextractor.ocr.CaptureImageResult;
import net.deepthought.data.contentextractor.ocr.TextRecognitionResult;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by ganymed on 20/08/15.
 */
public class CommunicatorTest extends CommunicationTestBase {

  private final static Logger log = LoggerFactory.getLogger(CommunicatorTest.class);

  protected final static String TestDeviceId = "Cuddle";

  protected final static String TestIpAddress = "0.0.0.0";

  protected final static int CommunicatorPort = 54321;

  protected final static int TestMessageId = 4711;

  protected final static String TestRecognizedText = "Cuddle";


//  protected Communicator communicator = null;

  protected net.deepthought.communication.messages.MessagesReceiver receiver = null;

  protected ConnectedDevice localHost = null;

  protected CountDownLatch waitLatch = new CountDownLatch(1);

  protected Map<String, Request> receivedRequests = new HashMap<>();


  @Override
  public void setup() throws IOException {
    super.setup();

//    communicator = new Communicator(new MessagesDispatcher(), null, null);
    receiver = new MessagesReceiver(new DeepThoughtMessagesReceiverConfig(CommunicatorPort, connector.getListenerManager()), receiverListener);
    receiver.start();
    localHost = new ConnectedDevice(TestDeviceId, TestIpAddress, CommunicatorPort);

    try { Thread.sleep(200); } catch(Exception ex) { } // it is very critical that server is fully started therefore wait some time
  }

  @After
  public void tearDown() {
    receiver.stop();

    super.tearDown();
  }


  @Test
  public void askForDeviceRegistration_ListenerMethodAllowDeviceToRegisterGetsCalled() {
    final AtomicBoolean methodCalled = new AtomicBoolean(false);
    final CountDownLatch waitLatch = new CountDownLatch(1);

    connector.openUserDeviceRegistrationServer(new UserDeviceRegistrationRequestListener() {
      @Override
      public void registerDeviceRequestRetrieved(AskForDeviceRegistrationRequest request) {
        methodCalled.set(true);
        waitLatch.countDown();
      }
    });

    communicator.askForDeviceRegistration(createLocalHostServerInfo(), null);

    try { waitLatch.await(2, TimeUnit.SECONDS); } catch(Exception ex) { }
    connector.closeUserDeviceRegistrationServer();

    Assert.assertTrue(methodCalled.get());
  }

  @Test
  public void askForDeviceRegistration_ServerResponseIsReceived() {
    final List<AskForDeviceRegistrationResponseMessage> responses = new ArrayList<>();
    final CountDownLatch waitLatch = new CountDownLatch(1);

    connector.openUserDeviceRegistrationServer(null);

    communicator.askForDeviceRegistration(createLocalHostServerInfo(), new AskForDeviceRegistrationListener() {
      @Override
      public void serverResponded(AskForDeviceRegistrationResponseMessage response) {
        responses.add(response);
        waitLatch.countDown();
      }
    });

    try { waitLatch.await(3, TimeUnit.SECONDS); } catch(Exception ex) { }
    connector.closeUserDeviceRegistrationServer();

    Assert.assertEquals(1, responses.size());
  }

  @Test
  public void askForDeviceRegistration_RegistrationIsProhibitedByServer_RegistrationDeniedResponseIsReceived() {
    final List<AskForDeviceRegistrationResponseMessage> responses = new ArrayList<>();
    final CountDownLatch waitLatch = new CountDownLatch(1);

    connector.openUserDeviceRegistrationServer(new UserDeviceRegistrationRequestListener() {
      @Override
      public void registerDeviceRequestRetrieved(AskForDeviceRegistrationRequest request) {
        communicator.sendAskForDeviceRegistrationResponse(request, AskForDeviceRegistrationResponseMessage.Deny, null);
      }
    });

    communicator.askForDeviceRegistration(createLocalHostServerInfo(), new net.deepthought.communication.listener.AskForDeviceRegistrationListener() {
      @Override
      public void serverResponded(AskForDeviceRegistrationResponseMessage response) {
        responses.add(response);
        waitLatch.countDown();
      }
    });

    try { waitLatch.await(3, TimeUnit.SECONDS); } catch(Exception ex) { }
    connector.closeUserDeviceRegistrationServer();

    Assert.assertFalse(responses.get(0).allowsRegistration());
  }

  @Test
  public void askForDeviceRegistration_ServerAllowsRegistration_RegistrationAllowedResponseIsReceived() {
    final List<AskForDeviceRegistrationResponseMessage> responses = new ArrayList<>();
    final CountDownLatch waitLatch = new CountDownLatch(1);

    connector.openUserDeviceRegistrationServer(new UserDeviceRegistrationRequestListener() {
      @Override
      public void registerDeviceRequestRetrieved(AskForDeviceRegistrationRequest request) {
        communicator.sendAskForDeviceRegistrationResponse(request, AskForDeviceRegistrationResponseMessage.createAllowRegistrationResponse(true,
            Application.getLoggedOnUser(), Application.getApplication().getLocalDevice()), null);
      }
    });

    communicator.askForDeviceRegistration(createLocalHostServerInfo(), new AskForDeviceRegistrationListener() {
      @Override
      public void serverResponded(AskForDeviceRegistrationResponseMessage response) {
        responses.add(response);
        waitLatch.countDown();
      }
    });

    try { waitLatch.await(3, TimeUnit.SECONDS); } catch(Exception ex) { }
    connector.closeUserDeviceRegistrationServer();

    Assert.assertTrue(responses.get(0).allowsRegistration());
  }

  @Test
  public void askForDeviceRegistrationDone_ListenerGetsRemoved() {
    final CountDownLatch waitLatch = new CountDownLatch(1);

    connector.openUserDeviceRegistrationServer(new UserDeviceRegistrationRequestListener() {
      @Override
      public void registerDeviceRequestRetrieved(AskForDeviceRegistrationRequest request) {
        communicator.sendAskForDeviceRegistrationResponse(request, AskForDeviceRegistrationResponseMessage.createAllowRegistrationResponse(true,
            Application.getLoggedOnUser(), Application.getApplication().getLocalDevice()), null);
      }
    });

    communicator.askForDeviceRegistration(createLocalHostServerInfo(), new AskForDeviceRegistrationListener() {
      @Override
      public void serverResponded(AskForDeviceRegistrationResponseMessage response) {
        waitLatch.countDown();
      }
    });

    Assert.assertEquals(1, communicator.askForDeviceRegistrationListeners.size());

    try { waitLatch.await(3, TimeUnit.SECONDS); } catch(Exception ex) { }
    connector.closeUserDeviceRegistrationServer();

    Assert.assertEquals(0, communicator.askForDeviceRegistrationListeners.size());
  }


  @Test
  public void notifyRemoteWeHaveConnected() throws IOException {
    communicator.notifyRemoteWeHaveConnected(localHost);

    waitTillListenerHasBeenCalled();

    GenericRequest request = (GenericRequest)assertThatCorrectMethodHasBeenCalled(Addresses.NotifyRemoteWeHaveConnectedMethodName, GenericRequest.class);
    Assert.assertTrue(request.getRequestBody() instanceof ConnectedDevice);
  }

  @Test
  public void sendHeartbeat() throws IOException {
    communicator.sendHeartbeat(localHost, null);

    waitTillListenerHasBeenCalled();

    GenericRequest request = (GenericRequest)assertThatCorrectMethodHasBeenCalled(Addresses.HeartbeatMethodName, GenericRequest.class);
    Assert.assertTrue(request.getRequestBody() instanceof ConnectedDevice);
  }



  @Test
  public void startCaptureImage_RequestIsReceived() {
    communicator.startCaptureImage(localHost, null);

    waitTillListenerHasBeenCalled();

    assertThatCorrectMethodHasBeenCalled(Addresses.StartCaptureImageMethodName, RequestWithAsynchronousResponse.class);
  }

  @Test
  public void respondToCaptureImageRequest_RequestIsReceived() throws IOException {
    byte[] imageData = getTestImage();
    RequestWithAsynchronousResponse request = new RequestWithAsynchronousResponse(TestMessageId, TestIpAddress, CommunicatorPort);
    communicator.respondToCaptureImageRequest(request, new CaptureImageResult(imageData), null);

    waitTillListenerHasBeenCalled();

    CaptureImageResultResponse response = (CaptureImageResultResponse)assertThatCorrectMethodHasBeenCalled(Addresses.CaptureImageResultMethodName, CaptureImageResultResponse.class);

    Assert.assertEquals(TestMessageId, response.getRequestMessageId());
    Assert.assertNotNull(response.getResult());
    Assert.assertNotNull(response.getResult().getImageUri());
    Assert.assertArrayEquals(imageData, response.getResult().getImageData());
  }

  @Test
  public void startCaptureImage_ResponseListenerGetsCalled() throws IOException {
    final AtomicBoolean hasResponseBeenReceived = new AtomicBoolean(false);
    final List<CaptureImageResultResponse> receivedResponseHolder = new ArrayList<>();

    RequestWithAsynchronousResponse request = communicator.startCaptureImage(localHost, new CaptureImageResultListener() {
      @Override
      public void responseReceived(RequestWithAsynchronousResponse requestWithAsynchronousResponse, CaptureImageResultResponse captureImageResult) {
        hasResponseBeenReceived.set(true);
        receivedResponseHolder.add(captureImageResult);
      }
    });

    waitTillListenerHasBeenCalled();
    resetWaitLatch();

    byte[] imageData = getTestImage();
    communicator.respondToCaptureImageRequest(request, new CaptureImageResult(imageData, true), null);

    waitTillListenerHasBeenCalled();

    Assert.assertEquals(2, receivedRequests.size());
    Assert.assertTrue(hasResponseBeenReceived.get());
    Assert.assertEquals(1, receivedResponseHolder.size());
    CaptureImageResultResponse result = receivedResponseHolder.get(0);
    Assert.assertArrayEquals(imageData, result.getResult().getImageData());
  }

  @Test
  public void startCaptureImage_ResponseListenerGetsRemovedFromListenerManager() throws IOException {
    RequestWithAsynchronousResponse request = communicator.startCaptureImage(localHost, null);

    waitTillListenerHasBeenCalled();
    resetWaitLatch();

    communicator.respondToCaptureImageRequest(request, new CaptureImageResult(getTestImage(), true), null);

    waitTillListenerHasBeenCalled();

    Assert.assertNull(communicator.listenerManager.getAndRemoveListenerForMessageId(request.getMessageId()));
    Assert.assertEquals(0, communicator.listenerManager.getRegisteredListenersCount());
  }


//  @Test
//  public void stopCaptureImage_RequestIsReceived() {
//    communicator.stopCaptureImage(TestMessageId, localHost, null);
//
//    waitTillListenerHasBeenCalled();
//
//    assertThatCorrectMethodHasBeenCalled(Addresses.StopCaptureImageMethodName, StopRequestWithAsynchronousResponse.class);
//  }
//
//  @Test
//  public void stopCaptureImage_ListenerGetsCalledCorrectly() {
//    final AtomicBoolean methodCalled = new AtomicBoolean(false);
//    final CountDownLatch waitLatch = new CountDownLatch(1);
//
//    connector.addCaptureImageOrDoOcrListener(new CaptureImageOrDoOcrListener() {
//      @Override
//      public void startCaptureImageOrDoOcr(CaptureImageOrDoOcrRequest request) {
//
//      }
//
//      @Override
//      public void stopCaptureImageOrDoOcr(StopRequestWithAsynchronousResponse request) {
//        methodCalled.set(true);
//        waitLatch.countDown();
//      }
//    });
//
//    RequestWithAsynchronousResponse request = communicator.startCaptureImage(localHost, null);
//    waitTillListenerHasBeenCalled();
//
//    communicator.stopCaptureImage(request.getMessageId(), localHost, null);
//    try { waitLatch.await(3, TimeUnit.SECONDS); } catch(Exception ex) { }
//
//    Assert.assertTrue(methodCalled.get());
//  }
//
//  @Test
//  public void stopCaptureImage_ListenerGetsRemoved() {
//    RequestWithAsynchronousResponse request = communicator.startCaptureImage(localHost, null);
//    waitTillListenerHasBeenCalled();
//    resetWaitLatch();
//
//    communicator.stopCaptureImage(request.getMessageId(), localHost, null);
//    waitTillListenerHasBeenCalled();
//
//    Assert.assertNull(communicator.listenerManager.getAndRemoveListenerForMessageId(request.getMessageId()));
//    Assert.assertEquals(0, communicator.listenerManager.getRegisteredListenersCount());
//  }



  @Test
  public void startCaptureImageAndDoOcr_RequestIsReceived() {
    communicator.startCaptureImageAndDoOcr(localHost, null);

    waitTillListenerHasBeenCalled();

    assertThatCorrectMethodHasBeenCalled(Addresses.StartCaptureImageAndDoOcrMethodName, RequestWithAsynchronousResponse.class);
  }

  @Test
  public void respondToCaptureImageAndDoOcrRequest_RequestIsReceived() {
    RequestWithAsynchronousResponse request = new RequestWithAsynchronousResponse(TestMessageId, TestIpAddress, CommunicatorPort);
    communicator.respondToCaptureImageAndDoOcrRequest(request, new TextRecognitionResult(TestRecognizedText), null);

    waitTillListenerHasBeenCalled();

    OcrResultResponse response = (OcrResultResponse)assertThatCorrectMethodHasBeenCalled(Addresses.OcrResultMethodName, OcrResultResponse.class);

    Assert.assertEquals(TestMessageId, response.getRequestMessageId());
    Assert.assertNotNull(response.getTextRecognitionResult());
    Assert.assertEquals(TestRecognizedText, response.getTextRecognitionResult().getRecognizedText());
  }

  @Test
  public void startCaptureImageAndDoOcr_ResponseListenerGetsCalled() {
    final AtomicBoolean hasResponseBeenReceived = new AtomicBoolean(false);
    final List<OcrResultResponse> receivedResponseHolder = new ArrayList<>();

    RequestWithAsynchronousResponse request = communicator.startCaptureImageAndDoOcr(localHost, new CaptureImageAndDoOcrResultListener() {
      @Override
      public void responseReceived(RequestWithAsynchronousResponse requestWithAsynchronousResponse, OcrResultResponse ocrResult) {
        hasResponseBeenReceived.set(true);
        receivedResponseHolder.add(ocrResult);
      }
    });

    waitTillListenerHasBeenCalled();
    resetWaitLatch();

    communicator.respondToCaptureImageAndDoOcrRequest(request, new TextRecognitionResult(TestRecognizedText), null);

    waitTillListenerHasBeenCalled();

    Assert.assertEquals(2, receivedRequests.size());
    Assert.assertTrue(hasResponseBeenReceived.get());
    Assert.assertEquals(1, receivedResponseHolder.size());
    OcrResultResponse result = receivedResponseHolder.get(0);
    Assert.assertEquals(TestRecognizedText, result.getTextRecognitionResult().getRecognizedText());
  }

  @Test
  public void startCaptureImageAndDoOcr_ResponseListenerGetsRemovedFromListenerManager() {
    RequestWithAsynchronousResponse request = communicator.startCaptureImageAndDoOcr(localHost, null);

    waitTillListenerHasBeenCalled();
    resetWaitLatch();

    communicator.respondToCaptureImageAndDoOcrRequest(request, new TextRecognitionResult(TestRecognizedText, true), null);

    waitTillListenerHasBeenCalled();

    Assert.assertNull(communicator.listenerManager.getAndRemoveListenerForMessageId(request.getMessageId()));
    Assert.assertEquals(0, communicator.listenerManager.getRegisteredListenersCount());
  }

  @Test
  public void startCaptureImageAndDoOcr_addCaptureImageOrDoOcrListenerIsCalled() {
    final AtomicBoolean methodCalled = new AtomicBoolean(false);
    final CountDownLatch waitLatch = new CountDownLatch(1);

    connector.addCaptureImageOrDoOcrListener(new CaptureImageOrDoOcrListener() {
      @Override
      public void startCaptureImageOrDoOcr(CaptureImageOrDoOcrRequest request) {
        methodCalled.set(true);
        waitLatch.countDown();
      }

      @Override
      public void stopCaptureImageOrDoOcr(StopRequestWithAsynchronousResponse request) {

      }
    });

    communicator.startCaptureImageAndDoOcr(localHost, null);

    try { waitLatch.await(2, TimeUnit.SECONDS); } catch(Exception ex) { }

    Assert.assertTrue(methodCalled.get());
  }


//  @Test
//  public void stopCaptureImageAndDoOcr_RequestIsReceived() {
//    communicator.stopCaptureImageAndDoOcr(TestMessageId, localHost, null);
//
//    waitTillListenerHasBeenCalled();
//
//    assertThatCorrectMethodHasBeenCalled(Addresses.StopCaptureImageAndDoOcrMethodName, StopRequestWithAsynchronousResponse.class);
//  }
//
//  @Test
//  public void stopCaptureImageAndDoOcr_ListenerGetsCalledCorrectly() {
//    final AtomicBoolean methodCalled = new AtomicBoolean(false);
//    final CountDownLatch waitLatch = new CountDownLatch(1);
//
//    connector.addCaptureImageOrDoOcrListener(new CaptureImageOrDoOcrListener() {
//      @Override
//      public void startCaptureImageOrDoOcr(CaptureImageOrDoOcrRequest request) {
//
//      }
//
//      @Override
//      public void stopCaptureImageOrDoOcr(StopRequestWithAsynchronousResponse request) {
//        methodCalled.set(true);
//        waitLatch.countDown();
//      }
//    });
//
//    RequestWithAsynchronousResponse request = communicator.startCaptureImageAndDoOcr(localHost, null);
//    waitTillListenerHasBeenCalled();
//
//    communicator.stopCaptureImageAndDoOcr(request.getMessageId(), localHost, null);
//    try { waitLatch.await(3, TimeUnit.SECONDS); } catch(Exception ex) { }
//
//    Assert.assertTrue(methodCalled.get());
//  }
//
//  @Test
//  public void stopCaptureImageAndDoOcr_ListenerGetsRemoved() {
//    RequestWithAsynchronousResponse request = communicator.startCaptureImageAndDoOcr(localHost, null);
//    waitTillListenerHasBeenCalled();
//    resetWaitLatch();
//
//    communicator.stopCaptureImageAndDoOcr(request.getMessageId(), localHost, null);
//    waitTillListenerHasBeenCalled();
//
//    Assert.assertNull(communicator.listenerManager.getAndRemoveListenerForMessageId(request.getMessageId()));
//    Assert.assertEquals(0, communicator.listenerManager.getRegisteredListenersCount());
//  }



  @Test
  public void startDoOcrOnImage_RequestIsReceived() throws IOException {
    byte[] imageData = getTestImage();
    communicator.startDoOcrOnImage(localHost, new DoOcrConfiguration(imageData), null);

    waitTillListenerHasBeenCalled();

    DoOcrOnImageRequest request = (DoOcrOnImageRequest)assertThatCorrectMethodHasBeenCalled(Addresses.DoOcrOnImageMethodName, DoOcrOnImageRequest.class);
    Assert.assertNotNull(request.getConfiguration());
    Assert.assertArrayEquals(imageData, request.getConfiguration().getImageToRecognize());
  }

  @Test
  public void respondToDoOcrOnImageRequest_RequestIsReceived() {
    DoOcrOnImageRequest request = new DoOcrOnImageRequest(TestMessageId, TestIpAddress, CommunicatorPort, new DoOcrConfiguration(new byte[0]));
    communicator.respondToDoOcrOnImageRequest(request, new TextRecognitionResult(TestRecognizedText), null);

    waitTillListenerHasBeenCalled();

    OcrResultResponse response = (OcrResultResponse)assertThatCorrectMethodHasBeenCalled(Addresses.OcrResultMethodName, OcrResultResponse.class);

    Assert.assertEquals(TestMessageId, response.getRequestMessageId());
    Assert.assertNotNull(response.getTextRecognitionResult());
    Assert.assertEquals(TestRecognizedText, response.getTextRecognitionResult().getRecognizedText());
  }

  @Test
  public void startDoOcrOnImage_ResponseListenerGetsCalled() {
    final AtomicBoolean hasResponseBeenReceived = new AtomicBoolean(false);
    final List<OcrResultResponse> receivedResponseHolder = new ArrayList<>();

    DoOcrOnImageRequest request = communicator.startDoOcrOnImage(localHost, new DoOcrConfiguration(new byte[0]), new DoOcrOnImageResultListener() {
      @Override
      public void responseReceived(DoOcrOnImageRequest request, OcrResultResponse ocrResult) {
        hasResponseBeenReceived.set(true);
        receivedResponseHolder.add(ocrResult);
      }
    });

    waitTillListenerHasBeenCalled();
    resetWaitLatch();

    communicator.respondToDoOcrOnImageRequest(request, new TextRecognitionResult(TestRecognizedText, true), null);

    waitTillListenerHasBeenCalled();

    Assert.assertEquals(2, receivedRequests.size());
    Assert.assertTrue(hasResponseBeenReceived.get());
    Assert.assertEquals(1, receivedResponseHolder.size());
    OcrResultResponse result = receivedResponseHolder.get(0);
    Assert.assertEquals(TestRecognizedText, result.getTextRecognitionResult().getRecognizedText());
  }

  @Test
  public void startDoOcrOnImage_ResponseListenerGetsRemovedFromListenerManager() {
    DoOcrOnImageRequest request = communicator.startDoOcrOnImage(localHost, new DoOcrConfiguration(new byte[0]), null);

    waitTillListenerHasBeenCalled();
    resetWaitLatch();

    communicator.respondToDoOcrOnImageRequest(request, new TextRecognitionResult(TestRecognizedText, true), null);

    waitTillListenerHasBeenCalled();

    Assert.assertNull(communicator.listenerManager.getAndRemoveListenerForMessageId(request.getMessageId()));
    Assert.assertEquals(0, communicator.listenerManager.getRegisteredListenersCount());
  }

  @Test
  public void startDoOcrOnImage_addCaptureImageOrDoOcrListenerIsCalled() {
    final AtomicBoolean methodCalled = new AtomicBoolean(false);
    final CountDownLatch waitLatch = new CountDownLatch(1);

    connector.addCaptureImageOrDoOcrListener(new CaptureImageOrDoOcrListener() {
      @Override
      public void startCaptureImageOrDoOcr(CaptureImageOrDoOcrRequest request) {
        methodCalled.set(true);
        waitLatch.countDown();
      }

      @Override
      public void stopCaptureImageOrDoOcr(StopRequestWithAsynchronousResponse request) {

      }
    });

    communicator.startDoOcrOnImage(localHost, new DoOcrConfiguration(new byte[0]), null);

    try { waitLatch.await(2, TimeUnit.SECONDS); } catch(Exception ex) { }

    Assert.assertTrue(methodCalled.get());
  }


  protected HostInfo createLocalHostServerInfo() {
    HostInfo hostInfo = HostInfo.fromUserAndDevice(Application.getLoggedOnUser(), Application.getApplication().getLocalDevice());
    hostInfo.setIpAddress(NetworkHelper.getIPAddressString(true));
    hostInfo.setPort(Application.getDeepThoughtsConnector().getMessageReceiverPort());

    return hostInfo;
  }


  protected byte[] getTestImage() throws IOException {
//    return FileHelper.loadTestImage();
    return new byte[] { 47, 11 };
  }


  protected void sendMessagesToCommunicator(int port, String... messages) throws IOException, InterruptedException {
    Runtime rt = Runtime.getRuntime();
    Process pr = rt.exec("telnet localhost " + port);
    Thread.sleep(50); // wait till connection has been established

    PrintStream streamWriter = new PrintStream(pr.getOutputStream());
    for(String message : messages) {
      streamWriter.print(message);
      streamWriter.flush();
    }

    streamWriter.close();
    pr.destroy();
  }


  protected void waitTillListenerHasBeenCalled() {
    waitTillListenerHasBeenCalled(3);
  }

  protected void waitTillListenerHasBeenCalled(int maxSecondsToWait) {
    try { waitLatch.await(maxSecondsToWait, TimeUnit.SECONDS); } catch(Exception ex) { }
  }

  protected void resetWaitLatch() {
    waitLatch = new CountDownLatch(1);
  }


  protected Request assertThatCorrectMethodHasBeenCalled(String methodName, Class<? extends Request> requestClass) {
    Assert.assertEquals(1, receivedRequests.size());

    String calledMethod = new ArrayList<String>(receivedRequests.keySet()).get(0);
    Assert.assertEquals(methodName, calledMethod);

    Request request = receivedRequests.get(calledMethod);
    Assert.assertTrue(requestClass.isAssignableFrom(request.getClass()));

    return request;
  }

  protected boolean isGenericRequest(Request request) {
    return request.getClass().isAssignableFrom(GenericRequest.class);
  }


  protected MessagesReceiverListener receiverListener = new MessagesReceiverListener() {
    @Override
    public void askForDeviceRegistrationResponseReceived(AskForDeviceRegistrationResponseMessage message) {

    }

    @Override
    public boolean messageReceived(String methodName, Request request) {
      receivedRequests.put(methodName, request);
      waitLatch.countDown();
      return true;
    }

    @Override
    public void registerDeviceRequestRetrieved(AskForDeviceRegistrationRequest request) {

    }
  };

}
