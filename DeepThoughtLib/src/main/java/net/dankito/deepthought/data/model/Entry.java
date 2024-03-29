package net.dankito.deepthought.data.model;

import net.dankito.deepthought.Application;
import net.dankito.deepthought.data.model.enums.Language;
import net.dankito.deepthought.data.model.listener.EntryPersonListener;
import net.dankito.deepthought.data.persistence.db.TableConfig;
import net.dankito.deepthought.data.persistence.db.UserDataEntity;
import net.dankito.deepthought.util.localization.Localization;
import net.dankito.deepthought.util.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
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


  @Column(name = TableConfig.EntryTitleColumnName)
  protected String title = "";

  //  @Column(name = TableConfig.EntryAbstractColumnName, length = 2048)
  @Column(name = TableConfig.EntryAbstractColumnName)
  @Lob
  protected String abstractString = ""; // field cannot be named 'abstract' as this is a Java Keyword. So i named field abstractString but getter is called getAbstract()

  protected transient String plainTextAbstract = null;

  @Column(name = TableConfig.EntryContentColumnName)
//  @Column(name = TableConfig.EntryContentColumnName, columnDefinition = "clob") // Derby needs explicitly clob column definition
  @Lob
  protected String content = "";

  protected transient String plainTextContent = null;

  @Column(name = TableConfig.EntryEntryIndexColumnName)
  protected int entryIndex;

  @ManyToMany(fetch = FetchType.LAZY )
  @JoinTable(
      name = TableConfig.EntryCategoryJoinTableName,
      joinColumns = { @JoinColumn(name = TableConfig.EntryCategoryJoinTableEntryIdColumnName) },
      inverseJoinColumns = { @JoinColumn(name = TableConfig.EntryCategoryJoinTableCategoryIdColumnName) }
  )
  protected Set<Category> categories = new HashSet<>();

  @ManyToMany(fetch = FetchType.LAZY/*, cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }*/ )
  @JoinTable(
      name = TableConfig.EntryTagJoinTableName,
      joinColumns = { @JoinColumn(name = TableConfig.EntryTagJoinTableEntryIdColumnName/*, referencedColumnName = "id"*/) },
      inverseJoinColumns = { @JoinColumn(name = TableConfig.EntryTagJoinTableTagIdColumnName/*, referencedColumnName = "id"*/) }
  )
//  @OrderBy("name ASC")
  protected Set<Tag> tags = new HashSet<>();

  protected transient Collection<Tag> sortedTags = null;

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "entry", cascade = CascadeType.PERSIST)
  protected Set<EntryPersonAssociation> entryPersonAssociations = new HashSet<>();

  protected transient Set<Person> persons = null;

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "entry", cascade = CascadeType.PERSIST)
  protected Set<Note> notes = new HashSet<>();

  @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
  @JoinTable(
      name = TableConfig.EntryEntriesGroupJoinTableName,
      joinColumns = { @JoinColumn(name = TableConfig.EntryEntriesGroupJoinTableEntryIdColumnName/*, referencedColumnName = "id"*/) },
      inverseJoinColumns = { @JoinColumn(name = TableConfig.EntryEntriesGroupJoinTableEntriesGroupIdColumnName/*, referencedColumnName = "id"*/) }
  )
  protected Set<EntriesGroup> entryGroups = new HashSet<>();

  @ManyToMany(fetch = FetchType.LAZY )
  @JoinTable(
      name = TableConfig.EntryAttachedFilesJoinTableName,
      joinColumns = { @JoinColumn(name = TableConfig.EntryAttachedFilesJoinTableEntryIdColumnName/*, referencedColumnName = "id"*/) },
      inverseJoinColumns = { @JoinColumn(name = TableConfig.EntryAttachedFilesJoinTableFileLinkIdColumnName/*, referencedColumnName = "id"*/) }
  )
  protected Set<FileLink> attachedFiles = new HashSet<>();

  @ManyToMany(fetch = FetchType.LAZY )
  @JoinTable(
      name = TableConfig.EntryEmbeddedFilesJoinTableName,
      joinColumns = { @JoinColumn(name = TableConfig.EntryEmbeddedFilesJoinTableEntryIdColumnName/*, referencedColumnName = "id"*/) },
      inverseJoinColumns = { @JoinColumn(name = TableConfig.EntryEmbeddedFilesJoinTableFileLinkIdColumnName/*, referencedColumnName = "id"*/) }
  )
  protected Set<FileLink> embeddedFiles = new HashSet<>();

  @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
  @JoinColumn(name = TableConfig.EntryPreviewImageJoinColumnName)
  protected FileLink previewImage;

  // Reference

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = TableConfig.EntrySeriesTitleJoinColumnName)
  protected SeriesTitle series;

  @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
  @JoinColumn(name = TableConfig.EntryReferenceJoinColumnName)
  protected Reference reference;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = TableConfig.EntryReferenceSubDivisionJoinColumnName)
  protected ReferenceSubDivision referenceSubDivision;

  @Column(name = TableConfig.EntryIndicationColumnName)
  protected String indication;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = TableConfig.EntryLanguageJoinColumnName)
  protected Language language;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = TableConfig.EntryDeepThoughtJoinColumnName)
  protected DeepThought deepThought;


  protected transient Set<EntryPersonListener> entryPersonListeners = new HashSet<>();



  public Entry() {

  }

  public Entry(String content) {
    this.content = content;
  }

  public Entry(String content, String abstractString) {
    this(content);
    this.abstractString = abstractString;
  }


  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    String previousTitle = this.title;
    this.title = title;
    preview = null;
    callPropertyChangedListeners(TableConfig.EntryTitleColumnName, previousTitle, title);
  }

  public boolean hasAbstract() {
    return StringUtils.isNotNullOrEmpty(getAbstractAsPlainText());
  }

  public String getAbstract() {
    return abstractString;
  }

  public String getAbstractAsPlainText() {
    if(plainTextAbstract == null) {
      if (Application.getHtmlHelper() != null)
        plainTextAbstract = Application.getHtmlHelper().extractPlainTextFromHtmlBody(abstractString);
      else
//        return content;
        return "";
    }
    return plainTextAbstract;
  }

  public void setAbstract(String abstractString) {
    String previousAbstract = this.abstractString;
    this.abstractString = abstractString == null ? "" : abstractString;
    plainTextAbstract = null;
    preview = null;
    longPreview = null;
    callPropertyChangedListeners(TableConfig.EntryAbstractColumnName, previousAbstract, abstractString);
  }

  public boolean hasContent() {
    return StringUtils.isNotNullOrEmpty(getContentAsPlainText());
  }

  public String getContent() {
    return content;
  }

  @Transient
  public String getContentAsPlainText() {
    if(plainTextContent == null) {
      if (Application.getHtmlHelper() != null)
        plainTextContent = Application.getHtmlHelper().extractPlainTextFromHtmlBody(content);
      else
//        return content;
        return "";
    }
    return plainTextContent;
  }

  public void setContent(String content) {
    String previousContent = this.content;
    this.content = content;
    plainTextContent = null;
    preview = null;
    longPreview = null;
    callPropertyChangedListeners(TableConfig.EntryContentColumnName, previousContent, content);
  }

  public FileLink getPreviewImage() {
    return previewImage;
  }

  public void setPreviewImage(FileLink previewImage) {
    FileLink previousPreviewImage = this.previewImage;
    this.previewImage = previewImage;

    if(previewImage.getDeepThought() == null && this.deepThought != null)
      deepThought.addFile(previewImage);

    callPropertyChangedListeners(TableConfig.EntryPreviewImageJoinColumnName, previousPreviewImage, previewImage);
  }

  public SeriesTitle getSeries() {
    return series;
  }

  public void setSeries(SeriesTitle series) {
    if(this.series == series)
      return;

    Object previousValue = this.series;

    if(this.series != null) {
      this.series.removeEntry(this);
      if(this.series.getSerialParts().contains(this.getReference()))
        setReference(null);
    }

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
    if(this.reference == reference)
      return;

    Object previousValue = this.reference;

    if(this.reference != null) {
      this.reference.removeEntry(this);
      if(this.reference.getSubDivisions().contains(this.getReferenceSubDivision()))
        setReferenceSubDivision(null);
    }

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
    if(this.referenceSubDivision == referenceSubDivision)
      return;

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

  public void clearReferenceBases() {
    setSeries(null);
  }

  public boolean isAReferenceSet() {
    return series != null || reference != null || referenceSubDivision != null;
  }

  @Transient
  public ReferenceBase getLowestReferenceBase() {
    if(referenceSubDivision != null)
      return referenceSubDivision;
    else if(reference != null)
      return reference;
    else if(series != null)
      return series;

    return null;
  }

  public String getIndication() {
    return indication;
  }

  public void setIndication(String indication) {
    Object previousValue = this.indication;
    this.indication = indication;
    callPropertyChangedListeners(TableConfig.EntryIndicationColumnName, previousValue, indication);
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
    if(subEntry.deepThought == null)
      Application.getDeepThought().addEntry(subEntry);

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

      if(deepThought != null /*&& this.equals(subEntry.getParentEntry())*/) // TODO: what is this.equals(subCategory.getParentCategory()) good for?
        deepThought.removeEntry(subEntry);

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

  public boolean addCategory(Category category) {
    if(categories.contains(category) == false) {
      if (categories.add(category)) {
        category.addEntry(this);

        callEntityAddedListeners(categories, category);
        return true;
      }
    }

    return false;
  }

  public boolean removeCategory(Category category) {
    boolean result = categories.remove(category);
    if(result) {
      category.removeEntry(this);
      callEntityRemovedListeners(categories, category);
    }

    return result;
  }


  public boolean hasTags() {
    return getCountTags() > 0;
  }

  public int getCountTags() {
    return getTags().size();
  }

  public Collection<Tag> getTags() {
    return tags;
  }

  @Transient
  public Collection<Tag> getTagsSorted() {
    if(sortedTags == null) {
      sortedTags = new ArrayList<>(getTags());
      Collections.sort((List) sortedTags);
    }

    return sortedTags;
  }

  public void setTags(Collection<Tag> newTags) {
    sortedTags = null;

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
    if (tags.contains(tag))
      return false;

    sortedTags = null;

    boolean result = tags.add(tag);
    if (result) {
      tag.addEntry(this);
      callEntityAddedListeners(tags, tag);
    }

    return result;
  }

  public boolean removeTag(Tag tag) {
    sortedTags = null;

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


  public boolean hasPersons() {
    return entryPersonAssociations.size() > 0;
  }

  public Set<EntryPersonAssociation> getEntryPersonAssociations() {
    return entryPersonAssociations;
  }

  public boolean addPerson(Person person) {
    EntryPersonAssociation existingAssociation = findEntryPersonAssociation(person);
    if(existingAssociation != null) // Person already added to Entry
      return false;

    persons = null;

    EntryPersonAssociation entryPersonAssociation = new EntryPersonAssociation(this, person, entryPersonAssociations.size());

    boolean result = this.entryPersonAssociations.add(entryPersonAssociation);
    result &= person.addEntry(entryPersonAssociation);

    callPersonAddedListeners(person);
    callEntityAddedListeners(entryPersonAssociations, entryPersonAssociation);

    return result;
  }

  public boolean removePerson(Person person) {
    EntryPersonAssociation entryPersonAssociation = findEntryPersonAssociation(person);
    if(entryPersonAssociation == null)
      return false;

    int removeIndex = entryPersonAssociation.getPersonOrder();

    boolean result = this.entryPersonAssociations.remove(entryPersonAssociation);
    result &= person.removeEntry(entryPersonAssociation);

    persons = null;

    for(EntryPersonAssociation association : entryPersonAssociations) {
      if(association.getPersonOrder() > removeIndex)
        association.setPersonOrder(association.getPersonOrder() - 1);
    }

    callEntityRemovedListeners(entryPersonAssociations, entryPersonAssociation);
    callPersonRemovedListeners(person);

    return result;
  }

  protected EntryPersonAssociation findEntryPersonAssociation(Person person) {
    for(EntryPersonAssociation association : this.entryPersonAssociations) {
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
    for(EntryPersonAssociation association : entryPersonAssociations) {
      persons.add(association.getPerson());
    }
  }

  public boolean hasPersonsOrIsAReferenceSet() {
    return hasPersons() || isAReferenceSet();
  }


  public boolean hasNotes() {
    return notes.size() > 0;
  }

  public Collection<Note> getNotes() {
    return notes;
  }

  public boolean addNote(Note note) {
    if(notes.contains(note))
      return false;
    boolean result = notes.add(note);
    if(result) {
      note.setEntry(this);

      if(note.getDeepThought() == null && this.deepThought != null)
        deepThought.addNote(note);

      callEntityAddedListeners(notes, note);
    }

    return result;
  }

  public boolean removeNote(Note note) {
    boolean result = notes.remove(note);
    if(result) {
      note.setEntry(null);
      if(deepThought != null)
        deepThought.removeNote(note);

      callEntityRemovedListeners(notes, note);
    }

    return result;
  }


  public boolean hasEntriesGroups() {
    return getEntriesGroups().size() > 0;
  }

  public Set<EntriesGroup> getEntriesGroups() {
    return entryGroups;
  }

  public boolean addEntriesGroup(EntriesGroup link) {
    if(entryGroups.contains(link))
      return false;

    boolean result = entryGroups.add(link);
    if(result) {
      link.addEntryToGroup(this);
      callEntityAddedListeners(entryGroups, link);
    }

    return result;
  }

  public boolean removeEntriesGroup(EntriesGroup link) {
    boolean result = entryGroups.remove(link);
    if(result) {
      link.removeEntryFromGroup(this);
      callEntityRemovedListeners(entryGroups, link);
    }

    return result;
  }


  public boolean hasAttachedFiles() {
    return attachedFiles.size() > 0;
  }

  public Collection<FileLink> getAttachedFiles() {
    return attachedFiles;
  }

  public boolean addAttachedFile(FileLink file) {
    if(attachedFiles.contains(file))
      return false;

    boolean result = attachedFiles.add(file);
    if(result) {
      file.addAsAttachmentToEntry(this);

      callEntityAddedListeners(attachedFiles, file);
    }

    return result;
  }

  public boolean removeAttachedFile(FileLink file) {
    boolean result = attachedFiles.remove(file);
    if(result) {
      file.removeAsAttachmentFromEntry(this);

      callEntityRemovedListeners(attachedFiles, file);
    }

    return result;
  }


  public boolean hasEmbeddedFiles() {
    return embeddedFiles.size() > 0;
  }

  public boolean containsEmbeddedFile(FileLink file) {
    return getEmbeddedFiles().contains(file);
  }

  public Collection<FileLink> getEmbeddedFiles() {
    return embeddedFiles;
  }

  public boolean addEmbeddedFile(FileLink file) {
    if(embeddedFiles.contains(file))
      return false;

    boolean result = embeddedFiles.add(file);
    if(result) {
      file.addAsEmbeddingToEntry(this);

      callEntityAddedListeners(embeddedFiles, file);
    }

    return result;
  }

  public boolean removeEmbeddedFile(FileLink file) {
    boolean result = embeddedFiles.remove(file);
    if(result) {
      file.removeAsEmbeddingFromEntry(this);

      callEntityRemovedListeners(embeddedFiles, file);
    }

    return result;
  }


  // TODO: remove again
  public DeepThought getDeepThought() {
    return deepThought;
  }

  protected void setDeepThought(DeepThought deepThought) {
    Object previousValue = this.deepThought;
    this.deepThought = deepThought;
    callPropertyChangedListeners(TableConfig.ReferenceSubDivisionDeepThoughtJoinColumnName, previousValue, deepThought);
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


  public boolean addEntryPersonListener(EntryPersonListener listener) {
    return entryPersonListeners.add(listener);
  }

  public boolean removeEntryPersonListener(EntryPersonListener listener) {
    return entryPersonListeners.remove(listener);
  }

  protected void callPersonAddedListeners(Person person) {
    for(EntryPersonListener listener : entryPersonListeners)
      listener.personAdded(this, person);
  }

  protected void callPersonRemovedListeners(Person person) {
    for(EntryPersonListener listener : entryPersonListeners)
      listener.personRemoved(this, person);
  }


  protected final static int PreviewMaxLength = 150;

  protected final static int LongPreviewMaxLength = 500;

  protected transient String preview = null;

  protected transient String longPreview = null;

  @Transient
  public String getPreview() {
    if(preview == null) {
      if(Application.getHtmlHelper() == null)
        return "";

      preview = determinePreview();
      if(preview.length() >= PreviewMaxLength)
        preview = preview.substring(0, PreviewMaxLength) + " ...";
    }

    return preview;
  }

  @Transient
  public String getLongPreview() {
    if(longPreview == null) {
      if(Application.getHtmlHelper() == null)
        return "";

      longPreview = determinePreview();
      if(longPreview.length() >= LongPreviewMaxLength)
        longPreview = longPreview.substring(0, LongPreviewMaxLength) + " ...";
    }

    return longPreview;
  }

  protected String determinePreview() {
    String preview = "";

    if (StringUtils.isNotNullOrEmpty(title))
      preview += title;

    if (StringUtils.isNotNullOrEmpty(getAbstractAsPlainText())) {
      if (preview.length() > 0)
        preview += ": ";
      preview += getAbstractAsPlainText();
    }

    if (preview.length() <= PreviewMaxLength && StringUtils.isNotNullOrEmpty(content)) {
      if (preview.length() > 0)
        preview += " - ";
      preview += getContentAsPlainText();
    }

    preview = preview.replace("\r", "").replace("\n", "");

    return preview;
  }


  @Override
  public boolean isPersisted() {
    return super.isPersisted() && deepThought != null;
  }

  @Override
  @Transient
  public String getTextRepresentation() {
    return getPreview();
  }

  @Override
  public String toString() {
    return "Entry " + getTextRepresentation();
  }


  @Override
  public int compareTo(Entry other) {
    if(other == null)
      return 1;
    return ((Integer)other.getEntryIndex()).compareTo(getEntryIndex());
  }


  public static Entry createTopLevelEntry() {
    Entry topLevelEntry = new Entry(Localization.getLocalizedString("i.know.me.nothing.knowing"));

    return topLevelEntry;
  }
}
