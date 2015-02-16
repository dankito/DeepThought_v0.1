package net.deepthought.controls;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.EventHandler;
import javafx.scene.control.ComboBox;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.Callback;

/**
 * Created by ganymed on 04/02/15.
 */
public class FXUtils {

  private final static Logger log = LoggerFactory.getLogger(FXUtils.class);


  public static<T> void autoCompleteComboBox(ComboBox<T> comboBox) {
    autoCompleteComboBox(comboBox, null);
  }

  public static<T> void autoCompleteComboBox(final ComboBox<T> comboBox, final Callback<DoesItemMatchSearchTermParam<T>, Boolean> doesItemMatchSearchTerm) {
    final ObservableList<T> allItems = comboBox.getItems();
    final FilteredList<T> filteredItems = new FilteredList<>(allItems, item -> true);
    comboBox.setItems(filteredItems);

    comboBox.setEditable(true);
    comboBox.getEditor().focusedProperty().addListener(observable -> {
      if (comboBox.getSelectionModel().getSelectedIndex() < 0) {
//        comboBox.getEditor().setText(null);
      }
    });
//    comboBox.addEventHandler(KeyEvent.KEY_PRESSED, t -> comboBox.hide());
    comboBox.addEventHandler(KeyEvent.KEY_RELEASED, new EventHandler<KeyEvent>() {

      private boolean moveCaretToPos = false;
      private int caretPos;

      @Override
      public void handle(KeyEvent event) {
        if (event.getCode() == KeyCode.UP) {
          caretPos = -1;
          log.debug("UP has been pressed");
          moveCaret(comboBox.getEditor().getText().length());
          return;
        } else if (event.getCode() == KeyCode.DOWN) {
          if (!comboBox.isShowing()) {
            comboBox.show();
          }
          caretPos = -1;
          log.debug("DOWN has been pressed");
          moveCaret(comboBox.getEditor().getText().length());
          return;
        } else if (event.getCode() == KeyCode.BACK_SPACE) {
//          moveCaretToPos = true;
//          caretPos = comboBox.getEditor().getCaretPosition();
        } else if (event.getCode() == KeyCode.DELETE) {
//          moveCaretToPos = true;
//          caretPos = comboBox.getEditor().getCaretPosition();
        }

        if (event.getCode() == KeyCode.RIGHT || event.getCode() == KeyCode.LEFT
            || event.isControlDown() || event.getCode() == KeyCode.HOME
            || event.getCode() == KeyCode.END || event.getCode() == KeyCode.TAB) {
          return;
        }

//        ObservableList<T> list = FXCollections.observableArrayList();
        String searchTerm = comboBox.getEditor().getText();

//        for (T datum : allItems) {
//          if(datum != null) {
//            if (doesItemMatchSearchTerm != null) {
//              if (doesItemMatchSearchTerm.call(new DoesItemMatchSearchTermParam<T>(searchTerm, datum)))
//                list.add(datum);
//            } else if (datum.toString().toLowerCase().contains(searchTerm.toLowerCase())) {
//              list.add(datum);
//            }
//          }
//        }
//
//        comboBox.setItems(list);
//        comboBox.getEditor().setText(searchTerm);

        filterItems(searchTerm, filteredItems, doesItemMatchSearchTerm);

        if (!moveCaretToPos) {
          caretPos = -1;
        }
//        moveCaret(searchTerm.length());
        if (!filteredItems.isEmpty()) {
          comboBox.show();
        }
      }

      private void moveCaret(int textLength) {
        if (caretPos == -1) {
          log.debug("caretPos == -1, setting caret position to {}", textLength);
          comboBox.getEditor().positionCaret(textLength);
        } else {
          log.debug("Setting caret position to {}", caretPos);
          comboBox.getEditor().positionCaret(caretPos);
        }
        moveCaretToPos = false;
      }
    });
  }

  protected static <T>void filterItems(final String searchTerm, FilteredList<T> filteredItems, final Callback<DoesItemMatchSearchTermParam<T>, Boolean> doesItemMatchSearchTerm) {
    filteredItems.setPredicate((item) -> {
      if(item == null)
        return false;
      // If searchTerm is empty, display all items.
      if (searchTerm == null || searchTerm.isEmpty()) {
        return true;
      }

      if(doesItemMatchSearchTerm != null) {
        if(doesItemMatchSearchTerm.call(new DoesItemMatchSearchTermParam<T>(searchTerm, item)))
          return true;
      }
      if (item.toString().toLowerCase().contains(searchTerm.toLowerCase())) {
        return true; // Filter matches Tag's name
      }
      return false; // Does not match.
    });
  }

//  public static<T> void autoCompleteComboBox(final ComboBox<T> comboBox, final Callback<DoesItemMatchSearchTermParam<T>, Boolean> doesItemMatchSearchTerm) {
//    final ObservableList<T> data = comboBox.getItems();
//
//    comboBox.setEditable(true);
//    comboBox.getEditor().focusedProperty().addListener(observable -> {
//      if (comboBox.getSelectionModel().getSelectedIndex() < 0) {
////        comboBox.getEditor().setText(null);
//      }
//    });
//    comboBox.addEventHandler(KeyEvent.KEY_PRESSED, t -> comboBox.hide());
//    comboBox.addEventHandler(KeyEvent.KEY_RELEASED, new EventHandler<KeyEvent>() {
//
//      private boolean moveCaretToPos = false;
//      private int caretPos;
//
//      @Override
//      public void handle(KeyEvent event) {
//        if (event.getCode() == KeyCode.UP) {
//          caretPos = -1;
//          moveCaret(comboBox.getEditor().getText().length());
//          return;
//        } else if (event.getCode() == KeyCode.DOWN) {
//          if (!comboBox.isShowing()) {
//            comboBox.show();
//          }
//          caretPos = -1;
//          moveCaret(comboBox.getEditor().getText().length());
//          return;
//        } else if (event.getCode() == KeyCode.BACK_SPACE) {
//          moveCaretToPos = true;
//          caretPos = comboBox.getEditor().getCaretPosition();
//        } else if (event.getCode() == KeyCode.DELETE) {
//          moveCaretToPos = true;
//          caretPos = comboBox.getEditor().getCaretPosition();
//        }
//
//        if (event.getCode() == KeyCode.RIGHT || event.getCode() == KeyCode.LEFT
//            || event.isControlDown() || event.getCode() == KeyCode.HOME
//            || event.getCode() == KeyCode.END || event.getCode() == KeyCode.TAB) {
//          return;
//        }
//
//        ObservableList<T> list = FXCollections.observableArrayList();
//        String searchTerm = comboBox.getEditor().getText();
//
//        for (T datum : data) {
//          if(datum != null) {
//            if (doesItemMatchSearchTerm != null) {
//              if (doesItemMatchSearchTerm.call(new DoesItemMatchSearchTermParam<T>(searchTerm, datum)))
//                list.add(datum);
//            } else if (datum.toString().toLowerCase().contains(searchTerm.toLowerCase())) {
//              list.add(datum);
//            }
//          }
//        }
//
//        comboBox.setItems(list);
//        comboBox.getEditor().setText(searchTerm);
//        if (!moveCaretToPos) {
//          caretPos = -1;
//        }
//        moveCaret(searchTerm.length());
//        if (!list.isEmpty()) {
//          comboBox.show();
//        }
//      }
//
//      private void moveCaret(int textLength) {
//        if (caretPos == -1) {
//          comboBox.getEditor().positionCaret(textLength);
//        } else {
//          comboBox.getEditor().positionCaret(caretPos);
//        }
//        moveCaretToPos = false;
//      }
//    });
//  }

  public static<T> T getComboBoxValue(ComboBox<T> comboBox){
    if (comboBox.getSelectionModel().getSelectedIndex() < 0) {
      return null;
    } else {
      return comboBox.getItems().get(comboBox.getSelectionModel().getSelectedIndex());
    }
  }

  public static class DoesItemMatchSearchTermParam<T> {
    protected String searchTerm;
    protected T item;

    public DoesItemMatchSearchTermParam(String searchTerm, T item) {
      this.searchTerm = searchTerm;
      this.item = item;
    }

    public String getSearchTerm() {
      return searchTerm;
    }

    public T getItem() {
      return item;
    }
  }

}
