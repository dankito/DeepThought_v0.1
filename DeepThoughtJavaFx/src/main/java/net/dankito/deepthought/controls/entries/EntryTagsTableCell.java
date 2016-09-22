package net.dankito.deepthought.controls.entries;

import net.dankito.deepthought.Application;
import net.dankito.deepthought.data.model.Entry;
import net.dankito.deepthought.data.model.Tag;
import net.dankito.deepthought.data.persistence.db.BaseEntity;
import net.dankito.deepthought.ui.model.IEntityPreviewService;

import java.util.Collection;

/**
 * Created by ganymed on 28/11/14.
 */
public class EntryTagsTableCell extends EntryTableCell {

  protected IEntityPreviewService previewService = Application.getEntityPreviewService();


  @Override
  protected String getTextRepresentationForCell(Entry entry) {
    if(entry != null)
      return previewService.getTagsPreview(entry);
    else
      return "";
  }


  @Override
  protected void entryCollectionChanged(Collection<? extends BaseEntity> collection, BaseEntity addedOrRemovedEntity) {
    super.entryCollectionChanged(collection, addedOrRemovedEntity);

    if(addedOrRemovedEntity instanceof Tag) {
      entryUpdated(entry);
    }
  }

}

