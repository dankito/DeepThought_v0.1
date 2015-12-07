package net.deepthought.controls.connected_devices;

import net.deepthought.communication.model.ConnectedDevice;

import javafx.scene.control.ContextMenu;

/**
 * Created by ganymed on 07/12/15.
 */
public class ScanIsbnConnectedDevicesPanel extends ConnectedDevicesPanel {

  @Override
  protected boolean checkIfConnectedDeviceShouldBeShown(ConnectedDevice connectedDevice) {
    return false;
  }

  @Override
  protected void addItemsToConnectedDeviceContextMenu(ConnectedDevice connectedDevice, ContextMenu contextMenu) {

  }
}
