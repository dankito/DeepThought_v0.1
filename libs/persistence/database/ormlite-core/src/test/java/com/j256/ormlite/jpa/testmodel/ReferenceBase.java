package com.j256.ormlite.jpa.testmodel;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
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

//  @OneToMany(fetch = FetchType.LAZY, mappedBy = "referenceBase", cascade = CascadeType.PERSIST)
//  protected Set<ReferenceBasePersonAssociation> referenceBasePersonAssociations = new HashSet<>();

//  protected transient Map<PersonRole, Set<Person>> personRoles = null;


  public ReferenceBase() {

  }

  public ReferenceBase(String title) {
    this.title = title;
  }


  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }
  
  public String getAbstract() {
    return abstractString;
  }

  public void setAbstract(String abstractString) {
    this.abstractString = abstractString;
  }

  public String getOnlineAddress() {
    return onlineAddress;
  }

  public void setOnlineAddress(String onlineAddress) {
    this.onlineAddress = onlineAddress;
  }

  public Date getLastAccessDate() {
    return lastAccessDate;
  }

  public void setLastAccessDate(Date lastAccessDate) {
    this.lastAccessDate = lastAccessDate;
  }


//  public boolean hasPersons() {
//    return referenceBasePersonAssociations.size() > 0;
//  }
//
//  public Set<ReferenceBasePersonAssociation> getReferenceBasePersonAssociations() {
//    return referenceBasePersonAssociations;
//  }
//
//  public boolean addPerson(Person person, PersonRole role) {
//    Set<Person> personsAlreadyInRole = getPersonsForRole(role);
//    ReferenceBasePersonAssociation association = new ReferenceBasePersonAssociation(this, person, role, personsAlreadyInRole == null ? 0 : personsAlreadyInRole.size());
//
//    boolean result = this.referenceBasePersonAssociations.add(association);
//    result &= person.addReference(association);
//    result &= role.addReference(association);
//
//    personRoles = null;
////    callPersonAddedListeners(role, person);
//    callEntityAddedListeners(referenceBasePersonAssociations, association);
//
//    return result;
//  }
//
//  public boolean removePerson(Person person, PersonRole role) {
//    ReferenceBasePersonAssociation association = findReferenceBasePersonAssociation(person, role);
//    if(association == null)
//      return false;
//
//    int removeIndex = association.getPersonOrder();
//
//    boolean result = this.referenceBasePersonAssociations.remove(association);
//    result &= person.removeReference(association);
//    result &= role.removeReference(association);
//
//    personRoles = null;
//
//    for(ReferenceBasePersonAssociation personAssociation : referenceBasePersonAssociations) {
//      if(personAssociation.getPersonOrder() > removeIndex)
//        personAssociation.setPersonOrder(personAssociation.getPersonOrder() - 1);
//    }
//
////    callPersonRemovedListeners(role, person);
//    callEntityRemovedListeners(referenceBasePersonAssociations, association);
//
//    return result;
//  }
//
//  protected ReferenceBasePersonAssociation findReferenceBasePersonAssociation(Person person, PersonRole role) {
//    for(ReferenceBasePersonAssociation association : this.referenceBasePersonAssociations) {
//      if(association.getPerson().equals(person) && association.getRole().equals(role))
//        return association;
//    }
//
//    return null;
//  }
//
//  public Set<PersonRole> getPersonRoles() {
//    if(personRoles == null)
//      createPersonRoles();
//
//    return personRoles.keySet();
//  }
//
//  public Set<Person> getPersonsForRole(PersonRole role) {
//    if(personRoles == null)
//      createPersonRoles();
//
//    return personRoles.get(role);
//  }
//
//  protected void createPersonRoles() {
//    personRoles = new HashMap<>();
//    for(ReferenceBasePersonAssociation association : referenceBasePersonAssociations) {
//      if(personRoles.containsKey(association.getRole()) == false)
//        personRoles.put(association.getRole(), new HashSet<Person>());
//      personRoles.get(association.getRole()).add(association.getPerson());
//    }
//  }

}
