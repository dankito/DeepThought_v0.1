package net.deepthought.adapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.Filter;
import android.widget.Filterable;

import net.deepthought.Application;
import net.deepthought.R;
import net.deepthought.data.model.DeepThought;
import net.deepthought.data.model.Entry;
import net.deepthought.data.model.Tag;
import net.deepthought.data.model.listener.EntityListener;
import net.deepthought.data.persistence.db.BaseEntity;
import net.deepthought.filter.TagsFilter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by ganymed on 12/10/14.
 */
public class EntryTagsAdapter extends BaseAdapter implements Filterable, TagsFilter.TagsFilterListener {

  public interface EntryTagsChangedListener {
    public void entryTagsChanged(List<Tag> entryTags);
  }

  protected Activity context;
  protected Entry entry;
  protected List<Tag> entryTags;
  protected List<Tag> filteredTags;

  protected TagsFilter tagsFilter = null;

  protected EntryTagsChangedListener entryTagsChangedListener = null;


  public EntryTagsAdapter(Activity context, Entry entry, List<Tag> entryTags) {
    this.context = context;
    this.entry = entry;
    this.entryTags = entryTags;

    tagsFilter = new TagsFilter(this);

    DeepThought deepThought = Application.getDeepThought();
    filteredTags = new ArrayList<>(deepThought.getTags());
    Collections.sort(filteredTags);
//    deepThought.addTagsChangedListener(this);
    deepThought.addEntityListener(deepThoughtListener);
//    deepThought.addEntriesChangedListener(this);
    entry.addEntityListener(entryListener);
  }

  public EntryTagsAdapter(Activity context, Entry entry, List<Tag> entryTags, EntryTagsChangedListener entryTagsChangedListener) {
    this(context, entry, entryTags);
    this.entryTagsChangedListener = entryTagsChangedListener;
  }

  @Override
  public int getCount() {
    return filteredTags.size();
  }

  public Tag getTagAt(int position) {
    return filteredTags.get(position);
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
    if(convertView == null)
      convertView = context.getLayoutInflater().inflate(R.layout.list_item_entry_tag, parent, false);

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


  @Override
  public Filter getFilter() {
    return tagsFilter;
  }


  protected EntityListener deepThoughtListener = new EntityListener() {
    @Override
    public void propertyChanged(BaseEntity entity, String propertyName, Object previousValue, Object newValue) {

    }

    @Override
    public void entityAddedToCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity addedEntity) {
      if(collection == Application.getDeepThought().getTags()) {
        tagsFilter.reapplyFilter();
        notifyDataSetChangedThreadSafe();
      }
    }

    @Override
    public void entityOfCollectionUpdated(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity updatedEntity) {
      if(collection == Application.getDeepThought().getTags()) {
        tagsFilter.reapplyFilter();
        notifyDataSetChangedThreadSafe();
      }
    }

    @Override
    public void entityRemovedFromCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity removedEntity) {
      if(collection == Application.getDeepThought().getTags()) {
        tagsFilter.reapplyFilter();
        notifyDataSetChangedThreadSafe();
      }
    }
  };

//  @Override
//  public void tagAdded(Tag tag) {
//    tagsFilter.reapplyFilter();
//    notifyDataSetChangedThreadSafe();
//  }
//
//  @Override
//  public void tagUpdated(Tag tag) {
//    tagsFilter.reapplyFilter();
//    notifyDataSetChangedThreadSafe();
//  }
//
//  @Override
//  public void tagRemoved(Tag tag) {
//    tagsFilter.reapplyFilter();
//    notifyDataSetChangedThreadSafe();
//  }


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

//  @Override
//  public void entryAdded(Entry entry) {
//
//  }
//
//  @Override
//  public void entryUpdated(Entry entry) {
//    if(this.entry.equals(this.entry))
//      notifyDataSetChangedThreadSafe();
//  }
//
//  @Override
//  public void entryRemoved(Entry entry) {
//
//  }

  @Override
  public void publishResults(List<Tag> filteredTags) {
    this.filteredTags = filteredTags;

    if (filteredTags.size() > 0) {
      notifyDataSetChanged();
    }
    else {
      notifyDataSetInvalidated();
    }
  }

}
