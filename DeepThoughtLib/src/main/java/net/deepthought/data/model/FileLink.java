package net.deepthought.data.model;

import net.deepthought.data.persistence.db.TableConfig;
import net.deepthought.data.persistence.db.UserDataEntity;
import net.deepthought.util.Localization;
import net.deepthought.util.StringUtils;
import net.deepthought.util.file.FileUtils;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
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
  protected String name = "";

  @Column(name = TableConfig.FileLinkIsFolderColumnName)
  protected boolean isFolder;

  @Column(name = TableConfig.FileLinkNotesColumnName)
  protected String notes;

  @Column(name = TableConfig.FileLinkSourceUriColumnName)
  protected String sourceUriString;

//  @ManyToOne(fetch = FetchType.LAZY)
//  @JoinColumn(name = TableConfig.FileLinkEntryJoinColumnName)
  @ManyToMany(fetch = FetchType.LAZY, mappedBy = "files")
  protected Set<Entry> entries = new HashSet<>();

//  @ManyToOne(fetch = FetchType.LAZY)
//  @JoinColumn(name = TableConfig.FileLinkReferenceBaseJoinColumnName)
  @ManyToMany(fetch = FetchType.LAZY, mappedBy = "files")
  protected Set<ReferenceBase> referenceBases = new HashSet<>();

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = TableConfig.FileLinkDeepThoughtJoinColumnName)
  protected DeepThought deepThought;


  public FileLink() {

  }

  public FileLink(String uri) {
    setUriString(uri);
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

    if(StringUtils.isNullOrEmpty(name))
      setName(FileUtils.getFileNameIncludingExtension(uriString));
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

  public String getSourceUriString() {
    return sourceUriString;
  }

  public void setSourceUriString(String sourceUriString) {
    Object previousValue = this.sourceUriString;
    this.sourceUriString = sourceUriString;
    callPropertyChangedListeners(TableConfig.FileLinkSourceUriColumnName, previousValue, sourceUriString);
  }

  public Collection<Entry> getEntries() {
    return entries;
  }

  protected boolean addEntry(Entry entry) {
    boolean result = entries.add(entry);
    if(result) {
      if(entry.isPersisted())
        callEntityAddedListeners(entries, entry);
    }

    return result;
  }

  protected boolean removeEntry(Entry entry) {
    boolean result = entries.remove(entry);
    if(result) {
      callEntityRemovedListeners(entries, entry);
    }

    return result;
  }

  public Set<ReferenceBase> getReferenceBases() {
    return referenceBases;
  }

  protected boolean addReferenceBase(ReferenceBase referenceBase) {
    boolean result = referenceBases.add(referenceBase);
    if(result) {
      if(referenceBase.isPersisted())
        callEntityAddedListeners(referenceBases, referenceBase);
    }

    return result;
  }

  protected boolean removeReferenceBase(ReferenceBase referenceBase) {
    boolean result = referenceBases.remove(referenceBase);
    if(result) {
      callEntityRemovedListeners(referenceBases, referenceBase);
    }

    return result;
  }

  public DeepThought getDeepThought() {
    return deepThought;
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
