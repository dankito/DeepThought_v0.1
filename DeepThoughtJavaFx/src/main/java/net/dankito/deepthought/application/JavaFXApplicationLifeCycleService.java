package net.dankito.deepthought.application;

import javafx.event.EventHandler;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * Created by ganymed on 01/09/16.
 */
public class JavaFXApplicationLifeCycleService extends ApplicationLifeCycleServiceBase {

  protected Stage stage;


  public JavaFXApplicationLifeCycleService() {

  }


  public void setMainWindowStage(Stage stage) {
    this.stage = stage;

    setStageOnHidingEvent(stage);
  }

  protected void setStageOnHidingEvent(Stage stage) {
    stage.setOnHiding(new EventHandler<WindowEvent>() {
      @Override
      public void handle(WindowEvent event) {
        applicationIsGoingToTerminate();
      }
    });
  }
}
