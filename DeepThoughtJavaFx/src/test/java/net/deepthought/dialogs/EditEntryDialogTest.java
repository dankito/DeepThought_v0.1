package net.deepthought.dialogs;

import net.deepthought.UiTestBase;
import net.deepthought.controls.categories.EntryCategoriesControl;
import net.deepthought.controls.file.FilesControl;
import net.deepthought.controls.html.CollapsibleHtmlEditor;
import net.deepthought.controls.person.EntryPersonsControl;
import net.deepthought.controls.reference.EntryReferenceControl;
import net.deepthought.data.model.Entry;
import net.deepthought.data.model.Person;
import net.deepthought.data.model.ReferenceBase;

import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javafx.application.Platform;
import javafx.geometry.VerticalDirection;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * Created by ganymed on 20/12/15.
 */
@Ignore
public class EditEntryDialogTest extends UiTestBase {

  protected static final String EntryAbstract = "Nelson Mandela";
  protected static final String EntryContent = "A great Person of our times";

  protected static final String ThreeExistingTags = "münchen,flüchtlinge,csu";
  protected static final String ExistingReference = "agora42";
  protected static final String ExistingPerson = "Augustinus";
  protected static final String ExistingFile = "Augustinus";


  @Test
  public void openEditEntryDialog_PressOk_EntryGetsCorrectlyAddedToDeepThought() {
    addTestEntities_ThenPressOk(false);
  }

  protected void addTestEntities_ThenPressOk(boolean createNewEntities) {
    fillEditEntryDialogWithTestData(createNewEntities);

    clickOk();

    assertThatEntryGotPersistedCorrectly(false);
  }


  @Test
  public void openEditEntryDialog_PressApply_EntryGetsCorrectlyAddedToDeepThought() {
    fillEditEntryDialogWithTestData(false);

    clickApply();

    assertThatEntryGotPersistedCorrectly(true);
  }

  @Test
  public void openEditEntryDialog_PressCancel_NoEntryGetsAddedToDeepThought() {
    fillEditEntryDialogWithTestData(false);

    clickCancel();

    assertThat(isEditEntryDialogVisible(), is(false));
    assertThat(deepThought.countEntries(), is(CountDefaultEntries));
  }


  protected void assertThatEntryGotPersistedCorrectly(boolean shouldEditEntryDialogBeVisible) {
    assertThat(deepThought.countEntries(), is(CountDefaultEntries + 1));

    Entry createdEntry = getMainWindowTableViewEntriesItems().get(0);
    createdEntities.add(createdEntry);

    assertThat(createdEntry.isPersisted(), is(true));

    assertThat(isEditEntryDialogVisible(), is(shouldEditEntryDialogBeVisible));
    assertThatEntryDataHasBeenSetCorrectly(createdEntry);

    removeEntityFromDeepThoughtAndAssertItGotCleanedUpWell(createdEntry);
    createdEntities.remove(createdEntry);
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
    CollapsibleHtmlEditor htmledAbstract = getEntryDialogHtmlEditorAbstract();
    focusEditingAreaOfHtmlEditor(htmledAbstract);
    write(EntryAbstract);

    CollapsibleHtmlEditor htmledContent = getEntryDialogHtmlEditorContent();
    focusEditingAreaOfHtmlEditor(htmledContent);
    write(EntryContent);

    final TextField txtfldSearchTags = getSearchAndSelectTagsControlSearchTextBox();
    clickOn(txtfldSearchTags);

    Platform.runLater(() -> txtfldSearchTags.setText(ThreeExistingTags));
    sleep(2, TimeUnit.SECONDS);
    push(KeyCode.ENTER);
    sleep(500);


    final EntryCategoriesControl categoriesControl = getEntryDialogCategoriesControl();
    Platform.runLater(() -> {
      categoriesControl.setVisible(true);
      categoriesControl.setExpanded(true);
    });
    sleep(500);
    scroll(20, VerticalDirection.DOWN);

    moveTo(getEntryDialogCategoriesControlCreateCategoryButton());

    moveBy(-50, 20);
    sleep(1000);
    doubleClickOn(MouseButton.PRIMARY);
    sleep(1000);
    moveBy(0, 20);
    doubleClickOn(MouseButton.PRIMARY);
    sleep(1, TimeUnit.SECONDS);


    // TODO: set a ReferenceSubDivision
    final EntryReferenceControl referenceControl = getEntryDialogReferenceControl();
    Platform.runLater(() -> referenceControl.setVisible(true));
    sleep(500);
    scroll(30, VerticalDirection.DOWN);

    TextField txtfldSearchForReference = getSearchAndSelectReferenceControlSearchTextBox();
    clickOn(txtfldSearchForReference);

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
    fillEditEntryDialogWithTestDataWithoutCreatingNewEntities();


    clickOn(getEntryDialogReferenceControlNewOrEditButton());
    sleep(2, TimeUnit.SECONDS);
    clickCancel();
  }

  protected void assertThatEntryDataHasBeenSetCorrectly(Entry createdEntry) {
    assertThat(createdEntry.getAbstractAsPlainText(), is(EntryAbstract));
    assertThat(createdEntry.getContentAsPlainText(), is(EntryContent));

    assertThat(createdEntry.getTags().size(), is(3));
    assertThat(createdEntry.getTagsPreview(), is("CSU, Flüchtlinge, München"));

    assertThat(createdEntry.getCategories().size(), is(2)); // TODO: test if correct Categories have been added

    assertThat(createdEntry.getSeries(), notNullValue());
    assertThat(createdEntry.getSeries().getTitle(), is("agora42"));
    assertThat(createdEntry.getReference(), nullValue());
    assertThat(createdEntry.getReferenceSubDivision(), nullValue());

    List<Person> persons = new ArrayList<>(createdEntry.getPersons());
    assertThat(persons.size(), is(1));
    assertThat(persons.get(0).getLastName(), is("Augustinus"));
  }

}
