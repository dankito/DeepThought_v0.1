package net.dankito.deepthought.controls.tag;

import net.dankito.deepthought.data.model.Tag;
import net.dankito.deepthought.data.model.listener.EntityListener;
import net.dankito.deepthought.data.persistence.db.BaseEntity;

import java.util.Collection;

import javafx.event.EventHandler;

/**
 * Created by ganymed on 01/02/15.
 */
public class EntryTagLabel extends net.dankito.deepthought.controls.CollectionItemLabel {

  protected Tag tag;


  public EntryTagLabel(Tag tag, EventHandler<net.dankito.deepthought.controls.event.CollectionItemLabelEvent> onButtonRemoveItemFromCollectionEventHandler) {
    super(onButtonRemoveItemFromCollectionEventHandler);
    this.tag = tag;

    tag.addEntityListener(tagListener);

    setUserData(tag);
    net.dankito.deepthought.util.localization.JavaFxLocalization.bindControlToolTip(btnRemoveItemFromCollection, "tool.tip.click.to.remove.tag.from.entry", tag.getName());
    itemDisplayNameUpdated();
  }


  @Override
  public void cleanUp() {
    super.cleanUp();

    if(tag != null)
      tag.removeEntityListener(tagListener);
  }

  @Override
  protected String getItemDisplayName() {
    if(tag != null)
      return tag.getName();
    return "";
  }

  @Override
  protected String getToolTipText() {
    if(tag != null) {
      if(tag.getDescription() != null && tag.getDescription().isEmpty() == false)
        return tag.getName() + " (" + tag.getDescription() + ")";
      return tag.getName();
    }
    return "";
  }


  protected EntityListener tagListener = new EntityListener() {
    @Override
    public void propertyChanged(BaseEntity entity, String propertyName, Object previousValue, Object newValue) {
      itemDisplayNameUpdated();
    }

    @Override
    public void entityAddedToCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity addedEntity) {
      if(collection == tag.getEntries())
        itemDisplayNameUpdated();
    }

    @Override
    public void entityOfCollectionUpdated(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity updatedEntity) {

    }

    @Override
    public void entityRemovedFromCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity removedEntity) {
      if(collection == tag.getEntries())
        itemDisplayNameUpdated();
    }
  };

}
