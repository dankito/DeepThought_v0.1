package net.deepthought.controls.html;

import net.deepthought.Application;
import net.deepthought.communication.listener.CaptureImageAndDoOcrResultListener;
import net.deepthought.communication.listener.CaptureImageResultListener;
import net.deepthought.communication.listener.ConnectedDevicesListener;
import net.deepthought.communication.listener.DoOcrOnImageResultListener;
import net.deepthought.communication.messages.request.DoOcrOnImageRequest;
import net.deepthought.communication.messages.request.RequestWithAsynchronousResponse;
import net.deepthought.communication.messages.response.CaptureImageResultResponse;
import net.deepthought.communication.messages.response.OcrResultResponse;
import net.deepthought.communication.model.ConnectedDevice;
import net.deepthought.communication.model.DoOcrConfiguration;
import net.deepthought.controls.CollapsiblePane;
import net.deepthought.controls.ICleanUp;
import net.deepthought.data.contentextractor.ocr.CaptureImageResult;
import net.deepthought.data.html.ImageElementData;
import net.deepthought.data.model.Device;
import net.deepthought.data.model.FileLink;
import net.deepthought.util.IconManager;
import net.deepthought.util.JavaFxLocalization;
import net.deepthought.util.Localization;
import net.deepthought.util.file.FileUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.stage.FileChooser;

/**
 * Created by ganymed on 13/09/15.
 */
public class CollapsibleHtmlEditor extends CollapsiblePane implements ICleanUp {

  protected final static Logger log = LoggerFactory.getLogger(CollapsibleHtmlEditor.class);


  protected DeepThoughtFxHtmlEditor htmlEditor = null;

  protected Label lblTitle = null;

  protected Label lblDoOcrProgress = null;

  protected HBox pnConnectedDevices = new HBox();


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
    Application.getDeepThoughtsConnector().removeConnectedDevicesListener(connectedDevicesListener);

    setContent(null); // remove from Parent
    DeepThoughtFxHtmlEditorPool.getInstance().htmlEditorReleased(htmlEditor);
  }


  protected void setupControl(String title, IHtmlEditorListener listener) {
    htmlEditor = DeepThoughtFxHtmlEditorPool.getInstance().getHtmlEditor(listener);
    htmlEditor.setMaxHeight(Double.MAX_VALUE);
    setContent(htmlEditor);

    setupTitle(title);

    setupConnectedDevicesControls();
  }

  protected void setupTitle(String title) {
    GridPane titlePane = new GridPane();
//    titlePane.setPrefHeight(USE_COMPUTED_SIZE);
    titlePane.setMaxHeight(22);
    titlePane.setMaxWidth(Double.MAX_VALUE);

    titlePane.getRowConstraints().add(new RowConstraints(22, USE_COMPUTED_SIZE, USE_COMPUTED_SIZE, Priority.ALWAYS, VPos.CENTER, true));
    titlePane.getColumnConstraints().add(new ColumnConstraints(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE, USE_COMPUTED_SIZE, Priority.ALWAYS, HPos.LEFT, false));
    titlePane.getColumnConstraints().add(new ColumnConstraints(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE, Double.MAX_VALUE, Priority.ALWAYS, HPos.LEFT, true));
    titlePane.getColumnConstraints().add(new ColumnConstraints(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE, USE_COMPUTED_SIZE, Priority.ALWAYS, HPos.RIGHT, false));
    titlePane.getColumnConstraints().add(new ColumnConstraints(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE, USE_COMPUTED_SIZE, Priority.ALWAYS, HPos.RIGHT, false));

    if(title != null) {
      lblTitle = new Label();
      JavaFxLocalization.bindLabeledText(lblTitle, title);
      titlePane.add(lblTitle, 0, 0);
    }

    lblDoOcrProgress = new Label();
    titlePane.add(lblDoOcrProgress, 2, 0);

    titlePane.add(pnConnectedDevices, 3, 0);

    setTitle(titlePane);
  }

  protected void setupConnectedDevicesControls() {
    Application.getDeepThoughtsConnector().addConnectedDevicesListener(connectedDevicesListener);

    for(ConnectedDevice connectedDevice : Application.getDeepThoughtsConnector().getConnectedDevicesManager().getConnectedDevices()) {
      if(connectedDevice.hasCaptureDevice() || connectedDevice.canDoOcr())
        addConnectedDeviceIcon(connectedDevice);
    }
  }


//  @Override
//  protected void setExpandedState() {
//    if(isExpanded())
//      setMaxHeight(Double.MAX_VALUE);
//    else
//      setMaxHeight(22);
//
//    super.setExpandedState();
//  }

  protected ConnectedDevicesListener connectedDevicesListener = new ConnectedDevicesListener() {

    @Override
    public void registeredDeviceConnected(ConnectedDevice device) {
      Platform.runLater(() -> addConnectedDeviceIcon(device));
    }

    @Override
    public void registeredDeviceDisconnected(ConnectedDevice device) {
      Platform.runLater(() -> removeConnectedDeviceIcon(device));
    }
  };

  protected void addConnectedDeviceIcon(final ConnectedDevice connectedDevice) {
    Device device = connectedDevice.getDevice();
    if(device == null) { // TODO: how can that ever happen?
      log.error("Got a ConnectedDevice " + connectedDevice + ", but its Device property is null");
      return;
    }

    ImageView icon = new ImageView(IconManager.getInstance().getIconForOperatingSystem(device.getPlatform(), device.getOsVersion(), device.getPlatformArchitecture()));
    icon.setPreserveRatio(true);
    icon.setFitHeight(24);
    icon.maxHeight(24);
//    icon.setUserData(connectedDevice);

    final Label label = new Label(null, icon);
    label.setUserData(connectedDevice); // add a Label so that we can set a ToolTip (ImageViews have no ToolTips)
    JavaFxLocalization.bindControlToolTip(label, "connected.device.tool.tip", connectedDevice.getDevice().getPlatform(), connectedDevice.getDevice().getOsVersion(),
        connectedDevice.getAddress(), connectedDevice.hasCaptureDevice(), connectedDevice.canDoOcr());

    pnConnectedDevices.getChildren().add(label);
    HBox.setMargin(label, new Insets(0, 4, 0, 0));
    label.setOnContextMenuRequested(event -> createConnectedDeviceContextMenu(connectedDevice, label));
    label.setOnMouseClicked(event -> {
      createConnectedDeviceContextMenu(connectedDevice, label);
      event.consume();
    });
  }

  protected void createConnectedDeviceContextMenu(final ConnectedDevice connectedDevice, Node icon) {
    ContextMenu contextMenu = new ContextMenu();

    if(connectedDevice.hasCaptureDevice()) {
      MenuItem captureImageMenuItem = new MenuItem(); // TODO: add icon
      JavaFxLocalization.bindMenuItemText(captureImageMenuItem, "capture.image");
      // TODO: store requestMessageId so that Capturing Image process can be stopped
      captureImageMenuItem.setOnAction(event -> Application.getDeepThoughtsConnector().getCommunicator().startCaptureImage(connectedDevice, captureImageResultListener));
      contextMenu.getItems().add(captureImageMenuItem);
    }

    if(connectedDevice.canDoOcr()) {
      MenuItem captureImageMenuItem = new MenuItem(); // TODO: add icon
      JavaFxLocalization.bindMenuItemText(captureImageMenuItem, "do.ocr");
      captureImageMenuItem.setOnAction(event -> selectImagesForOcr(connectedDevice));
      contextMenu.getItems().add(captureImageMenuItem);
    }

    if(connectedDevice.hasCaptureDevice() && connectedDevice.canDoOcr()) {
      MenuItem captureImageMenuItem = new MenuItem(); // TODO: add icon
      JavaFxLocalization.bindMenuItemText(captureImageMenuItem, "capture.image.and.do.ocr");
      // TODO: store requestMessageId so that Capturing Image and Doing OCR process can be stopped
      captureImageMenuItem.setOnAction(event -> Application.getDeepThoughtsConnector().getCommunicator().startCaptureImageAndDoOcr(connectedDevice, captureImageAndDoOcrResultListener));
      contextMenu.getItems().add(captureImageMenuItem);
    }

    contextMenu.show(icon, Side.BOTTOM, 0, 0);
  }

  protected void removeConnectedDeviceIcon(ConnectedDevice device) {
    for(Node node : pnConnectedDevices.getChildren()) {
      if(/*node instanceof ImageView &&*/ device.equals(node.getUserData())) { // TODO: will this ever return true as ConnectedDevice instance should be a different one than in  registeredDeviceConnected event
        pnConnectedDevices.getChildren().remove(node); // TODO: will foreach loop throw exception immediately or at next iteration (which would be ok than; but must be that way)
        break;
      }
    }
  }

  protected void selectImagesForOcr(ConnectedDevice connectedDevice) {
    // TODO: set file filter to Html compatible image types
    FileChooser fileChooser = new FileChooser();
    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(Localization.getLocalizedString("image.files") + " (*.jpg, *.png)",
        "*.jpg", "*.png"));

    List<File> selectedFiles = fileChooser.showOpenMultipleDialog(null);
    if(selectedFiles != null && selectedFiles.size() > 0) {
      doOcrForSelectedFiles(connectedDevice, selectedFiles);
    }
  }

  protected void doOcrForSelectedFiles(final ConnectedDevice connectedDevice, List<File> selectedFiles) {
    // TODO: what if doOcrForSelectedFiles() gets called a second time before first call has finished? Than there exist two parallel imagesToRecognize -> avoid
    final Queue<File> imagesToRecognize = new LinkedList<>(selectedFiles);
    final AtomicInteger currentImageIndex = new AtomicInteger(0);
    lblDoOcrProgress.setText(Localization.getLocalizedString("count.images.processed", 0, imagesToRecognize.size()));
    lblDoOcrProgress.setVisible(true);

    doOcrOnNextImage(connectedDevice, imagesToRecognize, currentImageIndex);
  }

  protected void doOcrOnNextImage(final ConnectedDevice connectedDevice, final Queue<File> imagesToRecognize, final AtomicInteger currentImageIndex) {
    File imageToRecognize = imagesToRecognize.poll();
    currentImageIndex.incrementAndGet();

    try {
      final DoOcrConfiguration configuration = new DoOcrConfiguration(imageToRecognize, false, false);

      Application.getDeepThoughtsConnector().getCommunicator().startDoOcrOnImage(connectedDevice, configuration, new DoOcrOnImageResultListener() {
        @Override
        public void responseReceived(DoOcrOnImageRequest doOcrOnImageRequest, OcrResultResponse result) {
          receivedOcrResultForImage(connectedDevice, imagesToRecognize, currentImageIndex, result);
        }
      });
    } catch(Exception ex) {
      log.error("Could not read Image file " + imageToRecognize.getAbsolutePath() + " and do OCR on it", ex);
      doOcrOnNextImage(connectedDevice, imagesToRecognize, currentImageIndex);
      // TODO: show error message to User
    }
  }

  protected void receivedOcrResultForImage(ConnectedDevice connectedDevice, final Queue<File> imagesToRecognize, final AtomicInteger currentImageIndex, OcrResultResponse ocrResult) {
    ocrResultReceived(ocrResult);

    if (ocrResult.isDone() && imagesToRecognize.size() > 0) {
      doOcrOnNextImage(connectedDevice, imagesToRecognize, currentImageIndex);
    }

    Platform.runLater(() -> {
      lblDoOcrProgress.setText(Localization.getLocalizedString("count.images.processed", currentImageIndex.get(), imagesToRecognize.size()));
      if (imagesToRecognize.size() == 0)
        lblDoOcrProgress.setVisible(false);
    });
  }

  protected void ocrResultReceived(OcrResultResponse ocrResult) {
    if(ocrResult.getTextRecognitionResult() != null && ocrResult.getTextRecognitionResult().recognitionSuccessful())
      Platform.runLater(() -> htmlEditor.setHtml(htmlEditor.getHtml() + ocrResult.getTextRecognitionResult().getRecognizedText(), false));
    // TODO: show error message (or has it already been shown at this time?)
  }

  protected CaptureImageResultListener captureImageResultListener = new CaptureImageResultListener() {
    @Override
    public void responseReceived(RequestWithAsynchronousResponse requestWithAsynchronousResponse, CaptureImageResultResponse response) {
      if (response.getResult() != null && response.getResult().successful())
        imageSuccessfullyCaptured(response.getResult());
      // TODO: show error message (or has it already been shown at this time?)
    }
  };

  protected CaptureImageAndDoOcrResultListener captureImageAndDoOcrResultListener = new CaptureImageAndDoOcrResultListener() {
    @Override
    public void responseReceived(RequestWithAsynchronousResponse requestWithAsynchronousResponse, OcrResultResponse ocrResult) {
      ocrResultReceived(ocrResult);
      // TODO: show error message (or has it already been shown at this time?)
    }
  };

  protected void imageSuccessfullyCaptured(CaptureImageResult captureImageResult) {
    FileLink imageFile = FileUtils.createCapturedImageFile();
    try {
      log.debug("Writing captured Image to file ...");
      if(captureImageResult.getImageUri() != null)
        FileUtils.copyFile(new File(captureImageResult.getImageUri()), new File(imageFile.getUriString())); // TODO: move or copy file
      else if(captureImageResult.getImageData() != null)
        FileUtils.writeToFile(captureImageResult.getImageData(), imageFile);
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
