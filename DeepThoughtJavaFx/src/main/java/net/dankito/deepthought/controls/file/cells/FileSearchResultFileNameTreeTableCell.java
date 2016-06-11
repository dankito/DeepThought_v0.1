package net.dankito.deepthought.controls.file.cells;

/**
 * Created by ganymed on 24/09/15.
 */
public class FileSearchResultFileNameTreeTableCell extends FileTreeTableCellBase {

  public FileSearchResultFileNameTreeTableCell() {
    super();
  }

  @Override
  protected String getCellTextRepresentation() {
    if(file != null)
      return file.getName();
    return "";
  }

}
