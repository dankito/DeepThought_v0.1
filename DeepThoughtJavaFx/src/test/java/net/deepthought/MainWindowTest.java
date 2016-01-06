package net.deepthought;

import net.deepthought.data.model.Entry;
import net.deepthought.data.model.settings.UserDeviceSettings;
import net.deepthought.data.model.settings.enums.DialogsFieldsDisplay;

import org.junit.Ignore;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * Created by ganymed on 20/12/15.
 */
@Ignore
public class MainWindowTest extends UiTestBase {

  /*      MainMenu -> View MenuItems tests    */

  @Test
  public void tabCategoriesIsHidden_CheckViewShowCategories_TabCategoriesGetsShown() {
    sleep(5, TimeUnit.SECONDS);

    UserDeviceSettings userSettings = Application.getSettings();
    userSettings.setShowCategories(false);
    sleep(3, TimeUnit.SECONDS);

    assertThat(isTabCategoriesVisible(), is(false));

    clickOn("#mnitmMainMenuView");
    clickOn("#mnitmMainMenuView #chkmnitmViewShowCategories");
    sleep(3, TimeUnit.SECONDS);

    assertThat(isTabCategoriesVisible(), is(true));
    assertThat(userSettings.showCategories(), is(true));
  }

  @Test
  public void tabCategoriesIsVisible_CheckViewShowCategories_TabCategoriesGetsHidden() {
    sleep(5, TimeUnit.SECONDS);

    UserDeviceSettings userSettings = Application.getSettings();
    userSettings.setShowCategories(true);
    sleep(3, TimeUnit.SECONDS);

    assertThat(isTabCategoriesVisible(), is(true));

    clickOn("#mnitmMainMenuView");
    clickOn("#mnitmMainMenuView #chkmnitmViewShowCategories");
    sleep(3, TimeUnit.SECONDS);

    assertThat(isTabCategoriesVisible(), is(false));
    assertThat(userSettings.showCategories(), is(false));
  }

  @Test
  public void entryQuickEditPaneIsHidden_CheckViewShowQuickEditEntryPane_EntryQuickEditPaneGetsShown() {
    sleep(5, TimeUnit.SECONDS);

    UserDeviceSettings userSettings = Application.getSettings();
    userSettings.setShowQuickEditEntryPane(false);
    sleep(3, TimeUnit.SECONDS);

    assertThat(isEntryQuickEditPaneVisible(), is(false));

    clickOn("#mnitmMainMenuView");
    clickOn("#mnitmMainMenuView #chkmnitmViewShowQuickEditEntryPane");
    sleep(3, TimeUnit.SECONDS);

    assertThat(isEntryQuickEditPaneVisible(), is(true));
    assertThat(userSettings.showEntryQuickEditPane(), is(true));
  }

  @Test
  public void entryQuickEditPaneIsVisible_CheckViewShowQuickEditEntryPane_EntryQuickEditPaneGetsHidden() {
    sleep(5, TimeUnit.SECONDS);

    UserDeviceSettings userSettings = Application.getSettings();
    userSettings.setShowQuickEditEntryPane(true);
    sleep(3, TimeUnit.SECONDS);

    assertThat(isEntryQuickEditPaneVisible(), is(true));

    clickOn("#mnitmMainMenuView");
    clickOn("#mnitmMainMenuView #chkmnitmViewShowQuickEditEntryPane");
    sleep(3, TimeUnit.SECONDS);

    assertThat(isEntryQuickEditPaneVisible(), is(false));
    assertThat(userSettings.showEntryQuickEditPane(), is(false));
  }


  /*         Tests for MenuItem View -> DialogsFieldDisplay          */

  @Test
  public void dialogsFieldsDisplayIsAll_CheckViewShowImportantOnes_OnlyImportantFieldsAreShown() {
    sleep(3, TimeUnit.SECONDS);

    UserDeviceSettings userSettings = Application.getSettings();
    userSettings.setDialogsFieldsDisplay(DialogsFieldsDisplay.All);
    userSettings.setShowCategories(true);
    sleep(1, TimeUnit.SECONDS);

    navigateToNewEditEntryDialog();

    assertThat(userSettings.getDialogsFieldsDisplay(), is(DialogsFieldsDisplay.All));
    assertThatAllFieldsAreShownInDialogs();

    activateWindow(getMainWindow());

    clickOn("#mnitmMainMenuView").sleep(200);
    moveBy(0, 20); // it's important to move mouse cursor down a little bit as otherwise accidentally Tools menu item is selected when moving mouse cursor
    clickOn("#mnitmMainMenuView #mnitmViewDialogsFieldsDisplay").sleep(200);
    clickOn("#mnitmMainMenuView #mnitmViewDialogsFieldsDisplay #chkmnitmViewDialogsFieldsDisplayShowImportantOnes");
    sleep(3, TimeUnit.SECONDS);

    assertThat(userSettings.getDialogsFieldsDisplay(), is(DialogsFieldsDisplay.ImportantOnes));
    assertThatImportantFieldsAreShownInDialogs();
  }

  @Test
  public void dialogsFieldsDisplayIsImportantOnes_CheckViewShowAll_AllFieldsAreShown() {
    sleep(3, TimeUnit.SECONDS);

    UserDeviceSettings userSettings = Application.getSettings();
    userSettings.setDialogsFieldsDisplay(DialogsFieldsDisplay.ImportantOnes);
    userSettings.setShowCategories(true);
    sleep(1, TimeUnit.SECONDS);

    navigateToNewEditEntryDialog();

    assertThat(userSettings.getDialogsFieldsDisplay(), is(DialogsFieldsDisplay.ImportantOnes));
    assertThatImportantFieldsAreShownInDialogs();

    activateWindow(getMainWindow());

    clickOn("#mnitmMainMenuView").sleep(200);
    moveBy(0, 20); // it's important to move mouse cursor down a little bit as otherwise accidentally Tools menu item is selected when moving mouse cursor
    clickOn("#mnitmMainMenuView #mnitmViewDialogsFieldsDisplay").sleep(200);
    moveBy(50, 0); // move Cursor to the right
    clickOn("#mnitmMainMenuView #mnitmViewDialogsFieldsDisplay #chkmnitmViewDialogsFieldsDisplayShowAll");
    sleep(3, TimeUnit.SECONDS);

    assertThat(userSettings.getDialogsFieldsDisplay(), is(DialogsFieldsDisplay.All));
    assertThatAllFieldsAreShownInDialogs();
  }

  protected void assertThatImportantFieldsAreShownInDialogs() {
    assertThat(isEntryDialogHtmlEditorAbstractVisible(), is(true));
    assertThat(isEntryDialogHtmlEditorContentVisible(), is(true));
    assertThat(isEntryDialogTagsControlVisible(), is(true));
    assertThat(isEntryDialogCategoriesControlVisible(), is(true));
    assertThat(isEntryDialogReferenceControlVisible(), is(false));
    assertThat(isEntryDialogPersonsControlVisible(), is(false));
    assertThat(isEntryDialogFilesControlVisible(), is(false));
  }

  protected void assertThatAllFieldsAreShownInDialogs() {
    assertThat(isEntryDialogHtmlEditorAbstractVisible(), is(true));
    assertThat(isEntryDialogHtmlEditorContentVisible(), is(true));
    assertThat(isEntryDialogTagsControlVisible(), is(true));
    assertThat(isEntryDialogCategoriesControlVisible(), is(true));
    assertThat(isEntryDialogReferenceControlVisible(), is(true));
    assertThat(isEntryDialogPersonsControlVisible(), is(true));
    assertThat(isEntryDialogFilesControlVisible(), is(true));
  }


  /*      File Menu tests           */

  @Test
  public void pressQuitMenuItem_ApplicationGetsClosed() {
    clickOn("#mnitmMainMenuViewFile").sleep(200);
    moveBy(0, 20);
    clickOn("#mnitmMainMenuViewFile #mnitmFileQuit").sleep(3, TimeUnit.SECONDS);


    assertThat(Application.isInstantiated(), is(false));
    assertThat(Application.getApplication(), nullValue());

    assertThat(isMainWindowVisible(), is(false));
  }


  /*      EntriesOverviewTests      */

  @Test
  public void searchEntries_ResultListHasCorrectSize() {
    TableView<Entry> tblvwEntries = getMainWindowTableViewEntries();
    TextField txtfldSearchEntries = getMainWindowTextFieldSearchEntriesEntries();

    assertThat(86, not(tblvwEntries.getItems().size()));

    sleep(2, TimeUnit.SECONDS);
    txtfldSearchEntries.setText("nsa");
    sleep(2, TimeUnit.SECONDS);

    assertThat(86, is(tblvwEntries.getItems().size()));
  }

}
