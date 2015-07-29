package net.deepthought.controls.tag;

import net.deepthought.controls.FXUtils;
import net.deepthought.data.model.Tag;
import net.deepthought.data.model.listener.EntityListener;
import net.deepthought.data.persistence.db.BaseEntity;
import net.deepthought.data.search.FilterTagsSearchResults;
import net.deepthought.util.Alerts;
import net.deepthought.util.JavaFxLocalization;

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
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
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
public class TagListCell extends ListCell<Tag> {

  private final static Logger log = LoggerFactory.getLogger(TagListCell.class);


  protected Tag tag = null;

  protected IEditedTagsHolder editedTagsHolder = null;

  protected HBox graphicsPane = new HBox();
  protected CheckBox chkbxIsTagSelected = new CheckBox();
  protected Label lblTagName = new Label();
  protected Button btnEditTag = new Button();
  protected Button btnDeleteTag = new Button();

  protected TextField txtfldEditTagName = null;

  protected FilterTagsSearchResults filterTagsSearchResults = FilterTagsSearchResults.NoFilterSearchResults;


  public TagListCell(SearchAndSelectTagsControl searchAndSelectTagsControl, IEditedTagsHolder editedTagsHolder) {
    this.editedTagsHolder = editedTagsHolder;
    editedTagsHolder.getEditedTags().addListener((SetChangeListener.Change<? extends Tag> change) -> tagUpdated());

    filterTagsSearchResults = searchAndSelectTagsControl.lastFilterTagsResults;
    searchAndSelectTagsControl.addFilteredTagsChangedListener(results -> {
      filterTagsSearchResults = results;
      setCellBackgroundColor();
    });

    setupGraphics();

    itemProperty().addListener(new ChangeListener<Tag>() {
      @Override
      public void changed(ObservableValue<? extends Tag> observable, Tag oldValue, Tag newValue) {
        tagChanged(newValue);
      }
    });

    setOnMouseClicked(event -> mouseClicked(event));
  }

  protected void setupGraphics() {
    setText(null);
    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
    setAlignment(Pos.CENTER_LEFT);
//    setMaxHeight(16);
//    graphicsPane.setMaxHeight(16);
//
//    String style = getStyle();
//    style += ".table-row-cell { -fx-cell-size: 16px; }";
//    setStyle(style);

    graphicsPane.setAlignment(Pos.CENTER_LEFT);

    graphicsPane.getChildren().add(chkbxIsTagSelected);

    HBox.setHgrow(lblTagName, Priority.ALWAYS);
    HBox.setMargin(lblTagName, new Insets(0, 6, 0, 6));
    lblTagName.setMaxWidth(Double.MAX_VALUE);
    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(lblTagName);
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

      chkbxIsTagSelected.setSelected(editedTagsHolder.containsEditedTag(item));

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
//    setText(getTagStringRepresentation(tag));
    updateItem(tag, tag == null);
  }


  protected void handleCheckBoxSelectedChanged(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
    if(newValue == true)
      editedTagsHolder.addTagToEntry(getItem());
    else
      editedTagsHolder.removeTagFromEntry(getItem());
  }

  protected void handleButtonEditTagAction(ActionEvent event) {
    // TODO: show EditTag dialog
  }

  protected void mouseClicked(MouseEvent event) {
    if(event.getButton() == MouseButton.PRIMARY) {
      if (event.getClickCount() == 2 && event.getButton() == MouseButton.PRIMARY) {
        //Dialogs.showEditTagDialog(getItem());

        if (getItem() != null) {
          if (editedTagsHolder.containsEditedTag(getItem()) == false)
            editedTagsHolder.addTagToEntry(getItem());
          else
            editedTagsHolder.removeTagFromEntry(getItem());
        }
      }

//      cancelEdit();
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
        if (t.getCode() == KeyCode.ENTER) {
          if (txtfldEditTagName.getText().equals(getTagStringRepresentation(tag)) == false)
            tag.setName(txtfldEditTagName.getText());
          try {
            commitEdit(getItem());
          } catch (Exception ex) {
            log.error("Could not commit changes to tag " + tag, ex);
          }
        } else if (t.getCode() == KeyCode.ESCAPE) {
          cancelEdit();
        }
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

  protected void handleButtonDeleteTagAction(ActionEvent event) {
    Alerts.deleteTagWithUserConfirmationIfIsSetOnEntries(tag);
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

  public void setComboBoxToUnselected() {
    chkbxIsTagSelected.selectedProperty().removeListener(checkBoxIsTagSelectedChangeListener);

    chkbxIsTagSelected.setSelected(false);

    chkbxIsTagSelected.selectedProperty().addListener(checkBoxIsTagSelectedChangeListener);
  }
}
