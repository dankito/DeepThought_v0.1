package net.dankito.deepthought.adapter;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;

import net.dankito.deepthought.Application;
import net.dankito.deepthought.R;
import net.dankito.deepthought.data.model.DeepThought;
import net.dankito.deepthought.data.model.Entry;
import net.dankito.deepthought.data.persistence.db.BaseEntity;
import net.dankito.deepthought.data.search.SearchCompletedListener;
import net.dankito.deepthought.data.search.specific.EntriesSearch;
import net.dankito.deepthought.data.search.ui.EntriesForTag;
import net.dankito.deepthought.data.search.ui.EntriesForTagRetrievedListener;
import net.dankito.deepthought.ui.model.IEntityPreviewService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by ganymed on 01/10/14.
 */
public class EntriesAdapter extends AsyncLoadingEntityAdapter {

  protected EntriesForTag entriesForTag = null;

  protected List<Entry> entriesToShow = null;

  protected DeepThought deepThought;

  protected EntriesSearch entriesSearch = null;

  protected List<Entry> searchResults = null;

  protected String lastSearchTerm = null;

  protected IEntityPreviewService previewService = Application.getEntityPreviewService();


  public EntriesAdapter(Activity context, EntriesForTag entriesForTag) {
    super(context, R.layout.list_item_entry);

    this.entriesForTag = entriesForTag != null ? entriesForTag : new EntriesForTag();
    this.entriesForTag.addEntriesForTagRetrievedListener(entriesForTagRetrievedListener);

    if(Application.getDeepThought() != null) {
      initializeInstancesForChangedDeepThought(Application.getDeepThought());
    }

    retrievedEntriesToShow(this.entriesForTag.getEntriesForTag());
  }

  @Override
  public void cleanUp() {
    entriesForTag.removeEntriesForTagRetrievedListener(entriesForTagRetrievedListener);
    entriesForTag = null;

    super.cleanUp();
  }

  @Override
  protected void deepThoughtChanged(DeepThought deepThought) {
    initializeInstancesForChangedDeepThought(deepThought);

    if(deepThought != null) {
      entriesForTag.setTag(deepThought.AllEntriesSystemTag());
    }
    else {
      context.runOnUiThread(new Runnable() {
        @Override
        public void run() {
          entriesToShow = new ArrayList<Entry>(); // or set to null?
          searchResults = null;
          notifyDataSetChanged();
        }
      });
    }
  }

  protected void initializeInstancesForChangedDeepThought(DeepThought deepThought) {
    this.deepThought = deepThought;

    previewService = Application.getEntityPreviewService();
  }

  protected void retrievedEntriesToShow(Collection<Entry> entriesToShow) {
    this.entriesToShow = getListFromCollection(entriesToShow);

    notifyDataSetChanged();
  }


  @Override
  public int getCount() {
    if(searchResults != null) {
      return searchResults.size();
    }

    if(entriesToShow != null) {
      return entriesToShow.size();
    }

    return 0;
  }

  public Entry getEntryAt(int position) {
    if(searchResults != null) {
      return searchResults.get(position);
    }

    return entriesToShow.get(position);
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
  protected void customizeViewForItem(View listItemView, Object item) {
    Entry entry = (Entry)item;

    TextView txtvwListItemEntryReferencePreview = (TextView)listItemView.findViewById(R.id.txtvwListItemEntryReferencePreview);
    txtvwListItemEntryReferencePreview.setText(previewService.getReferenceOrPersonsPreview(entry));
    txtvwListItemEntryReferencePreview.setVisibility(entry.hasPersonsOrIsAReferenceSet() ? View.VISIBLE : View.GONE);

    TextView txtvwPreview = (TextView)listItemView.findViewById(R.id.txtvwListItemEntryPreview);
    txtvwPreview.setText(entry.getLongPreview());

    int txtvwPreviewLines = entry.hasPersonsOrIsAReferenceSet() ? 3 : 4;

    TextView txtvwTags = (TextView)listItemView.findViewById(R.id.txtvwListItemEntryTags);
    txtvwTags.setText(previewService.getTagsPreview(entry));

    txtvwTags.setVisibility(entry.hasTags() ? View.VISIBLE : View.GONE);
    if(entry.hasTags() == false) {
      txtvwPreviewLines++;
    }

    txtvwPreview.setLines(txtvwPreviewLines);
  }

  @Override
  protected void showPlaceholderView(View listItemView) {
    TextView txtvwListItemEntryReferencePreview = (TextView)listItemView.findViewById(R.id.txtvwListItemEntryReferencePreview);
    txtvwListItemEntryReferencePreview.setVisibility(View.GONE);

    TextView txtvwPreview = (TextView)listItemView.findViewById(R.id.txtvwListItemEntryPreview);
    txtvwPreview.setText("");

    TextView txtvwTags = (TextView)listItemView.findViewById(R.id.txtvwListItemEntryTags);

    txtvwTags.setVisibility(View.GONE);
  }


  public void showAllEntries() {
    searchEntries("");
  }

  public void searchEntries(String searchTerm) {
    if(Application.isInstantiated() == false) {
      return;
    }

    if(entriesSearch != null && entriesSearch.isCompleted() == false) {
      entriesSearch.interrupt();
    }

    lastSearchTerm = searchTerm;

    // TODO: enable filtering Abstract or Content only (currently both set to true)
    entriesSearch = new EntriesSearch(searchTerm, true, true, new SearchCompletedListener<Collection<Entry>>() {
      @Override
      public void completed(Collection<Entry> results) {
        updateSearchResultsThreadSafe(results);
      }
    });

    Application.getSearchEngine().searchEntries(entriesSearch);
  }

  protected void updateSearchResultsThreadSafe(final Collection<Entry> results) {
    context.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        updateSearchResults(results);
      }
    });
  }

  protected void updateSearchResults(Collection<Entry> results) {
    searchResults = getListFromCollection(results);

    notifyDataSetChanged();
  }

  @Override
  protected void checkIfRelevantEntityHasChanged(BaseEntity entity) {
    checkIfAnEntryHasChanged(entity);
  }

  protected void checkIfAnEntryHasChanged(BaseEntity entity) {
    if(entity instanceof Entry /*|| entity instanceof Tag*/) {
      if(lastSearchTerm != null) { // reapply last search to update Entries
        searchEntries(lastSearchTerm);
      }
      else {
        notifyDataSetChangedThreadSafe();
      }
    }
  }


  protected EntriesForTagRetrievedListener entriesForTagRetrievedListener = new EntriesForTagRetrievedListener() {
    @Override
    public void retrievedEntriesForTag(final List<Entry> entries) {
      if(context != null) {
        context.runOnUiThread(new Runnable() {
          @Override
          public void run() {
            retrievedEntriesToShow(entries);
          }
        });
      }
    }
  };

}
