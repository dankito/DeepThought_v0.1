package net.deepthought.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;

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

  protected RelativeLayout rlySearchTags;

  protected EditText edtxtSearchTags;

  protected MenuItem mnitmSearchTags = null;

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

    rlySearchTags = (RelativeLayout)rootView.findViewById(R.id.rlySearchTags);

    edtxtSearchTags = (EditText)rootView.findViewById(R.id.edtxtSearchTags);
    edtxtSearchTags.addTextChangedListener(edtxtSearchTagsTextWatcher);

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
    inflater.inflate(R.menu.fragment_tags_options_menu, menu);

    mnitmSearchTags = menu.findItem(R.id.mnitmActionSearchTags);

    mnitmSearchTags.setVisible(!hasNavigatedToOtherFragment);

    super.onCreateOptionsMenu(menu, inflater);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();

    if(id == R.id.mnitmActionSearchTags) {
      toggleSearchBarVisibility();
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  protected void toggleSearchBarVisibility() {
    if(rlySearchTags.getVisibility() == View.GONE) {
      rlySearchTags.setVisibility(View.VISIBLE);
      edtxtSearchTags.requestFocus();
    }
    else {
      tagsAdapter.removeSearchTerm();
      rlySearchTags.setVisibility(View.GONE);
    }
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


  protected TextWatcher edtxtSearchTagsTextWatcher = new TextWatcher() {
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
      tagsAdapter.searchTags(s.toString());
    }

    @Override
    public void afterTextChanged(Editable s) {

    }
  };

  public boolean hasNavigatedToOtherFragment() {
    return hasNavigatedToOtherFragment;
  }
}
