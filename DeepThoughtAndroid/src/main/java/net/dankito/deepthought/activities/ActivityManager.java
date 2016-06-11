package net.dankito.deepthought.activities;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import net.dankito.deepthought.fragments.EntriesFragment;
import net.dankito.deepthought.listener.EntityEditedListener;
import net.dankito.deepthought.R;
import net.dankito.deepthought.data.contentextractor.EntryCreationResult;
import net.dankito.deepthought.data.contentextractor.IOnlineArticleContentExtractor;
import net.dankito.deepthought.data.model.Entry;
import net.dankito.deepthought.data.model.Tag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 * Created by ganymed on 12/10/14.
 */
public class ActivityManager {

  private final static Logger log = LoggerFactory.getLogger(ActivityManager.class);


  protected static ActivityManager instance = null;

//  public static void createInstance() {
//    instance = new ActivityManager();
//  }

  public static ActivityManager getInstance() {
    if(instance == null)
      instance = new ActivityManager();
    return instance;
  }

  public static void cleanUp() {
    if(instance != null) {
      instance.resetEditEntryActivityCachedData();
      instance.resetShowArticlesOverviewActivityCachedData();
    }

    instance = null;
  }



  protected ActivityManager() {

  }


  /*    Edit Entry Activity     */

  protected Entry entryToBeEdited = null;

  protected EntryCreationResult entryCreationResultToBeEdited = null;

  public Entry getEntryToBeEdited() {
    return entryToBeEdited;
  }

  public EntryCreationResult getEntryCreationResultToBeEdited() {
    return entryCreationResultToBeEdited;
  }

  public void resetEditEntryActivityCachedData() {
    entryToBeEdited = null;
    entryCreationResultToBeEdited = null;
  }


  public void showEditEntryActivity(Activity activity, Entry entry) {
    showEditEntryActivity(activity, entry, null);
  }

  public void showEditEntryActivity(Activity activity, EntryCreationResult creationResult) {
    showEditEntryActivity(activity, null, creationResult);
  }

  protected void showEditEntryActivity(Activity activity, Entry entry, EntryCreationResult creationResult) {
    entryToBeEdited = entry;
    entryCreationResultToBeEdited = creationResult;

    try {
      Intent startEditEntryActivityIntent = new Intent(activity, EditEntryActivity.class);
      activity.startActivityForResult(startEditEntryActivityIntent, EditEntryActivity.RequestCode);
    } catch(Exception ex) {
      log.error("Could not start EditEntryActivity", ex);
    }
  }


  /*    Articles Overview Activity     */

  protected IOnlineArticleContentExtractor extractorToShowArticlesOverviewActivityFor = null;

  public IOnlineArticleContentExtractor getExtractorToShowArticlesOverviewActivityFor() {
    return extractorToShowArticlesOverviewActivityFor;
  }

  public void resetShowArticlesOverviewActivityCachedData() {
    extractorToShowArticlesOverviewActivityFor = null;
  }


  public void showArticlesOverviewActivity(Activity activity, IOnlineArticleContentExtractor extractor) {
    try {
      this.extractorToShowArticlesOverviewActivityFor = extractor;

      Intent startEditEntryActivityIntent = new Intent(activity, ArticlesOverviewActivity.class);
      activity.startActivity(startEditEntryActivityIntent);
    } catch(Exception ex) {
      log.error("Could not start EditEntryActivity", ex);
    }
  }


  public void navigateToEntriesFragment(FragmentManager fragmentManager, Collection<Entry> entries, int fragmentToReplace) {
    Fragment entriesFragment = new EntriesFragment(entries);
    FragmentTransaction transaction = fragmentManager.beginTransaction();
    transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);

    transaction.replace(fragmentToReplace, entriesFragment);

    transaction.addToBackStack("Tags");

    transaction.commitAllowingStateLoss();
  }

  public void showEditTagAlert(Context context, final Tag tag) {
    showEditTagAlert(context, tag, null);
  }

  public void showEditTagAlert(Context context, final Tag tag, final EntityEditedListener listener) {
    LayoutInflater inflater = LayoutInflater.from(context);
    View editTagView = inflater.inflate(R.layout.alert_edit_tag, null);

    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
    alertDialogBuilder.setView(editTagView);

    final EditText edtxtTagName = (EditText) editTagView.findViewById(R.id.edtxtTagName);
    final EditText edtxtTagDescription = (EditText) editTagView.findViewById(R.id.edtxtTagDescription);

    edtxtTagName.setText(tag.getName());
    edtxtTagDescription.setText(tag.getDescription());

    alertDialogBuilder
        .setCancelable(true)
        .setPositiveButton(R.string.ok,
            new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog,int id) {
                tag.setName(edtxtTagName.getText().toString());
                tag.setDescription(edtxtTagDescription.getText().toString());

                if(listener != null)
                  listener.editingDone(false, tag);
              }
            })
        .setNegativeButton(R.string.cancel,
            new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog,int id) {
                dialog.cancel();
                if(listener != null)
                  listener.editingDone(true, tag);
              }
            });

    AlertDialog alertDialog = alertDialogBuilder.create();
    alertDialog.show();
    alertDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, 470);
  }

}
