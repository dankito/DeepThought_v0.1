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
import net.dankito.deepthought.ui.model.IEntityPreviewService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by ganymed on 01/10/14.
 */
public class EntriesAdapter extends AsyncLoadingEntityAdapter {

  protected List<Entry> entriesToShow = null;

  protected DeepThought deepThought;

  protected EntriesSearch entriesSearch = null;

  protected List<Entry> searchResults = null;

  protected String lastSearchTerm = null;

  protected IEntityPreviewService previewService = Application.getEntityPreviewService();


  public EntriesAdapter(Activity context, Collection<Entry> entriesToShow) {
    super(context, R.layout.list_item_entry);

    if(entriesToShow instanceof List) {
      this.entriesToShow = (List<Entry>) entriesToShow;
    }
    else if(entriesToShow != null) {
      this.entriesToShow = new ArrayList<>(entriesToShow); // TODO: use a lazy loading list
    }
  }

  @Override
  protected void deepThoughtChanged(DeepThought deepThought) {
    if(this.deepThought != null) {
      if(entriesToShow == this.deepThought.AllEntriesSystemTag().getEntries()) {
        entriesToShow = null;
      }
    }

    this.deepThought = deepThought;

    if(deepThought != null && entriesToShow == null) {
      if(deepThought.AllEntriesSystemTag().getEntries() instanceof List) {
        entriesToShow = (List<Entry>) deepThought.AllEntriesSystemTag().getEntries();
      }
      else {
        entriesToShow = new ArrayList<>(deepThought.AllEntriesSystemTag().getEntries()); // TODO: use a lazy loading list
      }
    }

    previewService = Application.getEntityPreviewService();

    notifyDataSetChangedThreadSafe();
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
        if(results instanceof List) {
          searchResults = (List<Entry>) results;
        }
        else {
          searchResults = new ArrayList<>(results);
        }

        notifyDataSetChangedThreadSafe();
      }
    });

    Application.getSearchEngine().searchEntries(entriesSearch);
  }

  @Override
  protected void checkIfRelevantEntityHasChanged(BaseEntity entity) {
    checkIfAnEntryHasChanged(entity);
  }

  @Override
  protected void checkIfRelevantEntityOfCollectionHasChanged(BaseEntity collectionHolder, BaseEntity changedEntity) {
    checkIfAnEntryHasChanged(collectionHolder);
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

}
