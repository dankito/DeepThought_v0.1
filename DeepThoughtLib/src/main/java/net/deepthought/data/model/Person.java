package net.deepthought.data.model;

import net.deepthought.data.model.enums.Gender;
import net.deepthought.data.model.enums.PersonRole;
import net.deepthought.data.persistence.db.TableConfig;
import net.deepthought.data.persistence.db.UserDataEntity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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

  @Column(name = TableConfig.PersonMiddleNamesColumnName)
  protected String middleNames;

  @Column(name = TableConfig.PersonLastNameColumnName)
  protected String lastName = "";

  @Column(name = TableConfig.PersonTitleColumnName)
  protected String title;

  @Column(name = TableConfig.PersonPrefixColumnName)
  protected String prefix;

  @Column(name = TableConfig.PersonSuffixColumnName)
  protected String suffix;

  @Column(name = TableConfig.PersonAbbreviationColumnName)
  protected String abbreviation;

  @Column(name = TableConfig.PersonGenderColumnName)
  protected Gender gender = Gender.Unset;

  @Column(name = TableConfig.PersonBirthDayColumnName)
//  @Transient
  protected Date birthDate;

  @Column(name = TableConfig.PersonNotesColumnName)
  protected String notes;

  @Column(name = TableConfig.PersonSortByColumnName)
  protected String sortBy;

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "person")
  protected Set<EntryPersonAssociation> entryPersonAssociations = new HashSet<>();

  protected transient Map<Entry, Set<PersonRole>> entryRoles = null;

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "person")
  protected Set<ReferenceBasePersonAssociation> referenceBasePersonAssociations = new HashSet<>();

  protected transient Map<SeriesTitle, Set<PersonRole>> seriesRoles = null;

  protected transient Map<Reference, Set<PersonRole>> referenceRoles = null;

  protected transient Map<ReferenceSubDivision, Set<PersonRole>> referenceSubDivisionRoles = null;

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

  public String getMiddleNames() {
    return middleNames;
  }

  public void setMiddleNames(String middleNames) {
    String previousMiddleNames = this.middleNames;
    this.middleNames = middleNames;
    callPropertyChangedListeners(TableConfig.PersonMiddleNamesColumnName, previousMiddleNames, middleNames);
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    String previousLastName = this.lastName;
    this.lastName = lastName;
    callPropertyChangedListeners(TableConfig.PersonLastNameColumnName, previousLastName, lastName);
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    String previousTitle = this.title;
    this.title = title;
    callPropertyChangedListeners(TableConfig.PersonTitleColumnName, previousTitle, title);
  }

  public String getPrefix() {
    return prefix;
  }

  public void setPrefix(String prefix) {
    String previousPrefix = this.prefix;
    this.prefix = prefix;
    callPropertyChangedListeners(TableConfig.PersonPrefixColumnName, previousPrefix, prefix);
  }

  public String getSuffix() {
    return suffix;
  }

  public void setSuffix(String suffix) {
    String previousSuffix = this.suffix;
    this.suffix = suffix;
    callPropertyChangedListeners(TableConfig.PersonSuffixColumnName, previousSuffix, suffix);
  }

  public String getAbbreviation() {
    return abbreviation;
  }

  public void setAbbreviation(String abbreviation) {
    String previousAbbreviation = this.abbreviation;
    this.abbreviation = abbreviation;
    callPropertyChangedListeners(TableConfig.PersonAbbreviationColumnName, previousAbbreviation, abbreviation);
  }

  public Gender getGender() {
    return gender;
  }

  public void setGender(Gender gender) {
    Gender previousGender = this.gender; // sounds a bit funny, previousGender
    this.gender = gender;
    callPropertyChangedListeners(TableConfig.PersonGenderColumnName, previousGender, gender);
  }

  public Date getBirthDate() {
    return birthDate;
  }

  public void setBirthDate(Date birthDate) {
    Date previousBirthDate = this.birthDate;
    this.birthDate = birthDate;
    callPropertyChangedListeners(TableConfig.PersonBirthDayColumnName, previousBirthDate, birthDate);
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    String previousNotes = this.notes;
    this.notes = notes;
    callPropertyChangedListeners(TableConfig.PersonNotesColumnName, previousNotes, notes);
  }

  public String getSortBy() {
    return sortBy;
  }

  public void setSortBy(String sortBy) {
    String previousSortBy = this.sortBy;
    this.sortBy = sortBy;
    callPropertyChangedListeners(TableConfig.PersonAbbreviationColumnName, previousSortBy, sortBy);
  }



  protected boolean addEntry(EntryPersonAssociation entryPersonAssociation) {
    boolean result = entryPersonAssociations.add(entryPersonAssociation);

    if(result) {
      entryRoles = null;
      callEntityAddedListeners(entryPersonAssociations, entryPersonAssociation);
    }

    return result;
  }

  protected boolean removeEntry(EntryPersonAssociation entryPersonAssociation) {
    boolean result = entryPersonAssociations.remove(entryPersonAssociation);

    if(result) {
      entryRoles = null;
      callEntityRemovedListeners(entryPersonAssociations, entryPersonAssociation);
    }

    return result;
  }

  public boolean isSetOnEntries() {
    return getAssociatedEntries().size() > 0;
  }

  public Set<Entry> getAssociatedEntries() {
    if(entryRoles == null)
      createEntryRoles();

    return entryRoles.keySet();
  }

  public Set<PersonRole> getRolesForEntry(Entry entry) {
    if(entryRoles == null)
      createEntryRoles();

    return entryRoles.get(entry);
  }

  protected void createEntryRoles() {
    entryRoles = new HashMap<>();
    for(EntryPersonAssociation association : entryPersonAssociations) {
      if(entryRoles.containsKey(association.getEntry()) == false)
        entryRoles.put(association.getEntry(), new HashSet<PersonRole>());
      entryRoles.get(association.getEntry()).add(association.getRole());
    }
  }


  protected boolean addReference(ReferenceBasePersonAssociation referenceBasePersonAssociation) {
    boolean result = referenceBasePersonAssociations.add(referenceBasePersonAssociation);

    if(result) {
      referenceRoles = null;
      callEntityAddedListeners(referenceBasePersonAssociations, referenceBasePersonAssociation);
    }

    return result;
  }

  protected boolean removeReference(ReferenceBasePersonAssociation referenceBasePersonAssociation) {
    boolean result = referenceBasePersonAssociations.remove(referenceBasePersonAssociation);

    if(result) {
      referenceRoles = null;
      callEntityRemovedListeners(referenceBasePersonAssociations, referenceBasePersonAssociation);
    }

    return result;
  }

  public boolean isSetOnSeries() {
    return getAssociatedSeries().size() > 0;
  }

  public Set<SeriesTitle> getAssociatedSeries() {
    if(seriesRoles == null)
      createSeriesRoles();

    return seriesRoles.keySet();
  }

  public Set<PersonRole> getRolesForSeries(SeriesTitle series) {
    if(seriesRoles == null)
      createSeriesRoles();

    return seriesRoles.get(series);
  }

  protected void createSeriesRoles() {
    seriesRoles = new HashMap<>();
    for(ReferenceBasePersonAssociation association : referenceBasePersonAssociations) {
      if(association.getReferenceBase() instanceof SeriesTitle) {
        if(seriesRoles.containsKey(association.getReferenceBase()) == false)
          seriesRoles.put((SeriesTitle)association.getReferenceBase(), new HashSet<PersonRole>());
        seriesRoles.get(association.getReferenceBase()).add(association.getRole());
      }
    }
  }

  public boolean isSetOnReferences() {
    return getAssociatedReferences().size() > 0;
  }

  public Set<Reference> getAssociatedReferences() {
    if(referenceRoles == null)
      createReferenceRoles();

    return referenceRoles.keySet();
  }

  public Set<PersonRole> getRolesForReference(Reference reference) {
    if(referenceRoles == null)
      createReferenceRoles();

    return referenceRoles.get(reference);
  }

  protected void createReferenceRoles() {
    referenceRoles = new HashMap<>();
    for(ReferenceBasePersonAssociation association : referenceBasePersonAssociations) {
      if(association.getReferenceBase() instanceof Reference) {
        if(referenceRoles.containsKey(association.getReferenceBase()) == false)
          referenceRoles.put((Reference)association.getReferenceBase(), new HashSet<PersonRole>());
        referenceRoles.get(association.getReferenceBase()).add(association.getRole());
      }
    }
  }

  public boolean isSetOnReferenceSubDivisions() {
    return getAssociatedReferenceSubDivisions().size() > 0;
  }

  public Set<ReferenceSubDivision> getAssociatedReferenceSubDivisions() {
    if(referenceSubDivisionRoles == null)
      createReferenceSubDivisionRoles();

    return referenceSubDivisionRoles.keySet();
  }

  public Set<PersonRole> getRolesForSubDivision(ReferenceSubDivision subDivision) {
    if(referenceSubDivisionRoles == null)
      createReferenceSubDivisionRoles();

    return referenceSubDivisionRoles.get(subDivision);
  }

  protected void createReferenceSubDivisionRoles() {
    referenceSubDivisionRoles = new HashMap<>();
    for(ReferenceBasePersonAssociation association : referenceBasePersonAssociations) {
      if(association.getReferenceBase() instanceof ReferenceSubDivision) {
        if(referenceSubDivisionRoles.containsKey(association.getReferenceBase()) == false)
          referenceSubDivisionRoles.put((ReferenceSubDivision)association.getReferenceBase(), new HashSet<PersonRole>());
        referenceSubDivisionRoles.get(association.getReferenceBase()).add(association.getRole());
      }
    }
  }



  @Transient
  public String getNameRepresentation() {
    String representation = "";

    representation += getLastName();

    if(suffix != null && representation.length() > 0)
      representation += " " + suffix;

    if(firstName != null && firstName.isEmpty() == false) {
      if(representation.length() > 0)
        representation += ", ";

      if(title != null && representation.length() > 0)
        representation += title + " ";

      representation += firstName;

      if(middleNames != null)
        representation += " " + middleNames;
    }

    if(abbreviation != null)
      representation += " " + abbreviation;

    if(prefix != null)
      representation += " " + prefix;

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

    String thisFirstCompareLevel = sortBy != null ? sortBy : lastName;
    String otherFirstCompareLevel = other.getSortBy() != null ? other.getSortBy() : other.getLastName();

    if(thisFirstCompareLevel.equals(otherFirstCompareLevel) == false)
      return thisFirstCompareLevel.compareTo(otherFirstCompareLevel);

    if(firstName.equals(other.getFirstName()) == false)
      return firstName.compareTo(other.getFirstName());

    if(middleNames != null) {
      if (other.getMiddleNames() == null)
        return 1;
      return middleNames.compareTo(other.getMiddleNames());
    }

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
