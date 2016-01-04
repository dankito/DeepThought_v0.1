package net.deepthought.controls;

import net.deepthought.UiTestBase;
import net.deepthought.data.model.Entry;
import net.deepthought.data.model.Tag;
import net.deepthought.data.model.ui.SystemTag;
import net.deepthought.javafx.dialogs.mainwindow.tabs.tags.TabTagsControl;

import org.junit.Ignore;
import org.junit.Test;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;

/**
 * Created by ganymed on 02/01/16.
 */
@Ignore
public class TabTagsTests extends UiTestBase {

  protected static final int CountSystemTags = 2;

  protected static final String NewTagName = "New_Tag_Name";
  protected static final String NewTagDescription = "New_Tag_Description";

  protected static final String NewTagRenamedName = "New_Tag_Renamed";
  protected static final String NewTagRenamedDescription = "New_Tag_Renamed_Description";

  protected static final String FilterForThreeTags = "münchen,flüchtlinge,csu";
  protected static final String OtherTagAvailableForFilterForThreeTags = "süddeutsche";
  protected static final String FilterForFourTagsIncludingANotExistingOne = "münchen,flüchtlinge,halleluja,csu";
  protected static final String FilterForThreeTermsIncludingThreeAmbiguousResults = "münchen,flüchtling,csu";

  protected static final String FilterSingleTag = "münchen";
  protected static final int CountOtherTagsOnEntriesWithSingleTagFilter = 149;


  protected static int CountDefaultTags = 0; // TODO

  protected Entry testEntry = null;


  @Override
  public void start(Stage stage) throws Exception {
    super.start(stage);

    CountDefaultTags = deepThought.countTags();

    List<Entry> entries = (List<Entry>)deepThought.getEntries();
    testEntry = entries.get(0);
  }


  /*        Create Tag          */

  @Test
  public void createTag_PressOk_TagGetsAddedCorrectly() {
    assertTagWithNewTagNameDoesNotExist();
    int CountDefaultTags = deepThought.countTags();

    showEditTagDialogAndFillWithTestData();
    clickOk();

    assertThat(isEditTagDialogVisible(), is(false));
    Tag newTag = assertThatTagHasBeenCreated(CountDefaultTags);

    deepThought.removeTag(newTag); // clean up again
  }

  @Test
  public void createTag_PressApply_TagGetsAddedCorrectly() {
    assertTagWithNewTagNameDoesNotExist();
    int CountDefaultTags = deepThought.countTags();

    showEditTagDialogAndFillWithTestData();
    clickApply();

    assertThat(isEditTagDialogVisible(), is(true));
    Tag newTag = assertThatTagHasBeenCreated(CountDefaultTags);

    clickOk();
    assertThat(isEditTagDialogVisible(), is(false));

    deepThought.removeTag(newTag); // clean up again
  }

  @Test
  public void createTag_PressCancel_NoTagGetsAdded() {
    assertTagWithNewTagNameDoesNotExist();
    int CountDefaultTags = deepThought.countTags();

    showEditTagDialogAndFillWithTestData();
    clickCancel();

    assertThat(isEditTagDialogVisible(), is(false));
    assertThat(deepThought.countTags(), is(CountDefaultTags));

    quickFilterTags(NewTagName);
    assertThat(getTagsInTableViewTags().size(), is(0));
  }


  protected Tag assertThatTagHasBeenCreated(int CountDefaultTags) {
    assertThat(deepThought.countTags(), is(CountDefaultTags + 1));

    ObservableList<Tag> filteredTags = getTagsInTableViewTags();
    assertThat(filteredTags.size(), is(greaterThan(1)));

    quickFilterTags(NewTagName);
    filteredTags = getTagsInTableViewTags();
    assertThat(filteredTags.size(), is(1));

    Tag newTag = filteredTags.get(0);
    assertThat(newTag.isPersisted(), is(true));
    assertThat(newTag.getName(), is(NewTagName));
    assertThat(newTag.getDescription(), is(NewTagDescription));
    assertThat(deepThought.containsTag(newTag), is(true));

    return newTag;
  }


  /*        Delete Tag        */

  @Test
  public void removeTagByButton_TagGetsRemovedCorrectly() {
    assertTagWithNewTagNameDoesNotExist();

    Tag newTag = createAndSelectTestTag();

    getTableViewTags().getSelectionModel().select(newTag);
    clickButtonRemoveSelectedTag();

    assertThatTagHasBeenDeleted(CountDefaultTags, newTag);
  }

  @Test
  public void removeTagWithEntriesByButton_AlertDefaultButtonIsPressed_TagGetsRemovedCorrectly() {
    assertTagWithNewTagNameDoesNotExist();

    Tag newTag = createAndSelectTestTagAndAddTestEntry();

    clickButtonRemoveSelectedTag();

    assertThatTagGetsDeletedByAlertDefaultButton(newTag);
  }

  @Test
  public void removeTagWithEntriesByButton_AlertCancelButtonIsPressed_TagWillNotBeRemoved() {
    assertTagWithNewTagNameDoesNotExist();

    Tag newTag = createAndSelectTestTagAndAddTestEntry();

    clickButtonRemoveSelectedTag();
    assertThat(isAnAlertVisible(), is(true));

    clickAlertCancelButton();
    assertThat(isAnAlertVisible(), is(false));

    assertThat(newTag.isDeleted(), is(false));
    assertThat(deepThought.getTags().size(), is(CountDefaultTags + 1));
    assertThat(testEntry.hasTag(newTag), is(true));

    deepThought.removeTag(newTag); // clean up again
  }

  @Test
  public void removeTagByPressingDeleteKey_TagGetsRemovedCorrectly() {
    assertTagWithNewTagNameDoesNotExist();

    Tag newTag = createAndSelectTestTag();

    getTableViewTags().getSelectionModel().select(newTag);
    pressAndReleaseKeyOnNode(getTableViewTags(), KeyCode.DELETE);

    assertThatTagHasBeenDeleted(CountDefaultTags, newTag);
  }

  @Test
  public void removeTagWithEntriesByPressingDeleteKey_AlertDefaultButtonIsPressed_TagGetsRemovedCorrectly() {
    assertTagWithNewTagNameDoesNotExist();

    Tag newTag = createAndSelectTestTagAndAddTestEntry();

    pressAndReleaseKeyOnNode(getTableViewTags(), KeyCode.DELETE);

    assertThatTagGetsDeletedByAlertDefaultButton(newTag);
  }

  @Test
  public void removeTagViaContextMenu_TagGetsRemovedCorrectly() {
    assertTagWithNewTagNameDoesNotExist();

    Tag newTag = createAndSelectTestTag();

    getTableViewTags().getSelectionModel().select(newTag);

    showContextMenuInNodeAndSelectItemById(getTableViewTags(), 2, 40 + 2, "mnitmDelete"); // 40 for Table header
    sleep(1, TimeUnit.SECONDS);

    assertThatTagHasBeenDeleted(CountDefaultTags, newTag);
  }

  @Test
  public void removeTagWithEntriesViaContextMenu_AlertDefaultButtonIsPressed_TagGetsRemovedCorrectly() {
    assertTagWithNewTagNameDoesNotExist();

    Tag newTag = createAndSelectTestTagAndAddTestEntry();

    showContextMenuInNodeAndSelectItemById(getTableViewTags(), 2, 40 + 2, "mnitmDelete"); // 40 for Table header
    sleep(1, TimeUnit.SECONDS);

    assertThatTagGetsDeletedByAlertDefaultButton(newTag);
  }

  @Test
  public void removeTagViaDeepThought_TagIsNotInTableViewTagsAnymore() {
    assertTagWithNewTagNameDoesNotExist();

    Tag newTag = createAndSelectTestTag();

    deepThought.removeTag(newTag);

    assertThatTagHasBeenDeleted(CountDefaultTags, newTag);
  }


  protected void assertThatTagHasBeenDeleted(int CountDefaultTags, Tag newTag) {
    assertThat(deepThought.countTags(), is(CountDefaultTags));
    assertThat(deepThought.containsTag(newTag), is(false));
    assertThat(newTag.isDeleted(), is(true));

    quickFilterTags(NewTagName);
    assertThat(getTagsInTableViewTags().size(), is(0));

    clearQuickFilterTags();
    assertThat(getTagsInTableViewTags().contains(newTag), is(false));
  }

  protected void assertThatTagGetsDeletedByAlertDefaultButton(Tag newTag) {
    assertThat(isAnAlertVisible(), is(true));

    clickAlertDefaultButton();
    assertThat(isAnAlertVisible(), is(false));

    assertThatTagHasBeenDeleted(CountDefaultTags, newTag);
    assertThat(testEntry.hasTag(newTag), is(false));
  }


  /*        Rename Tag        */

  @Test
  public void renameTagViaF2KeyPress_TagGetsRenamedCorrectly() {
    assertTagWithNewTagNameDoesNotExist();

    Tag newTag = createAndSelectTestTag();

    TableView<Tag> tblvwTags = getTableViewTags();
    tblvwTags.getSelectionModel().select(newTag);
    pressAndReleaseKeyOnNode(tblvwTags, KeyCode.F2);
    sleep(2, TimeUnit.SECONDS);

    setTextFieldEditCellToValue(NewTagRenamedName);

    assertThatTagHasBeenRenamed(newTag);
    deepThought.removeTag(newTag);
  }

  @Test
  public void renameTagViaContextMenu_TagGetsRenamedCorrectly() {
    assertTagWithNewTagNameDoesNotExist();

    Tag newTag = createAndSelectTestTag();

    TableView<Tag> tblvwTags = getTableViewTags();
    tblvwTags.getSelectionModel().select(newTag);

    showContextMenuInNodeAndSelectItemById(tblvwTags, 2, 40 + 2, "mnitmRename"); // 40 for Table header
    sleep(1, TimeUnit.SECONDS);

    setTextFieldEditCellToValue(NewTagRenamedName);

    assertThatTagHasBeenRenamed(newTag);
    deepThought.removeTag(newTag);
  }

//  @Test
//  public void renameTagViaEditTagDialogController_TagGetsRenamedCorrectly() {
//    assertTagWithNewTagNameDoesNotExist();
//
//    createNewTagViaUi();
//
//    quickFilterTags(NewTagName);
//    ObservableList<Tag> filteredTags = getTagsInTableViewTags();
//    Tag newTag = filteredTags.get(0);
//
//    TableView<Tag> tblvwTags = getTableViewTags();
//    tblvwTags.getSelectionModel().select(newTag);
//
//    showContextMenuInNodeAndSelectItem(tblvwTags, 2, 40 + 2, 0); // 40 for Table header
//    sleep(1, TimeUnit.SECONDS);
//
//    TextField txtfldName = getTextFieldName();
//    txtfldName.setText(NewTagRenamedName);
//
//    TextField txtfldDescription = getTextFieldDescription();
//    txtfldDescription.setText(NewTagRenamedDescription);
//
//    clickOk();
//
//    assertThatTagHasBeenRenamed(newTag, true);
//    deepThought.removeTag(newTag);
//  }

  @Test
  public void selectAndThanClickOnTag_TableViewDoesNotChangeToEditView() {
    assertTagWithNewTagNameDoesNotExist();

    Tag newTag = createAndSelectTestTag();

    TableView<Tag> tblvwTags = getTableViewTags();
    tblvwTags.getSelectionModel().select(newTag);

    clickOnCoordinateInNode(tblvwTags, 2, 40 + 2, MouseButton.PRIMARY); // 40 for Table header
    sleep(1, TimeUnit.SECONDS);

    TextField txtfldEditCell = getTextFieldEditCell();
    assertThat(txtfldEditCell, nullValue());

    deepThought.removeTag(newTag);
  }


  protected void assertThatTagHasBeenRenamed(Tag tag) {
    assertThatTagHasBeenRenamed(tag, false);
  }

  protected void assertThatTagHasBeenRenamed(Tag tag, boolean alsoDescriptionHasBeenRenamed) {
    assertThat(tag.getName(), is(NewTagRenamedName));
    if(alsoDescriptionHasBeenRenamed) {
      assertThat(tag.getDescription(), is(NewTagRenamedDescription));
    }

    quickFilterTags(NewTagName);
    assertThat(getTagsInTableViewTags().size(), is(0));

    quickFilterTags(NewTagRenamedName);
    assertThat(getTagsInTableViewTags().size(), is(1));

    clearQuickFilterTags();
    assertThat(getTagsInTableViewTags().contains(tag), is(true));

    assertThat(deepThought.containsTag(tag), is(true));
    assertThat(tag.isDeleted(), is(false));
  }


  /*      System Tags       */

  @Test
  public void testSystemTags() {
    sleep(3, TimeUnit.SECONDS);
    TableView<Tag> tblvwTags = getTableViewTags();
    ObservableList<Tag> allTags = getTagsInTableViewTags();

    for(int i = 0; i < CountSystemTags; i++) {
      Tag systemTag = allTags.get(i);
      assertThat(systemTag instanceof SystemTag, is(true));

      tblvwTags.getSelectionModel().clearSelection();
      tblvwTags.getSelectionModel().select(systemTag);

      // SystemTags cannot be deleted and have no ContextMenu
      assertThat(getButtonRemoveSelectedTag().isDisabled(), is(true));

      showContextMenuInNode(getTableViewTags(), 2, 40 + 2 + i * 20); // 40 for Table header
      assertThat(isAContextMenuShowing(), is(false));
    }

    // simply enter any filter term -> no SystemTags may be shown
    quickFilterTags("a");
    allTags = getTagsInTableViewTags();

    for(int i = 0; i < CountSystemTags; i++) {
      Tag tag = allTags.get(i);
      assertThat(tag instanceof SystemTag, is(false));
    }
  }


  /*      Filter Tags       */

  @Test
  public void filterThreeTags_OnlyTheseThreeTagsAreShown() {
    Button btnRemoveTagsFilter = getButtonRemoveTagsFilter();
    assertThat(btnRemoveTagsFilter.isDisabled(), is(true));

    int CountDefaultTags = deepThought.countTags();

    filterTags(FilterForThreeTags);
    assertThat(btnRemoveTagsFilter.isDisabled(), is(false));

    ObservableList<Tag> filteredTags = getTagsInTableViewTags();
    assertThat(filteredTags.size(), is(3));

    TableView<Tag> tblvwTags = getTableViewTags();
    Tag firstResult = filteredTags.get(0);
    tblvwTags.getSelectionModel().select(firstResult);
    sleep(1, TimeUnit.SECONDS);
    assertThat(getMainWindowTableViewEntriesItems().size(), is(1));

    clearQuickFilterTags();
    assertThat(getTagsInTableViewTags().size(), is(4));
    assertThat(btnRemoveTagsFilter.isDisabled(), is(false));

    removeTagsFilter();
    assertThat(btnRemoveTagsFilter.isDisabled(), is(true));
    assertThat(getTagsInTableViewTags().size(), is(CountDefaultTags));
  }

  @Test
  public void filterFourTagsIncludingOneNotExistingOne_OnlyThreeTagsAreShown() {
    Button btnRemoveTagsFilter = getButtonRemoveTagsFilter();
    int CountDefaultTags = deepThought.countTags();

    filterTags(FilterForFourTagsIncludingANotExistingOne);
    assertThat(btnRemoveTagsFilter.isDisabled(), is(false));

    ObservableList<Tag> filteredTags = getTagsInTableViewTags();
    assertThat(filteredTags.size(), is(3));

    removeTagsFilter();
    assertThat(btnRemoveTagsFilter.isDisabled(), is(true));
    assertThat(getTagsInTableViewTags().size(), is(CountDefaultTags));
  }

  @Test
  public void filterThreeTagsIncludingThreeAmbiguousOnes_FiveTagsAreShown() {
    Button btnRemoveTagsFilter = getButtonRemoveTagsFilter();
    int CountDefaultTags = deepThought.countTags();

    quickFilterTags(FilterForThreeTermsIncludingThreeAmbiguousResults);
    sleep(2, TimeUnit.SECONDS);
    assertThat(btnRemoveTagsFilter.isDisabled(), is(true));
    assertThat(getTagsInTableViewTags().size(), is(5));

    filterTags(FilterForThreeTermsIncludingThreeAmbiguousResults);
    assertThat(btnRemoveTagsFilter.isDisabled(), is(false));
    assertThat(getTagsInTableViewTags().size(), is(0));

    removeTagsFilter();
    assertThat(btnRemoveTagsFilter.isDisabled(), is(true));
    assertThat(getTagsInTableViewTags().size(), is(CountDefaultTags));
  }


  /*      Click CheckBox 'Filter' on a Tag    */

  @Test
  public void clickCheckBoxFilterOnATag_FilterGetsApplied() {
    int CountDefaultTags = deepThought.countTags();

    quickFilterTags(FilterSingleTag);
    sleep(4, TimeUnit.SECONDS);

    assertThat(getTagsInTableViewTags().size(), is(1));
    Tag singleSelectedTag = getTagsInTableViewTags().get(0);

    // Click CheckBox of Tag to Filter this Tag
    getCheckBoxFilterTag().setSelected(true);
    sleep(1, TimeUnit.SECONDS);

    TabTagsControl tabTags = getTabTags();
    assertThat(tabTags.getTagsFilter().size(), is(1));
    assertThat(tabTags.getTagsFilter().contains(singleSelectedTag), is(true));

    clearQuickFilterTags();
    sleep(2, TimeUnit.SECONDS);

    assertThat(getTagsInTableViewTags().size(), is(CountOtherTagsOnEntriesWithSingleTagFilter));

    quickFilterTags(FilterSingleTag);
    sleep(2, TimeUnit.SECONDS);

    // now unselect CheckBox again
    getCheckBoxFilterTag().setSelected(false);
    sleep(1, TimeUnit.SECONDS);

    assertThat(tabTags.getTagsFilter().size(), is(0));
    assertThat(tabTags.getTagsFilter().contains(singleSelectedTag), is(false));

    clearQuickFilterTags();
    sleep(2, TimeUnit.SECONDS);

    assertThat(getTagsInTableViewTags().size(), is(CountDefaultTags));
  }

  @Test
  public void clickCheckBoxFilterOnATag_PressButtonRemoveTagsFilter_FilterGetsRemoved() {
    int CountDefaultTags = deepThought.countTags();

    quickFilterTags(FilterSingleTag);
    sleep(4, TimeUnit.SECONDS);

    assertThat(getTagsInTableViewTags().size(), is(1));
    Tag singleSelectedTag = getTagsInTableViewTags().get(0);

    // Click CheckBox of Tag to Filter this Tag
    getCheckBoxFilterTag().setSelected(true);
    sleep(1, TimeUnit.SECONDS);

    TabTagsControl tabTags = getTabTags();
    assertThat(tabTags.getTagsFilter().size(), is(1));
    assertThat(tabTags.getTagsFilter().contains(singleSelectedTag), is(true));

    clickButtonRemoveTagsFilter();
    clearQuickFilterTags();
    sleep(2, TimeUnit.SECONDS);

    assertThat(tabTags.getTagsFilter().size(), is(0));
    assertThat(tabTags.getTagsFilter().contains(singleSelectedTag), is(false));
    assertThat(getTagsInTableViewTags().size(), is(CountDefaultTags));
  }

  @Test
  public void filterThreeTags_FilterAnotherTagByCheckBox_AllFourTagsAreFiltered() {
    filterTags(FilterForThreeTags);
    sleep(2, TimeUnit.SECONDS);

    assertThat(getTagsInTableViewTags().size(), is(3));

    quickFilterTags(OtherTagAvailableForFilterForThreeTags);
    Tag singleSelectedTag = getTagsInTableViewTags().get(0);

    // Click CheckBox of Tag to Filter this Tag
    getCheckBoxFilterTag().setSelected(true);
    sleep(1, TimeUnit.SECONDS);

    clearQuickFilterTags();

    assertThat(getTagsInTableViewTags().size(), is(4));

    TabTagsControl tabTags = getTabTags();
    assertThat(tabTags.getTagsFilter().size(), is(4));
    assertThat(tabTags.getTagsFilter().contains(singleSelectedTag), is(true));
  }

  @Test
  public void filterTagByCheckBox_FilterThreeOtherTags_AllFourTagsAreFiltered() {
    quickFilterTags(OtherTagAvailableForFilterForThreeTags);
    sleep(4, TimeUnit.SECONDS);

    // Click CheckBox of Tag to Filter this Tag
    getCheckBoxFilterTag().setSelected(true);
    Tag singleSelectedTag = getTagsInTableViewTags().get(0);
    sleep(1, TimeUnit.SECONDS);

    assertThat(getTagsInTableViewTags().size(), is(1));

    filterTags(FilterForThreeTags);
    clearQuickFilterTags();
    sleep(1, TimeUnit.SECONDS);

    assertThat(getTagsInTableViewTags().size(), is(4));

    TabTagsControl tabTags = getTabTags();
    assertThat(tabTags.getTagsFilter().size(), is(4));
    assertThat(tabTags.getTagsFilter().contains(singleSelectedTag), is(true));
  }


  /*      Check if after clearing Filter SystemTag AllEntries is selected    */

  @Test
  public void clearQuickFilter_SystemTagAllEntriesIsSelected() {
    quickFilterTags(NewTagName);
    sleep(1, TimeUnit.SECONDS);

    clearQuickFilterTags();

    Tag selectedTag = getTableViewTags().getSelectionModel().getSelectedItem();
    assertThat(selectedTag, is(deepThought.AllEntriesSystemTag()));
  }

  @Test
  public void clearTagsFilter_SystemTagAllEntriesIsSelected() {
    filterTags(FilterForThreeTags);
    sleep(1, TimeUnit.SECONDS);

    removeTagsFilter();

    Tag selectedTag = getTableViewTags().getSelectionModel().getSelectedItem();
    assertThat(selectedTag, is(deepThought.AllEntriesSystemTag()));
  }



  protected void assertTagWithNewTagNameDoesNotExist() {
    sleep(2, TimeUnit.SECONDS);

    deleteAllTagsWithName(NewTagName);
    deleteAllTagsWithName(NewTagRenamedName);

    clearQuickFilterTags();
  }

  protected void deleteAllTagsWithName(String name) {
    quickFilterTags(name);
    ObservableList<Tag> tagsWithNewTagName = getTagsInTableViewTags();

    while(tagsWithNewTagName.size() > 100) { // filter result hasn't been displayed yet, still all Tags are shown -> wait some more time
      sleep(1, TimeUnit.SECONDS);
      quickFilterTags(name);
      tagsWithNewTagName = getTagsInTableViewTags();
    }

    for(Tag tag : tagsWithNewTagName) {
      deepThought.removeTag(tag);
    }
  }


  protected Tag createAndSelectTestTag() {
    createNewTagViaUi();

    quickFilterTags(NewTagName);
    ObservableList<Tag> filteredTags = getTagsInTableViewTags();
    Tag newTag = filteredTags.get(0);

    getTableViewTags().getSelectionModel().select(newTag);

    return newTag;
  }

  protected Tag createAndSelectTestTagAndAddTestEntry() {
    Tag newTag = createAndSelectTestTag();

    testEntry.addTag(newTag);

    return newTag;
  }


  protected void showEditTagDialogAndFillWithTestData() {
    clickButtonAddTag();

    TextField txtfldName = getTextFieldName();
    txtfldName.setText(NewTagName);

    TextField txtfldDescription = getTextFieldDescription();
    txtfldDescription.setText(NewTagDescription);
  }

  protected void createNewTagViaUi() {
    showEditTagDialogAndFillWithTestData();

    clickOk();
  }


  protected void clickButtonAddTag() {
    clickOn("#btnAddTag");

    sleep(1, TimeUnit.SECONDS);
  }

  protected void clickButtonRemoveSelectedTag() {
    clickOn("#btnRemoveSelectedTag");

    sleep(1, TimeUnit.SECONDS);
  }

  protected void clickButtonRemoveTagsFilter() {
    clickOn("#btnRemoveTagsFilter");

    sleep(1, TimeUnit.SECONDS);
  }

  protected void quickFilterTags(String tagsQuickFilter) {
    getTextFieldSearchTags().setText(tagsQuickFilter);
    sleep(2, TimeUnit.SECONDS);
  }

  protected void clearQuickFilterTags() {
    getTextFieldSearchTags().setText("");
    sleep(2, TimeUnit.SECONDS);
  }

  protected void filterTags(String tagsFilter) {
    quickFilterTags(tagsFilter);
    sleep(2, TimeUnit.SECONDS);

    pressAndReleaseKeyOnNode(getTextFieldSearchTags(), KeyCode.ENTER);
    sleep(2, TimeUnit.SECONDS);
  }

  protected void removeTagsFilter() {
    clickButtonRemoveTagsFilter();
    clearQuickFilterTags();
  }

  protected TextField getTextFieldSearchTags() {
    return lookup("#txtfldSearchTags").queryFirst();
  }

  protected Button getButtonRemoveTagsFilter() {
    return lookup("#btnRemoveTagsFilter").queryFirst();
  }

  protected Button getButtonRemoveSelectedTag() {
    return lookup("#btnRemoveSelectedTag").queryFirst();
  }

  protected TableView<Tag> getTableViewTags() {
    return lookup("#tblvwTags").queryFirst();
  }

  protected ObservableList<Tag> getTagsInTableViewTags() {
    return getTableViewTags().getItems();
  }

  protected CheckBox getCheckBoxFilterTag() {
    return lookup("#chkbxFilterTag").queryFirst();
  }

  protected Set<CheckBox> getAllCheckBoxesFilterTag() {
    return lookup("#chkbxFilterTag").queryAll();
  }

  protected TextField getTextFieldEditCell() {
    return lookup("#txtfldEditCell").queryFirst();
  }

  protected void setTextFieldEditCellToValue(String newValue) {
    TextField txtfldEditCell = getTextFieldEditCell();
    txtfldEditCell.setText(newValue);
    pressAndReleaseKeyOnNode(txtfldEditCell, KeyCode.ENTER);
    sleep(1, TimeUnit.SECONDS);
  }


  /*       EditTagDialogController       */

  protected boolean isEditTagDialogVisible() {
    return getTextFieldName() != null;
  }

  protected TextField getTextFieldName() {
    return lookup("#txtfldName").queryFirst();
  }

  protected TextField getTextFieldDescription() {
    return lookup("#txtfldDescription").queryFirst();
  }

}
