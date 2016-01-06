package net.deepthought.dialogs;

import net.deepthought.UiTestBase;
import net.deepthought.controls.html.CollapsibleHtmlEditor;
import net.deepthought.data.model.Entry;

import org.junit.Ignore;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javafx.application.Platform;
import javafx.scene.input.MouseButton;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * Created by ganymed on 20/12/15.
 */
@Ignore
public class EditEntryDialogTest extends UiTestBase {

  protected static final String EntryAbstract = "Nelson Mandela";
  protected static final String EntryContent = "A great Person of our times";


  @Test
  public void openEditEntryDialog_PressOk_EntryGetsCorrectlyAddedToDeepThought() {
    navigateToNewEditEntryDialog();
    fillEditEntryDialogWithTestData();

    clickOk();

    assertThat(deepThought.countEntries(), is(CountDefaultEntries + 1));
    Entry createdEntry = ((List<Entry>)deepThought.getEntries()).get(0); // TODO: get Entry by tblvwEntries

    assertThat(createdEntry.isPersisted(), is(true));
    deepThought.removeEntry(createdEntry); // clean up

    assertThat(isEditEntryDialogVisible(), is(false));
    assertThatEntryDataHasBeenSetCorrectly(createdEntry);
  }


  @Test
  public void openEditEntryDialog_PressApply_EntryGetsCorrectlyAddedToDeepThought() {
    navigateToNewEditEntryDialog();
    fillEditEntryDialogWithTestData();
    sleep(4, TimeUnit.SECONDS);

    clickApply();

    assertThat(deepThought.countEntries(), is(CountDefaultEntries + 1));
    Entry createdEntry = ((List<Entry>)deepThought.getEntries()).get(0); // TODO: get Entry by tblvwEntries

    assertThat(createdEntry.isPersisted(), is(true));
    deepThought.removeEntry(createdEntry); // clean up

    assertThat(isEditEntryDialogVisible(), is(true));
    assertThatEntryDataHasBeenSetCorrectly(createdEntry);
  }

  @Test
  public void openEditEntryDialog_PressCancel_NoEntryGetsAddedToDeepThought() {
    navigateToNewEditEntryDialog();
    fillEditEntryDialogWithTestData();

    clickCancel();

    assertThat(isEditEntryDialogVisible(), is(false));
    assertThat(deepThought.countEntries(), is(CountDefaultEntries));
  }


  protected void fillEditEntryDialogWithTestData() {
    CollapsibleHtmlEditor htmledAbstract = getEntryDialogHtmlEditorAbstract();
    focusEditingAreaOfHtmlEditor(htmledAbstract);
    write(EntryAbstract);

    CollapsibleHtmlEditor htmledContent = getEntryDialogHtmlEditorContent();
    focusEditingAreaOfHtmlEditor(htmledContent);
    write(EntryContent);
  }

  protected void assertThatEntryDataHasBeenSetCorrectly(Entry createdEntry) {
    assertThat(createdEntry.getAbstractAsPlainText(), is(EntryAbstract));
    assertThat(createdEntry.getContentAsPlainText(), is(EntryContent));
  }

}
