package net.dankito.deepthought.controls;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.event.EventHandler;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * Created by ganymed on 27/11/14.
 */
public abstract class TextFieldTreeCell<T> extends TreeCell<T> {

  private final static Logger log = LoggerFactory.getLogger(TextFieldTreeCell.class);


  protected TextField textField;


  public TextFieldTreeCell() {
    itemProperty().addListener((observable, oldValue, newValue) -> newItemSet(newValue));
  }

  protected void newItemSet(T newValue) {

  }


  protected String getItemTextRepresentation() {
    return getItem() == null ? "" : getItem().toString();
  }

  protected abstract void itemValueUpdated(String newValue, String oldValue);


  /*      Edit Cell text      */

  @Override
  public void startEdit() {
    super.startEdit();

    if (textField == null) {
      createTextField();
    }
    textField.setText(getItemTextRepresentation());

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
          textField.setText(getItemTextRepresentation());
        }
        showCellInEditingState();
      }
      else {
        showCellInNotEditingState();
      }
    }
  }

  protected void showCellInNotEditingState() {
    setGraphic(getTreeItem() == null ? null : getTreeItem().getGraphic());
    setText(getItemTextRepresentation());
  }

  protected void showCellInEditingState() {
    setText(null);
    setGraphic(textField);
  }

  protected void createTextField() {
    textField = new TextField(getItemTextRepresentation());
    textField.setOnKeyReleased(new EventHandler<KeyEvent>() {

      @Override
      public void handle(KeyEvent t) {
        if (t.getCode() == KeyCode.ENTER) {
          if(textField.getText().equals(getItemTextRepresentation()) == false)
            itemValueUpdated(textField.getText(), getItemTextRepresentation());
          commitEdit(getItem());
        }
        else if (t.getCode() == KeyCode.ESCAPE) {
          cancelEdit();
        }
      }
    });

  }

}
