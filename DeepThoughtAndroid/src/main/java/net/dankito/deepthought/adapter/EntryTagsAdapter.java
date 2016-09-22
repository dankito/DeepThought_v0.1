package net.dankito.deepthought.adapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;

import net.dankito.deepthought.Application;
import net.dankito.deepthought.R;
import net.dankito.deepthought.data.model.DeepThought;
import net.dankito.deepthought.data.model.Entry;
import net.dankito.deepthought.data.model.Tag;
import net.dankito.deepthought.data.model.listener.EntityListener;
import net.dankito.deepthought.data.persistence.db.BaseEntity;
import net.dankito.deepthought.data.search.ui.TagsSearchResultListener;
import net.dankito.deepthought.data.search.ui.TagsSearcher;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by ganymed on 12/10/14.
 */
public class EntryTagsAdapter extends BaseAdapter {

  public interface EntryTagsChangedListener {
    void entryTagsChanged(List<Tag> entryTags);
  }

  protected Activity context;
  protected Entry entry;
  protected List<Tag> entryTags;

  protected List<Tag> searchTagsResult;

  protected TagsSearcher tagsSearcher;

  protected EntryTagsChangedListener entryTagsChangedListener = null;


  public EntryTagsAdapter(Activity context, Entry entry, List<Tag> entryTags) {
    this.context = context;
    this.entry = entry;
    this.entryTags = entryTags;

    tagsSearcher = new TagsSearcher(Application.getSearchEngine());
    searchTagsResult = new ArrayList<>();

    DeepThought deepThought = Application.getDeepThought();

    deepThought.addEntityListener(deepThoughtListener);
    entry.addEntityListener(entryListener);

    searchTags("");
  }

  public EntryTagsAdapter(Activity context, Entry entry, List<Tag> entryTags, EntryTagsChangedListener entryTagsChangedListener) {
    this(context, entry, entryTags);
    this.entryTagsChangedListener = entryTagsChangedListener;
  }

  public void cleanUp() {
    entry.removeEntityListener(entryListener);

    if(Application.getDeepThought() != null)
      Application.getDeepThought().removeEntityListener(deepThoughtListener);
  }

  @Override
  public int getCount() {
    return searchTagsResult.size();
  }

  public Tag getTagAt(int position) {
    return searchTagsResult.get(position);
  }

  @Override
  public Object getItem(int position) {
    return getTagAt(position);
  }

  @Override
  public long getItemId(int position) {
    return position;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    if(convertView == null) {
      convertView = context.getLayoutInflater().inflate(R.layout.list_item_entry_tag, parent, false);
    }

    Tag tag = getTagAt(position);

    CheckedTextView chktxtvwListItemEntryTag = (CheckedTextView)convertView.findViewById(R.id.chktxtvwListItemEntryTag);
    chktxtvwListItemEntryTag.setText(tag.getName());
    chktxtvwListItemEntryTag.setChecked(entryTags.contains(tag));
    chktxtvwListItemEntryTag.setOnClickListener(chktxtvwListItemEntryTagOnClickListener);
    chktxtvwListItemEntryTag.setTag(tag);

    return convertView;
  }


  View.OnClickListener chktxtvwListItemEntryTagOnClickListener = new View.OnClickListener() {
    @Override
    public void onClick(View view) {
      CheckedTextView chktxtvwListItemEntryTag = (CheckedTextView)view;
      Tag tag = (Tag)chktxtvwListItemEntryTag.getTag();

      chktxtvwListItemEntryTag.toggle();

      if(chktxtvwListItemEntryTag.isChecked())
        entryTags.add(tag);
      else
        entryTags.remove(tag);

      Collections.sort(entryTags);

      if(entryTagsChangedListener != null)
        entryTagsChangedListener.entryTagsChanged(entryTags);
    }
  };


  public void notifyDataSetChangedThreadSafe() {
    context.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        notifyDataSetChanged();
      }
    });
  }


  public void searchTags(String searchTerm) {
    tagsSearcher.search(searchTerm, searchResultListener);
  }

  protected TagsSearchResultListener searchResultListener = new TagsSearchResultListener() {
    @Override
    public void completed(List<Tag> searchResult) {
      searchTagsResult = searchResult;

      notifyDataSetChangedThreadSafe();
    }
  };


  protected EntityListener deepThoughtListener = new EntityListener() {
    @Override
    public void propertyChanged(BaseEntity entity, String propertyName, Object previousValue, Object newValue) {

    }

    @Override
    public void entityAddedToCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity addedEntity) {
      collectionUpdated(collectionHolder, addedEntity);
    }

    @Override
    public void entityOfCollectionUpdated(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity updatedEntity) {
      collectionUpdated(collectionHolder, updatedEntity);
    }

    @Override
    public void entityRemovedFromCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity removedEntity) {
      collectionUpdated(collectionHolder, removedEntity);
    }
  };

  protected void collectionUpdated(BaseEntity collectionHolder, BaseEntity changedEntity) {
    if(changedEntity instanceof Tag) {
      tagsSearcher.researchTagsWithLastSearchTerm(searchResultListener);
    }
  }


  protected EntityListener entryListener = new EntityListener() {
    @Override
    public void propertyChanged(BaseEntity entity, String propertyName, Object previousValue, Object newValue) {
      notifyDataSetChangedThreadSafe();
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
