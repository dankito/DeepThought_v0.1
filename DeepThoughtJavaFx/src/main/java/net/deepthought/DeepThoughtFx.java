package net.deepthought;

import net.deepthought.data.model.DeepThought;
import net.deepthought.util.JavaFxLocalization;
import net.deepthought.util.Localization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.application.Application;
import javafx.application.HostServices;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class DeepThoughtFx extends Application {

  private final static Logger log = LoggerFactory.getLogger(DeepThought.class);


  protected static HostServices hostServices = null;

  public static HostServices hostServices() {
    return hostServices;
  }


  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) {
    launch(args);
  }

  @Override
  public void start(Stage stage) throws Exception {
    try {
//      Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("MainDocument.fxml"));
      FXMLLoader loader = new FXMLLoader();
      loader.setResources(Localization.getStringsResourceBundle());
      loader.setLocation(getClass().getClassLoader().getResource("dialogs/MainWindow.fxml"));
      Parent root = (Parent)loader.load();

      Scene scene = new Scene(root);
//    String mainDocumentCss = getClass().getResource("/MainDocument.css").toExternalForm();
//    scene.getStylesheets().add(mainDocumentCss);

      stage.setScene(scene);
      stage.titleProperty().bind(Bindings.createStringBinding(
          () -> Localization.getLocalizedStringForResourceKey("i.know.me.nothing.knowing") + " - DeepThought", JavaFxLocalization.localeProperty()));

      MainWindowController controller = (MainWindowController)loader.getController();
      controller.setStage(stage);

      stage.show();

      hostServices = getHostServices();
    } catch(Exception ex) {
      log.error("Could not start MainWindow", ex);
    }
  }

  @Override
  public void stop() throws Exception {
    super.stop();
    System.exit(0);
  }
}
