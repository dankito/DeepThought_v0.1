package net.deepthought.data.model.ui;

import net.deepthought.data.model.DeepThought;
import net.deepthought.data.model.Entry;
import net.deepthought.data.model.listener.EntityListener;
import net.deepthought.data.persistence.db.BaseEntity;
import net.deepthought.util.Localization;

import java.util.Collection;

/**
 * Created by ganymed on 09/12/14.
 */
public class AllEntriesSystemTag extends SystemTag {

  public final static Long ID = -42L;


  public AllEntriesSystemTag(DeepThought deepThought) {
    super(deepThought, Localization.getLocalizedString("system.tag.all.entries")); // TODO: this will not react on Language change

    deepThought.addEntityListener(deepThoughtListener);
    this.filteredEntries = deepThought.getEntries();
  }


  @Override
  public Long getId() {
    return ID;
  }

  protected EntityListener deepThoughtListener = new EntityListener() {
    @Override
    public void propertyChanged(BaseEntity entity, String propertyName, Object previousValue, Object newValue) {

    }

    @Override
    public void entityAddedToCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity addedEntity) {
      if(collection == deepThought.getEntries()) {
//        filteredEntries.add((Entry) addedEntity); // TODO: this is not the correct sort order afterwards as new Entries should be shown first in list, not last
        callEntityAddedListeners(filteredEntries, (Entry) addedEntity);
      }
    }

    @Override
    public void entityOfCollectionUpdated(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity updatedEntity) {

    }

    @Override
    public void entityRemovedFromCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity removedEntity) {
      if(collection == deepThought.getEntries()) {
//        filteredEntries.remove((Entry) removedEntity);
        callEntityRemovedListeners(filteredEntries, (Entry)removedEntity);
      }
    }
  };

}
