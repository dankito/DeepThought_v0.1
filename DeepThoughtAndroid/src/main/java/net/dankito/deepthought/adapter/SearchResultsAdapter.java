package net.dankito.deepthought.adapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import net.dankito.deepthought.Application;
import net.dankito.deepthought.R;
import net.dankito.deepthought.data.model.Entry;
import net.dankito.deepthought.ui.model.IEntityPreviewService;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ganymed on 13/10/14.
 */
public class SearchResultsAdapter extends BaseAdapter {

  protected List<Entry> searchResults = new ArrayList<>();
  protected Activity context;

  protected IEntityPreviewService previewService = Application.getEntityPreviewService();


  public SearchResultsAdapter(Activity context) {
    this.context = context;
  }


  @Override
  public int getCount() {
    return searchResults.size();
  }

  public Entry getSearchResultAt(int position) {
    return searchResults.get(position);
  }

  @Override
  public Object getItem(int position) {
    return getSearchResultAt(position);
  }

  @Override
  public long getItemId(int position) {
    return position;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    if(convertView == null)
//      convertView = context.getLayoutInflater().inflate(R.layout.list_item_search_result, parent, false);
      convertView = context.getLayoutInflater().inflate(R.layout.list_item_entry, parent, false);

    Entry entry = getSearchResultAt(position);

    TextView txtvwPreview = (TextView)convertView.findViewById(R.id.txtvwListItemEntryPreview);
    txtvwPreview.setText(entry.getPreview());

    TextView txtvwTags = (TextView)convertView.findViewById(R.id.txtvwListItemEntryTags);
    txtvwTags.setText(previewService.getTagsPreview(entry));

    return convertView;
  }


  protected void notifyDataSetChangedThreadSafe() {
    context.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        notifyDataSetChanged();
      }
    });
  }

  public void setSearchResults(List<Entry> searchResults) {
    this.searchResults = searchResults;
    notifyDataSetChangedThreadSafe();
  }
}
