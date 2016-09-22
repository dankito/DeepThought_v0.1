package net.dankito.deepthought.ui.model;

import net.dankito.deepthought.data.model.Entry;
import net.dankito.deepthought.data.model.ReferenceBase;

/**
 * Created by ganymed on 22/09/16.
 */
public interface IEntityPreviewService {

  String getReferenceOrPersonsPreview(Entry entry);

  String getReferencePreview(Entry entry);

  String getPersonsPreview(Entry entry);

  String getReferenceBaseUrl(ReferenceBase referenceBase);

}
