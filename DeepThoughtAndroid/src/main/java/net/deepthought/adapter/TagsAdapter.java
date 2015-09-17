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
import net.deepthought.data.model.listener.AllEntitiesListener;
import net.deepthought.data.persistence.db.BaseEntity;
import net.deepthought.data.search.SearchCompletedListener;
import net.deepthought.data.search.specific.FilterTagsSearch;
import net.deepthought.data.search.specific.FilterTagsSearchResults;
import net.deepthought.data.search.specific.FindAllEntriesHavingTheseTagsResult;
import net.deepthought.util.Notification;
import net.deepthought.util.NotificationType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by ganymed on 01/10/14.
 */
public class TagsAdapter extends BaseAdapter {


  protected DeepThought deepThought;
  protected Activity context;

  protected FilterTagsSearch tagsSearch = null;
  protected String lastSearchTerm = FilterTagsSearch.EmptySearchTerm;

  protected List<Tag> searchResults = new ArrayList<>();

  protected List<Tag> tagsFilter = new ArrayList<>();
  protected FindAllEntriesHavingTheseTagsResult lastFilterTagsResult = null;


  public TagsAdapter(Activity context) {
    this.context = context;

    Application.addApplicationListener(applicationListener);

    if(Application.getDataManager() != null)
      Application.getDataManager().addAllEntitiesListener(allEntitiesListener);
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
      if(notification.getType() == NotificationType.ApplicationInstantiated)
        Application.getDataManager().addAllEntitiesListener(allEntitiesListener);
    }
  };

  protected void deepThoughtChanged(DeepThought deepThought) {
    this.deepThought = deepThought;

    searchForAllTags();
  }


  @Override
  public int getCount() {
    if(lastFilterTagsResult != null)
      return lastFilterTagsResult.getTagsOnEntriesContainingFilteredTagsCount();
    return searchResults.size();
  }

  public Tag getTagAt(int position) {
    if(lastFilterTagsResult != null)
      return lastFilterTagsResult.getTagsOnEntriesContainingFilteredTagsAt(position);
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
    if(tag.hasEntries())
      txtvwListItemTagName.setText(txtvwListItemTagName.getText() + "  ≻");

    ImageView imgvwFilter = (ImageView)convertView.findViewById(R.id.imgvwFilter);
    imgvwFilter.setTag(tag);
    setFilterIconDependingOnTagState(tag, imgvwFilter);
    imgvwFilter.setOnClickListener(imgvwFilterOnClickListener);

    return convertView;
  }

  private void setFilterIconDependingOnTagState(Tag tag, ImageView imgvwFilter) {
    if(tagsFilter.contains(tag))
      imgvwFilter.setImageResource(R.drawable.filter);
    else
      imgvwFilter.setImageResource(R.drawable.filter_disabled);
  }


  public void searchForAllTags() {
    searchTags(FilterTagsSearch.EmptySearchTerm);
  }

  public void researchTagsWithLastSearchTerm() {
    searchTags(lastSearchTerm);
  }

  public void searchTags(String searchTerm) {
    this.lastSearchTerm = searchTerm;

    if(isTagsFilterApplied())
      filterTags();
    else {
      if (tagsSearch != null && tagsSearch.isCompleted() == false)
        tagsSearch.interrupt();
      lastFilterTagsResult = null;

      tagsSearch = new FilterTagsSearch(searchTerm, new SearchCompletedListener<FilterTagsSearchResults>() {
        @Override
        public void completed(FilterTagsSearchResults results) {
          if (results.getAllMatchesSorted() instanceof List)
            searchResults = (List<Tag>) results.getAllMatchesSorted();
          else
            searchResults = new ArrayList<>(results.getAllMatchesSorted()); // TODO: use lazy loading list

          notifyDataSetChangedThreadSafe();
        }
      });

      Application.getSearchEngine().filterTags(tagsSearch);
    }
  }

  protected boolean isTagsFilterApplied() {
    return tagsFilter.size() > 0;
  }

  protected void filterTags() {
    if(isTagsFilterApplied() == false)
      researchTagsWithLastSearchTerm();
    else {
      Application.getSearchEngine().findAllEntriesHavingTheseTags(tagsFilter, lastSearchTerm, new SearchCompletedListener<FindAllEntriesHavingTheseTagsResult>() {
        @Override
        public void completed(FindAllEntriesHavingTheseTagsResult results) {
          lastFilterTagsResult = results;
          notifyDataSetChangedThreadSafe();
        }
      });
    }
  }

  public void removeSearchTerm() {
    searchForAllTags();
  }

  protected void toggleFilterTag(Tag tag) {
    if(tagsFilter.contains(tag))
      tagsFilter.remove(tag);
    else
      tagsFilter.add(tag);

    filterTags();
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
//      if(tag.hasEntries())
        toggleFilterTag(tag);
    }
  };


  protected AllEntitiesListener allEntitiesListener = new AllEntitiesListener() {
    @Override
    public void entityCreated(BaseEntity entity) {
      checkIfATagHasChanged(entity, true);
    }

    @Override
    public void entityUpdated(BaseEntity entity, String propertyName, Object previousValue, Object newValue) {
      checkIfATagHasChanged(entity, true);
    }

    @Override
    public void entityDeleted(BaseEntity entity) {
      checkIfATagHasChanged(entity, true);
    }

    @Override
    public void entityAddedToCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity addedEntity) {
      checkIfATagHasChanged(collectionHolder);
    }

    @Override
    public void entityRemovedFromCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity removedEntity) {
      checkIfATagHasChanged(collectionHolder);
    }
  };

  protected void checkIfATagHasChanged(BaseEntity entity) {
    checkIfATagHasChanged(entity, false);
  }

  protected void checkIfATagHasChanged(BaseEntity entity, boolean redoSearch) {
    if(entity instanceof Tag) {
      if(redoSearch)
        searchForAllTags();
      else
        notifyDataSetChangedThreadSafe();
    }
  }

  public void cleanUp() {
    Application.getDataManager().removeAllEntitiesListener(allEntitiesListener);

    Application.removeApplicationListener(applicationListener);
  }
}
