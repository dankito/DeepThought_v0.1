package net.deepthought.fragments;

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
import android.widget.AdapterView;
import android.widget.ListView;

import net.deepthought.Application;
import net.deepthought.R;
import net.deepthought.activities.ActivityManager;
import net.deepthought.adapter.TagsAdapter;
import net.deepthought.data.model.DeepThought;
import net.deepthought.data.model.Tag;

/**
 * Created by ganymed on 01/10/14.
 */
public class TagsFragment extends Fragment {


  protected TagsAdapter tagsAdapter;

  protected boolean hasNavigatedToOtherFragment = false;


  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setHasOptionsMenu(true);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.fragment_tags, container, false);

    ListView lstvwTags = (ListView)rootView.findViewById(R.id.lstvwTags);
    tagsAdapter = new TagsAdapter(getActivity());
    lstvwTags.setAdapter(tagsAdapter);
    registerForContextMenu(lstvwTags);
    lstvwTags.setOnItemClickListener(lstvwTagsOnItemClickListener);

    return rootView;
  }

  @Override
  public void onDestroyView() {
    if(tagsAdapter != null)
      tagsAdapter.cleanUp();

    super.onDestroyView();
  }

  protected AdapterView.OnItemClickListener lstvwTagsOnItemClickListener = new AdapterView.OnItemClickListener() {
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
      Tag tag = (Tag)parent.getItemAtPosition(position);
      if(tag.hasEntries() == false) // no Entries to show -> don't navigate
        return;

      ActivityManager.getInstance().navigateToEntriesFragment(getActivity().getSupportFragmentManager(), tag.getEntries(), R.id.rlyFragmentTags);

      hasNavigatedToOtherFragment = true;
      getActivity().invalidateOptionsMenu();
    }
  };

  public void backButtonPressed() {
    if(hasNavigatedToOtherFragment) {
      hasNavigatedToOtherFragment = false;
      getActivity().invalidateOptionsMenu();
    }
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    // TODO: this is almost the same code as in EntriesFragment -> merge
    // Associate searchable configuration with the SearchView
    MenuItem searchItem = menu.findItem(R.id.search);
    SearchView searchView = (SearchView) searchItem.getActionView();
    if (searchView != null) {
      SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
      searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
      searchView.setQueryHint(getActivity().getString(R.string.search_hint_tags));
      searchView.setOnQueryTextListener(tagsQueryTextListener);
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
    inflater.inflate(R.menu.list_item_tag_context_menu, menu);
  }

  @Override
  public boolean onContextItemSelected(MenuItem item) {
    AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
    DeepThought deepThought = Application.getDeepThought();

    switch(item.getItemId()) {
      case R.id.list_item_tag_context_menu_edit:
        Tag tagToEdit = tagsAdapter.getTagAt(info.position);
        editTag(tagToEdit);
        return true;
      case R.id.list_item_tag_context_menu_delete:
        Tag tagToDelete = tagsAdapter.getTagAt(info.position);
        deepThought.removeTag(tagToDelete);
        return true;
      default:
        return super.onContextItemSelected(item);
    }
  }

  protected void editTag(Tag tagToEdit) {
    ActivityManager.getInstance().showEditTagAlert(getActivity(), tagToEdit);
  }


  protected SearchView.OnQueryTextListener tagsQueryTextListener = new SearchView.OnQueryTextListener() {
    @Override
    public boolean onQueryTextSubmit(String query) {
      return onQueryTextChange(query);
    }

    @Override
    public boolean onQueryTextChange(String query) {
      tagsAdapter.searchTags(query);
      return true;
    }
  };


  public boolean hasNavigatedToOtherFragment() {
    return hasNavigatedToOtherFragment;
  }
}
