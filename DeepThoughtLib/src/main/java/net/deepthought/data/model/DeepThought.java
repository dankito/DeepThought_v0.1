package net.deepthought.data.model;

import net.deepthought.data.model.enums.BackupFileServiceType;
import net.deepthought.data.model.enums.Language;
import net.deepthought.data.model.enums.NoteType;
import net.deepthought.data.model.listener.EntityListener;
import net.deepthought.data.model.listener.SettingsChangedListener;
import net.deepthought.data.model.settings.DeepThoughtSettings;
import net.deepthought.data.model.settings.SettingsBase;
import net.deepthought.data.model.settings.enums.Setting;
import net.deepthought.data.persistence.db.BaseEntity;
import net.deepthought.data.persistence.db.TableConfig;
import net.deepthought.data.persistence.db.UserDataEntity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
//  @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
//  protected Set<Entry> entries = new HashSet<>();
  @OrderBy("entryIndex DESC")
  protected List<Entry> entries = new ArrayList<>();

  @Column(name = TableConfig.DeepThoughtNextEntryIndexColumnName)
  protected int nextEntryIndex = 1;

  @OneToMany(mappedBy = "deepThought", fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
  protected Set<Tag> tags = new HashSet<>();

  protected transient List<Tag> sortedTags = new ArrayList<>();

  @OneToMany(mappedBy = "deepThought", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
  protected Set<IndexTerm> indexTerms = new HashSet<>();


  @OneToMany(fetch = FetchType.LAZY, mappedBy = "deepThought", cascade = CascadeType.PERSIST)
  @OrderBy(value = "sortOrder")
  protected Set<Language> languages = new HashSet<>(); // these are Languages User can set to specify Language of their Entries, References, ...

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "deepThought", cascade = CascadeType.PERSIST)
  @OrderBy(value = "sortOrder")
  protected Set<NoteType> noteTypes = new HashSet<>();

  @OneToMany(fetch = FetchType.EAGER, mappedBy = "deepThought", cascade = CascadeType.PERSIST)
  @OrderBy(value = "sortOrder")
  protected Set<BackupFileServiceType> backupFileServiceTypes = new HashSet<>();


  @OneToMany(mappedBy = "deepThought", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
  protected Set<Person> persons = new HashSet<>();

  protected transient SortedSet<Person> personsSorted = null;

  @OneToMany(mappedBy = "deepThought", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
  protected Set<SeriesTitle> seriesTitles = new HashSet<>();

  protected transient SortedSet<SeriesTitle> seriesTitlesSorted = null;

  @OneToMany(mappedBy = "deepThought", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
  protected Set<Reference> references = new HashSet<>();

  protected transient SortedSet<Reference> referencesSorted = null;

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


  public Category getTopLevelCategory() {
    return topLevelCategory;
  }

  public Set<Category> getCategories() {
    return categories;
  }
  
  public boolean addCategory(Category category) {
    if(categories.add(category)) {
      category.deepThought = this;
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
          category.removeEntry(entry);
      }
      category.entries.clear();

      for(Category subCategory : new ArrayList<>(category.getSubCategories())) {
        if(subCategory.getParentCategory().equals(category)) // TODO: what is this line good for?
          removeCategory(subCategory);
      }
      category.subCategories.clear();

      category.deepThought = null;

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
    entry.setEntryIndex(nextEntryIndex++);

    if (entries instanceof List)
      ((List) entries).add(0, entry);
    else
      entries.add(entry);
    entry.deepThought = this;

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
      entry.deepThought = null;
      if(entry.getParentEntry() != null)
        entry.getParentEntry().removeSubEntry(entry);

      for(Category category : new ArrayList<>(entry.getCategories()))
        category.removeEntry(entry);

      for(Tag tag : new ArrayList<>(entry.getTags()))
        entry.removeTag(tag);

      for(IndexTerm indexTerm : new ArrayList<>(entry.getIndexTerms()))
        entry.removeIndexTerm(indexTerm);

      callEntityRemovedListeners(entries, entry);
      return true;
    }

    return false;
  }

  public boolean containsEntry(Entry entry) {
    return entries.contains(entry);
  }

  public int countEntries() {
    return entries.size();
  }

  public Entry entryAt(int index) {
//    return new ArrayList<Entry>(entries).get(index);
    return entries.get(index);
  }

  public int getNextEntryIndex() {
    return nextEntryIndex;
  }


  public Collection<Tag> getTags() {
    return tags;
  }

  public Collection<Tag> getSortedTags() {
    if(sortedTags == null)
      sortedTags = new ArrayList<>(tags);
    return sortedTags;
  }

  public boolean addTag(Tag tag) {
    if(tags.add(tag)) {
      tag.deepThought = this;

      getSortedTags().add(tag);
      Collections.sort(sortedTags);

      callEntityAddedListeners(tags, tag);
      return true;
    }

    return false;
  }

  public boolean removeTag(Tag tag) {
    if(tags.remove(tag)) {
      tag.deepThought = null;

      for(Entry entry : new ArrayList<>(tag.getEntries()))
        entry.removeTag(tag);

      getSortedTags().remove(tag);
//      Collections.sort(sortedTags); // why sorting, they should already be in correct order

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

  public int countTags() {
    return tags.size();
  }

//  public Tag tagAt(int index) {
//    return tags.get(index);
//  }


  public Collection<IndexTerm> getIndexTerms() {
    return indexTerms;
  }

  public boolean addIndexTerm(IndexTerm indexTerm) {
    if(indexTerms.add(indexTerm)) {
      indexTerm.deepThought = this;

      callEntityAddedListeners(indexTerms, indexTerm);
      return true;
    }

    return false;
  }

  public boolean removeIndexTerm(IndexTerm indexTerm) {
    if(indexTerms.remove(indexTerm)) {
      indexTerm.deepThought = null;

      for(Entry entry : new ArrayList<>(indexTerm.getEntries()))
        entry.removeIndexTerm(indexTerm);
      indexTerm.entries.clear();

      callEntityRemovedListeners(indexTerms, indexTerm);
      return true;
    }

    return false;
  }

  public boolean containsIndexTerm(IndexTerm indexTerm) {
    return indexTerms.contains(indexTerm);
  }

  public boolean containsIndexTermOfName(String keywordName) {
    for(Tag tag : tags) {
      if(tag.getName().equals(keywordName))
        return true;
    }

    return false;
  }

  public int countIndexTerms() {
    return indexTerms.size();
  }

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
      person.deepThought = this;

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

      person.deepThought = null;

      callEntityRemovedListeners(persons, person);
      return true;
    }

    return false;
  }

  public int countPersons() {
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
      seriesTitle.deepThought = this;
      seriesTitlesSorted = null;

      callEntityAddedListeners(seriesTitles, seriesTitle);
      return true;
    }

    return false;
  }

  public boolean removeSeriesTitle(SeriesTitle seriesTitle) {
    if(seriesTitles.remove(seriesTitle)) {
      for(Reference reference : new ArrayList<>(seriesTitle.getSerialParts()))
        reference.setSeries(null);

      seriesTitle.deepThought = null;
      seriesTitlesSorted = null;

      callEntityRemovedListeners(seriesTitles, seriesTitle);
      return true;
    }

    return false;
  }

  public int countSeriesTitles() {
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
      reference.deepThought = this;
      referencesSorted = null;

      callEntityAddedListeners(references, reference);
      return true;
    }

    return false;
  }

  public boolean removeReference(Reference reference) {
    if(references.remove(reference)) {
      referencesSorted = null;
      reference.setSeries(null);
      reference.deepThought = null;

      callEntityRemovedListeners(references, reference);
      return true;
    }

    return false;
  }

  public int countReferences() {
    return references.size();
  }

  public Collection<Reference> getReferencesSorted() {
    if(referencesSorted == null)
      referencesSorted = new TreeSet<>(references);
    return referencesSorted;
  }


  public DeepThoughtSettings getSettings() {
    if(settings == null) {
      settings = SettingsBase.createSettingsFromString(settingsString, DeepThoughtSettings.class);
      settings.addSettingsChangedListener(deepThoughtSettingsChangedListener);
    }
    return settings;
  }

  protected void setSettingsString(String settingsString) {
    Object previousValue = this.settingsString;
    this.settingsString = settingsString;
    callPropertyChangedListeners(TableConfig.DeepThoughtDeepThoughtSettingsColumnName, previousValue, settingsString);
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

    if(settingsHaveChanged == true) {
      settingsHaveChanged = false;
      String serializationResult = SettingsBase.serializeSettings(settings);
      if (serializationResult != null)
        settingsString = serializationResult;
    }
  }

  /**
   * <p>
   * (Not such a good solution)
   * This method such be called by Database persisted whenever a lazy loading Entity got loaded / mapped
   * so that Entity Listeners can be added to this Entity in order to keep its state modifiedOn in UI.
   * </p>
   * @param entity The just mapped Entity
   */
  public void lazyLoadedEntityMapped(BaseEntity entity) {
    log.debug("Lazy loaded Entity mapped: {}", entity);
    entity.addEntityListener(subEntitiesListener);
  }

  protected transient EntityListener subEntitiesListener = new EntityListener() {
    @Override
    public void propertyChanged(BaseEntity entity, String propertyName, Object previousValue, final Object newValue) {
      log.debug("SubEntity's property {} changed to {}; {}", propertyName, newValue, entity);
      if(entity instanceof Tag) {// a Tag has been updated -> reapply Tag sorting
        sortedTags = null;
      }

      // TODO: bad solution
      if(isEntityNotOwnedByDeepThought(newValue) && ((BaseEntity)newValue).isPersisted() == false)
        callEntityAddedListeners(entity, new ArrayList<BaseEntity>() {{ add((BaseEntity)newValue); }}, (BaseEntity)newValue);

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

  protected boolean isEntityNotOwnedByDeepThought(Object entity) {
    return entity instanceof FileLink || entity instanceof Note || entity instanceof ReferenceSubDivision;
  }

  protected boolean isCollectionOnDeepThought(BaseEntity collectionEntity) {
    return collectionEntity instanceof Category || collectionEntity instanceof Entry || collectionEntity instanceof Tag || collectionEntity instanceof IndexTerm ||
           collectionEntity instanceof Person || collectionEntity instanceof SeriesTitle || collectionEntity instanceof Reference;
  }

  protected void entityUpdated(final BaseEntity entity) {
    if(entity instanceof Category)
      callEntityOfCollectionUpdatedListeners(getCategories(), entity);
    else if(entity instanceof Entry)
      callEntityOfCollectionUpdatedListeners(getEntries(), entity);
    else if(entity instanceof Tag)
      callEntityOfCollectionUpdatedListeners(getTags(), entity);
    else if(entity instanceof IndexTerm)
      callEntityOfCollectionUpdatedListeners(getIndexTerms(), entity);
    else if(entity instanceof Person)
      callEntityOfCollectionUpdatedListeners(getPersons(), entity);
    else if(entity instanceof SeriesTitle)
      callEntityOfCollectionUpdatedListeners(getSeriesTitles(), entity);
    else if(entity instanceof Reference)
      callEntityOfCollectionUpdatedListeners(getReferences(), entity);
    else if(entity instanceof NoteType)
      callEntityOfCollectionUpdatedListeners(getNoteTypes(), entity);
    else if(entity instanceof Language)
      callEntityOfCollectionUpdatedListeners(getLanguages(), entity);
    else if(entity instanceof BackupFileServiceType)
      callEntityOfCollectionUpdatedListeners(getBackupFileServiceTypes(), entity);
    else if(isEntityNotOwnedByDeepThought(entity))
      callEntityOfCollectionUpdatedListeners(new ArrayList<BaseEntity>() {{ add(entity); }}, entity);
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

  @Override
  protected void callEntityAddedListeners(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity addedEntity) {
    addedEntity.addEntityListener(subEntitiesListener); // add a listener to every Entity so that it's changes can be tracked

    super.callEntityAddedListeners(collectionHolder, collection, addedEntity);
  }

  @Override
  protected void callEntityRemovedListeners(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity removedEntity) {
    // don't remove listener if removedEntity is still on a DeepThought collection
    if(collectionHolder == this || isComposition(collectionHolder, removedEntity) == true)
      removedEntity.removeEntityListener(subEntitiesListener);

    super.callEntityRemovedListeners(collectionHolder, collection, removedEntity);
  }

  public boolean isComposition(BaseEntity collectionHolder, BaseEntity entity) {
    if(collectionHolder instanceof Entry) {
      if(entity instanceof FileLink || entity instanceof Note)
        return true;
    }
    else if(collectionHolder instanceof Reference) {
      if(entity instanceof ReferenceSubDivision)
        return true;
    }
    else if(collectionHolder instanceof ReferenceSubDivision) {
      if(entity instanceof ReferenceSubDivision)
        return true;
    }

    if(collectionHolder instanceof ReferenceBase) {
      if(entity instanceof FileLink)
        return true;
    }

    return false;
  }


  @Override
  @Transient
  public String getTextRepresentation() {
    return "DeepThought";
  }

  @Override
  public String toString() {
    return "DeepThought with " + countEntries() + " Entries, " + categories.size() + " Categories and " + countTags() + " Tags";
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
    createBackupFileServiceTypeDefaultValues(deepThought);
  }

  protected static void createLanguageDefaultValues(DeepThought deepThought) {
    deepThought.addLanguage(new Language("language.english", true, true, 1));
    deepThought.addLanguage(new Language("language.german", true, true, 2));
  }

  protected static void createNoteTypeDefaultValues(DeepThought deepThought) {
    deepThought.addNoteType(new NoteType("note.type.comment", true, false, 1));
    deepThought.addNoteType(new NoteType("note.type.info", true, false, 2));
    deepThought.addNoteType(new NoteType("note.type.to.do", true, false, 3));
    deepThought.addNoteType(new NoteType("note.type.thought", true, false, 4));
//    deepThought.addNoteType(new NoteType("note.type.unset", true, false, 5));
  }

  protected static void createBackupFileServiceTypeDefaultValues(DeepThought deepThought) {
    deepThought.addBackupFileServiceType(new BackupFileServiceType("backup.file.service.type.all", true, false, 1));
    deepThought.addBackupFileServiceType(new BackupFileServiceType("backup.file.service.type.database", true, false, 2));
    deepThought.addBackupFileServiceType(new BackupFileServiceType("backup.file.service.type.json", true, false, 3));
  }

}
