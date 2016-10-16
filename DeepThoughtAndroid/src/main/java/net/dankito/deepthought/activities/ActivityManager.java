package net.dankito.deepthought.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import net.dankito.deepthought.R;
import net.dankito.deepthought.data.model.Tag;
import net.dankito.deepthought.data.search.ui.EntriesForTag;
import net.dankito.deepthought.fragments.EntriesFragment;
import net.dankito.deepthought.listener.EntityEditedListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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



  protected ActivityManager() {

  }



  public void navigateToEntriesFragment(FragmentManager fragmentManager, EntriesForTag entriesForTag, int fragmentToReplace) {
    Fragment entriesFragment = new EntriesFragment(entriesForTag);
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

    edtxtTagName.selectAll();

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

    edtxtTagName.requestFocus();

    InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
    imm.showSoftInput(edtxtTagName, InputMethodManager.SHOW_IMPLICIT);

    alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
  }

}
