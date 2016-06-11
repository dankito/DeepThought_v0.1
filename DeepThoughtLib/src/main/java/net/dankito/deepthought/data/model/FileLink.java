package net.dankito.deepthought.data.model;

import net.dankito.deepthought.data.model.enums.FileType;
import net.dankito.deepthought.data.persistence.db.TableConfig;
import net.dankito.deepthought.data.persistence.db.UserDataEntity;
import net.dankito.deepthought.util.localization.Localization;
import net.dankito.deepthought.util.StringUtils;
import net.dankito.deepthought.util.file.FileUtils;

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
public class FileLink extends UserDataEntity implements Serializable, Comparable<FileLink> {

  private static final long serialVersionUID = -7508656557829870722L;


  @Column(name = TableConfig.FileLinkUriColumnName)
  protected String uriString = "";

  @Column(name = TableConfig.FileLinkNameColumnName)
  protected String name = "";

  @Column(name = TableConfig.FileLinkIsFolderColumnName)
  protected boolean isFolder = false;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = TableConfig.FileLinkFileTypeColumnName)
  protected FileType fileType = FileType.getDefaultFileType();

  @Column(name = TableConfig.FileLinkDescriptionColumnName)
  protected String description = "";

  @Column(name = TableConfig.FileLinkSourceUriColumnName)
  protected String sourceUriString = "";

  @ManyToMany(fetch = FetchType.LAZY, mappedBy = "attachedFiles")
  protected Set<Entry> entriesAttachedTo = new HashSet<>();

  @ManyToMany(fetch = FetchType.LAZY, mappedBy = "embeddedFiles")
  protected Set<Entry> entriesEmbeddedIn = new HashSet<>();

  @ManyToMany(fetch = FetchType.LAZY, mappedBy = "attachedFiles")
  protected Set<ReferenceBase> referenceBasesAttachedTo = new HashSet<>();

  @ManyToMany(fetch = FetchType.LAZY, mappedBy = "embeddedFiles")
  protected Set<ReferenceBase> referenceBasesEmbeddedIn = new HashSet<>();

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = TableConfig.FileLinkDeepThoughtJoinColumnName)
  protected DeepThought deepThought;


  public FileLink() {
    this.fileType = FileType.getDefaultFileType();
  }

  public FileLink(String uri) {
    this();
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

    if(StringUtils.isNullOrEmpty(name)) {
      setName(FileUtils.getFileNameIncludingExtension(uriString));
    }

    determineFileType(uriString);
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

  public FileType getFileType() {
    return fileType;
  }

  public void setFileType(FileType fileType) {
    this.fileType = fileType;
  }

  protected void determineFileType(String uriString) {
    setFileType(FileUtils.getFileType(uriString));
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    if(description == null)
      description = "";

    Object previousValue = this.description;
    this.description = description;
    callPropertyChangedListeners(TableConfig.FileLinkDescriptionColumnName, previousValue, description);
  }

  public String getSourceUriString() {
    return sourceUriString;
  }

  public void setSourceUriString(String sourceUriString) {
    Object previousValue = this.sourceUriString;
    this.sourceUriString = sourceUriString;
    callPropertyChangedListeners(TableConfig.FileLinkSourceUriColumnName, previousValue, sourceUriString);
  }


  public boolean isAttachedToEntries() {
    return getEntriesAttachedTo().size() > 0;
  }

  public Collection<Entry> getEntriesAttachedTo() {
    return entriesAttachedTo;
  }

  protected boolean addAsAttachmentToEntry(Entry entry) {
    boolean result = entriesAttachedTo.add(entry);
    if(result) {
      if(entry.isPersisted())
        callEntityAddedListeners(entriesAttachedTo, entry);
    }

    return result;
  }

  protected boolean removeAsAttachmentFromEntry(Entry entry) {
    boolean result = entriesAttachedTo.remove(entry);
    if(result) {
      callEntityRemovedListeners(entriesAttachedTo, entry);
    }

    return result;
  }

  public boolean isEmbeddedInEntries() {
    return getEntriesEmbeddedIn().size() > 0;
  }

  public Collection<Entry> getEntriesEmbeddedIn() {
    return entriesEmbeddedIn;
  }

  protected boolean addAsEmbeddingToEntry(Entry entry) {
    boolean result = entriesEmbeddedIn.add(entry);
    if(result) {
      if(entry.isPersisted())
        callEntityAddedListeners(entriesEmbeddedIn, entry);
    }

    return result;
  }

  protected boolean removeAsEmbeddingFromEntry(Entry entry) {
    boolean result = entriesEmbeddedIn.remove(entry);
    if(result) {
      callEntityRemovedListeners(entriesEmbeddedIn, entry);
    }

    return result;
  }


  public boolean isAttachedToReferenceBases() {
    return getReferenceBasesAttachedTo().size() > 0;
  }

  public Set<ReferenceBase> getReferenceBasesAttachedTo() {
    return referenceBasesAttachedTo;
  }

  protected boolean addAsAttachmentToReferenceBase(ReferenceBase referenceBase) {
    boolean result = referenceBasesAttachedTo.add(referenceBase);
    if(result) {
      if(referenceBase.isPersisted())
        callEntityAddedListeners(referenceBasesAttachedTo, referenceBase);
    }

    return result;
  }

  protected boolean removeAsAttachmentFromReferenceBase(ReferenceBase referenceBase) {
    boolean result = referenceBasesAttachedTo.remove(referenceBase);
    if(result) {
      callEntityRemovedListeners(referenceBasesAttachedTo, referenceBase);
    }

    return result;
  }

  public boolean isEmbeddedInReferenceBases() {
    return getReferenceBasesEmbeddedIn().size() > 0;
  }

  public Collection<ReferenceBase> getReferenceBasesEmbeddedIn() {
    return referenceBasesEmbeddedIn;
  }

  protected boolean addAsEmbeddingToReferenceBase(ReferenceBase referenceBase) {
    boolean result = referenceBasesEmbeddedIn.add(referenceBase);
    if(result) {
      if(referenceBase.isPersisted())
        callEntityAddedListeners(referenceBasesEmbeddedIn, referenceBase);
    }

    return result;
  }

  protected boolean removeAsEmbeddingFromReferenceBase(ReferenceBase referenceBase) {
    boolean result = referenceBasesEmbeddedIn.remove(referenceBase);
    if(result) {
      callEntityRemovedListeners(referenceBasesEmbeddedIn, referenceBase);
    }

    return result;
  }


  public DeepThought getDeepThought() {
    return deepThought;
  }

  protected void setDeepThought(DeepThought deepThought) {
    Object previousValue = this.deepThought;
    this.deepThought = deepThought;
    callPropertyChangedListeners(TableConfig.FileLinkDeepThoughtJoinColumnName, previousValue, deepThought);
  }


  @Transient
  public String getTextRepresentation() {
    if(isFolder)
      return Localization.getLocalizedString("folder") + " " + name + " (" + uriString + ")";
    else
      return name + " (" + uriString + ")";
  }


  @Override
  public String toString() {
    return "File: " + getTextRepresentation();
  }


  @Override
  public int compareTo(FileLink other) {
    if(other == null)
      return 1;

    if(name.equals(other.getName()) == false)
      return name.compareTo(other.getName());

    if(uriString.equals(other.getUriString()) == false)
      return uriString.compareTo(other.getUriString());

    return 0;
  }

}
