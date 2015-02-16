package net.deepthought.adapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import net.deepthought.R;
import net.deepthought.data.model.Entry;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ganymed on 13/10/14.
 */
public class SearchResultsAdapter extends BaseAdapter {

  protected List<Entry> searchResults = new ArrayList<>();
  protected Activity context;


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
      convertView = context.getLayoutInflater().inflate(R.layout.list_item_search_result, parent, false);

    Entry entry = getSearchResultAt(position);

    TextView txtvwTitle = (TextView)convertView.findViewById(R.id.txtvwListItemSearchResultTitle);
    txtvwTitle.setText(entry.getTitle());

    TextView txtvwText = (TextView)convertView.findViewById(R.id.txtvwListItemSearchResultText);
    txtvwText.setText(entry.getContent());

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
