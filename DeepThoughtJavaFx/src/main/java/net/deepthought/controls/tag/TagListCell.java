package net.deepthought.controls.tag;

import net.deepthought.controller.Dialogs;
import net.deepthought.controls.Constants;
import net.deepthought.controls.FXUtils;
import net.deepthought.controls.ICleanableControl;
import net.deepthought.data.model.Tag;
import net.deepthought.data.model.listener.EntityListener;
import net.deepthought.data.persistence.db.BaseEntity;
import net.deepthought.data.search.specific.FilterTagsSearchResults;
import net.deepthought.util.Alerts;
import net.deepthought.util.ClipboardHelper;
import net.deepthought.util.JavaFxLocalization;
import net.deepthought.util.Localization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.SetChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextField;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * Created by ganymed on 30/11/14.
 */
public class TagListCell extends ListCell<Tag> implements ICleanableControl {

  private final static Logger log = LoggerFactory.getLogger(TagListCell.class);


  protected Tag tag = null;

  protected SearchAndSelectTagsControl searchAndSelectTagsControl = null;

  protected IEditedEntitiesHolder editedTagsHolder = null;

  protected HBox graphicsPane = new HBox();
  protected CheckBox chkbxIsTagSelected = new CheckBox();
  protected Label lblTagName = new Label();

  protected TextField txtfldEditTagName = null;

  protected FilterTagsSearchResults filterTagsSearchResults = FilterTagsSearchResults.NoFilterSearchResults;


  public TagListCell(SearchAndSelectTagsControl searchAndSelectTagsControl, IEditedEntitiesHolder editedTagsHolder) {
    this.searchAndSelectTagsControl = searchAndSelectTagsControl;
    this.editedTagsHolder = editedTagsHolder;

    editedTagsHolder.getEditedEntities().addListener(editedTagsChangedListener);

    filterTagsSearchResults = searchAndSelectTagsControl.lastFilterTagsResults;
    searchAndSelectTagsControl.addFilteredTagsChangedListener(filteredTagsChangedListener);

    setupGraphics();

    itemProperty().addListener(new ChangeListener<Tag>() {
      @Override
      public void changed(ObservableValue<? extends Tag> observable, Tag oldValue, Tag newValue) {
        tagChanged(newValue);
      }
    });

    setOnMouseClicked(event -> mouseClicked(event));

    setOnContextMenuRequested(event -> showContextMenu(event));

    selectedProperty().addListener((observable, oldValue, newValue) -> {
      if(newValue == true)
        setBackground(Constants.FilteredTagsSelectedBackground);
      else
        FXUtils.setTagCellBackgroundColor(tag, filterTagsSearchResults, TagListCell.this);
    });
  }

  @Override
  public void cleanUpControl() {
    editedTagsHolder.getEditedEntities().removeListener(editedTagsChangedListener);
    editedTagsHolder = null;

    searchAndSelectTagsControl.removeFilteredTagsChangedListener(filteredTagsChangedListener);
    searchAndSelectTagsControl = null;
    filterTagsSearchResults = null;

    if(this.tag != null)
      this.tag.removeEntityListener(tagListener);
  }

  protected SetChangeListener<Tag> editedTagsChangedListener = change -> tagUpdated();

  protected IFilteredTagsChangedListener filteredTagsChangedListener = results -> {
    filterTagsSearchResults = results;
    setCellBackgroundColor();
  };

  protected void setupGraphics() {
    setText(null);
    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
    setAlignment(Pos.CENTER_LEFT);

    graphicsPane.setAlignment(Pos.CENTER_LEFT);

    graphicsPane.getChildren().add(chkbxIsTagSelected);

    HBox.setHgrow(lblTagName, Priority.ALWAYS);
    HBox.setMargin(lblTagName, new Insets(0, 6, 0, 6));
    lblTagName.setMaxWidth(Double.MAX_VALUE);
    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(lblTagName);
    graphicsPane.getChildren().add(lblTagName);

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

        txtfldEditTagName.setText(item.getName());
        showCellInEditingState();
      }
      else {
        showCellInNotEditingState();
        lblTagName.setText(getTagStringRepresentation(item));
      }

      chkbxIsTagSelected.selectedProperty().removeListener(checkBoxIsTagSelectedChangeListener);

      chkbxIsTagSelected.setSelected(editedTagsHolder.containsEditedEntity(item));

      chkbxIsTagSelected.selectedProperty().addListener(checkBoxIsTagSelectedChangeListener);

      setGraphic(graphicsPane);
    }

    setCellBackgroundColor();
  }

  protected void setCellBackgroundColor() {
    FXUtils.setTagCellBackgroundColor(tag, filterTagsSearchResults, this);
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
          if (editedTagsHolder.containsEditedEntity(getItem()) == false)
            editedTagsHolder.addEntityToEntry(getItem());
          else
            editedTagsHolder.removeEntityFromEntry(getItem());
        }
      }

      showCellInNotEditingState();
        event.consume();
    }
  }

  @Override
  public void startEdit() {
    super.startEdit();

    if (txtfldEditTagName == null) {
      createTextField();
    }
    txtfldEditTagName.setText(tag.getName());

    showCellInEditingState();

    txtfldEditTagName.selectAll();
    txtfldEditTagName.requestFocus();
  }

  protected void createTextField() {
    txtfldEditTagName = new TextField();

    HBox.setHgrow(txtfldEditTagName, Priority.ALWAYS);
    HBox.setMargin(txtfldEditTagName, new Insets(0, 6, 0, 6));
    txtfldEditTagName.setMaxWidth(Double.MAX_VALUE);
    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(txtfldEditTagName);
    graphicsPane.getChildren().add(1, txtfldEditTagName);

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
    txtfldEditTagName.setVisible(true);
  }

  protected void showCellInNotEditingState() {
    if(txtfldEditTagName != null)
      txtfldEditTagName.setVisible(false);
    lblTagName.setVisible(true);

    setText(getTagStringRepresentation(tag));
  }


  protected void showContextMenu(ContextMenuEvent event) {
    ContextMenu contextMenu = createContextMenu();

    contextMenu.show(event.getPickResult().getIntersectedNode(), event.getScreenX(), event.getScreenY());
  }

  protected ContextMenu createContextMenu() {
    ContextMenu contextMenu = new ContextMenu();

    MenuItem editTagItem = new MenuItem(Localization.getLocalizedString("edit..."));
    editTagItem.setOnAction(actionEvent -> Dialogs.showEditTagDialog(tag, getScene().getWindow(), true));
    contextMenu.getItems().add(editTagItem);

    contextMenu.getItems().add(new SeparatorMenuItem());

    MenuItem deleteTagItem = new MenuItem(Localization.getLocalizedString("delete"));
    deleteTagItem.setOnAction(event -> Alerts.deleteTagWithUserConfirmationIfIsSetOnEntries(tag));
    contextMenu.getItems().add(deleteTagItem);

    return contextMenu;
  }



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

}
