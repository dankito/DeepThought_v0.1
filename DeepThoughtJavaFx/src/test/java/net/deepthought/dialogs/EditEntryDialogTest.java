package net.deepthought.dialogs;

import net.deepthought.UiTestBase;
import net.deepthought.controls.html.CollapsibleHtmlEditor;
import net.deepthought.data.model.Entry;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * Created by ganymed on 20/12/15.
 */
public class EditEntryDialogTest extends UiTestBase {

  protected static final String EntryAbstract = "Nelson Mandela";
  protected static final String EntryContent = "A great Person of our times";


//  @Test
  public void createNewEntry_EntryGetsCorrectlyAddedToDeepThought() {
    navigateToNewEditEntryDialog();

    CollapsibleHtmlEditor htmledAbstract = getEntryDialogHtmlEditorAbstract();
    htmledAbstract.setExpanded(true);
    htmledAbstract.setHtml(EntryAbstract);

    CollapsibleHtmlEditor htmledContent = getEntryDialogHtmlEditorContent();
    htmledContent.setHtml(EntryContent);

    clickOk();

//    assertThat(deepThought.getEntries().size(), is(CountDefaultEntries + 1));
    Entry createdEntry = ((List<Entry>)deepThought.getEntries()).get(0);

    assertThat(createdEntry.getAbstractAsPlainText(), is(EntryAbstract));
    assertThat(createdEntry.getContentAsPlainText(), is(EntryContent));
  }

}
