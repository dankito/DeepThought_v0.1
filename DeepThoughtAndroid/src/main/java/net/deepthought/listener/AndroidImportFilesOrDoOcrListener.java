package net.deepthought.listener;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import net.deepthought.AndroidHelper;
import net.deepthought.Application;
import net.deepthought.R;
import net.deepthought.communication.listener.ImportFilesOrDoOcrListener;
import net.deepthought.communication.listener.ResponseListener;
import net.deepthought.communication.messages.request.DoOcrRequest;
import net.deepthought.communication.messages.request.ImportFilesRequest;
import net.deepthought.communication.messages.request.Request;
import net.deepthought.communication.messages.request.RequestWithAsynchronousResponse;
import net.deepthought.communication.messages.request.StopRequestWithAsynchronousResponse;
import net.deepthought.communication.messages.response.Response;
import net.deepthought.communication.messages.response.ResponseCode;
import net.deepthought.communication.messages.response.ScanBarcodeResult;
import net.deepthought.communication.model.ImportFilesConfiguration;
import net.deepthought.communication.model.ImportFilesSource;
import net.deepthought.data.contentextractor.ocr.ImportFilesResult;
import net.deepthought.data.contentextractor.ocr.RecognizeTextListener;
import net.deepthought.data.contentextractor.ocr.TextRecognitionResult;
import net.deepthought.data.model.FileLink;
import net.deepthought.util.file.FileUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;

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
  protected static FileLink temporaryImageFile = null;
  protected static RequestWithAsynchronousResponse captureImageRequest = null;

  protected static ImportFilesRequest importFilesRequest = null;

  protected static RequestWithAsynchronousResponse scanBarcodeRequest = null;


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
    temporaryImageFile = AndroidHelper.takePhoto(context, CaptureImageForConnectPeerRequestCode);
    if(temporaryImageFile != null)
      this.captureImageRequest = request; // TODO: in this way only the last of several simultaneous Requests can be send back to caller
  }

  protected void selectImagesFromGalleryAndSendToCaller(ImportFilesRequest request) {
    this.importFilesRequest = request;

    Intent i = new Intent(Intent.ACTION_GET_CONTENT, null);

    // TODO: set Files types either to Html compatible types or that ones in request parameter
    if (Build.VERSION.SDK_INT >= 19) {
      i.setType("image/*");
      i.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/png", "image/jpg", "image/jpeg"});
    } else {
      i.setType("image/png,image/jpg, image/jpeg");
    }

    Intent chooser = Intent.createChooser(i, context.getString(R.string.image_source));
    context.startActivityForResult(chooser, SelectImageFromGalleryForConnectPeerRequestCode);
  }

  protected void doOcrAndSendToCaller(final DoOcrRequest request) {
    if(Application.getContentExtractorManager().hasOcrContentExtractors()) {
      Application.getContentExtractorManager().getPreferredOcrContentExtractor().recognizeTextAsync(request.getConfiguration(), new RecognizeTextListener() {
        @Override
        public void textRecognized(TextRecognitionResult result) {
          Application.getDeepThoughtsConnector().getCommunicator().respondToDoOcrRequest(request, result, new ResponseListener() {
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
      if (captureImageRequest != null && temporaryImageFile != null) {
        File imageFile = new File(temporaryImageFile.getUriString());
        try {
          byte[] imageData = FileUtils.readFile(imageFile);
          Application.getDeepThoughtsConnector().getCommunicator().respondToImportFilesRequest(captureImageRequest, new ImportFilesResult(imageData, true), null);
        } catch (Exception ex) {
          log.error("Could not read captured photo from temp file " + temporaryImageFile.getUriString(), ex);
          // TODO: send error response
        }

        imageFile.delete();
      }
    }

    temporaryImageFile = null;
    captureImageRequest = null;
  }

  public void handleSelectImageFromGalleryResult(int resultCode, Intent data) {
    if(resultCode == Activity.RESULT_OK) { // TODO: what to do in error case? Send Error Message?
      if(importFilesRequest != null) {
        String uri = data.getDataString();

        try {
          InputStream selectedFileStream = context.getContentResolver().openInputStream(data.getData());
          byte[] fileData = readDataFromInputStream(selectedFileStream);
          Application.getDeepThoughtsConnector().getCommunicator().respondToImportFilesRequest(importFilesRequest, new ImportFilesResult(fileData, true), null);
        } catch (Exception ex) {
          log.error("Could not read select file from uri " + uri, ex);
          // TODO: send error response
        }
      }
    }

    importFilesRequest = null;
  }

  protected byte[] readDataFromInputStream(InputStream inputStream) throws Exception{
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    int nRead;
    byte[] data = new byte[16384];

    while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
      buffer.write(data, 0, nRead);
    }

    buffer.flush();

    return buffer.toByteArray();
  }

  public void handleScanBarCodeResult(int requestCode, int resultCode, Intent data) {
    if(scanBarcodeRequest != null) {
      IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
      if (result != null) {
        Application.getDeepThoughtsConnector().getCommunicator().respondToScanBarcodeRequest(scanBarcodeRequest,
            new ScanBarcodeResult(result.getContents(), result.getFormatName()), null);
      }
    }

    scanBarcodeRequest = null;
  }

}
