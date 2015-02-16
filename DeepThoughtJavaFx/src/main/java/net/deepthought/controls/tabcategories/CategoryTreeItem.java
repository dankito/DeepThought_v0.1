package net.deepthought.controls.tabcategories;

import net.deepthought.data.model.Category;
import net.deepthought.data.model.listener.EntityListener;
import net.deepthought.data.persistence.db.BaseEntity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventType;
import javafx.scene.control.TreeItem;

/**
 * Created by ganymed on 26/11/14.
 */
public class CategoryTreeItem extends TreeItem<Category> {

  private final static Logger log = LoggerFactory.getLogger(CategoryTreeItem.class);


  protected Category category;

  protected boolean haveChildrenBeenLoaded = false;


  public CategoryTreeItem(final Category category) {
    super(category);
    this.category = category;
//    this.category.addCategoryListener(categoryListener);
    this.category.addEntityListener(categoryListener);
    setExpanded(category.isExpanded());

    expandedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        if(category != null)
          category.setIsExpanded(newValue);
      }
    });
  }


  @Override
  public boolean isLeaf() {
    return category.getSubCategories().size() == 0;
  }

  @Override
  public synchronized ObservableList<TreeItem<Category>> getChildren() {
    if(haveChildrenBeenLoaded == false) {
      ObservableList<TreeItem<Category>> children = super.getChildren();
      List<TreeItem<Category>> items = new ArrayList<>();
      for(Category subCategory : category.getSubCategories()) {
        items.add(new CategoryTreeItem(subCategory));
      }

      haveChildrenBeenLoaded = true;
      children.clear();
      children.addAll(items);
    }

    return super.getChildren(); // even in case haveChildrenBeenLoaded has been false, do some sorting, ... in parent's method
  }


  protected void categoryUpdated() {
    fireEvent(valueChangedEvent());
  }

  protected void subCategoriesUpdated() {
//    haveChildrenBeenLoaded = true;
    fireEvent(valueChangedEvent());
  }

  protected void fireEvent(EventType e) {
    Event.fireEvent(this, new TreeModificationEvent(e, this));
  }


  protected EntityListener categoryListener = new EntityListener() {
    @Override
    public void propertyChanged(BaseEntity entity, String propertyName, Object previousValue, Object newValue) {
      categoryUpdated();
    }

    @Override
    public void entityAddedToCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity addedEntity) {
      if (collection.equals(category.getSubCategories())) {
        Category parentCategory = (Category) collectionHolder;
        Category subCategory = (Category) addedEntity;

        log.debug("Added {} to Category {}, haveChildrenBeenLoaded = {}", subCategory, parentCategory, haveChildrenBeenLoaded);
        CategoryTreeItem newItem = new CategoryTreeItem(subCategory);

        if (haveChildrenBeenLoaded == true)
          getChildren().add(newItem);
        else
          getChildren();

//      subCategoriesUpdated();
        CategoryTreeItem.this.setExpanded(true);
      }
    }

    @Override
    public void entityOfCollectionUpdated(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity updatedEntity) {

    }

    @Override
    public void entityRemovedFromCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity removedEntity) {
      if (collection.equals(category.getSubCategories())) {
        Category parentCategory = (Category) collectionHolder;
        Category subCategory = (Category) removedEntity;

        ObservableList<TreeItem<Category>> children = getChildren();

        for (int i = 0; i < children.size(); i++) {
          TreeItem<Category> child = children.get(i);
          if (subCategory.equals(child.getValue())) {
            TreeItem itemToSelectNext = child.previousSibling() != null ? child.previousSibling() : child.getParent();
            children.remove(child);
            subCategoriesUpdated();

            break;
          }
        }
      }
    }
  };

//  protected CategoryListener categoryListener = new CategoryListener() {
//    @Override
//    public void subCategoryAdded(Category parentCategory, Category subCategory) {
//      log.debug("Added {} to Category {}, haveChildrenBeenLoaded = {}", subCategory, parentCategory, haveChildrenBeenLoaded);
//      CategoryTreeItem newItem = new CategoryTreeItem(subCategory);
//
//      if(haveChildrenBeenLoaded == true)
//        getChildren().add(newItem);
//      else
//        getChildren();
//
////      subCategoriesUpdated();
//      CategoryTreeItem.this.setExpanded(true);
//    }
//
//    @Override
//    public void subCategoryRemoved(Category parentCategory, Category subCategory) {
//      ObservableList<TreeItem<Category>> children = getChildren();
//
//      for(int i = 0; i < children.size(); i++) {
//        TreeItem<Category> child = children.get(i);
//        if(subCategory.equals(child.getValue())) {
//          TreeItem itemToSelectNext = child.previousSibling() != null ? child.previousSibling() : child.getParent();
//          children.remove(child);
//          subCategoriesUpdated();
//
//          break;
//        }
//      }
//    }
//
//    @Override
//    public void entryAdded(Category category, Entry entry) {
//
//    }
//
//    @Override
//    public void entryRemoved(Category category, Entry entry) {
//
//    }
//
//    @Override
//    public void propertyChanged(Category entity, String propertyName, Object newValue, Object previewValue) {
//      categoryUpdated();
//    }
//  };

  @Override
  public String toString() {
    return category.getName();
  }
}
