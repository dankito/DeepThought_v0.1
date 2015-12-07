package net.deepthought.dialogs;

import net.deepthought.Application;
import net.deepthought.controller.ChildWindowsController;
import net.deepthought.controller.ChildWindowsControllerListener;
import net.deepthought.controller.Dialogs;
import net.deepthought.controller.enums.DialogResult;
import net.deepthought.data.model.DeepThought;
import net.deepthought.data.model.Reference;
import net.deepthought.data.model.ReferenceBase;
import net.deepthought.data.model.ReferenceSubDivision;
import net.deepthought.data.model.SeriesTitle;
import net.deepthought.util.Localization;
import net.deepthought.util.StringUtils;
import net.deepthought.util.isbn.IsbnResolvingListener;
import net.deepthought.util.isbn.ResolveIsbnResult;

import javafx.application.Platform;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;
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
    Dialogs.showEditReferenceDialog(result.getResolvedReference(), new ChildWindowsControllerListener() {
      @Override
      public void windowClosing(Stage stage, ChildWindowsController controller) {
        mayPersistResolvedReferenceAndDispatchResult(controller, result);
      }

      @Override
      public void windowClosed(Stage stage, ChildWindowsController controller) {

      }
    });
  }

  protected void mayPersistResolvedReferenceAndDispatchResult(ChildWindowsController controller, ResolveIsbnResult result) {
    if(controller.getDialogResult() == DialogResult.Ok) {
      persistResolvedReference(result.getResolvedReference());
    }

    dispatchResult(result, controller);
  }

  protected void persistResolvedReference(ReferenceBase referenceBase) {
    if(referenceBase != null && referenceBase.isPersisted() == false) {
      DeepThought deepThought = Application.getDeepThought();

      if(referenceBase instanceof SeriesTitle) {
        deepThought.addSeriesTitle((SeriesTitle)referenceBase);
      }
      else if(referenceBase instanceof Reference) {
        deepThought.addReference((Reference)referenceBase);
      }
      else if(referenceBase instanceof ReferenceSubDivision) {
        deepThought.addReferenceSubDivision((ReferenceSubDivision)referenceBase);
      }
    }
  }

  protected void dispatchResult(ResolveIsbnResult result, ChildWindowsController controller) {
    if(listener != null) {
      if(controller.getDialogResult() == DialogResult.Ok) {
        listener.isbnResolvingDone(result);
      }
      else {
        listener.isbnResolvingDone(new ResolveIsbnResult(false));
      }
    }
  }

}
