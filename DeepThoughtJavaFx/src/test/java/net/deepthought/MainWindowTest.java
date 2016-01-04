package net.deepthought;

import net.deepthought.data.model.Entry;
import net.deepthought.data.model.settings.UserDeviceSettings;

import org.junit.Ignore;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import javafx.scene.Node;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import static org.hamcrest.CoreMatchers.not;
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

    UserDeviceSettings userSettings = Application.getApplication().getLastLoggedOnUser().getSettings();
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

    UserDeviceSettings userSettings = Application.getApplication().getLastLoggedOnUser().getSettings();
    userSettings.setShowCategories(true);
    sleep(3, TimeUnit.SECONDS);

    assertThat(isTabCategoriesVisible(), is(true));

    clickOn("#mnitmMainMenuView");
    clickOn("#mnitmMainMenuView #chkmnitmViewShowCategories");
    sleep(3, TimeUnit.SECONDS);

    assertThat(isTabCategoriesVisible(), is(false));
    assertThat(userSettings.showCategories(), is(false));
  }

  protected boolean isTabCategoriesVisible() {
    Node tabCategories = getTabCategories();
    return tabCategories != null && tabCategories.isVisible();
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
