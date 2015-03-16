package net.deepthought.data.model;

import net.deepthought.data.model.enums.PersonRole;
import net.deepthought.data.persistence.db.TableConfig;
import net.deepthought.data.persistence.db.UserDataEntity;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

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

//  @OneToOne(fetch = FetchType.EAGER)
//  @JoinColumn(name = TableConfig.ReferenceBaseOnlineAddressColumnName)
//  protected FileLink onlineAddress;
  @Column(name = TableConfig.ReferenceBaseOnlineAddressColumnName)
  protected String onlineAddress;

  @Column(name = TableConfig.ReferenceBaseLastAccessDateColumnName)
  @Temporal(TemporalType.TIMESTAMP)
  protected Date lastAccessDate;

  @Column(name = TableConfig.ReferenceBaseNotesColumnName)
  protected String notes;

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "referenceBase", cascade = CascadeType.PERSIST)
  protected Set<ReferenceBasePersonAssociation> referenceBasePersonAssociations = new HashSet<>();

  protected transient Map<PersonRole, Set<Person>> personRoles = null;

  @OneToMany(fetch = FetchType.EAGER, mappedBy = "referenceBase", cascade = CascadeType.PERSIST)
  protected Set<FileLink> files = new HashSet<>();

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "referenceBase")
  @OrderBy(value = "order")
  protected Collection<CustomField> customFields = new HashSet<>();


  public ReferenceBase() {

  }

  public ReferenceBase(String title) {
    this.title = title;
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
    this.title = title;
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

  public boolean addPerson(Person person, PersonRole role) {
    Set<Person> personsAlreadyInRole = getPersonsForRole(role);
    ReferenceBasePersonAssociation association = new ReferenceBasePersonAssociation(this, person, role, personsAlreadyInRole == null ? 0 : personsAlreadyInRole.size());

    boolean result = this.referenceBasePersonAssociations.add(association);
    result &= person.addReference(association);
    result &= role.addReference(association);

    personRoles = null;
//    callPersonAddedListeners(role, person);
    callEntityAddedListeners(referenceBasePersonAssociations, association);

    return result;
  }

  public boolean removePerson(Person person, PersonRole role) {
    ReferenceBasePersonAssociation association = findReferenceBasePersonAssociation(person, role);
    if(association == null)
      return false;

    int removeIndex = association.getPersonOrder();

    boolean result = this.referenceBasePersonAssociations.remove(association);
    result &= person.removeReference(association);
    result &= role.removeReference(association);

    personRoles = null;

    for(ReferenceBasePersonAssociation personAssociation : referenceBasePersonAssociations) {
      if(personAssociation.getPersonOrder() > removeIndex)
        personAssociation.setPersonOrder(personAssociation.getPersonOrder() - 1);
    }

//    callPersonRemovedListeners(role, person);
    callEntityRemovedListeners(referenceBasePersonAssociations, association);

    return result;
  }

  protected ReferenceBasePersonAssociation findReferenceBasePersonAssociation(Person person, PersonRole role) {
    for(ReferenceBasePersonAssociation association : this.referenceBasePersonAssociations) {
      if(association.getPerson().equals(person) && association.getRole().equals(role))
        return association;
    }

    return null;
  }

  public Set<PersonRole> getPersonRoles() {
    if(personRoles == null)
      createPersonRoles();

    return personRoles.keySet();
  }

  public Set<Person> getPersonsForRole(PersonRole role) {
    if(personRoles == null)
      createPersonRoles();

    return personRoles.get(role);
  }

  protected void createPersonRoles() {
    personRoles = new HashMap<>();
    for(ReferenceBasePersonAssociation association : referenceBasePersonAssociations) {
      if(personRoles.containsKey(association.getRole()) == false)
        personRoles.put(association.getRole(), new HashSet<Person>());
      personRoles.get(association.getRole()).add(association.getPerson());
    }
  }


  public boolean hasFiles() {
    return files.size() > 0;
  }

  public Collection<FileLink> getFiles() {
    return files;
  }

  public boolean addFile(FileLink file) {
    boolean result = files.add(file);
    if(result) {
      file.setReferenceBase(this);
      callEntityAddedListeners(files, file);
    }

    return result;
  }

  public boolean removeFile(FileLink file) {
    boolean result = files.remove(file);
    if(result) {
      file.setReferenceBase(null);
      callEntityRemovedListeners(files, file);
    }

    return result;
  }

  public Collection<CustomField> getCustomFields() {
    return customFields;
  }

  public boolean addCustomField(CustomField customField) {
    boolean result = customFields.add(customField);
    if(result) {
      customField.setReferenceBase(this);
      callEntityAddedListeners(customFields, customField);
    }

    return result;
  }

  public boolean removeCustomField(CustomField customField) {
    int removeCustomFieldOrder = customField.getOrder();

    boolean result = customFields.remove(customField);
    if(result) {
      customField.setReferenceBase(null);

      for(CustomField customFieldIter : getCustomFields()) {
        if(customFieldIter.getOrder() > removeCustomFieldOrder)
          customFieldIter.setOrder(customFieldIter.getOrder() - 1);
      }

      callEntityRemovedListeners(customFields, customField);
    }

    return result;
  }

}
