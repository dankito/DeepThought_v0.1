package net.dankito.deepthought.listener;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import net.dankito.deepthought.AndroidHelper;
import net.dankito.deepthought.Application;
import net.dankito.deepthought.communication.listener.ImportFilesOrDoOcrListener;
import net.dankito.deepthought.communication.listener.ResponseListener;
import net.dankito.deepthought.communication.messages.request.DoOcrRequest;
import net.dankito.deepthought.communication.messages.request.ImportFilesRequest;
import net.dankito.deepthought.communication.messages.request.Request;
import net.dankito.deepthought.communication.messages.request.RequestWithAsynchronousResponse;
import net.dankito.deepthought.communication.messages.request.StopRequestWithAsynchronousResponse;
import net.dankito.deepthought.communication.messages.response.Response;
import net.dankito.deepthought.communication.messages.response.ResponseCode;
import net.dankito.deepthought.communication.messages.response.ScanBarcodeResult;
import net.dankito.deepthought.communication.model.ImportFilesConfiguration;
import net.dankito.deepthought.communication.model.ImportFilesSource;
import net.dankito.deepthought.data.contentextractor.ocr.ImportFilesResult;
import net.dankito.deepthought.data.contentextractor.ocr.RecognizeTextListener;
import net.dankito.deepthought.data.contentextractor.ocr.TextRecognitionResult;
import net.dankito.deepthought.util.file.FileUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Created by ganymed on 09/02/16.
 */
public class AndroidImportFilesOrDoOcrListener implements ImportFilesOrDoOcrListener {


  public static final int CaptureImageForConnectPeerRequestCode = 7;

  public static final int SelectImageFromGalleryForConnectPeerRequestCode = 8;

  public static final int ScanBarCodeRequestCode = 49374; // TODO: is this really always == 49374


  private static final Logger log = LoggerFactory.getLogger(AndroidImportFilesOrDoOcrListener.class);


  protected Activity context;

  // make them static otherwise the will be cleaned up when starting TakePhoto Activity
  protected Uri capturedPhotoFile = null;
  protected RequestWithAsynchronousResponse captureImageRequest = null;

  protected ImportFilesRequest importFilesRequest = null;

  protected RequestWithAsynchronousResponse scanBarcodeRequest = null;


  public AndroidImportFilesOrDoOcrListener(Activity context) {
    this.context = context;
  }


  @Override
  public void importFiles(ImportFilesRequest request) {
    exportFilesToRemoteDevice(request);
  }

  @Override
  public void doOcr(DoOcrRequest request) {
    doOcrAndSendToCaller(request);
  }

  @Override
  public void scanBarcode(RequestWithAsynchronousResponse request) {
    doScanBarCode(request);
  }

  @Override
  public void stopCaptureImageOrDoOcr(StopRequestWithAsynchronousResponse request) {
    // TODO: implement
  }


  protected void exportFilesToRemoteDevice(ImportFilesRequest request) {
    ImportFilesConfiguration configuration = request.getConfiguration();

    if(configuration.getSource() == ImportFilesSource.CaptureImage) {
      capturePhotoAndSendToCaller(request);
    }
    else if(configuration.getSource() == ImportFilesSource.SelectFromExistingFiles) {
      selectImagesFromGalleryAndSendToCaller(request);
    }
  }

  protected void capturePhotoAndSendToCaller(ImportFilesRequest request) {
    capturedPhotoFile = AndroidHelper.takePhoto(context, CaptureImageForConnectPeerRequestCode);
    if(capturedPhotoFile != null) {
      this.captureImageRequest = request; // TODO: in this way only the last of may several simultaneous Requests can be send back to caller
    }
  }

  protected void selectImagesFromGalleryAndSendToCaller(ImportFilesRequest request) {
    this.importFilesRequest = request;

    AndroidHelper.selectImagesFromGallery(context, SelectImageFromGalleryForConnectPeerRequestCode);
  }

  protected void doOcrAndSendToCaller(final DoOcrRequest request) {
    if(Application.getContentExtractorManager().hasOcrContentExtractors()) {
      Application.getContentExtractorManager().getPreferredOcrContentExtractor().recognizeTextAsync(request.getConfiguration(), new RecognizeTextListener() {
        @Override
        public void textRecognized(TextRecognitionResult result) {
          Application.getDeepThoughtConnector().getCommunicator().respondToDoOcrRequest(request, result, new ResponseListener() {
            @Override
            public void responseReceived(Request request, Response response) {
              if (response.getResponseCode() == ResponseCode.Error) {
                // TODO: stop process then
              }
            }
          });
        }
      });
    }
  }

  protected void doScanBarCode(RequestWithAsynchronousResponse request) {
    scanBarcodeRequest = request;

    new IntentIntegrator(context).setOrientationLocked(false).initiateScan();
  }



  public void handleCaptureImageResult(int resultCode) {
    if(resultCode == Activity.RESULT_OK) {
      if (captureImageRequest != null && capturedPhotoFile != null) {
        File imageFile = new File(capturedPhotoFile.getPath());
        try {
          byte[] imageData = FileUtils.readFile(imageFile);
          Application.getDeepThoughtConnector().getCommunicator().respondToImportFilesRequest(captureImageRequest, new ImportFilesResult(imageData, true), null);
        } catch (Exception ex) {
          log.error("Could not read captured photo from temp file " + capturedPhotoFile, ex);
          // TODO: send error response
        }

        imageFile.delete();
      }
    }

    capturedPhotoFile = null;
    captureImageRequest = null;
  }

  public void handleSelectImageFromGalleryResult(int resultCode, Intent data) {
    if(resultCode == Activity.RESULT_OK) { // TODO: what to do in error case? Send Error Message?
      if(importFilesRequest != null) {
        try {
          byte[] fileData = AndroidHelper.getImageBytesFromIntent(context, data);
          Application.getDeepThoughtConnector().getCommunicator().respondToImportFilesRequest(importFilesRequest, new ImportFilesResult(fileData, true), null);
        } catch (Exception ex) {
          log.error("Could not read select file from uri " + data.getDataString(), ex);
          // TODO: send error response
        }
      }
    }

    importFilesRequest = null;
  }

  public void handleScanBarCodeResult(int requestCode, int resultCode, Intent data) {
    if(scanBarcodeRequest != null) {
      IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
      if (result != null) {
        Application.getDeepThoughtConnector().getCommunicator().respondToScanBarcodeRequest(scanBarcodeRequest,
            new ScanBarcodeResult(result.getContents(), result.getFormatName()), null);
      }
    }

    scanBarcodeRequest = null;
  }

}
