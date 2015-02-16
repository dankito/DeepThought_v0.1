package net.deepthought.controls.tag;

import net.deepthought.data.model.Entry;
import net.deepthought.data.model.Tag;
import net.deepthought.data.model.listener.EntityListener;
import net.deepthought.data.persistence.db.BaseEntity;
import net.deepthought.util.Alerts;
import net.deepthought.util.JavaFxLocalization;

import java.util.Collection;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.StringConverter;

/**
 * Created by ganymed on 30/11/14.
 */
public class TagListCell extends ListCell<Tag> {

  protected Entry entry = null;
  protected Tag tag = null;

  protected EntryTagsControl entryTagsControl;

  protected HBox graphicsPane = new HBox();
  protected CheckBox chkbxIsTagSelected = new CheckBox();
  protected Label lblTagName = new Label();
  protected Button btnEditTag = new Button();
  protected Button btnDeleteTag = new Button();


  public TagListCell(Entry entry, EntryTagsControl entryTagsControl) {
    this.entry = entry;
    this.entryTagsControl = entryTagsControl;

    if(entry != null)
      entry.addEntityListener(entryListener);

    setupGraphics();

    itemProperty().addListener(new ChangeListener<Tag>() {
      @Override
      public void changed(ObservableValue<? extends Tag> observable, Tag oldValue, Tag newValue) {
        tagChanged(newValue);
      }
    });
  }

  protected void setupGraphics() {
    setText(null);
    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
    setAlignment(Pos.CENTER_LEFT);

    graphicsPane.setAlignment(Pos.CENTER_LEFT);

    graphicsPane.getChildren().add(chkbxIsTagSelected);

    HBox.setHgrow(lblTagName, Priority.ALWAYS);
    HBox.setMargin(lblTagName, new Insets(0, 6, 0, 6));
    lblTagName.setMaxWidth(Double.MAX_VALUE);
    graphicsPane.getChildren().add(lblTagName);

    JavaFxLocalization.bindLabeledText(btnEditTag, "edit");
    btnEditTag.setDisable(true);
    graphicsPane.getChildren().add(btnEditTag);

    btnDeleteTag.setText("-");
    btnDeleteTag.setTextFill(Color.RED);
    btnDeleteTag.setFont(new Font(15));
    HBox.setMargin(btnDeleteTag, new Insets(0, 0, 0, 6));
    graphicsPane.getChildren().add(btnDeleteTag);

    chkbxIsTagSelected.selectedProperty().addListener(checkBoxIsTagSelectedChangeListener);
    btnEditTag.setOnAction((event) -> handleButtonEditTagAction(event));
    btnDeleteTag.setOnAction((event) -> handleButtonDeleteTagAction(event));
  }

  @Override
  protected void updateItem(Tag item, boolean empty) {
    super.updateItem(item, empty);

    if(empty) {
      setGraphic(null);
    }
    else {
      lblTagName.setText(getTagStringRepresentation(item));
      chkbxIsTagSelected.selectedProperty().removeListener(checkBoxIsTagSelectedChangeListener);

      if(entry != null)
        chkbxIsTagSelected.setSelected(entry.getTags().contains(item));
      else
        chkbxIsTagSelected.setSelected(false);

      chkbxIsTagSelected.selectedProperty().addListener(checkBoxIsTagSelectedChangeListener);

      setGraphic(graphicsPane);
    }
  }

  protected ChangeListener<Boolean> checkBoxIsTagSelectedChangeListener = new ChangeListener<Boolean>() {
    @Override
    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
      handleCheckBoxSelectedChanged(observable, oldValue, newValue);
    }
  };

  protected String getTagStringRepresentation(Tag tag) {
    return tag == null ? "" : tag.getName() + " (" + tag.getEntries().size() + ")";
  }

  protected void tagChanged(Tag newTag) {
    if(this.tag != null)
      this.tag.removeEntityListener(tagListener);

    this.tag = newTag;

    if(tag != null) {
      tag.addEntityListener(tagListener);
    }

    tagUpdated();
  }

  protected void tagUpdated() {
//    setText(getTagStringRepresentation(tag));
    updateItem(tag, tag == null);
  }


  protected void handleCheckBoxSelectedChanged(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
    if(entry == null) // should actually never be the case
      return;

    if(newValue == true)
//      entry.addTag(getItem());
      entryTagsControl.addTagToEntry(getItem());
    else
//      entry.removeTag(getItem());
      entryTagsControl.removeTagFromEntry(getItem());
  }

  protected void handleButtonEditTagAction(ActionEvent event) {
    // TODO: show EditTag dialog
  }


  public void setEntry(Entry entry) {
    if(this.entry != null)
      this.entry.removeEntityListener(entryListener);

    this.entry = entry;

    if(this.entry != null)
      this.entry.addEntityListener(entryListener);

    updateItem(getItem(), getItem() == null);
  }

  protected void handleButtonDeleteTagAction(ActionEvent event) {
    Alerts.deleteTagWithUserConfirmationIfIsSetOnEntries(tag);
  }

  protected StringConverter<Tag> tagStringConverter = new StringConverter<Tag>() {
    @Override
    public String toString(Tag tag) {
      return getTagStringRepresentation(tag);
    }

    @Override
    public Tag fromString(String string) {
      return null;
    }
  };


  protected EntityListener tagListener = new EntityListener() {
    @Override
    public void propertyChanged(BaseEntity entity, String propertyName, Object previousValue, Object newValue) {
      tagUpdated();
    }

    @Override
    public void entityAddedToCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity addedEntity) {
      tagUpdated();
    }

    @Override
    public void entityOfCollectionUpdated(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity updatedEntity) {
      tagUpdated();
    }

    @Override
    public void entityRemovedFromCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity removedEntity) {
      tagUpdated();
    }
  };

  protected EntityListener entryListener = new EntityListener() {
    @Override
    public void propertyChanged(BaseEntity entity, String propertyName, Object previousValue, Object newValue) {

    }

    @Override
    public void entityAddedToCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity addedEntity) {
      if(collection == entry.getTags() && addedEntity.equals(getItem()))
        tagUpdated();
    }

    @Override
    public void entityOfCollectionUpdated(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity updatedEntity) {

    }

    @Override
    public void entityRemovedFromCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity removedEntity) {
      if(collection == entry.getTags() && removedEntity.equals(getItem()))
        tagUpdated();
    }
  };

  public void setComboBoxToUnselected() {
    chkbxIsTagSelected.selectedProperty().removeListener(checkBoxIsTagSelectedChangeListener);

    chkbxIsTagSelected.setSelected(false);

    chkbxIsTagSelected.selectedProperty().addListener(checkBoxIsTagSelectedChangeListener);
  }
}
