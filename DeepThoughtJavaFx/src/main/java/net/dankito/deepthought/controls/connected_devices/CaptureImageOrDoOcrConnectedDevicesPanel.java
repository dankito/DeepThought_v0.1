package net.dankito.deepthought.controls.connected_devices;

import net.dankito.deepthought.Application;
import net.dankito.deepthought.communication.listener.ImportFilesResultListener;
import net.dankito.deepthought.communication.listener.OcrResultListener;
import net.dankito.deepthought.communication.messages.request.DoOcrRequest;
import net.dankito.deepthought.communication.messages.response.OcrResultResponse;
import net.dankito.deepthought.communication.model.ConnectedDevice;
import net.dankito.deepthought.communication.model.DoOcrConfiguration;
import net.dankito.deepthought.communication.model.ImportFilesConfiguration;
import net.dankito.deepthought.communication.model.ImportFilesSource;
import net.dankito.deepthought.communication.model.OcrSource;
import net.dankito.deepthought.controls.utils.FXUtils;
import net.dankito.deepthought.util.localization.Localization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;

/**
 * Created by ganymed on 07/12/15.
 */
public class CaptureImageOrDoOcrConnectedDevicesPanel extends ConnectedDevicesPanel {

  private static final Logger log = LoggerFactory.getLogger(CaptureImageOrDoOcrConnectedDevicesPanel.class);


  protected ImportFilesResultListener importFilesResultListener = null;

  protected OcrResultListener ocrResultListener = null;

  protected Label lblDoOcrProgress = null;


  public CaptureImageOrDoOcrConnectedDevicesPanel(ImportFilesResultListener importFilesResultListener, OcrResultListener ocrResultListener) {
    this.importFilesResultListener = importFilesResultListener;
    this.ocrResultListener = ocrResultListener;

    initLabelDoOcrProgress();
  }


  @Override
  public void cleanUp() {
    this.importFilesResultListener = null;
    this.ocrResultListener = null;

    super.cleanUp();
  }

  protected void initLabelDoOcrProgress() {
    lblDoOcrProgress = new Label();
    lblDoOcrProgress.setVisible(false);
    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(lblDoOcrProgress);

    this.getChildren().add(0, lblDoOcrProgress);

    HBox.setMargin(lblDoOcrProgress, new Insets(0, 6, 0, 0));
  }

  protected boolean checkIfConnectedDeviceShouldBeShown(ConnectedDevice connectedDevice) {
    return connectedDevice.hasCaptureDevice() || connectedDevice.canDoOcr();
  }

  protected void addItemsToConnectedDeviceContextMenu(ConnectedDevice connectedDevice, ContextMenu contextMenu) {
    addImportFilesMenuItem(connectedDevice, contextMenu);

    if(connectedDevice.hasCaptureDevice()) {
      addCaptureImageMenuItem(connectedDevice, contextMenu);
    }

    if(connectedDevice.canDoOcr()) {
      contextMenu.getItems().add(new SeparatorMenuItem());

      addSelectLocalImageAndDoOcrMenuItem(connectedDevice, contextMenu);

      if(connectedDevice.hasCaptureDevice()) {
        addCaptureImageAndDoOcrMenuItem(connectedDevice, contextMenu);
      }

      addChooseRemoteImageAndDoOcrMenuItem(connectedDevice, contextMenu);
    }
  }

  protected void addImportFilesMenuItem(ConnectedDevice connectedDevice, ContextMenu contextMenu) {
    MenuItem importFilesMenuItem = new MenuItem(); // TODO: add icon
    net.dankito.deepthought.util.localization.JavaFxLocalization.bindMenuItemText(importFilesMenuItem, "import.files");
    importFilesMenuItem.setGraphic(createMenuItemIcon(net.dankito.deepthought.controls.Constants.ImportFilesIconPath));
    // TODO: store requestMessageId so that Capturing Image process can be stopped
    importFilesMenuItem.setOnAction(event -> Application.getDeepThoughtConnector().getCommunicator().startImportFiles(connectedDevice, new ImportFilesConfiguration(ImportFilesSource.SelectFromExistingFiles), importFilesResultListener));
    contextMenu.getItems().add(importFilesMenuItem);
  }

  protected void addCaptureImageMenuItem(ConnectedDevice connectedDevice, ContextMenu contextMenu) {
    MenuItem captureImageMenuItem = new MenuItem(); // TODO: add icon
    net.dankito.deepthought.util.localization.JavaFxLocalization.bindMenuItemText(captureImageMenuItem, "capture.image");
    captureImageMenuItem.setGraphic(createMenuItemIcon(net.dankito.deepthought.controls.Constants.CameraIconPath));
    // TODO: store requestMessageId so that Capturing Image process can be stopped
    captureImageMenuItem.setOnAction(event -> Application.getDeepThoughtConnector().getCommunicator().startImportFiles(connectedDevice, new ImportFilesConfiguration(ImportFilesSource.CaptureImage), importFilesResultListener));
    contextMenu.getItems().add(captureImageMenuItem);
  }

  protected void addSelectLocalImageAndDoOcrMenuItem(ConnectedDevice connectedDevice, ContextMenu contextMenu) {
    MenuItem selectLocalImageAndDoOcrMenuItem = new MenuItem(); // TODO: add icon
    net.dankito.deepthought.util.localization.JavaFxLocalization.bindMenuItemText(selectLocalImageAndDoOcrMenuItem, "select.local.image.and.do.ocr");
    selectLocalImageAndDoOcrMenuItem.setGraphic(createMenuItemIcon(net.dankito.deepthought.controls.Constants.OcrOnLocalImageIconPath));
    selectLocalImageAndDoOcrMenuItem.setOnAction(event -> selectImagesForOcr(connectedDevice));
    contextMenu.getItems().add(selectLocalImageAndDoOcrMenuItem);
  }

  protected void addCaptureImageAndDoOcrMenuItem(ConnectedDevice connectedDevice, ContextMenu contextMenu) {
    MenuItem captureImageAndDoOcrMenuItem = new MenuItem(); // TODO: add icon
    net.dankito.deepthought.util.localization.JavaFxLocalization.bindMenuItemText(captureImageAndDoOcrMenuItem, "capture.image.and.do.ocr");
    captureImageAndDoOcrMenuItem.setGraphic(createMenuItemIcon(net.dankito.deepthought.controls.Constants.CaptureImageAndDoOcrIconPath));
    // TODO: store requestMessageId so that Capturing Image and Doing OCR process can be stopped
    captureImageAndDoOcrMenuItem.setOnAction(event -> Application.getDeepThoughtConnector().getCommunicator().startDoOcr(connectedDevice, new DoOcrConfiguration(OcrSource.CaptureImage), ocrResultListener));
    contextMenu.getItems().add(captureImageAndDoOcrMenuItem);
  }

  protected void addChooseRemoteImageAndDoOcrMenuItem(ConnectedDevice connectedDevice, ContextMenu contextMenu) {
    MenuItem chooseRemoteImageAndDoOcrMenuItem = new MenuItem(); // TODO: add icon
    net.dankito.deepthought.util.localization.JavaFxLocalization.bindMenuItemText(chooseRemoteImageAndDoOcrMenuItem, "chose.remote.image.and.do.ocr");
    chooseRemoteImageAndDoOcrMenuItem.setGraphic(createMenuItemIcon(net.dankito.deepthought.controls.Constants.OcrOnRemoteImageIconPath));
    // TODO: store requestMessageId so that Capturing Image and Doing OCR process can be stopped
    chooseRemoteImageAndDoOcrMenuItem.setOnAction(event -> Application.getDeepThoughtConnector().getCommunicator().startDoOcr(connectedDevice, new DoOcrConfiguration(OcrSource.SelectAnExistingImageOnDevice), ocrResultListener));
    contextMenu.getItems().add(chooseRemoteImageAndDoOcrMenuItem);
  }

  protected ImageView createMenuItemIcon(String iconPath) {
    ImageView icon = new ImageView(iconPath);
    icon.setFitHeight(24);
    icon.setFitWidth(32);

    return icon;
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
      final DoOcrConfiguration configuration = new DoOcrConfiguration(imageToRecognize, true);

      Application.getDeepThoughtConnector().getCommunicator().startDoOcr(connectedDevice, configuration, new OcrResultListener() {
        @Override
        public void responseReceived(DoOcrRequest doOcrRequest, OcrResultResponse result) {
          receivedOcrResultForImage(doOcrRequest, connectedDevice, imagesToRecognize, currentImageIndex, result);
        }
      });
    } catch(Exception ex) {
      log.error("Could not read Image file " + imageToRecognize.getAbsolutePath() + " and do OCR on it", ex);
      doOcrOnNextImage(connectedDevice, imagesToRecognize, currentImageIndex);
      // TODO: show error message to User
    }
  }

  protected void receivedOcrResultForImage(DoOcrRequest doOcrRequest, ConnectedDevice connectedDevice, final Queue<File> imagesToRecognize, final AtomicInteger currentImageIndex, OcrResultResponse ocrResult) {
    ocrResultListener.responseReceived(doOcrRequest, ocrResult);

    if (ocrResult.isDone() && imagesToRecognize.size() > 0) {
      doOcrOnNextImage(connectedDevice, imagesToRecognize, currentImageIndex);
    }

    Platform.runLater(() -> {
      lblDoOcrProgress.setText(Localization.getLocalizedString("count.images.processed", currentImageIndex.get(), imagesToRecognize.size()));
      if (imagesToRecognize.size() == 0)
        lblDoOcrProgress.setVisible(false);
    });
  }

}
