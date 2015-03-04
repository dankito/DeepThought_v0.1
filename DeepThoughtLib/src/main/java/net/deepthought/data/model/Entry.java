package net.deepthought.data.model;

import net.deepthought.data.model.enums.EntryContentFormat;
import net.deepthought.data.model.enums.Language;
import net.deepthought.data.model.enums.PersonRole;
import net.deepthought.data.model.enums.ReferenceIndicationUnit;
import net.deepthought.data.model.listener.EntryPersonListener;
import net.deepthought.data.persistence.db.TableConfig;
import net.deepthought.data.persistence.db.UserDataEntity;
import net.deepthought.util.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

/**
 * Created by ganymed on 16/12/14.
 */
@Entity(name = TableConfig.EntryTableName)
public class Entry extends UserDataEntity implements Serializable, Comparable<Entry> {

  private static final long serialVersionUID = 596730656893495215L;


  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = TableConfig.EntryParentEntryJoinColumnName)
  protected Entry parentEntry;

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "parentEntry"/*, cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }*/)
  protected Set<Entry> subEntries = new HashSet<>();


  @Column(name = TableConfig.EntryTitleColumnName, length = 512)
  protected String title = "";

  //  @Column(name = TableConfig.EntryAbstractColumnName, length = 2048)
  @Column(name = TableConfig.EntryAbstractColumnName)
  @Lob
  protected String abstractString; // field cannot be named 'abstract' as this is a Java Keyword. So i named field abstractString but getter is called getAbstract()

  @Column(name = TableConfig.EntryContentColumnName)
//  @Column(name = TableConfig.EntryContentColumnName, columnDefinition = "clob") // Derby needs explicitly clob column definition
  @Lob
  protected String content = "";

  @Column(name = TableConfig.EntryContentFormatColumnName)
  protected EntryContentFormat contentFormat = EntryContentFormat.PlainText;

  @Column(name = TableConfig.EntryEntryIndexColumnName)
  protected int entryIndex;

  @ManyToMany(fetch = FetchType.EAGER, mappedBy = "entries") // TODO: has cascade also to be set to { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH } as in Category?
  protected Set<Category> categories = new HashSet<>();

  @ManyToMany(fetch = FetchType.EAGER/*, cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }*/ )
  @JoinTable(
      name = TableConfig.EntryTagJoinTableName,
      joinColumns = { @JoinColumn(name = TableConfig.EntryTagJoinTableEntryIdColumnName, referencedColumnName = "id") },
      inverseJoinColumns = { @JoinColumn(name = TableConfig.EntryTagJoinTableTagIdColumnName, referencedColumnName = "id") }
  )
//  @OrderBy("name ASC")
  protected Set<Tag> tags = new HashSet<>();

  @ManyToMany(fetch = FetchType.EAGER/*, cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }*/ )
  @JoinTable(
      name = TableConfig.EntryIndexTermJoinTableName,
      joinColumns = { @JoinColumn(name = TableConfig.EntryIndexTermJoinTableEntryIdColumnName, referencedColumnName = "id") },
      inverseJoinColumns = { @JoinColumn(name = TableConfig.EntryIndexTermJoinTableIndexTermIdColumnName, referencedColumnName = "id") }
  )
  protected Set<IndexTerm> indexTerms = new HashSet<>();

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "entry", cascade = CascadeType.PERSIST)
  protected Set<EntryPersonAssociation> entryPersonAssociations = new HashSet<>();

  protected transient Map<PersonRole, Set<Person>> personRoles = null;

  @OneToMany(fetch = FetchType.EAGER, mappedBy = "entry", cascade = CascadeType.PERSIST)
  protected Set<Note> notes = new HashSet<>();

  @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
  @JoinTable(
      name = TableConfig.EntryEntriesLinkGroupJoinTableName,
      joinColumns = { @JoinColumn(name = TableConfig.EntryEntriesLinkGroupJoinTableEntryIdColumnName, referencedColumnName = "id") },
      inverseJoinColumns = { @JoinColumn(name = TableConfig.EntryEntriesLinkGroupJoinTableLinkGroupIdColumnName, referencedColumnName = "id") }
  )
  protected Set<EntriesLinkGroup> linkGroups = new HashSet<>();

  @OneToMany(fetch = FetchType.EAGER, mappedBy = "entry", cascade = CascadeType.PERSIST)
  protected Set<FileLink> files = new HashSet<>();

  @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
  @JoinColumn(name = TableConfig.EntryPreviewImageJoinColumnName)
  protected FileLink previewImage;

  @Column(name = TableConfig.EntryEvaluationColumnName)
  protected String evaluation;

  @Column(name = TableConfig.EntryRatingColumnName)
  protected Integer rating;

  // Reference

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = TableConfig.EntrySeriesTitleJoinColumnName)
  protected SeriesTitle series;

  @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
  @JoinColumn(name = TableConfig.EntryReferenceJoinColumnName)
  protected Reference reference;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = TableConfig.EntryReferenceSubDivisionJoinColumnName)
  protected ReferenceSubDivision referenceSubDivision;

  @Column(name = TableConfig.EntryIndicationStartColumnName)
  protected String indicationStart;

  @OneToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = TableConfig.EntryIndicationStartUnitJoinColumnName)
  protected ReferenceIndicationUnit indicationStartUnit;

  @Column(name = TableConfig.EntryIndicationEndColumnName)
  protected String indicationEnd;

  @OneToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = TableConfig.EntryIndicationEndUnitJoinColumnName)
  protected ReferenceIndicationUnit indicationEndUnit;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = TableConfig.EntryLanguageJoinColumnName)
  protected Language language;

  @Column(name = TableConfig.EntrySpecificField01ColumnName)
  protected String specificField01;

  @Column(name = TableConfig.EntrySpecificField02ColumnName)
  protected String specificField02;

  @Column(name = TableConfig.EntrySpecificField03ColumnName)
  protected String specificField03;

  @Column(name = TableConfig.EntrySpecificField04ColumnName)
  protected String specificField04;

  @Column(name = TableConfig.EntrySpecificField05ColumnName)
  protected String specificField05;

  @Column(name = TableConfig.EntrySpecificField06ColumnName)
  protected String specificField06;

  @Column(name = TableConfig.EntrySpecificField07ColumnName)
  protected String specificField07;

  @Column(name = TableConfig.EntryCustomField01ColumnName)
  protected String customField01;

  @Column(name = TableConfig.EntryCustomField02ColumnName)
  protected String customField02;

  @Column(name = TableConfig.EntryCustomField03ColumnName)
  protected String customField03;

  @Column(name = TableConfig.EntryCustomField04ColumnName)
  protected String customField04;

  @Column(name = TableConfig.EntryCustomField05ColumnName)
  protected String customField05;

  @Column(name = TableConfig.EntryCustomField06ColumnName)
  protected String customField06;

  @Column(name = TableConfig.EntryCustomField07ColumnName)
  protected String customField07;

  @Column(name = TableConfig.EntryCustomField08ColumnName)
  protected String customField08;

  @Column(name = TableConfig.EntryCustomField09ColumnName)
  protected String customField09;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = TableConfig.EntryDeepThoughtJoinColumnName)
  protected DeepThought deepThought;


  protected transient Set<EntryPersonListener> entryPersonListeners = new HashSet<>();



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
    String previousTitle = this.title;
    this.title = title;
    callPropertyChangedListeners(TableConfig.EntryTitleColumnName, previousTitle, title);
  }

  public String getAbstract() {
    return abstractString;
  }

  public void setAbstract(String abstractString) {
    String previousAbstract = this.abstractString;
    this.abstractString = abstractString;
    callPropertyChangedListeners(TableConfig.EntryAbstractColumnName, previousAbstract, abstractString);
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    String previousContent = this.content;
    this.content = content;
    callPropertyChangedListeners(TableConfig.EntryContentColumnName, previousContent, content);
  }

  public EntryContentFormat getContentFormat() {
    return contentFormat;
  }

  public void setContentFormat(EntryContentFormat contentFormat) {
    EntryContentFormat previousContentFormat = this.contentFormat;
    this.contentFormat = contentFormat;
    callPropertyChangedListeners(TableConfig.EntryContentFormatColumnName, previousContentFormat, contentFormat);
  }

  public FileLink getPreviewImage() {
    return previewImage;
  }

  public void setPreviewImage(FileLink previewImage) {
    FileLink previousPreviewImage = this.previewImage;
    this.previewImage = previewImage;
    callPropertyChangedListeners(TableConfig.EntryPreviewImageJoinColumnName, previousPreviewImage, previewImage);
  }

  public SeriesTitle getSeries() {
    return series;
  }

  public void setSeries(SeriesTitle series) {
    Object previousValue = this.series;

    if(this.series != null)
      this.series.removeEntry(this);

    this.series = series;

    if(series != null) {
      series.addEntry(this);

      if (getReference() != null && series.containsSerialParts(getReference()) == false)
        setReference(null);
    }
    else if(getReference() != null && getReference().getSeries() != null)
      setReference(null);

    callPropertyChangedListeners(TableConfig.EntrySeriesTitleJoinColumnName, previousValue, series);
  }

  public Reference getReference() {
    return reference;
  }

  public void setReference(Reference reference) {
    Object previousValue = this.reference;

    if(this.reference != null)
      this.reference.removeEntry(this);

    this.reference = reference;

    if(reference != null) {
      reference.addEntry(this);
      if(reference.getSeries() != series)
        setSeries(reference.getSeries());
      if(referenceSubDivision != null && reference.getSubDivisions().contains(referenceSubDivision) == false)
        setReferenceSubDivision(null);
    }
    else {
      if(referenceSubDivision != null)
        setReferenceSubDivision(null);
    }

    callPropertyChangedListeners(TableConfig.EntryReferenceJoinColumnName, previousValue, reference);
  }

  public ReferenceSubDivision getReferenceSubDivision() {
    return referenceSubDivision;
  }

  public void setReferenceSubDivision(ReferenceSubDivision referenceSubDivision) {
    Object previousValue = this.referenceSubDivision;

    if(this.referenceSubDivision != null)
      this.referenceSubDivision.removeEntry(this);

    this.referenceSubDivision = referenceSubDivision;

    if(referenceSubDivision != null) {
      referenceSubDivision.addEntry(this);

      if(referenceSubDivision.getReference() != reference)
        setReference(referenceSubDivision.getReference());
    }

    callPropertyChangedListeners(TableConfig.EntryReferenceSubDivisionJoinColumnName, previousValue, referenceSubDivision);
  }

  public String getIndicationStart() {
    return indicationStart;
  }

  public void setIndicationStart(String indicationStart) {
    Object previousValue = this.indicationStart;
    this.indicationStart = indicationStart;
    callPropertyChangedListeners(TableConfig.EntryIndicationStartColumnName, previousValue, indicationStart);
  }

  public ReferenceIndicationUnit getIndicationStartUnit() {
    return indicationStartUnit;
  }

  public void setIndicationStartUnit(ReferenceIndicationUnit indicationStartUnit) {
    Object previousValue = this.indicationStartUnit;
    this.indicationStartUnit = indicationStartUnit;
    callPropertyChangedListeners(TableConfig.EntryIndicationStartUnitJoinColumnName, previousValue, indicationStartUnit);
  }

  public String getIndicationEnd() {
    return indicationEnd;
  }

  public void setIndicationEnd(String indicationEnd) {
    Object previousValue = this.indicationEnd;
    this.indicationEnd = indicationEnd;
    callPropertyChangedListeners(TableConfig.EntryIndicationEndColumnName, previousValue, indicationEnd);
  }

  public ReferenceIndicationUnit getIndicationEndUnit() {
    return indicationEndUnit;
  }

  public void setIndicationEndUnit(ReferenceIndicationUnit indicationEndUnit) {
    Object previousValue = this.indicationEndUnit;
    this.indicationEndUnit = indicationEndUnit;
    callPropertyChangedListeners(TableConfig.EntryIndicationEndUnitJoinColumnName, previousValue, indicationEndUnit);
  }

  public String getEvaluation() {
    return evaluation;
  }

  public void setEvaluation(String evaluation) {
    Object previousValue = this.evaluation;
    this.evaluation = evaluation;
    callPropertyChangedListeners(TableConfig.EntryEvaluationColumnName, previousValue, evaluation);
  }

  public Integer getRating() {
    return rating;
  }

  public void setRating(Integer rating) {
    Object previousValue = this.rating;
    this.rating = rating;
    callPropertyChangedListeners(TableConfig.EntryRatingColumnName, previousValue, rating);
  }

  public Entry getParentEntry() {
    return parentEntry;
  }

  public boolean hasSubEntries() {
    return getSubEntries().size() > 0;
  }

  public Collection<Entry> getSubEntries() {
    return subEntries;
  }

  public boolean addSubEntry(Entry subEntry) {
    subEntry.parentEntry = this;

    boolean result = subEntries.add(subEntry);
    if(result) {
      callEntityAddedListeners(subEntries, subEntry);
    }

    return result;
  }

  public boolean removeSubEntry(Entry subEntry) {
    boolean result = subEntries.remove(subEntry);
    if(result) {
      subEntry.parentEntry = null;
      callEntityRemovedListeners(subEntries, subEntry);
    }

    return result;
  }

  public boolean containsSubEntry(Entry subEntry) {
    return subEntries.contains(subEntry);
  }


  public boolean hasCategories() {
    return categories.size() > 0;
  }

  public Collection<Category> getCategories() {
    return categories;
  }

  protected boolean addCategory(Category category) {
    boolean result = categories.add(category);
    callEntityAddedListeners(categories, category);
    return result;
  }

  protected boolean removeCategory(Category category) {
    boolean result = categories.remove(category);
    callEntityRemovedListeners(categories, category);
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
      callEntityAddedListeners(tags, tag);
    }

    return result;
  }

  public boolean removeTag(Tag tag) {
    boolean result = tags.remove(tag);
    if(result) {
      tag.removeEntry(this);
      callEntityRemovedListeners(tags, tag);
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

  public boolean hasIndexTerms() {
    return getIndexTerms().size() > 0;
  }

  public Collection<IndexTerm> getIndexTerms() {
    return indexTerms;
  }

  public boolean addIndexTerm(IndexTerm indexTerm) {
    boolean result = indexTerms.add(indexTerm);
    if(result) {
      indexTerm.addEntry(this);
      callEntityAddedListeners(indexTerms, indexTerm);
    }

    return result;
  }

  public boolean removeIndexTerm(IndexTerm indexTerm) {
    boolean result = indexTerms.remove(indexTerm);
    if(result) {
      indexTerm.removeEntry(this);
      callEntityRemovedListeners(indexTerms, indexTerm);
    }

    return result;
  }

  public boolean hasIndexTerm(IndexTerm indexTerm) {
    return indexTerms.contains(indexTerm);
  }

  public boolean hasIndexTerms(Collection<IndexTerm> indexTerms) {
    boolean result = true;

    for(IndexTerm indexTerm : indexTerms)
      result &= hasIndexTerm(indexTerm);

    return result;
  }

  @Transient
  public String getIndexTermsStringRepresentation() {
    String indexTermsString = "";
    for(IndexTerm indexTerm : getIndexTerms())
      indexTermsString += indexTerm.getName() + ", ";

    if(indexTermsString.length() > 2)
      indexTermsString = indexTermsString.substring(0, indexTermsString.length() - 2);

    return indexTermsString;
  }


  public boolean hasPersons() {
    return entryPersonAssociations.size() > 0;
  }

  public Set<EntryPersonAssociation> getEntryPersonAssociations() {
    return entryPersonAssociations;
  }

  public boolean addPerson(Person person, PersonRole role) {
    Set<Person> personsAlreadyInRole = getPersonsForRole(role);
    EntryPersonAssociation entryPersonAssociation = new EntryPersonAssociation(this, person, role, personsAlreadyInRole == null ? 0 : personsAlreadyInRole.size());

    boolean result = this.entryPersonAssociations.add(entryPersonAssociation);
    result &= person.addEntry(entryPersonAssociation);
    result &= role.addEntry(entryPersonAssociation);

    personRoles = null;
    callPersonAddedListeners(role, person);
    callEntityAddedListeners(entryPersonAssociations, entryPersonAssociation);

    return result;
  }

  public boolean removePerson(Person person, PersonRole role) {
    EntryPersonAssociation entryPersonAssociation = findEntryPersonAssociation(person, role);
    if(entryPersonAssociation == null)
      return false;

    int removeIndex = entryPersonAssociation.getPersonOrder();

    boolean result = this.entryPersonAssociations.remove(entryPersonAssociation);
    result &= person.removeEntry(entryPersonAssociation);
    result &= role.removeEntry(entryPersonAssociation);

    personRoles = null;

    for(EntryPersonAssociation association : entryPersonAssociations) {
      if(association.getPersonOrder() > removeIndex)
        association.setPersonOrder(association.getPersonOrder() - 1);
    }

    callEntityRemovedListeners(entryPersonAssociations, entryPersonAssociation);
    callPersonRemovedListeners(role, person);

    return result;
  }

  protected EntryPersonAssociation findEntryPersonAssociation(Person person, PersonRole role) {
    for(EntryPersonAssociation association : this.entryPersonAssociations) {
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
    for(EntryPersonAssociation association : entryPersonAssociations) {
      if(personRoles.containsKey(association.getRole()) == false)
        personRoles.put(association.getRole(), new HashSet<Person>());
      personRoles.get(association.getRole()).add(association.getPerson());
    }
  }


  public Collection<Note> getNotes() {
    return notes;
  }

  public boolean addNote(Note note) {
    boolean result = notes.add(note);
    if(result) {
      note.setEntry(this);
      callEntityAddedListeners(notes, note);
    }

    return result;
  }

  public boolean removeNote(Note note) {
    boolean result = notes.remove(note);
    if(result) {
      note.setEntry(null);
      callEntityRemovedListeners(notes, note);
    }

    return result;
  }


  public Set<EntriesLinkGroup> getLinkGroups() {
    return linkGroups;
  }

  public boolean addLinkGroup(EntriesLinkGroup link) {
    boolean result = linkGroups.add(link);
    if(result) {
      link.addEntryToGroup(this);
      callEntityAddedListeners(linkGroups, link);
    }

    return result;
  }

  public boolean removeLinkGroup(EntriesLinkGroup link) {
    boolean result = linkGroups.remove(link);
    if(result) {
      link.removeEntryFromGroup(this);
      callEntityRemovedListeners(linkGroups, link);
    }

    return result;
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
      file.setEntry(this);
      callEntityAddedListeners(files, file);
    }

    return result;
  }

  public boolean removeFile(FileLink file) {
    boolean result = files.remove(file);
    if(result) {
      file.setEntry(null);
      callEntityRemovedListeners(files, file);
    }

    return result;
  }


  // TODO: remove again
  public DeepThought getDeepThought() {
    return deepThought;
  }


  public int getEntryIndex() {
    return entryIndex;
  }

  protected void setEntryIndex(int entryIndex) {
    Object previousValue = this.entryIndex;
    this.entryIndex = entryIndex;
    callPropertyChangedListeners(TableConfig.EntryEntryIndexColumnName, previousValue, entryIndex);
  }

  public Language getLanguage() {
    return language;
  }

  public void setLanguage(Language language) {
    Object previousValue = this.language;
    this.language = language;
    callPropertyChangedListeners(TableConfig.EntryLanguageJoinColumnName, previousValue, language);
  }

  public String getSpecificField01() {
    return specificField01;
  }

  public void setSpecificField01(String specificField01) {
    this.specificField01 = specificField01;
  }

  public String getSpecificField02() {
    return specificField02;
  }

  public void setSpecificField02(String specificField02) {
    this.specificField02 = specificField02;
  }

  public String getSpecificField03() {
    return specificField03;
  }

  public void setSpecificField03(String specificField03) {
    this.specificField03 = specificField03;
  }

  public String getSpecificField04() {
    return specificField04;
  }

  public void setSpecificField04(String specificField04) {
    this.specificField04 = specificField04;
  }

  public String getSpecificField05() {
    return specificField05;
  }

  public void setSpecificField05(String specificField05) {
    this.specificField05 = specificField05;
  }

  public String getSpecificField06() {
    return specificField06;
  }

  public void setSpecificField06(String specificField06) {
    this.specificField06 = specificField06;
  }

  public String getSpecificField07() {
    return specificField07;
  }

  public void setSpecificField07(String specificField07) {
    this.specificField07 = specificField07;
  }

  public String getCustomField01() {
    return customField01;
  }

  public void setCustomField01(String customField01) {
    this.customField01 = customField01;
  }

  public String getCustomField02() {
    return customField02;
  }

  public void setCustomField02(String customField02) {
    this.customField02 = customField02;
  }

  public String getCustomField03() {
    return customField03;
  }

  public void setCustomField03(String customField03) {
    this.customField03 = customField03;
  }

  public String getCustomField04() {
    return customField04;
  }

  public void setCustomField04(String customField04) {
    this.customField04 = customField04;
  }

  public String getCustomField05() {
    return customField05;
  }

  public void setCustomField05(String customField05) {
    this.customField05 = customField05;
  }

  public String getCustomField06() {
    return customField06;
  }

  public void setCustomField06(String customField06) {
    this.customField06 = customField06;
  }

  public String getCustomField07() {
    return customField07;
  }

  public void setCustomField07(String customField07) {
    this.customField07 = customField07;
  }

  public String getCustomField08() {
    return customField08;
  }

  public void setCustomField08(String customField08) {
    this.customField08 = customField08;
  }

  public String getCustomField09() {
    return customField09;
  }

  public void setCustomField09(String customField09) {
    this.customField09 = customField09;
  }


  public boolean addEntryPersonListener(EntryPersonListener listener) {
    return entryPersonListeners.add(listener);
  }

  public boolean removeEntryPersonListener(EntryPersonListener listener) {
    return entryPersonListeners.remove(listener);
  }

  protected void callPersonAddedListeners(PersonRole role, Person person) {
    for(EntryPersonListener listener : entryPersonListeners)
      listener.personAdded(this, role, person);
  }

  protected void callPersonRemovedListeners(PersonRole role, Person person) {
    for(EntryPersonListener listener : entryPersonListeners)
      listener.personRemoved(this, role, person);
  }


  private final static int PreviewMaxLength = 100;

  @Transient
  public String getPreview() {
    String preview = "";

    if(StringUtils.isNotNullOrEmpty(title)) {
      preview += title;
    }
    if(StringUtils.isNotNullOrEmpty(abstractString)) {
      if(preview.length() > 0)
        preview += ": ";
      preview += abstractString;
    }

    if(preview.length() <= PreviewMaxLength && StringUtils.isNotNullOrEmpty(content)) {
      if(preview.length() > 0)
        preview += " - ";
      preview += content;
    }

    if(preview.length() >= PreviewMaxLength)
      preview = preview.substring(0, PreviewMaxLength) + " ...";

    preview = preview.replace("\r", "").replace("\n", "");

    return preview;
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
