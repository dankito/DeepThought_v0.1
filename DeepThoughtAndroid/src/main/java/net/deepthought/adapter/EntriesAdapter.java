package net.deepthought.adapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import net.deepthought.Application;
import net.deepthought.R;
import net.deepthought.data.listener.ApplicationListener;
import net.deepthought.data.model.DeepThought;
import net.deepthought.data.model.Entry;
import net.deepthought.data.model.listener.AllEntitiesListener;
import net.deepthought.data.model.listener.EntityListener;
import net.deepthought.data.persistence.db.BaseEntity;
import net.deepthought.data.search.SearchCompletedListener;
import net.deepthought.data.search.specific.FilterEntriesSearch;
import net.deepthought.util.Notification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by ganymed on 01/10/14.
 */
public class EntriesAdapter extends BaseAdapter {

  protected DeepThought deepThought;
  protected Activity context;

  protected FilterEntriesSearch filterEntriesSearch = null;

  protected List<Entry> searchResults = null;


  public EntriesAdapter(Activity context) {
    this.context = context;

    Application.addApplicationListener(applicationListener);

    if(Application.getDeepThought() != null)
      deepThoughtChanged(Application.getDeepThought());
  }

  protected ApplicationListener applicationListener = new ApplicationListener() {
    @Override
    public void deepThoughtChanged(DeepThought deepThought) {
      EntriesAdapter.this.deepThoughtChanged(deepThought);
    }

    @Override
    public void notification(Notification notification) {

    }
  };

  protected void deepThoughtChanged(DeepThought deepThought) {
    Application.getDataManager().removeAllEntitiesListener(allEntitiesListener);

    this.deepThought = deepThought;

    Application.getDataManager().addAllEntitiesListener(allEntitiesListener);

    notifyDataSetChangedThreadSafe();
  }


  @Override
  public int getCount() {
    if(searchResults != null)
      return searchResults.size();

    if(deepThought != null)
      return deepThought.countEntries();
    return 0;
  }

  public Entry getEntryAt(int position) {
    if(searchResults != null) {
      return searchResults.get(position);
    }

    return deepThought.entryAt(position);
  }

  @Override
  public Object getItem(int position) {
    return getEntryAt(position);
  }

  @Override
  public long getItemId(int position) {
    return position;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    if(convertView == null)
      convertView = context.getLayoutInflater().inflate(R.layout.list_item_entry, parent, false);

    Entry entry = getEntryAt(position);

    TextView txtvwPreview = (TextView)convertView.findViewById(R.id.txtvwListItemEntryPreview);
    txtvwPreview.setText(entry.getPreview());

    TextView txtvwTags = (TextView)convertView.findViewById(R.id.txtvwListItemEntryTags);
    txtvwTags.setText(entry.getTagsPreview());

    return convertView;
  }


  public void searchEntries(String searchTerm) {
    if(filterEntriesSearch != null && filterEntriesSearch.isCompleted() == false)
      filterEntriesSearch.interrupt();

    // TODO: enable filtering Abstract or Content only (currently both set to true)
    filterEntriesSearch = new FilterEntriesSearch(searchTerm, true, true, new SearchCompletedListener<Collection<Entry>>() {
      @Override
      public void completed(Collection<Entry> results) {
        if(results instanceof List)
          searchResults = (List<Entry>)results;
        else
          searchResults = new ArrayList<>(results);

        notifyDataSetChangedThreadSafe();
      }
    });

    Application.getSearchEngine().filterEntries(filterEntriesSearch);
  }

  public void showAllEntries() {
    searchResults = null;
    notifyDataSetChangedThreadSafe();
  }


  public void notifyDataSetChangedThreadSafe() {
    context.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        notifyDataSetChanged();
      }
    });
  }


  protected AllEntitiesListener allEntitiesListener = new AllEntitiesListener() {
    @Override
    public void entityCreated(BaseEntity entity) {
      checkIfRelevantEntityHasChanged(entity);
    }

    @Override
    public void entityUpdated(BaseEntity entity, String propertyName, Object previousValue, Object newValue) {
      checkIfRelevantEntityHasChanged(entity);
    }

    @Override
    public void entityDeleted(BaseEntity entity) {
      checkIfRelevantEntityHasChanged(entity);
    }

    @Override
    public void entityAddedToCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity addedEntity) {
      checkIfRelevantEntityHasChanged(collectionHolder);
    }

    @Override
    public void entityRemovedFromCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity removedEntity) {
      checkIfRelevantEntityHasChanged(collectionHolder);
    }
  };

  protected void checkIfRelevantEntityHasChanged(BaseEntity entity) {
    if(entity instanceof Entry /*|| entity instanceof Tag*/)
      notifyDataSetChangedThreadSafe();
  }

  protected EntityListener deepThoughtListener = new EntityListener() {
    @Override
    public void propertyChanged(BaseEntity entity, String propertyName, Object previousValue, Object newValue) {
      // TODO: how to get when an Entry has been updated?
    }

    @Override
    public void entityAddedToCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity addedEntity) {
      if(collection == deepThought.getEntries())
        notifyDataSetChangedThreadSafe();
    }

    @Override
    public void entityOfCollectionUpdated(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity updatedEntity) {
      if(collection == deepThought.getEntries())
        notifyDataSetChangedThreadSafe();
    }

    @Override
    public void entityRemovedFromCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity removedEntity) {
      if(collection == deepThought.getEntries() || collection == deepThought.getTags())
        notifyDataSetChangedThreadSafe();
    }
  };

  public void cleanUp() {
    Application.getDataManager().removeAllEntitiesListener(allEntitiesListener);

    Application.removeApplicationListener(applicationListener);
  }
}
