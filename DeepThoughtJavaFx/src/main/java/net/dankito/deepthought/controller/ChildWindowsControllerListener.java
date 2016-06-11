package net.dankito.deepthought.controller;

import javafx.stage.Stage;

/**
 * Created by ganymed on 25/12/14.
 */
public interface ChildWindowsControllerListener {

  public void windowClosing(Stage stage, ChildWindowsController controller);

  public void windowClosed(Stage stage, ChildWindowsController controller);

}
