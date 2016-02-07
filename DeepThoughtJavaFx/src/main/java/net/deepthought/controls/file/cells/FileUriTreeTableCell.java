package net.deepthought.controls.file.cells;

import net.deepthought.controls.utils.IEditedEntitiesHolder;
import net.deepthought.data.model.FileLink;

/**
 * Created by ganymed on 24/09/15.
 */
public class FileUriTreeTableCell extends EntityFilesOverviewTreeTableCellBase {

  public FileUriTreeTableCell(IEditedEntitiesHolder<FileLink> editedFiles) {
    super(editedFiles);
  }

  @Override
  protected String getCellTextRepresentation() {
    if(file != null)
      return file.getUriString();
    return "";
  }

}
