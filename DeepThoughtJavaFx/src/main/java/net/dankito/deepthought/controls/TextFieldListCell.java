package net.dankito.deepthought.controls;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.event.EventHandler;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * Created by ganymed on 27/11/14.
 */
public abstract class TextFieldListCell<T> extends ListCell<T> {

  protected final static Logger log = LoggerFactory.getLogger(TextFieldListCell.class);


  protected TextField textField;


  public TextFieldListCell() {
    itemProperty().addListener((observable, oldValue, newValue) -> newItemSet(newValue));
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

    if (textField == null) {
      createTextField();
    }
    textField.setText(getItemEditingTextFieldText());

    showCellInEditingState();

    textField.selectAll();
    textField.requestFocus();
  }

  @Override
  public void cancelEdit() {
    super.cancelEdit();

    showCellInNotEditingState();
  }

  @Override
  public void updateItem(T item, boolean empty) {
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
  }

}
