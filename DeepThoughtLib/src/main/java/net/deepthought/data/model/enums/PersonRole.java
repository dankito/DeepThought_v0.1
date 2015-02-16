package net.deepthought.data.model.enums;

import net.deepthought.Application;
import net.deepthought.data.model.Entry;
import net.deepthought.data.model.EntryPersonAssociation;
import net.deepthought.data.model.Person;
import net.deepthought.data.model.ReferenceBase;
import net.deepthought.data.model.ReferenceBasePersonAssociation;
import net.deepthought.data.persistence.db.TableConfig;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;

/**
 * Created by ganymed on 30/01/15.
 */
@Entity(name = TableConfig.PersonRoleTableName)
public class PersonRole extends ExtensibleEnumeration {

  private static final long serialVersionUID = 3684886733146036812L;


  @OneToMany(fetch = FetchType.LAZY, mappedBy = "role")
  protected Set<EntryPersonAssociation> entryPersonAssociations = new HashSet<>();

  protected transient Set<Entry> entries = null;
  protected transient Set<Person> persons = null;

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "role")
  protected Set<ReferenceBasePersonAssociation> referenceBasePersonAssociations = new HashSet<>();

  protected transient Set<ReferenceBase> referenceBases = null;
  protected transient Set<Person> referenceBasePersons = null;


  public PersonRole() {

  }

  public PersonRole(String name) {
    super(name);
  }

  public PersonRole(String nameResourceKey, boolean isSystemValue, boolean isDeletable, int sortOrder) {
    super(nameResourceKey, isSystemValue, isDeletable, sortOrder);
  }


  public boolean addEntry(EntryPersonAssociation entryPersonAssociation) {
    boolean result = entryPersonAssociations.add(entryPersonAssociation);

    if(result) {
      entries = null;
      persons = null;
      callEntityAddedListeners(entryPersonAssociations, entryPersonAssociation);
    }

    return result;
  }

  public boolean removeEntry(EntryPersonAssociation entryPersonAssociation) {
    boolean result = entryPersonAssociations.remove(entryPersonAssociation);

    if(result) {
      entries = null;
      persons = null;
      callEntityRemovedListeners(entryPersonAssociations, entryPersonAssociation);
    }

    return result;
  }

  public Set<Entry> getEntries() {
    if(entries == null) {
      entries = new HashSet<>();
      for(EntryPersonAssociation association : entryPersonAssociations) {
        entries.add(association.getEntry());
      }
    }

    return entries;
  }

  public Set<Person> getEntriesPersons() {
    if(persons == null) {
      persons = new HashSet<>();
      for(EntryPersonAssociation association : entryPersonAssociations) {
        persons.add(association.getPerson());
      }
    }

    return persons;
  }


  public boolean addReference(ReferenceBasePersonAssociation referenceBasePersonAssociation) {
    boolean result = referenceBasePersonAssociations.add(referenceBasePersonAssociation);

    if(result) {
      referenceBases = null;
      referenceBasePersons = null;
      callEntityAddedListeners(referenceBasePersonAssociations, referenceBasePersonAssociation);
    }

    return result;
  }

  public boolean removeReference(ReferenceBasePersonAssociation referenceBasePersonAssociation) {
    boolean result = referenceBasePersonAssociations.remove(referenceBasePersonAssociation);

    if(result) {
      referenceBases = null;
      referenceBasePersons = null;
      callEntityRemovedListeners(referenceBasePersonAssociations, referenceBasePersonAssociation);
    }

    return result;
  }

  public Set<ReferenceBase> getReferenceBases() {
    if(referenceBases == null) {
      referenceBases = new HashSet<>();
      for(ReferenceBasePersonAssociation association : referenceBasePersonAssociations) {
        referenceBases.add(association.getReferenceBase());
      }
    }

    return referenceBases;
  }

  public Set<Person> getReferenceBasesPersons() {
    if(referenceBasePersons == null) {
      referenceBasePersons = new HashSet<>();
      for(ReferenceBasePersonAssociation association : referenceBasePersonAssociations) {
        referenceBasePersons.add(association.getPerson());
      }
    }

    return referenceBasePersons;
  }


  @Override
  public String toString() {
    return "PersonRole " + getTextRepresentation();
  }


  public final static PersonRole RoleWithThatNameNotFound = new PersonRole("Role with that name not found");

  public static PersonRole findByName(String name) {
    for(PersonRole role : Application.getDeepThought().getPersonRoles()) {
      if(role.getName().equals(name))
        return role;
    }

    return RoleWithThatNameNotFound;
  }

  public static PersonRole findByNameResourceKey(String nameResourceKey) {
    for(PersonRole role : Application.getDeepThought().getPersonRoles()) {
      if(nameResourceKey.equals(role.nameResourceKey))
        return role;
    }

    return RoleWithThatNameNotFound;
  }

//  @Override
//  public int compare(PersonRole personRole1, PersonRole personRole2) {
//    return personRole1.getName().compareTo(personRole2.getName());
//  }

//  @Override
//  public int compareTo(PersonRole other) {
//    return getName().compareTo(other.getName());
//  }

}
