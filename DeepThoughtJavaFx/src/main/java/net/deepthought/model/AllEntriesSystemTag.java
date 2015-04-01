package net.deepthought.model;

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

  public AllEntriesSystemTag(DeepThought deepThought) {
    super(deepThought, Localization.getLocalizedStringForResourceKey("system.tag.all.entries")); // TODO: this will not react on Language change

    deepThought.addEntityListener(entityListener);
//    this.filteredEntries = new ArrayList<>(deepThought.getEntries());
    this.filteredEntries = deepThought.getEntries();
  }

//  @Override
//  protected Set<EntryFragment> filterDeepThoughtEntries() {
//    return deepThought.getAuthorOnEntries();
//  }


//  protected EntriesChangedListener entriesChangedListener = new EntriesChangedListener() {
//    @Override
//    public void entryAdded(Entry entry) {
////      AllEntriesSystemTag.this.deepThought.add(entry);
//
////      addAsAuthorOnEntry(entryFragment);
//
//      filteredEntries.add(entry);
//      callEntryAddedListeners(entry);
//    }
//
//    @Override
//    public void entryUpdated(Entry entry) {
//
//    }
//
//    @Override
//    public void entryRemoved(Entry entry) {
////      AllEntriesSystemTag.this.deepThought.remove(entry);
//
////      removeAsAuthorFromEntry(entryFragment);
//
//      filteredEntries.remove(entry);
//      callEntryRemovedListeners(entry);
//    }
//  };


  protected EntityListener entityListener = new EntityListener() {
    @Override
    public void propertyChanged(BaseEntity entity, String propertyName, Object previousValue, Object newValue) {

    }

    @Override
    public void entityAddedToCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity addedEntity) {
      if(collection == deepThought.getEntries()) {
//        filteredEntries.add((Entry) addedEntity); // TODO: this is not the correct sort order afterwards as new Entries should be shown first in list, not last
        callEntityAddedListeners(entries, (Entry) addedEntity);
      }
    }

    @Override
    public void entityOfCollectionUpdated(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity updatedEntity) {

    }

    @Override
    public void entityRemovedFromCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity removedEntity) {
      if(collection == deepThought.getEntries()) {
//        filteredEntries.remove((Entry) removedEntity);
        callEntityRemovedListeners(entries, (Entry)removedEntity);
      }
    }
  };

}
