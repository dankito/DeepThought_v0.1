package net.dankito.deepthought.fragments;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SearchView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import net.dankito.deepthought.Application;
import net.dankito.deepthought.MainActivity;
import net.dankito.deepthought.R;
import net.dankito.deepthought.adapter.EntriesAdapter;
import net.dankito.deepthought.data.model.DeepThought;
import net.dankito.deepthought.data.model.Entry;

import java.util.Collection;

/**
 * Created by ganymed on 01/10/14.
 */
public class EntriesFragment extends TabFragment {


  protected Collection<Entry> entriesToShow = null;

  protected EntriesAdapter entriesAdapter;


  public EntriesFragment() {

  }

  public EntriesFragment(Collection<Entry> entriesToShow) {
    this.entriesToShow = entriesToShow;
  }


  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setHasOptionsMenu(true);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.fragment_entries, container, false);

    ListView lstvwEntries = (ListView)rootView.findViewById(R.id.lstvwEntries);
    entriesAdapter = new EntriesAdapter(getActivity(), entriesToShow);
    lstvwEntries.setAdapter(entriesAdapter);
    registerForContextMenu(lstvwEntries);
    lstvwEntries.setOnItemClickListener(lstvwEntriesOnItemClickListener);
    lstvwEntries.setOnScrollListener(lstvwEntriesOnScrollListener); // for hiding FloatingActionMenu when scrolling

    return rootView;
  }

  @Override
  public void onDestroyView() {
    if(entriesAdapter != null) {
      entriesAdapter.cleanUp();
    }

    super.onDestroyView();
  }

  protected AdapterView.OnItemClickListener lstvwEntriesOnItemClickListener = new AdapterView.OnItemClickListener() {
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
      Entry entry = (Entry)parent.getItemAtPosition(position);
      showEditEntryDialog(entry);
    }
  };

  // TODO: this is almost the same code as in TagsFragment -> merge
  // hide FloatingActionMenu while scrolling
  AbsListView.OnScrollListener lstvwEntriesOnScrollListener = new AbsListView.OnScrollListener() {
    @Override
    public void onScrollStateChanged(AbsListView absListView, int scrollState) {
      if(scrollState == SCROLL_STATE_TOUCH_SCROLL) { // scroll started
        ((MainActivity)getActivity()).hideFloatingActionMenu();
      }
      else if(scrollState == SCROLL_STATE_IDLE) { // scroll stopped
        ((MainActivity)getActivity()).showFloatingActionMenu();
      }
    }

    @Override
    public void onScroll(AbsListView absListView, int firstVisible, int visibleCount, int totalCount) { }
  };


  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    // TODO: this is almost the same code as in TagsFragment -> merge
    // Associate searchable configuration with the SearchView
    MenuItem searchItem = menu.findItem(R.id.search);
    SearchView searchView = (SearchView) searchItem.getActionView();
    if (searchView != null) {
      SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
      searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
      searchView.setQueryHint(getActivity().getString(R.string.search_hint_entries));
      searchView.setOnQueryTextListener(entriesQueryTextListener);
    }

    super.onCreateOptionsMenu(menu, inflater);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    return super.onOptionsItemSelected(item);
  }


  @Override
  public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
    super.onCreateContextMenu(menu, v, menuInfo);
    MenuInflater inflater = getActivity().getMenuInflater();
    inflater.inflate(R.menu.list_item_entry_context_menu, menu);
  }

  @Override
  public boolean onContextItemSelected(MenuItem item) {
    AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
    DeepThought deepThought = Application.getDeepThought();

    switch(item.getItemId()) {
      case R.id.list_item_entry_context_menu_edit:
        Entry entryToEdit = entriesAdapter.getEntryAt(info.position);
        showEditEntryDialog(entryToEdit);
        return true;
      case R.id.list_item_entry_context_menu_delete:
        Entry entryToDelete = entriesAdapter.getEntryAt(info.position);
        deepThought.removeEntry(entryToDelete);
        return true;
      default:
        return super.onContextItemSelected(item);
    }
  }

  protected void showEditEntryDialog(Entry entryToEdit) {
    ((MainActivity)getActivity()).showViewEntryDialog(entryToEdit);
  }


  protected SearchView.OnQueryTextListener entriesQueryTextListener = new SearchView.OnQueryTextListener() {
    @Override
    public boolean onQueryTextSubmit(String query) {
      return onQueryTextChange(query);
    }

    @Override
    public boolean onQueryTextChange(String query) {
      entriesAdapter.searchEntries(query);
      return true;
    }
  };


  @Override
  public void tabSelected() {
    if(entriesAdapter != null) {
      entriesAdapter.showAllEntries();
    }
  }
}
