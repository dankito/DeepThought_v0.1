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
import net.deepthought.data.listener.ApplicationListener;
import net.deepthought.data.model.DeepThought;
import net.deepthought.data.model.Tag;
import net.deepthought.data.model.listener.EntityListener;
import net.deepthought.data.persistence.db.BaseEntity;
import net.deepthought.filter.TagsFilter;
import net.deepthought.util.Notification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by ganymed on 12/10/14.
 */
public class SearchForTagsAdapter extends BaseAdapter implements Filterable, TagsFilter.TagsFilterListener {

  public interface TagsToSearchForChangedListener {
    public void tagsToSearchForChanged(List<Tag> tagsToSearchFor);
  }

  protected Activity context;
  protected List<Tag> filteredTags;

  protected TagsFilter tagsFilter = null;

  protected List<Tag> tagsToSearchFor = null;

  protected DeepThought deepThought = null;

  protected TagsToSearchForChangedListener tagsToSearchForChangedListener;


  public SearchForTagsAdapter(Activity context, List<Tag> tagsToSearchFor) {
    this.context = context;
    this.tagsToSearchFor = tagsToSearchFor;

    tagsFilter = new TagsFilter(this);
    Application.addApplicationListener(applicationListener);

    deepThought = Application.getDeepThought();

    if(deepThought != null) {
      deepThought.addEntityListener(deepThoughtListener);

      filteredTags = new ArrayList<>(deepThought.getSortedTags());
    }
    else {
      filteredTags = new ArrayList<>();
    }
  }

  public void cleanUp() {
    if(deepThought != null)
      deepThought.removeEntityListener(deepThoughtListener);

    Application.removeApplicationListener(applicationListener);
  }

  protected ApplicationListener applicationListener = new ApplicationListener() {
    @Override
    public void deepThoughtChanged(DeepThought deepThought) {
      if (SearchForTagsAdapter.this.deepThought != null) {
        SearchForTagsAdapter.this.deepThought.removeEntityListener(deepThoughtListener);
      }

      SearchForTagsAdapter.this.deepThought = deepThought;
      tagsFilter.reapplyFilter();

      if (SearchForTagsAdapter.this.deepThought != null) {
        SearchForTagsAdapter.this.deepThought.addEntityListener(deepThoughtListener);
      }

      notifyDataSetChangedThreadSafe();
    }

    @Override
    public void notification(Notification notification) {

    }
  };

  public SearchForTagsAdapter(Activity context, List<Tag> tagsToSearchFor, TagsToSearchForChangedListener listener) {
    this(context, tagsToSearchFor);
    this.tagsToSearchForChangedListener = listener;
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
      convertView = context.getLayoutInflater().inflate(R.layout.list_item_search_for_tag, parent, false);

    Tag tag = getTagAt(position);

    CheckedTextView chktxtvwListItemEntryTag = (CheckedTextView)convertView.findViewById(R.id.chktxtvwListItemSearchForTag);
    chktxtvwListItemEntryTag.setText(tag.getName());
    chktxtvwListItemEntryTag.setChecked(tagsToSearchFor.contains(tag));
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
        tagsToSearchFor.add(tag);
      else
        tagsToSearchFor.remove(tag);

      Collections.sort(tagsToSearchFor);

      if(tagsToSearchForChangedListener != null)
        tagsToSearchForChangedListener.tagsToSearchForChanged(tagsToSearchFor);
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

//  @Override
//  public void entryAdded(Entry entry) {
//
//  }
//
//  @Override
//  public void entryUpdated(Entry entry) {
//
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
