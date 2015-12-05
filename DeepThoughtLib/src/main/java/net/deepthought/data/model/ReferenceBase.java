package net.deepthought.data.model;

import net.deepthought.data.persistence.db.TableConfig;
import net.deepthought.data.persistence.db.UserDataEntity;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

/**
 * Created by ganymed on 21/01/15.
 */
//@MappedSuperclass
@Entity
@Table(name = TableConfig.ReferenceBaseTableName)
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = TableConfig.ReferenceBaseDiscriminatorColumnName, discriminatorType = DiscriminatorType.STRING, length = 20)
//@DiscriminatorValue(JoinedTableInheritanceBaseEntityDiscriminatorValue)
public abstract class ReferenceBase extends UserDataEntity {

  private static final long serialVersionUID = 3600131148034407937L;


  @Column(name = TableConfig.ReferenceBaseTitleColumnName)
  protected String title = "";

  @Column(name = TableConfig.ReferenceBaseSubTitleColumnName)
  protected String subTitle;

  @Column(name = TableConfig.ReferenceBaseAbstractColumnName)
  protected String abstractString;

  @Column(name = TableConfig.ReferenceBaseOnlineAddressColumnName)
  protected String onlineAddress;

  @Column(name = TableConfig.ReferenceBaseLastAccessDateColumnName)
  @Temporal(TemporalType.TIMESTAMP)
  protected Date lastAccessDate;

  @Column(name = TableConfig.ReferenceBaseNotesColumnName)
  protected String notes;

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "referenceBase", cascade = CascadeType.PERSIST)
  protected Set<ReferenceBasePersonAssociation> referenceBasePersonAssociations = new HashSet<>();

  protected transient Set<Person> persons = null;

  @ManyToMany(fetch = FetchType.LAZY )
  @JoinTable(
      name = TableConfig.ReferenceBaseAttachedFileJoinTableName,
      joinColumns = { @JoinColumn(name = TableConfig.ReferenceBaseAttachedFileJoinTableReferenceBaseIdColumnName) },
      inverseJoinColumns = { @JoinColumn(name = TableConfig.ReferenceBaseAttachedFileJoinTableFileLinkIdColumnName) }
  )
  protected Set<FileLink> attachedFiles = new HashSet<>();

  @ManyToMany(fetch = FetchType.LAZY )
  @JoinTable(
      name = TableConfig.ReferenceBaseEmbeddedFileJoinTableName,
      joinColumns = { @JoinColumn(name = TableConfig.ReferenceBaseEmbeddedFileJoinTableReferenceBaseIdColumnName) },
      inverseJoinColumns = { @JoinColumn(name = TableConfig.ReferenceBaseEmbeddedFileJoinTableFileLinkIdColumnName) }
  )
  protected Set<FileLink> embeddedFiles = new HashSet<>();

  @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
  @JoinColumn(name = TableConfig.ReferenceBasePreviewImageJoinColumnName)
  protected FileLink previewImage;



  public ReferenceBase() {

  }

  public ReferenceBase(String title) {
    setTitle(title);
  }

  public ReferenceBase(String title, String subTitle) {
    this(title);
    this.subTitle = subTitle;
  }


  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    Object previousValue = this.title;
    this.title = title == null ? "" : title;
    callPropertyChangedListeners(TableConfig.ReferenceBaseTitleColumnName, previousValue, title);
  }

  public String getSubTitle() {
    return subTitle;
  }

  public void setSubTitle(String subTitle) {
    Object previousValue = this.subTitle;
    this.subTitle = subTitle;
    callPropertyChangedListeners(TableConfig.ReferenceBaseSubTitleColumnName, previousValue, subTitle);
  }
  
  public String getAbstract() {
    return abstractString;
  }

  public void setAbstract(String abstractString) {
    Object previousValue = this.abstractString;
    this.abstractString = abstractString;
    callPropertyChangedListeners(TableConfig.ReferenceBaseAbstractColumnName, previousValue, abstractString);
  }

  public String getOnlineAddress() {
    return onlineAddress;
  }

  public void setOnlineAddress(String onlineAddress) {
    Object previousValue = this.onlineAddress;
    this.onlineAddress = onlineAddress;
    callPropertyChangedListeners(TableConfig.ReferenceBaseOnlineAddressColumnName, previousValue, onlineAddress);
  }

  public Date getLastAccessDate() {
    return lastAccessDate;
  }

  public void setLastAccessDate(Date lastAccessDate) {
    Object previousValue = this.lastAccessDate;
    this.lastAccessDate = lastAccessDate;
    callPropertyChangedListeners(TableConfig.ReferenceBaseLastAccessDateColumnName, previousValue, lastAccessDate);
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    Object previousValue = this.notes;
    this.notes = notes;
    callPropertyChangedListeners(TableConfig.ReferenceBaseNotesColumnName, previousValue, notes);
  }


  public boolean hasPersons() {
    return referenceBasePersonAssociations.size() > 0;
  }

  public Set<ReferenceBasePersonAssociation> getReferenceBasePersonAssociations() {
    return referenceBasePersonAssociations;
  }

  public boolean addPerson(Person person) {
    ReferenceBasePersonAssociation existingAssociation = findReferenceBasePersonAssociation(person);
    if(existingAssociation != null) // Person already added to ReferenceBase
      return false;

    ReferenceBasePersonAssociation association = new ReferenceBasePersonAssociation(this, person, referenceBasePersonAssociations.size());

    boolean result = this.referenceBasePersonAssociations.add(association);
    result &= person.addReference(association);

//    callPersonAddedListeners(role, person);
    callEntityAddedListeners(referenceBasePersonAssociations, association);

    return result;
  }

  public boolean removePerson(Person person) {
    ReferenceBasePersonAssociation association = findReferenceBasePersonAssociation(person);
    if(association == null)
      return false;

    int removeIndex = association.getPersonOrder();

    boolean result = this.referenceBasePersonAssociations.remove(association);
    result &= person.removeReference(association);

    persons = null;

    for(ReferenceBasePersonAssociation personAssociation : referenceBasePersonAssociations) {
      if(personAssociation.getPersonOrder() > removeIndex)
        personAssociation.setPersonOrder(personAssociation.getPersonOrder() - 1);
    }

//    callPersonRemovedListeners(role, person);
    callEntityRemovedListeners(referenceBasePersonAssociations, association);

    return result;
  }

  protected ReferenceBasePersonAssociation findReferenceBasePersonAssociation(Person person) {
    for(ReferenceBasePersonAssociation association : this.referenceBasePersonAssociations) {
      if(association.getPerson().equals(person))
        return association;
    }

    return null;
  }

  public Set<Person> getPersons() {
    if(persons == null)
      createPersons();

    return persons;
  }

  protected void createPersons() {
    persons = new HashSet<>();
    for(ReferenceBasePersonAssociation association : referenceBasePersonAssociations) {
      persons.add(association.getPerson());
    }
  }


  public boolean hasAttachedFiles() {
    return attachedFiles.size() > 0;
  }

  public Collection<FileLink> getAttachedFiles() {
    return attachedFiles;
  }

  public boolean addAttachedFile(FileLink file) {
    boolean result = attachedFiles.add(file);
    if(result) {
      file.addAsAttachmentToReferenceBase(this);

      callEntityAddedListeners(attachedFiles, file);
    }

    return result;
  }

  public boolean removeAttachedFile(FileLink file) {
    boolean result = attachedFiles.remove(file);
    if(result) {
      file.removeAsAttachmentFromReferenceBase(this);

      callEntityRemovedListeners(attachedFiles, file);
    }

    return result;
  }


  public boolean hasEmbeddedFiles() {
    return embeddedFiles.size() > 0;
  }

  public Collection<FileLink> getEmbeddedFiles() {
    return embeddedFiles;
  }

  public boolean addEmbeddedFile(FileLink file) {
    if(embeddedFiles.contains(file))
      return false;

    boolean result = embeddedFiles.add(file);
    if(result) {
      file.addAsEmbeddingToReferenceBase(this);

      callEntityAddedListeners(embeddedFiles, file);
    }

    return result;
  }

  public boolean removeEmbeddedFile(FileLink file) {
    boolean result = embeddedFiles.remove(file);
    if(result) {
      file.removeAsEmbeddingFromReferenceBase(this);

      callEntityRemovedListeners(embeddedFiles, file);
    }

    return result;
  }


  public FileLink getPreviewImage() {
    return previewImage;
  }

  public void setPreviewImage(FileLink previewImage) {
    FileLink previousPreviewImage = this.previewImage;
    this.previewImage = previewImage;
    callPropertyChangedListeners(TableConfig.ReferenceBasePreviewImageJoinColumnName, previousPreviewImage, previewImage);
  }


  @Override
  @Transient
  public String getTextRepresentation() {
    return title;
  }


}
