package net.dankito.deepthought.adapter;

import android.app.Activity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import net.dankito.deepthought.Application;
import net.dankito.deepthought.R;
import net.dankito.deepthought.data.model.DeepThought;
import net.dankito.deepthought.data.model.Tag;
import net.dankito.deepthought.data.persistence.db.BaseEntity;
import net.dankito.deepthought.data.search.SearchBase;
import net.dankito.deepthought.data.search.SearchCompletedListener;
import net.dankito.deepthought.data.search.specific.FindAllEntriesHavingTheseTagsResult;
import net.dankito.deepthought.data.search.specific.TagsSearch;
import net.dankito.deepthought.data.search.specific.TagsSearchResults;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ganymed on 01/10/14.
 */
public class TagsAdapter extends AsyncLoadingEntityAdapter {


  protected DeepThought deepThought;

  protected TagsSearch tagsSearch = null;
  protected String lastSearchTerm = SearchBase.EmptySearchTerm;

  protected List<Tag> searchResults = new ArrayList<>();

  protected List<Tag> tagsFilter = new ArrayList<>();
  protected FindAllEntriesHavingTheseTagsResult lastFilterTagsResult = null;


  public TagsAdapter(Activity context) {
    super(context, R.layout.list_item_tag);
  }

  @Override
  protected void deepThoughtChanged(DeepThought deepThought) {
    this.deepThought = deepThought;

    if(Application.isInstantiated()) {
      showAllTags();
    }
  }

  @Override
  protected void applicationInstantiated() {
    super.applicationInstantiated();

    showAllTags();
  }


  @Override
  public int getCount() {
    if(lastFilterTagsResult != null) {
      return lastFilterTagsResult.getTagsOnEntriesContainingFilteredTagsCount();
    }

    return searchResults.size();
  }

  public Tag getTagAt(int position) {
    if(lastFilterTagsResult != null) {
      return lastFilterTagsResult.getTagsOnEntriesContainingFilteredTagsAt(position);
    }

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
  protected void customizeViewForItem(View listItemView, Object item) {
    Tag tag = (Tag)item;

    TextView txtvwListItemTagName = (TextView)listItemView.findViewById(R.id.txtvwTagName);
    txtvwListItemTagName.setText(tag.getTextRepresentation());
    if(tag.hasEntries())
      txtvwListItemTagName.setText(txtvwListItemTagName.getText() + "  ≻");

    ImageView imgvwFilter = (ImageView)listItemView.findViewById(R.id.imgvwFilter);
    imgvwFilter.setTag(tag);
    setFilterIconDependingOnTagState(tag, imgvwFilter);
    imgvwFilter.setOnClickListener(imgvwFilterOnClickListener);
  }

  @Override
  protected void showPlaceholderView(View listItemView) {
    TextView txtvwListItemTagName = (TextView)listItemView.findViewById(R.id.txtvwTagName);
    txtvwListItemTagName.setText("");

    ImageView imgvwFilter = (ImageView)listItemView.findViewById(R.id.imgvwFilter);
    imgvwFilter.setOnClickListener(null);
    setImgvwFilterToDisabledState(imgvwFilter);
  }

  private void setFilterIconDependingOnTagState(Tag tag, ImageView imgvwFilter) {
    if(tagsFilter.contains(tag)) {
      imgvwFilter.setImageResource(R.drawable.filter);
    }
    else {
      setImgvwFilterToDisabledState(imgvwFilter);
    }
  }

  protected void setImgvwFilterToDisabledState(ImageView imgvwFilter) {
    imgvwFilter.setImageResource(R.drawable.filter_disabled);
  }


  public void showAllTags() {
    searchTags(SearchBase.EmptySearchTerm);
  }

  public void researchTagsWithLastSearchTerm() {
    searchTags(lastSearchTerm);
  }

  public void searchTags(String searchTerm) {
    this.lastSearchTerm = searchTerm;

    if(isTagsFilterApplied()) {
      filterTags();
    }
    else {
      if(tagsSearch != null && tagsSearch.isCompleted() == false) {
        tagsSearch.interrupt();
      }
      lastFilterTagsResult = null;

      tagsSearch = new TagsSearch(searchTerm, new SearchCompletedListener<TagsSearchResults>() {
        @Override
        public void completed(TagsSearchResults results) {
          updateSearchResultsThreadSafe(results);
        }
      });

      Application.getSearchEngine().searchTags(tagsSearch);
    }
  }

  protected void updateSearchResultsThreadSafe(final TagsSearchResults results) {
    context.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        updateSearchResults(results);
      }
    });
  }

  protected void updateSearchResults(TagsSearchResults results) {
    if(results.getRelevantMatchesSorted() instanceof List) {
      searchResults = (List<Tag>)results.getRelevantMatchesSorted();
    }
    else {
      searchResults = new ArrayList<>(results.getRelevantMatchesSorted()); // TODO: use lazy loading list
    }

    notifyDataSetChanged();
  }

  protected boolean isTagsFilterApplied() {
    return tagsFilter.size() > 0;
  }

  protected void filterTags() {
    if(isTagsFilterApplied() == false) {
      researchTagsWithLastSearchTerm();
    }
    else {
      Application.getSearchEngine().findAllEntriesHavingTheseTags(tagsFilter, lastSearchTerm, new SearchCompletedListener<FindAllEntriesHavingTheseTagsResult>() {
        @Override
        public void completed(final FindAllEntriesHavingTheseTagsResult results) {
          context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
              lastFilterTagsResult = results;
              notifyDataSetChanged();
            }
          });
        }
      });
    }
  }

  protected void toggleFilterTag(Tag tag) {
    if(tagsFilter.contains(tag)) {
      tagsFilter.remove(tag);
    }
    else {
      tagsFilter.add(tag);
    }

    filterTags();
  }


  protected View.OnClickListener imgvwFilterOnClickListener = new View.OnClickListener() {
    @Override
    public void onClick(View view) {
      Tag tag = (Tag)view.getTag();
//      if(tag.hasEntries())
        toggleFilterTag(tag);
    }
  };


  @Override
  protected void checkIfRelevantEntityHasChanged(BaseEntity entity) {
    checkIfATagHasChanged(entity);
  }

  protected void checkIfATagHasChanged(BaseEntity entity) {
    if(entity instanceof Tag) {
      researchTagsWithLastSearchTerm();
    }
  }

}
