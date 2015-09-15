package net.deepthought.controls.tabcategories;

import net.deepthought.controller.Dialogs;
import net.deepthought.controls.TextFieldTreeCell;
import net.deepthought.data.model.Category;
import net.deepthought.util.Alerts;
import net.deepthought.util.JavaFxLocalization;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.ContextMenuEvent;

/**
 * Created by ganymed on 27/11/14.
 */
public class CategoryTreeCell extends TextFieldTreeCell<Category> {

  protected Category category;


  public CategoryTreeCell() {
    setOnContextMenuRequested(event -> showContextMenu(event));
  }


  @Override
  protected void newItemSet(Category newValue) {
    super.newItemSet(newValue);
    categoryChanged(newValue);
  }

  protected void categoryChanged(Category category) {
    this.category = category;

    categoryUpdated();
  }

  protected void categoryUpdated() {
    updateItem(category, getItemTextRepresentation().isEmpty());
  }

  protected String getItemTextRepresentation() {
    return category == null ? "" : category.getName();
  }

  @Override
  protected void itemValueUpdated(String newValue, String oldValue) {
    getItem().setName(newValue);
  }


  protected void showContextMenu(ContextMenuEvent event) {
    ContextMenu contextMenu = createContextMenu();

    contextMenu.show(event.getPickResult().getIntersectedNode(), event.getScreenX(), event.getScreenY());
  }

  protected ContextMenu createContextMenu() {
    ContextMenu contextMenu = new ContextMenu();

    addAddSubCategoryMenuItem(contextMenu);

    contextMenu.getItems().add(new SeparatorMenuItem());

    addEditMenuItem(contextMenu);

    addDeleteMenuItem(contextMenu);

    return contextMenu;
  }

  protected void addAddSubCategoryMenuItem(ContextMenu contextMenu) {
    MenuItem addCategoryMenuItem = new MenuItem();
    JavaFxLocalization.bindMenuItemText(addCategoryMenuItem, "add.sub.category");
    contextMenu.getItems().add(addCategoryMenuItem);

    addCategoryMenuItem.setOnAction(new EventHandler() {
      public void handle(Event t) {
        getTreeItem().setExpanded(true);

        Category newCategory = new Category();
        getItem().addSubCategory(newCategory);
//        Dialogs.showEditCategoryDialog(new Category(), getItem(), getScene().getWindow(), true);
      }
    });
  }

  protected void addEditMenuItem(ContextMenu contextMenu) {
    MenuItem renameMenuItem = new MenuItem();
    JavaFxLocalization.bindMenuItemText(renameMenuItem, "edit...");
    contextMenu.getItems().add(renameMenuItem);

    renameMenuItem.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent event) {
        Dialogs.showEditCategoryDialog(category, getScene().getWindow(), true);
      }
    });
  }

  protected void addDeleteMenuItem(ContextMenu contextMenu) {
    MenuItem deleteMenuItem = new MenuItem();
    JavaFxLocalization.bindMenuItemText(deleteMenuItem, "delete");
    contextMenu.getItems().add(deleteMenuItem);

    deleteMenuItem.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent event) {
        Category category = getItem();
        Alerts.deleteCategoryWithUserConfirmationIfHasSubCategoriesOrEntries(category);
      }
    });
  }

}
