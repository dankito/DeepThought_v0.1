package net.dankito.deepthought.plugin.ocr;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import net.dankito.deepthought.communication.model.OcrSource;
import net.dankito.deepthought.data.contentextractor.ocr.RecognizeTextListener;
import net.dankito.deepthought.data.contentextractor.ocr.TextRecognitionResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ganymed on 18/08/15.
 */
public class OcrResultBroadcastReceiver extends BroadcastReceiver {


  private final static Logger log = LoggerFactory.getLogger(OcrResultBroadcastReceiver.class);


  protected Context context;

  protected RecognizeTextListener listener;

  protected IntentFilter ocrResultIntentFilter;


  public OcrResultBroadcastReceiver(Context context, RecognizeTextListener listener) {
    this.context = context;
    this.listener = listener;

    createIntentFilter(context);
  }

  protected void createIntentFilter(Context context) {
    try {
      ocrResultIntentFilter = new IntentFilter(Constants.SEND_OCR_RESULT_INTENT_ACTION);

      context.registerReceiver(this, ocrResultIntentFilter);
    } catch(Exception ex) {
      log.error("Could not create IntentFilter", ex);
    }
  }

  @Override
  public void onReceive(Context context, Intent intent) {
    if(listener != null)
      listener.textRecognized(createRecognitionResult(intent));
  }

  protected TextRecognitionResult createRecognitionResult(Intent intent) {
    if(isProcessDone(intent)) {
      context.unregisterReceiver(this);
      return TextRecognitionResult.createRecognitionProcessDoneResult();
    }

    return createSuccessfulOrErrorRecognitionResult(intent);
  }

  protected boolean isProcessDone(Intent intent) {
    return intent.hasExtra(Constants.IS_DONE_OCR_RESULT_EXTRA_NAME);
  }

  protected TextRecognitionResult createSuccessfulOrErrorRecognitionResult(Intent intent) {
    boolean recognitionSuccessful = intent.getBooleanExtra(Constants.RECOGNITION_SUCCESSFUL_OCR_RESULT_EXTRA_NAME, false);

    if(recognitionSuccessful == false)
      return TextRecognitionResult.createErrorOccurredResult(intent.getStringExtra(Constants.ERROR_MESSAGE_OCR_RESULT_EXTRA_NAME));
    else {
      return createRecognitionSuccessfulResult(intent);
    }
  }

  protected TextRecognitionResult createRecognitionSuccessfulResult(Intent intent) {
    float accuracy = intent.getFloatExtra(Constants.ACCURACY_OCR_RESULT_EXTRA_NAME, 0f);
    boolean isDone = intent.getBooleanExtra(Constants.IS_DONE_OCR_RESULT_EXTRA_NAME, false);

    String hocr = intent.getStringExtra(Constants.HOCR_OCR_RESULT_EXTRA_NAME);
    String utf8String = intent.getStringExtra(Constants.UTF8_OCR_RESULT_EXTRA_NAME);

    String ocrSourceString = intent.getStringExtra(Constants.OCR_SOURCE_EXTRA_NAME);
    OcrSource ocrSource = getOcrSourceFromString(ocrSourceString);
    String ocrSourceUri = intent.getStringExtra(Constants.OCR_SOURCE_URI_EXTRA_NAME);

    return TextRecognitionResult.createRecognitionSuccessfulResult(utf8String, accuracy, isDone);
  }

  protected OcrSource getOcrSourceFromString(String ocrSourceString) {
    if(Constants.OCR_SOURCE_CAPTURE_IMAGE.equals(ocrSourceString)) {
      return OcrSource.CaptureImage;
    }
    else if(Constants.OCR_SOURCE_SELECT_AN_EXISTING_IMAGE_FROM_DEVICE.equals(ocrSourceString)) {
      return OcrSource.SelectAnExistingImageOnDevice;
    }
    else if(Constants.OCR_SOURCE_RECOGNIZE_FROM_URI.equals(ocrSourceString)) {
      return OcrSource.RecognizeFromUri;
    }

    return OcrSource.AskUser;
  }
}
