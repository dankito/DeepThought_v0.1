package net.dankito.deepthought.ui.model;

import net.dankito.deepthought.data.model.Reference;
import net.dankito.deepthought.data.model.ReferenceBase;
import net.dankito.deepthought.data.model.ReferenceSubDivision;
import net.dankito.deepthought.util.StringUtils;

/**
 * Created by ganymed on 12/09/16.
 */
public class ReferenceBasePreviewService {

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
}
