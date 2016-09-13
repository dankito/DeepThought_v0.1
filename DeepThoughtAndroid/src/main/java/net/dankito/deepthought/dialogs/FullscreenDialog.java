package net.dankito.deepthought.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import net.dankito.deepthought.Application;
import net.dankito.deepthought.R;
import net.dankito.deepthought.controls.ICleanUp;
import net.dankito.deepthought.listener.DialogListener;

/**
 * Created by ganymed on 06/09/16.
 */
public abstract class FullscreenDialog extends DialogFragment implements ICleanUp {

  protected boolean cleanUpOnClose = false;

  protected boolean hasDialogPreviouslyBeenShown = false;

  protected DialogListener dialogListener = null;


  public FullscreenDialog() {

  }



  public void setCleanUpOnClose(boolean cleanUpOnClose) {
    this.cleanUpOnClose = cleanUpOnClose;
  }

  public void setDialogListener(DialogListener dialogListener) {
    this.dialogListener = dialogListener;
  }



  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setStyle(DialogFragment.STYLE_NORMAL, R.style.FullscreenDialog);
  }


  @Override
  public void onStart() {
    super.onStart();

    Dialog dialog = getDialog();
    if(dialog != null) {
      int width = ViewGroup.LayoutParams.MATCH_PARENT;
      int height = ViewGroup.LayoutParams.MATCH_PARENT;
      dialog.getWindow().setLayout(width, height);

      WindowManager.LayoutParams attrs = dialog.getWindow().getAttributes();
      attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
      dialog.getWindow().setAttributes(attrs);
    }
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

  }


  public boolean canHandleActivityResult(int requestCode, int resultCode, Intent data) {
    return false;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();

    if (id == android.R.id.home) {
      checkForUnsavedChangesAndCloseDialog(false);
      return true;
    }

    return super.onOptionsItemSelected(item);
  }


  public void onBackPressed() {
    checkForUnsavedChangesAndCloseDialog(true);
  }

  protected void checkForUnsavedChangesAndCloseDialog(boolean hasBackButtonBeenPressed) {
    if(hasUnsavedChanges() == true) {
      askUserIfChangesShouldBeSaved(hasBackButtonBeenPressed);
    }
    else {
      closeDialog(hasBackButtonBeenPressed, false);
    }
  }

  protected boolean hasUnsavedChanges() {
    return false;
  }

  protected void askUserIfChangesShouldBeSaved(final boolean hasBackButtonBeenPressed) {
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    TextView view = new TextView(getActivity());
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
        resetEditedFieldsAndCloseDialog(hasBackButtonBeenPressed);
      }
    });

    builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialogInterface, int i) {
        saveEntryAndCloseDialog(hasBackButtonBeenPressed);
      }
    });

    builder.create().show();
  }

  protected int getAlertMessageIfChangesShouldGetSaved() {
    return -1; // to be overwritten in subclass
  }

  protected void saveEntryAndCloseDialog(boolean hasBackButtonBeenPressed) {
    saveEntityAsyncIfNeeded();

    closeDialog(hasBackButtonBeenPressed, true);
  }

  protected void saveEntityAsyncIfNeeded() {
    if(hasUnsavedChanges()) {
      saveEntityAsync();
    }
  }

  protected void saveEntityAsync() {
    // why do i run this little code on a new Thread? Getting HTML from AndroidHtmlEditor has to be done from a different one than main thread,
    // as async JavaScript response is dispatched to the main thread, therefore waiting for it as well on the main thread would block JavaScript response listener
    Application.getThreadPool().runTaskAsync(new Runnable() {
      @Override
      public void run() {
        saveEntity();
      }
    });
  }

  protected void saveEntity() {
    // to be implemented in subclass
  }

  protected void resetEditedFieldsAndCloseDialog(boolean hasBackButtonBeenPressed) {
    if(cleanUpOnClose == false) { // an instance of this Dialog is held somewhere
      // TODO: unset controls with edited fields
    }

    closeDialog(hasBackButtonBeenPressed, false);
  }

  public void closeDialog(boolean hasBackButtonBeenPressed, boolean hasEntryBeenSaved) {
    if(cleanUpOnClose) { // if calling Activity / Dialog keeps an instance of this Dialog, that one will call cleanUp(), don't do it itself
      cleanUp();
    }

    hideDialog(hasEntryBeenSaved);

    if(hasBackButtonBeenPressed == false) {
      getActivity().getSupportFragmentManager().popBackStack();
    }
  }



  public void showDialog(AppCompatActivity activity) {
    FragmentManager fragmentManager = activity.getSupportFragmentManager();
    FragmentTransaction transaction = fragmentManager.beginTransaction();
    transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);

//    if(hasDialogPreviouslyBeenShown == false) { // on first display create EditEntryDialog and add it to transaction
      transaction.add(this, "EditEntry"); // TODO: edit Tag
//    }
//    else { // on subsequent displays we only have to call show() on the then hidden Dialog
//      transaction.show(this);
//    }

    transaction.addToBackStack("");

    transaction.commit();

    hasDialogPreviouslyBeenShown = true;
  }

  protected void hideDialog(boolean hasEntryBeenSaved) {
    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
    FragmentTransaction transaction = fragmentManager.beginTransaction();
    transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
    transaction.hide(this);
    transaction.commit();

    callDialogBecameHiddenListener(hasEntryBeenSaved);
  }

  protected void callDialogBecameHiddenListener(boolean hasEntryBeenSaved) {
    if(dialogListener != null) {
      dialogListener.dialogBecameHidden(hasEntryBeenSaved);
    }
  }


  @Override
  public void cleanUp() {

  }

}
