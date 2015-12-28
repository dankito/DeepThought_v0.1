package net.deepthought;

import net.deepthought.data.model.Entry;

import org.junit.Ignore;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

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
