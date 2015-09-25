package net.deepthought.activities;

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

import net.deepthought.MainActivity;
import net.deepthought.R;
import net.deepthought.data.contentextractor.IOnlineArticleContentExtractor;
import net.deepthought.data.model.Entry;
import net.deepthought.data.model.Tag;
import net.deepthought.fragments.EntriesFragment;
import net.deepthought.listener.EntityEditedListener;

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


  /*    Articles OverviewA ctivity     */

  protected IOnlineArticleContentExtractor extractorToShowArticlesOverviewActivityFor = null;

  public IOnlineArticleContentExtractor getExtractorToShowArticlesOverviewActivityFor() {
    return extractorToShowArticlesOverviewActivityFor;
  }

  public void showArticlesOverviewActivity(IOnlineArticleContentExtractor extractor) {
    try {
      this.extractorToShowArticlesOverviewActivityFor = extractor;

      Intent startEditEntryActivityIntent = new Intent(mainActivity, ArticlesOverviewActivity.class);
      mainActivity.startActivity(startEditEntryActivityIntent);
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
