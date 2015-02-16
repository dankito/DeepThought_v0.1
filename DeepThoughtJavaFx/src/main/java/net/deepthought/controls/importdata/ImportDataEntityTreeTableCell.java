package net.deepthought.controls.importdata;

import net.deepthought.data.persistence.db.BaseEntity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;

import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

/**
 * Created by ganymed on 10/01/15.
 */
public class ImportDataEntityTreeTableCell extends ImportDataTreeTableCell {

  private final static Logger log = LoggerFactory.getLogger(ImportDataEntityTreeTableCell.class);


  protected String itemText = null;


  public ImportDataEntityTreeTableCell() {
    setText(null);
    setupGraphic();

    setOnMouseClicked(event -> mouseClicked(event));
  }

  protected void setupGraphic() {

  }


  @Override
  protected void updateItem(String item, boolean empty) {
    super.updateItem(item, empty);

    if(empty) {
      setGraphic(null);
      setText(null);
    }
    else {
//      if(file != null)
//        fileNameLabel.setText(file.getTextRepresentation());
//      setGraphic(graphicPane);
//      setText(item);
      setText(itemText);
    }
  }


  protected void mouseClicked(MouseEvent event) {
    if(event.getClickCount() == 2 && event.getButton() == MouseButton.PRIMARY) {

    }
  }

  @Override
  protected void itemChangedToNull() {
    setGraphic(null);
    setText(null);
  }

  @Override
  protected void itemChangedToField(Field field) {
    Object debug = getTreeTableRow().getTreeItem();

    itemText = field.getName();

    if(itemText != null) {
      char firstCharacter = Character.toUpperCase(itemText.charAt(0));
      itemText = firstCharacter + itemText.substring(1);
    }

    setText(itemText);

    updateItem(itemText, itemText != null);
  }

  @Override
  protected void itemChangedToEntity(BaseEntity entity) {
    itemText = entity.getTextRepresentation();

    setText(itemText);

    updateItem(itemText, itemText != null);
  }

}
