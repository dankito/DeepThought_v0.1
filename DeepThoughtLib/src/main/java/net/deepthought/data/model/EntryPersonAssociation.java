package net.deepthought.data.model;

import net.deepthought.data.model.enums.PersonRole;
import net.deepthought.data.persistence.db.AssociationEntity;
import net.deepthought.data.persistence.db.TableConfig;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

/**
 * Created by ganymed on 30/01/15.
 */
@Entity(name = TableConfig.EntryPersonAssociationTableName)
public class EntryPersonAssociation extends AssociationEntity {

  private static final long serialVersionUID = -5046567432089774614L;


//  @Id
  @ManyToOne(optional = false, fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
  @JoinColumn(name = TableConfig.EntryPersonAssociationEntryJoinColumnName)
  protected Entry entry;

//  @Id
  @ManyToOne(optional = false, fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
  @JoinColumn(name = TableConfig.EntryPersonAssociationPersonJoinColumnName)
  protected Person person;

//  @Id
  @ManyToOne(optional = false, fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
  @JoinColumn(name = TableConfig.EntryPersonAssociationPersonRoleJoinColumnName)
  protected PersonRole role;

  @Column(name = TableConfig.EntryPersonAssociationPersonOrderColumnName)
  protected int personOrder = Integer.MAX_VALUE;


  protected EntryPersonAssociation() {

  }

  public EntryPersonAssociation(Entry entry, Person person, PersonRole role) {
    this.entry = entry;
    this.person = person;
    this.role = role;
  }

  public EntryPersonAssociation(Entry entry, Person person, PersonRole role, int personOrder) {
    this(entry, person, role);
    this.personOrder = personOrder;
  }


  public Entry getEntry() {
    return entry;
  }

  public Person getPerson() {
    return person;
  }

  public PersonRole getRole() {
    return role;
  }

  public int getPersonOrder() {
    return personOrder;
  }

  public void setPersonOrder(int personOrder) {
    this.personOrder = personOrder;
  }


  //  @Override
//  public boolean equals(Object o) {
//    if (this == o) return true;
//    if (!(o instanceof EntryPersonRelation)) return false;
//
//    EntryPersonRelation that = (EntryPersonRelation) o;
//
//    if (!entry.equals(that.entry)) return false;
//    if (!person.equals(that.person)) return false;
//    if (!role.equals(that.role)) return false;
//
//    return true;
//  }
//
//  @Override
//  public int hashCode() {
//    int result = entry.hashCode();
//    result = 31 * result + person.hashCode();
//    result = 31 * result + role.hashCode();
//    return result;
//  }


  @Override
  @Transient
  public String getTextRepresentation() {
    return "EntryPersonAssociation: Person = " + person + ", Entry = " + entry;
  }
}
