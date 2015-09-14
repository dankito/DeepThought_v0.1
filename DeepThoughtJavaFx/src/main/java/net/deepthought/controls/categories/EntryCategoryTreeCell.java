package net.deepthought.controls.categories;

import net.deepthought.controller.Dialogs;
import net.deepthought.controls.FXUtils;
import net.deepthought.controls.ICleanableControl;
import net.deepthought.controls.tag.IEditedEntitiesHolder;
import net.deepthought.data.model.Category;
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
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
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

  protected TextField txtfldEditCategoryName = null;


  protected HBox categoryOptionsButtonsPane = new HBox();

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
          categoryChanged(newValue.getValue());
        else
          categoryChanged(null);
      }
    });

    setOnMouseClicked(event -> mouseClicked(event));

    setOnContextMenuRequested(event -> showContextMenu(event));
  }

  protected SetChangeListener<Category> editedCategoriesChangedListener = change -> categoryUpdated();

  @Override
  public void cleanUpControl() {
    if(this.category != null)
      this.category.removeEntityListener(categoryListener);

    editedCategoriesHolder.getEditedEntities().removeListener(editedCategoriesChangedListener);
  }

  protected void setupGraphic() {
    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
    setAlignment(Pos.CENTER_LEFT);

    double height = 24;

    graphicPane.setAlignment(Pos.CENTER_LEFT);
    graphicPane.setMinHeight(height);
    graphicPane.setMaxHeight(height);

    HBox.setHgrow(categoryNameLabel, Priority.ALWAYS);
    HBox.setMargin(categoryNameLabel, new Insets(0, 6, 0, 6));

    graphicPane.getChildren().add(isEntryInCategoryCheckBox);
    isEntryInCategoryCheckBox.selectedProperty().addListener(checkBoxIsEntryInCategoryChangeListener);

    categoryNameLabel.setMaxWidth(Double.MAX_VALUE);
    graphicPane.getChildren().add(categoryNameLabel);

    categoryOptionsButtonsPane.setAlignment(Pos.CENTER_LEFT);
    categoryOptionsButtonsPane.managedProperty().bind(categoryOptionsButtonsPane.visibleProperty());
    graphicPane.getChildren().add(categoryOptionsButtonsPane);

    addSubCategoryToCategoryButton.setText("+");
    addSubCategoryToCategoryButton.setTextFill(Color.GREEN);
    addSubCategoryToCategoryButton.setFont(new Font(9.5));
    addSubCategoryToCategoryButton.setMinWidth(height);
    addSubCategoryToCategoryButton.setMaxWidth(height);
    addSubCategoryToCategoryButton.setMinHeight(height);
    addSubCategoryToCategoryButton.setMaxHeight(height);
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
      categoryChanged(treeItem.getValue());

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
      isEntryInCategoryCheckBox.setSelected(!isEntryInCategoryCheckBox.isSelected()); // simply toggle selected state
    }
  }

  protected void selectCurrentCell() {
    updateSelected(true);
  }

  protected void categoryChanged(Category newValue) {
    if(this.category != null && this.category.equals(newValue) == false)
      this.category.removeEntityListener(categoryListener);

    this.category = newValue;

    if(category != null)
      this.category.addEntityListener(categoryListener);

    categoryUpdated();
  }

  protected void categoryUpdated() {
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
    if(category != null) {
      if(getTreeItem() != null)
        getTreeItem().setExpanded(true);
      Category newCategory = new Category();
      category.addSubCategory(newCategory);
      editedCategoriesHolder.addEntityToEntry(newCategory);
//      Dialogs.showEditCategoryDialog(new Category(), category, getScene().getWindow(), true);
    }
  }


  @Override
  public void startEdit() {
    super.startEdit();

    if (txtfldEditCategoryName == null) {
      createTextField();
    }
    txtfldEditCategoryName.setText(category.getName());

    showCellInEditingState();

    txtfldEditCategoryName.selectAll();
    txtfldEditCategoryName.requestFocus();
  }

  protected void createTextField() {
    txtfldEditCategoryName = new TextField();

    HBox.setHgrow(txtfldEditCategoryName, Priority.ALWAYS);
    HBox.setMargin(txtfldEditCategoryName, new Insets(0, 6, 0, 6));
    txtfldEditCategoryName.setMaxWidth(Double.MAX_VALUE);
    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(txtfldEditCategoryName);
    graphicPane.getChildren().add(1, txtfldEditCategoryName);

    txtfldEditCategoryName.setOnKeyReleased(new EventHandler<KeyEvent>() {

      @Override
      public void handle(KeyEvent t) {
        if (t.getCode() == KeyCode.ESCAPE) {
          cancelEdit();
        }
      }
    });

    txtfldEditCategoryName.setOnAction(event -> {
      if (txtfldEditCategoryName.getText().equals(category.getName()) == false)
        category.setName(txtfldEditCategoryName.getText());
      try {
//        commitEdit(getItem()); // throws an UnsupportedOperationException
        cancelEdit();
      } catch (Exception ex) {
        log.error("Could not commit changes to Category " + category, ex);
      }
    });

    txtfldEditCategoryName.focusedProperty().addListener(new ChangeListener<Boolean>() {
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
  public void commitEdit(Category newValue) {
    showCellInNotEditingState();

    super.commitEdit(newValue);
  }

  protected void showCellInEditingState() {
    categoryNameLabel.setVisible(false);
    txtfldEditCategoryName.setVisible(true);
  }

  protected void showCellInNotEditingState() {
    if(txtfldEditCategoryName != null)
      txtfldEditCategoryName.setVisible(false);

    categoryNameLabel.setVisible(true);
    categoryNameLabel.setText(category.getName());
  }

  protected void showContextMenu(ContextMenuEvent event) {
    ContextMenu contextMenu = createContextMenu();

    contextMenu.show(event.getPickResult().getIntersectedNode(), event.getScreenX(), event.getScreenY());
  }

  protected ContextMenu createContextMenu() {
    ContextMenu contextMenu = new ContextMenu();

    MenuItem editTagItem = new MenuItem();
    JavaFxLocalization.bindMenuItemText(editTagItem, "edit...");
    editTagItem.setOnAction(actionEvent -> {
      if(category != null)
        Dialogs.showEditCategoryDialog(category, getScene().getWindow(), true);
    });
    contextMenu.getItems().add(editTagItem);

    contextMenu.getItems().add(new SeparatorMenuItem());

    MenuItem deleteTagItem = new MenuItem();
    JavaFxLocalization.bindMenuItemText(deleteTagItem, "delete");
    deleteTagItem.setOnAction(event -> {
      if(category != null)
        if(Alerts.deleteCategoryWithUserConfirmationIfHasSubCategoriesOrEntries(category)) {
          if(editedCategoriesHolder.containsEditedEntity(category))
            editedCategoriesHolder.removeEntityFromEntry(category);
        }
    });
    contextMenu.getItems().add(deleteTagItem);

    return contextMenu;
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
