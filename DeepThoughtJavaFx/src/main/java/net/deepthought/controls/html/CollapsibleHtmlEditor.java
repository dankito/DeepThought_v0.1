package net.deepthought.controls.html;

import net.deepthought.Application;
import net.deepthought.communication.listener.CaptureImageOrDoOcrResponseListener;
import net.deepthought.communication.listener.ConnectedDevicesListener;
import net.deepthought.communication.model.ConnectedDevice;
import net.deepthought.controls.CollapsiblePane;
import net.deepthought.controls.ICleanUp;
import net.deepthought.data.contentextractor.ocr.TextRecognitionResult;
import net.deepthought.data.model.Device;
import net.deepthought.util.IconManager;
import net.deepthought.util.JavaFxLocalization;

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

/**
 * Created by ganymed on 13/09/15.
 */
public class CollapsibleHtmlEditor extends CollapsiblePane implements ICleanUp {


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

    ImageView icon = new ImageView(IconManager.getInstance().getIconForOperatingSystem(device.getPlatform(), device.getOsVersion(), device.getPlatformArchitecture()));
    icon.setPreserveRatio(true);
    icon.setFitHeight(24);
    icon.maxHeight(24);
//    icon.setUserData(connectedDevice);

//    pnConnectedDevices.getChildren().add(icon);
//    HBox.setMargin(icon, new Insets(0, 4, 0, 0));

    Label label = new Label(null, icon);
    label.setUserData(connectedDevice);
    JavaFxLocalization.bindControlToolTip(label, "connected.device.tool.tip", connectedDevice.getDevice().getPlatform(), connectedDevice.getDevice().getOsVersion(),
        connectedDevice.getAddress(), connectedDevice.hasCaptureDevice(), connectedDevice.canDoOcr());

    pnConnectedDevices.getChildren().add(label);
    HBox.setMargin(label, new Insets(0, 4, 0, 0));
    label.setOnContextMenuRequested(event -> createConnectedDeviceContextMenu(connectedDevice, label));
  }

  protected void createConnectedDeviceContextMenu(final ConnectedDevice connectedDevice, Node icon) {
    ContextMenu contextMenu = new ContextMenu();

    if(connectedDevice.hasCaptureDevice()) {
      MenuItem captureImageMenuItem = new MenuItem(); // TODO: add icon
      JavaFxLocalization.bindMenuItemText(captureImageMenuItem, "capture.image");
      captureImageMenuItem.setOnAction(event -> Application.getDeepThoughtsConnector().getCommunicator().startCaptureImage(connectedDevice, captureImageOrDoOcrResponseListener));
      contextMenu.getItems().add(captureImageMenuItem);
    }

    if(connectedDevice.canDoOcr()) {
      MenuItem captureImageMenuItem = new MenuItem(); // TODO: add icon
      JavaFxLocalization.bindMenuItemText(captureImageMenuItem, "do.ocr");
      captureImageMenuItem.setOnAction(event -> {
        // TODO: load image which text should be recognized
//        Application.getDeepThoughtsConnector().getCommunicator().startCaptureImage(connectedDevice, captureImageOrDoOcrResponseListener);
      });
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

  protected CaptureImageOrDoOcrResponseListener captureImageOrDoOcrResponseListener = new CaptureImageOrDoOcrResponseListener() {
    @Override
    public void ocrResult(final TextRecognitionResult ocrResult) {
      if(ocrResult.recognitionSuccessful())
        Platform.runLater(() -> htmlEditor.setHtml(htmlEditor.getHtml() + ocrResult.getRecognizedText()));
    }
  };



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
