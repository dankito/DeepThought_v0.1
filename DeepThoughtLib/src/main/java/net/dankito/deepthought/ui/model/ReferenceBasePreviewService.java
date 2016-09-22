package net.dankito.deepthought.ui.model;

import net.dankito.deepthought.data.model.Reference;
import net.dankito.deepthought.data.model.ReferenceBase;
import net.dankito.deepthought.data.model.ReferenceSubDivision;
import net.dankito.deepthought.data.model.SeriesTitle;
import net.dankito.deepthought.util.StringUtils;

/**
 * Created by ganymed on 12/09/16.
 */
public class ReferenceBasePreviewService {

  protected PersonPreviewService personPreviewService;


  public ReferenceBasePreviewService(PersonPreviewService personPreviewService) {
    this.personPreviewService = personPreviewService;
  }


  public String getReferenceBaseUrl(ReferenceBase referenceBase) {
    String url = null;

    if(StringUtils.isNotNullOrEmpty(referenceBase.getOnlineAddress())) {
      url = referenceBase.getOnlineAddress();
    }
    else if(referenceBase instanceof ReferenceSubDivision) {
      ReferenceSubDivision subDivision = (ReferenceSubDivision)referenceBase;
      if(subDivision.getReference() != null) {
        url = getReferenceBaseUrl(subDivision.getReference());
      }
    }
    else if(referenceBase instanceof Reference) {
      Reference reference = (Reference)referenceBase;
      if(reference.getSeries() != null) {
        url = getReferenceBaseUrl(reference.getSeries());
      }
    }

    return url;
  }



  public String getReferencePreview(SeriesTitle seriesTitle, Reference reference, ReferenceSubDivision subDivision) {
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

}
