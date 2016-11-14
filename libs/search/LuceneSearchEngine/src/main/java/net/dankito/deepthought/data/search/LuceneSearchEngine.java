package net.dankito.deepthought.data.search;

import net.dankito.deepthought.Application;
import net.dankito.deepthought.data.listener.AllEntitiesListener;
import net.dankito.deepthought.data.model.Category;
import net.dankito.deepthought.data.model.DeepThought;
import net.dankito.deepthought.data.model.Entry;
import net.dankito.deepthought.data.model.EntryPersonAssociation;
import net.dankito.deepthought.data.model.FileLink;
import net.dankito.deepthought.data.model.Note;
import net.dankito.deepthought.data.model.Person;
import net.dankito.deepthought.data.model.Reference;
import net.dankito.deepthought.data.model.ReferenceBase;
import net.dankito.deepthought.data.model.ReferenceSubDivision;
import net.dankito.deepthought.data.model.SeriesTitle;
import net.dankito.deepthought.data.model.Tag;
import net.dankito.deepthought.data.model.ui.AllEntriesSystemTag;
import net.dankito.deepthought.data.model.ui.EntriesWithoutTagsSystemTag;
import net.dankito.deepthought.data.persistence.LazyLoadingList;
import net.dankito.deepthought.data.persistence.db.BaseEntity;
import net.dankito.deepthought.data.persistence.db.UserDataEntity;
import net.dankito.deepthought.data.search.results.LazyLoadingLuceneSearchResultsList;
import net.dankito.deepthought.data.search.specific.CategoriesSearch;
import net.dankito.deepthought.data.search.specific.EntriesSearch;
import net.dankito.deepthought.data.search.specific.FilesSearch;
import net.dankito.deepthought.data.search.specific.FindAllEntriesHavingTheseTagsResult;
import net.dankito.deepthought.data.search.specific.ReferenceBasesSearch;
import net.dankito.deepthought.data.search.specific.TagsSearch;
import net.dankito.deepthought.data.search.specific.TagsSearchResult;
import net.dankito.deepthought.util.Notification;
import net.dankito.deepthought.util.NotificationType;
import net.dankito.deepthought.util.StringUtils;
import net.dankito.deepthought.util.file.FileUtils;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FieldCacheRangeFilter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.NumericUtils;
import org.apache.lucene.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;

public class LuceneSearchEngine extends SearchEngineBase {

  public static final String BooleanFieldFalseValue = "false";
  public static final String BooleanFieldTrueValue = "true";

  public static final String NoTagsFieldValue = "notags";
  public static final String NoCategoriesFieldValue = "nocategories";
  public static final String NoPersonsFieldValue = "nopersons";
  public static final String NoNotesFieldValue = "nonotes";
  public static final String NoSeriesFieldValue = "noseries";
  public static final String NoReferenceFieldValue = "noreference";
  public static final String NoReferenceSubDivisionFieldValue = "noreferencesubdivision";

  public static final String NoParentCategoryNameFieldValue = "noparentcategory";
  public static final String NoParentCategoryIdFieldValue = "-42";

  public static final int SeriesTitleReferenceBaseType = 1;
  public static final int ReferenceReferenceBaseType = 2;
  public static final int ReferenceSubDivisionReferenceBaseType = 3;

  public static BytesRef SeriesTitleReferenceBaseTypeIntRef;
  public static BytesRef ReferenceReferenceBaseTypeIntRef;
  public static BytesRef ReferenceSubDivisionReferenceBaseTypeIntRef;


  private static final Logger log = LoggerFactory.getLogger(LuceneSearchEngine.class);


  protected static final List<Class> ClassesWithOwnIndexDirectories = Arrays.asList(new Class[] { Entry.class, Tag.class, ReferenceBase.class, Person.class,
                                                                                                  Category.class, Note.class, FileLink.class });

  protected static final Class DefaultIndexDirectoryClass = UserDataEntity.class;

  protected static final int WAIT_TIME_BEFORE_COMMITTING_INDICES_MILLIS = 1500;


  protected Map<Class, Directory> directories = new HashMap<>();

  protected Analyzer defaultAnalyzer;

  protected Map<Class, IndexWriter> indexWriters = new HashMap<>();

  protected Map<Class, DirectoryReader> directoryReaders = new HashMap<>();
  protected Map<Class, IndexSearcher> indexSearchers = new HashMap<>();

  protected boolean isIndexReady = false;

  protected boolean isReadOnly = false;

  protected boolean isInitialized = false;

  protected int indexUpdatedEntitiesAfterMilliseconds = 1000;
  protected Queue<UserDataEntity> updatedEntitiesToIndex = new ConcurrentLinkedQueue<>();
  protected Timer indexUpdatedEntitiesTimer = null;

  protected List<IndexWriter> indicesWithUnsavedChanges = new CopyOnWriteArrayList<>();
  protected Timer commitIndicesTimer = null;


  public LuceneSearchEngine() {
    SeriesTitleReferenceBaseTypeIntRef = getByteRefFromInteger(SeriesTitleReferenceBaseType);
    ReferenceReferenceBaseTypeIntRef = getByteRefFromInteger(ReferenceReferenceBaseType);
    ReferenceSubDivisionReferenceBaseTypeIntRef = getByteRefFromInteger(ReferenceSubDivisionReferenceBaseType);
    
    if(deepThought != null) {
      deepThoughtChanged(null, deepThought);
    }
  }

  public LuceneSearchEngine(Directory directory) {
    this();
    setDirectory(directory);
  }

  @Override
  protected void deepThoughtChanged(DeepThought previousDeepThought, DeepThought newDeepThought) {
    super.deepThoughtChanged(previousDeepThought, newDeepThought);

    if(previousDeepThought != null) {
      closeIndexSearchersAndWriters();
    }

    createDirectoryAndIndexSearcherAndWriterForDeepThought(newDeepThought);
  }

  @Override
  protected void applicationInstantiated() {
    super.applicationInstantiated();

    Application.getEntityChangesService().addAllEntitiesListener(allEntitiesListener);
  }

  @Override
  protected void initialDatabaseSynchronizationDone() {
    super.initialDatabaseSynchronizationDone();

    File previousIndexPath = getPreviousIndexPath();

    closeIndexSearchersAndWriters();

    if(previousIndexPath != null) {
      FileUtils.deleteFile(previousIndexPath); // delete previous index directory to not waste any disk space with a not used anymore index
    }

    createDirectoryAndIndexSearcherAndWriterForDeepThought(deepThought);

    rebuildIndex(); // as a lot of Ids have changed we better go rebuilding whole index even though it may takes a lot of time
  }

  protected File getPreviousIndexPath() {
    Directory previousIndexDir = directories.get(DefaultIndexDirectoryClass);
    if(previousIndexDir instanceof FSDirectory) {
      File previousIndexPathSubDir = ((FSDirectory)previousIndexDir).getDirectory();
      return previousIndexPathSubDir.getParentFile();
    }

    return null;
  }

  public void close() {
    closeIndexSearchersAndWriters();

    super.close();
  }

  protected void closeIndexSearchersAndWriters() {
    closeIndexSearchers();

    closeIndexWriters();

    closeDirectories();
  }

  protected void closeDirectories() {
    for(Directory directory : directories.values()) {
      try {
        if (directory != null) {
          directory.close();
          directory = null;
        }
      } catch (Exception ex) {
        log.error("Could not close directory", ex);
      }
    }

    directories.clear();
  }

  protected void closeIndexSearchers() {
    for(DirectoryReader directoryReader : new ArrayList<>(directoryReaders.values())) {
      try {
        if (directoryReader != null) {
          directoryReader.close();
          directoryReader = null;
        }
      } catch (Exception ex) {
        log.error("Could not close DirectoryReader", ex);
      }
    }
    directoryReaders.clear();

    indexSearchers.clear();
  }

  protected void markIndexHasBeenUpdated() {
    for(Class entityClass : new ArrayList<>(indexSearchers.keySet()))
      markIndexHasBeenUpdated(entityClass);
  }

  protected void markIndexHasBeenUpdated(Class<? extends UserDataEntity> entityClass) {
    entityClass = findIndexEntityClass(entityClass);
    indexSearchers.put(entityClass, null);
  }

  protected void closeIndexWriters() {
    for(Class entityClass : new HashMap<>(indexWriters).keySet()) {
      IndexWriter indexWriter = indexWriters.get(entityClass);
      try {
        if (indexWriter != null) {
          indexWriter.close();
          indexWriter = null;
        }
      } catch (Exception ex) {
        log.error("Could not close IndexWriter", ex);
      }
    }

    indexWriters.clear();
  }

  protected void setDirectory(Directory directory) {
    if(directory instanceof RAMDirectory) { // TODO: if not read path from FSDirectory and create Entity specific sub directories
      for(Class classWithOwnIndexDirectory : ClassesWithOwnIndexDirectories)
        directories.put(classWithOwnIndexDirectory, new RAMDirectory());
    }
    directories.put(DefaultIndexDirectoryClass, directory);

    isIndexReady = directory != null;

    createIndexSearchersAndWriters();
  }

  protected void createDirectoryAndIndexSearcherAndWriterForDeepThought(DeepThought deepThought) {
    if(directories.size() > 0) { // on unit tests
      isInitialized = true;
      return;
    }
    if(deepThought == null) { // there's nothing to create an index for
      return;
    }

    try {
      File deepThoughtIndexDirectory = new File(new File(Application.getDataFolderPath(), "index"), deepThought.getId());
      boolean indexDirExists = deepThoughtIndexDirectory.exists();

      for(Class classWithOwnIndexDirectory : ClassesWithOwnIndexDirectories) {
        File indexDirectory = new File(deepThoughtIndexDirectory, getDirectoryNameForClass(classWithOwnIndexDirectory));
        directories.put(classWithOwnIndexDirectory, FSDirectory.open(indexDirectory));
      }

      File defaultIndexDirectory = new File(deepThoughtIndexDirectory, "default");
      directories.put(DefaultIndexDirectoryClass, FSDirectory.open(defaultIndexDirectory));

      isIndexReady = true;

      createIndexSearchersAndWriters();

      if(indexDirExists == false) {
        rebuildIndex(); // do not rebuild index asynchronously as Application depends on some functions of SearchEngine (like Entries without Tags)
      }

      isInitialized = true;
    } catch(Exception ex) {
      log.error("Could not open Lucene Index Directory for DeepThought " + deepThought, ex);
    }
  }

  protected String getDirectoryNameForClass(Class classWithOwnIndexDirectory) {
    if(Entry.class.equals(classWithOwnIndexDirectory))
      return "entries";
    if(ReferenceBase.class.equals(classWithOwnIndexDirectory))
      return "references";
    if(Category.class.equals(classWithOwnIndexDirectory))
      return "categories";
    if(FileLink.class.equals(classWithOwnIndexDirectory))
      return "files";

    return classWithOwnIndexDirectory.getSimpleName().toLowerCase() + "s"; // 's' for plural
  }

  protected void createIndexSearchersAndWriters() {
    defaultAnalyzer = new DeepThoughtAnalyzer();
//    defaultAnalyzer = new StandardAnalyzer(Version.LUCENE_47);

    for(Class entityClass : directories.keySet())
      createIndexSearcherAndWriter(entityClass);
  }

  protected void createIndexSearcherAndWriter(Class entityClass) {
    IndexWriter indexWriter = createIndexWriter(entityClass);
    indexWriters.put(entityClass, indexWriter);

    createIndexSearcherOnOpeningDirectory(entityClass);
  }

  /**
   * <p>
   *   On opening an index directory there are no new changes yet
   *   so on first call call this simple method to create an IndexSearcher.
   * </p>
   * @return
   * @param entityClass
   */
  protected void createIndexSearcherOnOpeningDirectory(Class entityClass) {
    try {
      DirectoryReader directoryReader = createDirectoryReader(entityClass);

      IndexSearcher indexSearcher = new IndexSearcher(directoryReader);
      indexSearchers.put(entityClass, indexSearcher);
    } catch(Exception ex) {
      log.error("Could not create IndexSearcher for EntityClass " + entityClass, ex);
    }
  }

  protected IndexWriter createIndexWriter(Class<? extends UserDataEntity> entryClass) {
    return createIndexWriter(directories.get(entryClass));
  }

  /**
   * <p>
   *   Creates a new IndexWriter with specified Analyzer.
   * </p>
   * @param directory
   * @return Created IndexWriter or null on failure!
   */
  protected IndexWriter createIndexWriter(Directory directory) {
    if(isReadOnly == false) { // it is better that when once isReadOnly has been set to true not to unset it again even though write access would not be possible again
      try { // as otherwise all changes done till index becomes writable again would be lost which could lead to data inconsistency
        IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_47, defaultAnalyzer);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
        return new IndexWriter(directory, config);
      } catch (Exception ex) {
        if (ex instanceof LockObtainFailedException) {
          if (isReadOnly == false)
            Application.notifyUser(new Notification(NotificationType.HasOnlyReadOnlyAccessToData)); // TODO: add message
          isReadOnly = true;
        } else
          log.error("Could not create IndexWriter for DeepThought " + deepThought + " (directory = " + directory + ")", ex);
      }

      isIndexReady = false;
    }
    return null;
  }

  protected IndexWriter getIndexWriter() {
    return getIndexWriter(DefaultIndexDirectoryClass);
  }

  protected IndexWriter getIndexWriter(Class<? extends UserDataEntity> entityClass) {
    entityClass = findIndexEntityClass(entityClass);
    return indexWriters.get(entityClass);
  }

//  /**
//   * <p>
//   *   On opening an index directory there are no new changes yet
//   *   so on first call call this simple method to create an IndexSearcher.
//   * </p>
//   * @return
//   */
//  protected IndexSearcher createIndexSearcherOnOpeningDirectory() {
//    try {
//      directoryReader = DirectoryReader.open(indexWriter, true);
//      indexSearcher = new IndexSearcher(directoryReader);
//    } catch(Exception ex) {
//      log.error("Could not create IndexSearcher", ex);
//    }
//
//    return indexSearcher;
//  }

  /**
   * <p>
   *   As IndexSearcher only reflects the index of the time IndexSearcher has been created,
   *   on all changes to index IndexSearcher gets closed.
   *   On next usage IndexSearcher will then be recreated to reflect changes commit to index.
   * </p>
   * @return
   */
  protected IndexSearcher getIndexSearcher() {
      return getIndexSearcher(DefaultIndexDirectoryClass);
  }

  protected IndexSearcher getIndexSearcher(Class entityClass) {
    entityClass = findIndexEntityClass(entityClass);
    IndexSearcher indexSearcher = indexSearchers.get(entityClass);

    if(indexSearcher == null) {
      try {
        DirectoryReader directoryReader = createDirectoryReader(entityClass);

        indexSearcher = new IndexSearcher(directoryReader);
        indexSearchers.put(entityClass, indexSearcher);
      } catch(Exception ex) {
        log.error("Could not create IndexSearcher", ex);
      }
    }

    return indexSearcher;
  }

  protected DirectoryReader createDirectoryReader(Class entityClass) {
    try {
      if(isReadOnly) {
        DirectoryReader directoryReader = DirectoryReader.open(directories.get(entityClass)); // open readonly
        directoryReaders.put(entityClass, directoryReader);
        return directoryReader;
      }
      else if(directoryReaders.containsKey(entityClass) == false) { // on startup
        DirectoryReader directoryReader = DirectoryReader.open(getIndexWriter(entityClass), true);
        directoryReaders.put(entityClass, directoryReader);
        return directoryReader;
      }
      else {
        DirectoryReader directoryReader = directoryReaders.get(entityClass);
        DirectoryReader newDirectoryReader = DirectoryReader.openIfChanged(directoryReader, getIndexWriter(entityClass), true);
        if (newDirectoryReader != null) {
          directoryReaders.put(entityClass, newDirectoryReader);
          directoryReader = newDirectoryReader;
        }
        return directoryReader;
      }
    } catch(Exception ex) {
      log.error("Could not create DirectoryReader for Entity class " + entityClass, ex);
    }

    return null;
  }

  private Class findIndexEntityClass(Class entityClass) {
    if(ClassesWithOwnIndexDirectories.contains(entityClass))
      return entityClass;
    if(SeriesTitle.class.equals(entityClass) || Reference.class.equals(entityClass) || ReferenceSubDivision.class.equals(entityClass))
      return ReferenceBase.class;

    return DefaultIndexDirectoryClass;
  }


  /**
   * Know what you do when you call this method!
   * Deletes index and rebuilds it from scratch which can take a very long time if you have a big database
   */
  public void rebuildIndex() {
    if(isReadOnly == true)
      return;
    if(isIndexReady == false)
      return;

    deleteIndex();

    log.info("Going to rebuild Lucene index ...");

    try {
      for (Entry entry : deepThought.getEntries())
        indexEntry(entry);

      for (Tag tag : deepThought.getTags())
        indexTag(tag);

      for (Category category : deepThought.getCategories())
        indexCategory(category);

      for (Person person : deepThought.getPersons())
        indexPerson(person);

      for (SeriesTitle seriesTitle : deepThought.getSeriesTitles())
        indexSeriesTitle(seriesTitle);
      for (Reference reference : deepThought.getReferences())
        indexReference(reference);
      for (ReferenceSubDivision subDivision : deepThought.getReferenceSubDivisions())
        indexReferenceSubDivision(subDivision);

      for(Note note : deepThought.getNotes())
        indexNote(note);

      for(FileLink file : deepThought.getFiles())
        indexFile(file);

      for(IndexWriter indexWriter : indexWriters.values())
        indexWriter.commit();
      log.info("Done rebuilding Lucene Index.");
    } catch(Exception ex) {
      log.error("Could not rebuild Lucene Index", ex);
    }
  }

  /**
   * <p>
   *   Deletes complete Lucene index.
   *   We hope you know what you are doing.
   * </p>
   */
  public void deleteIndex() {
    log.info("Going to delete Lucene Index ...");
    try {
      for(IndexWriter indexWriter : indexWriters.values()) {
        indexWriter.deleteAll();
        indexWriter.prepareCommit();
        indexWriter.commit();
      }
      log.info("Lucene Index successfully deleted");
    } catch(Exception ex) {
      log.error("Could not delete Lucene index", ex);
    }

    markIndexHasBeenUpdated();
  }


  /**
   * Calling commit() is a costly operation
   * -> don't call it on each update / deletion, wait some time before commit accumulated changes.
   * @param indexWriter
   */
  protected void addWriterToCommitQueue(IndexWriter indexWriter) {
    synchronized(indicesWithUnsavedChanges) {
      indicesWithUnsavedChanges.add(indexWriter);
    }

    startCommitIndicesTimer();
  }

  protected synchronized void startCommitIndicesTimer() {
    if(commitIndicesTimer != null) { // timer already started
      return;
    }

    commitIndicesTimer = new Timer("Commit Indices Timer");

    commitIndicesTimer.schedule(new TimerTask() {
      @Override
      public void run() {
        commitIndicesTimer = null;
        commitUnsavedChanges();
      }
    }, WAIT_TIME_BEFORE_COMMITTING_INDICES_MILLIS);
  }

  protected void commitUnsavedChanges() {
    synchronized(indicesWithUnsavedChanges) {
      for(IndexWriter indexWriter : indicesWithUnsavedChanges) {
        try {
          indexWriter.commit();
        } catch (Exception e) {
          log.error("Could not commit Index " + indexWriter.getDirectory(), e);
        }
      }

      indicesWithUnsavedChanges.clear();
    }
  }


  public void indexEntity(UserDataEntity entity) {
    if(isReadOnly == true)
      return;
    if(entity.isPersisted() == false || entity.isDeleted() == true)
      return;

    if(entity instanceof Entry)
      indexEntry((Entry)entity);
    else if(entity instanceof Tag)
      indexTag((Tag)entity);
    else if(entity instanceof Category)
      indexCategory((Category)entity);
    else if(entity instanceof Person)
      indexPerson((Person)entity);
    else if(entity instanceof SeriesTitle)
      indexSeriesTitle((SeriesTitle)entity);
    else if(entity instanceof Reference)
      indexReference((Reference)entity);
    else if(entity instanceof ReferenceSubDivision)
      indexReferenceSubDivision((ReferenceSubDivision)entity);
    else if(entity instanceof Note)
      indexNote((Note)entity);
    else if(entity instanceof FileLink)
      indexFile((FileLink) entity);
  }

  protected void indexEntry(Entry entry) {
//    if(StringUtils.isNullOrEmpty(entry.getContent()) || StringUtils.isNullOrEmpty(entry.getAbstract()))
//      return;

    try {
      ((DeepThoughtAnalyzer) defaultAnalyzer).setNextEntryToBeAnalyzed(entry);

      Document doc = createDocumentFromEntry(entry);

      indexDocument(doc, Entry.class);
    } catch(Exception ex) {
      log.error("Could not index Entry " + entry, ex);
    }
  }

  protected Document createDocumentFromEntry(Entry entry) {
    Document doc = new Document();

    doc.add(new StringField(FieldName.EntryId, entry.getId(), Field.Store.YES));

    doc.add(new Field(FieldName.EntryAbstract, entry.getAbstractAsPlainText(), TextField.TYPE_NOT_STORED));
    doc.add(new Field(FieldName.EntryContent, entry.getContentAsPlainText(), TextField.TYPE_NOT_STORED));

    doc.add(new LongField(FieldName.EntryIndex, entry.getEntryIndex(), Field.Store.YES));

    doc.add(new LongField(FieldName.EntryCreated, entry.getCreatedOn().getTime(), Field.Store.YES));
    doc.add(new LongField(FieldName.EntryModified, entry.getModifiedOn().getTime(), Field.Store.YES));

    if(entry.hasTags()) {
      for (Tag tag : entry.getTags()) {
        doc.add(new StringField(FieldName.EntryTagsIds, tag.getId(), Field.Store.YES));
//        doc.add(new StringField(FieldName.EntryTags, tag.getName().toLowerCase(), Field.Store.YES));
      }
    }
    else
      doc.add(new StringField(FieldName.EntryNoTags, NoTagsFieldValue, Field.Store.NO));

//    if(entry.hasCategories()) {
//      for (Category category : entry.getCategories())
//        doc.add(new Field(FieldName.EntryCategories, category.getName(), TextField.TYPE_NOT_STORED));
//    }
//    else
//      doc.add(new StringField(FieldName.EntryNoCategories, NoCategoriesFieldValue, Field.Store.NO));
//
//    if(entry.hasPersons()) {
//      for (Person person : entry.getPersons())
//        doc.add(new Field(FieldName.EntryPersons, person.getFirstName() + " " + person.getLastName(), TextField.TYPE_NOT_STORED));
//    }
//    else
//      doc.add(new Field(FieldName.EntryNoPersons, NoPersonsFieldValue, TextField.TYPE_NOT_STORED));
//
//    if(entry.hasNotes()) {
//      for (Note note : entry.getNotes())
//        doc.add(new Field(FieldName.EntryNotes, note.getNote(), TextField.TYPE_NOT_STORED));
//    }
//    else
//      doc.add(new Field(FieldName.EntryNoNotes, NoNotesFieldValue, TextField.TYPE_NOT_STORED));
//
//    try {
//      if (entry.getReferenceSubDivision() != null)
//        doc.add(new Field(FieldName.EntryReferenceSubDivision, getReferenceSubDivisionIndexTerm(entry.getReferenceSubDivision()), TextField.TYPE_NOT_STORED));
//      else
//        doc.add(new Field(FieldName.EntryNoReferenceSubDivision, NoReferenceSubDivisionFieldValue, TextField.TYPE_NOT_STORED));
//
//      if (entry.getReference() != null && StringUtils.isNotNullOrEmpty(entry.getReference().getTextRepresentation()))
//        doc.add(new Field(FieldName.EntryReference, getReferenceIndexTerm(entry.getReference()), TextField.TYPE_NOT_STORED));
//      else
//        doc.add(new Field(FieldName.EntryNoReference, NoReferenceFieldValue, TextField.TYPE_NOT_STORED));
//
//      if (entry.getSeries() != null)
//        doc.add(new Field(FieldName.EntrySeries, getReferenceBaseTitleIndexTerm(entry.getSeries()), TextField.TYPE_NOT_STORED));
//      else
//        doc.add(new Field(FieldName.EntryNoSeries, NoSeriesFieldValue, TextField.TYPE_NOT_STORED));
//    } catch(Exception ex) {
//      log.error("Could not index Reference of Entry " + entry, ex);
//    }

    return doc;
  }

  protected void indexTag(Tag tag) {
    Document doc = new Document();

    doc.add(new StringField(FieldName.TagId, tag.getId(), Field.Store.YES));
    doc.add(new StringField(FieldName.TagName, tag.getName().toLowerCase(), Field.Store.NO)); // for an not analyzed String it's important to index it lower case as only than lower case search finds ti

    indexDocument(doc, Tag.class);
  }

  protected void indexCategory(Category category) {
    if(category.getParentCategory() == null) // invisible 'I know me nothing knowing' Category (= parent of all Top Level Categories)
      return;

    Document doc = new Document();

    doc.add(new StringField(FieldName.CategoryId, category.getId(), Field.Store.YES));
    doc.add(new StringField(FieldName.CategoryName, category.getName().toLowerCase(), Field.Store.NO));
    doc.add(new StringField(FieldName.CategoryDescription, category.getDescription().toLowerCase(), Field.Store.NO));

    if(category.isTopLevelCategory()) {
      doc.add(new StringField(FieldName.CategoryParentCategoryId, NoParentCategoryIdFieldValue, Field.Store.YES));
      doc.add(new StringField(FieldName.CategoryParentCategoryName, NoParentCategoryNameFieldValue, Field.Store.YES));
    }
    else {
      doc.add(new StringField(FieldName.CategoryParentCategoryId, category.getParentCategory().getId(), Field.Store.YES));
      doc.add(new StringField(FieldName.CategoryParentCategoryName, category.getParentCategory().getName().toLowerCase(), Field.Store.NO));
    }

    indexDocument(doc, Category.class);
  }

  protected void indexPerson(Person person) {
    Document doc = new Document();

    doc.add(new StringField(FieldName.PersonId, person.getId(), Field.Store.YES));
    doc.add(new StringField(FieldName.PersonFirstName, person.getFirstName().toLowerCase(), Field.Store.NO));
    doc.add(new StringField(FieldName.PersonLastName, person.getLastName().toLowerCase(), Field.Store.NO));

    indexDocument(doc, Person.class);
  }

  protected void indexSeriesTitle(SeriesTitle seriesTitle) {
    Document doc = new Document();

    doc.add(new StringField(FieldName.ReferenceBaseId, seriesTitle.getId(), Field.Store.YES));
    doc.add(new IntField(FieldName.ReferenceBaseType, SeriesTitleReferenceBaseType, Field.Store.NO));

    addSeriesTitleFields(seriesTitle, doc);

    indexDocument(doc, ReferenceBase.class);
  }

  protected void addSeriesTitleFields(SeriesTitle seriesTitle, Document doc) {
    doc.add(new StringField(FieldName.SeriesTitleTitle, getReferenceBaseTitleIndexTerm(seriesTitle), Field.Store.NO));
  }

  protected String getReferenceBaseTitleIndexTerm(ReferenceBase referenceBase) {
    String indexTerm = referenceBase.getSubTitle() == null ? referenceBase.getTitle() : referenceBase.getTitle() + " " + referenceBase.getSubTitle();
    return indexTerm.trim().toLowerCase();
  }

  protected void indexReference(Reference reference) {
    Document doc = new Document();

    doc.add(new StringField(FieldName.ReferenceBaseId, reference.getId(), Field.Store.YES));
    doc.add(new IntField(FieldName.ReferenceBaseType, ReferenceReferenceBaseType, Field.Store.NO));

    addReferenceFields(reference, doc);

    indexDocument(doc, ReferenceBase.class);
  }

  protected void addReferenceFields(Reference reference, Document doc) {
//    doc.add(new StringField(FieldName.ReferenceTitle, getReferenceIndexTerm(reference), Field.Store.NO));
    doc.add(new StringField(FieldName.ReferenceTitle, getReferenceBaseTitleIndexTerm(reference), Field.Store.NO));

    if(reference.getIssueOrPublishingDate() != null) {
      doc.add(new StringField(FieldName.ReferenceIssueOrPublishingDate, reference.getIssueOrPublishingDate().toLowerCase().trim(), Field.Store.NO));
    }

    if(reference.getPublishingDate() != null) {
      doc.add(new LongField(FieldName.ReferencePublishingDate, reference.getPublishingDate().getTime(), Field.Store.NO));
    }

    if(reference.getSeries() != null) {
      addSeriesTitleFields(reference.getSeries(), doc);
    }
  }

  protected String getReferenceIndexTerm(Reference reference) {
//    return (reference.getPreview() + " " + reference.getIssueOrPublishingDate() + " " + reference.getSubTitle()).toLowerCase();

    String indexTerm = reference.getTitle();
    if(reference.getSubTitle() != null)
      indexTerm = indexTerm + " " + reference.getSubTitle();

    if(reference.getSeries() != null)
      indexTerm = getReferenceBaseTitleIndexTerm(reference.getSeries()) + " " + indexTerm;

    return indexTerm.trim().toLowerCase();
  }

  protected void indexReferenceSubDivision(ReferenceSubDivision referenceSubDivision) {
    Document doc = new Document();

    doc.add(new StringField(FieldName.ReferenceBaseId, referenceSubDivision.getId(), Field.Store.YES));
    doc.add(new IntField(FieldName.ReferenceBaseType, ReferenceSubDivisionReferenceBaseType, Field.Store.NO));

    addReferenceSubDivisionFields(referenceSubDivision, doc);

    indexDocument(doc, ReferenceBase.class);
  }

  protected void addReferenceSubDivisionFields(ReferenceSubDivision referenceSubDivision, Document doc) {
    doc.add(new StringField(FieldName.ReferenceSubDivisionTitle, getReferenceBaseTitleIndexTerm(referenceSubDivision), Field.Store.NO));

    if(referenceSubDivision.getReference() != null) {
      addReferenceFields(referenceSubDivision.getReference(), doc);
    }
  }

  protected String getReferenceSubDivisionIndexTerm(ReferenceSubDivision subDivision) {
//    return (referenceSubDivision.getTextRepresentation() + " " + referenceSubDivision.getSubTitle()).toLowerCase();

    String indexTerm = subDivision.getTitle();
    if(subDivision.getSubTitle() != null)
      indexTerm = indexTerm + " " + subDivision.getSubTitle();

    if(subDivision.getReference() != null)
      indexTerm = getReferenceIndexTerm(subDivision.getReference()) + " " + indexTerm;

    return indexTerm.trim().toLowerCase();
  }

  protected void indexNote(Note note) {
    Document doc = new Document();

    doc.add(new StringField(FieldName.NoteId, note.getId(), Field.Store.YES));
    doc.add(new StringField(FieldName.NoteNote, note.getNote().toLowerCase(), Field.Store.NO));

    indexDocument(doc, Note.class);
  }

  protected void indexFile(FileLink file) {
    Document doc = new Document();

    doc.add(new StringField(FieldName.FileId, file.getId(), Field.Store.YES));
    doc.add(new StringField(FieldName.FileName, file.getName().toLowerCase(), Field.Store.NO));
    doc.add(new StringField(FieldName.FileUri, file.getUriString().toLowerCase(), Field.Store.NO));

    if(file.getFileType() != null)
      doc.add(new StringField(FieldName.FileFileType, file.getFileType().getId(), Field.Store.NO));
    doc.add(new StringField(FieldName.FileDescription, file.getDescription().toLowerCase(), Field.Store.NO));
    if(StringUtils.isNotNullOrEmpty(file.getSourceUriString()))
      doc.add(new StringField(FieldName.FileSourceUri, file.getSourceUriString().toLowerCase(), Field.Store.NO));

    addBooleanFieldToDocument(doc, FieldName.FileIsEmbeddableInHtml, FileUtils.isFileEmbeddableInHtml(file));

    addBooleanFieldToDocument(doc, FieldName.FileIsAttachedToEntries, file.isAttachedToEntries());
    addBooleanFieldToDocument(doc, FieldName.FileIsEmbeddedInEntries, file.isEmbeddedInEntries());
    addBooleanFieldToDocument(doc, FieldName.FileIsAttachedToReferenceBase, file.isAttachedToReferenceBases());
    addBooleanFieldToDocument(doc, FieldName.FileIsEmbeddedInReferenceBase, file.isEmbeddedInReferenceBases());

    indexDocument(doc, FileLink.class);
  }

  protected void addBooleanFieldToDocument(Document doc, String fieldName, boolean isTrueBooleanValue) {
    if(isTrueBooleanValue)
      doc.add(new StringField(fieldName, BooleanFieldTrueValue, Field.Store.NO));
    else
      doc.add(new StringField(fieldName, BooleanFieldFalseValue, Field.Store.NO));
  }

  protected void addDateFieldToDocument(Document doc, String fieldName, Date date) {
    addDateFieldToDocument(doc, fieldName, date, DateTools.Resolution.MINUTE);
  }

  protected void addDateFieldToDocument(Document doc, String fieldName, Date date, DateTools.Resolution resolution) {
    doc.add(new Field(fieldName,
        DateTools.timeToString(date.getTime(), resolution),
        Field.Store.NO, Field.Index.NOT_ANALYZED));
  }

  protected Query createQueryForDateField(String fieldName, Date date) {
    return createQueryForDateField(fieldName, date, DateTools.Resolution.MINUTE);
  }

  protected Query createQueryForDateField(String fieldName, Date date, DateTools.Resolution resolution) {
    String dateValue = DateTools.dateToString(date, resolution);
    FieldCacheRangeFilter<String> filter = FieldCacheRangeFilter.newStringRange(fieldName, dateValue, null, true, false);
    return null; // TODO:
  }

  protected void indexDocument(Document doc) {
    indexDocument(doc, DefaultIndexDirectoryClass);
  }

  protected void indexDocument(Document doc, Class entityClass) {
    try {
      log.info("Indexing document {}", doc);
      IndexWriter indexWriter = getIndexWriter(entityClass);
      indexWriter.addDocument(doc);
      addWriterToCommitQueue(indexWriter);
    } catch(Exception ex) {
      log.error("Could not index Document " + doc, ex);
    }

    markIndexHasBeenUpdated(entityClass); // so that on next search updates are reflected
  }


  /*        Search          */

  protected void getEntriesForTag(final Tag tag, final SearchCompletedListener<Collection<Entry>> listener) {
    if(isInitialized) {
      queryForEntriesWithTag(tag, listener);
    }
    else {
      final Timer waitTillIndexIsReadyTimer = new Timer();
      waitTillIndexIsReadyTimer.schedule(new TimerTask() {
        @Override
        public void run() {
          if(isInitialized) {
            this.cancel(); // stop Timer
            waitTillIndexIsReadyTimer.purge();

            queryForEntriesWithTag(tag, listener);
          }
        }
      }, 200, 200);
    }
  }

  protected void queryForEntriesWithTag(Tag tag, final SearchCompletedListener<Collection<Entry>> listener) {
    if(tag instanceof EntriesWithoutTagsSystemTag) {
      getEntriesWithoutTags(listener);
    }
    else {
      Query query = null;

      if(tag instanceof AllEntriesSystemTag) {
        query = new WildcardQuery(new Term(FieldName.EntryId, "*"));
      }
      else {
        query = new TermQuery(new Term(FieldName.EntryTagsIds, tag.getId()));
      }

      listener.completed(new LazyLoadingLuceneSearchResultsList(getIndexSearcher(Entry.class), query, Entry.class,
          FieldName.EntryId, 100000, SortOrder.Descending, FieldName.EntryCreated));
    }
  }

  protected void getEntriesWithoutTags(final SearchCompletedListener<Collection<Entry>> listener) {
    final Query query = new TermQuery(new Term(FieldName.EntryNoTags, NoTagsFieldValue));

    Application.getThreadPool().runTaskAsync(new Runnable() {
      @Override
      public void run() {
        try {
          listener.completed(new LazyLoadingLuceneSearchResultsList<Entry>(getIndexSearcher(Entry.class), query, Entry.class, FieldName.EntryId, 100000,
              SortOrder.Descending, FieldName.EntryCreated));
        } catch (Exception ex) {
          log.error("Could not search for Entries without Tags", ex);
        }
      }
    });
  }

  @Override
  protected void filterTagsForEmptySearchTerm(TagsSearch search) {
    search.setHasEmptySearchTerm(true);

//    if(isIndexReady == false) {
////      if(Application.getDeepThought() != null)
////        search.addResult(new FilterTagsSearchResult("", Application.getDeepThought().getSortedTags()));
//      search.setRelevantMatchesSorted(new ArrayList<Tag>());
//
//      search.fireSearchCompleted();
//      return;
//    }

    Query query = new WildcardQuery(new Term(FieldName.TagName, "*"));
    if(search.isInterrupted())
      return;

    search.setRelevantMatchesSorted(new LazyLoadingLuceneSearchResultsList(getIndexSearcher(Tag.class), query, Tag.class,
        FieldName.TagId, 100000, SortOrder.Ascending, FieldName.TagName));

    search.fireSearchCompleted();
  }

  protected void filterTags(TagsSearch search, String[] tagNamesToFilterFor) {
    IndexSearcher indexSearcher = getIndexSearcher(Tag.class);

    for(String tagNameToFilterFor : tagNamesToFilterFor) {
      if(search.isInterrupted())
        return;
      try {
        String searchTerm = QueryParser.escape(tagNameToFilterFor);
        if(search.isInterrupted())
          return;

        Tag exactMatch = getExactMatchTag(indexSearcher, searchTerm);

        Query query = new WildcardQuery(new Term(FieldName.TagName, "*" + searchTerm + "*"));
        if(search.isInterrupted())
          return;

        search.addResult(new TagsSearchResult(tagNameToFilterFor, new LazyLoadingLuceneSearchResultsList(indexSearcher, query, Tag.class,
            FieldName.TagId, 100000), exactMatch));
      } catch(Exception ex) {
        log.error("Could not parse query " + tagNamesToFilterFor, ex);
        // TODO: set error flag in search
      }
    }

    getAllRelevantTagsSorted(search, indexSearcher);

    search.fireSearchCompleted();
  }

  protected Tag getExactMatchTag(IndexSearcher indexSearcher, String searchTerm) {
    Query exactMatchQuery = new TermQuery(new Term(FieldName.TagName, searchTerm));
    List<Tag> exactMatchResults = new LazyLoadingLuceneSearchResultsList(indexSearcher, exactMatchQuery, Tag.class, FieldName.TagId, 2);
    return exactMatchResults.size() == 1 ? exactMatchResults.get(0) : null;
  }

  protected void getAllRelevantTagsSorted(TagsSearch search, IndexSearcher indexSearcher) {
    BooleanQuery sortRelevantTagsQuery = new BooleanQuery();
    for(TagsSearchResult result : search.getResults().getResults()) {
      if(search.isInterrupted())
        return;

      String searchTerm = QueryParser.escape(result.getSearchTerm());
      if(result.hasExactMatch() && result != search.getResults().getLastResult())
        sortRelevantTagsQuery.add(new TermQuery(new Term(FieldName.TagName, searchTerm)), BooleanClause.Occur.SHOULD);
      else
        sortRelevantTagsQuery.add(new WildcardQuery(new Term(FieldName.TagName, "*" + searchTerm + "*")), BooleanClause.Occur.SHOULD);
    }

    if(search.isInterrupted())
      return;

    List<Tag> relevantMatchesSorted = new LazyLoadingLuceneSearchResultsList(indexSearcher, sortRelevantTagsQuery, Tag.class,
        FieldName.TagId, 100000, SortOrder.Ascending, FieldName.TagName);
    search.setRelevantMatchesSorted(relevantMatchesSorted);
  }

  protected void findAllEntriesHavingTheseTagsAsync(Collection<Tag> tagsToFilterFor, String[] tagNamesToFilterFor, SearchCompletedListener<FindAllEntriesHavingTheseTagsResult> listener) {
    Collection<Entry> entriesHavingFilteredTags = new LazyLoadingList<Entry>(Entry.class);
    Collection<Tag> tagsOnEntriesContainingFilteredTags = new HashSet<>();
    BooleanQuery query = new BooleanQuery();

    for(Tag tag : tagsToFilterFor) {
      query.add(new BooleanClause(new TermQuery(new Term(FieldName.EntryTagsIds, tag.getId())), BooleanClause.Occur.MUST));
    }

    try {
      entriesHavingFilteredTags.addAll(getBaseEntitiesFromQuery(Entry.class, query, FieldName.EntryId, SortOrder.Descending, FieldName.EntryCreated));
      for(Entry resultEntry : entriesHavingFilteredTags)
        tagsOnEntriesContainingFilteredTags.addAll(resultEntry.getTags()); // TODO: this is really bad as in this way all Entries of entriesHavingFilteredTags get loaded

      if(tagNamesToFilterFor != null && tagNamesToFilterFor.length > 0)
        tagsOnEntriesContainingFilteredTags = filterTagsOnEntriesContainingFilteredTagsWithSearchTerm(tagsOnEntriesContainingFilteredTags, tagNamesToFilterFor);
    } catch(Exception ex) {
      log.error("Could not execute Query " + query.toString(), ex);
    }

    listener.completed(new FindAllEntriesHavingTheseTagsResult(entriesHavingFilteredTags, tagsOnEntriesContainingFilteredTags));
  }

  protected Collection<Tag> filterTagsOnEntriesContainingFilteredTagsWithSearchTerm(Collection<Tag> tagsOnEntriesContainingFilteredTags, String[] tagNamesToFilterFor) {
    BooleanQuery searchTermQuery = new BooleanQuery();
    for(String tagName : tagNamesToFilterFor) {
      searchTermQuery.add(new WildcardQuery(new Term(FieldName.TagName, "*" + tagName + "*")), BooleanClause.Occur.SHOULD);
    }

    Collection<String> tagsWithSearchTermIds = getEntityIdsFromQuery(Tag.class, searchTermQuery, FieldName.TagId, SortOrder.Ascending, FieldName.TagName);

    Map<String, Tag> tagsOnEntriesIds = new HashMap<>();
    for(Tag tagOnEntries : tagsOnEntriesContainingFilteredTags)
      tagsOnEntriesIds.put(tagOnEntries.getId(), tagOnEntries);

    tagsWithSearchTermIds.retainAll(tagsOnEntriesIds.keySet());

    Collection<Tag> sortedResultTags = new ArrayList<>();
    for(String tagId : tagsWithSearchTermIds) {
      sortedResultTags.add(tagsOnEntriesIds.get(tagId));
    }

    return sortedResultTags;
  }

  protected <T extends BaseEntity> List<T> getBaseEntitiesFromQuery(Class<T> type, Query query, String idFieldName) {
    Collection<String> ids = getEntityIdsFromQuery(type, query, idFieldName);

    return getBaseEntitiesFromIds(type, ids);
  }

  protected <T extends BaseEntity> List<T> getBaseEntitiesFromQuery(Class<T> type, Query query, String idFieldName, SortOrder sortOrder, String... sortFieldNames) {
    Collection<String> ids = getEntityIdsFromQuery(type, query, idFieldName, sortOrder, sortFieldNames);

    return getBaseEntitiesFromIds(type, ids);
  }

  protected <T extends BaseEntity> Collection<String> getEntityIdsFromQuery(Class<T> type, Query query, String idFieldName) {
    return getEntityIdsFromQuery(type, query, idFieldName, SortOrder.Unsorted);
  }

  protected <T extends BaseEntity> Collection<String> getEntityIdsFromQuery(Class<T> type, Query query, String idFieldName, SortOrder sortOrder, String... sortFieldNames) {
    List<String> ids = new ArrayList<>();

    try {
      IndexSearcher searcher = getIndexSearcher(type);

      ScoreDoc[] hits = searcher.search(query, 10000, getSorting(sortOrder, sortFieldNames)).scoreDocs;

      for (int i = 0; i < hits.length; i++) {
        try {
          Document hitDoc = searcher.doc(hits[i].doc);
          ids.add(hitDoc.getField(idFieldName).stringValue());
        } catch(Exception ex) { log.error("Could not extract result from hitDoc of Query " + query, ex); }
      }
    } catch(Exception ex) {
      log.error("Could not execute Query " + query.toString(), ex);
    }

    return ids;
  }

  protected Sort getSorting(SortOrder sortOrder, String[] sortFieldNames) {
    Sort sort = new Sort();

    if(sortOrder != SortOrder.Unsorted) {
      boolean reverse = sortOrder == SortOrder.Descending;

      SortField[] sortFields = new SortField[sortFieldNames.length];
      for (int i = 0; i < sortFieldNames.length; i++) {
        sortFields[i] = new SortField(sortFieldNames[i], SortField.Type.STRING, reverse);
      }

      sort.setSort(sortFields);
    }

    return sort;
  }


  @Override
  protected void filterEntries(EntriesSearch search, String[] termsToFilterFor) {
    // TODO: i think it's better to analyze content- and abstractFilter as they are being used on analyzed fields
//    Analyzer analyzer = getAnalyzerForTextLanguage(search.getSearchTerm());
    BooleanQuery query = new BooleanQuery();

    if(search.filterOnlyEntriesWithoutTags())
      query.add(new TermQuery(new Term(FieldName.EntryNoTags, NoTagsFieldValue)), BooleanClause.Occur.MUST);
    else if(search.getEntriesMustHaveTheseTags().size() > 0) {
      BooleanQuery filterEntriesQuery = new BooleanQuery();
      for (Tag tag : search.getEntriesMustHaveTheseTags())
        filterEntriesQuery.add(new TermQuery(new Term(FieldName.EntryTagsIds, tag.getId())), BooleanClause.Occur.MUST);
      query.add(filterEntriesQuery, BooleanClause.Occur.MUST);
    }

    for(String term : termsToFilterFor) {
      term = QueryParser.escape(term);
      BooleanQuery termQuery = new BooleanQuery();

      if(search.filterContent())
        termQuery.add(new PrefixQuery(new Term(FieldName.EntryContent, term)), BooleanClause.Occur.SHOULD);
      if(search.filterAbstract())
        termQuery.add(new PrefixQuery(new Term(FieldName.EntryAbstract, term)), BooleanClause.Occur.SHOULD);

      query.add(termQuery, BooleanClause.Occur.MUST);
    }

    executeQuery(search, query, Entry.class, FieldName.EntryId, SortOrder.Descending, FieldName.EntryCreated);
  }

  @Override
  public void searchCategories(CategoriesSearch search) {
    BooleanQuery query = new BooleanQuery();
    String searchTerm = "*" + QueryParser.escape(search.getSearchTerm().toLowerCase()) + "*";

    BooleanQuery categoryNameQuery = new BooleanQuery();
    categoryNameQuery.add(new WildcardQuery(new Term(FieldName.CategoryName, searchTerm)), BooleanClause.Occur.SHOULD);
    categoryNameQuery.add(new WildcardQuery(new Term(FieldName.CategoryDescription, searchTerm)), BooleanClause.Occur.SHOULD);
    query.add(categoryNameQuery, BooleanClause.Occur.MUST);

    if(search.topLevelCategoriesOnly()) {
      query.add(new TermQuery(new Term(FieldName.CategoryParentCategoryId, NoParentCategoryIdFieldValue)), BooleanClause.Occur.MUST);
    }
    else if(search.isParentCategorySet()) {
      query.add(new TermQuery(new Term(FieldName.CategoryParentCategoryId, search.getParentCategory().getId())), BooleanClause.Occur.MUST);
    }

    executeQuery(search, query, Category.class, FieldName.CategoryId, SortOrder.Ascending, FieldName.CategoryName);
  }

  @Override
  protected void searchPersons(Search<Person> search, String personSearchTerm) {
    BooleanQuery query = new BooleanQuery();
    personSearchTerm = QueryParser.escape(personSearchTerm);

    query.add(new WildcardQuery(new Term(FieldName.PersonFirstName, "*" + personSearchTerm + "*")), BooleanClause.Occur.SHOULD);
    query.add(new WildcardQuery(new Term(FieldName.PersonLastName, "*" + personSearchTerm + "*")), BooleanClause.Occur.SHOULD);

    executeQuery(search, query, Person.class, FieldName.PersonId, SortOrder.Ascending, FieldName.PersonLastName, FieldName.PersonFirstName);
  }

  @Override
  protected void searchPersons(Search<Person> search, String lastNameSearchTerm, String firstNameSearchTerm) {
    lastNameSearchTerm = QueryParser.escape(lastNameSearchTerm);
    firstNameSearchTerm = QueryParser.escape(firstNameSearchTerm);

    BooleanQuery query = new BooleanQuery();

    query.add(new WildcardQuery(new Term(FieldName.PersonLastName, "*" + lastNameSearchTerm + "*")), BooleanClause.Occur.MUST);
    query.add(new WildcardQuery(new Term(FieldName.PersonFirstName, "*" + firstNameSearchTerm + "*")), BooleanClause.Occur.MUST);

    executeQuery(search, query, Person.class, FieldName.PersonId, SortOrder.Ascending, FieldName.PersonLastName, FieldName.PersonFirstName);
  }

  @Override
  protected void searchAllReferenceBaseTypesForSameFilter(ReferenceBasesSearch search, String referenceBaseSearchTerm) {
    BooleanQuery query = new BooleanQuery();

    referenceBaseSearchTerm = QueryParser.escape(referenceBaseSearchTerm);
    referenceBaseSearchTerm = "*" + referenceBaseSearchTerm + "*";

    query.add(new WildcardQuery(new Term(FieldName.SeriesTitleTitle, referenceBaseSearchTerm)), BooleanClause.Occur.SHOULD);

    if(search.isInterrupted())
      return;

    query.add(new WildcardQuery(new Term(FieldName.ReferenceTitle, referenceBaseSearchTerm)), BooleanClause.Occur.SHOULD);
    query.add(new WildcardQuery(new Term(FieldName.ReferenceIssueOrPublishingDate, referenceBaseSearchTerm)), BooleanClause.Occur.SHOULD);

    if(search.isInterrupted())
      return;

    query.add(new WildcardQuery(new Term(FieldName.ReferenceSubDivisionTitle, referenceBaseSearchTerm)), BooleanClause.Occur.SHOULD);

    executeReferenceBaseQuery(search, query);
  }

  @Override
  protected void searchEachReferenceBaseWithSeparateSearchTerm(ReferenceBasesSearch search, String seriesTitleSearchTerm, String referenceSearchTerm, String referenceSubDivisionSearchTerm) {
    BooleanQuery query = new BooleanQuery();

    if(seriesTitleSearchTerm != null) {
      seriesTitleSearchTerm = "*" + QueryParser.escape(seriesTitleSearchTerm) + "*";
      BooleanQuery seriesTitleQuery = new BooleanQuery();

      seriesTitleQuery.add(new TermQuery(new Term(FieldName.ReferenceBaseType, SeriesTitleReferenceBaseTypeIntRef)), BooleanClause.Occur.MUST);
      seriesTitleQuery.add(new WildcardQuery(new Term(FieldName.SeriesTitleTitle, seriesTitleSearchTerm)), BooleanClause.Occur.MUST);

      query.add(seriesTitleQuery, BooleanClause.Occur.MUST);
    }

    if(search.isInterrupted())
      return;

    if(referenceSearchTerm != null) {
      referenceSearchTerm = "*" + QueryParser.escape(referenceSearchTerm) + "*";
      BooleanQuery referenceQuery = new BooleanQuery();
      referenceQuery.add(new TermQuery(new Term(FieldName.ReferenceBaseType, ReferenceReferenceBaseTypeIntRef)), BooleanClause.Occur.MUST);

      BooleanQuery referenceValuesQuery = new BooleanQuery();
      referenceValuesQuery.add(new WildcardQuery(new Term(FieldName.SeriesTitleTitle, referenceSearchTerm)), BooleanClause.Occur.SHOULD);
      referenceValuesQuery.add(new WildcardQuery(new Term(FieldName.ReferenceTitle, referenceSearchTerm)), BooleanClause.Occur.SHOULD);
      referenceValuesQuery.add(new WildcardQuery(new Term(FieldName.ReferenceIssueOrPublishingDate, referenceSearchTerm)), BooleanClause.Occur.SHOULD);
      referenceQuery.add(referenceValuesQuery, BooleanClause.Occur.MUST);

      query.add(referenceQuery, BooleanClause.Occur.MUST);
    }

    if(search.isInterrupted())
      return;

    if(referenceSubDivisionSearchTerm != null) {
      referenceSubDivisionSearchTerm = "*" + QueryParser.escape(referenceSubDivisionSearchTerm) + "*";
      BooleanQuery subDivisionQuery = new BooleanQuery();
      subDivisionQuery.add(new TermQuery(new Term(FieldName.ReferenceBaseType, ReferenceSubDivisionReferenceBaseTypeIntRef)), BooleanClause.Occur.MUST);

      BooleanQuery subDivisionValuesQuery = new BooleanQuery();
      subDivisionValuesQuery.add(new WildcardQuery(new Term(FieldName.SeriesTitleTitle, referenceSubDivisionSearchTerm)), BooleanClause.Occur.SHOULD);
      subDivisionValuesQuery.add(new WildcardQuery(new Term(FieldName.ReferenceTitle, referenceSubDivisionSearchTerm)), BooleanClause.Occur.SHOULD);
      subDivisionValuesQuery.add(new WildcardQuery(new Term(FieldName.ReferenceIssueOrPublishingDate, referenceSubDivisionSearchTerm)), BooleanClause.Occur.SHOULD);
      subDivisionValuesQuery.add(new WildcardQuery(new Term(FieldName.ReferenceSubDivisionTitle, referenceSubDivisionSearchTerm)), BooleanClause.Occur.SHOULD);
      subDivisionQuery.add(subDivisionValuesQuery, BooleanClause.Occur.MUST);

      query.add(subDivisionQuery, BooleanClause.Occur.MUST);
    }

    executeReferenceBaseQuery(search, query);
  }

  protected void executeReferenceBaseQuery(ReferenceBasesSearch search, Query query) {
    if(search.isInterrupted())
      return;
    log.debug("Executing ReferenceBase Query " + query);

    try {
      search.setResults(new LazyLoadingLuceneSearchResultsList(getIndexSearcher(ReferenceBase.class), query, ReferenceBase.class, FieldName.ReferenceBaseId, 10000,
          SortOrder.Ascending, FieldName.ReferenceBaseType, FieldName.SeriesTitleTitle, FieldName.ReferenceTitle, FieldName.ReferencePublishingDate, FieldName.ReferenceSubDivisionTitle));
    } catch(Exception ex) {
      log.error("Could not execute Query " + query.toString(), ex);
      // TODO: set error flag in Search
    }

    search.fireSearchCompleted();
  }


  @Override
  public void searchForReferenceOfDate(String optionalSeriesTitleTitle, Search<Reference> search) {
    BooleanQuery query = new BooleanQuery();

    String issueOrPublishingDate = search.getSearchTerm();
    issueOrPublishingDate = QueryParser.escape(issueOrPublishingDate.toLowerCase());
    issueOrPublishingDate = "*" + issueOrPublishingDate + "*";

    query.add(new WildcardQuery(new Term(FieldName.ReferenceIssueOrPublishingDate, issueOrPublishingDate)), BooleanClause.Occur.MUST);
    query.add(new TermQuery(new Term(FieldName.ReferenceBaseType, ReferenceReferenceBaseTypeIntRef)), BooleanClause.Occur.MUST);

    if(search.isInterrupted())
      return;

    if(StringUtils.isNotNullOrEmpty(optionalSeriesTitleTitle)) {
      String seriesTitleSearchTerm = "*" + QueryParser.escape(optionalSeriesTitleTitle.toLowerCase()) + "*";
      query.add(new WildcardQuery(new Term(FieldName.SeriesTitleTitle, seriesTitleSearchTerm)), BooleanClause.Occur.MUST);

      if (search.isInterrupted())
        return;
    }

    log.debug("Executing Reference Query for Date " + query);

    try {
      search.setResults(new LazyLoadingLuceneSearchResultsList(getIndexSearcher(ReferenceBase.class), query, ReferenceBase.class, FieldName.ReferenceBaseId, 10000,
          SortOrder.Ascending, FieldName.SeriesTitleTitle));
    } catch(Exception ex) {
      log.error("Could not execute Query " + query.toString(), ex);
      // TODO: set error flag in Search
    }

    search.fireSearchCompleted();
  }


  @Override
  public void searchFiles(FilesSearch search) {
    BooleanQuery query = new BooleanQuery();

    String searchTerm = "*" + QueryParser.escape(search.getSearchTerm().toLowerCase()) + "*";
    BooleanQuery contentQuery = new BooleanQuery();

    if(search.searchFileName())
      contentQuery.add(new WildcardQuery(new Term(FieldName.FileName, searchTerm)), BooleanClause.Occur.SHOULD);
    if(search.searchFileUri())
      contentQuery.add(new WildcardQuery(new Term(FieldName.FileUri, searchTerm)), BooleanClause.Occur.SHOULD);
    if(search.searchFileDescription())
      contentQuery.add(new WildcardQuery(new Term(FieldName.FileDescription, searchTerm)), BooleanClause.Occur.SHOULD);

    query.add(contentQuery, BooleanClause.Occur.MUST);

    if(search.inHtmlEmbeddableFilesOnly())
      query.add(new WildcardQuery(new Term(FieldName.FileIsEmbeddableInHtml, BooleanFieldTrueValue)), BooleanClause.Occur.MUST);

    executeQuery(search, query, FileLink.class, FieldName.FileId, SortOrder.Ascending, FieldName.FileName, FieldName.FileUri);
  }


  protected void executeQuery(Search search, Query query, Class<? extends BaseEntity> resultEntityClass, String idFieldName) {
    executeQuery(search, query, resultEntityClass, idFieldName, SortOrder.Unsorted);
  }

  protected void executeQuery(Search search, Query query, Class<? extends BaseEntity> resultEntityClass, String idFieldName, SortOrder sortOrder, String... sortFieldNames) {
    if(search.isInterrupted())
      return;
    log.debug("Executing Query " + query);

    try {
      search.setResults(new LazyLoadingLuceneSearchResultsList(getIndexSearcher(resultEntityClass), query, resultEntityClass, idFieldName, 1000, sortOrder, sortFieldNames));
    } catch(Exception ex) {
      log.error("Could not execute Query " + query.toString(), ex);
      // TODO: set error flag in Search
    }

    search.fireSearchCompleted();
  }

  /**
   * Execute Query directly. Mostly for Unit testing.
   * @param query
   * @return
   */
  protected ScoreDoc[] search(Query query, Class entityClass) {
    try {
      IndexSearcher searcher = getIndexSearcher(entityClass);

      return searcher.search(query, 1000).scoreDocs;
    } catch(Exception ex) {
      log.error("Could not execute Query " + query.toString(), ex);
    }

    return new ScoreDoc[0];
  }

  protected <T extends BaseEntity> List<T> getBaseEntitiesFromIds(Class<T> type, Collection<String> searchResultIds) {
    return Application.getEntityManager().getEntitiesById(type, searchResultIds, false);
  }




  protected BytesRef getByteRefFromInteger(int intValue) {
    BytesRef bytesRef = new BytesRef(NumericUtils.BUF_SIZE_INT);
    NumericUtils.intToPrefixCoded(intValue, 0, bytesRef);

    return bytesRef;
  }

  protected BytesRef getByteRefFromLong(Long longValue) {
    BytesRef bytesRef = new BytesRef(NumericUtils.BUF_SIZE_LONG);
    NumericUtils.longToPrefixCoded(longValue, 0, bytesRef);

    return bytesRef;
  }


  protected AllEntitiesListener allEntitiesListener = new AllEntitiesListener() {
    @Override
    public void entityCreated(BaseEntity entity) {
      if(entity instanceof UserDataEntity)
        indexEntity((UserDataEntity)entity);
    }

    @Override
    public void entityUpdated(BaseEntity entity, String propertyName, Object previousValue, Object newValue) {
      if(entity instanceof UserDataEntity) {
        updateIndexForEntity((UserDataEntity) entity);
        updateEntitysEntries((UserDataEntity) entity);
      }
    }

    @Override
    public void entityDeleted(BaseEntity entity) {
      if(entity instanceof UserDataEntity)
        removeEntityFromIndex((UserDataEntity) entity);
    }

    @Override
    public void entityAddedToCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity addedEntity) {
      if(collectionHolder instanceof Entry) {
        if(isIndexedEntityOnEntry(addedEntity))
          updateIndexForEntity((UserDataEntity)collectionHolder);
      }
      else if(collectionHolder instanceof Category && addedEntity instanceof Category) { // SubCategory Added
        updateIndexForEntity((UserDataEntity)addedEntity);
      }
    }

    @Override
    public void entityRemovedFromCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity removedEntity) {
      if(collectionHolder instanceof Entry) {
        if(isIndexedEntityOnEntry(removedEntity))
          updateIndexForEntity((UserDataEntity) collectionHolder);
      }
      else if(collectionHolder instanceof Category && removedEntity instanceof Category) { // SubCategory removed
        updateIndexForEntity((UserDataEntity)removedEntity);
      }
    }
  };

  protected boolean isIndexedEntityOnEntry(BaseEntity entity) {
    return entity instanceof Tag || entity instanceof Category || entity instanceof Person || entity instanceof EntryPersonAssociation || entity instanceof Note ||
        entity instanceof SeriesTitle || entity instanceof Reference || entity instanceof ReferenceSubDivision || entity instanceof FileLink;
  }


  protected void updateIndexForEntity(UserDataEntity updatedEntity) {
    if(indexUpdatedEntitiesAfterMilliseconds == 0 || updatedEntity instanceof Tag)
      doUpdateIndexForEntity(updatedEntity);
    else {
      if(updatedEntitiesToIndex.contains(updatedEntity) == false)
        updatedEntitiesToIndex.add(updatedEntity);
      activateIndexUpdatedEntitiesTimer();
    }
  }

  protected void doUpdateIndexForEntity(UserDataEntity updatedEntity) {
    removeEntityFromIndex(updatedEntity);
    indexEntity(updatedEntity);
  }

  protected void activateIndexUpdatedEntitiesTimer() {
    if(indexUpdatedEntitiesTimer != null)
      return;

    indexUpdatedEntitiesTimer = new Timer("Index updated Entities timer");

    indexUpdatedEntitiesTimer.schedule(new TimerTask() {
      @Override
      public void run() {
        indexUpdatedEntitiesTimer = null;
        indexUpdatedEntities();
      }
    }, indexUpdatedEntitiesAfterMilliseconds);
  }

  protected void indexUpdatedEntities() {
    for(UserDataEntity updatedEntity : new ArrayList<>(updatedEntitiesToIndex)) // make a copy of updatedEntitiesToIndex as updatedEntitiesToIndex gets changed during iteration
      doUpdateIndexForEntity(updatedEntity);

    updatedEntitiesToIndex.clear();
  }

  protected void removeEntityFromIndex(UserDataEntity removedEntity) {
    if(isReadOnly == true)
      return;

    log.info("Removing Entity {} from index", removedEntity);
    updatedEntitiesToIndex.remove(removedEntity);

    String idFieldName = getIdFieldNameForEntity(removedEntity);
    IndexWriter indexWriter = getIndexWriter(removedEntity.getClass());
    try {
      if(idFieldName != null && indexWriter != null) {
        indexWriter.deleteDocuments(new Term(idFieldName, removedEntity.getId()));
        addWriterToCommitQueue(indexWriter);
      }
    } catch(Exception ex) {
      log.error("Could not delete Document for removed entity " + removedEntity, ex);
    }

    markIndexHasBeenUpdated(removedEntity.getClass()); // so that on next search updates are reflected
  }

  protected String getIdFieldNameForEntity(UserDataEntity entity) {
    if(entity instanceof Entry)
      return FieldName.EntryId;
    else if(entity instanceof Tag)
      return FieldName.TagId;
    else if(entity instanceof Category)
      return FieldName.CategoryId;
    else if(entity instanceof Person)
      return FieldName.PersonId;
    else if(entity instanceof ReferenceBase)
      return FieldName.ReferenceBaseId;
    else if(entity instanceof FileLink)
      return FieldName.FileId;
    else if(entity instanceof Note)
      return FieldName.NoteId;

    return null;
  }

  protected void updateEntitysEntries(UserDataEntity updatedEntity) {
    // currently for an Entry no information of other Entities are stored in Index. If that changes, uncomment this code again

//    if(updatedEntity instanceof Tag) {
//      for(Entry entry : ((Tag)updatedEntity).getEntries())
//        updateIndexForEntity(entry);
//    }
//    else if(updatedEntity instanceof Category) {
//      for(Entry entry : ((Category)updatedEntity).getEntries())
//        updateIndexForEntity(entry);
//    }
//    else if(updatedEntity instanceof Person) {
//      for(Entry entry : ((Person)updatedEntity).getAssociatedEntries())
//        updateIndexForEntity(entry);
//    }
//    else if(updatedEntity instanceof Note) {
//      updateIndexForEntity(((Note)updatedEntity).getEntry());
//    }
//    else if(updatedEntity instanceof SeriesTitle) {
//      for(Entry entry : ((SeriesTitle)updatedEntity).getEntries())
//        updateIndexForEntity(entry);
//    }
//    else if(updatedEntity instanceof Reference) {
//      for(Entry entry : ((Reference)updatedEntity).getEntries())
//        updateIndexForEntity(entry);
//    }
//    else if(updatedEntity instanceof ReferenceSubDivision) {
//      for(Entry entry : ((ReferenceSubDivision)updatedEntity).getEntries())
//        updateIndexForEntity(entry);
//    }
  }


  public int getIndexUpdatedEntitiesAfterMilliseconds() {
    return indexUpdatedEntitiesAfterMilliseconds;
  }

  public void setIndexUpdatedEntitiesAfterMilliseconds(int indexUpdatedEntitiesAfterMilliseconds) {
    this.indexUpdatedEntitiesAfterMilliseconds = indexUpdatedEntitiesAfterMilliseconds;
  }

}
