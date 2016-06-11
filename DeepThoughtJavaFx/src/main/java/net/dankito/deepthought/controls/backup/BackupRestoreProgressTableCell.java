package net.dankito.deepthought.controls.backup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableRow;

/**
 * Created by ganymed on 10/01/15.
 */
public abstract class BackupRestoreProgressTableCell extends TableCell<net.dankito.deepthought.controller.RestoreBackupDialogController.RestoreProgressItem, String> {

  private final static Logger log = LoggerFactory.getLogger(BackupRestoreProgressTableCell.class);


  protected net.dankito.deepthought.controller.RestoreBackupDialogController.RestoreProgressItem progressItem;


  public BackupRestoreProgressTableCell() {
    tableRowProperty().addListener(new ChangeListener<TableRow>() {
      @Override
      public void changed(ObservableValue<? extends TableRow> observable, TableRow oldValue, TableRow newValue) {
        if(newValue != null) {
          itemChanged(newValue.getItem());

          newValue.itemProperty().addListener(new ChangeListener<net.dankito.deepthought.controller.RestoreBackupDialogController.RestoreProgressItem>() {
            @Override
            public void changed(ObservableValue<? extends net.dankito.deepthought.controller.RestoreBackupDialogController.RestoreProgressItem> observable, net.dankito.deepthought.controller.RestoreBackupDialogController.RestoreProgressItem oldValue, net.dankito.deepthought.controller.RestoreBackupDialogController.RestoreProgressItem newValue) {
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
      if(newValue instanceof net.dankito.deepthought.controller.RestoreBackupDialogController.RestoreProgressItem) {
        this.progressItem = (net.dankito.deepthought.controller.RestoreBackupDialogController.RestoreProgressItem)newValue;
        itemChangedToProgressItem(progressItem);
      }
    }
  }

  protected abstract void itemChangedToNull();

  protected abstract void itemChangedToProgressItem(net.dankito.deepthought.controller.RestoreBackupDialogController.RestoreProgressItem progressItem);

}
