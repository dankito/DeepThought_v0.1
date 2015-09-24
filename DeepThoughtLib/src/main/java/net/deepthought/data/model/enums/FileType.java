package net.deepthought.data.model.enums;

import net.deepthought.Application;
import net.deepthought.data.persistence.db.TableConfig;

import java.util.HashMap;
import java.util.Map;

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


  public static void resetStaticCaches() {
    defaultFileType = null;
    imageFileType = null;
  }

  protected static FileType defaultFileType = null;

  public static FileType getDefaultFileType() {
    if(defaultFileType == null && Application.getDeepThought() != null) { // TODO: bad solution as then FileLink's FileType stays null
      for(FileType fileType : Application.getDeepThought().getFileTypes()) {
        if("file.type.other.files".equals(fileType.nameResourceKey)) {
          defaultFileType = fileType;
          break;
        }
      }
    }

    return defaultFileType;
  }

  protected static FileType imageFileType = null;

  public static FileType getImageFileType() {
    if(imageFileType == null && Application.getDeepThought() != null) { // TODO: bad solution as then FileLink's FileType stays null
      for(FileType fileType : Application.getDeepThought().getFileTypes()) {
        if("file.type.image".equals(fileType.nameResourceKey)) {
          imageFileType = fileType;
          break;
        }
      }
    }

    return imageFileType;
  }

  protected static Map<String, FileType> detectedFileTypesForResourceKeys = new HashMap<>();

  public static FileType getForResourceKey(String resourceKey) {
    if(detectedFileTypesForResourceKeys.containsKey(resourceKey))
      return detectedFileTypesForResourceKeys.get(resourceKey);

    if(Application.getDeepThought() != null) {
      for(FileType fileType : Application.getDeepThought().getFileTypes()) {
        if(resourceKey.equals(fileType.nameResourceKey)) {
          detectedFileTypesForResourceKeys.put(resourceKey, fileType);
          return fileType;
        }
      }
    }
    return getDefaultFileType();
  }
}
