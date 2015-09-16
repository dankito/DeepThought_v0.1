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
import net.deepthought.adapter.EntriesAdapter;
import net.deepthought.data.model.DeepThought;
import net.deepthought.data.model.Entry;

import java.util.List;

/**
 * Created by ganymed on 01/10/14.
 */
public class EntriesFragment extends Fragment {


  protected List<Entry> entriesToShow = null;

  protected EntriesAdapter entriesAdapter;

  protected RelativeLayout rlySearchEntries;

  protected EditText edtxtSearchEntries;


  public EntriesFragment() {
//    this(new ArrayList<Entry>());
  }

//  public EntriesFragment(Collection<Entry> entriesToShow) {
//    if(entriesToShow instanceof List)
//      this.entriesToShow = (List<Entry>)entriesToShow;
//    else
//      this.entriesToShow = new ArrayList<>(entriesToShow);
//  }


  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setHasOptionsMenu(true);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.fragment_entries, container, false);

    ListView lstvwEntries = (ListView)rootView.findViewById(R.id.lstvwEntries);
    entriesAdapter = new EntriesAdapter(getActivity());
    lstvwEntries.setAdapter(entriesAdapter);
    registerForContextMenu(lstvwEntries);
    lstvwEntries.setOnItemClickListener(lstvwEntriesOnItemClickListener);

    rlySearchEntries = (RelativeLayout)rootView.findViewById(R.id.rlySearchEntries);

    edtxtSearchEntries = (EditText)rootView.findViewById(R.id.edtxtSearchEntries);
    edtxtSearchEntries.addTextChangedListener(edtxtSearchEntriesTextWatcher);

    return rootView;
  }

  @Override
  public void onDestroyView() {
    if(entriesAdapter != null)
      entriesAdapter.cleanUp();

    super.onDestroyView();
  }

  protected AdapterView.OnItemClickListener lstvwEntriesOnItemClickListener = new AdapterView.OnItemClickListener() {
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
      Entry entry = (Entry)parent.getItemAtPosition(position);
      ActivityManager.getInstance().showEditEntryActivity(entry);
    }
  };

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    inflater.inflate(R.menu.fragment_entries_options_menu, menu);

    super.onCreateOptionsMenu(menu, inflater);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();

    if(id == R.id.mnitmActionSearchEntries) {
      toggleSearchBarVisibility();
      return true;
    }
    if (id == R.id.mnitmActionAddEntry) {
      onActionAddEntrySelected();
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  protected void toggleSearchBarVisibility() {
    if(rlySearchEntries.getVisibility() == View.GONE) {
      rlySearchEntries.setVisibility(View.VISIBLE);
      edtxtSearchEntries.requestFocus();
    }
    else {
      entriesAdapter.showAllEntries();
      rlySearchEntries.setVisibility(View.GONE);
    }
  }

  protected void onActionAddEntrySelected() {
    Entry entry = new Entry();
    ActivityManager.getInstance().showEditEntryActivity(entry);
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
        ActivityManager.getInstance().showEditEntryActivity(entryToEdit);
        return true;
      case R.id.list_item_entry_context_menu_delete:
        Entry entryToDelete = entriesAdapter.getEntryAt(info.position);
        deepThought.removeEntry(entryToDelete);
        return true;
      default:
        return super.onContextItemSelected(item);
    }
  }

  protected AdapterView.OnItemLongClickListener lstvwEntriesOnItemLongClickListener = new AdapterView.OnItemLongClickListener() {
    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
      Entry entry = (Entry)parent.getItemAtPosition(position);
      if(entry != null) {

        return true;
      }

      return false;
    }
  };

  protected TextWatcher edtxtSearchEntriesTextWatcher = new TextWatcher() {
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
      entriesAdapter.searchEntries(s.toString());
    }

    @Override
    public void afterTextChanged(Editable s) {

    }
  };

}
