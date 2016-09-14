package net.dankito.deepthought.communication;

import net.dankito.deepthought.Application;
import net.dankito.deepthought.TestApplicationConfiguration;
import net.dankito.deepthought.communication.listener.ImportFilesOrDoOcrListener;
import net.dankito.deepthought.communication.messages.request.DoOcrRequest;
import net.dankito.deepthought.communication.messages.request.ImportFilesRequest;
import net.dankito.deepthought.communication.messages.request.RequestWithAsynchronousResponse;
import net.dankito.deepthought.communication.messages.request.StopRequestWithAsynchronousResponse;
import net.dankito.deepthought.communication.model.ConnectedDevice;
import net.dankito.deepthought.communication.model.DoOcrConfiguration;
import net.dankito.deepthought.communication.model.ImportFilesConfiguration;
import net.dankito.deepthought.communication.model.ImportFilesSource;
import net.dankito.deepthought.communication.model.OcrSource;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by ganymed on 19/08/15.
 */
public class DeepThoughtConnectorTest extends CommunicationTestBase {

  protected IDeepThoughtConnector connector;

  protected Communicator communicator;


  @Override
  public void setup() throws Exception {
    super.setup();

    Application.instantiate(new TestApplicationConfiguration());
    try { Thread.sleep(200); } catch(Exception ex) { } // it is very critical that server is fully started therefore wait some time

    connector = Application.getDeepThoughtConnector();
    communicator = connector.getCommunicator();

    loggedOnUser = Application.getLoggedOnUser();
    localDevice = Application.getApplication().getLocalDevice();
    localHost = new ConnectedDevice(localDevice.getUniversallyUniqueId(), TestIpAddress, connector.getMessageReceiverPort());
  }

  @After
  public void tearDown() {
    Application.shutdown();
  }


  @Test
  public void startTwoDeepThoughtsConnectors_BothMessageHandlersStartSuccessfully() {
//    DeepThoughtConnector connector1 = new DeepThoughtConnector(null);
//    connector1.runAsync();

    DeepThoughtConnector connector2 = new DeepThoughtConnector(Application.getDevicesFinder(), threadPool);
    connector2.runAsync();

    try { Thread.sleep(500); } catch(Exception ex) { } // wait same time till Servers have started

    // now assure that both have started successfully even so by default the use the same port to listen on
    Assert.assertTrue(connector.isStarted());
    Assert.assertTrue(connector2.isStarted());
    Assert.assertNotEquals(connector.getMessageReceiverPort(), connector2.getMessageReceiverPort());
  }


  @Test
  public void startCaptureImage_CaptureImageOrDoOcrListenerGetsCalled() {
    final AtomicBoolean methodCalled = new AtomicBoolean(false);
    final CountDownLatch waitLatch = new CountDownLatch(1);

    connector.addImportFilesOrDoOcrListener(new ImportFilesOrDoOcrListener() {

      @Override
      public void importFiles(ImportFilesRequest request) {
        methodCalled.set(true);
        waitLatch.countDown();
      }

      @Override
      public void doOcr(DoOcrRequest request) {

      }

      @Override
      public void scanBarcode(RequestWithAsynchronousResponse request) {

      }

      @Override
      public void stopCaptureImageOrDoOcr(StopRequestWithAsynchronousResponse request) {

      }
    });

    communicator.startImportFiles(localHost, new ImportFilesConfiguration(ImportFilesSource.CaptureImage), null);

    try { waitLatch.await(2, TimeUnit.SECONDS); } catch(Exception ex) { }

    Assert.assertTrue(methodCalled.get());
  }

  @Test
  public void startCaptureImageAndDoOcr_CaptureImageOrDoOcrListenerGetsCalled() {
    final AtomicBoolean methodCalled = new AtomicBoolean(false);
    final CountDownLatch waitLatch = new CountDownLatch(1);

    connector.addImportFilesOrDoOcrListener(new ImportFilesOrDoOcrListener() {

      @Override
      public void importFiles(ImportFilesRequest request) {

      }

      @Override
      public void doOcr(DoOcrRequest request) {
        methodCalled.set(true);
        waitLatch.countDown();
      }

      @Override
      public void scanBarcode(RequestWithAsynchronousResponse request) {

      }

      @Override
      public void stopCaptureImageOrDoOcr(StopRequestWithAsynchronousResponse request) {

      }
    });

    communicator.startDoOcr(localHost, new DoOcrConfiguration(OcrSource.CaptureImage), null);

    try { waitLatch.await(2, TimeUnit.SECONDS); } catch(Exception ex) { }

    Assert.assertTrue(methodCalled.get());
  }


  @Test
  public void startSelectRemoteImageAndDoOcr_CaptureImageOrDoOcrListenerGetsCalled() {
    final AtomicBoolean methodCalled = new AtomicBoolean(false);
    final CountDownLatch waitLatch = new CountDownLatch(1);

    connector.addImportFilesOrDoOcrListener(new ImportFilesOrDoOcrListener() {

      @Override
      public void importFiles(ImportFilesRequest request) {

      }

      @Override
      public void doOcr(DoOcrRequest request) {
        methodCalled.set(true);
        waitLatch.countDown();
      }

      @Override
      public void scanBarcode(RequestWithAsynchronousResponse request) {

      }

      @Override
      public void stopCaptureImageOrDoOcr(StopRequestWithAsynchronousResponse request) {

      }
    });

    communicator.startDoOcr(localHost, new DoOcrConfiguration(OcrSource.SelectAnExistingImageOnDevice), null);

    try { waitLatch.await(2, TimeUnit.SECONDS); } catch(Exception ex) { }

    Assert.assertTrue(methodCalled.get());
  }


  @Test
  public void startRecognizeFromUri_CaptureImageOrDoOcrListenerGetsCalled() {
    final AtomicBoolean methodCalled = new AtomicBoolean(false);
    final CountDownLatch waitLatch = new CountDownLatch(1);

    connector.addImportFilesOrDoOcrListener(new ImportFilesOrDoOcrListener() {

      @Override
      public void importFiles(ImportFilesRequest request) {

      }

      @Override
      public void doOcr(DoOcrRequest request) {
        methodCalled.set(true);
        waitLatch.countDown();
      }

      @Override
      public void scanBarcode(RequestWithAsynchronousResponse request) {

      }

      @Override
      public void stopCaptureImageOrDoOcr(StopRequestWithAsynchronousResponse request) {

      }
    });

    communicator.startDoOcr(localHost, new DoOcrConfiguration(OcrSource.RecognizeFromUri), null);

    try { waitLatch.await(2, TimeUnit.SECONDS); } catch(Exception ex) { }

    Assert.assertTrue(methodCalled.get());
  }


  @Test
  public void startAskUserForSourceAndDoOcr_CaptureImageOrDoOcrListenerGetsCalled() {
    final AtomicBoolean methodCalled = new AtomicBoolean(false);
    final CountDownLatch waitLatch = new CountDownLatch(1);

    connector.addImportFilesOrDoOcrListener(new ImportFilesOrDoOcrListener() {

      @Override
      public void importFiles(ImportFilesRequest request) {

      }

      @Override
      public void doOcr(DoOcrRequest request) {
        methodCalled.set(true);
        waitLatch.countDown();
      }

      @Override
      public void scanBarcode(RequestWithAsynchronousResponse request) {

      }

      @Override
      public void stopCaptureImageOrDoOcr(StopRequestWithAsynchronousResponse request) {

      }
    });

    communicator.startDoOcr(localHost, new DoOcrConfiguration(OcrSource.AskUser), null);

    try { waitLatch.await(2, TimeUnit.SECONDS); } catch(Exception ex) { }

    Assert.assertTrue(methodCalled.get());
  }


  @Test
  public void startDoOcrOnLocalImage_CaptureImageOrDoOcrListenerGetsCalled() {
    final AtomicBoolean methodCalled = new AtomicBoolean(false);
    final CountDownLatch waitLatch = new CountDownLatch(1);

    connector.addImportFilesOrDoOcrListener(new ImportFilesOrDoOcrListener() {

      @Override
      public void importFiles(ImportFilesRequest request) {

      }

      @Override
      public void doOcr(DoOcrRequest request) {
        methodCalled.set(true);
        waitLatch.countDown();
      }

      @Override
      public void scanBarcode(RequestWithAsynchronousResponse request) {

      }

      @Override
      public void stopCaptureImageOrDoOcr(StopRequestWithAsynchronousResponse request) {

      }
    });

    communicator.startDoOcr(localHost, new DoOcrConfiguration(new byte[0]), null);

    try { waitLatch.await(2, TimeUnit.SECONDS); } catch(Exception ex) { }

    Assert.assertTrue(methodCalled.get());
  }


  @Test
  public void startScanBarcode_CaptureImageOrDoOcrListenerGetsCalled() {
    final AtomicBoolean methodCalled = new AtomicBoolean(false);
    final CountDownLatch waitLatch = new CountDownLatch(1);

    connector.addImportFilesOrDoOcrListener(new ImportFilesOrDoOcrListener() {

      @Override
      public void importFiles(ImportFilesRequest request) {

      }

      @Override
      public void doOcr(DoOcrRequest request) {

      }

      @Override
      public void scanBarcode(RequestWithAsynchronousResponse request) {
        methodCalled.set(true);
        waitLatch.countDown();
      }

      @Override
      public void stopCaptureImageOrDoOcr(StopRequestWithAsynchronousResponse request) {

      }
    });

    communicator.startScanBarcode(localHost, null);

    try { waitLatch.await(200, TimeUnit.SECONDS); } catch(Exception ex) { }

    Assert.assertTrue(methodCalled.get());
  }

}
