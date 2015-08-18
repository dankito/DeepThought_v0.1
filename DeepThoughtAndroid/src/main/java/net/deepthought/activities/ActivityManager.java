package net.deepthought.activities;

import android.content.Intent;

import net.deepthought.MainActivity;
import net.deepthought.data.model.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
      //startEditEntryActivityIntent.putExtra(EditEntryActivity.EntryArgumentKey, entry); // after serializing / deserializing it's not the same object anymore!
      mainActivity.startActivityForResult(startEditEntryActivityIntent, EditEntryActivity.RequestCode);
    } catch(Exception ex) {
      log.error("Could not start EditEntryActivity", ex);
    }
  }
}
