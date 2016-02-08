package net.deepthought.controls.connected_devices;

import net.deepthought.Application;
import net.deepthought.communication.listener.ConnectedDevicesListener;
import net.deepthought.communication.model.ConnectedDevice;
import net.deepthought.controls.ICleanUp;
import net.deepthought.data.model.Device;
import net.deepthought.util.IconManager;
import net.deepthought.util.localization.JavaFxLocalization;
import net.deepthought.util.localization.Localization;

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

    Application.getDeepThoughtsConnector().addConnectedDevicesListener(connectedDevicesListener);

    for(ConnectedDevice connectedDevice : Application.getDeepThoughtsConnector().getConnectedDevicesManager().getConnectedDevices()) {
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

    if(Application.getDeepThoughtsConnector() != null) {
      Application.getDeepThoughtsConnector().removeConnectedDevicesListener(connectedDevicesListener);
    }
  }


  protected ConnectedDevicesListener connectedDevicesListener = new ConnectedDevicesListener() {

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
    if(connectedDeviceIcons.containsKey(connectedDevice.getUniqueDeviceId()))
      return;

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
        connectedDevice.getAddress(), booleanToString(connectedDevice.hasCaptureDevice()), booleanToString(connectedDevice.canDoOcr()));

    this.getChildren().add(label);
    HBox.setMargin(label, new Insets(0, 4, 0, 0));
    label.setOnContextMenuRequested(event -> createConnectedDeviceContextMenu(connectedDevice, label));
    label.setOnMouseClicked(event -> {
      createConnectedDeviceContextMenu(connectedDevice, label);
      event.consume();
    });

    connectedDeviceIcons.put(connectedDevice.getUniqueDeviceId(), label);
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
    if(connectedDeviceIcons.containsKey(device.getUniqueDeviceId())) {
      Node icon = connectedDeviceIcons.remove(device.getUniqueDeviceId());
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
