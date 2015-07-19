package net.deepthought.controls.reference;

import net.deepthought.controls.CollectionItemLabel;
import net.deepthought.controls.event.CollectionItemLabelEvent;
import net.deepthought.data.model.ReferenceBase;
import net.deepthought.data.model.listener.EntityListener;
import net.deepthought.data.persistence.db.BaseEntity;

import java.util.Collection;

import javafx.event.EventHandler;

/**
 * Created by ganymed on 08/04/15.
 */
public class EntryReferenceBaseLabel extends CollectionItemLabel {

  protected ReferenceBase referenceBase;

  public EntryReferenceBaseLabel(ReferenceBase referenceBase, EventHandler<CollectionItemLabelEvent> onButtonRemoveItemFromCollectionEventHandler) {
    super(onButtonRemoveItemFromCollectionEventHandler);
    this.referenceBase = referenceBase;
    referenceBase.addEntityListener(referenceBaseListener);

    itemDisplayNameUpdated();
  }

  @Override
  protected String getItemDisplayName() {
    if(referenceBase != null)
      return referenceBase.getTextRepresentation();
    return "";
  }

  @Override
  protected String getToolTipText() {
    return getItemDisplayName();
  }

  protected EntityListener referenceBaseListener = new EntityListener() {
    @Override
    public void propertyChanged(BaseEntity entity, String propertyName, Object previousValue, Object newValue) {
      itemDisplayNameUpdated();
    }

    @Override
    public void entityAddedToCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity addedEntity) {

    }

    @Override
    public void entityOfCollectionUpdated(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity updatedEntity) {

    }

    @Override
    public void entityRemovedFromCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity removedEntity) {

    }
  };

}
