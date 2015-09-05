package net.deepthought.controls.categories;

import net.deepthought.controller.Dialogs;
import net.deepthought.controls.ICleanableControl;
import net.deepthought.controls.tag.IEditedEntitiesHolder;
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
import javafx.collections.SetChangeListener;
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
public class EntryCategoryTreeCell extends TreeCell<Category> implements ICleanableControl {

  private final static Logger log = LoggerFactory.getLogger(EntryCategoryTreeCell.class);


  protected Category category = null;

  protected IEditedEntitiesHolder<Category> editedCategoriesHolder;


  protected HBox graphicPane = new HBox();

  protected CheckBox isEntryInCategoryCheckBox = new CheckBox();

  protected Label categoryNameLabel = new Label();


  protected HBox categoryOptionsButtonsPane = new HBox();

  protected Button editCategoryButton = new Button();
  protected Button deleteCategoryButton = new Button();
  protected Button addSubCategoryToCategoryButton = new Button();


  public EntryCategoryTreeCell(IEditedEntitiesHolder<Category> editedCategoriesHolder) {
    this.editedCategoriesHolder = editedCategoriesHolder;

    editedCategoriesHolder.getEditedEntities().addListener(editedCategoriesChangedListener);

    setText(null);
    setupGraphic();

    treeItemProperty().addListener(new ChangeListener<TreeItem<Category>>() {
      @Override
      public void changed(ObservableValue<? extends TreeItem<Category>> observable, TreeItem<Category> oldValue, TreeItem<Category> newValue) {
        if(newValue != null)
          itemChanged(newValue.getValue());
        else
          itemChanged(null);
      }
    });

    setOnMouseClicked(event -> mouseClicked(event));
  }

  protected SetChangeListener<Category> editedCategoriesChangedListener = change -> categoryPropertiesHaveBeenUpdated();

  @Override
  public void cleanUpControl() {
    if(this.category != null)
      this.category.removeEntityListener(categoryListener);

    editedCategoriesHolder.getEditedEntities().removeListener(editedCategoriesChangedListener);
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

  protected void isEntryInCategoryChanged(boolean isOnCategory) {
    if(category != null) {
      if(isOnCategory)
        editedCategoriesHolder.addEntityToEntry(category);
      else
        editedCategoriesHolder.removeEntityFromEntry(category);
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
      setGraphic(graphicPane);
      categoryPropertiesHaveBeenUpdated();
    }
  }


  protected void mouseClicked(MouseEvent event) {
    if(event.getClickCount() == 2 && event.getButton() == MouseButton.PRIMARY) {
      //Dialogs.showEditCategoryDialog(category);

//      if(category != null) {
//        if (editedCategoriesHolder.getEditedEntryCategories().contains(category) == false)
//          editedCategoriesHolder.addEntityToEntry(entry, category);
//        else
//          editedCategoriesHolder.removeCategoryFromEntry(entry, category);
//      }
      isEntryInCategoryCheckBox.setSelected(!isEntryInCategoryCheckBox.isSelected()); // simply toggle selected state
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

    updateItem(category, category == null);
  }

  protected void categoryPropertiesHaveBeenUpdated() {
    categoryNameLabel.setText(category.getName());
    if(category.getDescription() != null && category.getDescription().isEmpty() == false)
      setTooltip(new Tooltip(category.getDescription()));

    isEntryInCategoryCheckBox.selectedProperty().removeListener(checkBoxIsEntryInCategoryChangeListener);

    isEntryInCategoryCheckBox.setSelected(editedCategoriesHolder.containsEditedEntity(category));

    isEntryInCategoryCheckBox.selectedProperty().addListener(checkBoxIsEntryInCategoryChangeListener);
  }


  protected void handleAddSubCategoryToCategoryButtonAction() {
    if(category != null)
      category.addSubCategory(new Category());
  }

  protected void handleDeleteCategoryButtonAction() {
    if(category != null) {
      Alerts.deleteCategoryWithUserConfirmationIfHasSubCategoriesOrEntries(category);
    }
  }

  protected void handleEditFileButtonAction() {
    Dialogs.showEditCategoryDialog(category);
  }


  protected EntityListener categoryListener = new EntityListener() {
    @Override
    public void propertyChanged(BaseEntity entity, String propertyName, Object previousValue, Object newValue) {
        categoryPropertiesHaveBeenUpdated();
    }

    @Override
    public void entityAddedToCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity addedEntity) {
      categoryPropertiesHaveBeenUpdated();
    }

    @Override
    public void entityOfCollectionUpdated(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity updatedEntity) {

    }

    @Override
    public void entityRemovedFromCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity removedEntity) {
      categoryPropertiesHaveBeenUpdated();
    }
  };


}
