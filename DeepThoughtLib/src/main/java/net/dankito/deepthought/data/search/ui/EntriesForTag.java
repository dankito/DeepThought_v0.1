package net.dankito.deepthought.data.search.ui;

import net.dankito.deepthought.Application;
import net.dankito.deepthought.data.listener.AllEntitiesListener;
import net.dankito.deepthought.data.listener.ApplicationListener;
import net.dankito.deepthought.data.listener.IEntityChangesService;
import net.dankito.deepthought.data.model.DeepThought;
import net.dankito.deepthought.data.model.Entry;
import net.dankito.deepthought.data.model.Tag;
import net.dankito.deepthought.data.model.ui.SystemTag;
import net.dankito.deepthought.data.persistence.db.BaseEntity;
import net.dankito.deepthought.data.search.ISearchEngine;
import net.dankito.deepthought.data.search.SearchCompletedListener;
import net.dankito.deepthought.util.Notification;
import net.dankito.deepthought.util.NotificationType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by ganymed on 16/10/16.
 */
public class EntriesForTag {

  protected static final List<Entry> EntriesToShowIfNoTagIsSelected = new ArrayList<>();


  protected Tag currentTag;

  protected List<Entry> entriesForTag = new ArrayList<>();

  protected ISearchEngine searchEngine;

  protected IEntityChangesService entityChangesService;

  protected EntriesForTagRetrievedListener entriesForTagRetrievedListener;


  public EntriesForTag() {
    Application.addApplicationListener(applicationListener);
  }

  protected void applicationInitialized(ISearchEngine searchEngine, IEntityChangesService entityChangesService) {
    this.searchEngine = searchEngine;
    this.entityChangesService = entityChangesService;

    entityChangesService.addAllEntitiesListener(allEntitiesListener);

    DeepThought deepThought = Application.getDeepThought();
    if(deepThought.getSettings().getLastViewedTag() != null) {
      setTag(deepThought.getSettings().getLastViewedTag());
    }
    else {
      setTag(deepThought.AllEntriesSystemTag());
    }
  }


  public void setTag(Tag tag) {
    this.currentTag = tag;

    if(currentTag != null) {
      getEntriesForTag();
    }
    else {
      receivedEntriesForTag(EntriesToShowIfNoTagIsSelected);
    }
  }

  protected void getEntriesForTag() {
    if(searchEngine == null) { // Application not initialized yet
      return;
    }

    searchEngine.getEntriesForTagAsync(currentTag, new SearchCompletedListener<Collection<Entry>>() {
      @Override
      public void completed(Collection<Entry> results) {
        receivedEntriesForTag(results);
      }
    });
  }

  protected void receivedEntriesForTag(Collection<Entry> entries) {
    this.entriesForTag = createListFromCollection(entries);

    callEntriesForTagRetrievedListener(entriesForTag);
  }

  protected void callEntriesForTagRetrievedListener(List<Entry> entriesForTag) {
    if(entriesForTagRetrievedListener != null) {
      entriesForTagRetrievedListener.retrievedEntriesForTag(entriesForTag);
    }
  }


  protected <T> List<T> createListFromCollection(Collection<T> tagCollection) {
    List<T> tagList;

    if(tagCollection instanceof List) {
      tagList = (List<T>)tagCollection;
    }
    else {
      tagList = new ArrayList<T>(tagCollection); // TODO: use lazy loading list
    }

    return tagList;
  }


  public void setEntriesForTagRetrievedListener(EntriesForTagRetrievedListener entriesForTagRetrievedListener) {
    this.entriesForTagRetrievedListener = entriesForTagRetrievedListener;
  }


  protected ApplicationListener applicationListener = new ApplicationListener() {
    @Override
    public void deepThoughtChanged(DeepThought deepThought) {

    }

    @Override
    public void notification(Notification notification) {
      if(notification.getType() == NotificationType.ApplicationInstantiated) {
        Application.removeApplicationListener(applicationListener);

        applicationInitialized(Application.getSearchEngine(), Application.getEntityChangesService());
      }
    }
  };

  protected AllEntitiesListener allEntitiesListener = new AllEntitiesListener() {
    @Override
    public void entityCreated(BaseEntity entity) {
      if(entity instanceof Entry) {
        Entry entry = (Entry)entity;
        if(entry.hasTag(currentTag)) { // if an Entry is created with its Tags already set
          getEntriesForTag();
        }
      }
    }

    @Override
    public void entityUpdated(BaseEntity entity, String propertyName, Object previousValue, Object newValue) {

    }

    @Override
    public void entityDeleted(BaseEntity entity) {

    }

    @Override
    public void entityAddedToCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity addedEntity) {
      checkIfEntriesForTagChanged(collectionHolder, addedEntity);
    }

    @Override
    public void entityRemovedFromCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity removedEntity) {
      checkIfEntriesForTagChanged(collectionHolder, removedEntity);
    }
  };

  protected void checkIfEntriesForTagChanged(BaseEntity collectionHolder, BaseEntity addedOrRemovedEntity) {
    if(addedOrRemovedEntity instanceof Entry) {
      if(collectionHolder == currentTag) {
        getEntriesForTag();
      }
      else if(collectionHolder instanceof DeepThought && currentTag instanceof SystemTag) {
        getEntriesForTag();
      }
    }
  }

}
