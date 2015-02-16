package net.deepthought.controls.backup;

import net.deepthought.controller.RestoreBackupDialogController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableRow;

/**
 * Created by ganymed on 10/01/15.
 */
public abstract class BackupRestoreProgressTableCell extends TableCell<RestoreBackupDialogController.RestoreProgressItem, String> {

  private final static Logger log = LoggerFactory.getLogger(BackupRestoreProgressTableCell.class);


  protected RestoreBackupDialogController.RestoreProgressItem progressItem;


  public BackupRestoreProgressTableCell() {
    tableRowProperty().addListener(new ChangeListener<TableRow>() {
      @Override
      public void changed(ObservableValue<? extends TableRow> observable, TableRow oldValue, TableRow newValue) {
        if(newValue != null) {
          itemChanged(newValue.getItem());

          newValue.itemProperty().addListener(new ChangeListener<RestoreBackupDialogController.RestoreProgressItem>() {
            @Override
            public void changed(ObservableValue<? extends RestoreBackupDialogController.RestoreProgressItem> observable, RestoreBackupDialogController.RestoreProgressItem oldValue, RestoreBackupDialogController.RestoreProgressItem newValue) {
              itemChanged(newValue);
            }
          });
      }
    }});
  }


  protected void itemChanged(Object newValue) {
    log.debug("Item changed to " + newValue);

    if(newValue == null) {
      this.progressItem = null;
      itemChangedToNull();
    }
    else {
      if(newValue instanceof RestoreBackupDialogController.RestoreProgressItem) {
        this.progressItem = (RestoreBackupDialogController.RestoreProgressItem)newValue;
        itemChangedToProgressItem(progressItem);
      }
    }
  }

  protected abstract void itemChangedToNull();

  protected abstract void itemChangedToProgressItem(RestoreBackupDialogController.RestoreProgressItem progressItem);

}
