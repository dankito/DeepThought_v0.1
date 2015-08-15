package net.deepthought.controller;

import net.deepthought.controller.enums.DialogResult;

import javafx.stage.Stage;

/**
 * Created by ganymed on 25/12/14.
 */
public class ChildWindowsController {


  protected Stage windowStage = null;

  protected DialogResult dialogResult = DialogResult.Unset;

  protected ChildWindowsControllerListener listener;


  protected void closeDialog() {
    if(windowStage != null) {
      if (listener != null)
        listener.windowClosing(windowStage, this);

      windowStage.close();

      if (listener != null)
        listener.windowClosed(windowStage, this);
    }
  }

  protected void closeDialog(DialogResult dialogResult) {
    setDialogResult(dialogResult);
    closeDialog();
  }

  public ChildWindowsControllerListener getListener() {
    return listener;
  }

  public void setListener(ChildWindowsControllerListener listener) {
    this.listener = listener;
  }


  public DialogResult getDialogResult() {
    return dialogResult;
  }

  protected void setDialogResult(DialogResult dialogResult) {
    this.dialogResult = dialogResult;
  }

  public void setWindowStage(Stage windowStage) {
    this.windowStage = windowStage;

    windowStage.setOnCloseRequest(event -> {
      event.consume();

      if(askIfStageShouldBeClosed() == true) {
        windowStage.setOnCloseRequest(null);
        closeDialog(DialogResult.Cancel);
      }
    });
  }

  protected boolean askIfStageShouldBeClosed() {
    return true;
  }
}
