package net.deepthought.data.search;

import net.deepthought.Application;
import net.deepthought.data.model.Category;
import net.deepthought.data.model.DeepThought;
import net.deepthought.data.model.Entry;
import net.deepthought.data.model.EntryPersonAssociation;
import net.deepthought.data.model.FileLink;
import net.deepthought.data.model.Note;
import net.deepthought.data.model.Person;
import net.deepthought.data.model.Reference;
import net.deepthought.data.model.ReferenceBase;
import net.deepthought.data.model.ReferenceSubDivision;
import net.deepthought.data.model.SeriesTitle;
import net.deepthought.data.model.Tag;
import net.deepthought.data.model.listener.AllEntitiesListener;
import net.deepthought.data.persistence.LazyLoadingList;
import net.deepthought.data.persistence.db.BaseEntity;
import net.deepthought.data.persistence.db.UserDataEntity;
import net.deepthought.data.search.results.LazyLoadingLuceneSearchResultsList;
import net.deepthought.data.search.specific.FilterEntriesSearch;
import net.deepthought.data.search.specific.FilterReferenceBasesSearch;
import net.deepthought.data.search.specific.FilterTagsSearch;
import net.deepthought.data.search.specific.FilterTagsSearchResult;
import net.deepthought.data.search.specific.FindAllEntriesHavingTheseTagsResult;

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
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
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
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArraySet;

public class LuceneSearchEngine extends SearchEngineBase {

  public final static String NoTagsFieldValue = "notags";
  public final static String NoCategoriesFieldValue = "nocategories";
  public final static String NoPersonsFieldValue = "nopersons";
  public final static String NoNotesFieldValue = "nonotes";
  public final static String NoSeriesFieldValue = "noseries";
  public final static String NoReferenceFieldValue = "noreference";
  public final static String NoReferenceSubDivisionFieldValue = "noreferencesubdivision";

  public final static int SeriesTitleReferenceBaseType = 1;
  public final static int ReferenceReferenceBaseType = 2;
  public final static int ReferenceSubDivisionReferenceBaseType = 3;

  public static BytesRef SeriesTitleReferenceBaseTypeIntRef;
  public static BytesRef ReferenceReferenceBaseTypeIntRef;
  public static BytesRef ReferenceSubDivisionReferenceBaseTypeIntRef;


  private final static Logger log = LoggerFactory.getLogger(LuceneSearchEngine.class);


  // TODO: what about Category, Notes and Files?
  protected final static List<Class> ClassesWithOwnIndexDirectories = Arrays.asList(new Class[] { Entry.class, Tag.class, ReferenceBase.class, Person.class });

  protected final static Class DefaultIndexDirectoryClass = UserDataEntity.class;


  protected Map<Class, Directory> directories = new HashMap<>();

  protected Analyzer defaultAnalyzer;

  protected Map<Class, IndexWriter> indexWriters = new HashMap<>();

  protected Map<Class, DirectoryReader> directoryReaders = new HashMap<>();
  protected Map<Class, IndexSearcher> indexSearchers = new HashMap<>();

  protected boolean isIndexReady = false;

  protected int indexUpdatedEntitiesAfterMilliseconds = 1000;
  protected Queue<UserDataEntity> updatedEntitiesToIndex = new ConcurrentLinkedQueue<>();
  protected Timer indexUpdatedEntitiesTimer = null;


  public LuceneSearchEngine() {
    SeriesTitleReferenceBaseTypeIntRef = getByteRefFromInteger(SeriesTitleReferenceBaseType);
    ReferenceReferenceBaseTypeIntRef = getByteRefFromInteger(ReferenceReferenceBaseType);
    ReferenceSubDivisionReferenceBaseTypeIntRef = getByteRefFromInteger(ReferenceSubDivisionReferenceBaseType);
    
    if(deepThought != null)
      deepThoughtChanged(null, deepThought);
  }

  public LuceneSearchEngine(Directory directory) {
    this();
    setDirectory(directory);
  }

  @Override
  protected void deepThoughtChanged(DeepThought previousDeepThought, DeepThought newDeepThought) {
    super.deepThoughtChanged(previousDeepThought, newDeepThought);

    Application.getDataManager().addAllEntitiesListener(allEntitiesListener);

    if(previousDeepThought != null) {
      closeIndexSearchersAndWriters();
    }

    createDirectoryAndIndexSearcherAndWriterForDeepThought(newDeepThought);
  }


  public void close() {
    timer.cancel();
    timer = null;

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
    if(directories.size() > 0) // on unit tests
      return;

    try {
//   directory = FSDirectory.open(Paths.get(Application.getDataFolderPath(), "index")); // Android doesn't support java.nio package (like therefor also not class Paths)
      File deepThoughtIndexDirectory = new File(new File(Application.getDataFolderPath(), "index"), String.format("%02d", deepThought.getId()));
      boolean indexDirExists = deepThoughtIndexDirectory.exists();

      for(Class classWithOwnIndexDirectory : ClassesWithOwnIndexDirectories) {
        File indexDirectory = new File(deepThoughtIndexDirectory, getDirectoryNameForClass(classWithOwnIndexDirectory));
        directories.put(classWithOwnIndexDirectory, FSDirectory.open(indexDirectory));
      }

      File defaultIndexDirectory = new File(deepThoughtIndexDirectory, "default");
      directories.put(DefaultIndexDirectoryClass, FSDirectory.open(defaultIndexDirectory));

      isIndexReady = true;

      createIndexSearchersAndWriters();

      if(indexDirExists == false)
        rebuildIndex(); // do not rebuild index asynchronously as Application depends on some functions of SearchEngine (like Entries without Tags)
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

    createIndexSearcherOnOpeningDirectory(entityClass, indexWriter);
  }

  /**
   * <p>
   *   On opening an index directory there are no new changes yet
   *   so on first call call this simple method to create an IndexSearcher.
   * </p>
   * @return
   * @param entityClass
   * @param indexWriter
   */
  protected void createIndexSearcherOnOpeningDirectory(Class entityClass, IndexWriter indexWriter) {
    try {
      DirectoryReader directoryReader = DirectoryReader.open(indexWriter, true);
      directoryReaders.put(entityClass, directoryReader);

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
    try {
      IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_47, defaultAnalyzer);
      config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
      return new IndexWriter(directory, config);
    } catch (Exception ex) {
      log.error("Could not create IndexWriter for DeepThought " + deepThought + " (directory = " + directory + ")", ex);
    }

    isIndexReady = false;
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
        DirectoryReader directoryReader = directoryReaders.get(entityClass);
        DirectoryReader newDirectoryReader = DirectoryReader.openIfChanged(directoryReader, getIndexWriter(entityClass), true);
        if(newDirectoryReader != null) {
          directoryReaders.put(entityClass, newDirectoryReader);
          directoryReader = newDirectoryReader;
        }

        indexSearcher = new IndexSearcher(directoryReader);
        indexSearchers.put(entityClass, indexSearcher);
      } catch(Exception ex) {
        log.error("Could not create IndexSearcher", ex);
      }
    }

    return indexSearcher;
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
    if(isIndexReady == false)
      return;

    deleteIndex();

    log.debug("Going to rebuild Lucene index ...");

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

      for(IndexWriter indexWriter : indexWriters.values())
        indexWriter.commit();
      log.debug("Done rebuilding Lucene Index.");
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
    log.debug("Going to delete Lucene Index ...");
    try {
      for(IndexWriter indexWriter : indexWriters.values()) {
        indexWriter.deleteAll();
        indexWriter.prepareCommit();
        indexWriter.commit();
      }
      log.debug("Lucene Index successfully deleted");
    } catch(Exception ex) {
      log.error("Could not delete Lucene index", ex);
    }

    markIndexHasBeenUpdated();
  }

  protected Set<Entry> entriesToIndex = new CopyOnWriteArraySet<>();
  protected Timer timer = new Timer("IndexEntryTimer");


  public void indexEntity(UserDataEntity entity) {
    if(entity.isPersisted() == false)
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

    doc.add(new LongField(FieldName.EntryId, entry.getId(), Field.Store.YES));

    doc.add(new Field(FieldName.EntryAbstract, entry.getAbstractAsPlainText(), TextField.TYPE_NOT_STORED));
    doc.add(new Field(FieldName.EntryContent, entry.getContentAsPlainText(), TextField.TYPE_NOT_STORED));

    if(entry.hasTags()) {
      for (Tag tag : entry.getTags()) {
        doc.add(new LongField(FieldName.EntryTagsIds, tag.getId(), Field.Store.YES));
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

    doc.add(new LongField(FieldName.TagId, tag.getId(), Field.Store.YES));
    doc.add(new StringField(FieldName.TagName, tag.getName().toLowerCase(), Field.Store.NO)); // for an not analyzed String it's important to index it lower case as only than lower case search finds ti

    indexDocument(doc, Tag.class);
  }

  protected void indexCategory(Category category) {
    if(category.getParentCategory() == null) // TopLevelCategory
      return;

    Document doc = new Document();

    doc.add(new LongField(FieldName.CategoryId, category.getId(), Field.Store.YES));
    doc.add(new StringField(FieldName.CategoryName, category.getName().toLowerCase(), Field.Store.NO));

    indexDocument(doc, Category.class);
  }

  protected void indexPerson(Person person) {
    Document doc = new Document();

    doc.add(new LongField(FieldName.PersonId, person.getId(), Field.Store.YES));
    doc.add(new StringField(FieldName.PersonFirstName, person.getFirstName().toLowerCase(), Field.Store.NO));
    doc.add(new StringField(FieldName.PersonLastName, person.getLastName().toLowerCase(), Field.Store.NO));

    indexDocument(doc, Person.class);
  }

  protected void indexSeriesTitle(SeriesTitle seriesTitle) {
    Document doc = new Document();

    doc.add(new LongField(FieldName.ReferenceBaseId, seriesTitle.getId(), Field.Store.YES));
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

    doc.add(new LongField(FieldName.ReferenceBaseId, reference.getId(), Field.Store.YES));
    doc.add(new IntField(FieldName.ReferenceBaseType, ReferenceReferenceBaseType, Field.Store.NO));

    addReferenceFields(reference, doc);

    indexDocument(doc, ReferenceBase.class);
  }

  protected void addReferenceFields(Reference reference, Document doc) {
//    doc.add(new StringField(FieldName.ReferenceTitle, getReferenceIndexTerm(reference), Field.Store.NO));
    doc.add(new StringField(FieldName.ReferenceTitle, getReferenceBaseTitleIndexTerm(reference), Field.Store.NO));

    if(reference.getIssueOrPublishingDate() != null)
      doc.add(new StringField(FieldName.ReferenceIssueOrPublishingDate, reference.getIssueOrPublishingDate(), Field.Store.NO));

    if(reference.getPublishingDate() != null)
      doc.add(new LongField(FieldName.ReferencePublishingDate, reference.getPublishingDate().getTime(), Field.Store.NO));

    if(reference.getSeries() != null)
      addSeriesTitleFields(reference.getSeries(), doc);
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

    doc.add(new LongField(FieldName.ReferenceBaseId, referenceSubDivision.getId(), Field.Store.YES));
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

    doc.add(new LongField(FieldName.NoteId, note.getId(), Field.Store.YES));
    doc.add(new StringField(FieldName.NoteNote, note.getNote().toLowerCase(), Field.Store.NO));

    indexDocument(doc, Note.class);
  }

  protected void indexFile(FileLink file) {
    Document doc = new Document();

    doc.add(new LongField(FieldName.FileId, file.getId(), Field.Store.YES));
    doc.add(new StringField(FieldName.FileName, file.getName().toLowerCase(), Field.Store.NO));
    doc.add(new StringField(FieldName.FileUri, file.getUriString().toLowerCase(), Field.Store.NO));
    doc.add(new StringField(FieldName.FileSourceUri, file.getSourceUriString().toLowerCase(), Field.Store.NO));
    doc.add(new StringField(FieldName.FileDescription, file.getNotes().toLowerCase(), Field.Store.NO));

    indexDocument(doc, FileLink.class);
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
      log.debug("Indexing document {}", doc);
      IndexWriter indexWriter = getIndexWriter(entityClass);
      indexWriter.addDocument(doc);
      indexWriter.commit();
    } catch(Exception ex) {
      log.error("Could not index Document " + doc, ex);
    }

    markIndexHasBeenUpdated(entityClass); // so that on next search updates are reflected
  }


  /*        Search          */

  @Override
  public void getEntriesWithoutTags(final SearchCompletedListener<Collection<Entry>> listener) {
      final Query query = new TermQuery(new Term(FieldName.EntryNoTags, NoTagsFieldValue));

    Application.getThreadPool().runTaskAsync(new Runnable() {
      @Override
      public void run() {
        try {
          listener.completed(new LazyLoadingLuceneSearchResultsList<Entry>(getIndexSearcher(Entry.class), query, Entry.class, FieldName.EntryId, 100000, SortOrder.Descending, FieldName.EntryId));
        } catch (Exception ex) {
          log.error("Could not search for Entries without Tags", ex);
        }
      }
    });
  }

  protected void filterTags(FilterTagsSearch search, String[] tagNamesToFilterFor) {
    for(String tagNameToFilterFor : tagNamesToFilterFor) {
      if(search.isInterrupted())
        return;
      try {
        String searchTerm = "*" + QueryParser.escape(tagNameToFilterFor) + "*";
        Query query = new WildcardQuery(new Term(FieldName.TagName, searchTerm));
        if(search.isInterrupted())
          return;

        search.addResult(new FilterTagsSearchResult(tagNameToFilterFor, new LazyLoadingLuceneSearchResultsList(getIndexSearcher(Tag.class), query, Tag.class, FieldName.TagId, 10000)));
      } catch(Exception ex) {
        log.error("Could not parse query " + tagNamesToFilterFor, ex);
        // TODO: set error flag in search
      }
    }

    search.fireSearchCompleted();
  }

  protected void findAllEntriesHavingTheseTagsAsync(Collection<Tag> tagsToFilterFor, SearchCompletedListener<net.deepthought.data.search.specific.FindAllEntriesHavingTheseTagsResult> listener) {
    Collection<Entry> entriesHavingFilteredTags = new LazyLoadingList<Entry>(Entry.class);
    Set<Tag> tagsOnEntriesContainingFilteredTags = new HashSet<>();

    BooleanQuery query = new BooleanQuery();
    for(Tag tag : tagsToFilterFor) {
      query.add(new BooleanClause(new TermQuery(new Term(FieldName.EntryTagsIds, getByteRefFromLong(tag.getId()))), BooleanClause.Occur.MUST));
    }

    try {
      IndexSearcher searcher = getIndexSearcher(Entry.class);

      ScoreDoc[] hits = searcher.search(query, 10000).scoreDocs;
      Set<Long> ids = new HashSet<>();

      // Iterate through the results:
      for (int i = 0; i < hits.length; i++) {
        try {
          Document hitDoc = searcher.doc(hits[i].doc);
//          Entry resultEntry = (Entry)getEntityFromDocument(hitDoc, Entry.class, FieldName.EntryId);
//          entriesHavingFilteredTags.add(resultEntry);
//          tagsOnEntriesContainingFilteredTags.addAll(resultEntry.getTags());
          ids.add(hitDoc.getField(FieldName.EntryId).numericValue().longValue());
        } catch(Exception ex) { log.error("Could not extract result from hitDoc", ex); }
      }

      entriesHavingFilteredTags.addAll(getBaseEntitiesFromIds(Entry.class, ids));
      for(Entry resultEntry : entriesHavingFilteredTags)
        tagsOnEntriesContainingFilteredTags.addAll(resultEntry.getTags());
    } catch(Exception ex) {
      log.error("Could not execute Query " + query.toString(), ex);
    }

    listener.completed(new FindAllEntriesHavingTheseTagsResult(entriesHavingFilteredTags, tagsOnEntriesContainingFilteredTags));
  }

  @Override
  protected void filterEntries(FilterEntriesSearch search, String[] termsToFilterFor) {
    // TODO: i think it's better to analyze content- and abstractFilter as they are being used on analyzed fields
//    Analyzer analyzer = getAnalyzerForTextLanguage(search.getSearchTerm());
    BooleanQuery query = new BooleanQuery();

    if(search.filterOnlyEntriesWithoutTags())
      query.add(new TermQuery(new Term(FieldName.EntryNoTags, NoTagsFieldValue)), BooleanClause.Occur.MUST);
    else if(search.getEntriesMustHaveTheseTags().size() > 0) {
      BooleanQuery filterEntriesQuery = new BooleanQuery();
      for (Tag tag : search.getEntriesMustHaveTheseTags())
        filterEntriesQuery.add(new TermQuery(new Term(FieldName.EntryTagsIds, getByteRefFromLong(tag.getId()))), BooleanClause.Occur.MUST);
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

    executeQuery(search, query, Entry.class, FieldName.EntryId, SortOrder.Descending, FieldName.EntryId);
  }

  @Override
  protected void filterPersons(Search<Person> search, String personFilter) {
    BooleanQuery query = new BooleanQuery();
    personFilter = QueryParser.escape(personFilter);

    query.add(new WildcardQuery(new Term(FieldName.PersonFirstName, "*" + personFilter + "*")), BooleanClause.Occur.SHOULD);
    query.add(new WildcardQuery(new Term(FieldName.PersonLastName, "*" + personFilter + "*")), BooleanClause.Occur.SHOULD);

    executeQuery(search, query, Person.class, FieldName.PersonId, SortOrder.Ascending, FieldName.PersonLastName, FieldName.PersonFirstName);
  }

  @Override
  protected void filterPersons(Search<Person> search, String lastNameFilter, String firstNameFilter) {
    lastNameFilter = QueryParser.escape(lastNameFilter);
    firstNameFilter = QueryParser.escape(firstNameFilter);

    BooleanQuery query = new BooleanQuery();

    query.add(new WildcardQuery(new Term(FieldName.PersonLastName, "*" + lastNameFilter + "*")), BooleanClause.Occur.MUST);
    query.add(new WildcardQuery(new Term(FieldName.PersonFirstName, "*" + firstNameFilter + "*")), BooleanClause.Occur.MUST);

    executeQuery(search, query, Person.class, FieldName.PersonId, SortOrder.Ascending, FieldName.PersonLastName, FieldName.PersonFirstName);
  }

  @Override
  protected void filterAllReferenceBaseTypesForSameFilter(FilterReferenceBasesSearch search, String referenceBaseFilter) {
    BooleanQuery query = new BooleanQuery();

    referenceBaseFilter = QueryParser.escape(referenceBaseFilter);
    referenceBaseFilter = "*" + referenceBaseFilter + "*";

    query.add(new WildcardQuery(new Term(FieldName.SeriesTitleTitle, referenceBaseFilter)), BooleanClause.Occur.SHOULD);

    if(search.isInterrupted())
      return;

    query.add(new WildcardQuery(new Term(FieldName.ReferenceTitle, referenceBaseFilter)), BooleanClause.Occur.SHOULD);
    query.add(new WildcardQuery(new Term(FieldName.ReferenceIssueOrPublishingDate, referenceBaseFilter)), BooleanClause.Occur.SHOULD);

    if(search.isInterrupted())
      return;

    query.add(new WildcardQuery(new Term(FieldName.ReferenceSubDivisionTitle, referenceBaseFilter)), BooleanClause.Occur.SHOULD);

    executeReferenceBaseQuery(search, query);
  }

  @Override
  protected void filterEachReferenceBaseWithSeparateFilter(FilterReferenceBasesSearch search, String seriesTitleFilter, String referenceFilter, String referenceSubDivisionFilter) {
    BooleanQuery query = new BooleanQuery();

    if(seriesTitleFilter != null) {
      seriesTitleFilter = "*" + QueryParser.escape(seriesTitleFilter) + "*";
      BooleanQuery seriesTitleQuery = new BooleanQuery();

      seriesTitleQuery.add(new TermQuery(new Term(FieldName.ReferenceBaseType, SeriesTitleReferenceBaseTypeIntRef)), BooleanClause.Occur.MUST);
      seriesTitleQuery.add(new WildcardQuery(new Term(FieldName.SeriesTitleTitle, seriesTitleFilter)), BooleanClause.Occur.MUST);

      query.add(seriesTitleQuery, BooleanClause.Occur.MUST);
    }

    if(search.isInterrupted())
      return;

    if(referenceFilter != null) {
      referenceFilter = "*" + QueryParser.escape(referenceFilter) + "*";
      BooleanQuery referenceQuery = new BooleanQuery();
      referenceQuery.add(new TermQuery(new Term(FieldName.ReferenceBaseType, ReferenceReferenceBaseTypeIntRef)), BooleanClause.Occur.MUST);

      BooleanQuery referenceValuesQuery = new BooleanQuery();
      referenceValuesQuery.add(new WildcardQuery(new Term(FieldName.SeriesTitleTitle, referenceFilter)), BooleanClause.Occur.SHOULD);
      referenceValuesQuery.add(new WildcardQuery(new Term(FieldName.ReferenceTitle, referenceFilter)), BooleanClause.Occur.SHOULD);
      referenceValuesQuery.add(new WildcardQuery(new Term(FieldName.ReferenceIssueOrPublishingDate, referenceFilter)), BooleanClause.Occur.SHOULD);
      referenceQuery.add(referenceValuesQuery, BooleanClause.Occur.MUST);

      query.add(referenceQuery, BooleanClause.Occur.MUST);
    }

    if(search.isInterrupted())
      return;

    if(referenceSubDivisionFilter != null) {
      referenceSubDivisionFilter = "*" + QueryParser.escape(referenceSubDivisionFilter) + "*";
      BooleanQuery subDivisionQuery = new BooleanQuery();
      subDivisionQuery.add(new TermQuery(new Term(FieldName.ReferenceBaseType, ReferenceSubDivisionReferenceBaseTypeIntRef)), BooleanClause.Occur.MUST);

      BooleanQuery subDivisionValuesQuery = new BooleanQuery();
      subDivisionValuesQuery.add(new WildcardQuery(new Term(FieldName.SeriesTitleTitle, referenceSubDivisionFilter)), BooleanClause.Occur.SHOULD);
      subDivisionValuesQuery.add(new WildcardQuery(new Term(FieldName.ReferenceTitle, referenceSubDivisionFilter)), BooleanClause.Occur.SHOULD);
      subDivisionValuesQuery.add(new WildcardQuery(new Term(FieldName.ReferenceIssueOrPublishingDate, referenceSubDivisionFilter)), BooleanClause.Occur.SHOULD);
      subDivisionValuesQuery.add(new WildcardQuery(new Term(FieldName.ReferenceSubDivisionTitle, referenceSubDivisionFilter)), BooleanClause.Occur.SHOULD);
      subDivisionQuery.add(subDivisionValuesQuery, BooleanClause.Occur.MUST);

      query.add(subDivisionQuery, BooleanClause.Occur.MUST);
    }

    executeReferenceBaseQuery(search, query);
  }

  protected void executeReferenceBaseQuery(FilterReferenceBasesSearch search, Query query) {
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

  protected <T extends BaseEntity> List<T> getBaseEntitiesFromIds(Class<T> type, Collection<Long> searchResultIds) {
    return Application.getEntityManager().getEntitiesById(type, searchResultIds);
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
        checkIfEntityIsOnEntry((UserDataEntity)entity);
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
        if (isIndexedEntityOnEntry(addedEntity))
          updateIndexForEntity((UserDataEntity)collectionHolder);
      }
    }

    @Override
    public void entityRemovedFromCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity removedEntity) {
      if(collectionHolder instanceof Entry) {
        if(isIndexedEntityOnEntry(removedEntity))
          updateIndexForEntity((UserDataEntity) collectionHolder);
      }
    }
  };

  protected boolean isIndexedEntityOnEntry(BaseEntity entity) {
    return entity instanceof Tag || entity instanceof Category || entity instanceof Person || entity instanceof EntryPersonAssociation || entity instanceof Note ||
        entity instanceof SeriesTitle || entity instanceof Reference || entity instanceof ReferenceSubDivision;
  }


  protected void updateIndexForEntity(UserDataEntity updatedEntity) {
    if(indexUpdatedEntitiesAfterMilliseconds == 0)
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
    log.debug("Removing Entity {} from index", removedEntity);
    updatedEntitiesToIndex.remove(removedEntity);

    String idFieldName = getIdFieldNameForEntity(removedEntity);
    IndexWriter indexWriter = getIndexWriter(removedEntity.getClass());
    try {
      if(idFieldName != null)
        indexWriter.deleteDocuments(new Term(idFieldName, getByteRefFromLong(removedEntity.getId())));
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
    else if(entity instanceof Category) // TODO: will they ever be index?
      return FieldName.CategoryId;
    else if(entity instanceof Person)
      return FieldName.PersonId;
    else if(entity instanceof ReferenceBase)
      return FieldName.ReferenceBaseId;
    else if(entity instanceof Note)
      return FieldName.NoteId;

    return null;
  }

  protected void checkIfEntityIsOnEntry(UserDataEntity updatedEntity) {
    if(updatedEntity instanceof Tag) {
      for(Entry entry : ((Tag)updatedEntity).getEntries())
        updateIndexForEntity(entry);
    }
    else if(updatedEntity instanceof Category) {
      for(Entry entry : ((Category)updatedEntity).getEntries())
        updateIndexForEntity(entry);
    }
    else if(updatedEntity instanceof Person) {
      for(Entry entry : ((Person)updatedEntity).getAssociatedEntries())
        updateIndexForEntity(entry);
    }
    else if(updatedEntity instanceof Note) {
      updateIndexForEntity(((Note)updatedEntity).getEntry());
    }
    else if(updatedEntity instanceof SeriesTitle) {
      for(Entry entry : ((SeriesTitle)updatedEntity).getEntries())
        updateIndexForEntity(entry);
    }
    else if(updatedEntity instanceof Reference) {
      for(Entry entry : ((Reference)updatedEntity).getEntries())
        updateIndexForEntity(entry);
    }
    else if(updatedEntity instanceof ReferenceSubDivision) {
      for(Entry entry : ((ReferenceSubDivision)updatedEntity).getEntries())
        updateIndexForEntity(entry);
    }
  }


  public int getIndexUpdatedEntitiesAfterMilliseconds() {
    return indexUpdatedEntitiesAfterMilliseconds;
  }

  public void setIndexUpdatedEntitiesAfterMilliseconds(int indexUpdatedEntitiesAfterMilliseconds) {
    this.indexUpdatedEntitiesAfterMilliseconds = indexUpdatedEntitiesAfterMilliseconds;
  }

}
