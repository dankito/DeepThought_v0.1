package com.j256.ormlite.jpa.testmodel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.Transient;

/**
 * Created by ganymed on 16/12/14.
 */
@Entity(name = TableConfig.EntryTableName)
public class Entry extends UserDataEntity implements Serializable, Comparable<Entry> {

  private static final long serialVersionUID = 596730656893495215L;


//  @ManyToOne(fetch = FetchType.EAGER)
//  @JoinColumn(name = TableConfig.EntryParentEntryJoinColumnName)
//  protected Entry parentEntry;
//
//  @OneToMany(fetch = FetchType.LAZY, mappedBy = "parentEntry"/*, cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }*/)
//  protected Set<Entry> subEntries = new HashSet<>();


  @Column(name = TableConfig.EntryTitleColumnName, length = 512)
  protected String title = "";

  @Column(name = TableConfig.EntryContentColumnName)
//  @Column(name = TableConfig.EntryContentColumnName, columnDefinition = "clob") // Derby needs explicitly clob column definition
  @Lob
  protected String content = "";
  @Column(name = TableConfig.EntryEntryIndexColumnName)
  protected int entryIndex;

  @ManyToMany(fetch = FetchType.EAGER, mappedBy = "entries")
  protected Set<Category> categories = new HashSet<>();

  @ManyToMany(fetch = FetchType.EAGER/*, cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }*/ )
  @JoinTable(
      name = TableConfig.EntryTagJoinTableName,
      joinColumns = { @JoinColumn(name = TableConfig.EntryTagJoinTableEntryIdColumnName/*, referencedColumnName = "id"*/) },
      inverseJoinColumns = { @JoinColumn(name = TableConfig.EntryTagJoinTableTagIdColumnName/*, referencedColumnName = "id"*/) }
  )
//  @OrderBy("name ASC")
  protected Set<Tag> tags = new HashSet<>();

//  @OneToMany(fetch = FetchType.LAZY, mappedBy = "entry", cascade = CascadeType.PERSIST)
//  protected Set<EntryPersonAssociation> entryPersonAssociations = new HashSet<>();
//
//  protected transient Map<PersonRole, Set<Person>> personRoles = null;

  // Reference

//  @ManyToOne(fetch = FetchType.LAZY)
//  @JoinColumn(name = TableConfig.EntrySeriesTitleJoinColumnName)
//  protected SeriesTitle series;
//
//  @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
//  @JoinColumn(name = TableConfig.EntryReferenceJoinColumnName)
//  protected Reference reference;
//
//  @ManyToOne(fetch = FetchType.LAZY)
//  @JoinColumn(name = TableConfig.EntryReferenceSubDivisionJoinColumnName)
//  protected ReferenceSubDivision referenceSubDivision;

//  @ManyToOne(fetch = FetchType.EAGER)
//  @JoinColumn(name = TableConfig.EntryDeepThoughtJoinColumnName)
//  protected DeepThought deepThought;



  public Entry() {

  }

  public Entry(String title) {
    this.title = title;
  }

  public Entry(String title, String content) {
    this(title);
    this.content = content;
  }


  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

//  public SeriesTitle getSeries() {
//    return series;
//  }
//
//  public void setSeries(SeriesTitle series) {
//    Object previousValue = this.series;
//
//    if(this.series != null)
//      this.series.removeEntry(this);
//
//    this.series = series;
//
//    if(series != null) {
//      series.addEntry(this);
//
//      if (getReference() != null && series.containsSerialParts(getReference()) == false)
//        setReference(null);
//    }
//    else if(getReference() != null && getReference().getSeries() != null)
//      setReference(null);
//  }
//
//  public Reference getReference() {
//    return reference;
//  }
//
//  public void setReference(Reference reference) {
//    Object previousValue = this.reference;
//
//    if(this.reference != null)
//      this.reference.removeEntry(this);
//
//    this.reference = reference;
//
//    if(reference != null) {
//      reference.addEntry(this);
//      if(reference.getSeries() != series)
//        setSeries(reference.getSeries());
//      if(referenceSubDivision != null && reference.getSubDivisions().contains(referenceSubDivision) == false)
//        setReferenceSubDivision(null);
//    }
//    else {
//      if(referenceSubDivision != null)
//        setReferenceSubDivision(null);
//    }
//  }
//
//  public ReferenceSubDivision getReferenceSubDivision() {
//    return referenceSubDivision;
//  }
//
//  public void setReferenceSubDivision(ReferenceSubDivision referenceSubDivision) {
//    Object previousValue = this.referenceSubDivision;
//
//    if(this.referenceSubDivision != null)
//      this.referenceSubDivision.removeEntry(this);
//
//    this.referenceSubDivision = referenceSubDivision;
//
//    if(referenceSubDivision != null) {
//      referenceSubDivision.addEntry(this);
//
//      if(referenceSubDivision.getReference() != reference)
//        setReference(referenceSubDivision.getReference());
//    }
//  }

//  public Entry getParentEntry() {
//    return parentEntry;
//  }
//
//  public boolean hasSubEntries() {
//    return getSubEntries().size() > 0;
//  }
//
//  public Collection<Entry> getSubEntries() {
//    return subEntries;
//  }
//
//  public boolean addSubEntry(Entry subEntry) {
//    subEntry.parentEntry = this;
//
//    boolean result = subEntries.add(subEntry);
//    if(result) {
//      callEntityAddedListeners(subEntries, subEntry);
//    }
//
//    return result;
//  }
//
//  public boolean removeSubEntry(Entry subEntry) {
//    boolean result = subEntries.remove(subEntry);
//    if(result) {
//      subEntry.parentEntry = null;
//      callEntityRemovedListeners(subEntries, subEntry);
//    }
//
//    return result;
//  }
//
//  public boolean containsSubEntry(Entry subEntry) {
//    return subEntries.contains(subEntry);
//  }


  public boolean hasCategories() {
    return categories.size() > 0;
  }

  public Collection<Category> getCategories() {
    return categories;
  }

  protected boolean addCategory(Category category) {
    boolean result = categories.add(category);
    return result;
  }

  protected boolean removeCategory(Category category) {
    boolean result = categories.remove(category);
    return result;
  }


  public boolean hasTags() {
    return getTags().size() > 0;
  }

  public Collection<Tag> getTags() {
    return tags;
  }

  @Transient
  public Collection<Tag> getTagsSorted() {
    List<Tag> sortedTags = new ArrayList<>(getTags());
    Collections.sort(sortedTags);
    return sortedTags;
  }

  public void setTags(Collection<Tag> newTags) {
    for(Tag currentTag : new ArrayList<>(getTags())) {
      if(newTags.contains(currentTag) == false)
        removeTag(currentTag);
    }

    for(Tag newTag : new ArrayList<>(newTags)) {
      if(hasTag(newTag) == false)
        addTag(newTag);
    }
  }

  public boolean addTag(Tag tag) {
    boolean result = tags.add(tag);
    if(result) {
      tag.addEntry(this);
    }

    return result;
  }

  public boolean removeTag(Tag tag) {
    boolean result = tags.remove(tag);
    if(result) {
      tag.removeEntry(this);
    }

    return result;
  }

  public boolean hasTag(Tag tag) {
    return tags.contains(tag);
  }

  public boolean hasTags(Collection<Tag> tags) {
    boolean result = true;

    for(Tag tag : tags)
      result &= hasTag(tag);

    return result;
  }

  @Transient
  public String getTagsStringRepresentation() {
    String tagsString = "";
    for(Tag tag : getTagsSorted())
      tagsString += tag.getName() + ", ";

    if(tagsString.length() > 2)
      tagsString = tagsString.substring(0, tagsString.length() - 2);

    return tagsString;
  }


//  public boolean hasPersons() {
//    return entryPersonAssociations.size() > 0;
//  }
//
//  public Set<EntryPersonAssociation> getEntryPersonAssociations() {
//    return entryPersonAssociations;
//  }
//
//  public boolean addPerson(Person person, PersonRole role) {
//    Set<Person> personsAlreadyInRole = getPersonsForRole(role);
//    EntryPersonAssociation entryPersonAssociation = new EntryPersonAssociation(this, person, role, personsAlreadyInRole == null ? 0 : personsAlreadyInRole.size());
//
//    boolean result = this.entryPersonAssociations.add(entryPersonAssociation);
//    result &= person.addEntry(entryPersonAssociation);
//    result &= role.addEntry(entryPersonAssociation);
//
//    personRoles = null;
//    callPersonAddedListeners(role, person);
//    callEntityAddedListeners(entryPersonAssociations, entryPersonAssociation);
//
//    return result;
//  }
//
//  public boolean removePerson(Person person, PersonRole role) {
//    EntryPersonAssociation entryPersonAssociation = findEntryPersonAssociation(person, role);
//    if(entryPersonAssociation == null)
//      return false;
//
//    int removeIndex = entryPersonAssociation.getPersonOrder();
//
//    boolean result = this.entryPersonAssociations.remove(entryPersonAssociation);
//    result &= person.removeEntry(entryPersonAssociation);
//    result &= role.removeEntry(entryPersonAssociation);
//
//    personRoles = null;
//
//    for(EntryPersonAssociation association : entryPersonAssociations) {
//      if(association.getPersonOrder() > removeIndex)
//        association.setPersonOrder(association.getPersonOrder() - 1);
//    }
//
//    callEntityRemovedListeners(entryPersonAssociations, entryPersonAssociation);
//    callPersonRemovedListeners(role, person);
//
//    return result;
//  }
//
//  protected EntryPersonAssociation findEntryPersonAssociation(Person person, PersonRole role) {
//    for(EntryPersonAssociation association : this.entryPersonAssociations) {
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
//    for(EntryPersonAssociation association : entryPersonAssociations) {
//      if(personRoles.containsKey(association.getRole()) == false)
//        personRoles.put(association.getRole(), new HashSet<Person>());
//      personRoles.get(association.getRole()).add(association.getPerson());
//    }
//  }


//  public DeepThought getDeepThought() {
//    return deepThought;
//  }


  public int getEntryIndex() {
    return entryIndex;
  }

  protected void setEntryIndex(int entryIndex) {
    this.entryIndex = entryIndex;
  }


  @Override
  @Transient
  public String getTextRepresentation() {
    return "Entry " + getTitle();
  }

  @Override
  public String toString() {
    return "Entry " + getTitle() + " (" + getContent() + ")";
  }

  @Override
  public int compareTo(Entry other) {
    if(other == null)
      return 1;
    return ((Integer)other.getEntryIndex()).compareTo(getEntryIndex());
  }
}
