package net.deepthought.data.model;

import net.deepthought.data.persistence.db.TableConfig;
import net.deepthought.data.persistence.db.UserDataEntity;
import net.deepthought.util.Localization;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

/**
 * Created by ganymed on 16/12/14.
 */
@Entity(name = TableConfig.FileLinkTableName)
public class FileLink extends UserDataEntity implements Serializable {

  private static final long serialVersionUID = -7508656557829870722L;


  @Column(name = TableConfig.FileLinkUriColumnName)
  protected String uriString;

  @Column(name = TableConfig.FileLinkNameColumnName)
  protected String name;

  @Column(name = TableConfig.FileLinkIsFolderColumnName)
  protected boolean isFolder;

  @Column(name = TableConfig.FileLinkNotesColumnName)
  protected String notes;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = TableConfig.FileLinkEntryJoinColumnName)
  protected Entry entry;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = TableConfig.FileLinkReferenceBaseJoinColumnName)
  protected ReferenceBase referenceBase;


  public FileLink() {

  }

  public FileLink(String uri) {
    this.uriString = uri;
  }

  public FileLink(String uri, String name) {
    this(uri);
    this.name = name;
  }

  public FileLink(String uri, String name, boolean isFolder) {
    this(uri, name);
    this.isFolder = isFolder;
  }


  public String getUriString() {
    return uriString;
  }

  public void setUriString(String uriString) {
    Object previousValue = this.uriString;
    this.uriString = uriString;
    callPropertyChangedListeners(TableConfig.FileLinkUriColumnName, previousValue, uriString);
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    Object previousValue = this.name;
    this.name = name;
    callPropertyChangedListeners(TableConfig.FileLinkNameColumnName, previousValue, name);
  }

  public boolean isFolder() {
    return isFolder;
  }

  public void setIsFolder(boolean isFolder) {
    Object previousValue = this.isFolder;
    this.isFolder = isFolder;
    callPropertyChangedListeners(TableConfig.FileLinkIsFolderColumnName, previousValue, isFolder);
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    Object previousValue = this.notes;
    this.notes = notes;
    callPropertyChangedListeners(TableConfig.FileLinkNotesColumnName, previousValue, notes);
  }

  public Entry getEntry() {
    return entry;
  }

  protected void setEntry(Entry entry) {
    Object previousValue = this.entry;
    this.entry = entry;
    callPropertyChangedListeners(TableConfig.FileLinkEntryJoinColumnName, previousValue, entry);
  }

  protected void setReferenceBase(ReferenceBase referenceBase) {
    Object previousValue = this.referenceBase;
    this.referenceBase = referenceBase;
    callPropertyChangedListeners(TableConfig.FileLinkReferenceBaseJoinColumnName, previousValue, referenceBase);
  }

  @Transient
  public String getTextRepresentation() {
    if(isFolder)
      return Localization.getLocalizedStringForResourceKey("folder") + " " + name + " (" + uriString + ")";
    else
      return name + " (" + uriString + ")";
  }


  @Override
  public String toString() {
    return "File: " + getTextRepresentation();
  }

}
