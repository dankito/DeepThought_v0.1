package net.dankito.deepthought.controls.backup;

import net.dankito.deepthought.Application;
import net.dankito.deepthought.data.backup.RestoreBackupStepResult;
import net.dankito.deepthought.util.localization.Localization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.application.Platform;

/**
 * Created by ganymed on 10/01/15.
 */
public class BackupRestoreProgressMessageTableCell extends BackupRestoreProgressTableCell {

  private final static Logger log = LoggerFactory.getLogger(BackupRestoreProgressMessageTableCell.class);


  protected String message = null;


  public BackupRestoreProgressMessageTableCell() {
    setText(null);
    setGraphic(null);
  }


  @Override
  protected void updateItem(String item, boolean empty) {
    super.updateItem(item, empty);

    if(empty)
      setText(null);
    else
      setText(message);
  }

  protected void itemChangedToNull() {
    if(progressItem != null)
      progressItem.removeRestoreProgressItemListener(restoreProgressItemListener);

    message = null;
    setText(null);
    updateItem(message, true);
  }

  protected void itemChangedToProgressItem(net.dankito.deepthought.controller.RestoreBackupDialogController.RestoreProgressItem progressItem) {
    progressItem.addRestoreProgressItemListener(restoreProgressItemListener);
    setProgressItemMessage();
  }

  protected void setProgressItemMessage() {
    if(progressItem == null)
      message = null;
    else if(progressItem.isInProgress())
      message = progressItem.getStep().toString();
    else {
      if(progressItem.getResult().successful())
        message = progressItem.getStep().toString() + Application.getPlatformConfiguration().getLineSeparator() + Localization.getLocalizedString("successful");
      else
        message = progressItem.getStep().toString() + Application.getPlatformConfiguration().getLineSeparator() + progressItem.getResult().getError().getNotificationMessage();
    }

    setItem(message);
    setText(message);
    updateItem(message, message == null);
  }

  net.dankito.deepthought.controller.RestoreBackupDialogController.RestoreProgressItem.RestoreProgressItemListener restoreProgressItemListener = new net.dankito.deepthought.controller.RestoreBackupDialogController.RestoreProgressItem.RestoreProgressItemListener() {
    @Override
    public void resultRetrieved(RestoreBackupStepResult result) {
      Platform.runLater(new Runnable() {
        @Override
        public void run() {
          setProgressItemMessage();
        }
      });
    }
  };

}
