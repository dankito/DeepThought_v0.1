package net.deepthought.controls.connected_devices;

import net.deepthought.Application;
import net.deepthought.communication.messages.response.ScanBarcodeResultResponse;
import net.deepthought.communication.model.ConnectedDevice;
import net.deepthought.util.JavaFxLocalization;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;

/**
 * Created by ganymed on 07/12/15.
 */
public class ScanIsbnConnectedDevicesPanel extends ConnectedDevicesPanel {

  @Override
  protected boolean checkIfConnectedDeviceShouldBeShown(ConnectedDevice connectedDevice) {
    return connectedDevice.canScanBarcodes();
  }

  @Override
  protected void addItemsToConnectedDeviceContextMenu(ConnectedDevice connectedDevice, ContextMenu contextMenu) {
    MenuItem scanBarcodeMenuItem = new MenuItem(); // TODO: add icon
    JavaFxLocalization.bindMenuItemText(scanBarcodeMenuItem, "scan.from.isbn");
    // TODO: store requestMessageId so that Scanning Barcode process can be stopped
    scanBarcodeMenuItem.setOnAction(event -> Application.getDeepThoughtsConnector().getCommunicator().startScanBarcode(connectedDevice,
        (request, response) -> scanBarcodeResultReceived(response)));
    contextMenu.getItems().add(scanBarcodeMenuItem);
  }

  protected void scanBarcodeResultReceived(ScanBarcodeResultResponse response) {

  }
}
