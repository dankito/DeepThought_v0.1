package net.dankito.deepthought.data.model;

import net.dankito.deepthought.data.model.enums.BackupFileServiceType;
import net.dankito.deepthought.data.model.enums.FileType;
import net.dankito.deepthought.data.model.enums.Language;
import net.dankito.deepthought.data.model.enums.NoteType;
import net.dankito.deepthought.data.model.listener.EntityListener;
import net.dankito.deepthought.data.model.listener.SettingsChangedListener;
import net.dankito.deepthought.data.model.settings.DeepThoughtSettings;
import net.dankito.deepthought.data.model.settings.SettingsBase;
import net.dankito.deepthought.data.model.settings.enums.Setting;
import net.dankito.deepthought.data.model.ui.AllEntriesSystemTag;
import net.dankito.deepthought.data.model.ui.EntriesWithoutTagsSystemTag;
import net.dankito.deepthought.data.persistence.db.TableConfig;
import net.dankito.deepthought.util.file.FileUtils;
import net.dankito.deepthought.data.persistence.db.BaseEntity;
import net.dankito.deepthought.data.persistence.db.UserDataEntity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.Transient;

/**
 * Created by ganymed on 01/10/14.
 */
@Entity(name = TableConfig.DeepThoughtTableName)
public class DeepThought extends UserDataEntity implements Serializable {

  private static final long serialVersionUID = 441616313532856392L;


  protected transient AllEntriesSystemTag allEntriesSystemTag = null;

  protected transient EntriesWithoutTagsSystemTag entriesWithoutTagsSystemTag = null;

  public AllEntriesSystemTag AllEntriesSystemTag() {
    if(allEntriesSystemTag == null) {
      allEntriesSystemTag = new AllEntriesSystemTag(this);
    }
    return allEntriesSystemTag;
  }

  public final EntriesWithoutTagsSystemTag EntriesWithoutTagsSystemTag() {
    if(entriesWithoutTagsSystemTag == null) {
      entriesWithoutTagsSystemTag = new EntriesWithoutTagsSystemTag(this);
    }
    return entriesWithoutTagsSystemTag;
  }


  private final static Logger log = LoggerFactory.getLogger(DeepThought.class);


  @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
  @JoinColumn(name = TableConfig.DeepThoughtTopLevelCategoryJoinColumnName)
  protected Category topLevelCategory;

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "deepThought", cascade = { CascadeType.PERSIST/*, CascadeType.MERGE, CascadeType.REFRESH*/ })
//  @OneToMany(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH })
  protected Set<Category> categories = new HashSet<>();

  @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
  @JoinColumn(name = TableConfig.DeepThoughtTopLevelEntryJoinColumnName)
  protected Entry topLevelEntry;

  @OneToMany(mappedBy = "deepThought", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
  protected List<Entry> entries = new ArrayList<>();

  @Column(name = TableConfig.DeepThoughtNextEntryIndexColumnName)
  protected int nextEntryIndex = 0;

  @OneToMany(mappedBy = "deepThought", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
  protected Set<Tag> tags = new HashSet<>();

  protected transient Collection<Tag> sortedTags = null;


  @OneToMany(mappedBy = "deepThought", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
  @OrderBy("lastName, firstName")
  protected Set<Person> persons = new HashSet<>();

  protected transient SortedSet<Person> personsSorted = null;

  @OneToMany(mappedBy = "deepThought", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
  protected Set<SeriesTitle> seriesTitles = new HashSet<>();

  protected transient SortedSet<SeriesTitle> seriesTitlesSorted = null;

  @OneToMany(mappedBy = "deepThought", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
  protected Set<Reference> references = new HashSet<>();

  protected transient SortedSet<Reference> referencesSorted = null;

  @OneToMany(mappedBy = "deepThought", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
  protected Set<ReferenceSubDivision> referenceSubDivisions = new HashSet<>();

  @OneToMany(mappedBy = "deepThought", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
  protected Set<Note> notes = new HashSet<>();

  @OneToMany(mappedBy = "deepThought", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
  protected Set<FileLink> files = new HashSet<>();


  @OneToMany(fetch = FetchType.LAZY, mappedBy = "deepThought", cascade = CascadeType.PERSIST)
  @OrderBy(value = "sortOrder")
  protected Set<Language> languages = new TreeSet<>(); // these are Languages User can set to specify Language of their Entries, References, ...

  @OneToMany(fetch = FetchType.EAGER, mappedBy = "deepThought", cascade = CascadeType.PERSIST)
  @OrderBy(value = "sortOrder")
  protected Set<NoteType> noteTypes = new TreeSet<>();

  @OneToMany(fetch = FetchType.EAGER, mappedBy = "deepThought", cascade = CascadeType.PERSIST)
  @OrderBy(value = "sortOrder")
  protected Set<FileType> fileTypes = new TreeSet<>();

  @OneToMany(fetch = FetchType.EAGER, mappedBy = "deepThought", cascade = CascadeType.PERSIST)
  @OrderBy(value = "sortOrder")
  protected Set<BackupFileServiceType> backupFileServiceTypes = new TreeSet<>();


//  @JsonIgnore
  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = TableConfig.DeepThoughtDeepThoughtOwnerJoinColumnName)
  protected User deepThoughtOwner;

  protected transient DeepThoughtSettings settings;

  @Column(name = TableConfig.DeepThoughtDeepThoughtSettingsColumnName)
  @Lob
  protected String settingsString = "";



  public DeepThought() {

  }

  public DeepThought(Entry topLevelEntry) {
    this.topLevelEntry = topLevelEntry;
  }


  public Category getTopLevelCategory() {
    return topLevelCategory;
  }

  public int getCountCategories() {
    return categories.size();
  }

  public Set<Category> getCategories() {
    return categories;
  }
  
  public boolean addCategory(Category category) {
    if(categories.add(category)) {
      category.setDeepThought(this);
      if(category.getParentCategory() == null && category.equals(topLevelCategory) == false)
        topLevelCategory.addSubCategory(category);

      callEntityAddedListeners(categories, category);
      return true;
    }

    return false;
  }

  public boolean removeCategory(Category category) {
    if(categories.remove(category)) {
      if(category.getParentCategory() != null)
        category.getParentCategory().removeSubCategory(category);

      for(Entry entry : new ArrayList<>(category.getEntries())) {
        if(entry.getCategories().contains(category)) // TODO: what is this line good for?
          entry.removeCategory(category);
      }
      category.entries.clear();

      for(Category subCategory : new ArrayList<>(category.getSubCategories())) {
        if(subCategory != null && subCategory.getParentCategory().equals(category)) // TODO: what is this line good for?
          removeCategory(subCategory);
      }
      category.subCategories.clear();

      category.setDeepThought(null);

      callEntityRemovedListeners(categories, category);
      return true;
    }

    return false;
  }

  public boolean containsCategory(Category category) {
    return categories.contains(category);
  }


  public Entry getTopLevelEntry() {
    return topLevelEntry;
  }

  public Collection<Entry> getEntries() {
    return entries;
  }

  public boolean addEntry(Entry entry) {
    entry.setEntryIndex(increaseNextEntryIndex());

    if (entries instanceof List)
      ((List) entries).add(0, entry);
    else
      entries.add(entry);
    entry.setDeepThought(this);

    // TODO: why not adding entry to TopLevelCategory? -> because then it's displayed everywhere that Entry belongs to Category 'Ich weiss dass ich nichts weiss'
//      if(entry.getCategories() == null)
//        entry.addCategory(getTopLevelCategory());

    if(entry.getParentEntry() == null && entry.equals(topLevelEntry) == false)
      topLevelEntry.addSubEntry(entry);

    callEntityAddedListeners(entries, entry);
    return true;
  }

  public boolean removeEntry(Entry entry) {
    if(entries.remove(entry)) {
      removeAllRelationsFromEntry(entry);

      callEntityRemovedListeners(entries, entry);
      return true;
    }

    return false;
  }

  protected void removeAllRelationsFromEntry(Entry entry) {
    entry.setDeepThought(null);

    if(entry.getParentEntry() != null) {
      entry.getParentEntry().removeSubEntry(entry);
    }
    for(Entry subEntry : new ArrayList<>(entry.getSubEntries())) {
      entry.removeSubEntry(subEntry);
    }

    for(Category category : new ArrayList<>(entry.getCategories())) {
      entry.removeCategory(category);
    }

    for(Tag tag : new ArrayList<>(entry.getTags()))
      entry.removeTag(tag);

    if(entry.getReferenceSubDivision() != null) {
      entry.setReferenceSubDivision(null);
    }
    if(entry.getReference() != null) {
      entry.setReference(null);
    }
    if(entry.getSeries() != null) {
      entry.setSeries(null);
    }

    for(Person person : new ArrayList<>(entry.getPersons())) {
      entry.removePerson(person);
    }

    for(Note note : new ArrayList<>(entry.getNotes())) {
      entry.removeNote(note);
    }
    for(EntriesGroup linkGroup : new ArrayList<>(entry.getEntryGroups())) {
      entry.removeLinkGroup(linkGroup);
    }

    for(FileLink attachedFile : new ArrayList<>(entry.getAttachedFiles())) {
      entry.removeAttachedFile(attachedFile);
    }
    for(FileLink embeddedFile : new ArrayList<>(entry.getEmbeddedFiles())) {
      entry.removeEmbeddedFile(embeddedFile);
    }
    if(entry.getPreviewImage() != null) {
      entry.setPreviewImage(null);
    }

    if(entry.getLanguage() != null) {
      entry.setLanguage(null);
    }
  }

  public boolean containsEntry(Entry entry) {
    return entries.contains(entry);
  }

  public int getCountEntries() {
    return entries.size();
  }

  public Entry entryAt(int index) {
//    return new ArrayList<Entry>(entries).get(index);
    return entries.get(index);
  }

  public int getNextEntryIndex() {
    return nextEntryIndex;
  }

  protected int increaseNextEntryIndex() {
    Object previousValue = this.nextEntryIndex;
    this.nextEntryIndex++;
    callPropertyChangedListeners(TableConfig.DeepThoughtNextEntryIndexColumnName, previousValue, nextEntryIndex);

    return nextEntryIndex;
  }


  public Collection<Tag> getTags() {
    return tags;
  }

  public Collection<Tag> getSortedTags() {
    if(sortedTags == null) {
      sortedTags = new TreeSet<>(tags);
    }

    return sortedTags;
  }

  public boolean addTag(Tag tag) {
    if(tags.add(tag)) {
      tag.setDeepThought(this);

      if(sortedTags != null)
        sortedTags.add(tag);

      callEntityAddedListeners(tags, tag);
      return true;
    }

    return false;
  }

  public boolean removeTag(Tag tag) {
    if(tags.remove(tag)) {
      tag.setDeepThought(null);

      for(Entry entry : new ArrayList<>(tag.getEntries()))
        entry.removeTag(tag);

      if(sortedTags != null)
        sortedTags.remove(tag);

      callEntityRemovedListeners(tags, tag);
      return true;
    }

    return false;
  }

  public boolean containsTag(Tag tag) {
    return tags.contains(tag);
  }

  public boolean containsTagOfName(String tagName) {
    for(Tag tag : tags) {
      if(tag.getName().equals(tagName))
        return true;
    }

    return false;
  }

  public int getCountTags() {
    return tags.size();
  }

//  public Tag tagAt(int index) {
//    return tags.get(index);
//  }


  public Collection<Language> getLanguages() {
    return languages;
  }

  public boolean addLanguage(Language language) {
    boolean result = languages.add(language);

    if(result) {
      language.setDeepThought(this);
      callEntityAddedListeners(languages, language);
    }

    return result;
  }

  public boolean removeLanguage(Language language) {
    if(language.isDeletable() == false)
      return false;

    boolean result = languages.remove(language);

    if(result) {
      language.setDeepThought(null);
      callEntityRemovedListeners(languages, language);
    }

    return result;
  }


  public Collection<NoteType> getNoteTypes() {
    return noteTypes;
  }

  public boolean addNoteType(NoteType noteType) {
    boolean result = noteTypes.add(noteType);

    if(result) {
      noteType.setDeepThought(this);
      callEntityAddedListeners(noteTypes, noteType);
    }

    return result;
  }

  public boolean removeNoteType(NoteType noteType) {
    if(noteType.isDeletable() == false)
      return false;

    boolean result = noteTypes.remove(noteType);

    if(result) {
      noteType.setDeepThought(null);
      callEntityRemovedListeners(noteTypes, noteType);
    }

    return result;
  }


  public Collection<FileType> getFileTypes() {
    return fileTypes;
  }

  public boolean addFileType(FileType fileType) {
    boolean result = fileTypes.add(fileType);

    if(result) {
      fileType.setDeepThought(this);
      callEntityAddedListeners(fileTypes, fileType);
    }

    return result;
  }

  public boolean removeFileType(FileType fileType) {
    if(fileType.isDeletable() == false)
      return false;

    boolean result = fileTypes.remove(fileType);

    if(result) {
      fileType.setDeepThought(null);
      callEntityRemovedListeners(fileTypes, fileType);
    }

    return result;
  }

  public Collection<BackupFileServiceType> getBackupFileServiceTypes() {
    return backupFileServiceTypes;
  }

  public boolean addBackupFileServiceType(BackupFileServiceType fileServiceType) {
    boolean result = backupFileServiceTypes.add(fileServiceType);

    if(result) {
      fileServiceType.setDeepThought(this);
      callEntityAddedListeners(backupFileServiceTypes, fileServiceType);
    }

    return result;
  }

  public boolean removeBackupFileServiceType(BackupFileServiceType fileServiceType) {
    if(fileServiceType.isDeletable() == false)
      return false;

    boolean result = backupFileServiceTypes.remove(fileServiceType);

    if(result) {
      fileServiceType.setDeepThought(null);
      callEntityRemovedListeners(backupFileServiceTypes, fileServiceType);
    }

    return result;
  }


  public Collection<Person> getPersons() {
    return persons;
  }

  public boolean addPerson(Person person) {
    personsSorted = null;
    if(persons.add(person)) {
      person.setDeepThought(this);

      callEntityAddedListeners(persons, person);
      return true;
    }

    return false;
  }

  public boolean removePerson(Person person) {
    personsSorted = null;
    if(persons.remove(person)) {
      for(Entry entry : new ArrayList<>(person.getAssociatedEntries())) {
        entry.removePerson(person);
      }

      person.setDeepThought(null);

      callEntityRemovedListeners(persons, person);
      return true;
    }

    return false;
  }

  public int getCountPersons() {
    return persons.size();
  }

  public SortedSet<Person> getPersonsSorted() {
    if(personsSorted == null)
      personsSorted = new TreeSet<>(getPersons());
    return personsSorted;
  }


  public Collection<SeriesTitle> getSeriesTitles() {
    return seriesTitles;
  }

  public boolean addSeriesTitle(SeriesTitle seriesTitle) {
    if(seriesTitles.add(seriesTitle)) {
      seriesTitle.setDeepThought(this);
      seriesTitlesSorted = null;

      mayPersistReferenceBaseRelations(seriesTitle);

      callEntityAddedListeners(seriesTitles, seriesTitle);
      return true;
    }

    return false;
  }

  public boolean removeSeriesTitle(SeriesTitle seriesTitle) {
    if(seriesTitles.remove(seriesTitle)) {
      for(Entry entry : new ArrayList<>(seriesTitle.getEntries()))
        entry.setSeries(null);
      for(Reference reference : new ArrayList<>(seriesTitle.getSerialParts()))
        reference.setSeries(null);

      seriesTitle.setDeepThought(null);
      seriesTitlesSorted = null;

      callEntityRemovedListeners(seriesTitles, seriesTitle);
      return true;
    }

    return false;
  }

  public int getCountSeriesTitles() {
    return seriesTitles.size();
  }

  public Collection<SeriesTitle> getSeriesTitlesSorted() {
    if(seriesTitlesSorted == null)
      seriesTitlesSorted = new TreeSet<>(seriesTitles);
    return seriesTitlesSorted;
  }


  public Collection<Reference> getReferences() {
    return references;
  }

  public boolean addReference(Reference reference) {
    if(references.add(reference)) {
      reference.setDeepThought(this);
      referencesSorted = null;

      mayPersistReferenceBaseRelations(reference);

      callEntityAddedListeners(references, reference);
      return true;
    }

    return false;
  }

  public boolean removeReference(Reference reference) {
    if(references.remove(reference)) {
      for(Entry entry : new ArrayList<>(reference.getEntries()))
        entry.setSeries(null);

      if(reference.getSeries() != null)
        reference.setSeries(null);
      for(ReferenceSubDivision subDivision : new ArrayList<>(reference.getSubDivisions()))
        reference.removeSubDivision(subDivision);

      referencesSorted = null;
      reference.setSeries(null);
      reference.setDeepThought(null);

      callEntityRemovedListeners(references, reference);
      return true;
    }

    return false;
  }

  public int getCountReferences() {
    return references.size();
  }

  public Collection<Reference> getReferencesSorted() {
    if(referencesSorted == null)
      referencesSorted = new TreeSet<>(references);
    return referencesSorted;
  }

  public int getCountReferenceSubDivisions() {
    return referenceSubDivisions.size();
  }

  public Set<ReferenceSubDivision> getReferenceSubDivisions() {
    return referenceSubDivisions;
  }

  public boolean addReferenceSubDivision(ReferenceSubDivision subDivision) {
    if(referenceSubDivisions.add(subDivision)) {
      subDivision.setDeepThought(this);

      mayPersistReferenceBaseRelations(subDivision);

      callEntityAddedListeners(referenceSubDivisions, subDivision);
      return true;
    }

    return false;
  }

  public boolean removeReferenceSubDivision(ReferenceSubDivision subDivision) {
    if(referenceSubDivisions.remove(subDivision)) {
      for(Entry entry : new ArrayList<>(subDivision.getEntries()))
        entry.setSeries(null);

      if(subDivision.getReference() != null)
        subDivision.setReference(null);
      if(subDivision.getParentSubDivision() != null)
        subDivision.getParentSubDivision().removeSubDivision(subDivision);
      for(ReferenceSubDivision subSubDivision : new ArrayList<>(subDivision.getSubDivisions()))
        removeReferenceSubDivision(subSubDivision);

      subDivision.setDeepThought(null);

      callEntityRemovedListeners(referenceSubDivisions, subDivision);
      return true;
    }

    return false;
  }


  public Set<Note> getNotes() {
    return notes;
  }

  public boolean addNote(Note note) {
    if(notes.add(note)) {
      note.setDeepThought(this);

      callEntityAddedListeners(notes, note);
      return true;
    }

    return false;
  }

  public boolean removeNote(Note note) {
    if(notes.remove(note)) {
      note.setDeepThought(null);

      callEntityRemovedListeners(notes, note);
      return true;
    }

    return false;
  }


  public int getCountFiles() {
    return files.size();
  }

  public Set<FileLink> getFiles() {
    return files;
  }

  public boolean addFile(FileLink file) {
    if(files.add(file)) {
      file.setDeepThought(this);

      callEntityAddedListeners(files, file);
      return true;
    }

    return false;
  }

  public boolean removeFile(FileLink file) {
    if(files.remove(file)) {
      file.setDeepThought(null);
      for(Entry entry : new ArrayList<>(file.getEntriesAttachedTo()))
        file.removeAsAttachmentFromEntry(entry);
      for(ReferenceBase referenceBase : new ArrayList<>(file.getReferenceBasesAttachedTo()))
        file.removeAsAttachmentFromReferenceBase(referenceBase);

      callEntityRemovedListeners(files, file);
      return true;
    }

    return false;
  }

  public FileLink getFileById(String fileId) {
    for(FileLink file : getFiles()) {
      if(file.getId().equals(fileId))
        return file;
    }

    return null;
  }


  public User getDeepThoughtOwner() {
    return deepThoughtOwner;
  }

  public DeepThoughtSettings getSettings() {
    if(settings == null) {
      settings = SettingsBase.createSettingsFromString(settingsString, DeepThoughtSettings.class);
      settings.addSettingsChangedListener(deepThoughtSettingsChangedListener);
    }
    return settings;
  }

  public String getSettingsString() {
    maySerializeSettingsToString();
    return settingsString;
  }

  protected void setSettingsString(String settingsString) {
    Object previousValue = this.settingsString;
    this.settingsString = settingsString;
    callPropertyChangedListeners(TableConfig.DeepThoughtDeepThoughtSettingsColumnName, previousValue, settingsString);
  }

  protected void maySerializeSettingsToString() {
    if(settingsHaveChanged == true) {
      settingsHaveChanged = false;
      String serializationResult = SettingsBase.serializeSettings(settings);
      if (serializationResult != null)
        settingsString = serializationResult; // do not call setSettingsString() as we're right in a Entity Lifecycle method.
                                              // Calling setSettingsString() would again trigger saving to DB -> infinite loop
    }
  }


  // TODO: find a better place for persisting ReferenceBase Relations methods

  protected void mayPersistReferenceBaseRelations(ReferenceBase referenceBase) {
    mayPersistFiles(referenceBase);
    mayPersistPersons(referenceBase);
    mayPersistPreviewImage(referenceBase);
  }

  protected void mayPersistPersons(ReferenceBase referenceBase) {
    List<Person> unpersistedPersons = new ArrayList<>();
    List<Person> referenceBasePersons = new ArrayList<>(referenceBase.getPersons()); // make a copy as Collection may gets changed

    for(Person person : referenceBasePersons) {
      if(person.isPersisted() == false) {
        unpersistedPersons.add(person);
        referenceBase.removePerson(person);
      }
    }

    for(Person unpersistedPerson : unpersistedPersons) {
      addPerson(unpersistedPerson);
      referenceBase.addPerson(unpersistedPerson); // TODO: how to keep correct order of Persons?
    }
  }

  protected void mayPersistFiles(ReferenceBase referenceBase) {
    mayPersistAttachedFiles(referenceBase);
    mayPersistEmbeddedFiles(referenceBase);
  }

  protected void mayPersistAttachedFiles(ReferenceBase referenceBase) {
    List<FileLink> unpersistedFiles = new ArrayList<>();
    List<FileLink> referenceBaseAttachedFiles = new ArrayList<>(referenceBase.getAttachedFiles()); // make a copy as Collection may gets changed

    for(FileLink file : referenceBaseAttachedFiles) {
      if(file.isPersisted() == false) {
        unpersistedFiles.add(file);
        referenceBase.removeAttachedFile(file);
      }
    }

    for(FileLink unpersistedFile : unpersistedFiles) {
      addFile(unpersistedFile);
      referenceBase.addAttachedFile(unpersistedFile); // TODO: how to keep correct order of Files?
    }
  }

  protected void mayPersistEmbeddedFiles(ReferenceBase referenceBase) {
    List<FileLink> unpersistedFiles = new ArrayList<>();
    List<FileLink> referenceBaseEmbeddedFiles = new ArrayList<>(referenceBase.getEmbeddedFiles()); // make a copy as Collection may gets changed

    for(FileLink file : referenceBaseEmbeddedFiles) {
      if(file.isPersisted() == false) {
        unpersistedFiles.add(file);
        referenceBase.removeEmbeddedFile(file);
      }
    }

    for(FileLink unpersistedFile : unpersistedFiles) {
      addFile(unpersistedFile);
      referenceBase.addEmbeddedFile(unpersistedFile); // TODO: how to keep correct order of Files?
    }
  }

  protected void mayPersistPreviewImage(ReferenceBase referenceBase) {
    FileLink previewImage = referenceBase.getPreviewImage();
    if(previewImage != null && previewImage.isPersisted() == false) {
      addFile(previewImage);
    }
  }


  public List<Entry> findEntriesByTags(Collection<Tag> tags) {
    List<Entry> foundEntries = new ArrayList<>();

    for(Entry entry : entries) {
      if(entry.hasTags(tags))
        foundEntries.add(entry);
    }
    return foundEntries;
  }


  protected transient boolean settingsHaveChanged = false;

  protected transient SettingsChangedListener deepThoughtSettingsChangedListener = new SettingsChangedListener() {
    @Override
    public void settingsChanged(Setting setting, Object previousValue, Object newValue) {
      // do not serialize at all, it takes to much time to serialize, simply set flag an serialize in PreUpdate life cycle event handler
//      String serializationResult = SettingsBase.serializeSettings(settings);
//
//      if(serializationResult != null)
//        // setSettingsString() would call PropertyChangedListeners, DeepThought would therefor get updated in Db, i think this is overkill
//        // setSettingsString(serializationResult);
//        // -> set Settings String directly so when DeepThought gets the next time written to Db, also Settings get written to Db
//        settingsString = serializationResult;

      settingsHaveChanged = true;
    }
  };

  @Override
  protected void preUpdate() {
    super.preUpdate();

    maySerializeSettingsToString();
  }

  protected transient EntityListener subEntitiesListener = new EntityListener() {
    @Override
    public void propertyChanged(BaseEntity entity, String propertyName, Object previousValue, final Object newValue) {
      log.debug("SubEntity's property {} changed to {}; {}", propertyName, newValue, entity);
      if(entity instanceof Tag) {// a Tag has been updated -> reapply Tag sorting
        sortedTags = null;
      }

      entityUpdated(entity);
    }

    @Override
    public void entityAddedToCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity addedEntity) {
//      addedEntity.addEntityListener(subEntitiesListener);
      callEntityAddedListeners(collectionHolder, collection, addedEntity);
      entityUpdated(collectionHolder);
    }

    @Override
    public void entityOfCollectionUpdated(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity updatedEntity) {
      callEntityOfCollectionUpdatedListeners(collectionHolder, collection, updatedEntity);
    }

    @Override
    public void entityRemovedFromCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity removedEntity) {
      callEntityRemovedListeners(collectionHolder, collection, removedEntity);
      entityUpdated(collectionHolder);
    }
  };

  protected void entityUpdated(final BaseEntity entity) {
    if(entity instanceof Category)
      callEntityOfCollectionUpdatedListeners(getCategories(), entity);
    else if(entity instanceof Entry)
      callEntityOfCollectionUpdatedListeners(getEntries(), entity);
    else if(entity instanceof Tag)
      callEntityOfCollectionUpdatedListeners(getTags(), entity);
    else if(entity instanceof Person)
      callEntityOfCollectionUpdatedListeners(getPersons(), entity);
    else if(entity instanceof SeriesTitle)
      callEntityOfCollectionUpdatedListeners(getSeriesTitles(), entity);
    else if(entity instanceof Reference)
      callEntityOfCollectionUpdatedListeners(getReferences(), entity);
    else if(entity instanceof ReferenceSubDivision)
      callEntityOfCollectionUpdatedListeners(getReferenceSubDivisions(), entity);
    else if(entity instanceof FileLink)
      callEntityOfCollectionUpdatedListeners(getFiles(), entity);
    else if(entity instanceof Note)
      callEntityOfCollectionUpdatedListeners(getNotes(), entity);
    else if(entity instanceof NoteType)
      callEntityOfCollectionUpdatedListeners(getNoteTypes(), entity);
    else if(entity instanceof Language)
      callEntityOfCollectionUpdatedListeners(getLanguages(), entity);
    else if(entity instanceof BackupFileServiceType)
      callEntityOfCollectionUpdatedListeners(getBackupFileServiceTypes(), entity);
    else
      log.warn("Updated entity of type " + entity.getClass() + " retrieved, but don't know what to do with this type");
  }

//  /**
//   * <p>
//   * (Not such a good solution)
//   * This method such be called by Database persisted whenever a Entity instance got removed from Cache
//   * so that the Entity may still exist in Database but this instance isn't used anymore.
//   * Its Listeners can then be removed to avoid stale listeners.
//   * </p>
//   * @param entity The just deleted Entity
//   */
//  public void entityInstanceRemoved(BaseEntity entity) {
//    if(entity instanceof Entry)
//      removeEntryListener((Entry) entity);
//    else if(entity instanceof Tag)
//      removeTagListener((Tag) entity);
//    else
//      log.warn("Entity instance of type " + entity.getClass() + " got removed, but don't know what to do with this type");
//  }


  /*        Listeners handling        */

//  @Override
//  protected void callEntityAddedListeners(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity addedEntity) {
//    addedEntity.addEntityListener(subEntitiesListener); // add a listener to every Entity so that it's changes can be tracked
//
//    super.callEntityAddedListeners(collectionHolder, collection, addedEntity);
//  }
//
//  @Override
//  protected void callEntityRemovedListeners(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity removedEntity) {
//    // don't remove listener if removedEntity is still on a DeepThought collection
//    if(collectionHolder == this)
//      removedEntity.removeEntityListener(subEntitiesListener);
//
//    super.callEntityRemovedListeners(collectionHolder, collection, removedEntity);
//  }


  @Override
  @Transient
  public String getTextRepresentation() {
    return "DeepThought";
  }

  @Override
  public String toString() {
    return "DeepThought with " + getCountEntries() + " Entries, " + getCountCategories() + " Categories and " + getCountTags() + " Tags";
  }


  public static DeepThought createEmptyDeepThought() {
    DeepThought emptyDeepThought = new DeepThought();

    Category topLevelCategory = Category.createTopLevelCategory();
    emptyDeepThought.topLevelCategory = topLevelCategory;
    emptyDeepThought.getSettings().setLastViewedCategory(topLevelCategory);

    Entry topLevelEntry = Entry.createTopLevelEntry();
    emptyDeepThought.topLevelEntry = topLevelEntry;

    createEnumerationsDefaultValues(emptyDeepThought);

    return emptyDeepThought;
  }

  protected static void createEnumerationsDefaultValues(DeepThought deepThought) {
    createLanguageDefaultValues(deepThought);
    createNoteTypeDefaultValues(deepThought);
    createFileTypeDefaultValues(deepThought);
    createBackupFileServiceTypeDefaultValues(deepThought);
  }

  protected static void createLanguageDefaultValues(DeepThought deepThought) {
    deepThought.addLanguage(new Language("en", "English", "language.english", true, true, 1));
    deepThought.addLanguage(new Language("de", "Deutsch", "language.german", true, true, 2));
    deepThought.addLanguage(new Language("es", "Español", "language.spanish", true, true, 3));
    deepThought.addLanguage(new Language("fr", "Français", "language.french", true, true, 4));
    deepThought.addLanguage(new Language("it", "Italiano", "language.italian", true, true, 5));
    deepThought.addLanguage(new Language("ar", "العربية", "language.arabic", true, true, 6));
    deepThought.addLanguage(new Language("bg", "Български", "language.bulgarian", true, true, 7));
    deepThought.addLanguage(new Language("cs", "Čeština", "language.czech", true, true, 8));
    deepThought.addLanguage(new Language("da", "Dansk", "language.danish", true, true, 9));
    deepThought.addLanguage(new Language("el", "Ελληνικά", "language.greek", true, true, 10));
    deepThought.addLanguage(new Language("fa", "فارسی", "language.persian", true, true, 11));
    deepThought.addLanguage(new Language("fi", "Suomi", "language.finnish", true, true, 12));
    deepThought.addLanguage(new Language("hi", "हिन्दी", "language.hindi", true, true, 13));
    deepThought.addLanguage(new Language("hu", "Magyar", "language.hungarian", true, true, 14));
    deepThought.addLanguage(new Language("id", "Bahasa Indonesia", "language.indonesian", true, true, 15));
    deepThought.addLanguage(new Language("ja", "日本語", "language.japanese", true, true, 16));
    deepThought.addLanguage(new Language("ko", "한국어", "language.korean", true, true, 17));
    deepThought.addLanguage(new Language("nl", "Nederlands", "language.dutch", true, true, 18));
    deepThought.addLanguage(new Language("no", "Norsk bokmål", "language.norwegian", true, true, 19));
    deepThought.addLanguage(new Language("pt", "Português", "language.portuguese", true, true, 20));
    deepThought.addLanguage(new Language("ro", "Română", "language.romanian", true, true, 21));
    deepThought.addLanguage(new Language("ru", "Русский", "language.russian", true, true, 22));
    deepThought.addLanguage(new Language("sv", "Svenska", "language.swedish", true, true, 23));
    deepThought.addLanguage(new Language("th", "ไทย", "language.thai", true, true, 24));
    deepThought.addLanguage(new Language("tr", "Türkçe", "language.turkish", true, true, 25));
    deepThought.addLanguage(new Language("zh-cn", "中文", "language.chinese.simplified", true, true, 26));
    deepThought.addLanguage(new Language("zh-tw", "文言", "language.chinese.traditional", true, true, 27));

    deepThought.addLanguage(new Language("ar", "Afrikaans", "language.afrikaans", true, true, 28));
    deepThought.addLanguage(new Language("bn", "বাংলা", "language.bengali", true, true, 29));
    deepThought.addLanguage(new Language("gu", "ગુજરાતી", "language.gujarati", true, true, 30));
    deepThought.addLanguage(new Language("he", "עברית", "language.hebrew", true, true, 31));
    deepThought.addLanguage(new Language("hr", "Hrvatski", "language.croatian", true, true, 32));
    deepThought.addLanguage(new Language("kn", "ಕನ್ನಡ", "language.kannada", true, true, 33));
    deepThought.addLanguage(new Language("mk", "Македонски", "language.macedonian", true, true, 34));
    deepThought.addLanguage(new Language("ml", "മലയാളം", "language.malayalam", true, true, 35));
    deepThought.addLanguage(new Language("mr", "मराठी", "language.marathi", true, true, 36));
    deepThought.addLanguage(new Language("ne", "नेपाली", "language.nepali", true, true, 37));
    deepThought.addLanguage(new Language("pa", "ਪੰਜਾਬੀ", "language.punjabi", true, true, 38));
    deepThought.addLanguage(new Language("pl", "Polski", "language.polish", true, true, 39));
    deepThought.addLanguage(new Language("sk", "Slovenčina", "language.slovak", true, true, 40));
    deepThought.addLanguage(new Language("so", "Soomaaliga", "language.somali", true, true, 41));
    deepThought.addLanguage(new Language("sq", "Shqip", "language.albanian", true, true, 42));
    deepThought.addLanguage(new Language("sw", "Kiswahili", "language.swahili", true, true, 43));
    deepThought.addLanguage(new Language("ta", "தமிழ்", "language.tamil", true, true, 44));
    deepThought.addLanguage(new Language("te", "తెలుగు", "language.telugu", true, true, 45));
    deepThought.addLanguage(new Language("tl", "Tagalog", "language.tagalog", true, true, 46));
    deepThought.addLanguage(new Language("uk", "Українська", "language.ukrainian", true, true, 47));
    deepThought.addLanguage(new Language("ur", "اردو", "language.urdu", true, true, 48));
    deepThought.addLanguage(new Language("vi", "Tiếng Việt", "language.vietnamese", true, true, 49));
  }

  protected static void createNoteTypeDefaultValues(DeepThought deepThought) {
    deepThought.addNoteType(new NoteType("note.type.unset", true, false, 1));
    deepThought.addNoteType(new NoteType("note.type.comment", true, false, 2));
    deepThought.addNoteType(new NoteType("note.type.info", true, false, 3));
    deepThought.addNoteType(new NoteType("note.type.to.do", true, false, 4));
    deepThought.addNoteType(new NoteType("note.type.thought", true, false, 5));
  }

  protected static void createFileTypeDefaultValues(DeepThought deepThought) {
    deepThought.addFileType(new FileType("file.type.other.files", FileUtils.OtherFilesFolderName, true, false, Integer.MAX_VALUE));
    deepThought.addFileType(new FileType("file.type.document", FileUtils.DocumentsFilesFolderName, true, true, 1));
    deepThought.addFileType(new FileType("file.type.image", FileUtils.ImagesFilesFolderName, true, false, 2));
    deepThought.addFileType(new FileType("file.type.audio", FileUtils.AudioFilesFolderName, true, true, 3));
    deepThought.addFileType(new FileType("file.type.video", FileUtils.VideoFilesFolderName, true, true, 4));
  }

  protected static void createBackupFileServiceTypeDefaultValues(DeepThought deepThought) {
    deepThought.addBackupFileServiceType(new BackupFileServiceType("backup.file.service.type.all", true, false, 1));
    deepThought.addBackupFileServiceType(new BackupFileServiceType("backup.file.service.type.database", true, false, 2));
    deepThought.addBackupFileServiceType(new BackupFileServiceType("backup.file.service.type.json", true, false, 3));
  }

}
