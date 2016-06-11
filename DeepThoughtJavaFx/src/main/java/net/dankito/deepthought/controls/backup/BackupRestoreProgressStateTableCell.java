package net.dankito.deepthought.controls.backup;

import net.dankito.deepthought.data.backup.RestoreBackupStepResult;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.ImageView;

/**
 * Created by ganymed on 10/01/15.
 */
public class BackupRestoreProgressStateTableCell extends BackupRestoreProgressTableCell {


  protected static ImageView IconError = new ImageView("icons/icon_error_100x100.png");
  protected static ImageView IconOk = new ImageView("icons/icon_ok_100x100.png");

  protected static ProgressIndicator ProgressIndicator = new ProgressIndicator();


  static {
    IconError.setPreserveRatio(true);
    IconError.setFitHeight(32);

    IconOk.setPreserveRatio(true);
    IconOk.setFitHeight(32);

    ProgressIndicator.setMaxHeight(32);
  }


  protected Node graphic;


  public BackupRestoreProgressStateTableCell() {
    setAlignment(Pos.CENTER);
    setText(null);
    setGraphic(null);
  }


  @Override
  protected void updateItem(String item, boolean empty) {
    super.updateItem(item, empty);

    if(empty)
      setGraphic(null);
    else
      setGraphic(graphic);
  }

  protected void itemChangedToNull() {
    if(progressItem != null)
      progressItem.removeRestoreProgressItemListener(restoreProgressItemListener);

    graphic = null;
    setGraphic(null);
    updateItem(null, true);
  }

  protected void itemChangedToProgressItem(net.dankito.deepthought.controller.RestoreBackupDialogController.RestoreProgressItem progressItem) {
    progressItem.addRestoreProgressItemListener(restoreProgressItemListener);
    setProgressItemStateIcon();
  }

  protected void setProgressItemStateIcon() {
    if(progressItem == null)
      graphic = null;
    else if(progressItem.isInProgress())
      graphic = createProgressIndicator();
    else {
      if(progressItem.getResult().successful())
        graphic = createOkIcon();
      else
        graphic = createErrorIcon();
    }

    setGraphic(graphic);
    updateItem("", graphic == null);
  }

  protected ProgressIndicator createProgressIndicator() {
    ProgressIndicator progressIndicator = new ProgressIndicator();
    progressIndicator.setMaxHeight(32);

    return progressIndicator;
  }

  protected ImageView createErrorIcon() {
    ImageView iconError = new ImageView("icons/icon_error_100x100.png");
    iconError.setPreserveRatio(true);
    iconError.setFitHeight(32);

    return iconError;
  }

  protected ImageView createOkIcon() {
    ImageView iconOk = new ImageView("icons/icon_ok_100x100.png");
    iconOk.setPreserveRatio(true);
    iconOk.setFitHeight(32);

    return iconOk;
  }

  net.dankito.deepthought.controller.RestoreBackupDialogController.RestoreProgressItem.RestoreProgressItemListener restoreProgressItemListener = new net.dankito.deepthought.controller.RestoreBackupDialogController.RestoreProgressItem.RestoreProgressItemListener() {
    @Override
    public void resultRetrieved(RestoreBackupStepResult result) {
      Platform.runLater(new Runnable() {
        @Override
        public void run() {
          setProgressItemStateIcon();
        }
      });
    }
  };

}
