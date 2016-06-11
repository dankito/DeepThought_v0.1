package net.dankito.deepthought.controls.file.cells;

import net.dankito.deepthought.data.model.FileLink;

/**
 * Created by ganymed on 24/09/15.
 */
public class FileNameTreeTableCell extends EntityFilesOverviewTreeTableCellBase {

  public FileNameTreeTableCell(net.dankito.deepthought.controls.utils.IEditedEntitiesHolder<FileLink> editedFiles) {
    super(editedFiles);
  }

  @Override
  protected String getCellTextRepresentation() {
    if(file != null)
      return file.getName();
    return "";
  }

}
