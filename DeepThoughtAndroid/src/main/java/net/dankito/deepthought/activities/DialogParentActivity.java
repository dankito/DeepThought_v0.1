package net.dankito.deepthought.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

import net.dankito.deepthought.dialogs.FullscreenDialog;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ganymed on 13/09/16.
 */
public class DialogParentActivity extends AppCompatActivity {

  protected List<FullscreenDialog> visibleDialogs = new ArrayList<>();


  public void dialogShown(final FullscreenDialog dialog) {
    visibleDialogs.add(0, dialog); // add on top to simulate Stack -> we always know the most recently shown Dialog
  }

  public void dialogHidden(FullscreenDialog dialog) {
    visibleDialogs.remove(dialog);
  }

  public boolean isDialogVisible(FullscreenDialog dialog) {
    return visibleDialogs.contains(dialog);
  }


  protected boolean isBackButtonPressHandledByDialog() {
    if(visibleDialogs.size() > 0) {
      visibleDialogs.get(0).onBackPressed();
      return true;
    }

    return false;
  }

  protected boolean canDialogHandleActivityResult(int requestCode, int resultCode, Intent data) {
    for(FullscreenDialog dialog : visibleDialogs) {
      if(dialog.canHandleActivityResult(requestCode, resultCode, data)) {
        return true;
      }
    }

    return false;
  }

}
