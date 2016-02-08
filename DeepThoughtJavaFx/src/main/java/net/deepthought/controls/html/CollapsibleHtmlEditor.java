package net.deepthought.controls.html;

import net.deepthought.Application;
import net.deepthought.communication.listener.ImportFilesResultListener;
import net.deepthought.communication.listener.OcrResultListener;
import net.deepthought.communication.messages.request.DoOcrRequest;
import net.deepthought.communication.messages.request.ImportFilesRequest;
import net.deepthought.communication.messages.response.ImportFilesResultResponse;
import net.deepthought.communication.messages.response.OcrResultResponse;
import net.deepthought.controls.CollapsiblePane;
import net.deepthought.controls.ICleanUp;
import net.deepthought.controls.connected_devices.CaptureImageOrDoOcrConnectedDevicesPanel;
import net.deepthought.controls.utils.FXUtils;
import net.deepthought.data.contentextractor.ocr.ImportFilesResult;
import net.deepthought.data.html.ImageElementData;
import net.deepthought.data.model.FileLink;
import net.deepthought.util.file.FileUtils;
import net.deepthought.util.localization.JavaFxLocalization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;

/**
 * Created by ganymed on 13/09/15.
 */
public class CollapsibleHtmlEditor extends CollapsiblePane implements ICleanUp {

  protected final static Logger log = LoggerFactory.getLogger(CollapsibleHtmlEditor.class);


  protected DeepThoughtFxHtmlEditor htmlEditor = null;

  protected Label lblTitle = null;

  protected CaptureImageOrDoOcrConnectedDevicesPanel connectedDevicesPanel = null;


  public CollapsibleHtmlEditor() {
    this(null);
  }

  public CollapsibleHtmlEditor(IHtmlEditorListener listener) {
    this(null, listener);
  }

  public CollapsibleHtmlEditor(String title, IHtmlEditorListener listener) {
    setupControl(title, listener);
  }


  @Override
  public void cleanUp() {
    connectedDevicesPanel.cleanUp();

    setContent(null); // remove from Parent
    Application.getHtmlEditorPool().htmlEditorReleased(htmlEditor);
  }


  protected void setupControl(String title, IHtmlEditorListener listener) {
    htmlEditor = (DeepThoughtFxHtmlEditor)Application.getHtmlEditorPool().getHtmlEditor(listener);
    htmlEditor.setMaxHeight(FXUtils.SizeMaxValue);
    setContent(htmlEditor);

    setupTitle(title);
  }

  protected void setupTitle(String title) {
    GridPane titlePane = new GridPane();
//    titlePane.setPrefHeight(USE_COMPUTED_SIZE);
    titlePane.setMaxHeight(22);
    titlePane.setMaxWidth(FXUtils.SizeMaxValue);

    titlePane.getRowConstraints().add(new RowConstraints(22, USE_COMPUTED_SIZE, USE_COMPUTED_SIZE, Priority.ALWAYS, VPos.CENTER, true));
    titlePane.getColumnConstraints().add(new ColumnConstraints(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE, USE_COMPUTED_SIZE, Priority.ALWAYS, HPos.LEFT, false));
    titlePane.getColumnConstraints().add(new ColumnConstraints(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE, FXUtils.SizeMaxValue, Priority.ALWAYS, HPos.LEFT, true));
    titlePane.getColumnConstraints().add(new ColumnConstraints(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE, USE_COMPUTED_SIZE, Priority.ALWAYS, HPos.RIGHT, false));
    titlePane.getColumnConstraints().add(new ColumnConstraints(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE, USE_COMPUTED_SIZE, Priority.ALWAYS, HPos.RIGHT, false));

    if(title != null) {
      lblTitle = new Label();
      JavaFxLocalization.bindLabeledText(lblTitle, title);
      titlePane.add(lblTitle, 0, 0);
    }

    connectedDevicesPanel = new CaptureImageOrDoOcrConnectedDevicesPanel(importFilesResultListener, ocrResultListener);
    titlePane.add(connectedDevicesPanel, 3, 0);

    setTitle(titlePane);
  }


  protected void ocrResultReceived(OcrResultResponse ocrResult) {
    if(ocrResult.getTextRecognitionResult() != null && ocrResult.getTextRecognitionResult().recognitionSuccessful())
      Platform.runLater(() -> htmlEditor.setHtml(htmlEditor.getHtml() + ocrResult.getTextRecognitionResult().getRecognizedText(), false));
    // TODO: show error message (or has it already been shown at this time?)
  }

  protected ImportFilesResultListener importFilesResultListener = new ImportFilesResultListener() {
    @Override
    public void responseReceived(ImportFilesRequest request, ImportFilesResultResponse response) {
      if (response.getResult() != null && response.getResult().successful())
        imageSuccessfullyCaptured(response.getResult());
      // TODO: show error message (or has it already been shown at this time?)
    }
  };

  protected OcrResultListener ocrResultListener = new OcrResultListener() {
    @Override
    public void responseReceived(DoOcrRequest doOcrRequest, OcrResultResponse ocrResult) {
      ocrResultReceived(ocrResult);
      // TODO: show error message (or has it already been shown at this time?)
    }
  };

  protected void imageSuccessfullyCaptured(ImportFilesResult importFilesResult) {
    FileLink imageFile = FileUtils.createCapturedImageFile();
    try {
      log.debug("Writing captured Image to file ...");
      if(importFilesResult.getFileUri() != null)
        FileUtils.copyFile(new File(importFilesResult.getFileUri()), new File(imageFile.getUriString())); // TODO: move or copy file
      else if(importFilesResult.getFileData() != null)
        FileUtils.writeToFile(importFilesResult.getFileData(), imageFile);
      log.debug("Wrote to file, adding it to DeepThought ...");
      Application.getDeepThought().addFile(imageFile);

      log.debug("Inserting it into HtmlEditor ...");
      final ImageElementData imageElementData = new ImageElementData(imageFile);
      Platform.runLater(() -> htmlEditor.insertHtml(imageElementData.createHtmlCode()));
    } catch(Exception ex) {
      log.error("Could not save Captured Image to file " + imageFile.getUriString(), ex);
    }
  }


  public DeepThoughtFxHtmlEditor getHtmlEditor() {
    return htmlEditor;
  }

  public String getHtml() {
    return htmlEditor.getHtml();
  }

  public void setHtml(String html) {
    setHtml(html, false);
  }

  public void setHtml(String html, boolean resetUndoStack) {
    htmlEditor.setHtml(html, resetUndoStack);
  }

}
