package net.dankito.deepthought.data.model;

import net.dankito.deepthought.data.persistence.db.TableConfig;
import net.dankito.deepthought.data.persistence.db.UserDataEntity;
import net.dankito.deepthought.util.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

/**
 * Created by ganymed on 16/12/14.
 */
@Entity(name = TableConfig.PersonTableName)
//@Inheritance(strategy = InheritanceType.JOINED)
//@DiscriminatorColumn(name = "person_type", discriminatorType = DiscriminatorType.STRING)
//@DiscriminatorValue("PERSON")
public class Person extends UserDataEntity implements Serializable, Comparable<Person> {

  private static final long serialVersionUID = -5060968855708551118L;

  private final static Logger log = LoggerFactory.getLogger(Person.class);


  @Column(name = TableConfig.PersonFirstNameColumnName)
  protected String firstName = "";

  @Column(name = TableConfig.PersonLastNameColumnName)
  protected String lastName = "";

  @Column(name = TableConfig.PersonNotesColumnName)
  protected String notes;

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "person")
  protected Set<EntryPersonAssociation> entryPersonAssociations = new HashSet<>();

  protected transient Set<Entry> entries = null;

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "person")
  protected Set<ReferenceBasePersonAssociation> referenceBasePersonAssociations = new HashSet<>();

  protected transient Set<SeriesTitle> associatedSeriesTitles = null;

  protected transient Set<Reference> associatedReferences = null;

  protected transient Set<ReferenceSubDivision> associatedReferenceSubDivisions = null;

//  @JsonIgnore
  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = TableConfig.PersonDeepThoughtJoinColumnName)
  protected DeepThought deepThought;


  public Person() {

  }

  public Person(String firstName, String lastName) {
    this.firstName = firstName;
    this.lastName = lastName;
  }


  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    String previousFirstName = this.firstName;
    this.firstName = firstName;
    callPropertyChangedListeners(TableConfig.PersonFirstNameColumnName, previousFirstName, firstName);
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    String previousLastName = this.lastName;
    this.lastName = lastName;
    callPropertyChangedListeners(TableConfig.PersonLastNameColumnName, previousLastName, lastName);
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    String previousNotes = this.notes;
    this.notes = notes;
    callPropertyChangedListeners(TableConfig.PersonNotesColumnName, previousNotes, notes);
  }



  protected boolean addEntry(EntryPersonAssociation entryPersonAssociation) {
    boolean result = entryPersonAssociations.add(entryPersonAssociation);

    if(result) {
      entries = null;
      callEntityAddedListeners(entryPersonAssociations, entryPersonAssociation);
    }

    return result;
  }

  protected boolean removeEntry(EntryPersonAssociation entryPersonAssociation) {
    boolean result = entryPersonAssociations.remove(entryPersonAssociation);

    if(result) {
      entries = null;
      callEntityRemovedListeners(entryPersonAssociations, entryPersonAssociation);
    }

    return result;
  }

  public boolean isSetOnEntries() {
    return getAssociatedEntries().size() > 0;
  }

  public Set<Entry> getAssociatedEntries() {
    if(entries == null)
      createEntries();

    return entries;
  }

  protected void createEntries() {
    entries = new HashSet<>();
    for(EntryPersonAssociation association : entryPersonAssociations) {
      entries.add(association.getEntry());
    }
  }


  protected boolean addReference(ReferenceBasePersonAssociation referenceBasePersonAssociation) {
    boolean result = referenceBasePersonAssociations.add(referenceBasePersonAssociation);

    if(result) {
      associatedReferences = null;
      callEntityAddedListeners(referenceBasePersonAssociations, referenceBasePersonAssociation);
    }

    return result;
  }

  protected boolean removeReference(ReferenceBasePersonAssociation referenceBasePersonAssociation) {
    boolean result = referenceBasePersonAssociations.remove(referenceBasePersonAssociation);

    if(result) {
      associatedReferences = null;
      callEntityRemovedListeners(referenceBasePersonAssociations, referenceBasePersonAssociation);
    }

    return result;
  }

  public boolean isSetOnSeries() {
    return getAssociatedSeries().size() > 0;
  }

  public Set<SeriesTitle> getAssociatedSeries() {
    if(associatedSeriesTitles == null)
      createAssociatedSeries();

    return associatedSeriesTitles;
  }

  protected void createAssociatedSeries() {
    associatedSeriesTitles = new HashSet<>();
    for(ReferenceBasePersonAssociation association : referenceBasePersonAssociations) {
      if(association.getReferenceBase() instanceof SeriesTitle) {
        associatedSeriesTitles.add((SeriesTitle) association.getReferenceBase());
      }
    }
  }

  public boolean isSetOnReferences() {
    return getAssociatedReferences().size() > 0;
  }

  public Set<Reference> getAssociatedReferences() {
    if(associatedReferences == null)
      createAssociatedReferences();

    return associatedReferences;
  }

  protected void createAssociatedReferences() {
    associatedReferences = new HashSet<>();
    for(ReferenceBasePersonAssociation association : referenceBasePersonAssociations) {
      if(association.getReferenceBase() instanceof Reference) {
        associatedReferences.add((Reference) association.getReferenceBase());
      }
    }
  }

  public boolean isSetOnReferenceSubDivisions() {
    return getAssociatedReferenceSubDivisions().size() > 0;
  }

  public Set<ReferenceSubDivision> getAssociatedReferenceSubDivisions() {
    if(associatedReferenceSubDivisions == null)
      createAssociatedReferenceSubDivisions();

    return associatedReferenceSubDivisions;
  }

  protected void createAssociatedReferenceSubDivisions() {
    associatedReferenceSubDivisions = new HashSet<>();
    for(ReferenceBasePersonAssociation association : referenceBasePersonAssociations) {
      if(association.getReferenceBase() instanceof ReferenceSubDivision) {
        associatedReferenceSubDivisions.add((ReferenceSubDivision)association.getReferenceBase());
      }
    }
  }

  public DeepThought getDeepThought() {
    return deepThought;
  }

  protected void setDeepThought(DeepThought deepThought) {
    Object previousValue = this.deepThought;
    this.deepThought = deepThought;
    callPropertyChangedListeners(TableConfig.PersonDeepThoughtJoinColumnName, previousValue, deepThought);
  }



  @Transient
  public String getNameRepresentation() {
    String representation = "";

    representation += getLastName();

    if(StringUtils.isNotNullOrEmpty(firstName)) {
      if(representation.length() > 0)
        representation += ", ";

      representation += firstName;
    }

    return representation;
  }

  public String getNameRepresentationStartingWithFirstName() {
    String representation = "";

    representation += getFirstName();

    if(StringUtils.isNotNullOrEmpty(lastName)) {
      if(representation.length() > 0)
        representation += " ";

      representation += lastName;
    }

    return representation;
  }


  @Override
  @Transient
  public String getTextRepresentation() {
    return "Person " + getNameRepresentation();
  }

  @Override
  public String toString() {
    return getTextRepresentation();
  }


  @Override
  public int compareTo(Person other) {
    if(other == null)
      return 1;

    if(lastName.equals(other.getLastName()) == false)
      return lastName.compareTo(other.getLastName());

    if(firstName.equals(other.getFirstName()) == false)
      return firstName.compareTo(other.getFirstName());

    return 0;
  }


  public static Person createPersonFromStringRepresentation(String stringRepresentation) {
    String firstName = "";
    String lastName = stringRepresentation;

    if(lastName.contains(",")) {
      try {
        firstName = lastName.substring(lastName.indexOf(",") + 1).trim();
        lastName = lastName.substring(0, lastName.indexOf(",")).trim();
      } catch(Exception ex) {
        log.warn("Could not split Person stringPresentation '" + stringRepresentation + "' to it's first and last name", ex);
      }
    }

    return new Person(firstName, lastName);
  }

}
