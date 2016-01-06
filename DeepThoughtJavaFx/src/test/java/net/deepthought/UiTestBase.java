package net.deepthought;

import com.sun.javafx.scene.control.skin.ContextMenuContent;

import net.deepthought.controls.categories.EntryCategoriesControl;
import net.deepthought.controls.file.FilesControl;
import net.deepthought.controls.html.CollapsibleHtmlEditor;
import net.deepthought.controls.person.EntryPersonsControl;
import net.deepthought.controls.reference.EntryReferenceControl;
import net.deepthought.controls.tag.EntryTagsControl;
import net.deepthought.controls.utils.FXUtils;
import net.deepthought.data.model.DeepThought;
import net.deepthought.data.model.Entry;
import net.deepthought.javafx.dialogs.mainwindow.tabs.tags.TabTagsControl;
import net.deepthought.util.JavaFxLocalization;
import net.deepthought.util.Localization;

import org.junit.BeforeClass;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.service.finder.WindowFinder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
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

  protected static int CountDefaultEntries = 20;


  protected Stage stage;

  protected DeepThought deepThought = null;

  protected WindowFinder windowFinder = null;


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

    this.stage = stage;
    setupMainStage(stage);

    windowFinder = robotContext().getWindowFinder();
  }

  @Override
  public void stop() throws Exception {
    super.stop();

    stage.close();
    sleep(1, TimeUnit.SECONDS); // give Application some time to close
  }

  protected void setupMainStage(Stage stage) throws Exception {
    setupStage(stage, "dialogs/MainWindow.fxml", 1150, 620);

    deepThought = Application.getDeepThought();
    CountDefaultEntries = deepThought.countEntries();
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

    sleep(5, TimeUnit.SECONDS); // give Stage some time to initialize
  }

  protected Parent loadFxml(Stage stage, String fxmlFilePath) throws IOException {
    FXMLLoader loader = new FXMLLoader();
    loader.setResources(JavaFxLocalization.Resources);
    loader.setLocation(getClass().getClassLoader().getResource(fxmlFilePath));
    Parent root = loader.load();
    JavaFxLocalization.resolveResourceKeys(root);

    net.deepthought.javafx.dialogs.mainwindow.MainWindowController controller = loader.getController();
    controller.setStage(stage);

    return root;
  }


  protected void clickOk() {
    clickOn("#btnOk");
    sleep(2, TimeUnit.SECONDS);
  }

  protected void clickApply() {
    clickOn("#btnApplyChanges");
    sleep(2, TimeUnit.SECONDS);
  }

  protected void clickCancel() {
    clickOn("#btnCancel");
    sleep(2, TimeUnit.SECONDS);
  }


  protected boolean isAnAlertVisible() {
    return lookup(".alert").queryAll().size() > 0;
  }

  protected Button getAlertDefaultButton() {
    return lookup(".alert .button").lookup((Node node) -> {
      return ((Button) node).isDefaultButton();
    }).queryFirst();
  }

  protected void clickAlertDefaultButton() {
    clickOn(getAlertDefaultButton());
    sleep(1, TimeUnit.SECONDS);
  }

  protected Button getAlertCancelButton() {
    return lookup(".alert .button").lookup((Node node) -> {
      return ((Button) node).isCancelButton();
    }).queryFirst();
  }

  protected void clickAlertCancelButton() {
    clickOn(getAlertCancelButton());
    sleep(1, TimeUnit.SECONDS);
  }


  protected void focusNode(Node node) {
    final CountDownLatch waitLatch = new CountDownLatch(1);
    Platform.runLater(() -> {
      node.requestFocus();
      waitLatch.countDown();
    });
    try { waitLatch.await(2, TimeUnit.SECONDS); } catch(Exception ex) { }
  }

  protected void pressAndReleaseKeyOnNode(Node node, KeyCode key) {
    focusNode(node);

    press(key);
    release(key);
  }

  protected void clickOnCoordinateInNode(Node node, double coordinateInNodeX, double coordinateInNodeY, MouseButton button) {
    Point2D rowPoint = FXUtils.getNodeScreenCoordinates(node);
    moveTo(rowPoint.getX() + coordinateInNodeX, rowPoint.getY() + coordinateInNodeY);

    clickOn(button);
  }

  protected void rightClickOnCoordinateInNode(Node node, double coordinateInNodeX, double coordinateInNodeY) {
    clickOnCoordinateInNode(node, coordinateInNodeX, coordinateInNodeY, MouseButton.SECONDARY);
  }

  protected void showContextMenuInNode(Node node, double coordinateInNodeX, double coordinateInNodeY) {
    rightClickOnCoordinateInNode(node, coordinateInNodeX, coordinateInNodeY);
    sleep(1, TimeUnit.SECONDS);
  }

  protected void showContextMenuInNodeAndSelectItemById(Node node, double coordinateInNodeX, double coordinateInNodeY, String menuItemId) {
    showContextMenuInNode(node, coordinateInNodeX, coordinateInNodeY);

    clickOn(".context-menu #" + menuItemId);
  }

  protected void showContextMenuInNodeAndSelectItem(Node node, double coordinateInNodeX, double coordinateInNodeY, int indexOfItemToSelect) {
    showContextMenuInNode(node, coordinateInNodeX, coordinateInNodeY);

    moveBy(10, 10 + indexOfItemToSelect * 30); // TODO: is 30 really MenuItem's height?
    clickOn(MouseButton.PRIMARY);
  }

  protected boolean isAContextMenuShowing() {
    return findContextMenu() != null;
  }

  protected ContextMenu findContextMenu() {
    List<Node> lookupResult = new ArrayList<>(lookup(".context-menu").queryAll());
    int size = lookupResult.size();
    int index = 0;

    while(index < size) { // per ContextMenu a PopupControl.CSSBridge (which is protected) and a ContextMenuContent is returned
      Node node = lookupResult.get(index);
      if(node instanceof ContextMenuContent) {
        return (ContextMenu)((ContextMenuContent) node).getStyleableParent();
      }

      index++;
    }

    return findContextMenuViaMenuItems();
  }

  /**
   * Another way to find a ContextMenu: Search for classes context-menu -> menu-item and then get ContextMenu from MenuItem
   * @return
   */
  protected ContextMenu findContextMenuViaMenuItems() {
    List<Node> debug = new ArrayList<>(lookup(".context-menu .menu-item").queryAll());
    if(debug.size() > 0) {
      if(debug.get(0) instanceof ContextMenuContent.MenuItemContainer) {
        MenuItem item = ((ContextMenuContent.MenuItemContainer)debug.get(0)).getItem();
        if(item.getParentPopup() instanceof ContextMenu) {
          return (ContextMenu)item.getParentPopup();
        }
      }
    }

    return null;
  }


  protected void focusEditingAreaOfHtmlEditor(CollapsibleHtmlEditor htmledAbstract) {
    Platform.runLater(() -> htmledAbstract.setExpanded(true));
    sleep(500, TimeUnit.MILLISECONDS);

    moveTo(htmledAbstract);
    moveBy(40, 40); // move to Editing Area

    clickOn(MouseButton.PRIMARY);
  }


  private Stage getWindowWithTitle(String titleRegex) {
    return (Stage)windowFinder.window(titleRegex);
//    return (Stage)windowFinder.listOrderedWindows().stream().filter(window -> titleRegex.equals(((Stage)window).getTitle())).findFirst().get()
  }

  protected void activateWindow(Stage window) {
    Platform.runLater(() -> { // it's important to call toFront() on UI thread
      window.toFront();
      window.requestFocus();
    });
    sleep(1, TimeUnit.SECONDS);
  }

  protected Stage getMainWindow() {
    return getWindowWithTitle("PrimaryStageApplication");
  }

  protected boolean isMainWindowVisible() {
    try {
      Stage mainWindow = getMainWindow();
      return mainWindow != null && mainWindow.isShowing();
    } catch(NoSuchElementException ex) { }

    return false;
  }


  protected TabTagsControl getTabTags() {
    return lookup("#tabTags").queryFirst();
  }

  protected Node getTabCategories() {
    return lookup("#tabCategories").queryFirst();
  }

  protected boolean isTabCategoriesVisible() {
    Node tabCategories = getTabCategories();
    return tabCategories != null && tabCategories.isVisible();
  }


  protected TableView<Entry> getMainWindowTableViewEntries() {
    return lookup("#tblvwEntries").queryFirst();
  }

  protected ObservableList<Entry> getMainWindowTableViewEntriesItems() {
    return getMainWindowTableViewEntries().getItems();
  }

  protected TextField getMainWindowTextFieldSearchEntriesEntries() {
    return lookup("#txtfldSearchEntries").queryFirst();
  }

  protected ScrollPane getQuickEditEntryScrollPane() {
    return lookup("#pnQuickEditEntryScrollPane").queryFirst();
  }

  protected boolean isEntryQuickEditPaneVisible() {
    Node quickEditEntryScrollPane = getQuickEditEntryScrollPane();
    return quickEditEntryScrollPane != null && quickEditEntryScrollPane.isVisible();
  }


  protected void navigateToNewEditEntryDialog() {
    clickOn("#btnAddEntry");

    sleep(2, TimeUnit.SECONDS);
  }

  protected Stage getEditEntryDialog() {
    return getWindowWithTitle(Localization.getLocalizedString("create.entry"));
  }

  protected boolean isEditEntryDialogVisible() {
    try {
      Stage editEntryDialog = getEditEntryDialog();
      return editEntryDialog != null && editEntryDialog.isShowing();
    } catch(NoSuchElementException ex) { }

    return false;
  }


  protected CollapsibleHtmlEditor getEntryDialogHtmlEditorAbstract() {
    return lookup("#htmledAbstract").queryFirst();
  }

  protected boolean isEntryDialogHtmlEditorAbstractVisible() {
    CollapsibleHtmlEditor htmlEditorAbstract = getEntryDialogHtmlEditorAbstract();
    return htmlEditorAbstract != null && htmlEditorAbstract.isVisible();
  }

  protected CollapsibleHtmlEditor getEntryDialogHtmlEditorContent() {
    return lookup("#htmledContent").queryFirst();
  }

  protected boolean isEntryDialogHtmlEditorContentVisible() {
    CollapsibleHtmlEditor htmlEditorContent = getEntryDialogHtmlEditorContent();
    return htmlEditorContent != null && htmlEditorContent.isVisible();
  }

  protected EntryTagsControl getEntryDialogTagsControl() {
    return lookup("#editEntryDialogTagsControl").queryFirst();
  }

  protected boolean isEntryDialogTagsControlVisible() {
    EntryTagsControl entryTagsControl = getEntryDialogTagsControl();
    return entryTagsControl != null && entryTagsControl.isVisible();
  }

  protected EntryCategoriesControl getEntryDialogCategoriesControl() {
    return lookup("#editEntryDialogCategoriesControl").queryFirst();
  }

  protected boolean isEntryDialogCategoriesControlVisible() {
    EntryCategoriesControl entryCategoriesControl = getEntryDialogCategoriesControl();
    return entryCategoriesControl != null && entryCategoriesControl.isVisible();
  }

  protected EntryReferenceControl getEntryDialogReferenceControl() {
    return lookup("#editEntryDialogReferenceControl").queryFirst();
  }

  protected boolean isEntryDialogReferenceControlVisible() {
    EntryReferenceControl entryReferenceControl = getEntryDialogReferenceControl();
    return entryReferenceControl != null && entryReferenceControl.isVisible();
  }

  protected EntryPersonsControl getEntryDialogPersonsControl() {
    return lookup("#editEntryDialogPersonsControl").queryFirst();
  }

  protected boolean isEntryDialogPersonsControlVisible() {
    EntryPersonsControl entryPersonsControl = getEntryDialogPersonsControl();
    return entryPersonsControl != null && entryPersonsControl.isVisible();
  }

  protected FilesControl getEntryDialogFilesControl() {
    return lookup("#editEntryDialogFilesControl").queryFirst();
  }

  protected boolean isEntryDialogFilesControlVisible() {
    FilesControl filesControl = getEntryDialogFilesControl();
    return filesControl != null && filesControl.isVisible();
  }

}
