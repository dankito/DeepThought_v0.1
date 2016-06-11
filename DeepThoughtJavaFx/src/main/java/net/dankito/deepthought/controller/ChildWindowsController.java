package net.dankito.deepthought.controller;

import javafx.stage.Stage;

/**
 * Created by ganymed on 25/12/14.
 */
public class ChildWindowsController {


  protected Stage windowStage = null;

  protected net.dankito.deepthought.controller.enums.DialogResult dialogResult = net.dankito.deepthought.controller.enums.DialogResult.Unset;

  protected ChildWindowsControllerListener listener;


  protected void closeDialog() {
    if(windowStage != null) {
      if (listener != null)
        listener.windowClosing(windowStage, this);

      windowStage.close();

      if (listener != null)
        listener.windowClosed(windowStage, this);
    }

    // added 13.013.2016
    listener = null;
    windowStage = null;
  }

  protected void closeDialog(net.dankito.deepthought.controller.enums.DialogResult dialogResult) {
    setDialogResult(dialogResult);
    closeDialog();
  }

  public ChildWindowsControllerListener getListener() {
    return listener;
  }

  public void setListener(ChildWindowsControllerListener listener) {
    this.listener = listener;
  }


  public net.dankito.deepthought.controller.enums.DialogResult getDialogResult() {
    return dialogResult;
  }

  protected void setDialogResult(net.dankito.deepthought.controller.enums.DialogResult dialogResult) {
    this.dialogResult = dialogResult;
  }

  public void setWindowStage(Stage windowStage) {
    this.windowStage = windowStage;

    windowStage.setOnCloseRequest(event -> {
      event.consume();

      if(askIfStageShouldBeClosed() == true) {
        windowStage.setOnCloseRequest(null);
        closeDialog(net.dankito.deepthought.controller.enums.DialogResult.Cancel);
      }
    });
  }

  protected boolean askIfStageShouldBeClosed() {
    return true; // maybe subclasses want to perform their own checks (e.g. to save unsaved changes)
  }
}
