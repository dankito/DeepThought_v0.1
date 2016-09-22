package net.dankito.deepthought.ui.model;

import net.dankito.deepthought.data.html.IHtmlHelper;
import net.dankito.deepthought.data.model.Entry;
import net.dankito.deepthought.data.model.Person;
import net.dankito.deepthought.data.model.Reference;
import net.dankito.deepthought.data.model.ReferenceSubDivision;
import net.dankito.deepthought.data.model.SeriesTitle;
import net.dankito.deepthought.data.persistence.db.BaseEntity;
import net.dankito.deepthought.data.persistence.db.TableConfig;
import net.dankito.deepthought.util.StringUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by ganymed on 21/09/16.
 */
public class EntryPreviewService implements IUpdatablePreviewService {

  protected PersonPreviewService personPreviewService;

  protected IHtmlHelper htmlHelper;

  protected Map<Entry, String> cachedReferencePreviews = new ConcurrentHashMap<>();

  protected Map<Entry, String> cachedPersonPreviews = new ConcurrentHashMap<>();


  public EntryPreviewService(PersonPreviewService personPreviewService, IHtmlHelper htmlHelper) {
    this.personPreviewService = personPreviewService;
    this.htmlHelper = htmlHelper;
  }


  /*      ReferenceBases      */

  public String getReferenceOrPersonsPreview(Entry entry) {
    String referencePreview = getReferencePreview(entry);
    if(StringUtils.isNotNullOrEmpty(referencePreview)) {
      return referencePreview;
    }

    return getLongPersonsPreview(entry);
  }

  public String getReferencePreview(Entry entry) {
    String referencePreview = cachedReferencePreviews.get(entry);
    if(referencePreview == null) {
      referencePreview = determineReferencePreview(entry);
      cachedReferencePreviews.put(entry, referencePreview);
    }

    return referencePreview;
  }

  protected String determineReferencePreview(Entry entry) {
    return determineReferencePreview(entry.getSeries(), entry.getReference(), entry.getReferenceSubDivision());
  }

  protected String determineReferencePreview(SeriesTitle seriesTitle, Reference reference, ReferenceSubDivision subDivision) {
    String preview = determineSeriesTitlePreview(seriesTitle);

    String referencePreview = determineReferencePreview(reference);
    if(StringUtils.isNotNullOrEmpty(referencePreview)) {
      preview += ( (StringUtils.isNullOrEmpty(preview) ? "" : " ") + referencePreview);
    }

    String subDivisionPreview = determineReferenceSubDivisionPreview(subDivision);
    preview += (StringUtils.isNullOrEmpty(subDivisionPreview) ? "" : " - " + subDivisionPreview);

    return preview;
  }

  protected String determineSeriesTitlePreview(SeriesTitle seriesTitle) {
    String preview = "";

    if(seriesTitle != null) {
      preview = seriesTitle.getTitle();
    }

    return preview;
  }

  protected String determineReferencePreview(Reference reference) {
    String preview = "";

    if(reference != null) {
      preview = reference.getTitle();

      if(StringUtils.isNullOrEmpty(preview)) {
        if(reference.getPublishingDate() != null) {
          preview = reference.formatPublishingDate();
        }
        else if(reference.getIssueOrPublishingDate() != null) {
          preview = reference.getIssueOrPublishingDate().toString();
        }
      }
      else {
        if(reference.getSeries() == null && reference.hasPersons()) {
          preview = personPreviewService.getShortPersonsPreview(reference.getPersons()) + " - " + preview;
        }
      }
    }

    return preview;
  }

  protected String determineReferenceSubDivisionPreview(ReferenceSubDivision subDivision) {
    String preview = "";

    if(subDivision != null) {
      preview = subDivision.getTitle();
    }

    return preview;
  }

  protected void resetReferencePreview(Entry entry) {
    cachedReferencePreviews.remove(entry);
  }


  /*      Persons       */

  protected String getLongPersonsPreview(Entry entry) {
    String preview = cachedPersonPreviews.get(entry);

    if(preview == null) {
      preview = personPreviewService.getLongPersonsPreview(entry.getPersons());
      cachedPersonPreviews.put(entry, preview);
    }

    return preview;
  }

  protected void resetPersonPreview(Entry entry) {
    cachedPersonPreviews.remove(entry);
  }



  /*    AllEntitiesListener     */

  public void entityUpdated(BaseEntity entity, String propertyName) {
    if(entity instanceof Entry) {
      entryUpdated((Entry)entity, propertyName);
    }
    else if(entity instanceof SeriesTitle) {
      seriesTitleUpdated((SeriesTitle)entity, propertyName);
    }
    else if(entity instanceof Reference) {
      referenceUpdated((Reference)entity, propertyName);
    }
    else if(entity instanceof ReferenceSubDivision) {
      subDivisionUpdated((ReferenceSubDivision)entity, propertyName);
    }
  }

  protected void entryUpdated(Entry entry, String propertyName) {
    if(TableConfig.EntrySeriesTitleJoinColumnName.equals(propertyName) || TableConfig.EntryReferenceJoinColumnName.equals(propertyName) ||
        TableConfig.EntryReferenceSubDivisionJoinColumnName.equals(propertyName)) {
      resetReferencePreview(entry);
    }
  }

  protected void seriesTitleUpdated(SeriesTitle seriesTitle, String propertyName) {
    for(Entry entry : seriesTitle.getEntries()) {
      resetReferencePreview(entry);
    }
  }

  protected void referenceUpdated(Reference reference, String propertyName) {
    for(Entry entry : reference.getEntries()) {
      resetReferencePreview(entry);
    }
  }

  protected void subDivisionUpdated(ReferenceSubDivision subDivision, String propertyName) {
    for(Entry entry : subDivision.getEntries()) {
      resetReferencePreview(entry);
    }
  }


  public void entityDeleted(BaseEntity entity) {
    if(entity instanceof Entry) {
      Entry entry = (Entry)entity;
      resetReferencePreview(entry);
      resetPersonPreview(entry);
    }
  }

  public void collectionOfEntityUpdated(BaseEntity collectionHolder, BaseEntity addedOrRemovedEntity) {
    if(collectionHolder instanceof Entry) {
      collectionOfEntryUpdated((Entry)collectionHolder, addedOrRemovedEntity);
    }
  }

  protected void collectionOfEntryUpdated(Entry entry, BaseEntity addedOrRemovedEntity) {
    if(addedOrRemovedEntity instanceof Person) {
      resetPersonPreview(entry);
    }
  }


  @Override
  public void languageChanged() {
    cachedReferencePreviews.clear();
  }

}
