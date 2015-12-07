package net.deepthought.controls.connected_devices;

import net.deepthought.Application;
import net.deepthought.communication.messages.response.ScanBarcodeResult;
import net.deepthought.communication.messages.response.ScanBarcodeResultResponse;
import net.deepthought.communication.model.ConnectedDevice;
import net.deepthought.controller.Dialogs;
import net.deepthought.util.Alerts;
import net.deepthought.util.JavaFxLocalization;
import net.deepthought.util.Localization;
import net.deepthought.util.isbn.IsbnResolvingListener;
import net.deepthought.util.isbn.ResolveIsbnResult;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;

/**
 * Created by ganymed on 07/12/15.
 */
public class ScanIsbnConnectedDevicesPanel extends ConnectedDevicesPanel {

  protected IsbnResolvingListener listener = null;


  public ScanIsbnConnectedDevicesPanel(IsbnResolvingListener listener) {
    this.listener = listener;
  }


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
    ScanBarcodeResult result = response.getResult();
    if(result.isSuccessful()) {
      Application.getIsbnResolver().resolveIsbnAsync(result.getDecodedBarcode(), new IsbnResolvingListener() {
        @Override
        public void isbnResolvingDone(ResolveIsbnResult resolveIsbnResult) {
          retrievedIsbnResolvingResult(result, resolveIsbnResult);
        }
      });
    }


  }

  protected void retrievedIsbnResolvingResult(ScanBarcodeResult scanBarcodeResult, ResolveIsbnResult resolveIsbnResult) {
    if(resolveIsbnResult.isSuccessful() == false) {
      if(resolveIsbnResult.getError() != null) {
        Alerts.showErrorMessage((Stage)getScene().getWindow(), Localization.getLocalizedString("could.not.resolve.isbn", scanBarcodeResult.getDecodedBarcode()),
            resolveIsbnResult.getError().getLocalizedMessage());
      }
    }
    else if(listener != null) {
      Dialogs.showEditReferenceDialogAndPersistOnResultOk(resolveIsbnResult, listener);
    }
  }
}
