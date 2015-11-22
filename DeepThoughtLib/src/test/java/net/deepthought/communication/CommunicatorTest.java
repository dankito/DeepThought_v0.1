package net.deepthought.communication;

import net.deepthought.Application;
import net.deepthought.communication.listener.AskForDeviceRegistrationListener;
import net.deepthought.communication.listener.CaptureImageAndDoOcrResultListener;
import net.deepthought.communication.listener.CaptureImageOrDoOcrListener;
import net.deepthought.communication.listener.CaptureImageOrDoOcrResponseListener;
import net.deepthought.communication.listener.CaptureImageResultListener;
import net.deepthought.communication.listener.MessagesReceiverListener;
import net.deepthought.communication.messages.DeepThoughtMessagesReceiverConfig;
import net.deepthought.communication.messages.MessagesReceiver;
import net.deepthought.communication.messages.request.AskForDeviceRegistrationRequest;
import net.deepthought.communication.messages.request.CaptureImageOrDoOcrRequest;
import net.deepthought.communication.messages.request.GenericRequest;
import net.deepthought.communication.messages.request.Request;
import net.deepthought.communication.messages.request.RequestWithAsynchronousResponse;
import net.deepthought.communication.messages.request.StopCaptureImageOrDoOcrRequest;
import net.deepthought.communication.messages.response.AskForDeviceRegistrationResponseMessage;
import net.deepthought.communication.messages.response.CaptureImageResultResponse;
import net.deepthought.communication.messages.response.OcrResultResponse;
import net.deepthought.communication.model.ConnectedDevice;
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
    communicator.startCaptureImageNew(localHost, null);

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
    Assert.assertArrayEquals(imageData, response.getResult().getImageData());
  }

  @Test
  public void startCaptureImage_ResponseListenerGetsCalled() throws IOException {
    final AtomicBoolean hasResponseBeenReceived = new AtomicBoolean(false);
    final List<CaptureImageResultResponse> receivedResponseHolder = new ArrayList<>();

    RequestWithAsynchronousResponse request = communicator.startCaptureImageNew(localHost, new CaptureImageResultListener() {
      @Override
      public void responseReceived(RequestWithAsynchronousResponse requestWithAsynchronousResponse, CaptureImageResultResponse captureImageResultResponse) {
        hasResponseBeenReceived.set(true);
        receivedResponseHolder.add(captureImageResultResponse);
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
    CaptureImageResultResponse response = receivedResponseHolder.get(0);
    Assert.assertArrayEquals(imageData, response.getResult().getImageData());
  }

  @Test
  public void startCaptureImage_ResponseListenerGetsRemovedFromListenerManager() throws IOException {
    RequestWithAsynchronousResponse request = communicator.startCaptureImageNew(localHost, null);

    waitTillListenerHasBeenCalled();
    resetWaitLatch();

    communicator.respondToCaptureImageRequest(request, new CaptureImageResult(getTestImage(), true), null);

    waitTillListenerHasBeenCalled();

    Assert.assertNull(communicator.listenerManager.getAndRemoveListenerForMessageId(request.getMessageId()));
    Assert.assertEquals(0, communicator.listenerManager.getRegisteredListenersCount());
  }


  @Test
  public void startCaptureImageAndDoOcr_RequestIsReceived() {
    communicator.startCaptureImageAndDoOcrNew(localHost, null);

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

    RequestWithAsynchronousResponse request = communicator.startCaptureImageAndDoOcrNew(localHost, new CaptureImageAndDoOcrResultListener() {
      @Override
      public void responseReceived(RequestWithAsynchronousResponse requestWithAsynchronousResponse, OcrResultResponse ocrResultResponse) {
        hasResponseBeenReceived.set(true);
        receivedResponseHolder.add(ocrResultResponse);
      }
    });

    waitTillListenerHasBeenCalled();
    resetWaitLatch();

    communicator.respondToCaptureImageAndDoOcrRequest(request, new TextRecognitionResult(TestRecognizedText), null);

    waitTillListenerHasBeenCalled();

    Assert.assertEquals(2, receivedRequests.size());
    Assert.assertTrue(hasResponseBeenReceived.get());
    Assert.assertEquals(1, receivedResponseHolder.size());
    OcrResultResponse response = receivedResponseHolder.get(0);
    Assert.assertEquals(TestRecognizedText, response.getTextRecognitionResult().getRecognizedText());
  }

  @Test
  public void startCaptureImageAndDoOcr_ResponseListenerGetsRemovedFromListenerManager() {
    RequestWithAsynchronousResponse request = communicator.startCaptureImageAndDoOcrNew(localHost, null);

    waitTillListenerHasBeenCalled();
    resetWaitLatch();

    communicator.respondToCaptureImageAndDoOcrRequest(request, new TextRecognitionResult(TestRecognizedText, true), null);

    waitTillListenerHasBeenCalled();

    Assert.assertNull(communicator.listenerManager.getAndRemoveListenerForMessageId(request.getMessageId()));
    Assert.assertEquals(0, communicator.listenerManager.getRegisteredListenersCount());
  }

//  @Test
//  public void startCaptureImageAndDoOcr_addCaptureImageOrDoOcrListenerIsCalled() {
//    final AtomicBoolean methodCalled = new AtomicBoolean(false);
//    final CountDownLatch waitLatch = new CountDownLatch(1);
//
//    connector.addCaptureImageOrDoOcrListener(new CaptureImageOrDoOcrListener() {
//      @Override
//      public void startCaptureImageOrDoOcr(CaptureImageOrDoOcrRequest request) {
//        methodCalled.set(true);
//        waitLatch.countDown();
//      }
//
//      @Override
//      public void stopCaptureImageOrDoOcr(StopCaptureImageOrDoOcrRequest request) {
//
//      }
//    });
//
//    communicator.startCaptureImageAndDoOcr(localHost, null);
//
//    try { waitLatch.await(2, TimeUnit.SECONDS); } catch(Exception ex) { }
//
//    Assert.assertTrue(methodCalled.get());
//  }

  @Test
  public void startCaptureImageAndDoOcr_ServerSendsOcrResult_ServerResponseIsReceived() {
    final List<TextRecognitionResult> ocrResults = new ArrayList<>();
    final AtomicBoolean methodCalled = new AtomicBoolean(false);
    final String recognizedText = "Hyper, hyper";
    final CountDownLatch waitLatch = new CountDownLatch(1);

    connector.addCaptureImageOrDoOcrListener(new CaptureImageOrDoOcrListener() {

      @Override
      public void startCaptureImageOrDoOcr(CaptureImageOrDoOcrRequest request) {
        communicator.sendOcrResult(request, TextRecognitionResult.createRecognitionSuccessfulResult(recognizedText), null);
      }

      @Override
      public void stopCaptureImageOrDoOcr(StopCaptureImageOrDoOcrRequest request) {

      }
    });

    communicator.startCaptureImageAndDoOcr(new ConnectedDevice("unique", NetworkHelper.getIPAddressString(true), connector.getMessageReceiverPort()), new CaptureImageOrDoOcrResponseListener() {
      @Override
      public void captureImageResult(CaptureImageResult captureImageResult) {

      }

      @Override
      public void ocrResult(TextRecognitionResult ocrResult) {
        methodCalled.set(true);
        ocrResults.add(ocrResult);
        waitLatch.countDown();
      }
    });

    try { waitLatch.await(5, TimeUnit.SECONDS); } catch(Exception ex) { }

    Assert.assertTrue(methodCalled.get());
    Assert.assertEquals(recognizedText, ocrResults.get(0).getRecognizedText());
  }


  @Test
  public void startDoOcr_RequestIsReceived() throws IOException {
    byte[] imageData = getTestImage();
    communicator.startDoOcr(localHost, imageData, false, false, null);

    waitTillListenerHasBeenCalled();

    CaptureImageOrDoOcrRequest request = (CaptureImageOrDoOcrRequest)assertThatCorrectMethodHasBeenCalled(Addresses.StartCaptureImageAndDoOcrMethodName, CaptureImageOrDoOcrRequest.class);
    Assert.assertNotNull(request.getConfiguration());
    Assert.assertArrayEquals(imageData, request.readBytesFromImageUri());
  }

//  @Test
//  public void startDoOcr_ServerSendsOcrResult_ServerResponseIsReceived() throws IOException {
//    final List<TextRecognitionResult> ocrResults = new ArrayList<>();
//    final AtomicBoolean methodCalled = new AtomicBoolean(false);
//    final String recognizedText = "Hyper, hyper";
//    final CountDownLatch waitLatch = new CountDownLatch(1);
//
//    connector.addCaptureImageOrDoOcrListener(new CaptureImageOrDoOcrListener() {
//
//      @Override
//      public void startCaptureImageOrDoOcr(CaptureImageOrDoOcrRequest request) {
//        communicator.sendOcrResult(request, TextRecognitionResult.createRecognitionSuccessfulResult(recognizedText), null);
//      }
//
//      @Override
//      public void stopCaptureImageOrDoOcr(StopCaptureImageOrDoOcrRequest request) {
//
//      }
//    });
//
//    byte[] imageData = getTestImage();
//
//    communicator.startDoOcr(new ConnectedDevice("unique", NetworkHelper.getIPAddressString(true), connector.getMessageReceiverPort()),
//        imageData, false, false, new CaptureImageOrDoOcrResponseListener() {
//          @Override
//          public void captureImageResult(CaptureImageResult captureImageResult) {
//
//          }
//
//          @Override
//          public void ocrResult(TextRecognitionResult ocrResult) {
//            methodCalled.set(true);
//            ocrResults.add(ocrResult);
//            waitLatch.countDown();
//          }
//        });
//
//    try { waitLatch.await(3, TimeUnit.SECONDS); } catch(Exception ex) { }
//
//    Assert.assertTrue(methodCalled.get());
//    Assert.assertEquals(recognizedText, ocrResults.get(0).getRecognizedText());
//  }


//  @Test
//  public void sendCaptureImageResult_ResultIsReceived() throws IOException {
//    byte[] imageData = getTestImage();
//    communicator.startDoOcr(localHost, imageData, false, false, null);
//
//    waitTillListenerHasBeenCalled(100);
//
//    CaptureImageOrDoOcrRequest request = (CaptureImageOrDoOcrRequest)assertThatCorrectMethodHasBeenCalled(Addresses.StartCaptureImageAndDoOcrMethodName, CaptureImageOrDoOcrRequest.class);
//    Assert.assertNotNull(request.getConfiguration());
//    Assert.assertArrayEquals(imageData, request.readBytesFromImageUri());
//
//    final AtomicBoolean methodCalled = new AtomicBoolean(false);
//    final CountDownLatch waitLatch = new CountDownLatch(1);
//
//    ConnectedDevice self = ConnectedDevice.createSelfInstance();
//    CaptureImageOrDoOcrRequest request = new CaptureImageOrDoOcrRequest(self.getAddress(), self.getMessagesPort(), true, false);
//    connector.getCommunicator().captureImageOrDoOcrListeners.put(request, new CaptureImageOrDoOcrResponseListener() {
//      @Override
//      public void captureImageResult(CaptureImageResult captureImageResult) {
//        methodCalled.set(true);
//        waitLatch.countDown();
//      }
//
//      @Override
//      public void ocrResult(TextRecognitionResult ocrResult) {
//
//      }
//    });
//
//    byte[] imageData = getTestImage();
//
//    communicator.sendCaptureImageResult(request, imageData, null);
//
//    try { waitLatch.await(2, TimeUnit.SECONDS); } catch(Exception ex) { }
//
//    Assert.assertTrue(methodCalled.get());
//  }

  @Test
  public void sendCapturedImage_ResultIsReceived() throws IOException {
    communicator.sendCaptureImageResult(new CaptureImageOrDoOcrRequest(TestMessageId, TestIpAddress, CommunicatorPort, false, false), getTestImage(), null);

    waitTillListenerHasBeenCalled();

    CaptureImageResultResponse request = (CaptureImageResultResponse)assertThatCorrectMethodHasBeenCalled(Addresses.CaptureImageResultMethodName, CaptureImageResultResponse.class);
    Assert.assertNotNull(request.getResult());
    Assert.assertNotNull(request.getResult().getImageUri());
  }


  @Test
  public void sendCaptureImageResult_ReceivedImageDataIsCorrect() throws IOException {
    byte[] sentImageData = getTestImage();

    communicator.sendCaptureImageResult(new CaptureImageOrDoOcrRequest(TestMessageId, TestIpAddress, CommunicatorPort, false, false), sentImageData, null);

    waitTillListenerHasBeenCalled();

    CaptureImageResultResponse request = (CaptureImageResultResponse)assertThatCorrectMethodHasBeenCalled(Addresses.CaptureImageResultMethodName, CaptureImageResultResponse.class);
    Assert.assertArrayEquals(sentImageData, request.getResult().getImageData());
  }


  @Test
  public void stopCaptureImageAndDoOcr_RequestIsReceived() {
//    communicator.stopCaptureImageAndDoOcr(ocrResponseListener, new CaptureImageOrDoOcrRequest(TestMessageId, TestIpAddress, CommunicatorPort, false, false), new TextRecognitionResult(), null);
//
//    waitTillListenerHasBeenCalled();
//
//    Assert.assertEquals(1, receivedRequests.size());
//    String calledMethod = new ArrayList<String>(receivedRequests.keySet()).get(0);
//    Assert.assertEquals(Addresses.OcrResultMethodName, calledMethod);
//    Assert.assertTrue(TextRecognitionResult.class.equals(receivedRequests.get(calledMethod)));

    final AtomicBoolean methodCalled = new AtomicBoolean(false);
    final CountDownLatch waitLatch = new CountDownLatch(1);

    connector.addMessagesReceiverListener(new MessagesReceiverListener() {
      @Override
      public void askForDeviceRegistrationResponseReceived(AskForDeviceRegistrationResponseMessage message) {

      }

      @Override
      public boolean messageReceived(String methodName, Request request) {
        return false;
      }

//      @Override
      public void stopCaptureImageOrDoOcr(StopCaptureImageOrDoOcrRequest request) {
        methodCalled.set(true);
        waitLatch.countDown();
      }

      @Override
      public void registerDeviceRequestRetrieved(AskForDeviceRegistrationRequest request) {

      }
    });

    communicator.startCaptureImageAndDoOcr(new ConnectedDevice("unique", NetworkHelper.getIPAddressString(true), connector.getMessageReceiverPort()), ocrResponseListener);
    communicator.stopCaptureImageAndDoOcr(ocrResponseListener, null);

    try { waitLatch.await(3, TimeUnit.SECONDS);
    } catch(Exception ex) { }

    Assert.assertTrue(methodCalled.get());
  }

  @Test
  public void stopCaptureImageAndDoOcr_ListenerGetsCalledCorrectly() {
    final AtomicBoolean methodCalled = new AtomicBoolean(false);
    final CountDownLatch waitLatch = new CountDownLatch(1);

    connector.addCaptureImageOrDoOcrListener(new CaptureImageOrDoOcrListener() {
      @Override
      public void startCaptureImageOrDoOcr(CaptureImageOrDoOcrRequest request) {
        communicator.stopCaptureImageAndDoOcr(ocrResponseListener, null);
      }

      @Override
      public void stopCaptureImageOrDoOcr(StopCaptureImageOrDoOcrRequest request) {
        methodCalled.set(true);
        waitLatch.countDown();
      }
    });

    communicator.startCaptureImageAndDoOcr(new ConnectedDevice("unique", NetworkHelper.getIPAddressString(true), connector.getMessageReceiverPort()), ocrResponseListener);

    try { waitLatch.await(3, TimeUnit.SECONDS);
    } catch(Exception ex) { }

    Assert.assertTrue(methodCalled.get());
  }

  @Test
  public void stopCaptureImageAndDoOcrDone_ListenerGetsRemoved() {
    final CountDownLatch waitLatch = new CountDownLatch(1);

    connector.addCaptureImageOrDoOcrListener(new CaptureImageOrDoOcrListener() {
      @Override
      public void startCaptureImageOrDoOcr(CaptureImageOrDoOcrRequest request) {
        communicator.stopCaptureImageAndDoOcr(ocrResponseListener, null);
      }

      @Override
      public void stopCaptureImageOrDoOcr(StopCaptureImageOrDoOcrRequest request) {
        waitLatch.countDown();
      }
    });

    communicator.startCaptureImageAndDoOcr(new ConnectedDevice("unique", NetworkHelper.getIPAddressString(true), connector.getMessageReceiverPort()), ocrResponseListener);
    Assert.assertEquals(1, communicator.captureImageOrDoOcrListeners.size());

    try { waitLatch.await(3, TimeUnit.SECONDS);
    } catch (Exception ex) {
    }

    Assert.assertEquals(0, communicator.captureImageOrDoOcrListeners.size());
  }

  protected CaptureImageOrDoOcrResponseListener ocrResponseListener = new CaptureImageOrDoOcrResponseListener() {
    @Override
    public void captureImageResult(CaptureImageResult captureImageResult) {

    }

    @Override
    public void ocrResult(TextRecognitionResult ocrResult) {

    }
  };


  @Test
  public void sendOcrResult() {
    communicator.sendOcrResult(new CaptureImageOrDoOcrRequest(TestMessageId, TestIpAddress, CommunicatorPort, false, false), new TextRecognitionResult(), null);

    waitTillListenerHasBeenCalled();

    assertThatCorrectMethodHasBeenCalled(Addresses.OcrResultMethodName, OcrResultResponse.class);
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
