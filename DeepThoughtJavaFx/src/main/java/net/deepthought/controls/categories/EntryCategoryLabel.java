package net.deepthought.controls.categories;

import net.deepthought.controls.CollectionItemLabel;
import net.deepthought.controls.event.CollectionItemLabelEvent;
import net.deepthought.data.model.Category;
import net.deepthought.data.model.Entry;
import net.deepthought.data.model.listener.EntityListener;
import net.deepthought.data.persistence.db.BaseEntity;
import net.deepthought.util.JavaFxLocalization;

import java.util.Collection;

import javafx.event.EventHandler;

/**
 * Created by ganymed on 01/02/15.
 */
public class EntryCategoryLabel extends CollectionItemLabel {

  protected Entry entry;

  protected Category category;


  public EntryCategoryLabel(Entry entry, Category category, EventHandler<CollectionItemLabelEvent> onButtonRemoveItemFromCollectionEventHandler) {
    super(onButtonRemoveItemFromCollectionEventHandler);

    this.entry = entry;
    this.category = category;

    category.addEntityListener(categoryListener);

    setUserData(category);
    JavaFxLocalization.bindControlToolTip(btnRemoveItemFromCollection, "tool.tip.click.to.remove.category.from.entry", category.getName(), entry.getPreview());
    itemDisplayNameUpdated();
  }


  @Override
  public void cleanUpControl() {
    super.cleanUpControl();

    if(category != null)
      category.removeEntityListener(categoryListener);
  }

  @Override
  protected String getItemDisplayName() {
    if(category != null)
      return category.getName();
    return "";
  }

  @Override
  protected String getToolTipText() {
    if(category != null) {
      if(category.getDescription() != null && category.getDescription().isEmpty() == false)
        return category.getName() + " (" + category.getDescription() + ")";
      return category.getName();
    }
    return "";
  }


  protected EntityListener categoryListener = new EntityListener() {
    @Override
    public void propertyChanged(BaseEntity entity, String propertyName, Object previousValue, Object newValue) {
      itemDisplayNameUpdated();
    }

    @Override
    public void entityAddedToCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity addedEntity) {
      if(collection == category.getEntries()) // TODO: senseful?
        itemDisplayNameUpdated();
    }

    @Override
    public void entityOfCollectionUpdated(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity updatedEntity) {

    }

    @Override
    public void entityRemovedFromCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity removedEntity) {
      if(collection == category.getEntries()) // TODO: senseful?
        itemDisplayNameUpdated();
    }
  };

}
