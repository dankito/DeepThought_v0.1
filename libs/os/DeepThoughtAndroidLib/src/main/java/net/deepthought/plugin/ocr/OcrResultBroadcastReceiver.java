package net.deepthought.plugin.ocr;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import net.deepthought.data.contentextractor.ocr.RecognizeTextListener;
import net.deepthought.data.contentextractor.ocr.TextRecognitionResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ganymed on 18/08/15.
 */
public class OcrResultBroadcastReceiver extends BroadcastReceiver {

  public final static String SEND_OCR_RESULT_INTENT_ACTION = "TextFairyOcrResult";

  public final static String IS_USER_CANCELLED_OCR_RESULT_EXTRA_NAME = "UserCancelled";
  public final static String RECOGNITION_SUCCESSFUL_OCR_RESULT_EXTRA_NAME = "RecognitionSuccessful";
  public final static String ERROR_MESSAGE_OCR_RESULT_EXTRA_NAME = "ErrorMessage";

  public final static String ACCURACY_OCR_RESULT_EXTRA_NAME = "Accuracy";
  public final static String IS_DONE_OCR_RESULT_EXTRA_NAME = "IsDone";

  public final static String HOCR_OCR_RESULT_EXTRA_NAME = "HOCR";
  public final static String UTF8_OCR_RESULT_EXTRA_NAME = "Utf8";


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
      ocrResultIntentFilter = new IntentFilter(SEND_OCR_RESULT_INTENT_ACTION);
//      ocrResultIntentFilter.addDataType("text/plain");

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
    if(intent.hasExtra(IS_USER_CANCELLED_OCR_RESULT_EXTRA_NAME)) {
      context.unregisterReceiver(this);
      return TextRecognitionResult.createUserCancelledResult();
    }
    else if(intent.hasExtra(IS_DONE_OCR_RESULT_EXTRA_NAME)) {
      context.unregisterReceiver(this);
      return TextRecognitionResult.createRecognitionProcessDoneResult();
    }

    boolean recognitionSuccessful = intent.getBooleanExtra(RECOGNITION_SUCCESSFUL_OCR_RESULT_EXTRA_NAME, false);

    if(recognitionSuccessful == false)
      return TextRecognitionResult.createErrorOccurredResult(intent.getStringExtra(ERROR_MESSAGE_OCR_RESULT_EXTRA_NAME));
    else {
      float accuracy = intent.getFloatExtra(ACCURACY_OCR_RESULT_EXTRA_NAME, 0f);
      boolean isDone = intent.getBooleanExtra(IS_DONE_OCR_RESULT_EXTRA_NAME, false);

      String hocr = intent.getStringExtra(HOCR_OCR_RESULT_EXTRA_NAME);
      String utf8String = intent.getStringExtra(UTF8_OCR_RESULT_EXTRA_NAME);

      return TextRecognitionResult.createRecognitionSuccessfulResult(utf8String, accuracy, isDone);
    }
  }
}
