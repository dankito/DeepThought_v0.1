package net.dankito.deepthought.data.model.ui;

import net.dankito.deepthought.Application;
import net.dankito.deepthought.data.listener.AllEntitiesListener;
import net.dankito.deepthought.data.model.DeepThought;
import net.dankito.deepthought.data.model.Entry;
import net.dankito.deepthought.data.model.Tag;
import net.dankito.deepthought.data.persistence.db.BaseEntity;
import net.dankito.deepthought.data.search.SearchCompletedListener;
import net.dankito.deepthought.util.localization.Localization;

import java.util.Collection;
import java.util.List;

/**
 * Created by ganymed on 09/12/14.
 */
public class EntriesWithoutTagsSystemTag extends SystemTag {

  public final static String ID = "-43";


  public EntriesWithoutTagsSystemTag(DeepThought deepThought) {
    super(deepThought);

    Application.getEntityChangesService().addAllEntitiesListener(allEntitiesListener);

    Application.getSearchEngine().getEntriesWithoutTags(new SearchCompletedListener<Collection<Entry>>() {
      @Override
      public void completed(Collection<Entry> results) {
        filteredEntries = results;
//        callEntityAddedListeners(entries, entry);
      }
    });
  }


  @Override
  public String getId() {
    return ID;
  }


  protected AllEntitiesListener allEntitiesListener = new AllEntitiesListener() {
    @Override
    public void entityCreated(BaseEntity entity) {

    }

    @Override
    public void entityUpdated(BaseEntity entity, String propertyName, Object previousValue, Object newValue) {

    }

    @Override
    public void entityDeleted(BaseEntity entity) {

    }

    @Override
    public void entityAddedToCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity addedEntity) {
      EntriesWithoutTagsSystemTag.this.entityAddedToCollection(collectionHolder, addedEntity);
    }

    @Override
    public void entityRemovedFromCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity removedEntity) {
      EntriesWithoutTagsSystemTag.this.entityRemovedFromCollection(collectionHolder, removedEntity);
    }
  };

  protected void entityAddedToCollection(BaseEntity collectionHolder, BaseEntity addedEntity) {
    if(collectionHolder instanceof Entry && addedEntity instanceof Tag) {
      Entry entry = (Entry)collectionHolder;
      if(entry.getCountTags() == 1) {
        removeEntryFromFilteredEntries(entry);
      }
    }
    else if(collectionHolder instanceof DeepThought && addedEntity instanceof Entry) {
      Entry entry = (Entry)addedEntity;
      if(entry.getCountTags() == 0) {
        addEntryToFilteredEntries(entry);
      }
    }
  }

  protected void entityRemovedFromCollection(BaseEntity collectionHolder, BaseEntity removedEntity) {
    if(collectionHolder instanceof Entry && removedEntity instanceof Tag) {
      Entry entry = (Entry)collectionHolder;
      if(entry.getCountTags() == 0) {
        addEntryToFilteredEntries(entry);
      }
    }
    else if(collectionHolder instanceof DeepThought && removedEntity instanceof Entry) {
      removeEntryFromFilteredEntries((Entry)removedEntity);
    }
  }

  protected void removeEntryFromFilteredEntries(Entry entry) {
    filteredEntries.remove(entry);
    callEntityRemovedListeners(entries, entry);
  }

  protected void addEntryToFilteredEntries(Entry entry) {
    if(filteredEntries instanceof List) {
      ((List) filteredEntries).add(0, entry);
    }
    else {
      filteredEntries.add(entry);
    }

    callEntityAddedListeners(filteredEntries, entry);
  }


  @Override
  protected String getSystemTagName() {
    return Localization.getLocalizedString("system.tag.entries.with.no.tags");
  }

}
