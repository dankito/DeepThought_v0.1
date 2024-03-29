package net.dankito.deepthought.controls.categories;

import net.dankito.deepthought.controls.CollectionItemLabel;
import net.dankito.deepthought.controls.event.CollectionItemLabelEvent;
import net.dankito.deepthought.data.model.Category;
import net.dankito.deepthought.data.model.listener.EntityListener;
import net.dankito.deepthought.data.persistence.db.BaseEntity;
import net.dankito.deepthought.util.localization.JavaFxLocalization;

import java.util.Collection;

import javafx.event.EventHandler;

/**
 * Created by ganymed on 01/02/15.
 */
public class EntryCategoryLabel extends CollectionItemLabel {

  protected Category category;


  public EntryCategoryLabel(Category category, EventHandler<CollectionItemLabelEvent> onButtonRemoveItemFromCollectionEventHandler) {
    super(onButtonRemoveItemFromCollectionEventHandler);

    this.category = category;

    category.addEntityListener(categoryListener);

    setUserData(category);
    JavaFxLocalization.bindControlToolTip(btnRemoveItemFromCollection, "tool.tip.click.to.remove.category.from.entry", category.getName());
    itemDisplayNameUpdated();
  }


  @Override
  public void cleanUp() {
    super.cleanUp();

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
