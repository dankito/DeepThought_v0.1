package net.dankito.deepthought.communication;

import net.dankito.deepthought.communication.listener.AskForDeviceRegistrationResultListener;
import net.dankito.deepthought.communication.listener.ImportFilesResultListener;
import net.dankito.deepthought.communication.listener.MessagesReceiverListener;
import net.dankito.deepthought.communication.listener.OcrResultListener;
import net.dankito.deepthought.communication.listener.ScanBarcodeResultListener;
import net.dankito.deepthought.communication.messages.AsynchronousResponseListenerManager;
import net.dankito.deepthought.communication.messages.DeepThoughtMessagesReceiverConfig;
import net.dankito.deepthought.communication.messages.MessagesDispatcher;
import net.dankito.deepthought.communication.messages.MessagesReceiver;
import net.dankito.deepthought.communication.messages.request.AskForDeviceRegistrationRequest;
import net.dankito.deepthought.communication.messages.request.DoOcrRequest;
import net.dankito.deepthought.communication.messages.request.GenericRequest;
import net.dankito.deepthought.communication.messages.request.ImportFilesRequest;
import net.dankito.deepthought.communication.messages.request.Request;
import net.dankito.deepthought.communication.messages.request.RequestWithAsynchronousResponse;
import net.dankito.deepthought.communication.messages.response.AskForDeviceRegistrationResponse;
import net.dankito.deepthought.communication.messages.response.ImportFilesResultResponse;
import net.dankito.deepthought.communication.messages.response.OcrResultResponse;
import net.dankito.deepthought.communication.messages.response.ScanBarcodeResult;
import net.dankito.deepthought.communication.messages.response.ScanBarcodeResultResponse;
import net.dankito.deepthought.communication.model.ConnectedDevice;
import net.dankito.deepthought.communication.model.DoOcrConfiguration;
import net.dankito.deepthought.communication.model.HostInfo;
import net.dankito.deepthought.communication.model.ImportFilesConfiguration;
import net.dankito.deepthought.communication.model.ImportFilesSource;
import net.dankito.deepthought.communication.model.OcrSource;
import net.dankito.deepthought.data.contentextractor.ocr.ImportFilesResult;
import net.dankito.deepthought.data.contentextractor.ocr.TextRecognitionResult;
import net.dankito.deepthought.util.IThreadPool;

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


  protected final static int TestMessageId = 4711;

  protected final static String TestRecognizedText = "Cuddle";


  protected Communicator communicator = null;

  protected MessagesReceiver receiver = null;

  protected CountDownLatch waitLatch = new CountDownLatch(1);

  protected Map<String, Request> receivedRequests = new HashMap<>();


  @Override
  public void setup() throws Exception {
    super.setup();

    final AsynchronousResponseListenerManager listenerManager = new AsynchronousResponseListenerManager();

    communicator = new Communicator(new CommunicatorConfig(new MessagesDispatcher(threadPool), listenerManager, CommunicatorPort, messagesCreator, registeredDevicesManager));

    startMessagesReceiverAsync(threadPool, listenerManager);
  }

  protected void startMessagesReceiverAsync(IThreadPool threadPool, final AsynchronousResponseListenerManager listenerManager) throws Exception {
    final List<Exception> caughtExceptionsHolder = new ArrayList<>();
    final CountDownLatch waitForMessagesReceiverStartUp = new CountDownLatch(1);

    threadPool.runTaskAsync(new Runnable() {
      @Override
      public void run() {
        receiver = new MessagesReceiver(new DeepThoughtMessagesReceiverConfig(CommunicatorPort, listenerManager), receiverListener);
        try { receiver.start(); } catch(Exception ex) { caughtExceptionsHolder.add(ex); }
        try { Thread.sleep(200); } catch(Exception ex) { } // it is very critical that server is fully started therefore wait some time
        waitForMessagesReceiverStartUp.countDown();
      }
    });

    try { waitForMessagesReceiverStartUp.await(2, TimeUnit.SECONDS); } catch(Exception ex) { }
    for(Exception caughtException : caughtExceptionsHolder)
      throw caughtException;
  }

  @After
  public void tearDown() {
    receiver.stop();
  }



  @Test
  public void askForDeviceRegistration_RequestIsReceived() {
    communicator.askForDeviceRegistration(createLocalHostServerInfo(), loggedOnUser, localDevice, null);

    waitTillListenerHasBeenCalled();

    assertThatCorrectMethodHasBeenCalled(Addresses.AskForDeviceRegistrationMethodName, AskForDeviceRegistrationRequest.class);
  }

  @Test
  public void respondToAskForDeviceRegistrationRequest_RequestIsReceived() throws IOException {
    AskForDeviceRegistrationRequest request = communicator.createAskForDeviceRegistrationRequest(loggedOnUser, localDevice);
    request.setAddress(TestIpAddress);
    request.setPort(CommunicatorPort);
    communicator.respondToAskForDeviceRegistrationRequest(request, createAskForDeviceRegistrationResponseFromRequest(request), null);

    waitTillListenerHasBeenCalled();

    AskForDeviceRegistrationResponse response = (AskForDeviceRegistrationResponse)assertThatCorrectMethodHasBeenCalled(Addresses.AskForDeviceRegistrationResponseMethodName, AskForDeviceRegistrationResponse.class);

    Assert.assertEquals(request.getMessageId(), response.getRequestMessageId());
    Assert.assertNotNull(response.getUser());
    Assert.assertNotNull(response.getDevice());
    Assert.assertNotNull(response.getGroup());
    Assert.assertEquals(true, response.allowsRegistration());
    Assert.assertEquals(true, response.getUseSendersUserInformation());
  }

  @Test
  public void askForDeviceRegistration_ResponseListenerGetsCalled() throws IOException {
    final AtomicBoolean hasResponseBeenReceived = new AtomicBoolean(false);
    final List<AskForDeviceRegistrationResponse> receivedResponseHolder = new ArrayList<>();

    AskForDeviceRegistrationRequest request = communicator.askForDeviceRegistration(createLocalHostServerInfo(), loggedOnUser, localDevice, new AskForDeviceRegistrationResultListener() {
      @Override
      public void responseReceived(AskForDeviceRegistrationRequest request, AskForDeviceRegistrationResponse response) {
        hasResponseBeenReceived.set(true);
        receivedResponseHolder.add(response);
      }
    });

    waitTillListenerHasBeenCalled();
    resetWaitLatch();

    communicator.respondToAskForDeviceRegistrationRequest(request, createAskForDeviceRegistrationResponseFromRequest(request), null);

    waitTillListenerHasBeenCalled();

    Assert.assertEquals(2, receivedRequests.size());
    Assert.assertTrue(hasResponseBeenReceived.get());
    Assert.assertEquals(1, receivedResponseHolder.size());
  }

  @Test
  public void askForDeviceRegistration_ResponseListenerGetsRemovedFromListenerManager() throws IOException {
    AskForDeviceRegistrationRequest request = communicator.askForDeviceRegistration(createLocalHostServerInfo(), loggedOnUser, localDevice, null);

    waitTillListenerHasBeenCalled();
    resetWaitLatch();

    Assert.assertEquals(1, communicator.listenerManager.getRegisteredListenersCount());

    communicator.respondToAskForDeviceRegistrationRequest(request, createAskForDeviceRegistrationResponseFromRequest(request), null);

    waitTillListenerHasBeenCalled();

    Assert.assertNull(communicator.listenerManager.getAndRemoveListenerForMessageId(request.getMessageId()));
    Assert.assertEquals(0, communicator.listenerManager.getRegisteredListenersCount());
  }


  @Test
  public void notifyRemoteWeHaveConnected() throws IOException {
    communicator.notifyRemoteWeHaveConnected(localHost, localHost);

    waitTillListenerHasBeenCalled();

    GenericRequest request = (GenericRequest)assertThatCorrectMethodHasBeenCalled(Addresses.NotifyRemoteWeHaveConnectedMethodName, GenericRequest.class);
    Assert.assertTrue(request.getRequestBody() instanceof ConnectedDevice);
  }

  @Test
  public void sendHeartbeat() throws IOException {
    communicator.sendHeartbeat(localHost, localHost, null);

    waitTillListenerHasBeenCalled();

    GenericRequest request = (GenericRequest)assertThatCorrectMethodHasBeenCalled(Addresses.HeartbeatMethodName, GenericRequest.class);
    Assert.assertTrue(request.getRequestBody() instanceof ConnectedDevice);
  }



  @Test
  public void startCaptureImage_RequestIsReceived() {
    communicator.startImportFiles(localHost, new ImportFilesConfiguration(ImportFilesSource.CaptureImage), null);

    waitTillListenerHasBeenCalled();

    assertThatCorrectMethodHasBeenCalled(Addresses.StartImportFilesMethodName, RequestWithAsynchronousResponse.class);
  }

  @Test
  public void respondToCaptureImageRequest_RequestIsReceived() throws IOException {
    byte[] imageData = getTestImage();
    RequestWithAsynchronousResponse request = new RequestWithAsynchronousResponse(TestMessageId, TestIpAddress, CommunicatorPort);
    communicator.respondToImportFilesRequest(request, new ImportFilesResult(imageData), null);

    waitTillListenerHasBeenCalled();

    ImportFilesResultResponse response = (ImportFilesResultResponse)assertThatCorrectMethodHasBeenCalled(Addresses.ImportFilesResultMethodName, ImportFilesResultResponse.class);

    Assert.assertEquals(TestMessageId, response.getRequestMessageId());
    Assert.assertNotNull(response.getResult());
    Assert.assertNotNull(response.getResult().getFileUri());
    Assert.assertArrayEquals(imageData, response.getResult().getFileData());
  }

  @Test
  public void startCaptureImage_ResponseListenerGetsCalled() throws IOException {
    final AtomicBoolean hasResponseBeenReceived = new AtomicBoolean(false);
    final List<ImportFilesResultResponse> receivedResponseHolder = new ArrayList<>();

    ImportFilesRequest request = communicator.startImportFiles(localHost, new ImportFilesConfiguration(ImportFilesSource.CaptureImage), new ImportFilesResultListener() {
      @Override
      public void responseReceived(ImportFilesRequest importFilesRequest, ImportFilesResultResponse importFilesResult) {
        hasResponseBeenReceived.set(true);
        receivedResponseHolder.add(importFilesResult);
      }
    });

    waitTillListenerHasBeenCalled();
    resetWaitLatch();

    byte[] imageData = getTestImage();
    communicator.respondToImportFilesRequest(request, new ImportFilesResult(imageData, true), null);

    waitTillListenerHasBeenCalled();

    Assert.assertEquals(2, receivedRequests.size());
    Assert.assertTrue(hasResponseBeenReceived.get());
    Assert.assertEquals(1, receivedResponseHolder.size());
    ImportFilesResultResponse result = receivedResponseHolder.get(0);
    Assert.assertArrayEquals(imageData, result.getResult().getFileData());
  }

  @Test
  public void startCaptureImage_ResponseListenerGetsRemovedFromListenerManager() throws IOException {
    ImportFilesRequest request = communicator.startImportFiles(localHost, new ImportFilesConfiguration(ImportFilesSource.CaptureImage), null);

    waitTillListenerHasBeenCalled();
    resetWaitLatch();

    Assert.assertEquals(1, communicator.listenerManager.getRegisteredListenersCount());

    communicator.respondToImportFilesRequest(request, new ImportFilesResult(getTestImage(), true), null);

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
//    assertThatCorrectMethodHasBeenCalled(Addresses.StopImportFilesMethodName, StopRequestWithAsynchronousResponse.class);
//  }
//
//  @Test
//  public void stopCaptureImage_ListenerGetsCalledCorrectly() {
//    final AtomicBoolean methodCalled = new AtomicBoolean(false);
//    final CountDownLatch waitLatch = new CountDownLatch(1);
//
//    connector.addImportFilesOrDoOcrListener(new ImportFilesOrDoOcrListener() {
//      @Override
//      public void captureImageAndDoOcr(CaptureImageOrDoOcrRequest request) {
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
//    RequestWithAsynchronousResponse request = communicator.startImportFiles(localHost, null);
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
//    RequestWithAsynchronousResponse request = communicator.startImportFiles(localHost, null);
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
  public void respondToCaptureImageAndDoOcrRequest_RequestIsReceived() {
    DoOcrRequest request = new DoOcrRequest(TestMessageId, TestIpAddress, CommunicatorPort, new DoOcrConfiguration(OcrSource.CaptureImage));
    communicator.respondToDoOcrRequest(request, new TextRecognitionResult(TestRecognizedText), null);

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

    DoOcrRequest request = communicator.startDoOcr(localHost, new DoOcrConfiguration(OcrSource.CaptureImage), new OcrResultListener() {
      @Override
      public void responseReceived(DoOcrRequest doOcrRequest, OcrResultResponse ocrResult) {
        hasResponseBeenReceived.set(true);
        receivedResponseHolder.add(ocrResult);
      }
    });

    waitTillListenerHasBeenCalled();
    resetWaitLatch();

    communicator.respondToDoOcrRequest(request, new TextRecognitionResult(TestRecognizedText), null);

    waitTillListenerHasBeenCalled();

    Assert.assertEquals(2, receivedRequests.size());
    Assert.assertTrue(hasResponseBeenReceived.get());
    Assert.assertEquals(1, receivedResponseHolder.size());
    OcrResultResponse result = receivedResponseHolder.get(0);
    Assert.assertEquals(TestRecognizedText, result.getTextRecognitionResult().getRecognizedText());
  }

  @Test
  public void startCaptureImageAndDoOcr_ResponseListenerGetsRemovedFromListenerManager() {
    DoOcrRequest request = communicator.startDoOcr(localHost, new DoOcrConfiguration(OcrSource.CaptureImage), null);

    waitTillListenerHasBeenCalled();
    resetWaitLatch();

    Assert.assertEquals(1, communicator.listenerManager.getRegisteredListenersCount());

    communicator.respondToDoOcrRequest(request, new TextRecognitionResult(TestRecognizedText, true), null);

    waitTillListenerHasBeenCalled();

    Assert.assertNull(communicator.listenerManager.getAndRemoveListenerForMessageId(request.getMessageId()));
    Assert.assertEquals(0, communicator.listenerManager.getRegisteredListenersCount());
  }



  @Test
  public void respondToSelectRemoteImageAndDoOcrRequest_RequestIsReceived() {
    DoOcrRequest request = new DoOcrRequest(TestMessageId, TestIpAddress, CommunicatorPort, new DoOcrConfiguration(OcrSource.SelectAnExistingImageOnDevice));
    communicator.respondToDoOcrRequest(request, new TextRecognitionResult(TestRecognizedText), null);

    waitTillListenerHasBeenCalled();

    OcrResultResponse response = (OcrResultResponse)assertThatCorrectMethodHasBeenCalled(Addresses.OcrResultMethodName, OcrResultResponse.class);

    Assert.assertEquals(TestMessageId, response.getRequestMessageId());
    Assert.assertNotNull(response.getTextRecognitionResult());
    Assert.assertEquals(TestRecognizedText, response.getTextRecognitionResult().getRecognizedText());
  }

  @Test
  public void startSelectRemoteImageAndDoOcr_ResponseListenerGetsCalled() {
    final AtomicBoolean hasResponseBeenReceived = new AtomicBoolean(false);
    final List<OcrResultResponse> receivedResponseHolder = new ArrayList<>();

    DoOcrRequest request = communicator.startDoOcr(localHost, new DoOcrConfiguration(OcrSource.SelectAnExistingImageOnDevice), new OcrResultListener() {
      @Override
      public void responseReceived(DoOcrRequest doOcrRequest, OcrResultResponse ocrResult) {
        hasResponseBeenReceived.set(true);
        receivedResponseHolder.add(ocrResult);
      }
    });

    waitTillListenerHasBeenCalled();
    resetWaitLatch();

    communicator.respondToDoOcrRequest(request, new TextRecognitionResult(TestRecognizedText), null);

    waitTillListenerHasBeenCalled();

    Assert.assertEquals(2, receivedRequests.size());
    Assert.assertTrue(hasResponseBeenReceived.get());
    Assert.assertEquals(1, receivedResponseHolder.size());
    OcrResultResponse result = receivedResponseHolder.get(0);
    Assert.assertEquals(TestRecognizedText, result.getTextRecognitionResult().getRecognizedText());
  }

  @Test
  public void startSelectRemoteImageAndDoOcr_ResponseListenerGetsRemovedFromListenerManager() {
    DoOcrRequest request = communicator.startDoOcr(localHost, new DoOcrConfiguration(OcrSource.SelectAnExistingImageOnDevice), null);

    waitTillListenerHasBeenCalled();
    resetWaitLatch();

    Assert.assertEquals(1, communicator.listenerManager.getRegisteredListenersCount());

    communicator.respondToDoOcrRequest(request, new TextRecognitionResult(TestRecognizedText, true), null);

    waitTillListenerHasBeenCalled();

    Assert.assertNull(communicator.listenerManager.getAndRemoveListenerForMessageId(request.getMessageId()));
    Assert.assertEquals(0, communicator.listenerManager.getRegisteredListenersCount());
  }



  @Test
  public void respondToRecognizeFromUriRequest_RequestIsReceived() {
    DoOcrRequest request = new DoOcrRequest(TestMessageId, TestIpAddress, CommunicatorPort, new DoOcrConfiguration(OcrSource.RecognizeFromUri));
    communicator.respondToDoOcrRequest(request, new TextRecognitionResult(TestRecognizedText), null);

    waitTillListenerHasBeenCalled();

    OcrResultResponse response = (OcrResultResponse)assertThatCorrectMethodHasBeenCalled(Addresses.OcrResultMethodName, OcrResultResponse.class);

    Assert.assertEquals(TestMessageId, response.getRequestMessageId());
    Assert.assertNotNull(response.getTextRecognitionResult());
    Assert.assertEquals(TestRecognizedText, response.getTextRecognitionResult().getRecognizedText());
  }

  @Test
  public void startRecognizeFromUri_ResponseListenerGetsCalled() {
    final AtomicBoolean hasResponseBeenReceived = new AtomicBoolean(false);
    final List<OcrResultResponse> receivedResponseHolder = new ArrayList<>();

    DoOcrRequest request = communicator.startDoOcr(localHost, new DoOcrConfiguration(OcrSource.RecognizeFromUri), new OcrResultListener() {
      @Override
      public void responseReceived(DoOcrRequest doOcrRequest, OcrResultResponse ocrResult) {
        hasResponseBeenReceived.set(true);
        receivedResponseHolder.add(ocrResult);
      }
    });

    waitTillListenerHasBeenCalled();
    resetWaitLatch();

    communicator.respondToDoOcrRequest(request, new TextRecognitionResult(TestRecognizedText), null);

    waitTillListenerHasBeenCalled();

    Assert.assertEquals(2, receivedRequests.size());
    Assert.assertTrue(hasResponseBeenReceived.get());
    Assert.assertEquals(1, receivedResponseHolder.size());
    OcrResultResponse result = receivedResponseHolder.get(0);
    Assert.assertEquals(TestRecognizedText, result.getTextRecognitionResult().getRecognizedText());
  }

  @Test
  public void startRecognizeFromUri_ResponseListenerGetsRemovedFromListenerManager() {
    DoOcrRequest request = communicator.startDoOcr(localHost, new DoOcrConfiguration(OcrSource.RecognizeFromUri), null);

    waitTillListenerHasBeenCalled();
    resetWaitLatch();

    Assert.assertEquals(1, communicator.listenerManager.getRegisteredListenersCount());

    communicator.respondToDoOcrRequest(request, new TextRecognitionResult(TestRecognizedText, true), null);

    waitTillListenerHasBeenCalled();

    Assert.assertNull(communicator.listenerManager.getAndRemoveListenerForMessageId(request.getMessageId()));
    Assert.assertEquals(0, communicator.listenerManager.getRegisteredListenersCount());
  }



  @Test
  public void respondToAskUserForSourceAndDoOcrRequest_RequestIsReceived() {
    DoOcrRequest request = new DoOcrRequest(TestMessageId, TestIpAddress, CommunicatorPort, new DoOcrConfiguration(OcrSource.AskUser));
    communicator.respondToDoOcrRequest(request, new TextRecognitionResult(TestRecognizedText), null);

    waitTillListenerHasBeenCalled();

    OcrResultResponse response = (OcrResultResponse)assertThatCorrectMethodHasBeenCalled(Addresses.OcrResultMethodName, OcrResultResponse.class);

    Assert.assertEquals(TestMessageId, response.getRequestMessageId());
    Assert.assertNotNull(response.getTextRecognitionResult());
    Assert.assertEquals(TestRecognizedText, response.getTextRecognitionResult().getRecognizedText());
  }

  @Test
  public void startAskUserForSourceAndDoOcr_ResponseListenerGetsCalled() {
    final AtomicBoolean hasResponseBeenReceived = new AtomicBoolean(false);
    final List<OcrResultResponse> receivedResponseHolder = new ArrayList<>();

    DoOcrRequest request = communicator.startDoOcr(localHost, new DoOcrConfiguration(OcrSource.AskUser), new OcrResultListener() {
      @Override
      public void responseReceived(DoOcrRequest doOcrRequest, OcrResultResponse ocrResult) {
        hasResponseBeenReceived.set(true);
        receivedResponseHolder.add(ocrResult);
      }
    });

    waitTillListenerHasBeenCalled();
    resetWaitLatch();

    communicator.respondToDoOcrRequest(request, new TextRecognitionResult(TestRecognizedText), null);

    waitTillListenerHasBeenCalled();

    Assert.assertEquals(2, receivedRequests.size());
    Assert.assertTrue(hasResponseBeenReceived.get());
    Assert.assertEquals(1, receivedResponseHolder.size());
    OcrResultResponse result = receivedResponseHolder.get(0);
    Assert.assertEquals(TestRecognizedText, result.getTextRecognitionResult().getRecognizedText());
  }

  @Test
  public void startAskUserForSourceAndDoOcr_ResponseListenerGetsRemovedFromListenerManager() {
    DoOcrRequest request = communicator.startDoOcr(localHost, new DoOcrConfiguration(OcrSource.AskUser), null);

    waitTillListenerHasBeenCalled();
    resetWaitLatch();

    Assert.assertEquals(1, communicator.listenerManager.getRegisteredListenersCount());

    communicator.respondToDoOcrRequest(request, new TextRecognitionResult(TestRecognizedText, true), null);

    waitTillListenerHasBeenCalled();

    Assert.assertNull(communicator.listenerManager.getAndRemoveListenerForMessageId(request.getMessageId()));
    Assert.assertEquals(0, communicator.listenerManager.getRegisteredListenersCount());
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
//    connector.addImportFilesOrDoOcrListener(new ImportFilesOrDoOcrListener() {
//      @Override
//      public void captureImageAndDoOcr(CaptureImageOrDoOcrRequest request) {
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
//    RequestWithAsynchronousResponse request = communicator.captureImageAndDoOcr(localHost, null);
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
//    RequestWithAsynchronousResponse request = communicator.captureImageAndDoOcr(localHost, null);
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
    communicator.startDoOcr(localHost, new DoOcrConfiguration(imageData), null);

    waitTillListenerHasBeenCalled();

    DoOcrRequest request = (DoOcrRequest)assertThatCorrectMethodHasBeenCalled(Addresses.DoOcrOnImageMethodName, DoOcrRequest.class);
    Assert.assertNotNull(request.getConfiguration());
    Assert.assertArrayEquals(imageData, request.getConfiguration().getImageToRecognize());
  }

  @Test
  public void respondToDoOcrOnImageRequest_RequestIsReceived() {
    DoOcrRequest request = new DoOcrRequest(TestMessageId, TestIpAddress, CommunicatorPort, new DoOcrConfiguration(new byte[0]));
    communicator.respondToDoOcrRequest(request, new TextRecognitionResult(TestRecognizedText), null);

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

    DoOcrRequest request = communicator.startDoOcr(localHost, new DoOcrConfiguration(new byte[0]), new OcrResultListener() {
      @Override
      public void responseReceived(DoOcrRequest request, OcrResultResponse ocrResult) {
        hasResponseBeenReceived.set(true);
        receivedResponseHolder.add(ocrResult);
      }
    });

    waitTillListenerHasBeenCalled();
    resetWaitLatch();

    communicator.respondToDoOcrRequest(request, new TextRecognitionResult(TestRecognizedText, true), null);

    waitTillListenerHasBeenCalled();

    Assert.assertEquals(2, receivedRequests.size());
    Assert.assertTrue(hasResponseBeenReceived.get());
    Assert.assertEquals(1, receivedResponseHolder.size());
    OcrResultResponse result = receivedResponseHolder.get(0);
    Assert.assertEquals(TestRecognizedText, result.getTextRecognitionResult().getRecognizedText());
  }

  @Test
  public void startDoOcrOnImage_ResponseListenerGetsRemovedFromListenerManager() {
    DoOcrRequest request = communicator.startDoOcr(localHost, new DoOcrConfiguration(new byte[0]), null);

    waitTillListenerHasBeenCalled();
    resetWaitLatch();

    Assert.assertEquals(1, communicator.listenerManager.getRegisteredListenersCount());

    communicator.respondToDoOcrRequest(request, new TextRecognitionResult(TestRecognizedText, true), null);

    waitTillListenerHasBeenCalled();

    Assert.assertNull(communicator.listenerManager.getAndRemoveListenerForMessageId(request.getMessageId()));
    Assert.assertEquals(0, communicator.listenerManager.getRegisteredListenersCount());
  }



  @Test
  public void startScanBarcode_RequestIsReceived() {
    communicator.startScanBarcode(localHost, null);

    waitTillListenerHasBeenCalled();

    assertThatCorrectMethodHasBeenCalled(Addresses.StartScanBarcodeMethodName, RequestWithAsynchronousResponse.class);
  }

  @Test
  public void respondToScanBarcodeRequest_RequestIsReceived() throws IOException {
    String decodedBarcode = "Cuddle";
    String barcodeFormat = "QrCode";
    RequestWithAsynchronousResponse request = new RequestWithAsynchronousResponse(TestMessageId, TestIpAddress, CommunicatorPort);
    communicator.respondToScanBarcodeRequest(request, new ScanBarcodeResult(decodedBarcode, barcodeFormat), null);

    waitTillListenerHasBeenCalled();

    ScanBarcodeResultResponse response = (ScanBarcodeResultResponse)assertThatCorrectMethodHasBeenCalled(Addresses.ScanBarcodeResultMethodName, ScanBarcodeResultResponse.class);

    Assert.assertEquals(TestMessageId, response.getRequestMessageId());
    Assert.assertTrue(response.isDone());

    ScanBarcodeResult result = response.getResult();
    Assert.assertEquals(decodedBarcode, result.getDecodedBarcode());
    Assert.assertEquals(barcodeFormat, result.getBarcodeFormat());
  }

  @Test
  public void startScanBarcode_ResponseListenerGetsCalled() throws IOException {
    final AtomicBoolean hasResponseBeenReceived = new AtomicBoolean(false);
    final List<ScanBarcodeResultResponse> receivedResponseHolder = new ArrayList<>();

    RequestWithAsynchronousResponse request = communicator.startScanBarcode(localHost, new ScanBarcodeResultListener() {
      @Override
      public void responseReceived(RequestWithAsynchronousResponse requestWithAsynchronousResponse, ScanBarcodeResultResponse captureImageResult) {
        hasResponseBeenReceived.set(true);
        receivedResponseHolder.add(captureImageResult);
      }
    });

    waitTillListenerHasBeenCalled();
    resetWaitLatch();

    String decodedBarcode = "Cuddle";
    String barcodeFormat = "QrCode";
    communicator.respondToScanBarcodeRequest(request, new ScanBarcodeResult(decodedBarcode, barcodeFormat), null);

    waitTillListenerHasBeenCalled();

    Assert.assertEquals(2, receivedRequests.size());
    Assert.assertTrue(hasResponseBeenReceived.get());
    Assert.assertEquals(1, receivedResponseHolder.size());

    ScanBarcodeResultResponse response = receivedResponseHolder.get(0);
    Assert.assertTrue(response.isDone());

    ScanBarcodeResult result = response.getResult();
    Assert.assertEquals(decodedBarcode, result.getDecodedBarcode());
    Assert.assertEquals(barcodeFormat, result.getBarcodeFormat());
  }

  @Test
  public void startScanBarcode_ResponseListenerGetsRemovedFromListenerManager() throws IOException {
    RequestWithAsynchronousResponse request = communicator.startScanBarcode(localHost, null);

    waitTillListenerHasBeenCalled();
    resetWaitLatch();

    Assert.assertEquals(1, communicator.listenerManager.getRegisteredListenersCount());

    communicator.respondToScanBarcodeRequest(request, new ScanBarcodeResult("", ""), null);

    waitTillListenerHasBeenCalled();

    Assert.assertNull(communicator.listenerManager.getAndRemoveListenerForMessageId(request.getMessageId()));
    Assert.assertEquals(0, communicator.listenerManager.getRegisteredListenersCount());
  }


  protected HostInfo createLocalHostServerInfo() {
    HostInfo hostInfo = HostInfo.fromUserAndDevice(loggedOnUser, localDevice);
    hostInfo.setAddress(TestIpAddress);
    hostInfo.setMessagesPort(CommunicatorPort);

    return hostInfo;
  }

  protected AskForDeviceRegistrationResponse createAskForDeviceRegistrationResponseFromRequest(AskForDeviceRegistrationRequest request) {
    AskForDeviceRegistrationResponse response = new AskForDeviceRegistrationResponse(true, true, true, request.getUser(), request.getGroup(),
        request.getDevice(), request.getCurrentDeepThoughtInfo(), request.getAddress(), request.getPort());
    response.setRequestMessageId(request.getMessageId());

    return response;
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


  protected MessagesReceiverListener receiverListener = new MessagesReceiverListener() {

    @Override
    public boolean messageReceived(String methodName, Request request) {
      receivedRequests.put(methodName, request);
      waitLatch.countDown();
      return true;
    }

  };

}
