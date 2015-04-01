package net.deepthought.model;

import net.deepthought.data.model.DeepThought;
import net.deepthought.data.model.Entry;
import net.deepthought.data.model.listener.EntityListener;
import net.deepthought.data.persistence.db.BaseEntity;
import net.deepthought.util.Localization;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by ganymed on 09/12/14.
 */
public class EntriesWithoutTagsSystemTag extends SystemTag {

  public EntriesWithoutTagsSystemTag(DeepThought deepThought) {
    super(deepThought, Localization.getLocalizedStringForResourceKey("system.tag.entries.with.no.tags"));

//    deepThought.addEntriesChangedListener(entriesChangedListener);
    deepThought.addEntityListener(entityListener);
    this.filteredEntries = filterDeepThoughtEntries();
  }

  @Override
  public boolean hasEntries() {
    return super.hasEntries();
  }

  protected Collection<Entry> filterDeepThoughtEntries() {
    List<Entry> entriesWithoutTags = new ArrayList<>();

    for(Entry entry : deepThought.getEntries()) {
      entry.addEntityListener(entryListener);

      if(entry.hasTags() == false)
        entriesWithoutTags.add(entry);
    }

    Collections.sort(entriesWithoutTags, new Comparator<Entry>() {
      @Override
      public int compare(Entry o1, Entry o2) {
        return ((Integer)o2.getEntryIndex()).compareTo(o1.getEntryIndex());
      }
    });

    return entriesWithoutTags;
  }


//  protected EntriesChangedListener entriesChangedListener = new EntriesChangedListener() {
//    @Override
//    public void entryAdded(Entry entry) {
//      entry.addEntryListener(entryListener);
//
//      if(entry.hasTags() == false) {
////        EntriesWithoutTagsSystemTag.this.addAsAuthorOnEntry(entryFragment);
//        filteredEntries.add(entry);
//        callEntryAddedListeners(entry);
//      }
//    }
//
//    @Override
//    public void entryUpdated(Entry entry) {
//
//    }
//
//    @Override
//    public void entryRemoved(Entry entry) {
//      entry.removeEntryListener(entryListener);
//
////      if(EntriesWithoutTagsSystemTag.this.entryFragments.contains(entryFragment)) {
////        EntriesWithoutTagsSystemTag.this.removeAsAuthorFromEntry(entryFragment);
////      }
//
//      if(filteredEntries.contains(entry)) {
//        filteredEntries.remove(entry);
//        callEntryRemovedListeners(entry);
//      }
//    }
//  };

  protected EntityListener entityListener = new EntityListener() {
    @Override
    public void propertyChanged(BaseEntity entity, String propertyName, Object previousValue, Object newValue) {

    }

    @Override
    public void entityAddedToCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity addedEntity) {
      if(collection == deepThought.getEntries()) {
        Entry entry = (Entry)addedEntity;
        entry.addEntityListener(entryListener);

        if(entry.hasTags() == false) {
//        EntriesWithoutTagsSystemTag.this.addAsAuthorOnEntry(entryFragment);
          filteredEntries.add(entry);
          callEntityAddedListeners(entries, entry);
        }
      }
    }

    @Override
    public void entityOfCollectionUpdated(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity updatedEntity) {

    }

    @Override
    public void entityRemovedFromCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity removedEntity) {
      if(collection == deepThought.getEntries()) {
        Entry entry = (Entry)removedEntity;
        entry.removeEntityListener(entryListener);

//      if(EntriesWithoutTagsSystemTag.this.entryFragments.contains(entryFragment)) {
//        EntriesWithoutTagsSystemTag.this.removeAsAuthorFromEntry(entryFragment);
//      }

        if(filteredEntries.contains(entry)) {
          filteredEntries.remove(entry);
          callEntityRemovedListeners(entries, entry);
        }
      }
    }
  };


  protected EntityListener entryListener = new EntityListener() {
    @Override
    public void propertyChanged(BaseEntity entity, String propertyName, Object previousValue, Object newValue) {

    }

    @Override
    public void entityAddedToCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity addedEntity) {
      Entry entry = ((Entry)collectionHolder);
      if(collection == entry.getTags()) {
        if(entry.getTags().size() == 1) {
//        EntriesWithoutTagsSystemTag.this.removeAsAuthorFromEntry(entryFragment);
          filteredEntries.remove(entry);
          callEntityRemovedListeners(entries, entry);
        }
      }
    }

    @Override
    public void entityOfCollectionUpdated(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity updatedEntity) {

    }

    @Override
    public void entityRemovedFromCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity removedEntity) {
      Entry entry = ((Entry)collectionHolder);
      if(collection == entry.getTags()) {
        if(entry.hasTags() == false) {
//        EntriesWithoutTagsSystemTag.this.addAsAuthorOnEntry(entryFragment);
          filteredEntries.add(entry);
          callEntityAddedListeners(entries, entry);
        }
      }
    }
  };

}
