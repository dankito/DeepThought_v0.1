package net.deepthought.controls.importdata;

import net.deepthought.data.persistence.db.BaseEntity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableRow;

/**
 * Created by ganymed on 10/01/15.
 */
public abstract class ImportDataTreeTableCell extends TreeTableCell<Object, String> {

  private final static Logger log = LoggerFactory.getLogger(ImportDataTreeTableCell.class);



  public ImportDataTreeTableCell() {
    tableRowProperty().addListener(new ChangeListener<TreeTableRow<Object>>() {
      @Override
      public void changed(ObservableValue<? extends TreeTableRow<Object>> observable, TreeTableRow<Object> oldValue, TreeTableRow<Object> newValue) {
        if(newValue != null) {
          itemChanged(newValue.getItem());

          newValue.itemProperty().addListener(new ChangeListener<Object>() {
            @Override
            public void changed(ObservableValue<? extends Object> observable, Object oldValue, Object newValue) {
              itemChanged(newValue);
            }
          });
        }
      }
    });
  }


  protected void itemChanged(Object newValue) {
    if(newValue == null) {
      itemChangedToNull();
    }
    else {
      if(newValue instanceof Field) {
        itemChangedToField((Field)newValue);
      }
      else if(newValue instanceof BaseEntity) {
        itemChangedToEntity((BaseEntity)newValue);
      }
    }
  }

  protected abstract void itemChangedToNull();

  protected abstract void itemChangedToField(Field field);

  protected abstract void itemChangedToEntity(BaseEntity entity);

}
