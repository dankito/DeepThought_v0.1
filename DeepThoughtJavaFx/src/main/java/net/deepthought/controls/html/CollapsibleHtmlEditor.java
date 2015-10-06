package net.deepthought.controls.html;

import net.deepthought.Application;
import net.deepthought.communication.listener.CaptureImageOrDoOcrResponseListener;
import net.deepthought.communication.listener.ConnectedDevicesListener;
import net.deepthought.communication.model.ConnectedDevice;
import net.deepthought.controls.CollapsiblePane;
import net.deepthought.controls.ICleanUp;
import net.deepthought.data.contentextractor.ocr.CaptureImageResult;
import net.deepthought.data.contentextractor.ocr.TextRecognitionResult;
import net.deepthought.data.html.ImageElementData;
import net.deepthought.data.model.Device;
import net.deepthought.data.model.FileLink;
import net.deepthought.util.IconManager;
import net.deepthought.util.JavaFxLocalization;
import net.deepthought.util.Localization;
import net.deepthought.util.ObjectHolder;
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

    if(title != null) {
      lblTitle = new Label();
      JavaFxLocalization.bindLabeledText(lblTitle, title);
      titlePane.add(lblTitle, 0, 0);
    }

    titlePane.add(pnConnectedDevices, 2, 0);

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
      captureImageMenuItem.setOnAction(event -> Application.getDeepThoughtsConnector().getCommunicator().startCaptureImage(connectedDevice, captureImageOrDoOcrResponseListener));
      contextMenu.getItems().add(captureImageMenuItem);
    }

    // TODO: load image which text should be recognized
    if(connectedDevice.canDoOcr()) {
      MenuItem captureImageMenuItem = new MenuItem(); // TODO: add icon
      JavaFxLocalization.bindMenuItemText(captureImageMenuItem, "do.ocr");
      captureImageMenuItem.setOnAction(event -> selectImagesForOcr(connectedDevice));
      contextMenu.getItems().add(captureImageMenuItem);
    }

    if(connectedDevice.hasCaptureDevice() && connectedDevice.canDoOcr()) {
      MenuItem captureImageMenuItem = new MenuItem(); // TODO: add icon
      JavaFxLocalization.bindMenuItemText(captureImageMenuItem, "capture.image.and.do.ocr");
      captureImageMenuItem.setOnAction(event -> Application.getDeepThoughtsConnector().getCommunicator().startCaptureImageAndDoOcr(connectedDevice, captureImageOrDoOcrResponseListener));
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
    final Queue<File> imagesToRecognize = new LinkedList<>(selectedFiles);
    final int totalAmountOfImages = selectedFiles.size();
    final AtomicInteger currentImageIndex = new AtomicInteger(0);
    final ObjectHolder<File> currentImage = new ObjectHolder(imagesToRecognize.poll());
//    lblDoOcrProgress.setText(Localization.getLocalizedString("count.images.processed", 0, totalAmountOfImages));
//    lblDoOcrProgress.setVisible(true);

    Application.getDeepThoughtsConnector().getCommunicator().startDoOcr(connectedDevice, currentImage.get(), false, false, new CaptureImageOrDoOcrResponseListener() {
      @Override
      public void captureImageResult(CaptureImageResult captureImageResult) {

      }

      @Override
      public void ocrResult(TextRecognitionResult ocrResult) {
//        lblDoOcrProgress.setText(Localization.getLocalizedString("count.images.processed", currentImageIndex.incrementAndGet(), totalAmountOfImages));
        captureImageOrDoOcrResponseListener.ocrResult(ocrResult);

        if (imagesToRecognize.size() > 0) {
          currentImage.set(imagesToRecognize.poll());
          Application.getDeepThoughtsConnector().getCommunicator().startDoOcr(connectedDevice, currentImage.get(), false, false, this);
        } else {
//          lblDoOcrProgress.setVisible(false);
        }
      }
    });
  }

  protected CaptureImageOrDoOcrResponseListener captureImageOrDoOcrResponseListener = new CaptureImageOrDoOcrResponseListener() {
    @Override
    public void captureImageResult(CaptureImageResult captureImageResult) {
      if (captureImageResult.successful())
        imageSuccessfullyCaptured(captureImageResult);
      // TODO: show error message (or has it already been shown at this time?)
    }

    @Override
    public void ocrResult(final TextRecognitionResult ocrResult) {
      if(ocrResult.recognitionSuccessful())
        Platform.runLater(() -> htmlEditor.setHtml(htmlEditor.getHtml() + ocrResult.getRecognizedText()));
      // TODO: show error message (or has it already been shown at this time?)
    }
  };

  protected void imageSuccessfullyCaptured(CaptureImageResult captureImageResult) {
    FileLink imageFile = FileUtils.createCapturedImageFile();
    try {
      log.debug("Writing captured Image to file ...");
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
    htmlEditor.setHtml(html);
  }

}
