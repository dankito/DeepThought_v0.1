package net.dankito.deepthought.controls.connected_devices;

import net.dankito.deepthought.Application;
import net.dankito.deepthought.communication.messages.response.ScanBarcodeResult;
import net.dankito.deepthought.communication.messages.response.ScanBarcodeResultResponse;
import net.dankito.deepthought.communication.model.ConnectedDevice;
import net.dankito.deepthought.util.localization.Localization;
import net.dankito.deepthought.util.isbn.IsbnResolvingListener;
import net.dankito.deepthought.util.isbn.ResolveIsbnResult;

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
  public void cleanUp() {
    this.listener = null;

    super.cleanUp();
  }

  @Override
  protected boolean checkIfConnectedDeviceShouldBeShown(ConnectedDevice connectedDevice) {
    return connectedDevice.canScanBarcodes();
  }

  @Override
  protected void addItemsToConnectedDeviceContextMenu(ConnectedDevice connectedDevice, ContextMenu contextMenu) {
    MenuItem scanBarcodeMenuItem = new MenuItem(); // TODO: add icon
    net.dankito.deepthought.util.localization.JavaFxLocalization.bindMenuItemText(scanBarcodeMenuItem, "scan.from.isbn");
    // TODO: store requestMessageId so that Scanning Barcode process can be stopped
    scanBarcodeMenuItem.setOnAction(event -> Application.getDeepThoughtConnector().getCommunicator().startScanBarcode(connectedDevice,
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
        net.dankito.deepthought.util.Alerts.showErrorMessage((Stage)getScene().getWindow(), Localization.getLocalizedString("could.not.resolve.isbn", scanBarcodeResult.getDecodedBarcode()),
            resolveIsbnResult.getError().getLocalizedMessage());
      }
    }
    else if(listener != null) {
      net.dankito.deepthought.controller.Dialogs.showEditReferenceDialogAndPersistOnResultOk(resolveIsbnResult, listener);
    }
  }
}
