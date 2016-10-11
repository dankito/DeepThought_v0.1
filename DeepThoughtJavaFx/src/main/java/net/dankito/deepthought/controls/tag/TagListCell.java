package net.dankito.deepthought.controls.tag;

import net.dankito.deepthought.controls.ICleanUp;
import net.dankito.deepthought.controls.utils.FXUtils;
import net.dankito.deepthought.controls.utils.IEditedEntitiesHolder;
import net.dankito.deepthought.data.model.Tag;
import net.dankito.deepthought.data.model.listener.EntityListener;
import net.dankito.deepthought.data.persistence.db.BaseEntity;
import net.dankito.deepthought.data.search.specific.TagsSearchResults;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.SetChangeListener;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.MenuItem;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextField;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

/**
 * Created by ganymed on 30/11/14.
 */
public class TagListCell extends ListCell<Tag> implements ICleanUp {

  private final static Logger log = LoggerFactory.getLogger(TagListCell.class);


  protected Tag tag = null;

  protected SearchAndSelectTagsControl searchAndSelectTagsControl = null;

  protected IEditedEntitiesHolder editedTagsHolder = null;

  protected HBox graphicPane = new HBox();
  protected CheckBox chkbxIsTagSelected = new CheckBox();
  protected Label lblTagName = new Label();

  protected TextField txtfldEditTagName = null;

  protected TagsSearchResults lastSearchResults = null;


  public TagListCell(SearchAndSelectTagsControl searchAndSelectTagsControl, IEditedEntitiesHolder editedTagsHolder) {
    this.searchAndSelectTagsControl = searchAndSelectTagsControl;
    this.editedTagsHolder = editedTagsHolder;

    lastSearchResults = searchAndSelectTagsControl.lastSearchResults;
    searchAndSelectTagsControl.addDisplayedTagsChangedListener(displayedTagsChangedListener);

    editedTagsHolder.getEditedEntities().addListener(editedTagsChangedListener);

    setupGraphics();

    itemProperty().addListener(new ChangeListener<Tag>() {
      @Override
      public void changed(ObservableValue<? extends Tag> observable, Tag oldValue, Tag newValue) {
        tagChanged(newValue);
      }
    });

    // bind ListView Item's width to ListView's width
    listViewProperty().addListener((observable, oldValue, newValue) -> graphicPane.maxWidthProperty().bind(newValue.widthProperty().subtract(35)));

    setOnMouseClicked(event -> mouseClicked(event));

    setOnContextMenuRequested(event -> showContextMenu(event));

    selectedProperty().addListener((observable, oldValue, newValue) -> setCellBackgroundColor(newValue));
  }

  @Override
  public void cleanUp() {
    editedTagsHolder.getEditedEntities().removeListener(editedTagsChangedListener);
    editedTagsHolder = null;

    searchAndSelectTagsControl.removeDisplayedTagsChangedListener(displayedTagsChangedListener);
    searchAndSelectTagsControl = null;
    lastSearchResults = null;

    if(this.tag != null)
      this.tag.removeEntityListener(tagListener);
  }

  protected SetChangeListener<Tag> editedTagsChangedListener = change -> tagUpdated();

  protected IDisplayedTagsChangedListener displayedTagsChangedListener = results -> {
    lastSearchResults = results;
    setCellBackgroundColor();
  };

  protected void setupGraphics() {
    setText(null);
    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
    setAlignment(Pos.CENTER_LEFT);

    graphicPane.setAlignment(Pos.CENTER_LEFT);

    graphicPane.getChildren().add(chkbxIsTagSelected);
    chkbxIsTagSelected.setId("chkbxIsTagSelected");

    lblTagName.setTextOverrun(OverrunStyle.ELLIPSIS);
    lblTagName.setMaxWidth(FXUtils.SizeMaxValue);
    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(lblTagName);
    graphicPane.getChildren().add(lblTagName);
    HBox.setHgrow(lblTagName, Priority.ALWAYS);
    HBox.setMargin(lblTagName, new Insets(0, 6, 0, 6));

    chkbxIsTagSelected.selectedProperty().addListener(checkBoxIsTagSelectedChangeListener);
  }

  @Override
  protected void updateItem(Tag item, boolean empty) {
    super.updateItem(item, empty);

    if(empty) {
      setGraphic(null);
    }
    else {
      if (isEditing()) {
        if (txtfldEditTagName == null)
          createTextField();

        showCellInEditingState();
      }
      else {
        showCellInNotEditingState();
        lblTagName.setText(getTagStringRepresentation(item));
      }

      chkbxIsTagSelected.selectedProperty().removeListener(checkBoxIsTagSelectedChangeListener);

      chkbxIsTagSelected.setSelected(editedTagsHolder.containsEditedEntity(item));

      chkbxIsTagSelected.selectedProperty().addListener(checkBoxIsTagSelectedChangeListener);

      setGraphic(graphicPane);
    }

    setCellBackgroundColor();
  }

  protected void setCellBackgroundColor() {
    setCellBackgroundColor(isSelected());
  }

  protected void setCellBackgroundColor(boolean isSelected) {
    if(isSelected == true)
      setBackground(net.dankito.deepthought.controls.Constants.TagCellSelectedBackground);
    else
      FXUtils.setTagCellBackgroundColor(tag, lastSearchResults, TagListCell.this);
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

  protected void tagUpdatedThreadSafe() {
    FXUtils.runOnUiThread(() -> tagUpdated());
  }

  protected void tagUpdated() {
    updateItem(tag, tag == null);
  }


  protected void handleCheckBoxSelectedChanged(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
    if(newValue == true)
      editedTagsHolder.addEntityToEntry(getItem());
    else
      editedTagsHolder.removeEntityFromEntry(getItem());
  }

  protected void mouseClicked(MouseEvent event) {
    if(event.getButton() == MouseButton.PRIMARY) {
      if (event.getClickCount() == 2 && event.getButton() == MouseButton.PRIMARY) {
        if (getItem() != null) {
          toggleTagAffiliation();
        }
      }

      showCellInNotEditingState();
        event.consume();
    }
  }

  protected void toggleTagAffiliation() {
    if (editedTagsHolder.containsEditedEntity(getItem()) == false)
      editedTagsHolder.addEntityToEntry(getItem());
    else
      editedTagsHolder.removeEntityFromEntry(getItem());
  }

  @Override
  public void startEdit() {
    super.startEdit();

    if (txtfldEditTagName == null) {
      createTextField();
    }

    showCellInEditingState();

    txtfldEditTagName.selectAll();
    txtfldEditTagName.requestFocus();
  }

  protected void createTextField() {
    txtfldEditTagName = new TextField();

    HBox.setHgrow(txtfldEditTagName, Priority.ALWAYS);
    HBox.setMargin(txtfldEditTagName, new Insets(0, 6, 0, 6));
    txtfldEditTagName.setMaxWidth(FXUtils.SizeMaxValue);
    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(txtfldEditTagName);
    graphicPane.getChildren().add(1, txtfldEditTagName);

    txtfldEditTagName.setOnKeyReleased(new EventHandler<KeyEvent>() {

      @Override
      public void handle(KeyEvent t) {
        if (t.getCode() == KeyCode.ESCAPE) {
          cancelEdit();
        }
      }
    });

    txtfldEditTagName.setOnAction(event -> {
      if (txtfldEditTagName.getText().equals(getTagStringRepresentation(tag)) == false)
        tag.setName(txtfldEditTagName.getText());
      try {
//        commitEdit(getItem()); // throws an UnsupportedOperationException
        cancelEdit();
      } catch (Exception ex) {
        log.error("Could not commit changes to tag " + tag, ex);
      }
    });

    txtfldEditTagName.focusedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        if (newValue == false)
          cancelEdit();
      }
    });
  }

  @Override
  public void cancelEdit() {
    super.cancelEdit();

    showCellInNotEditingState();
  }

  @Override
  public void commitEdit(Tag newValue) {
    showCellInNotEditingState();

    super.commitEdit(newValue);
  }

  protected void showCellInEditingState() {
    lblTagName.setVisible(false);

    txtfldEditTagName.setText(tag.getName());
    txtfldEditTagName.setVisible(true);
  }

  protected void showCellInNotEditingState() {
    if(txtfldEditTagName != null)
      txtfldEditTagName.setVisible(false);
    lblTagName.setVisible(true);

    lblTagName.setText(getTagStringRepresentation(tag));

    setCellBackgroundColor();
  }


  protected void showContextMenu(ContextMenuEvent event) {
    ContextMenu contextMenu = createContextMenu();

    contextMenu.show(event.getPickResult().getIntersectedNode(), event.getScreenX(), event.getScreenY());
  }

  protected ContextMenu createContextMenu() {
    ContextMenu contextMenu = new ContextMenu();

    MenuItem editTagItem = new MenuItem();
    net.dankito.deepthought.util.localization.JavaFxLocalization.bindMenuItemText(editTagItem, "edit...");
    editTagItem.setOnAction(actionEvent -> net.dankito.deepthought.controller.Dialogs.showEditTagDialog(tag, getScene().getWindow(), true));
    contextMenu.getItems().add(editTagItem);

    contextMenu.getItems().add(new SeparatorMenuItem());

    MenuItem deleteTagItem = new MenuItem();
    net.dankito.deepthought.util.localization.JavaFxLocalization.bindMenuItemText(deleteTagItem, "delete");
    deleteTagItem.setOnAction(event -> deleteTag());
    contextMenu.getItems().add(deleteTagItem);

    return contextMenu;
  }

  protected void deleteTag() {
    if(net.dankito.deepthought.util.Alerts.deleteTagWithUserConfirmationIfIsSetOnEntries(tag)) {
      if(editedTagsHolder != null && editedTagsHolder.containsEditedEntity(tag))
        editedTagsHolder.removeEntityFromEntry(tag);
    }
  }


  protected EntityListener tagListener = new EntityListener() {
    @Override
    public void propertyChanged(BaseEntity entity, String propertyName, Object previousValue, Object newValue) {
      tagUpdatedThreadSafe();
    }

    @Override
    public void entityAddedToCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity addedEntity) {
      tagUpdatedThreadSafe();
    }

    @Override
    public void entityOfCollectionUpdated(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity updatedEntity) {
      tagUpdatedThreadSafe();
    }

    @Override
    public void entityRemovedFromCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity removedEntity) {
      tagUpdatedThreadSafe();
    }
  };

}
