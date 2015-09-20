package net.deepthought.data.model.enums;

import net.deepthought.data.persistence.db.TableConfig;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;

/**
 * Created by ganymed on 20/09/15.
 */
@Entity(name = TableConfig.FileTypeTableName)
public class FileType extends ExtensibleEnumeration {

  private static final long serialVersionUID = -1765124075257854178L;


  @Column(name = TableConfig.FileTypeFolderNameColumnName)
  protected String folderName;

  @Column(name = TableConfig.FileTypeIconColumnName)
  @Lob
  protected byte[] icon = null;


  public FileType() {

  }

  public FileType(String name, String folderName) {
    super(name);
    this.folderName = folderName;
  }

  public FileType(String nameResourceKey, String folderName, boolean isSystemValue, boolean isDeletable, int sortOrder) {
    super(nameResourceKey, isSystemValue, isDeletable, sortOrder);
    this.folderName = folderName;
  }


  public String getFolderName() {
    return folderName;
  }

  public void setFolderName(String folderName) {
    this.folderName = folderName;
  }

  public byte[] getIcon() {
    return icon;
  }

  public void setIcon(byte[] icon) {
    this.icon = icon;
  }


  @Override
  public String toString() {
    return "FileType " + getTextRepresentation();
  }

}
