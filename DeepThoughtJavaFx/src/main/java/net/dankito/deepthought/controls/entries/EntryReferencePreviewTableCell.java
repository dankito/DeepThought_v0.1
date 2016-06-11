package net.dankito.deepthought.controls.entries;

import net.dankito.deepthought.controls.ICleanUp;
import net.dankito.deepthought.data.model.Entry;
import net.dankito.deepthought.data.model.enums.ApplicationLanguage;
import net.dankito.deepthought.util.localization.LanguageChangedListener;
import net.dankito.deepthought.util.localization.Localization;

/**
 * Created by ganymed on 28/11/14.
 */
public class EntryReferencePreviewTableCell extends EntryTableCell implements ICleanUp {

  public EntryReferencePreviewTableCell() {
    Localization.addLanguageChangedListener(languageChangedListener);
  }

  @Override
  public void cleanUp() { // TODO: not called right now
    Localization.removeLanguageChangedListener(languageChangedListener);
  }


  @Override
  protected String getTextRepresentationForCell(Entry entry) {
    if(entry != null)
      return entry.getReferenceOrPersonsPreview();
    else
      return "";
  }


  protected LanguageChangedListener languageChangedListener = new LanguageChangedListener() {
    @Override
    public void languageChanged(ApplicationLanguage newLanguage) {
      EntryReferencePreviewTableCell.this.languageChanged();
    }
  };

  protected void languageChanged() {
    if(entry != null) {
      entry.resetReferencePreview();
      entryChanged(entry); // show updated Entry's Reference preview (has Date format may has changed)
    }
  }

}

