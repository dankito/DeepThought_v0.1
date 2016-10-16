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
import net.dankito.deepthought.data.search.specific.FindAllEntriesHavingTheseTagsResult;
import net.dankito.deepthought.util.Notification;
import net.dankito.deepthought.util.NotificationType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by ganymed on 16/10/16.
 */
public class EntriesForTag {

  protected static final List<Entry> EntriesToShowIfNoTagIsSelected = new ArrayList<>();


  protected Tag currentTag;

  protected List<Entry> entriesForTag = new ArrayList<>();

  protected FindAllEntriesHavingTheseTagsResult lastFilterTagsResult;

  protected ISearchEngine searchEngine;

  protected IEntityChangesService entityChangesService;

  protected List<EntriesForTagRetrievedListener> entriesForTagRetrievedListeners = new CopyOnWriteArrayList<>();


  public EntriesForTag() {
    Application.addApplicationListener(applicationListener);

    if(Application.isInstantiated()) {
      applicationInitialized(Application.getSearchEngine(), Application.getEntityChangesService());
    }
  }

  protected void applicationInitialized(ISearchEngine searchEngine, IEntityChangesService entityChangesService) {
    this.searchEngine = searchEngine;
    this.entityChangesService = entityChangesService;

    entityChangesService.addAllEntitiesListener(allEntitiesListener);
  }

  protected void deepThoughtChanged(DeepThought deepThought) {
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
      retrieveEntriesForTag();
    }
    else {
      receivedEntriesForTag(EntriesToShowIfNoTagIsSelected);
    }
  }

  protected void retrieveEntriesForTag() {
    if(searchEngine == null) { // Application not initialized yet
      return;
    }

    if(lastFilterTagsResult == null) {
      searchEngine.getEntriesForTagAsync(currentTag, new SearchCompletedListener<Collection<Entry>>() {
        @Override
        public void completed(Collection<Entry> results) {
          receivedEntriesForTag(results);
        }
      });
    }
    else {
      showEntriesForSelectedTagWithAppliedTagsFilter(currentTag);
    }
  }

  protected void showEntriesForSelectedTagWithAppliedTagsFilter(Tag tag) {
    List<Entry> filteredEntriesWithThisTag = new ArrayList<>();

    Application.getThreadPool().runTaskAsync(new Runnable() {
      @Override
      public void run() {
        Collection<Entry> debug = lastFilterTagsResult.getEntriesHavingFilteredTags();
        if(debug.size() > 0) { }
      }
    });

    for(Entry entry : lastFilterTagsResult.getEntriesHavingFilteredTags()) {
      if(entry.hasTag(tag))
        filteredEntriesWithThisTag.add(entry);
    }

    receivedEntriesForTag(filteredEntriesWithThisTag); // TODO: here can may be a problem when Entries are search. How to know about this filter?
  }

  protected void receivedEntriesForTag(Collection<Entry> entries) {
    this.entriesForTag = createListFromCollection(entries);

    callEntriesForTagRetrievedListener(entriesForTag);
  }

  protected void callEntriesForTagRetrievedListener(List<Entry> entriesForTag) {
    for(EntriesForTagRetrievedListener listener : entriesForTagRetrievedListeners) {
      listener.retrievedEntriesForTag(entriesForTag);
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


  public boolean addEntriesForTagRetrievedListener(EntriesForTagRetrievedListener entriesForTagRetrievedListener) {
    return this.entriesForTagRetrievedListeners.add(entriesForTagRetrievedListener);
  }

  public boolean removeEntriesForTagRetrievedListener(EntriesForTagRetrievedListener entriesForTagRetrievedListener) {
    return this.entriesForTagRetrievedListeners.remove(entriesForTagRetrievedListener);
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

        EntriesForTag.this.deepThoughtChanged(Application.getDeepThought());
      }
    }
  };

  protected AllEntitiesListener allEntitiesListener = new AllEntitiesListener() {
    @Override
    public void entityCreated(BaseEntity entity) {
      if(entity instanceof Entry) {
        Entry entry = (Entry)entity;
        if(entry.hasTag(currentTag)) { // if an Entry is created with its Tags already set
          retrieveEntriesForTag();
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
        retrieveEntriesForTag();
      }
      else if(collectionHolder instanceof DeepThought && currentTag instanceof SystemTag) {
        retrieveEntriesForTag();
      }
    }
  }

  public void setFindAllEntriesHavingTheseTagsResult(FindAllEntriesHavingTheseTagsResult lastFilterTagsResult) {
    this.lastFilterTagsResult = lastFilterTagsResult;

    retrieveEntriesForTag();
  }


  public List<Entry> getEntriesForTag() {
    return entriesForTag;
  }

}
