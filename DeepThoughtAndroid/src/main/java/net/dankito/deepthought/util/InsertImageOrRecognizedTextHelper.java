package net.dankito.deepthought.util;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AlertDialog;

import net.dankito.deepthought.AndroidHelper;
import net.dankito.deepthought.Application;
import net.dankito.deepthought.R;
import net.dankito.deepthought.adapter.AddImageOrOcrTextOptionsListAdapter;
import net.dankito.deepthought.communication.model.DoOcrConfiguration;
import net.dankito.deepthought.communication.model.OcrSource;
import net.dankito.deepthought.controls.html.AndroidHtmlEditor;
import net.dankito.deepthought.data.contentextractor.ocr.RecognizeTextListener;
import net.dankito.deepthought.data.contentextractor.ocr.TextRecognitionResult;
import net.dankito.deepthought.data.html.ImageElementData;
import net.dankito.deepthought.data.model.FileLink;
import net.dankito.deepthought.helper.AlertHelper;
import net.dankito.deepthought.util.file.FileUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ganymed on 06/09/16.
 */
public class InsertImageOrRecognizedTextHelper {

  public static final int TakePhotoRequestCode = 10;
  public static final int SelectPhotosFromGalleryRequestCode = 11;


  private static final Logger log = LoggerFactory.getLogger(InsertImageOrRecognizedTextHelper.class);


  protected Activity activity;

  protected Uri takenPhotoTempFile = null;

  protected AndroidHtmlEditor htmlEditorForLastInsertImageCommand = null;


  public InsertImageOrRecognizedTextHelper(Activity activity) {
    this.activity = activity;
  }


  public void addImageOrOcrTextToHtmlEditor(final AndroidHtmlEditor htmlEditor) {
    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
    builder = builder.setAdapter(new AddImageOrOcrTextOptionsListAdapter(activity), new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialogInterface, int index) {
        addImageOrOcrTextOptionsSelected(index, htmlEditor);
      }
    });

    builder.setNegativeButton(R.string.cancel, null);

    builder.create().show();
  }

  protected void addImageOrOcrTextOptionsSelected(int index, AndroidHtmlEditor htmlEditor) {
    if(index == 1) {
      insertPhotoFromCamera(htmlEditor);
    }
    else if(index == 2) {
      insertPhotoFromGallery(htmlEditor);
    }
    else if(index == 4) {
      recognizeTextFromCapturedPhoto(htmlEditor);
    }
    else if(index == 5) {
      recognizeTextFromPhotoFromGallery(htmlEditor);
    }
  }

  protected void insertPhotoFromCamera(AndroidHtmlEditor htmlEditor) {
    htmlEditorForLastInsertImageCommand = htmlEditor;

    takenPhotoTempFile = AndroidHelper.takePhoto(activity, TakePhotoRequestCode);
  }

  protected void insertPhotoFromGallery(AndroidHtmlEditor htmlEditor) {
    htmlEditorForLastInsertImageCommand = htmlEditor;

    AndroidHelper.selectImagesFromGallery(activity, SelectPhotosFromGalleryRequestCode);
  }

  protected void recognizeTextFromCapturedPhoto(final AndroidHtmlEditor htmlEditor) {
    if(Application.getContentExtractorManager().hasOcrContentExtractors() == false) {
      return;
    }

    Application.getContentExtractorManager().getPreferredOcrContentExtractor().recognizeTextAsync(new DoOcrConfiguration(OcrSource.CaptureImage), new RecognizeTextListener() {
      @Override
      public void textRecognized(TextRecognitionResult result) {
        InsertImageOrRecognizedTextHelper.this.textRecognized(result, htmlEditor);
      }
    });
  }

  protected void recognizeTextFromPhotoFromGallery(final AndroidHtmlEditor htmlEditor) {
    if(Application.getContentExtractorManager().hasOcrContentExtractors() == false) {
      return;
    }

    Application.getContentExtractorManager().getPreferredOcrContentExtractor().recognizeTextAsync(new DoOcrConfiguration(OcrSource.SelectAnExistingImageOnDevice), new RecognizeTextListener() {
      @Override
      public void textRecognized(TextRecognitionResult result) {
        InsertImageOrRecognizedTextHelper.this.textRecognized(result, htmlEditor);
      }
    });
  }

  protected void textRecognized(TextRecognitionResult result, AndroidHtmlEditor htmlEditor) {
    try {
      if (result.isUserCancelled() || (result.isDone() && result.getRecognizedText() == null)) {
        // nothing to do, user knows that he/she cancelled capturing/recognition
      }
      else if (result.recognitionSuccessful() == false) {
        AlertHelper.showErrorMessage(activity, result.getErrorMessage());
      } else {
        htmlEditor.insertHtml(result.getRecognizedText());
      }
    } catch(Exception ex) {
      log.error("Could not handle TextRecognitionResult " + result, ex);
    }
  }


  public boolean canHandleActivityResult(int requestCode, int resultCode, Intent data) {
    if(requestCode == TakePhotoRequestCode) {
      handleTakePhotoResult(resultCode);
      return true;
    }
    else if(requestCode == SelectPhotosFromGalleryRequestCode) {
      handleSelectPhotosFromGalleryResult(resultCode, data);
      return true;
    }

    return false;
  }

  protected void handleTakePhotoResult(int resultCode) {
    if(resultCode == activity.RESULT_OK) {
      if (takenPhotoTempFile != null) {
        FileLink imageFile = FileUtils.moveFileToCapturedImagesFolder(takenPhotoTempFile.toString());
        embedImageInHtmlEditor(imageFile);
      }
    }

    takenPhotoTempFile = null;
  }

  protected void handleSelectPhotosFromGalleryResult(int resultCode, Intent data) {
    if(resultCode == activity.RESULT_OK) {
      try {
        FileLink imageFile = new FileLink(data.getDataString());
        embedImageInHtmlEditor(imageFile);
      } catch (Exception ex) {
        log.error("Could not read select file from uri " + data.getDataString(), ex);
        // TODO: send error response
      }
    }
  }

  protected void embedImageInHtmlEditor(FileLink imageFile) {
    if(htmlEditorForLastInsertImageCommand != null) {
      Application.getDeepThought().addFile(imageFile);
      ImageElementData imageData = new ImageElementData(imageFile);

      htmlEditorForLastInsertImageCommand.insertHtml(imageData.createHtmlCode());
    }

    htmlEditorForLastInsertImageCommand = null;
  }

}
