package net.deepthought;

import net.deepthought.data.model.DeepThought;
import net.deepthought.data.model.Entry;
import net.deepthought.util.JavaFxLocalization;

import org.junit.BeforeClass;
import org.testfx.framework.junit.ApplicationTest;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Created by ganymed on 20/12/15.
 */
public abstract class UiTestBase extends ApplicationTest {

  /**
   * <p>Copied from {@link .ApplicationLauncherImpl}</p>
   * <p>
   *   Used for running in headless Environments.<br />
   *
   * </p>
   */
  private static final String PROPERTY_TESTFX_HEADLESS = "testfx.headless";

  protected static final String TestFolder = "data/uitests";

  protected static final int CountDefaultEntries = 20;


  protected DeepThought deepThought = null;


  @BeforeClass
  public static void changeToHeadless() {
    if(testHeadless()) {
      setToHeadless();
    }
  }

  protected static boolean testHeadless() {
    return false;
  }

  protected static void setToHeadless() {
    System.setProperty("glass.platform", "Monocle");
    System.setProperty("monocle.platform", "Headless");
    System.setProperty("prism.order", "sw");

    System.setProperty(PROPERTY_TESTFX_HEADLESS, "true");
  }


  @Override
  public void start(Stage stage) throws Exception {
//    System.setProperty("user.dir", new File(TestFolder).getAbsolutePath());

    setupStage(stage);
  }

  protected void setupStage(Stage stage) throws Exception {
    setupStage(stage, "dialogs/MainWindow.fxml", 1150, 620);
  }

  protected void setupStage(Stage stage, String fxmlFilePath) throws Exception {
    setupStage(stage, fxmlFilePath, 800, 600);
  }

  protected void setupStage(Stage stage, String fxmlFilePath, double stageWidth, double stageHeight) throws Exception {
    Parent root = loadFxml(stage, fxmlFilePath);

    Scene scene = new Scene(root, stageWidth, stageHeight);
    stage.setScene(scene);
    stage.show();
    stage.toFront();

    sleep(3, TimeUnit.SECONDS); // give Stage some time to initialize

    deepThought = Application.getDeepThought();
  }

  protected Parent loadFxml(Stage stage, String fxmlFilePath) throws IOException {
    FXMLLoader loader = new FXMLLoader();
    loader.setResources(JavaFxLocalization.Resources);
    loader.setLocation(getClass().getClassLoader().getResource(fxmlFilePath));
    Parent root = loader.load();
    JavaFxLocalization.resolveResourceKeys(root);

    MainWindowController controller = loader.getController();
    controller.setStage(stage);

    return root;
  }


  protected TableView<Entry> getMainWindowTableViewEntries() {
    return lookup("#tblvwEntries").queryFirst();
  }

  protected TextField getMainWindowTextFieldSearchEntriesEntries() {
    return lookup("#txtfldSearchEntries").queryFirst();
  }

}
