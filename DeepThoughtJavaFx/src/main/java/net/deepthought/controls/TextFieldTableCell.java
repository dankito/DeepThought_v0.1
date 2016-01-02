package net.deepthought.controls;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.control.TableCell;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * Created by ganymed on 27/11/14.
 */
public abstract class TextFieldTableCell<T> extends TableCell<T, String> {

  protected final static Logger log = LoggerFactory.getLogger(TextFieldTableCell.class);


  protected TextField textField;

  protected boolean editingStarted = false;


  public TextFieldTableCell() {
    tableRowProperty().addListener((observable, oldValue, newValue) -> {
      newValue.itemProperty().addListener((observable2, oldValue2, newValue2) -> newItemSet((T)newValue2));
    });
  }

  protected void newItemSet(T newValue) {

  }


  public String getItemTextRepresentation() {
    return getItem() == null ? "" : getItem().toString();
  }

  protected String getItemEditingTextFieldText() {
    return getItemTextRepresentation();
  }

  protected abstract void editingItemDone(String newValue, String oldValue);


  /*      Edit Cell text      */

  @Override
  public void startEdit() {
    super.startEdit();

    if(editingStarted == true) // there must be a Bug in JavaFX: startEdit() is called twice, on real start and on committing -> filter out second call
      return;

    editingStarted = true;
    if (textField == null) {
      createTextField();
    }
    if(isEmpty() == false)
      textField.setText(getItemEditingTextFieldText());
    log.debug("startEdit() has been called, textField's text has been set to {}; isEmpty = {}", textField.getText(), isEmpty());

    showCellInEditingState();

    textField.selectAll();
    textField.requestFocus();
  }

  @Override
  public void cancelEdit() {
    super.cancelEdit();

    editingStarted = false;
    showCellInNotEditingState();
  }

  @Override
  public void commitEdit(String newValue) {
    super.commitEdit(newValue);
    editingStarted = false;
  }

  @Override
  public void updateItem(String item, boolean empty) {
    super.updateItem(item, empty);

    if (empty) {
      setText(null);
      setGraphic(null);
    }
    else {
      if (isEditing()) {
        if (textField != null) {
          textField.setText(getItemEditingTextFieldText());
        }
        showCellInEditingState();
      }
      else {
        showCellInNotEditingState();
      }
    }
  }

  protected void showCellInNotEditingState() {
    //    setGraphic(getTreeItem().getGraphic());
    setGraphic(null);
    setText(getItemTextRepresentation());
  }

  protected void showCellInEditingState() {
    setText(null);
    setGraphic(textField);
  }

  protected void createTextField() {
    textField = new TextField(getItemEditingTextFieldText());
    textField.setId("txtfldEditCell");
    textField.setOnKeyReleased(new EventHandler<KeyEvent>() {
      @Override
      public void handle(KeyEvent t) {
        if (t.getCode() == KeyCode.ENTER) {
          if(textField.getText().equals(getItemEditingTextFieldText()) == false)
            editingItemDone(textField.getText(), getItemEditingTextFieldText());

          // throws an exception!
//          commitEdit(getItem());
          // as a workaround a call cancelEdit() and tell item manually to update itself
          cancelEdit();
          updateItem(getItem(), getItemTextRepresentation().isEmpty());
        }
        else if (t.getCode() == KeyCode.ESCAPE) {
          cancelEdit();
        }
      }
    });

    textField.focusedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        if(newValue == false)
          cancelEdit();
      }
    });
  }

}
