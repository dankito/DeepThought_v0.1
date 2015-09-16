package net.deepthought.adapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import net.deepthought.Application;
import net.deepthought.R;
import net.deepthought.data.listener.ApplicationListener;
import net.deepthought.data.model.DeepThought;
import net.deepthought.data.model.Tag;
import net.deepthought.data.model.listener.EntityListener;
import net.deepthought.data.persistence.db.BaseEntity;
import net.deepthought.data.search.SearchCompletedListener;
import net.deepthought.data.search.specific.FilterTagsSearch;
import net.deepthought.data.search.specific.FilterTagsSearchResults;
import net.deepthought.util.Notification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by ganymed on 01/10/14.
 */
public class TagsAdapter extends BaseAdapter {

  protected DeepThought deepThought;
  protected Activity context;

  protected FilterTagsSearch filterTagsSearch = null;

  protected List<Tag> searchResults = new ArrayList<>();


  public TagsAdapter(Activity context) {
    this.context = context;

    Application.addApplicationListener(applicationListener);

    if(Application.getDeepThought() != null)
      deepThoughtChanged(Application.getDeepThought());
  }

  protected ApplicationListener applicationListener = new ApplicationListener() {
    @Override
    public void deepThoughtChanged(DeepThought deepThought) {
      TagsAdapter.this.deepThoughtChanged(deepThought);
    }

    @Override
    public void notification(Notification notification) {

    }
  };

  protected void deepThoughtChanged(DeepThought deepThought) {
    if (this.deepThought != null)
      this.deepThought.removeEntityListener(deepThoughtListener);

    this.deepThought = deepThought;

    if (this.deepThought != null)
      this.deepThought.addEntityListener(deepThoughtListener);

    searchTags();
  }


  @Override
  public int getCount() {
    return searchResults.size();
  }

  public Tag getTagAt(int position) {
    return searchResults.get(position);
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
      convertView = context.getLayoutInflater().inflate(R.layout.list_item_tag, parent, false);

    Tag tag = getTagAt(position);

    TextView txtvwListItemTagName = (TextView)convertView.findViewById(R.id.txtvwTagName);
    txtvwListItemTagName.setText(tag.getTextRepresentation());

    ImageView imgvwFilter = (ImageView)convertView.findViewById(R.id.imgvwFilter);
    imgvwFilter.setTag(tag);
    imgvwFilter.setOnClickListener(imgvwFilterOnClickListener);

    return convertView;
  }


  public void searchTags() {
    searchTags("");
  }

  public void searchTags(String searchTerm) {
    if(filterTagsSearch != null && filterTagsSearch.isCompleted() == false)
      filterTagsSearch.interrupt();

    filterTagsSearch = new FilterTagsSearch(searchTerm, new SearchCompletedListener<FilterTagsSearchResults>() {
      @Override
      public void completed(FilterTagsSearchResults results) {
        if(results.getRelevantMatches() instanceof List)
          searchResults = (List<Tag>)results.getRelevantMatches();
        else
          searchResults = new ArrayList<>(results.getRelevantMatches());

        notifyDataSetChangedThreadSafe();
      }
    });

    Application.getSearchEngine().filterTags(filterTagsSearch);
  }

  public void showAllTags() {
    searchTags();
  }

  protected void toggleFilterTag(Tag tag) {

  }


  public void notifyDataSetChangedThreadSafe() {
    context.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        notifyDataSetChanged();
      }
    });
  }


  protected View.OnClickListener imgvwFilterOnClickListener = new View.OnClickListener() {
    @Override
    public void onClick(View view) {
      Tag tag = (Tag)view.getTag();
      toggleFilterTag(tag);
    }
  };


  protected EntityListener deepThoughtListener = new EntityListener() {
    @Override
    public void propertyChanged(BaseEntity entity, String propertyName, Object previousValue, Object newValue) {
      // TODO: how to get when a Tag has been updated?
    }

    @Override
    public void entityAddedToCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity addedEntity) {
      if(collection == deepThought.getTags())
        searchTags();
    }

    @Override
    public void entityOfCollectionUpdated(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity updatedEntity) {
      if(collection == deepThought.getTags())
        searchTags();
    }

    @Override
    public void entityRemovedFromCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity removedEntity) {
      if(collection == deepThought.getTags())
        searchTags();
    }
  };

  public void cleanUp() {
    if(deepThought != null)
      deepThought.removeEntityListener(deepThoughtListener);

    Application.removeApplicationListener(applicationListener);
  }
}
