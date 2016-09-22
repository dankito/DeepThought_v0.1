package net.dankito.deepthought.ui.model;

import net.dankito.deepthought.controls.ICleanUp;
import net.dankito.deepthought.data.model.Entry;
import net.dankito.deepthought.data.model.ReferenceBase;
import net.dankito.deepthought.data.model.Tag;

import java.util.Collection;

/**
 * Created by ganymed on 22/09/16.
 */
public interface IEntityPreviewService extends ICleanUp {

  String getReferenceOrPersonsPreview(Entry entry);

  String getReferencePreview(Entry entry);

  String getPersonsPreview(Entry entry);

  String getTagsPreview(Entry entry);

  String getTagsPreview(Collection<Tag> tags, boolean showNoTagsSetMessage);

  String getReferenceBaseUrl(ReferenceBase referenceBase);

}
