package net.dankito.deepthought.controls.connected_devices;

import net.dankito.deepthought.Application;
import net.dankito.deepthought.communication.connected_device.ConnectedRegisteredDevicesListener;
import net.dankito.deepthought.communication.model.ConnectedDevice;
import net.dankito.deepthought.controls.ICleanUp;
import net.dankito.deepthought.data.model.Device;
import net.dankito.deepthought.util.localization.Localization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

/**
 * Created by ganymed on 07/12/15.
 */
public class ConnectedDevicesPanel extends HBox implements ICleanUp {

  private static final Logger log = LoggerFactory.getLogger(ConnectedDevicesPanel.class);


  protected Map<String, Node> connectedDeviceIcons = new HashMap<>();


  public ConnectedDevicesPanel() {
    init();
  }

  protected void init() {
    initUi();

    Application.getDeepThoughtConnector().addConnectedDevicesListener(connectedDevicesListener);

    for(ConnectedDevice connectedDevice : Application.getDeepThoughtConnector().getConnectedDevicesManager().getConnectedDevices()) {
      if(checkIfConnectedDeviceShouldBeShown(connectedDevice))
        addConnectedDeviceIcon(connectedDevice);
    }
  }

  protected void initUi() {
    this.setAlignment(Pos.CENTER_LEFT);
  }


  protected boolean checkIfConnectedDeviceShouldBeShown(ConnectedDevice connectedDevice) {
    // maybe overwritten in subclass to filter devices
    return true;
  }

  @Override
  public void cleanUp() {
    connectedDeviceIcons.clear();
    getChildren().clear();

    if(Application.getDeepThoughtConnector() != null) {
      Application.getDeepThoughtConnector().removeConnectedDevicesListener(connectedDevicesListener);
    }
  }


  protected ConnectedRegisteredDevicesListener connectedDevicesListener = new ConnectedRegisteredDevicesListener() {

    @Override
    public void registeredDeviceConnected(ConnectedDevice device) {
      if(checkIfConnectedDeviceShouldBeShown(device)) {
        Platform.runLater(() -> addConnectedDeviceIcon(device));
      }
    }

    @Override
    public void registeredDeviceDisconnected(ConnectedDevice device) {
      Platform.runLater(() -> removeConnectedDeviceIcon(device));
    }
  };

  protected void addConnectedDeviceIcon(final ConnectedDevice connectedDevice) {
    if(connectedDeviceIcons.containsKey(connectedDevice.getDeviceUniqueId()))
      return;

    Device device = connectedDevice.getDevice();
    if(device == null) { // TODO: how can that ever happen?
      log.error("Got a ConnectedDevice " + connectedDevice + ", but its Device property is null");
      return;
    }

    ImageView icon = new ImageView(net.dankito.deepthought.util.IconManager.getInstance().getIconForOperatingSystem(device.getPlatform(), device.getOsVersion(), device.getPlatformArchitecture()));
    icon.setPreserveRatio(true);
    icon.setFitHeight(24);
    icon.maxHeight(24);
//    icon.setUserData(connectedDevice);

    final Label label = new Label(null, icon);
    label.setUserData(connectedDevice); // add a Label so that we can set a ToolTip (ImageViews have no ToolTips)
    net.dankito.deepthought.util.localization.JavaFxLocalization.bindControlToolTip(label, "connected.device.tool.tip", connectedDevice.getDevice().getTextRepresentation(),
        connectedDevice.getAddress(), booleanToString(connectedDevice.hasCaptureDevice()), booleanToString(connectedDevice.canDoOcr()));

    this.getChildren().add(label);
    HBox.setMargin(label, new Insets(0, 4, 0, 0));
    label.setOnContextMenuRequested(event -> createConnectedDeviceContextMenu(connectedDevice, label));
    label.setOnMouseClicked(event -> {
      createConnectedDeviceContextMenu(connectedDevice, label);
      event.consume();
    });

    connectedDeviceIcons.put(connectedDevice.getDeviceUniqueId(), label);
  }

  protected String booleanToString(boolean bool) {
    if(bool) {
      return Localization.getLocalizedString("yes");
    }
    else {
      return Localization.getLocalizedString("no");
    }
  }

  protected void createConnectedDeviceContextMenu(final ConnectedDevice connectedDevice, Node icon) {
    ContextMenu contextMenu = new ContextMenu();

    addItemsToConnectedDeviceContextMenu(connectedDevice, contextMenu);

    contextMenu.show(icon, Side.BOTTOM, 0, 0);
  }

  protected void addItemsToConnectedDeviceContextMenu(ConnectedDevice connectedDevice, ContextMenu contextMenu) {
    // maybe overwritten in subclass
  }

  protected void removeConnectedDeviceIcon(ConnectedDevice device) {
    if(connectedDeviceIcons.containsKey(device.getDeviceUniqueId())) {
      Node icon = connectedDeviceIcons.remove(device.getDeviceUniqueId());
      getChildren().remove(icon);
    }

//    for(Node node : this.getChildren()) {
//      if(/*node instanceof ImageView &&*/ device.equals(node.getUserData())) { // TODO: will this ever return true as ConnectedDevice instance should be a different one than in  registeredDeviceConnected event
//        this.getChildren().remove(node); // TODO: will foreach loop throw exception immediately or at next iteration (which would be ok than; but must be that way)
//        break;
//      }
//    }
  }

}
