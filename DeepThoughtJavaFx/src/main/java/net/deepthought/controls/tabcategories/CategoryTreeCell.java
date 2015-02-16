package net.deepthought.controls.tabcategories;

import net.deepthought.controls.TextFieldTreeCell;
import net.deepthought.data.model.Category;
import net.deepthought.util.Alerts;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;

/**
 * Created by ganymed on 27/11/14.
 */
public class CategoryTreeCell extends TextFieldTreeCell<Category> {

  protected Category category;


  public CategoryTreeCell() {

  }


  @Override
  protected void newItemSet(Category newValue) {
    super.newItemSet(newValue);
    categoryChanged(newValue);
  }

  protected void categoryChanged(Category category) {
    if(this.category != null) {
      setContextMenu(null);
    }

    this.category = category;

    if(category != null) {
      setContextMenu(createContextMenu());
    }

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

  protected ContextMenu createContextMenu() {
    ContextMenu contextMenu = new ContextMenu();

    addAddSubCategoryMenuItem(contextMenu);

    contextMenu.getItems().add(new SeparatorMenuItem());

    addRenameMenuItem(contextMenu);

    addDeleteMenuItem(contextMenu);

    return contextMenu;
  }

  protected void addAddSubCategoryMenuItem(ContextMenu contextMenu) {
    MenuItem addCategoryMenuItem = new MenuItem("Add SubCategory");
    contextMenu.getItems().add(addCategoryMenuItem);

    addCategoryMenuItem.setOnAction(new EventHandler() {
      public void handle(Event t) {
        getTreeItem().setExpanded(true);

        Category newCategory = new Category(getItem().getDefaultEntryTemplate());
        getItem().addSubCategory(newCategory);
      }
    });
  }

  protected void addRenameMenuItem(ContextMenu contextMenu) {
    MenuItem renameMenuItem = new MenuItem("Rename");
    contextMenu.getItems().add(renameMenuItem);

    renameMenuItem.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent event) {
        startEdit();
      }
    });
  }

  protected void addDeleteMenuItem(ContextMenu contextMenu) {
    MenuItem deleteMenuItem = new MenuItem("Delete");
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
