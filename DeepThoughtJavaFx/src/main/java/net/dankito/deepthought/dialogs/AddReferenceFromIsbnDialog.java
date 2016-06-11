package net.dankito.deepthought.dialogs;

import net.dankito.deepthought.Application;
import net.dankito.deepthought.util.localization.Localization;
import net.dankito.deepthought.util.StringUtils;
import net.dankito.deepthought.util.isbn.IsbnResolvingListener;
import net.dankito.deepthought.util.isbn.ResolveIsbnResult;

import javafx.application.Platform;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Window;

/**
 * Created by ganymed on 07/12/15.
 */
public class AddReferenceFromIsbnDialog {

  protected IsbnResolvingListener listener = null;


  public AddReferenceFromIsbnDialog() {

  }


  public void showAsync(Window owner, IsbnResolvingListener listener) {
    this.listener = listener;
    showEnterIsbnDialog(owner, null, null);
  }

  protected void showEnterIsbnDialog(Window owner, final String lastEnteredIsbn, final String lastEnteredIsbnErrorText) {
    if(Platform.isFxApplicationThread())
      showEnterIsbnDialogOnUiThread(owner, lastEnteredIsbn, lastEnteredIsbnErrorText);
    else
      Platform.runLater(() -> showEnterIsbnDialogOnUiThread(owner, lastEnteredIsbn, lastEnteredIsbnErrorText));
  }

  protected void showEnterIsbnDialogOnUiThread(Window owner, String lastEnteredIsbn, String lastEnteredIsbnErrorText) {
    TextInputDialog dialog = new TextInputDialog(lastEnteredIsbn);
    dialog.initOwner(owner);
    dialog.setHeaderText(lastEnteredIsbnErrorText);
    dialog.setTitle(Localization.getLocalizedString("enter.isbn.dialog.title"));
    dialog.setContentText(Localization.getLocalizedString("enter.isbn"));

    waitForAndHandleUserIsbnInput(dialog);
  }

  protected void waitForAndHandleUserIsbnInput(TextInputDialog dialog) {
    dialog.setOnCloseRequest(event -> {
      String enteredIsbn = dialog.getResult();
      if(StringUtils.isNotNullOrEmpty(enteredIsbn)) {
        event.consume();
        getReferenceForIsbn(enteredIsbn, dialog);
      }
      else if(listener != null) {
        listener.isbnResolvingDone(new ResolveIsbnResult(false));
      }
    });

    dialog.show();
  }

  protected void getReferenceForIsbn(final String enteredIsbn, final TextInputDialog askForIsbnDialog) {
    Application.getIsbnResolver().resolveIsbnAsync(enteredIsbn, new IsbnResolvingListener() {
      @Override
      public void isbnResolvingDone(ResolveIsbnResult result) {
        Platform.runLater(() -> retrievedIsbnResolvingResult(result, askForIsbnDialog, enteredIsbn));
      }
    });
  }

  protected void retrievedIsbnResolvingResult(ResolveIsbnResult result, TextInputDialog askForIsbnDialog, String enteredIsbn) {
    if (result.isSuccessful()) {
      askForIsbnDialog.setOnCloseRequest(null);
      askForIsbnDialog.close();
      showEditReferenceDialog(result);
    } else {
//          showEnterIsbnDialog(askForIsbnDialog.getOwner(), enteredIsbn, Localization.getLocalizedString("could.not.resolve.isbn", enteredIsbn));
      askForIsbnDialog.setHeaderText(Localization.getLocalizedString("could.not.resolve.isbn", enteredIsbn));
    }
  }

  protected void showEditReferenceDialog(ResolveIsbnResult result) {
    net.dankito.deepthought.controller.Dialogs.showEditReferenceDialogAndPersistOnResultOk(result, listener);
  }

}
