package net.deepthought.activities;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import net.deepthought.MainActivity;
import net.deepthought.R;
import net.deepthought.data.model.Entry;
import net.deepthought.data.model.Tag;
import net.deepthought.fragments.EntriesFragment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 * Created by ganymed on 12/10/14.
 */
public class ActivityManager {

  private final static Logger log = LoggerFactory.getLogger(ActivityManager.class);


  protected static ActivityManager instance = null;

  public static void createInstance(MainActivity mainActivity) {
    instance = new ActivityManager(mainActivity);
  }

  public static ActivityManager getInstance() {
    return instance;
  }


  protected MainActivity mainActivity = null;


  protected ActivityManager(MainActivity mainActivity) {
    this.mainActivity = mainActivity;
  }


  /*    Edit Entry Activity     */

  protected Entry entryToBeEdited = null;

  public Entry getEntryToBeEdited() {
    return entryToBeEdited;
  }

  public void showEditEntryActivity(Entry entry) {
    try {
      entryToBeEdited = entry;

      Intent startEditEntryActivityIntent = new Intent(mainActivity, EditEntryActivity.class);
      mainActivity.startActivityForResult(startEditEntryActivityIntent, EditEntryActivity.RequestCode);
    } catch(Exception ex) {
      log.error("Could not start EditEntryActivity", ex);
    }
  }

  public void navigateToEntriesFragment(Collection<Entry> entries, int fragmentToReplace) {
    Fragment entriesFragment = new EntriesFragment(entries);
    FragmentTransaction transaction = mainActivity.getSupportFragmentManager().beginTransaction();
    transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);

    transaction.replace(R.id.rlyFragmentTags, entriesFragment);

    transaction.addToBackStack("Tags");

    transaction.commit();
  }

  public void showEditTagActivity(Tag tag) {
    // TODO
  }


  public MainActivity getMainActivity() {
    return mainActivity;
  }
}
