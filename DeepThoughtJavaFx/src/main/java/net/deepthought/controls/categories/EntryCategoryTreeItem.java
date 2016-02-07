package net.deepthought.controls.categories;

import net.deepthought.Application;
import net.deepthought.controls.ICleanUp;
import net.deepthought.data.model.Category;
import net.deepthought.data.model.listener.EntityListener;
import net.deepthought.data.persistence.db.BaseEntity;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javafx.event.Event;
import javafx.event.EventType;
import javafx.scene.control.TreeItem;

/**
 * Created by ganymed on 03/02/15.
 */
public class EntryCategoryTreeItem extends TreeItem<Category> implements ICleanUp {

  protected Category category;

  protected Map<Category, EntryCategoryTreeItem> mapSubCategoryToTreeItem = new HashMap<>();


  public EntryCategoryTreeItem(Category category) {
    super(category);
    this.category = category;
    this.category.addEntityListener(categoryListener);

    if(category.getParentCategory() == null || category.getParentCategory().equals(Application.getDeepThought().getTopLevelCategory()))
      setExpanded(true);

    // TODO: add Sub Categories on demand not in constructor
    addSubCategoriesTreeItems(category);
  }

  @Override
  public void cleanUp() {
    if(category != null)
      category.removeEntityListener(categoryListener);

    for(EntryCategoryTreeItem item : mapSubCategoryToTreeItem.values())
      item.cleanUp();
  }

  protected void addSubCategoriesTreeItems(Category category) {
    for(Category subCategory : category.getSubCategories())
      addSubCategoryItem(subCategory);
  }

  protected void addSubCategoryItem(Category subCategory) {
    EntryCategoryTreeItem subCategoryTreeItem = new EntryCategoryTreeItem(subCategory);
    if(subCategory.getCategoryOrder() < this.getChildren().size())
      this.getChildren().add(subCategory.getCategoryOrder(), subCategoryTreeItem);
    else
      this.getChildren().add(subCategoryTreeItem);

    mapSubCategoryToTreeItem.put(subCategory, subCategoryTreeItem);
  }

  protected void removeSubCategoryItem(Category subCategory) {
    if(mapSubCategoryToTreeItem.containsKey(subCategory))
      this.getChildren().remove(mapSubCategoryToTreeItem.get(subCategory));
  }


  protected void categoryUpdated() {
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
      if(collection == category.getSubCategories()) {
        addSubCategoryItem((Category)addedEntity);
      }
    }

    @Override
    public void entityOfCollectionUpdated(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity updatedEntity) {

    }

    @Override
    public void entityRemovedFromCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity removedEntity) {
      if(collection == category.getSubCategories()) {
        removeSubCategoryItem((Category) removedEntity);
      }
    }
  };

}
