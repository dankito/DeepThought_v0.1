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
import net.deepthought.data.persistence.CombinedLazyLoadingList;
import net.deepthought.data.persistence.LazyLoadingList;
import net.deepthought.data.persistence.db.BaseEntity;
import net.deepthought.data.persistence.db.UserDataEntity;
import net.deepthought.data.search.results.LazyLoadingLuceneSearchResultsList;
import net.deepthought.data.search.results.LazyLoadingReferenceBasesSearchResultsList;
import net.deepthought.data.search.specific.FilterEntriesSearch;
import net.deepthought.data.search.specific.FilterReferenceBasesSearch;
import net.deepthought.data.search.specific.FilterTagsSearch;
import net.deepthought.data.search.specific.FilterTagsSearchResult;
import net.deepthought.data.search.specific.FindAllEntriesHavingTheseTagsResult;
import net.deepthought.util.StringUtils;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongField;
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

  public static final String NoTagsFieldValue = "notags";
  public static final String NoCategoriesFieldValue = "nocategories";
  public static final String NoPersonsFieldValue = "nopersons";
  public static final String NoNotesFieldValue = "nonotes";
  public static final String NoSeriesFieldValue = "noseries";
  public static final String NoReferenceFieldValue = "noreference";
  public static final String NoReferenceSubDivisionFieldValue = "noreferencesubdivision";


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
        doc.add(new Field(FieldName.EntryTags, tag.getName(), TextField.TYPE_NOT_STORED));
        doc.add(new LongField(FieldName.EntryTagsIds, tag.getId(), Field.Store.YES));
      }
    }
    else
      doc.add(new Field(FieldName.EntryNoTags, NoTagsFieldValue, TextField.TYPE_NOT_STORED)); // TODO: isn't it better to just store it? As in this way value gets analyzed

    if(entry.hasCategories()) {
      for (Category category : entry.getCategories())
        doc.add(new Field(FieldName.EntryCategories, category.getName(), TextField.TYPE_NOT_STORED));
    }
    else
      doc.add(new Field(FieldName.EntryNoCategories, NoCategoriesFieldValue, TextField.TYPE_NOT_STORED));

    if(entry.hasPersons()) {
      for (Person person : entry.getPersons())
        doc.add(new Field(FieldName.EntryPersons, person.getFirstName() + " " + person.getLastName(), TextField.TYPE_NOT_STORED));
    }
    else
      doc.add(new Field(FieldName.EntryNoPersons, NoPersonsFieldValue, TextField.TYPE_NOT_STORED));

    if(entry.hasNotes()) {
      for (Note note : entry.getNotes())
        doc.add(new Field(FieldName.EntryNotes, note.getNote(), TextField.TYPE_NOT_STORED));
    }
    else
      doc.add(new Field(FieldName.EntryNoNotes, NoNotesFieldValue, TextField.TYPE_NOT_STORED));

    try {
      if (entry.getReferenceSubDivision() != null)
        doc.add(new Field(FieldName.EntryReferenceSubDivision, getReferenceSubDivisionTitleValue(entry.getReferenceSubDivision()), TextField.TYPE_NOT_STORED));
      else
        doc.add(new Field(FieldName.EntryNoReferenceSubDivision, NoReferenceSubDivisionFieldValue, TextField.TYPE_NOT_STORED));

      if (entry.getReference() != null && StringUtils.isNotNullOrEmpty(entry.getReference().getTextRepresentation()))
        doc.add(new Field(FieldName.EntryReference, getReferenceTitleValue(entry.getReference()), TextField.TYPE_NOT_STORED));
      else
        doc.add(new Field(FieldName.EntryNoReference, NoReferenceFieldValue, TextField.TYPE_NOT_STORED));

      if (entry.getSeries() != null)
        doc.add(new Field(FieldName.EntrySeries, getSeriesTitleTitleValue(entry.getSeries()), TextField.TYPE_NOT_STORED));
      else
        doc.add(new Field(FieldName.EntryNoSeries, NoSeriesFieldValue, TextField.TYPE_NOT_STORED));
    } catch(Exception ex) {
      log.error("Could not index Reference of Entry " + entry, ex);
    }

    return doc;
  }

  protected void indexTag(Tag tag) {
    Document doc = new Document();

    doc.add(new LongField(FieldName.TagId, tag.getId(), Field.Store.YES));
    doc.add(new Field(FieldName.TagName, tag.getName(), TextField.TYPE_NOT_STORED));

    indexDocument(doc, Tag.class);
  }

  protected void indexCategory(Category category) {
    if(category.getParentCategory() == null) // TopLevelCategory
      return;

    Document doc = new Document();

    doc.add(new LongField(FieldName.CategoryId, category.getId(), Field.Store.YES));
    doc.add(new Field(FieldName.CategoryName, category.getName(), TextField.TYPE_NOT_STORED));

    indexDocument(doc, Category.class);
  }

  protected void indexPerson(Person person) {
    Document doc = new Document();

    doc.add(new LongField(FieldName.PersonId, person.getId(), Field.Store.YES));
    doc.add(new Field(FieldName.PersonFirstName, person.getFirstName().toLowerCase(), TextField.TYPE_NOT_STORED));
    doc.add(new Field(FieldName.PersonLastName, person.getLastName().toLowerCase(), TextField.TYPE_NOT_STORED));

    indexDocument(doc, Person.class);
  }

  protected void indexSeriesTitle(SeriesTitle seriesTitle) {
    Document doc = new Document();

    doc.add(new LongField(FieldName.ReferenceBaseId, seriesTitle.getId(), Field.Store.YES));
//    doc.add(new Field(FieldName.SeriesTitleTitle, getSeriesTitleTitleValue(seriesTitle), TextField.TYPE_NOT_STORED));
    doc.add(new Field(FieldName.SeriesTitleTitle, getSeriesTitleTitleValue(seriesTitle), TextField.TYPE_STORED));

//    doc.add(new Field(FieldName.ReferenceTitle, NoReferenceFieldValue, TextField.TYPE_NOT_STORED));
//    doc.add(new Field(FieldName.ReferenceSubDivisionTitle, NoReferenceFieldValue, TextField.TYPE_NOT_STORED));

    indexDocument(doc, ReferenceBase.class);
  }

  protected String getSeriesTitleTitleValue(SeriesTitle seriesTitle) {
    return seriesTitle.getTitle() + " " + seriesTitle.getSubTitle();
  }

  protected void indexReference(Reference reference) {
    Document doc = new Document();

    doc.add(new LongField(FieldName.ReferenceBaseId, reference.getId(), Field.Store.YES));
//    doc.add(new Field(FieldName.ReferenceTitle, getReferenceTitleValue(reference), TextField.TYPE_NOT_STORED));
    doc.add(new Field(FieldName.ReferenceTitle, reference.getPreview() + " " + reference.getIssueOrPublishingDate() + " " + reference.getSubTitle(), TextField.TYPE_STORED));
    if(reference.getPublishingDate() != null)
      addDateFieldToDocument(doc, FieldName.ReferencePublishingDate, reference.getPublishingDate());

//    if(reference.getSeries() != null)
////      doc.add(new Field(FieldName.ReferenceSeriesTitle, getSeriesTitleTitleValue(reference.getSeries()), TextField.TYPE_NOT_STORED));
//      doc.add(new Field(FieldName.SeriesTitleTitle, getSeriesTitleTitleValue(reference.getSeries()), TextField.TYPE_NOT_STORED));
//    else
//      doc.add(new Field(FieldName.SeriesTitleTitle, NoReferenceFieldValue, TextField.TYPE_NOT_STORED));
//
//    doc.add(new Field(FieldName.ReferenceSubDivisionTitle, NoReferenceFieldValue, TextField.TYPE_NOT_STORED));

    indexDocument(doc, ReferenceBase.class);
  }

  protected String getReferenceTitleValue(Reference reference) {
    return reference.getTitle() + " " + reference.getSubTitle() + " " + reference.getIssueOrPublishingDate();
  }

  protected void indexReferenceSubDivision(ReferenceSubDivision referenceSubDivision) {
    Document doc = new Document();

    doc.add(new LongField(FieldName.ReferenceBaseId, referenceSubDivision.getId(), Field.Store.YES));
//    doc.add(new Field(FieldName.ReferenceSubDivisionTitle, getReferenceSubDivisionTitleValue(referenceSubDivision), TextField.TYPE_NOT_STORED));
    doc.add(new Field(FieldName.ReferenceSubDivisionTitle, referenceSubDivision.getTextRepresentation() + " " + referenceSubDivision.getSubTitle(), TextField.TYPE_STORED));

//    if(referenceSubDivision.getReference() != null) {
//      Reference reference = referenceSubDivision.getReference();
////      doc.add(new Field(FieldName.ReferenceSubDivisionReference, getReferenceTitleValue(reference), TextField.TYPE_NOT_STORED));
//      doc.add(new Field(FieldName.ReferenceTitle, getReferenceTitleValue(reference), TextField.TYPE_NOT_STORED));
//
//      if (reference.getSeries() != null)
////        doc.add(new Field(FieldName.ReferenceSubDivisionSeriesTitle, getSeriesTitleTitleValue(reference.getSeries()), TextField.TYPE_NOT_STORED));
//        doc.add(new Field(FieldName.SeriesTitleTitle, getSeriesTitleTitleValue(reference.getSeries()), TextField.TYPE_NOT_STORED));
//      else
//        doc.add(new Field(FieldName.ReferenceSubDivisionTitle, NoReferenceFieldValue, TextField.TYPE_NOT_STORED));
//    }
//    else
//      doc.add(new Field(FieldName.ReferenceTitle, NoReferenceFieldValue, TextField.TYPE_NOT_STORED));

    indexDocument(doc, ReferenceBase.class);
  }

  protected String getReferenceSubDivisionTitleValue(ReferenceSubDivision referenceSubDivision) {
    return referenceSubDivision.getTitle() + " " + referenceSubDivision.getSubTitle();
  }

  protected void indexNote(Note note) {
    Document doc = new Document();

    doc.add(new LongField(FieldName.NoteId, note.getId(), Field.Store.YES));
    doc.add(new Field(FieldName.NoteNote, note.getNote(), TextField.TYPE_NOT_STORED));

    indexDocument(doc, Note.class);
  }

  protected void indexFile(FileLink file) {
    Document doc = new Document();

    doc.add(new LongField(FieldName.FileId, file.getId(), Field.Store.YES));
    doc.add(new Field(FieldName.FileName, file.getName(), TextField.TYPE_NOT_STORED));
    doc.add(new Field(FieldName.FileUri, file.getUriString(), TextField.TYPE_NOT_STORED));
    doc.add(new Field(FieldName.FileSourceUri, file.getSourceUriString(), TextField.TYPE_NOT_STORED));
    doc.add(new Field(FieldName.FileDescription, file.getNotes(), TextField.TYPE_NOT_STORED));

    indexDocument(doc, FileLink.class);
  }

  protected void addDateFieldToDocument(Document doc, String fieldName, Date date) {
    addDateFieldToDocument(doc, fieldName, date, DateTools.Resolution.MINUTE);
  }

  protected void addDateFieldToDocument(Document doc, String fieldName, Date date, DateTools.Resolution resolution) {
    doc.add(new Field(fieldName,
        DateTools.timeToString(date.getTime(), resolution),
        Field.Store.YES, Field.Index.NOT_ANALYZED));
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
          listener.completed(new LazyLoadingLuceneSearchResultsList<Entry>(getIndexSearcher(Entry.class), query, Entry.class, FieldName.EntryId, 100000, SortOrder.Descending));
        } catch(Exception ex) {
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
        Query query = new WildcardQuery(new Term(FieldName.TagName, "*" + tagNameToFilterFor + "*"));
        if(search.isInterrupted())
          return;

        search.addResult(new FilterTagsSearchResult(tagNameToFilterFor, new LazyLoadingLuceneSearchResultsList(getIndexSearcher(Tag.class), query, Tag.class, FieldName.TagId, 1000)));
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

    executeQuery(search, query, Entry.class, FieldName.EntryId, SortOrder.Descending);
  }

  @Override
  protected void filterPersons(Search<Person> search, String personFilter) {
    BooleanQuery query = new BooleanQuery();
    personFilter = QueryParser.escape(personFilter);

//    query.add(new PrefixQuery(new Term(FieldName.PersonFirstName, personFilter)), BooleanClause.Occur.SHOULD);
    query.add(new WildcardQuery(new Term(FieldName.PersonFirstName, "*" + personFilter + "*")), BooleanClause.Occur.SHOULD);
//    query.add(new PrefixQuery(new Term(FieldName.PersonLastName, personFilter)), BooleanClause.Occur.SHOULD);
    query.add(new WildcardQuery(new Term(FieldName.PersonLastName, "*" + personFilter + "*")), BooleanClause.Occur.SHOULD);

    executeQuery(search, query, Person.class, FieldName.PersonId);
  }

  @Override
  protected void filterPersons(Search<Person> search, String lastNameFilter, String firstNameFilter) {
    BooleanQuery query = new BooleanQuery();

    try {
//        QueryParser parser = new QueryParser(FieldName.EntryContent, analyzer);
//        query.add(parser.parse(firstNameFilter), BooleanClause.Occur.SHOULD);
//      query.add(new PrefixQuery(new Term(FieldName.PersonFirstName, firstNameFilter)), BooleanClause.Occur.MUST);
      firstNameFilter = QueryParser.escape(firstNameFilter);
      query.add(new WildcardQuery(new Term(FieldName.PersonFirstName, "*" + firstNameFilter + "*")), BooleanClause.Occur.MUST);
//      query.add(new TermQuery(new Term(FieldName.PersonFirstName, firstNameFilter)), BooleanClause.Occur.MUST);
    } catch(Exception ex) {
      log.error("Could not parse query " + firstNameFilter, ex);
      // TODO: set error flag in search
      search.fireSearchCompleted();
      return;
    }
    try {
//        QueryParser parser = new QueryParser(FieldName.EntryContent, analyzer);
//        query.add(parser.parse(lastNameFilter), BooleanClause.Occur.SHOULD);
//      query.add(new PrefixQuery(new Term(FieldName.PersonLastName, lastNameFilter)), BooleanClause.Occur.MUST);
      lastNameFilter = QueryParser.escape(lastNameFilter);
      query.add(new WildcardQuery(new Term(FieldName.PersonLastName, "*" + lastNameFilter + "*")), BooleanClause.Occur.MUST);
//      query.add(new TermQuery(new Term(FieldName.PersonLastName, lastNameFilter)), BooleanClause.Occur.MUST);
    } catch(Exception ex) {
      log.error("Could not parse query " + lastNameFilter, ex);
      // TODO: set error flag in search
      search.fireSearchCompleted();
      return;
    }

    executeQuery(search, query, Person.class, FieldName.PersonId);
  }

  @Override
  protected void filterAllReferenceBaseTypesForSameFilter(FilterReferenceBasesSearch search, String referenceBaseFilter) {
    CombinedLazyLoadingList<ReferenceBase> searchResults = new CombinedLazyLoadingList<>();
    IndexSearcher searcher = getIndexSearcher(ReferenceBase.class);
    referenceBaseFilter = QueryParser.escape(referenceBaseFilter);
    referenceBaseFilter = "*" + referenceBaseFilter + "*";

//    query.add(new PrefixQuery(new Term(FieldName.SeriesTitleTitle, referenceBaseFilter)), BooleanClause.Occur.SHOULD);
    Query seriesTitleQuery = new WildcardQuery(new Term(FieldName.SeriesTitleTitle, referenceBaseFilter));
    try {
//      searchResults.addAll(new LazyLoadingLuceneSearchResultsList<ReferenceBase>(getIndexSearcher(), seriesTitleQuery, SeriesTitle.class,))
      ScoreDoc[] hits = searcher.search(seriesTitleQuery, 1000, new Sort(new SortField(FieldName.SeriesTitleTitle, SortField.Type.STRING))).scoreDocs;
      if(search.isInterrupted())
        return;
      Collection<Long> entityIds = getEntityIds(FieldName.ReferenceBaseId, hits, searcher);
      searchResults.addAll(new LazyLoadingList<ReferenceBase>(ReferenceBase.class, entityIds));
    } catch(Exception ex) {
      log.error("Could not search SeriesTitles for " + search, ex);
    }

    if(search.isInterrupted())
      return;

//    query.add(new PrefixQuery(new Term(FieldName.ReferenceTitle, referenceBaseFilter)), BooleanClause.Occur.SHOULD);
    Query referencesQuery = new WildcardQuery(new Term(FieldName.ReferenceTitle, referenceBaseFilter));
    try {
      ScoreDoc[] hits = searcher.search(referencesQuery, 1000,
          new Sort(new SortField(FieldName.ReferencePublishingDate, SortField.Type.STRING), new SortField(FieldName.ReferenceTitle, SortField.Type.STRING))).scoreDocs;
      if(search.isInterrupted())
        return;
      Collection<Long> entityIds = getEntityIds(FieldName.ReferenceBaseId, hits, searcher);
      searchResults.addAll(new LazyLoadingList<ReferenceBase>(ReferenceBase.class, entityIds));
    } catch(Exception ex) {
      log.error("Could not search SeriesTitles for " + search, ex);
    }

    if(search.isInterrupted())
      return;

//    query.add(new PrefixQuery(new Term(FieldName.ReferenceSubDivisionTitle, referenceBaseFilter)), BooleanClause.Occur.SHOULD);
    Query referenceSubDivisionsQuery = new WildcardQuery(new Term(FieldName.ReferenceSubDivisionTitle, referenceBaseFilter));
    try {
      ScoreDoc[] hits = searcher.search(referenceSubDivisionsQuery, 1000, new Sort(new SortField(FieldName.ReferenceSubDivisionTitle, SortField.Type.STRING))).scoreDocs;
      if(search.isInterrupted())
        return;
      Collection<Long> entityIds = getEntityIds(FieldName.ReferenceBaseId, hits, searcher);
      searchResults.addAll(new LazyLoadingList<ReferenceBase>(ReferenceBase.class, entityIds));
    } catch(Exception ex) {
      log.error("Could not search SeriesTitles for " + search, ex);
    }

    search.setResults(searchResults);
    search.fireSearchCompleted();
  }

  protected Collection<Long> getEntityIds(String idFieldName, ScoreDoc[] hits, IndexSearcher searcher) {
    Set<Long> ids = new HashSet<>();

    try {
      for (int index = 0; index < hits.length; index++) {
//        ids.add(getEntityIdForIndex(index));
        Document hitDoc = searcher.doc(hits[index].doc);
        ids.add(hitDoc.getField(idFieldName).numericValue().longValue());
      }
    } catch(Exception ex) {
      log.error("Could not get all Entity IDs from Lucene Search Result", ex);
    }

    return ids;
  }

//  @Override
//  protected void filterAllReferenceBaseTypesForSameFilter(Search search, String referenceBaseFilter) {
//    BooleanQuery query = new BooleanQuery();
//    referenceBaseFilter = QueryParser.escape(referenceBaseFilter);
//
////    query.add(new PrefixQuery(new Term(FieldName.SeriesTitleTitle, referenceBaseFilter)), BooleanClause.Occur.SHOULD);
//    query.add(new WildcardQuery(new Term(FieldName.SeriesTitleTitle, "*" + referenceBaseFilter + "*")), BooleanClause.Occur.SHOULD);
////    query.add(new PrefixQuery(new Term(FieldName.ReferenceTitle, referenceBaseFilter)), BooleanClause.Occur.SHOULD);
//    query.add(new WildcardQuery(new Term(FieldName.ReferenceTitle, "*" + referenceBaseFilter + "*")), BooleanClause.Occur.SHOULD);
////    query.add(new PrefixQuery(new Term(FieldName.ReferenceSubDivisionTitle, referenceBaseFilter)), BooleanClause.Occur.SHOULD);
//    query.add(new WildcardQuery(new Term(FieldName.ReferenceSubDivisionTitle, "*" + referenceBaseFilter + "*")), BooleanClause.Occur.SHOULD);
//
////    executeQuery(search, query, ReferenceBase.class, FieldName.ReferenceBaseId);
//    search.setResults(new LazyLoadingReferenceBasesSearchResultsList(getIndexSearcher(), query, 10000));
//    search.fireSearchCompleted();
//  }

  @Override
  protected void filterEachReferenceBaseWithSeparateFilter(FilterReferenceBasesSearch search, String seriesTitleFilter, String referenceFilter, String referenceSubDivisionFilter) {
    BooleanQuery query = new BooleanQuery();

    if(seriesTitleFilter != null) {
      try {
//        QueryParser parser = new QueryParser(FieldName.EntryContent, analyzer);
//        query.add(parser.parse(contentFilter), BooleanClause.Occur.SHOULD);
//        query.add(new PrefixQuery(new Term(FieldName.SeriesTitleTitle, seriesTitleFilter)), BooleanClause.Occur.MUST);
//        query.add(new WildcardQuery(new Term(FieldName.SeriesTitleTitle, "*" + seriesTitleFilter + "*")), BooleanClause.Occur.MUST);

        QueryParser parser = new QueryParser(Version.LUCENE_47, FieldName.SeriesTitleTitle, defaultAnalyzer);
        parser.setAllowLeadingWildcard(true);
        String escapedSearchText = QueryParser.escape(seriesTitleFilter);
        query.add(parser.parse("*" + escapedSearchText + "*"), BooleanClause.Occur.MUST);
      } catch(Exception ex) {
        log.error("Could not parse query " + seriesTitleFilter, ex);
        // TODO: set error flag in search
        search.fireSearchCompleted();
        return;
      }
    }
//    else
//      query.add(new TermQuery(new Term(FieldName.SeriesTitleTitle, NoReferenceFieldValue)), BooleanClause.Occur.MUST);

    if(referenceFilter != null) {
      try {
//        QueryParser parser = new QueryParser(FieldName.EntryAbstract, analyzer);
//        query.add(parser.parse(abstractFilter), BooleanClause.Occur.SHOULD);
//        query.add(new PrefixQuery(new Term(FieldName.ReferenceTitle, referenceFilter)), BooleanClause.Occur.MUST);
//        query.add(new WildcardQuery(new Term(FieldName.ReferenceTitle, "*" + referenceFilter + "*")), BooleanClause.Occur.MUST);

        QueryParser parser = new QueryParser(Version.LUCENE_47, FieldName.ReferenceTitle, defaultAnalyzer);
        parser.setAllowLeadingWildcard(true);
        String escapedSearchText = QueryParser.escape(referenceFilter);
        query.add(parser.parse("*" + escapedSearchText + "*"), BooleanClause.Occur.MUST);
      } catch(Exception ex) {
        log.error("Could not parse query " + referenceFilter, ex);
        // TODO: set error flag in search
        search.fireSearchCompleted();
        return;
      }
    }
//    else if(referenceSubDivisionFilter == null)
//      query.add(new TermQuery(new Term(FieldName.ReferenceTitle, NoReferenceFieldValue)), BooleanClause.Occur.MUST);
//      try {
//        query.add((new QueryParser(Version.LUCENE_47, FieldName.ReferenceTitle, defaultAnalyzer)).parse(NoReferenceFieldValue), BooleanClause.Occur.MUST);
//      } catch(Exception ex) { }

    if(referenceSubDivisionFilter != null) {
      try {
//        QueryParser parser = new QueryParser(FieldName.EntryAbstract, analyzer);
//        query.add(parser.parse(abstractFilter), BooleanClause.Occur.SHOULD);
//        query.add(new PrefixQuery(new Term(FieldName.ReferenceSubDivisionTitle, referenceSubDivisionFilter)), BooleanClause.Occur.MUST);
//        query.add(new WildcardQuery(new Term(FieldName.ReferenceSubDivisionTitle, "*" + referenceSubDivisionFilter + "*")), BooleanClause.Occur.MUST);

        QueryParser parser = new QueryParser(Version.LUCENE_47, FieldName.ReferenceSubDivisionTitle, defaultAnalyzer);
        parser.setAllowLeadingWildcard(true);
        String escapedSearchText = QueryParser.escape(referenceSubDivisionFilter);
        query.add(parser.parse("*" + escapedSearchText + "*"), BooleanClause.Occur.MUST);
      } catch(Exception ex) {
        log.error("Could not parse query " + referenceSubDivisionFilter, ex);
        // TODO: set error flag in search
        search.fireSearchCompleted();
        return;
      }
    }
//    else
//      query.add(new TermQuery(new Term(FieldName.ReferenceSubDivisionTitle, NoReferenceFieldValue)), BooleanClause.Occur.MUST);
//    try {
//      query.add((new QueryParser(Version.LUCENE_47, FieldName.ReferenceSubDivisionTitle, defaultAnalyzer)).parse(NoReferenceFieldValue), BooleanClause.Occur.MUST);
//    } catch(Exception ex) { }

//    executeQuery(search, query, ReferenceBase.class, FieldName.ReferenceBaseId);
    search.setResults(new LazyLoadingReferenceBasesSearchResultsList(getIndexSearcher(ReferenceBase.class), query, 10000));
    search.fireSearchCompleted();
  }

  protected void filterSeriesTitles(Search search, String seriesTitleFilter) {
//    Analyzer analyzer = getAnalyzerForTextLanguage(search.getSearchTerm());
//        QueryParser parser = new QueryParser(FieldName.SeriesTitleTitle, analyzer);
//    Query query = new PrefixQuery(new Term(FieldName.SeriesTitleTitle, seriesTitleFilter));
//    Query query = new WildcardQuery(new Term(FieldName.SeriesTitleTitle, "*" + seriesTitleFilter + "*"));

    try {
      QueryParser parser = new QueryParser(Version.LUCENE_47, FieldName.SeriesTitleTitle, defaultAnalyzer);
      parser.setAllowLeadingWildcard(true);
      String escapedSearchText = QueryParser.escape(seriesTitleFilter);
      Query query = parser.parse("*" + escapedSearchText + "*");

      executeQuery(search, query, ReferenceBase.class, FieldName.ReferenceBaseId);
    } catch(Exception ex) {
      log.error("Could not parse query " + seriesTitleFilter, ex);
      // TODO: set error flag in search
      search.fireSearchCompleted();
    }
  }

//  @Override
  protected void filterReferences(Search search, String seriesTitleFilter, String referenceFilter, String referenceSubDivisionFilter) {
    // TODO:
//    super.filterReferences(search, seriesTitleFilter, referenceFilter, referenceSubDivisionFilter);
  }

//  @Override
  protected void filterReferenceSubDivisions(Search search, Reference reference, String seriesTitleFilter, String referenceFilter, String referenceSubDivisionFilter) {
    // TODO:
//    super.filterReferenceSubDivisions(search, reference, seriesTitleFilter, referenceFilter, referenceSubDivisionFilter);
  }

//  protected void filterReferences(Search search, String seriesTitleFilter, String referenceFilter, String referenceSubDivisionFilter) {
//    for(Reference reference : Application.getDeepThought().getReferences()) {
//      if(search.isInterrupted())
//        return;
//
//      if(referenceSubDivisionFilter == null && reference.getTextRepresentation().toLowerCase().contains(referenceFilter) && // cannot fulfill all filters as ReferenceSubDivisionFilter is set and it isn't a ReferenceSubDivision
//          ((seriesTitleFilter == null && reference.getSeries() == null) ||
//              seriesTitleFilter != null && reference.getSeries() != null && reference.getSeries().getTextRepresentation().toLowerCase().contains(seriesTitleFilter)))
//        search.addResult(reference);
//
//      if(referenceSubDivisionFilter != null)
//        filterReferenceSubDivisions(search, reference, seriesTitleFilter, referenceFilter, referenceSubDivisionFilter);
//    }
//  }
//
//  protected void filterReferenceSubDivisions(Search search, Reference reference, String seriesTitleFilter, String referenceFilter, String referenceSubDivisionFilter) {
//    for(ReferenceSubDivision subDivision : reference.getSubDivisions()) {
//      if(search.isInterrupted())
//        return;
//
//      if(subDivision.getTextRepresentation().toLowerCase().contains(referenceSubDivisionFilter) &&
//          ((referenceFilter == null && subDivision.getReference() == null) ||
//              (referenceFilter != null && subDivision.getReference() != null && subDivision.getReference().getTextRepresentation().toLowerCase().contains(referenceFilter))) &&
//          ((seriesTitleFilter == null && (subDivision.getReference() == null || subDivision.getReference().getSeries() == null)) ||
//              (seriesTitleFilter != null && subDivision.getReference() != null && subDivision.getReference().getSeries() != null &&
//                  subDivision.getReference().getSeries().getTextRepresentation().toLowerCase().contains(seriesTitleFilter))))
//        search.addResult(subDivision);
//    }
//  }

  protected void executeQuery(Search search, Query query, Class<? extends BaseEntity> resultEntityClass, String idFieldName) {
    executeQuery(search, query, resultEntityClass, idFieldName, SortOrder.Unsorted);
  }

  protected void executeQuery(Search search, Query query, Class<? extends BaseEntity> resultEntityClass, String idFieldName, SortOrder sortOrder) {
    if(search.isInterrupted())
      return;
    log.debug("Executing Query " + query);

    try {
      search.setResults(new LazyLoadingLuceneSearchResultsList(getIndexSearcher(resultEntityClass), query, resultEntityClass, idFieldName, 1000, sortOrder));
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




  protected BytesRef getByteRefFromLong(Long longValue) {
//    BytesRefBuilder byteRefBuilder = new BytesRefBuilder();
//    NumericUtils.longToPrefixCoded(longValue, 0, byteRefBuilder);
//    return byteRefBuilder.toBytesRef();

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
