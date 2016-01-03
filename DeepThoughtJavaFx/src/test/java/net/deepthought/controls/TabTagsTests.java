package net.deepthought.controls;

import net.deepthought.UiTestBase;
import net.deepthought.data.model.Tag;

import org.junit.Ignore;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;

/**
 * Created by ganymed on 02/01/16.
 */
@Ignore
public class TabTagsTests extends UiTestBase {

  protected static final String NewTagName = "New_Tag_Name";
  protected static final String NewTagDescription = "New_Tag_Description";

  protected static final String NewTagRenamedName = "New_Tag_Renamed";
  protected static final String NewTagRenamedDescription = "New_Tag_Renamed_Description";

  protected static final String FilterForThreeTags = "münchen,flüchtlinge,csu";
  protected static final String FilterForFourTagsIncludingANotExistingOne = "münchen,flüchtlinge,halleluja,csu";
  protected static final String FilterForThreeTermsIncludingThreeAmbiguousResults = "münchen,flüchtling,csu";


  /*        Create Tag          */

  @Test
  public void createTag_TagGetsAddedCorrectly() {
    assertTagWithNewTagNameDoesNotExist();
    int countDefaultTags = deepThought.countTags();

    createNewTagViaUi();

    assertThat(deepThought.countTags(), is(countDefaultTags + 1));

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

    deepThought.removeTag(newTag); // clean up again
  }


  /*        Delete Tag        */

  @Test
  public void removeTagByButton_TagGetsRemovedCorrectly() {
    assertTagWithNewTagNameDoesNotExist();
    int countDefaultTags = deepThought.countTags();

    createNewTagViaUi();

    quickFilterTags(NewTagName);
    ObservableList<Tag> filteredTags = getTagsInTableViewTags();
    Tag newTag = filteredTags.get(0);

    getTableViewTags().getSelectionModel().select(newTag);
    clickButtonRemoveSelectedTag();

    assertThatTagHasBeenDeleted(countDefaultTags, newTag);
  }

  @Test
  public void removeTagByPressingDeleteKey_TagGetsRemovedCorrectly() {
    assertTagWithNewTagNameDoesNotExist();
    int countDefaultTags = deepThought.countTags();

    createNewTagViaUi();

    quickFilterTags(NewTagName);
    ObservableList<Tag> filteredTags = getTagsInTableViewTags();
    Tag newTag = filteredTags.get(0);

    getTableViewTags().getSelectionModel().select(newTag);
    pressAndReleaseKeyOnNode(getTableViewTags(), KeyCode.DELETE);

    assertThatTagHasBeenDeleted(countDefaultTags, newTag);
  }

  @Test
  public void removeTagViaContextMenu_TagGetsRemovedCorrectly() {
    assertTagWithNewTagNameDoesNotExist();
    int countDefaultTags = deepThought.countTags();

    createNewTagViaUi();

    quickFilterTags(NewTagName);
    ObservableList<Tag> filteredTags = getTagsInTableViewTags();
    Tag newTag = filteredTags.get(0);

    getTableViewTags().getSelectionModel().select(newTag);

    showContextMenuInNodeAndSelectItem(getTableViewTags(), 2, 40 + 2, 1); // 40 for Table header
    sleep(1, TimeUnit.SECONDS);

    assertThatTagHasBeenDeleted(countDefaultTags, newTag);
  }

  @Test
  public void removeTagViaDeepThought_TagIsNotInTableViewTagsAnymore() {
    assertTagWithNewTagNameDoesNotExist();
    int countDefaultTags = deepThought.countTags();

    createNewTagViaUi();

    quickFilterTags(NewTagName);
    ObservableList<Tag> filteredTags = getTagsInTableViewTags();
    Tag newTag = filteredTags.get(0);

    deepThought.removeTag(newTag);

    assertThatTagHasBeenDeleted(countDefaultTags, newTag);
  }

  protected void assertThatTagHasBeenDeleted(int countDefaultTags, Tag newTag) {
    assertThat(deepThought.countTags(), is(countDefaultTags));
    assertThat(deepThought.containsTag(newTag), is(false));
    assertThat(newTag.isDeleted(), is(true));

    quickFilterTags(NewTagName);
    assertThat(getTagsInTableViewTags().size(), is(0));

    clearQuickFilterTags();
    assertThat(getTagsInTableViewTags().contains(newTag), is(false));
  }


  /*        Rename Tag        */

  @Test
  public void renameTagViaF2KeyPress_TagGetsRenamedCorrectly() {
    assertTagWithNewTagNameDoesNotExist();

    createNewTagViaUi();

    quickFilterTags(NewTagName);
    ObservableList<Tag> filteredTags = getTagsInTableViewTags();
    Tag newTag = filteredTags.get(0);

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

    createNewTagViaUi();

    quickFilterTags(NewTagName);
    ObservableList<Tag> filteredTags = getTagsInTableViewTags();
    Tag newTag = filteredTags.get(0);

    TableView<Tag> tblvwTags = getTableViewTags();
    tblvwTags.getSelectionModel().select(newTag);

    showContextMenuInNodeAndSelectItem(tblvwTags, 2, 40 + 2, 0); // 40 for Table header
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

    createNewTagViaUi();

    quickFilterTags(NewTagName);
    ObservableList<Tag> filteredTags = getTagsInTableViewTags();
    Tag newTag = filteredTags.get(0);

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


  /*      Filter Tags       */

  @Test
  public void filterThreeTags_OnlyTheseThreeTagsAreShown() {
    Button btnRemoveTagsFilter = getButtonRemoveTagsFilter();
    assertThat(btnRemoveTagsFilter.isDisabled(), is(true));

    int countDefaultTags = deepThought.countTags();

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
    assertThat(getTagsInTableViewTags().size(), is(countDefaultTags));
  }

  @Test
  public void filterFourTagsIncludingOneNotExistingOne_OnlyThreeTagsAreShown() {
    Button btnRemoveTagsFilter = getButtonRemoveTagsFilter();
    int countDefaultTags = deepThought.countTags();

    filterTags(FilterForFourTagsIncludingANotExistingOne);
    assertThat(btnRemoveTagsFilter.isDisabled(), is(false));

    ObservableList<Tag> filteredTags = getTagsInTableViewTags();
    assertThat(filteredTags.size(), is(3));

    removeTagsFilter();
    assertThat(btnRemoveTagsFilter.isDisabled(), is(true));
    assertThat(getTagsInTableViewTags().size(), is(countDefaultTags));
  }

  @Test
  public void filterThreeTagsIncludingThreeAmbiguousOnes_FiveTagsAreShown() {
    Button btnRemoveTagsFilter = getButtonRemoveTagsFilter();
    int countDefaultTags = deepThought.countTags();

    quickFilterTags(FilterForThreeTermsIncludingThreeAmbiguousResults);
    sleep(2, TimeUnit.SECONDS);
    assertThat(btnRemoveTagsFilter.isDisabled(), is(true));
    assertThat(getTagsInTableViewTags().size(), is(5));

    filterTags(FilterForThreeTermsIncludingThreeAmbiguousResults);
    assertThat(btnRemoveTagsFilter.isDisabled(), is(false));
    assertThat(getTagsInTableViewTags().size(), is(0));

    removeTagsFilter();
    assertThat(btnRemoveTagsFilter.isDisabled(), is(true));
    assertThat(getTagsInTableViewTags().size(), is(countDefaultTags));
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

  protected void createNewTagViaUi() {
    clickButtonAddTag();

    TextField txtfldName = getTextFieldName();
    txtfldName.setText(NewTagName);

    TextField txtfldDescription = getTextFieldDescription();
    txtfldDescription.setText(NewTagDescription);

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

  protected TableView<Tag> getTableViewTags() {
    return lookup("#tblvwTags").queryFirst();
  }

  protected ObservableList<Tag> getTagsInTableViewTags() {
    return getTableViewTags().getItems();
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

  protected TextField getTextFieldName() {
    return lookup("#txtfldName").queryFirst();
  }

  protected TextField getTextFieldDescription() {
    return lookup("#txtfldDescription").queryFirst();
  }

}
