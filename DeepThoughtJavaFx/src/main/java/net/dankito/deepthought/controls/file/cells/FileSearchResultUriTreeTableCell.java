package net.dankito.deepthought.controls.file.cells;

/**
 * Created by ganymed on 24/09/15.
 */
public class FileSearchResultUriTreeTableCell extends FileTreeTableCellBase {

  public FileSearchResultUriTreeTableCell() {
    super();
  }

  @Override
  protected String getCellTextRepresentation() {
    if(file != null)
      return file.getUriString();
    return "";
  }

}
