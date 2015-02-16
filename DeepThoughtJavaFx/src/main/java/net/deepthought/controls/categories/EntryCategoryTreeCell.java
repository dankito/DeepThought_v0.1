package net.deepthought.controls.categories;

import net.deepthought.controller.Dialogs;
import net.deepthought.data.model.Category;
import net.deepthought.data.model.Entry;
import net.deepthought.data.model.listener.EntityListener;
import net.deepthought.data.persistence.db.BaseEntity;
import net.deepthought.util.Alerts;
import net.deepthought.util.JavaFxLocalization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * Created by ganymed on 27/12/14.
 */
public class EntryCategoryTreeCell extends TreeCell<Category> {

  private final static Logger log = LoggerFactory.getLogger(EntryCategoryTreeCell.class);


  protected Category category = null;
  protected Entry entry = null;

  protected EntryCategoriesControl entryCategoriesControl;


  protected HBox graphicPane = new HBox();

  protected CheckBox isEntryInCategoryCheckBox = new CheckBox();

  protected Label categoryNameLabel = new Label();


  protected HBox categoryOptionsButtonsPane = new HBox();

  protected Button addSubCategoryToCategoryButton = new Button();
  protected Button deleteCategoryButton = new Button();
  protected Button editCategoryButton = new Button();


  public EntryCategoryTreeCell(Entry entry, EntryCategoriesControl entryCategoriesControl) {
    this.entry = entry;
    this.entryCategoriesControl = entryCategoriesControl;

    isEntryInCategoryCheckBox.setDisable(entry == null);

    setText(null);
    setupGraphic();

    treeItemProperty().addListener(new ChangeListener<TreeItem<Category>>() {
      @Override
      public void changed(ObservableValue<? extends TreeItem<Category>> observable, TreeItem<Category> oldValue, TreeItem<Category> newValue) {
        if(newValue != null) {
          itemChanged(newValue.getValue());
        }
      }
    });

    setOnMouseClicked(event -> mouseClicked(event));
  }

  protected void setupGraphic() {
    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
    setAlignment(Pos.CENTER_LEFT);

    graphicPane.setAlignment(Pos.CENTER_LEFT);
    graphicPane.setPrefHeight(20);

    HBox.setHgrow(categoryNameLabel, Priority.ALWAYS);
    HBox.setMargin(categoryNameLabel, new Insets(0, 6, 0, 6));
    HBox.setMargin(deleteCategoryButton, new Insets(0, 6, 0, 6));

    graphicPane.getChildren().add(isEntryInCategoryCheckBox);
    isEntryInCategoryCheckBox.selectedProperty().addListener(checkBoxIsEntryInCategoryChangeListener);

    categoryNameLabel.setMaxWidth(Double.MAX_VALUE);
    graphicPane.getChildren().add(categoryNameLabel);

    categoryOptionsButtonsPane.setAlignment(Pos.CENTER_LEFT);
    categoryOptionsButtonsPane.managedProperty().bind(categoryOptionsButtonsPane.visibleProperty());
    graphicPane.getChildren().add(categoryOptionsButtonsPane);

    addSubCategoryToCategoryButton.setText("+");
    addSubCategoryToCategoryButton.setTextFill(Color.GREEN);
    addSubCategoryToCategoryButton.setFont(new Font(15));
    addSubCategoryToCategoryButton.setPrefWidth(50);
    JavaFxLocalization.bindControlToolTip(addSubCategoryToCategoryButton, "add.sub.category.to.category.tool.tip");
    categoryOptionsButtonsPane.getChildren().add(addSubCategoryToCategoryButton);
    addSubCategoryToCategoryButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent event) {
        selectCurrentCell();
        handleAddSubCategoryToCategoryButtonAction();
      }
    });

    deleteCategoryButton.setText("-");
    deleteCategoryButton.setTextFill(Color.RED);
    deleteCategoryButton.setFont(new Font(15));
    deleteCategoryButton.setPrefWidth(50);
    JavaFxLocalization.bindControlToolTip(deleteCategoryButton, "delete.category.tool.tip");
    categoryOptionsButtonsPane.getChildren().add(deleteCategoryButton);
    deleteCategoryButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent event) {
        selectCurrentCell();
        handleDeleteCategoryButtonAction();
      }
    });

    JavaFxLocalization.bindLabeledText(editCategoryButton, "edit");
    JavaFxLocalization.bindControlToolTip(editCategoryButton, "edit.category.tool.tip");
    editCategoryButton.setMinWidth(80);
    categoryOptionsButtonsPane.getChildren().add(editCategoryButton);
    editCategoryButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent event) {
        selectCurrentCell();
        handleEditFileButtonAction();
      }
    });

    editCategoryButton.setDisable(true);

    disclosureNodeProperty().addListener(new ChangeListener<Node>() {
      @Override
      public void changed(ObservableValue<? extends Node> observable, Node oldValue, Node newValue) {
        if(newValue != null)
          ((StackPane)newValue).setAlignment(Pos.CENTER);
      }
    });
  }

  protected ChangeListener<Boolean> checkBoxIsEntryInCategoryChangeListener = new ChangeListener<Boolean>() {
    @Override
    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
      selectCurrentCell();
      isEntryInCategoryChanged(newValue);
    }
  };

  protected void isEntryInCategoryChanged(Boolean isOnCategory) {
    if(entry != null && category != null) {
      if(isOnCategory)
//        category.addEntry(entry);
        entryCategoriesControl.addCategoryToEntry(entry, category);
      else
//        category.removeEntry(entry);
        entryCategoriesControl.removeCategoryFromEntry(entry, category);
    }
  }


  @Override
  protected void updateItem(Category item, boolean empty) {
    TreeItem<Category> treeItem = getTreeItem();
    if(treeItem != null && treeItem.getValue() != category)
      itemChanged(treeItem.getValue());

    super.updateItem(item, empty);

    if(empty) {
      setGraphic(null);
    }
    else {
      if(category != null)
        categoryNameLabel.setText(category.getName());
      setGraphic(graphicPane);
    }
  }


  protected void mouseClicked(MouseEvent event) {
    if(event.getClickCount() == 2 && event.getButton() == MouseButton.PRIMARY) {
      Dialogs.showEditCategoryDialog(category);
    }
  }

  protected void selectCurrentCell() {
//    getTreeView().getSelectionModel().select(getIndex());
    updateSelected(true);
  }

  protected void itemChanged(Category newValue) {
    if(this.category != null && this.category.equals(newValue) == false)
      this.category.removeEntityListener(categoryListener);

    this.category = newValue;

    if(category != null)
      this.category.addEntityListener(categoryListener);

    if(newValue == null) {
      setGraphic(null);
    }
    else {
      setGraphic(graphicPane);
      categoryPropertiesHaveBeenUpdated(newValue);
    }
  }

  private void categoryPropertiesHaveBeenUpdated(Category newValue) {
    categoryNameLabel.setText(newValue.getName());
    if(category.getDescription() != null && category.getDescription().isEmpty() == false)
      setTooltip(new Tooltip(category.getDescription()));

    isEntryInCategoryCheckBox.selectedProperty().removeListener(checkBoxIsEntryInCategoryChangeListener);

    if(entry == null)
      isEntryInCategoryCheckBox.setSelected(false);
    else
//      isEntryInCategoryCheckBox.setSelected(category.containsEntry(entry));
      isEntryInCategoryCheckBox.setSelected(entryCategoriesControl.getEditedEntryCategories().contains(category));

    isEntryInCategoryCheckBox.selectedProperty().addListener(checkBoxIsEntryInCategoryChangeListener);
  }


  protected void handleAddSubCategoryToCategoryButtonAction() {
    if(category != null)
      category.addSubCategory(new Category(category.getDefaultEntryTemplate()));
  }

  protected void handleDeleteCategoryButtonAction() {
    if(category != null) {
      Alerts.deleteCategoryWithUserConfirmationIfHasSubCategoriesOrEntries(category);
    }
  }

  protected void handleEditFileButtonAction() {
    Dialogs.showEditCategoryDialog(category);
  }


  public void setEntry(Entry entry) {
//    if(this.entry != null)
//      this.entry.removeEntityListener(list)

    this.entry = entry;
    isEntryInCategoryCheckBox.setDisable(entry == null);

//    if(category == null)
//      isEntryInCategoryCheckBox.setSelected(false);
//    else
//      isEntryInCategoryCheckBox.setSelected(category.containsEntry(entry));
    categoryPropertiesHaveBeenUpdated(category);

  }


  protected EntityListener categoryListener = new EntityListener() {
    @Override
    public void propertyChanged(BaseEntity entity, String propertyName, Object previousValue, Object newValue) {
      if(entity.equals(category))
        categoryPropertiesHaveBeenUpdated(category);
    }

    @Override
    public void entityAddedToCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity addedEntity) {
      if(collectionHolder.equals(category) && collection == category.getEntries()) {
        if(addedEntity.equals(entry))
          setComboBoxToSelected();
      }
    }

    @Override
    public void entityOfCollectionUpdated(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity updatedEntity) {

    }

    @Override
    public void entityRemovedFromCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity removedEntity) {
      if(collectionHolder.equals(category) && collection == category.getEntries()) {
        if(removedEntity.equals(entry))
          setComboBoxToUnselected();
      }
    }
  };


  public void setComboBoxToSelected() {
    isEntryInCategoryCheckBox.selectedProperty().removeListener(checkBoxIsEntryInCategoryChangeListener);
    isEntryInCategoryCheckBox.setSelected(true);
    isEntryInCategoryCheckBox.selectedProperty().addListener(checkBoxIsEntryInCategoryChangeListener);
  }

  public void setComboBoxToUnselected() {
    isEntryInCategoryCheckBox.selectedProperty().removeListener(checkBoxIsEntryInCategoryChangeListener);
    isEntryInCategoryCheckBox.setSelected(false);
    isEntryInCategoryCheckBox.selectedProperty().addListener(checkBoxIsEntryInCategoryChangeListener);
  }
}
