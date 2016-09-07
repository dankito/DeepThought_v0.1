package net.dankito.deepthought.dialogs;

import net.dankito.deepthought.UiTestBase;
import net.dankito.deepthought.controls.categories.EntryCategoriesControl;
import net.dankito.deepthought.controls.file.FilesControl;
import net.dankito.deepthought.controls.html.CollapsibleHtmlEditor;
import net.dankito.deepthought.controls.person.EntryPersonsControl;
import net.dankito.deepthought.controls.reference.EntryReferenceControl;
import net.dankito.deepthought.data.model.Category;
import net.dankito.deepthought.data.model.Entry;
import net.dankito.deepthought.data.model.Person;
import net.dankito.deepthought.data.model.ReferenceBase;
import net.dankito.deepthought.data.model.Tag;

import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.VerticalDirection;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * Created by ganymed on 20/12/15.
 */
@Ignore
public class EditEntryDialogTest extends UiTestBase {

  /*        Existing Data       */

  protected static final String EntryAbstract = "Nelson Mandela";
  protected static final String EntryContent = "A great Person of our times";

  protected static final String ThreeExistingTags = "münchen,flüchtlinge,csu";
  protected static final String ExistingTagForPressingButtonCreateTag = "2015";
  protected static final String ExistingTagForDoubleClickingOnItem = "postillon";
  protected static final String ExistingTagForSelectingCheckBoxOnItem = "zitat";

  protected static final String ExistingReference = "agora42";
  protected static final String ExistingPerson = "Augustinus";
  protected static final String ExistingFile = "Augustinus";


  /*        Create new Entities       */

  protected static final String NewTag1Name = "South Africa";
  protected static final String NewTag2Name = "Apartheid";

  protected static final String NewCategory1Name = "Colonialism";
  protected static final String NewCategory1Description = "A demo description";
  protected static final String NewCategory2Name = "We are not the good ones";
  protected static final String NewCategory2Description = "Another demo description";



  @Test
  public void fillEditEntryDialogWithTestData_PressOk_EntryGetsCorrectlyAddedToDeepThought() {
    addTestEntities_ThenPressOk(false);
  }

  @Test
  public void fillEditEntryDialogWithTestDataAlsoCreateNewEntities_PressOk_EntryGetsCorrectlyAddedToDeepThought() {
    addTestEntities_ThenPressOk(true);
  }

  protected void addTestEntities_ThenPressOk(boolean createNewEntities) {
    fillEditEntryDialogWithTestData(createNewEntities);

    clickOk();

    assertThatEntryGotPersistedCorrectly(createNewEntities, false);
  }


  @Test
  public void fillEditEntryDialogWithTestData_PressApply_EntryGetsCorrectlyAddedToDeepThought() {
    addTestEntities_ThenPressApply(false);
  }

  @Test
  public void fillEditEntryDialogWithTestDataAlsoCreateNewEntities_PressApply_EntryGetsCorrectlyAddedToDeepThought() {
    addTestEntities_ThenPressApply(true);
  }

  protected void addTestEntities_ThenPressApply(boolean createNewEntities) {
    fillEditEntryDialogWithTestData(createNewEntities);

    clickApply();

    assertThatEntryGotPersistedCorrectly(createNewEntities, true);
  }


  @Test
  public void fillEditEntryDialogWithTestData_PressCancel_NoEntryGetsAddedToDeepThought() {
    addTestEntities_ThenPressCancel(false);
  }

  @Test
  public void fillEditEntryDialogWithTestDataAlsoCreateNewEntities_PressCancel_NoEntryGetsAddedToDeepThought() {
    addTestEntities_ThenPressCancel(true);
  }

  protected void addTestEntities_ThenPressCancel(boolean createNewEntities) {
    fillEditEntryDialogWithTestData(createNewEntities);

    clickCancel();

    assertThat(isEditEntryDialogVisible(), is(false));
    assertThat(deepThought.getCountEntries(), is(CountDefaultEntries));
  }


  protected void fillEditEntryDialogWithTestData(boolean createNewEntities) {
    sleep(3, TimeUnit.SECONDS);
    navigateToNewEditEntryDialog();

    if(createNewEntities) {
      fillEditEntryDialogWithTestDataAndCreateNewEntities();
    }
    else {
      fillEditEntryDialogWithTestDataWithoutCreatingNewEntities();
    }
  }

  protected void fillEditEntryDialogWithTestDataWithoutCreatingNewEntities() {
    setEntryContentAndAbstract();

    final TextField txtfldSearchTags = moveToTextFieldSearchTags();
    addExistingTags(txtfldSearchTags);


    EntryCategoriesControl categoriesControl = moveToCategoriesControl();
    addExistingCategories();

    moveTo(getEntryDialogCategoriesControlAddTopLevelCategoryButton());
    moveBy(20, 0);
    sleep(100);


    // TODO: set a ReferenceSubDivision
    TextField txtfldSearchForReference = moveToEntryReferenceControlSearchTextBox();

    Platform.runLater(() -> txtfldSearchForReference.setText(ExistingReference));
    sleep(1, TimeUnit.SECONDS);

    ListView<ReferenceBase> lstvwReferences = getSearchAndSelectReferenceControlListViewReferences();

    doubleClickOnCoordinateInNode(lstvwReferences, 2, 2, MouseButton.PRIMARY);
    sleep(1, TimeUnit.SECONDS);


    final EntryPersonsControl personsControl = getEntryDialogPersonsControl();
    Platform.runLater(() -> {
      personsControl.setVisible(true);
      personsControl.setExpanded(true);
    });
    sleep(500);
    scroll(30, VerticalDirection.DOWN);

    TextField txtfldSearchForPerson = getSearchAndSelectPersonControlSearchTextBox();
    clickOn(txtfldSearchForPerson);

    Platform.runLater(() -> txtfldSearchForPerson.setText(ExistingPerson));
    sleep(2, TimeUnit.SECONDS);

    ListView<Person> lstvwPersons = getSearchAndSelectPersonControlListViewPersons();

    doubleClickOnCoordinateInNode(lstvwPersons, 2, 2, MouseButton.PRIMARY);
    sleep(1, TimeUnit.SECONDS);

    // TODO: set a File
    final FilesControl filesControl = getEntryDialogFilesControl();
    Platform.runLater(() -> {
      filesControl.setVisible(true);
      filesControl.setExpanded(true);
    });
    sleep(500);
    scroll(40, VerticalDirection.DOWN);

    clickOn(getEntryDialogFilesControlShowHideSearchPaneButton());
    scroll(30, VerticalDirection.DOWN);

    TextField txtfldSearchForFiles = getSearchAndSelectFilesControlSearchTextBox();
    clickOn(txtfldSearchForFiles);

    Platform.runLater(() -> txtfldSearchForFiles.setText(ExistingFile));
    //sleep(2, TimeUnit.SECONDS);

    // TODO: add Notes, LinkGroups, a PreviewImage, Language, Sub Entries and a Content with Embedded Images
  }

  protected void fillEditEntryDialogWithTestDataAndCreateNewEntities() {
    setEntryContentAndAbstract(); // TODO: set a different Content, one containing Images (for testing attached files)

    final TextField txtfldSearchTags = moveToTextFieldSearchTags();
    addExistingTags(txtfldSearchTags);
    createNewTags(txtfldSearchTags);


    EntryCategoriesControl categoriesControl = moveToCategoriesControl();
    addExistingCategories();
    createNewCategories();

    moveTo(getEntryDialogCategoriesControlAddTopLevelCategoryButton());
    moveBy(20, 0);
    sleep(100);


    moveToEntryReferenceControlSearchTextBox();

    clickOn(getEntryDialogReferenceControlNewOrEditButton());
    sleep(2, TimeUnit.SECONDS);
    clickCancel();
  }


  protected void setEntryContentAndAbstract() {
    CollapsibleHtmlEditor htmledAbstract = getEntryDialogHtmlEditorAbstract();
    focusEditingAreaOfHtmlEditor(htmledAbstract);
    write(EntryAbstract);

    CollapsibleHtmlEditor htmledContent = getEntryDialogHtmlEditorContent();
    focusEditingAreaOfHtmlEditor(htmledContent);
    write(EntryContent);
  }

  protected TextField moveToTextFieldSearchTags() {
    final TextField txtfldSearchTags = getSearchAndSelectTagsControlSearchTextBox();
    clickOn(txtfldSearchTags);
    return txtfldSearchTags;
  }

  protected void addExistingTags(TextField txtfldSearchTags) {
    // add existing Tags via Pressing Enter
    Platform.runLater(() -> txtfldSearchTags.setText(ThreeExistingTags));
    sleep(1, TimeUnit.SECONDS);
    push(KeyCode.ENTER);
    sleep(500);

    // add existing Tags via Pressing Button CreateTag
    Platform.runLater(() -> txtfldSearchTags.setText(ExistingTagForPressingButtonCreateTag));
    sleep(1, TimeUnit.SECONDS);
    clickOn(getSearchAndSelectTagsControlCreateTagButton());
    sleep(500);

    // add existing Tags via Double Clicking on ListView Item
    Platform.runLater(() -> txtfldSearchTags.setText(ExistingTagForDoubleClickingOnItem));
    sleep(1, TimeUnit.SECONDS);
    moveTo(getSearchAndSelectTagsControlSearchTextBox());
    moveBy(0, 30);
    doubleClickOn(MouseButton.PRIMARY);
    sleep(500);

    // add existing Tags via Selecting CheckBox of ListView Item
    Platform.runLater(() -> txtfldSearchTags.setText(ExistingTagForSelectingCheckBoxOnItem));
    sleep(1, TimeUnit.SECONDS);
    clickOn(getSearchAndSelectTagsControlListViewTagsCheckBoxIsTagSelected());
    sleep(500);
  }

  protected void createNewTags(TextField txtfldSearchTags) {
    ListView<Tag> lstvwTags = getSearchAndSelectTagsControlListViewTags();
    createTagViaEnterKeyPress(txtfldSearchTags, lstvwTags, NewTag1Name);
    createTagViaButtonNewTag(txtfldSearchTags, lstvwTags, NewTag2Name);
  }

  protected void createTagViaEnterKeyPress(TextField txtfldSearchTags, ListView<Tag> lstvwTags, final String tagName) {
    Platform.runLater(() -> txtfldSearchTags.setText(tagName));
    sleep(1, TimeUnit.SECONDS);

    clickOn(txtfldSearchTags);
    push(KeyCode.ENTER);
    sleep(500);

    Tag createdTag = lstvwTags.getItems().get(0);
    createdEntities.add(createdTag);
  }

  protected void createTagViaButtonNewTag(TextField txtfldSearchTags, ListView<Tag> lstvwTags, final String tagName) {
    Platform.runLater(() -> txtfldSearchTags.setText(tagName));
    sleep(1, TimeUnit.SECONDS);

    clickOn(getSearchAndSelectTagsControlCreateTagButton());
    sleep(500);

    Tag createdTag = lstvwTags.getItems().get(0);
    createdEntities.add(createdTag);
  }

  protected EntryCategoriesControl moveToCategoriesControl() {
    final EntryCategoriesControl categoriesControl = getEntryDialogCategoriesControl();
    Platform.runLater(() -> {
      categoriesControl.setVisible(true);
      categoriesControl.setExpanded(true);
    });
    sleep(500);
    scroll(20, VerticalDirection.DOWN);
    return categoriesControl;
  }

  protected void addExistingCategories() {
    moveTo(getEntryDialogCategoriesControlAddTopLevelCategoryButton());

    moveBy(-100, 30);
    sleep(200);
    doubleClickOn(MouseButton.PRIMARY);
    sleep(200);
    moveBy(0, 50);
    doubleClickOn(MouseButton.PRIMARY);
    sleep(1, TimeUnit.SECONDS);
  }

  protected void createNewCategories() {
    TreeView<Category> trvwCategories = getEntryDialogCategoriesControlTreeViewCategories();

    createNewTopLevelCategory(trvwCategories, NewCategory1Name, NewCategory1Description);

    ObservableList<TreeItem<Category>> topLevelCategoryItems = trvwCategories.getRoot().getChildren();
    TreeItem<Category> firstItem = topLevelCategoryItems.get(0);

    createSubCategoryOfCategory(trvwCategories, firstItem, NewCategory2Name, NewCategory2Description);
  }

  protected void createNewTopLevelCategory(TreeView<Category> trvwCategories, String categoryName, String categoryDescription) {
    clickOn(getEntryDialogCategoriesControlAddTopLevelCategoryButton());
    fillInEditCategoryDialog(categoryName, categoryDescription);

    TreeItem<Category> root = trvwCategories.getRoot();
    ObservableList<TreeItem<Category>> topLevelCategoryItems = root.getChildren();

    Category createdCategory = topLevelCategoryItems.get(topLevelCategoryItems.size() - 1).getValue();
    createdEntities.add(createdCategory);
  }

  protected void createSubCategoryOfCategory(TreeView<Category> trvwCategories, TreeItem<Category> firstItem, String categoryName, String categoryDescription) {
    scrollToTreeViewItem(trvwCategories, firstItem);
    sleep(300);

    clickOn(getEntryDialogCategoriesControlTreeItemAddSubCategoryButtonForItem(firstItem));
    sleep(300);

    TreeItem<Category> createdItem = firstItem.getChildren().get(firstItem.getChildren().size() - 1);
    scrollToTreeViewItem(trvwCategories, createdItem);
    sleep(200);
    clickOn(MouseButton.SECONDARY);
    sleep(100);
    clickOnContextMenuItemWithId("mnitmEditCategory");
    sleep(1, TimeUnit.SECONDS);

    fillInEditCategoryDialog(categoryName, categoryDescription);

    ObservableList<TreeItem<Category>> firstCategorySubCategoryItems = firstItem.getChildren();
    Category createdCategory = firstCategorySubCategoryItems.get(firstCategorySubCategoryItems.size() - 1).getValue();
    createdEntities.add(createdCategory);
  }

  protected void fillInEditCategoryDialog(String categoryName, String categoryDescription) {
    sleep(1, TimeUnit.SECONDS);

    TextField txtfldName = getEditCategoryDialogNameTextBox();
    txtfldName.setText(categoryName);

    TextField txtfldDescription = getEditCategoryDialogDescriptionTextBox();
    txtfldDescription.setText(categoryDescription);

    clickOn(getEditCategoryDialogOkButton());
    sleep(2, TimeUnit.SECONDS);
  }


  protected TextField moveToEntryReferenceControlSearchTextBox() {
    final EntryReferenceControl referenceControl = getEntryDialogReferenceControl();
    Platform.runLater(() -> referenceControl.setVisible(true));
    sleep(500);
    scroll(30, VerticalDirection.DOWN);

    TextField txtfldSearchForReference = getSearchAndSelectReferenceControlSearchTextBox();
    clickOn(txtfldSearchForReference);
    return txtfldSearchForReference;
  }


  protected void assertThatEntryGotPersistedCorrectly(boolean hasDataBeenCreated, boolean shouldEditEntryDialogBeVisible) {
    assertThat(deepThought.getCountEntries(), is(CountDefaultEntries + 1));

    Entry createdEntry = getMainWindowTableViewEntriesItems().get(0);
    createdEntities.add(createdEntry);

    assertThat(createdEntry.isPersisted(), is(true));

    assertThat(isEditEntryDialogVisible(), is(shouldEditEntryDialogBeVisible));

    if(hasDataBeenCreated) {
      assertThatEntryDataHasBeenSetCorrectlyForNewlyCreatedData(createdEntry);
    }
    else {
      assertThatEntryDataHasBeenSetCorrectlyForExistingData(createdEntry);
    }

    removeEntityFromDeepThoughtAndAssertItGotCleanedUpWell(createdEntry);
    createdEntities.remove(createdEntry);
  }

  protected void assertThatEntryDataHasBeenSetCorrectlyForExistingData(Entry createdEntry) {
    assertThat(createdEntry.getAbstractAsPlainText(), is(EntryAbstract));
    assertThat(createdEntry.getContentAsPlainText(), is(EntryContent));

    assertThat(createdEntry.getTags().size(), is(6));
    String debug = createdEntry.getTagsPreview();
//    assertThat(createdEntry.getTagsPreview(), is("CSU, Flüchtlinge, München"));

    assertThat(createdEntry.getCategories().size(), is(2)); // TODO: test that correct Categories have been added

    assertThat(createdEntry.getSeries(), notNullValue());
    assertThat(createdEntry.getSeries().getTitle(), is("agora42"));
    assertThat(createdEntry.getReference(), nullValue());
    assertThat(createdEntry.getReferenceSubDivision(), nullValue());

    List<Person> persons = new ArrayList<>(createdEntry.getPersons());
    assertThat(persons.size(), is(1));
    assertThat(persons.get(0).getLastName(), is("Augustinus"));
  }

  protected void assertThatEntryDataHasBeenSetCorrectlyForNewlyCreatedData(Entry createdEntry) {
    assertThat(createdEntry.getAbstractAsPlainText(), is(EntryAbstract));
    assertThat(createdEntry.getContentAsPlainText(), is(EntryContent));

    assertThat(createdEntry.getTags().size(), is(8));
    assertThat(createdEntry.getTagsPreview(), is("2015, Apartheid, CSU, Flüchtlinge, München, Postillon, South Africa, Zitat"));

    assertThat(createdEntry.getCategories().size(), is(4)); // TODO: test that correct Categories have been added
  }

}
