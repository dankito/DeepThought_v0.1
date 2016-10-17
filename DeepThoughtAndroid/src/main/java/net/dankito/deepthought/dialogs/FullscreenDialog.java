package net.dankito.deepthought.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import net.dankito.deepthought.Application;
import net.dankito.deepthought.R;
import net.dankito.deepthought.activities.DialogParentActivity;
import net.dankito.deepthought.controls.ICleanUp;
import net.dankito.deepthought.listener.DialogListener;

/**
 * Created by ganymed on 06/09/16.
 */
public abstract class FullscreenDialog extends DialogFragment implements ICleanUp {

  protected DialogParentActivity activity;

  protected boolean hideOnClose = false;

  protected boolean hasDialogPreviouslyBeenShown = false;

  protected DialogListener dialogListener = null;


  public FullscreenDialog() {

  }


  /**
   * Sets if Dialog should be really closed (false) or only be hidden (true) so it can quickly be re-shown again.
   * When set to false (the default value) {@link #cleanUp()} will be called automatically on close.
   * When set to true it relies on the caller to call {@link #cleanUp()} by himself after he's done using it.
   * @param hideOnClose
   */
  public void setHideOnClose(boolean hideOnClose) {
    this.hideOnClose = hideOnClose;
  }

  public void setDialogListener(DialogListener dialogListener) {
    this.dialogListener = dialogListener;
  }



  @Override
  public void onResume() {
    Dialog dialog = getDialog();
    if(dialog != null) {
      int width = ViewGroup.LayoutParams.MATCH_PARENT;
      int height = ViewGroup.LayoutParams.MATCH_PARENT;
      dialog.getWindow().setLayout(width, height);

      WindowManager.LayoutParams attrs = dialog.getWindow().getAttributes();
      attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
      dialog.getWindow().setAttributes(attrs);

      // TODO: not working // Set to adjust screen height automatically, when soft keyboard appears on screen
      dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    }

    super.onResume();
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    restoreSavedInstance(savedInstanceState);

    View rootView = inflater.inflate(getLayoutId(), container, false);

    // don't know why but when placing Dialog in android.R.id.content, the Dialog's content starts below the system status bar -> set a top margin in height of status bar
    FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)rootView.getLayoutParams();
    params.setMargins(0, getStatusBarHeight(), 0, 0);
    rootView.setLayoutParams(params);

    setupToolbar(rootView);

    setHasOptionsMenu(true);


    setupUi(rootView);

    return rootView;
  }

  protected void restoreSavedInstance(Bundle savedInstanceState) {

  }

  protected abstract int getLayoutId();

  protected int getStatusBarHeight() {
    int result = 0;

    int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
    if (resourceId > 0) {
      result = getResources().getDimensionPixelSize(resourceId);
    }

    return result;
  }

  protected void setupToolbar(View rootView) {
    Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
    toolbar.setTitle("");

    ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

    ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
    if (actionBar != null) {
      actionBar.setDisplayHomeAsUpEnabled(true);
      actionBar.setHomeButtonEnabled(true);
      actionBar.setHomeAsUpIndicator(android.R.drawable.ic_menu_close_clear_cancel);

      customizeToolbar(rootView, actionBar);
    }
  }

  protected void customizeToolbar(View rootView, ActionBar actionBar) {
    // may be overwritten in sub class
  }

  protected abstract void setupUi(View rootView);


  public boolean canHandleActivityResult(int requestCode, int resultCode, Intent data) {
    return false;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();

    if (id == android.R.id.home) {
      checkForUnsavedChangesAndCloseDialog();
      return true;
    }

    return super.onOptionsItemSelected(item);
  }


  public void onBackPressed() {
    checkForUnsavedChangesAndCloseDialog();
  }

  protected void checkForUnsavedChangesAndCloseDialog() {
    if(shouldUserBeAskedToSaveChanges() == true) {
      askUserIfChangesShouldBeSaved();
    }
    else {
      closeDialogAndMayCleanUp(false);
    }
  }

  /**
   * When leaving dialog, determines if User should be ask if she/he likes to save changes.
   * @return
   */
  protected boolean shouldUserBeAskedToSaveChanges() {
    return hasUnsavedChangesThatShouldBeSaved();
  }

  protected void askUserIfChangesShouldBeSaved() {
    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
    TextView view = new TextView(activity);
    view.setText(getAlertMessageIfChangesShouldGetSaved());
    builder.setView(view);

    builder.setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {

      }
    });

    builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialogInterface, int i) {
        resetEditedFieldsAndCloseDialog();
      }
    });

    builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialogInterface, int i) {
        saveEntryAndCloseDialog();
      }
    });

    builder.create().show();
  }

  protected int getAlertMessageIfChangesShouldGetSaved() {
    return -1; // to be overwritten in subclass
  }

  protected void saveEntryAndCloseDialog() {
    if(hasUnsavedChangesThatShouldBeSaved()) {
      saveEntityAsyncAndCloseDialog();
    }
    else {
      closeDialogAndMayCleanUp(true);
    }
  }

  /**
   * Determines if there are changes that should be saved.
   * Only if this method returns true {@link #saveEntity()} will be called.
   * @return
   */
  protected boolean hasUnsavedChangesThatShouldBeSaved() {
    return false;
  }

  protected void saveEntityAsyncAndCloseDialog() {
    // why do i run this little code on a new Thread? Getting HTML from AndroidHtmlEditor has to be done from a different one than main thread,
    // as async JavaScript response is dispatched to the main thread, therefore waiting for it as well on the main thread would block JavaScript response listener
    Application.getThreadPool().runTaskAsync(new Runnable() {
      @Override
      public void run() {
        saveEntity();

        activity.runOnUiThread(new Runnable() {
          @Override
          public void run() {
            closeDialogAndMayCleanUp(true);
          }
        });
      }
    });
  }

  protected void saveEntity() {
    // to be implemented in subclass
  }

  protected void resetEditedFieldsAndCloseDialog() {
    if(hideOnClose == true) { // an instance of this Dialog is held somewhere
      // TODO: unset controls with edited fields
    }

    closeDialogAndMayCleanUp(false);
  }

  public void closeDialogAndMayCleanUp(boolean hasEntryBeenSaved) {
    closeDialog(hasEntryBeenSaved);

    if(hideOnClose == false) { // if calling Activity / Dialog keeps an instance of this Dialog, that one will call cleanUp(), don't do it itself
      cleanUp();
    }
  }



  public void showDialog(DialogParentActivity activity) {
    this.activity = activity;

    FragmentManager fragmentManager = activity.getSupportFragmentManager();
    FragmentTransaction transaction = fragmentManager.beginTransaction();
    transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);

    if(hasDialogPreviouslyBeenShown == false) { // on first display create EditEntryDialog and add it to transaction
      // i don't know why this makes such a huge difference: when adding to android.R.id.content soft keyboard hides toolbar, ...
      transaction.add(android.R.id.content, this, getClass().getName());
//      transaction.add(this, getClass().getName()); // ... but using this method instead dialog gets destroyed on back button press

      this.setStyle(DialogFragment.STYLE_NORMAL, R.style.FullscreenDialog);
    }
    else { // on subsequent displays we only have to call show() on the then hidden Dialog
      transaction.show(this);
    }

    transaction.commit();

    hasDialogPreviouslyBeenShown = true;

    activity.dialogShown(this);
  }

  protected void closeDialog(boolean hasEntryBeenSaved) {
    FragmentManager fragmentManager = activity.getSupportFragmentManager();
    FragmentTransaction transaction = fragmentManager.beginTransaction();
    transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);

    if(hideOnClose) {
      transaction.hide(this); // only hide Dialog so that it quickly can be redisplayed later on
    }
    else {
      transaction.remove(this);
    }

    transaction.commit();

    if(activity != null) {
      activity.dialogHidden(this);
    }

    callDialogBecameHiddenListener(hasEntryBeenSaved);
  }

  protected void callDialogBecameHiddenListener(boolean hasEntryBeenSaved) {
    if(dialogListener != null) {
      dialogListener.dialogBecameHidden(hasEntryBeenSaved);
    }
  }


  @Override
  public void cleanUp() {
    dialogListener = null;
  }

}
