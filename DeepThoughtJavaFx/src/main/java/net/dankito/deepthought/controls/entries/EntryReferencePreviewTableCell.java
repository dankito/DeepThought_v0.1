package net.dankito.deepthought.controls.entries;

import net.dankito.deepthought.controls.ICleanUp;
import net.dankito.deepthought.data.model.Entry;
import net.dankito.deepthought.data.model.Person;
import net.dankito.deepthought.data.model.Reference;
import net.dankito.deepthought.data.model.ReferenceSubDivision;
import net.dankito.deepthought.data.model.SeriesTitle;
import net.dankito.deepthought.data.model.enums.ApplicationLanguage;
import net.dankito.deepthought.data.persistence.db.BaseEntity;
import net.dankito.deepthought.ui.model.IEntityPreviewService;
import net.dankito.deepthought.util.localization.LanguageChangedListener;
import net.dankito.deepthought.util.localization.Localization;

import java.util.Collection;

/**
 * Created by ganymed on 28/11/14.
 */
public class EntryReferencePreviewTableCell extends EntryTableCell implements ICleanUp {

  protected IEntityPreviewService previewService;


  public EntryReferencePreviewTableCell(IEntityPreviewService previewService) {
    this.previewService = previewService;

    Localization.addLanguageChangedListener(languageChangedListener);
  }

  @Override
  public void cleanUp() { // TODO: not called right now
    Localization.removeLanguageChangedListener(languageChangedListener);
  }


  @Override
  protected String getTextRepresentationForCell(Entry entry) {
    if(entry != null)
      return previewService.getReferenceOrPersonsPreview(entry);
    else
      return "";
  }


  @Override
  protected void entityUpdated(BaseEntity entity, String propertyName) {
    super.entityUpdated(entity, propertyName);

    if(entity instanceof SeriesTitle) {
      if(entry.getSeries() == entity) {
        entryUpdated(entry);
      }
    }
    else if(entity instanceof Reference) {
      if(entry.getReference() == entity) {
        entryUpdated(entry);
      }
    }
    else if(entity instanceof ReferenceSubDivision) {
      if(entry.getReferenceSubDivision() == entity) {
        entryUpdated(entry);
      }
    }
  }

  @Override
  protected void entryCollectionChanged(Collection<? extends BaseEntity> collection, BaseEntity addedOrRemovedEntity) {
    super.entryCollectionChanged(collection, addedOrRemovedEntity);

    if(addedOrRemovedEntity instanceof Person) {
      entryUpdated(entry);
    }
  }


  protected LanguageChangedListener languageChangedListener = new LanguageChangedListener() {
    @Override
    public void languageChanged(ApplicationLanguage newLanguage) {
      EntryReferencePreviewTableCell.this.languageChanged();
    }
  };

  protected void languageChanged() {
    if(entry != null) {
      entryChanged(entry); // show updated Entry's Reference preview (its Date format may has changed)
    }
  }


}

