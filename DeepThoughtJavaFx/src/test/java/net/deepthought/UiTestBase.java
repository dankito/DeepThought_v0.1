package net.deepthought;

import com.sun.javafx.scene.control.skin.ContextMenuContent;

import net.deepthought.controls.NewOrEditButton;
import net.deepthought.controls.categories.EntryCategoriesControl;
import net.deepthought.controls.categories.EntryCategoryTreeCell;
import net.deepthought.controls.file.FilesControl;
import net.deepthought.controls.html.CollapsibleHtmlEditor;
import net.deepthought.controls.person.EntryPersonsControl;
import net.deepthought.controls.reference.EntryReferenceControl;
import net.deepthought.controls.tag.EntryTagsControl;
import net.deepthought.controls.utils.FXUtils;
import net.deepthought.data.model.Category;
import net.deepthought.data.model.DeepThought;
import net.deepthought.data.model.Entry;
import net.deepthought.data.model.FileLink;
import net.deepthought.data.model.Note;
import net.deepthought.data.model.Person;
import net.deepthought.data.model.Reference;
import net.deepthought.data.model.ReferenceBase;
import net.deepthought.data.model.ReferenceSubDivision;
import net.deepthought.data.model.SeriesTitle;
import net.deepthought.data.model.Tag;
import net.deepthought.data.model.enums.Language;
import net.deepthought.data.persistence.db.BaseEntity;
import net.deepthought.javafx.dialogs.mainwindow.tabs.tags.TabTagsControl;
import net.deepthought.util.localization.JavaFxLocalization;
import net.deepthought.util.localization.Localization;

import org.junit.After;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * Created by ganymed on 20/12/15.
 */
public abstract class UiTestBase extends ApplicationTest {

  private static final Logger log = LoggerFactory.getLogger(UiTestBase.class);

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


  protected List<BaseEntity> createdEntities = new ArrayList<>();


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

  @After
  public void deleteCreatedEntities() {
    for(BaseEntity createdEntity : createdEntities) {
      removeEntityFromDeepThought(createdEntity);
    }

    for(BaseEntity createdEntity : createdEntities) {
      assertEntityGotCleanedUpWell(createdEntity);
    }
  }

  @Override
  public void stop() throws Exception {
    super.stop();

    stage.close();
    sleep(1, TimeUnit.SECONDS); // give Application some time to close
  }

  protected void setupMainStage(Stage stage) throws Exception {
    setupStage(stage, "dialogs/MainWindow.fxml", 1150, 620);

    // Bug in IntelliJ (only on Linux?): Created Window doesn't get selected but stays in Background. Pressing Alt+Tab twice brings it to the foreground
//    push(KeyCode.ALT, KeyCode.TAB);
//    sleep(100);
//    push(KeyCode.ALT, KeyCode.TAB);

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

    sleep(3, TimeUnit.SECONDS); // give Stage some time to initialize
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

  protected void doubleClickOnCoordinateInNode(Node node, double coordinateInNodeX, double coordinateInNodeY, MouseButton button) {
    Point2D rowPoint = FXUtils.getNodeScreenCoordinates(node);
    moveTo(rowPoint.getX() + coordinateInNodeX, rowPoint.getY() + coordinateInNodeY);

    doubleClickOn(button);
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

    clickOnContextMenuItemWithId(menuItemId);
  }

  protected void clickOnContextMenuItemWithId(String menuItemId) {
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


  protected void scrollToTreeViewItem(TreeView treeView, TreeItem item) {
    treeView.getSelectionModel().clearSelection();
    treeView.getSelectionModel().select(item);
    treeView.scrollTo(treeView.getSelectionModel().getSelectedIndex());
  }


  protected void focusEditingAreaOfHtmlEditor(CollapsibleHtmlEditor htmledAbstract) {
    Platform.runLater(() -> htmledAbstract.setExpanded(true));
    sleep(500, TimeUnit.MILLISECONDS);

    moveTo(htmledAbstract);
    moveBy(40, 40); // move to Editing Area

    clickOn(MouseButton.PRIMARY);
  }


  private Stage getWindowWithTitle(final String titleRegex) {
    try {
//      return (Stage) windowFinder.window(titleRegex);
    return (Stage)windowFinder.listOrderedWindows().stream().filter(window -> ((Stage) window).getTitle().contains(titleRegex)).findFirst().get();
    } catch(Exception ex) { log.error("Could not find Window with titleRegex " + titleRegex, ex); }

    return null;
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


  /*      EditEntryDialog EntryTagsControl       */

  protected EntryTagsControl getEntryDialogTagsControl() {
    return lookup("#editEntryDialogTagsControl").queryFirst();
  }

  protected boolean isEntryDialogTagsControlVisible() {
    EntryTagsControl entryTagsControl = getEntryDialogTagsControl();
    return entryTagsControl != null && entryTagsControl.isVisible();
  }

  protected TextField getSearchAndSelectTagsControlSearchTextBox() {
    return lookup("#editEntryDialogTagsControl #txtfldSearchTags").queryFirst();
  }

  protected Button getSearchAndSelectTagsControlCreateTagButton() {
    return lookup("#editEntryDialogTagsControl #btnCreateTag").queryFirst();
  }

  protected ListView<Tag> getSearchAndSelectTagsControlListViewTags() {
    return lookup("#lstvwTags").queryFirst();
  }

  protected CheckBox getSearchAndSelectTagsControlListViewTagsCheckBoxIsTagSelected() {
    return lookup("#lstvwTags #chkbxIsTagSelected").queryFirst();
  }


  /*      EditEntryDialog EntryCategoriesControl       */

  protected EntryCategoriesControl getEntryDialogCategoriesControl() {
    return lookup("#editEntryDialogCategoriesControl").queryFirst();
  }

  protected boolean isEntryDialogCategoriesControlVisible() {
    EntryCategoriesControl entryCategoriesControl = getEntryDialogCategoriesControl();
    return entryCategoriesControl != null && entryCategoriesControl.isVisible();
  }

  protected Button getEntryDialogCategoriesControlAddTopLevelCategoryButton() {
    return lookup("#btnAddTopLevelCategory").queryFirst();
  }

//  protected TextField getEntryDialogCategoriesControlSearchTextBox() {
//    return lookup("#txtfldSearchCategories").queryFirst();
//  }
//
//  protected Button getEntryDialogCategoriesControlCreateCategoryButton() {
//    return lookup("#editEntryDialogCategoriesControl #trvwCategories #btnCreateCategory").queryFirst();
//  }

  protected TreeView<Category> getEntryDialogCategoriesControlTreeViewCategories() {
    return lookup("#editEntryDialogCategoriesControl #trvwCategories").queryFirst();
  }

  protected CheckBox getEntryDialogCategoriesControlTreeItemIsEntryInCategoryCheckBox() {
    return lookup("#editEntryDialogCategoriesControl #trvwCategories #isEntryInCategoryCheckBox").queryFirst();
  }

  protected Button getEntryDialogCategoriesControlTreeItemAddSubCategoryButton() {
    return lookup("#editEntryDialogCategoriesControl #trvwCategories #addSubCategoryToCategoryButton").queryFirst();
  }

  protected Button getEntryDialogCategoriesControlTreeItemAddSubCategoryButtonForItem(TreeItem<Category> item) {
    Category categoryToFind = item.getValue();

    return from(getEntryDialogCategoriesControlTreeViewCategories()).lookup(".cell").lookup((EntryCategoryTreeCell cell) -> {
      return categoryToFind.equals(cell.getItem());
    }).lookup("#addSubCategoryToCategoryButton").queryFirst();
  }


  /*      EditEntryDialog EntryReferenceControl       */

  protected EntryReferenceControl getEntryDialogReferenceControl() {
    return lookup("#editEntryDialogReferenceControl").queryFirst();
  }

  protected boolean isEntryDialogReferenceControlVisible() {
    EntryReferenceControl entryReferenceControl = getEntryDialogReferenceControl();
    return entryReferenceControl != null && entryReferenceControl.isVisible();
  }

  protected NewOrEditButton getEntryDialogReferenceControlNewOrEditButton() {
    return lookup("#btnNewOrEditReference").queryFirst();
  }

  protected TextField getSearchAndSelectReferenceControlSearchTextBox() {
    return lookup("#txtfldSearchForReference").queryFirst();
  }

  protected ListView<ReferenceBase> getSearchAndSelectReferenceControlListViewReferences() {
    return lookup("#lstvwReferences").queryFirst();
  }

  protected EntryPersonsControl getEntryDialogPersonsControl() {
    return lookup("#editEntryDialogPersonsControl").queryFirst();
  }

  protected boolean isEntryDialogPersonsControlVisible() {
    EntryPersonsControl entryPersonsControl = getEntryDialogPersonsControl();
    return entryPersonsControl != null && entryPersonsControl.isVisible();
  }

  protected TextField getSearchAndSelectPersonControlSearchTextBox() {
    return lookup("#txtfldSearchForPerson").queryFirst();
  }

  protected ListView<Person> getSearchAndSelectPersonControlListViewPersons() {
    return lookup("#lstvwPersons").queryFirst();
  }

  protected FilesControl getEntryDialogFilesControl() {
    return lookup("#editEntryDialogFilesControl").queryFirst();
  }

  protected boolean isEntryDialogFilesControlVisible() {
    FilesControl filesControl = getEntryDialogFilesControl();
    return filesControl != null && filesControl.isVisible();
  }

  protected ToggleButton getEntryDialogFilesControlShowHideSearchPaneButton() {
    return lookup("#btnShowHideSearchPane").queryFirst();
  }

  protected TextField getSearchAndSelectFilesControlSearchTextBox() {
    return lookup("#txtfldSearchForFiles").queryFirst();
  }


  /*      EditCategoryDialog      */

  protected Stage getEditCategoryDialog() {
    Stage stage = getWindowWithTitle(Localization.getLocalizedString("create.category"));

    if(stage == null) {
      String editCategoryTitle = Localization.getLocalizedString("edit.category");
      editCategoryTitle = editCategoryTitle.substring(0, editCategoryTitle.length() - 3);
      stage = getWindowWithTitle(editCategoryTitle);
    }

    return stage;
  }

  protected boolean isEditCategoryDialogVisible() {
    try {
      Stage editCategoryDialog = getEditCategoryDialog();
      return editCategoryDialog != null && editCategoryDialog.isShowing();
    } catch(NoSuchElementException ex) { }

    return false;
  }

  protected TextField getEditCategoryDialogNameTextBox() {
    return from(getEditCategoryDialog().getScene().getRoot()).lookup("#txtfldName").queryFirst();
  }

  protected TextField getEditCategoryDialogDescriptionTextBox() {
    return from(getEditCategoryDialog().getScene().getRoot()).lookup("#txtfldDescription").queryFirst();
  }

  protected Button getEditCategoryDialogOkButton() {
    return from(getEditCategoryDialog().getScene().getRoot()).lookup("#btnOk").queryFirst();
  }


  /*          Clean up        */

  protected void removeEntityFromDeepThoughtAndAssertItGotCleanedUpWell(BaseEntity entity) {
    removeEntityFromDeepThought(entity);
    assertEntityGotCleanedUpWell(entity);
  }

  protected void removeEntityFromDeepThought(BaseEntity entity) {
    if(entity instanceof Entry) {
      deepThought.removeEntry((Entry) entity);
    }
    else if(entity instanceof Tag) {
      deepThought.removeTag((Tag)entity);
    }
    else if(entity instanceof Category) {
      deepThought.removeCategory((Category) entity);
    }
    else if(entity instanceof SeriesTitle) {
      deepThought.removeSeriesTitle((SeriesTitle) entity);
    }
    else if(entity instanceof Reference) {
      deepThought.removeReference((Reference) entity);
    }
    else if(entity instanceof ReferenceSubDivision) {
      deepThought.removeReferenceSubDivision((ReferenceSubDivision) entity);
    }
    else if(entity instanceof Person) {
      deepThought.removePerson((Person) entity);
    }
    else if(entity instanceof FileLink) {
      deepThought.removeFile((FileLink) entity);
    }
    else if(entity instanceof Language) {
      deepThought.removeLanguage((Language) entity);
    }
    else if(entity instanceof Note) {
      deepThought.removeNote((Note) entity);
    }
  }

  protected void assertEntityGotCleanedUpWell(BaseEntity entity) {
    if(entity instanceof Entry) {
      assertEntryGotCleanedUpWell((Entry) entity);
    }
    else if(entity instanceof Tag) {
      assertTagGotCleanedUpWell((Tag) entity);
    }
    else if(entity instanceof Category) {
      assertCategoryGotCleanedUpWell((Category)entity);
    }
    else if(entity instanceof SeriesTitle) {
      assertSeriesTitleGotCleanedUpWell((SeriesTitle)entity);
    }
    else if(entity instanceof Reference) {
      assertReferenceGotCleanedUpWell((Reference)entity);
    }
    else if(entity instanceof ReferenceSubDivision) {
      assertReferenceSubDivisionGotCleanedUpWell((ReferenceSubDivision)entity);
    }
    else if(entity instanceof Person) {
      assertPersonGotCleanedUpWell((Person)entity);
    }
    else if(entity instanceof FileLink) {
      assertFileGotCleanedUpWell((FileLink)entity);
    }
    else if(entity instanceof Language) {
      assertLanguageGotCleanedUpWell((Language)entity);
    }
    else if(entity instanceof Note) {
      assertNoteGotCleanedUpWell((Note)entity);
    }
  }

  protected void assertEntryGotCleanedUpWell(Entry entry) {
    // check if all related entities have been removed properly
    assertThat(entry.hasTags(), is(false));
    assertThat(entry.hasCategories(), is(false));

    assertThat(entry.getParentEntry(), nullValue());
    assertThat(entry.hasSubEntries(), is(false));

    assertThat(entry.getSeries(), nullValue());
    assertThat(entry.getReference(), nullValue());
    assertThat(entry.getReferenceSubDivision(), nullValue());

    assertThat(entry.hasPersons(), is(false));
    assertThat(entry.hasNotes(), is(false));
    assertThat(entry.hasLinkGroups(), is(false));

    assertThat(entry.hasAttachedFiles(), is(false));
    assertThat(entry.hasEmbeddedFiles(), is(false));
    assertThat(entry.getPreviewImage(), nullValue());

    assertThat(entry.getLanguage(), nullValue());
    assertThat(entry.getDeepThought(), nullValue());

    assertBaseEntityGotCleanedUpWell(entry);
  }

  protected void assertTagGotCleanedUpWell(Tag tag) {
    assertThat(tag.getEntries().size(), is(0));
    assertThat(tag.getDeepThought(), nullValue());

    assertBaseEntityGotCleanedUpWell(tag);
  }

  protected void assertCategoryGotCleanedUpWell(Category category) {
    assertThat(category.getEntries().size(), is(0));
    assertThat(category.getParentCategory(), nullValue());
    assertThat(category.getSubCategories().size(), is(0));
    assertThat(category.getDeepThought(), nullValue());

    assertBaseEntityGotCleanedUpWell(category);
  }

  protected void assertSeriesTitleGotCleanedUpWell(SeriesTitle series) {
    assertThat(series.getEntries().size(), is(0));
    assertThat(series.getSerialParts().size(), is(0));
    assertThat(series.getSerialPartsSorted().size(), is(0));
    assertThat(series.getDeepThought(), nullValue());

    assertReferenceBaseGotCleanedUpWell(series);
  }

  protected void assertReferenceGotCleanedUpWell(Reference reference) {
    assertThat(reference.getEntries().size(), is(0));
    assertThat(reference.getSubDivisions().size(), is(0));
    assertThat(reference.getSubDivisionsSorted().size(), is(0));

    assertThat(reference.getSeries(), nullValue());
    assertThat(reference.getDeepThought(), nullValue());

    assertReferenceBaseGotCleanedUpWell(reference);
  }

  protected void assertReferenceSubDivisionGotCleanedUpWell(ReferenceSubDivision subDivision) {
    assertThat(subDivision.getEntries().size(), is(0));
    assertThat(subDivision.getSubDivisions().size(), is(0));

    assertThat(subDivision.getParentSubDivision(), nullValue());
    assertThat(subDivision.getReference(), nullValue());
    assertThat(subDivision.getDeepThought(), nullValue());

    assertReferenceBaseGotCleanedUpWell(subDivision);
  }

  protected void assertReferenceBaseGotCleanedUpWell(ReferenceBase referenceBase) {
    assertThat(referenceBase.getPersons().size(), is(0));
    assertThat(referenceBase.getAttachedFiles().size(), is(0));
    assertThat(referenceBase.getEmbeddedFiles().size(), is(0));
    assertThat(referenceBase.getPreviewImage(), nullValue());

    assertReferenceBaseGotCleanedUpWell(referenceBase);
  }

  protected void assertPersonGotCleanedUpWell(Person person) {
    assertThat(person.getAssociatedEntries().size(), is(0));
    assertThat(person.getAssociatedSeries().size(), is(0));
    assertThat(person.getAssociatedReferences().size(), is(0));
    assertThat(person.getAssociatedReferenceSubDivisions().size(), is(0));
    assertThat(person.getDeepThought(), nullValue());

    assertBaseEntityGotCleanedUpWell(person);
  }

  protected void assertFileGotCleanedUpWell(FileLink file) {
    assertThat(file.getEntriesAttachedTo().size(), is(0));
    assertThat(file.getEntriesEmbeddedIn().size(), is(0));

    assertThat(file.getReferenceBasesAttachedTo().size(), is(0));
    assertThat(file.getReferenceBasesEmbeddedIn().size(), is(0));

    assertThat(file.getDeepThought(), nullValue());

    assertBaseEntityGotCleanedUpWell(file);
  }

  protected void assertLanguageGotCleanedUpWell(Language language) {
    assertThat(language.getDeepThought(), nullValue());

    assertBaseEntityGotCleanedUpWell(language);
  }

  protected void assertNoteGotCleanedUpWell(Note note) {
    assertThat(note.getEntry(), nullValue());
    assertThat(note.getDeepThought(), nullValue());

    assertBaseEntityGotCleanedUpWell(note);
  }

  protected void assertBaseEntityGotCleanedUpWell(BaseEntity entity) {
    assertThat(entity.isDeleted(), is(true));
  }

}
