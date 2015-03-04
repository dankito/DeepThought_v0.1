package net.deepthought.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import net.deepthought.adapter.EntryOverviewAdapter;
import net.deepthought.data.model.DeepThought;
import net.deepthought.data.model.Entry;

/**
 * Created by ganymed on 01/10/14.
 */
public class OverviewFragment extends Fragment {



  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setHasOptionsMenu(true);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.fragment_overview, container, false);

    ListView lstvwEntries = (ListView)rootView.findViewById(R.id.lstvwEntries);
    lstvwEntries.setAdapter(new EntryOverviewAdapter(getActivity()));
    registerForContextMenu(lstvwEntries);
    lstvwEntries.setOnItemClickListener(lstvwEntriesOnItemClickListener);

    return rootView;
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
    inflater.inflate(R.menu.fragment_overview_options_menu, menu);

    super.onCreateOptionsMenu(menu, inflater);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();
    if (id == R.id.mnitmActionAddEntry) {
      onActionAddEntrySelected();
      return true;
    }
    return super.onOptionsItemSelected(item);
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
        Entry entryToEdit = deepThought.entryAt(info.position);
        ActivityManager.getInstance().showEditEntryActivity(entryToEdit);
        return true;
      case R.id.list_item_entry_context_menu_delete:
        Entry entryToDelete = deepThought.entryAt(info.position);
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
}
