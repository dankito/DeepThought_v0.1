package net.deepthought.data.search;

import net.deepthought.Application;
import net.deepthought.data.model.Category;
import net.deepthought.data.model.Entry;
import net.deepthought.data.model.Person;
import net.deepthought.data.model.Reference;
import net.deepthought.data.model.ReferenceBase;
import net.deepthought.data.model.ReferenceSubDivision;
import net.deepthought.data.model.SeriesTitle;
import net.deepthought.data.model.Tag;
import net.deepthought.data.persistence.db.BaseEntity;
import net.deepthought.data.persistence.db.TableConfig;
import net.deepthought.data.persistence.db.UserDataEntity;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.de.GermanAnalyzer;
import org.apache.lucene.analysis.de.GermanNormalizationFilter;
import org.apache.lucene.analysis.de.GermanStemFilter;
import org.apache.lucene.analysis.de.GermanStemmer;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.DocsEnum;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Bits;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.BytesRefBuilder;
import org.apache.lucene.util.NumericUtils;
import org.apache.tika.language.LanguageIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.concurrent.CopyOnWriteArraySet;

public class LuceneSearchEngine extends DefaultSearchEngine {

  public static final String NoTagsFieldValue = "NOTAGS";
  public static final String NoCategoriesFieldValue = "NOCATEGORIES";
  public static final String NoReferenceFieldValue = "NOREFERENCE";


  private final static Logger log = LoggerFactory.getLogger(LuceneSearchEngine.class);


  protected Directory directory;

  protected Analyzer defaultAnalyzer;

  protected IndexWriter indexWriter;

  protected DirectoryReader directoryReader;
  protected IndexSearcher indexSearcher;


  public LuceneSearchEngine() throws IOException {
    this(FSDirectory.open(Paths.get(Application.getDataFolderPath(), "index")));
  }

  public LuceneSearchEngine(Directory directory) throws IOException {
    this.directory = directory;

    defaultAnalyzer = new DeepThoughtAnalyzer();
    indexWriter = createIndexWriter();
    indexSearcher = createIndexSearcher();
  }

  public void close() {
    timer.cancel();
    timer = null;

    closeIndexSearcher();

    try {
      indexWriter.close();
    } catch(Exception ex) {
      log.error("Could not close IndexWriter", ex);
    }
  }

  protected void closeIndexSearcher() {
    try {
      directoryReader.close();
    } catch(Exception ex) {
      log.error("Could not close DirectoryReader", ex);
    }

    indexSearcher = null;
  }

  /**
   * Know what you do when you call this method!
   * Deletes index and rebuilds it from scratch which can take a very long time if you have a big database
   */
  public void rebuildIndex() {
    deleteIndex();

    try {
      IndexWriter indexWriter = createIndexWriter();

      for (Entry entry : Application.getDeepThought().getEntries())
        indexEntry(entry);

      for (Tag tag : Application.getDeepThought().getTags())
        indexTag(tag);

      for (Category category : Application.getDeepThought().getCategories())
        indexCategory(category);

      for (Person person : Application.getDeepThought().getPersons())
        indexPerson(person);

      for (SeriesTitle seriesTitle : Application.getDeepThought().getSeriesTitles())
        indexSeriesTitle(seriesTitle);
      for (Reference reference : Application.getDeepThought().getReferences()) {
        indexReference(reference);

        for(ReferenceSubDivision subDivision : reference.getSubDivisions())
          indexReferenceSubDivision(subDivision);
      }

//      try {
//        indexWriter.prepareCommit();
//      } catch(Exception ex) {
//        log.error("Could not prepare commit on Lucene Index", ex);
//        indexWriter.rollback();
//      }

      indexWriter.commit();
//      indexWriter.close();
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
    try {
      indexWriter.deleteAll();
      indexWriter.prepareCommit();
      indexWriter.commit();
    } catch(Exception ex) {
      log.error("Could not delete Lucene index", ex);
    }

    indexSearcher = null;
  }

  protected IndexSearcher createIndexSearcher() {
    try {
//      directoryReader = DirectoryReader.open(directory);
      directoryReader = DirectoryReader.open(indexWriter, true);
      indexSearcher = new IndexSearcher(directoryReader);
    } catch(Exception ex) {
      log.error("Could not create IndexSearcher", ex);
    }

    return indexSearcher;
  }

  protected IndexSearcher getIndexSearcher() {
    if(indexSearcher == null) {
      try {
//        directoryReader = DirectoryReader.openIfChanged(directoryReader);
        DirectoryReader newDirectoryReader = DirectoryReader.openIfChanged(directoryReader, indexWriter, true);
        if(newDirectoryReader != null)
          directoryReader = newDirectoryReader;
        indexSearcher = new IndexSearcher(directoryReader);
      } catch(Exception ex) {
        log.error("Could not create IndexSearcher", ex);
      }
    }

    return indexSearcher;
  }

  protected IndexWriter createIndexWriter() throws IOException {
    return createIndexWriter(defaultAnalyzer);
  }

  protected IndexWriter createIndexWriter(Analyzer analyzer) throws IOException {
    IndexWriterConfig config = new IndexWriterConfig(analyzer);
    config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
    return new IndexWriter(directory, config);
  }

  protected Set<Entry> entriesToIndex = new CopyOnWriteArraySet<>();
  protected Timer timer = new Timer("IndexEntryTimer");

  @Override
  protected void indexEntry(Entry entry) {
    Document doc = createDocumentFromEntry(entry);

    ((DeepThoughtAnalyzer)defaultAnalyzer).setNextLanguageDependentFieldValueToBeAnalyzed(entry.getAbstract() + " " + entry.getContent());
    indexDocument(doc);
  }

  protected Document createDocumentFromEntry(Entry entry) {
    Document doc = new Document();

    doc.add(new LongField(FieldName.EntryId, entry.getId(), Field.Store.YES));

    doc.add(new Field(FieldName.Abstract, entry.getAbstract(), TextField.TYPE_NOT_STORED));
    doc.add(new Field(FieldName.Content, entry.getContentAsPlainText(), TextField.TYPE_NOT_STORED));

    if(entry.hasTags()) {
      for (Tag tag : entry.getTags()) {
        doc.add(new Field(FieldName.Tags, tag.getName(), TextField.TYPE_NOT_STORED));
        doc.add(new LongField(FieldName.TagsIds, tag.getId(), Field.Store.YES));
      }
    }
    else
      doc.add(new Field(FieldName.NoTags, NoTagsFieldValue, TextField.TYPE_NOT_STORED));

    if(entry.hasCategories()) {
      for (Category category : entry.getCategories())
        doc.add(new Field(FieldName.Categories, category.getName(), TextField.TYPE_NOT_STORED));
    }
    else
      doc.add(new Field(FieldName.NoCategories, NoCategoriesFieldValue, TextField.TYPE_NOT_STORED));

    if(entry.getReferenceSubDivision() != null)
      doc.add(new Field(FieldName.Reference, entry.getReferenceSubDivision().getTextRepresentation(), TextField.TYPE_NOT_STORED));
    else if(entry.getReference() != null)
      doc.add(new Field(FieldName.Reference, entry.getReference().getTextRepresentation(), TextField.TYPE_NOT_STORED));
    else if(entry.getSeries() != null)
      doc.add(new Field(FieldName.Reference, entry.getSeries().getTextRepresentation(), TextField.TYPE_NOT_STORED));
    else if(entry.getReference() != null)
      doc.add(new Field(FieldName.NoReference, NoReferenceFieldValue, TextField.TYPE_NOT_STORED));
    return doc;
  }

  @Override
  protected void updateIndexForEntry(Entry entry, String propertyName) {
//    indexEntity(entry);
//    if(entriesToIndex.contains(entry) == false) {
//      entriesToIndex.add(entry);
//
//      timer.schedule(new TimerTask() {
//        @Override
//        public void run() {
//          List<Entry> entriesCopy = new ArrayList<Entry>(entriesToIndex);
//          for(int i = entriesToIndex.size() - 1; i >= 0; i--) {
//            Entry entryToIndex = entriesCopy.get(i);
//            entriesToIndex.remove(entryToIndex);
//            indexEntity(entryToIndex);
//          }
//        }
//      }, 10000);
//    }

    if(TableConfig.EntryAbstractColumnName.equals(propertyName)) {
      updateStringField(FieldName.EntryId, entry, FieldName.Abstract, entry.getAbstract());
    }
    else if(TableConfig.EntryContentColumnName.equals(propertyName)) {
      updateStringField(FieldName.EntryId, entry, FieldName.Content, entry.getContentAsPlainText());
    }
    else if(TableConfig.EntryTagsPseudoColumnName.equals(propertyName)) {
      try {
        Term idTerm = new Term(FieldName.EntryId, getByteRefFromLong(entry.getId()));
        IndexSearcher searcher = getIndexSearcher();
        ScoreDoc[] hits = searcher.search(new TermQuery(idTerm), 1).scoreDocs;
        if(hits.length == 0) {
          log.error("Could not find Document for updated Entity " + entry);
          indexEntity(entry);
        }
        else {
          Document doc = searcher.doc(hits[0].doc);
          doc.removeField(FieldName.Tags);
          doc.removeField(FieldName.NoTags);

          if(entry.hasTags()) {
            for (Tag tag : entry.getTags()) {
              doc.add(new Field(FieldName.Tags, tag.getName(), TextField.TYPE_NOT_STORED));
              doc.add(new LongField(FieldName.TagsIds, tag.getId(), Field.Store.YES));
            }
          }
          else
            doc.add(new Field(FieldName.NoTags, NoTagsFieldValue, TextField.TYPE_NOT_STORED));

          updateDocument(idTerm, doc);
        }

//        indexWriter.deleteDocuments(idTerm);
//        indexEntity(entity);
      } catch(Exception ex) {
        log.error("Could not update Entity " + entry, ex);
      }
    }
  }

  @Override
  protected void indexTag(Tag tag) {
    Document doc = new Document();

    doc.add(new LongField(FieldName.TagId, tag.getId(), Field.Store.YES));
    doc.add(new Field(FieldName.TagName, tag.getName(), TextField.TYPE_NOT_STORED));

    indexDocument(doc);
  }

  @Override
  protected void updateIndexForTag(Tag tag, String propertyName) {
    if(TableConfig.TagNameColumnName.equals(propertyName)) {
      updateStringField(FieldName.TagId, tag, FieldName.TagName, tag.getName());
    }
  }

  @Override
  protected void indexCategory(Category category) {
    Document doc = new Document();

    doc.add(new LongField(FieldName.CategoryId, category.getId(), Field.Store.YES));
    doc.add(new Field(FieldName.CategoryName, category.getName(), TextField.TYPE_NOT_STORED));

    indexDocument(doc);
  }

  @Override
  protected void updateIndexForCategory(Category category, String propertyName) {
    if(TableConfig.CategoryNameColumnName.equals(propertyName)) {
      updateStringField(FieldName.CategoryId, category, FieldName.CategoryName, category.getName());
    }
  }

  @Override
  protected void indexPerson(Person person) {
    Document doc = new Document();

    doc.add(new LongField(FieldName.PersonId, person.getId(), Field.Store.YES));
    doc.add(new Field(FieldName.PersonFirstName, person.getFirstName(), TextField.TYPE_NOT_STORED));
    doc.add(new Field(FieldName.PersonLastName, person.getLastName(), TextField.TYPE_NOT_STORED));

    indexDocument(doc);
  }

  @Override
  protected void updateIndexForPerson(Person person, String propertyName) {
    if(TableConfig.PersonLastNameColumnName.equals(propertyName)) {
      updateStringField(FieldName.PersonId, person, FieldName.PersonLastName, person.getLastName());
    }
    else if(TableConfig.PersonFirstNameColumnName.equals(propertyName)) {
      updateStringField(FieldName.PersonId, person, FieldName.PersonFirstName, person.getFirstName());
    }
  }

  @Override
  protected void indexSeriesTitle(SeriesTitle seriesTitle) {
    Document doc = new Document();

    doc.add(new LongField(FieldName.ReferenceBaseId, seriesTitle.getId(), Field.Store.YES));
    doc.add(new Field(FieldName.SeriesTitleTitle, getSeriesTitleTitleValue(seriesTitle), TextField.TYPE_NOT_STORED));

    indexDocument(doc);
  }

  protected String getSeriesTitleTitleValue(SeriesTitle seriesTitle) {
    return seriesTitle.getTitle() + " " + seriesTitle.getSubTitle();
  }

  @Override
  protected void updateIndexForSeriesTitle(SeriesTitle seriesTitle, String propertyName) {
    if(TableConfig.ReferenceBaseTitleColumnName.equals(propertyName) || TableConfig.ReferenceBaseSubTitleColumnName.equals(propertyName)) {
      updateStringField(FieldName.ReferenceBaseId, seriesTitle, FieldName.SeriesTitleTitle, getSeriesTitleTitleValue(seriesTitle));
    }
  }

  @Override
  protected void indexReference(Reference reference) {
    Document doc = new Document();

    doc.add(new LongField(FieldName.ReferenceBaseId, reference.getId(), Field.Store.YES));
    doc.add(new Field(FieldName.ReferenceTitle, getReferenceTitleValue(reference), TextField.TYPE_NOT_STORED));

    if(reference.getSeries() != null)
      doc.add(new Field(FieldName.ReferenceSeriesTitle, getSeriesTitleTitleValue(reference.getSeries()), TextField.TYPE_NOT_STORED));

    indexDocument(doc);
  }

  protected String getReferenceTitleValue(Reference reference) {
    return reference.getTitle() + " " + reference.getSubTitle() + " " + reference.getIssueOrPublishingDate();
  }

  @Override
  protected void updateIndexForReference(Reference reference, String propertyName) {
    if(TableConfig.ReferenceBaseTitleColumnName.equals(propertyName) || TableConfig.ReferenceBaseSubTitleColumnName.equals(propertyName) ||
        TableConfig.ReferenceIssueOrPublishingDateColumnName.equals(propertyName)) {
      updateStringField(FieldName.ReferenceBaseId, reference, FieldName.ReferenceTitle, getReferenceTitleValue(reference));
    }
    else if(TableConfig.ReferenceSeriesTitleJoinColumnName.equals(propertyName)) { // TODO: how to determine if SeriesTitle's title or sub title has changed?
      updateStringField(FieldName.ReferenceBaseId, reference, FieldName.ReferenceSeriesTitle, getSeriesTitleTitleValue(reference.getSeries()));
    }
  }

  @Override
  protected void indexReferenceSubDivision(ReferenceSubDivision referenceSubDivision) {
    Document doc = new Document();

    doc.add(new LongField(FieldName.ReferenceBaseId, referenceSubDivision.getId(), Field.Store.YES));
    doc.add(new Field(FieldName.ReferenceSubDivisionTitle, getReferenceSubDivisionTitleValue(referenceSubDivision), TextField.TYPE_NOT_STORED));

    if(referenceSubDivision.getReference() != null) {
      Reference reference = referenceSubDivision.getReference();
      doc.add(new Field(FieldName.ReferenceSubDivisionReference, getReferenceTitleValue(reference), TextField.TYPE_NOT_STORED));

      if (reference.getSeries() != null)
        doc.add(new Field(FieldName.ReferenceSubDivisionSeriesTitle, getSeriesTitleTitleValue(reference.getSeries()), TextField.TYPE_NOT_STORED));
    }

    indexDocument(doc);
  }

  protected String getReferenceSubDivisionTitleValue(ReferenceSubDivision referenceSubDivision) {
    return referenceSubDivision.getTitle() + " " + referenceSubDivision.getSubTitle();
  }

  @Override
  protected void updateIndexForReferenceSubDivision(ReferenceSubDivision subDivision, String propertyName) {
    if(TableConfig.ReferenceBaseTitleColumnName.equals(propertyName) || TableConfig.ReferenceBaseSubTitleColumnName.equals(propertyName)) {
      updateStringField(FieldName.ReferenceBaseId, subDivision, FieldName.ReferenceSubDivisionTitle, getReferenceSubDivisionTitleValue(subDivision));
    }
    else if(TableConfig.ReferenceSubDivisionReferenceJoinColumnName.equals(propertyName)) { // TODO: how to determine if Reference's title or sub title has changed?
      updateStringField(FieldName.ReferenceBaseId, subDivision, FieldName.ReferenceSubDivisionReference, getReferenceTitleValue(subDivision.getReference()));
    }
    // TODO: how to determine if SubDivision's SeriesTitle's title or sub title has changed?
  }

  protected void indexDocument(Document doc) {
    try {
      indexWriter.addDocument(doc);
      indexWriter.commit();

      indexSearcher = null; // so that on next search updates are reflected
    } catch(Exception ex) {
      log.error("Could not index Document " + doc, ex);
    }
  }

  protected void updateStringField(String idFieldName, UserDataEntity entity, String stringFieldName, String updatedValue) {
    try {
      Term idTerm = new Term(idFieldName, getByteRefFromLong(entity.getId()));
      IndexSearcher searcher = getIndexSearcher();
      ScoreDoc[] hits = searcher.search(new TermQuery(idTerm), 1).scoreDocs;
      if(hits.length == 0) {
        log.error("Could not find Document for updated Entity " + entity);
        indexEntity(entity);
      }
      else {
        Document doc = searcher.doc(hits[0].doc);
        doc.removeField(stringFieldName);
        doc.add(new Field(stringFieldName, updatedValue, TextField.TYPE_NOT_STORED));
        updateDocument(idTerm, doc);
      }

//        indexWriter.deleteDocuments(idTerm);
//        indexEntity(entity);
    } catch(Exception ex) {
      log.error("Could not update Entity " + entity, ex);
    }
  }

  protected void updateDocument(Term term, Document doc) {
    try {
      indexWriter.updateDocument(term, doc);

      indexSearcher = null; // so that on next search updates are reflected
    } catch(Exception ex) {
      log.error("Could not update Document " + doc, ex);
    }
  }


  /*        Search          */

  protected void filterTags(Search<Tag> search, String[] tagNamesToFilterFor) {
    BooleanQuery query = new BooleanQuery();

    for(String tagNameToFilterFor : tagNamesToFilterFor) {
      try {
//        QueryParser parser = new QueryParser(FieldName.TagName, analyzer);
//        query.add(parser.parse(tagNamesToFilterFor), BooleanClause.Occur.SHOULD);
        query.add(new PrefixQuery(new Term(FieldName.TagName, tagNameToFilterFor)), BooleanClause.Occur.SHOULD);
      } catch(Exception ex) {
        log.error("Could not parse query " + tagNamesToFilterFor, ex);
        // TODO: set error flag in search
        search.fireSearchCompleted();
        return;
      }
    }

    executeQuery(search, query, Tag.class, FieldName.TagId);
  }

  public void findAllEntriesHavingTheseTags(Collection<Tag> tagsToFilterFor, Collection<Entry> entriesHavingFilteredTags, Set<Tag> tagsOnEntriesContainingFilteredTags) {
    BooleanQuery query = new BooleanQuery();
    for(Tag tag : tagsToFilterFor) {
      query.add(new BooleanClause(new TermQuery(new Term(FieldName.TagsIds, getByteRefFromLong(tag.getId()))), BooleanClause.Occur.MUST));
    }

    try {
      IndexSearcher searcher = getIndexSearcher();

      ScoreDoc[] hits = searcher.search(query, 10000).scoreDocs;

      // Iterate through the results:
      for (int i = 0; i < hits.length; i++) {
        try {
          Document hitDoc = searcher.doc(hits[i].doc);
          Entry resultEntry = (Entry)getEntityFromDocument(hitDoc, Entry.class, FieldName.EntryId);
          entriesHavingFilteredTags.add(resultEntry);
          tagsOnEntriesContainingFilteredTags.addAll(resultEntry.getTags());
        } catch(Exception ex) { log.error("Could not extract result from hitDoc", ex); }
      }

    } catch(Exception ex) {
      log.error("Could not execute Query " + query.toString(), ex);
    }
  }

  @Override
  protected void filterEntries(FilterEntriesSearch search, String contentFilter, String abstractFilter) {
    // TODO: i think it's better to analyze content- and abstractFilter as they are being used on analyzed fields
//    Analyzer analyzer = getAnalyzerForTextLanguage(search.getSearchTerm());
    BooleanQuery query = new BooleanQuery();

    if(contentFilter != null) {
      try {
//        QueryParser parser = new QueryParser(FieldName.Content, analyzer);
//        query.add(parser.parse(contentFilter), BooleanClause.Occur.SHOULD);
        query.add(new PrefixQuery(new Term(FieldName.Content, contentFilter)), BooleanClause.Occur.SHOULD);
      } catch(Exception ex) {
        log.error("Could not parse query " + contentFilter, ex);
        // TODO: set error flag in search
        search.fireSearchCompleted();
        return;
      }
    }
    if(abstractFilter != null) {
      try {
//        QueryParser parser = new QueryParser(FieldName.Abstract, analyzer);
//        query.add(parser.parse(abstractFilter), BooleanClause.Occur.SHOULD);
        query.add(new PrefixQuery(new Term(FieldName.Abstract, abstractFilter)), BooleanClause.Occur.SHOULD);
      } catch(Exception ex) {
        log.error("Could not parse query " + abstractFilter, ex);
        // TODO: set error flag in search
        search.fireSearchCompleted();
        return;
      }
    }

    executeQuery(search, query, Entry.class, FieldName.EntryId);
  }

  @Override
  protected void filterPersons(Search<Person> search, String personFilter) {
    BooleanQuery query = new BooleanQuery();

    query.add(new PrefixQuery(new Term(FieldName.PersonFirstName, personFilter)), BooleanClause.Occur.SHOULD);
    query.add(new PrefixQuery(new Term(FieldName.PersonLastName, personFilter)), BooleanClause.Occur.SHOULD);

    executeQuery(search, query, Person.class, FieldName.PersonId);
  }

  @Override
  protected void filterPersons(Search<Person> search, String lastNameFilter, String firstNameFilter) {
    BooleanQuery query = new BooleanQuery();

    try {
//        QueryParser parser = new QueryParser(FieldName.Content, analyzer);
//        query.add(parser.parse(firstNameFilter), BooleanClause.Occur.SHOULD);
      query.add(new PrefixQuery(new Term(FieldName.PersonFirstName, firstNameFilter)), BooleanClause.Occur.MUST);
//      query.add(new TermQuery(new Term(FieldName.PersonFirstName, firstNameFilter)), BooleanClause.Occur.MUST);
    } catch(Exception ex) {
      log.error("Could not parse query " + firstNameFilter, ex);
      // TODO: set error flag in search
      search.fireSearchCompleted();
      return;
    }
    try {
//        QueryParser parser = new QueryParser(FieldName.Content, analyzer);
//        query.add(parser.parse(lastNameFilter), BooleanClause.Occur.SHOULD);
      query.add(new PrefixQuery(new Term(FieldName.PersonLastName, lastNameFilter)), BooleanClause.Occur.MUST);
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
  protected void filterAllReferenceBasesForSameFilter(Search search, String referenceBaseFilter) {
    BooleanQuery query = new BooleanQuery();

    query.add(new PrefixQuery(new Term(FieldName.SeriesTitleTitle, referenceBaseFilter)), BooleanClause.Occur.SHOULD);
    query.add(new PrefixQuery(new Term(FieldName.ReferenceTitle, referenceBaseFilter)), BooleanClause.Occur.SHOULD);
    query.add(new PrefixQuery(new Term(FieldName.ReferenceSubDivisionTitle, referenceBaseFilter)), BooleanClause.Occur.SHOULD);

    executeQuery(search, query, ReferenceBase.class, FieldName.ReferenceBaseId);
  }

  @Override
  protected void filterReferenceBases(Search search, String seriesTitleFilter, String referenceFilter, String referenceSubDivisionFilter) {
    BooleanQuery query = new BooleanQuery();

    if(seriesTitleFilter != null) {
      try {
//        QueryParser parser = new QueryParser(FieldName.Content, analyzer);
//        query.add(parser.parse(contentFilter), BooleanClause.Occur.SHOULD);
        query.add(new PrefixQuery(new Term(FieldName.SeriesTitleTitle, seriesTitleFilter)), BooleanClause.Occur.SHOULD);
      } catch(Exception ex) {
        log.error("Could not parse query " + seriesTitleFilter, ex);
        // TODO: set error flag in search
        search.fireSearchCompleted();
        return;
      }
    }
    if(referenceFilter != null) {
      try {
//        QueryParser parser = new QueryParser(FieldName.Abstract, analyzer);
//        query.add(parser.parse(abstractFilter), BooleanClause.Occur.SHOULD);
        query.add(new PrefixQuery(new Term(FieldName.ReferenceTitle, referenceFilter)), BooleanClause.Occur.SHOULD);
      } catch(Exception ex) {
        log.error("Could not parse query " + referenceFilter, ex);
        // TODO: set error flag in search
        search.fireSearchCompleted();
        return;
      }
    }
    if(referenceSubDivisionFilter != null) {
      try {
//        QueryParser parser = new QueryParser(FieldName.Abstract, analyzer);
//        query.add(parser.parse(abstractFilter), BooleanClause.Occur.SHOULD);
        query.add(new PrefixQuery(new Term(FieldName.ReferenceSubDivisionTitle, referenceSubDivisionFilter)), BooleanClause.Occur.SHOULD);
      } catch(Exception ex) {
        log.error("Could not parse query " + referenceSubDivisionFilter, ex);
        // TODO: set error flag in search
        search.fireSearchCompleted();
        return;
      }
    }

    executeQuery(search, query, ReferenceBase.class, FieldName.ReferenceBaseId);
  }

  protected void filterSeriesTitles(Search search, String seriesTitleFilter) {
//    Analyzer analyzer = getAnalyzerForTextLanguage(search.getSearchTerm());
//        QueryParser parser = new QueryParser(FieldName.SeriesTitleTitle, analyzer);
    Query query = new PrefixQuery(new Term(FieldName.SeriesTitleTitle, seriesTitleFilter));

    executeQuery(search, query, ReferenceBase.class, FieldName.ReferenceBaseId);
  }

  @Override
  protected void filterReferences(Search search, String seriesTitleFilter, String referenceFilter, String referenceSubDivisionFilter) {
    super.filterReferences(search, seriesTitleFilter, referenceFilter, referenceSubDivisionFilter);
  }

  @Override
  protected void filterReferenceSubDivisions(Search search, Reference reference, String seriesTitleFilter, String referenceFilter, String referenceSubDivisionFilter) {
    super.filterReferenceSubDivisions(search, reference, seriesTitleFilter, referenceFilter, referenceSubDivisionFilter);
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
    if(search.isInterrupted())
      return;

    try {
      IndexSearcher searcher = getIndexSearcher();

      ScoreDoc[] hits = searcher.search(query, 1000).scoreDocs;

      // Iterate through the results:
      for (int i = 0; i < hits.length; i++) {
        if(search.isInterrupted())
          return;

        try {
          Document hitDoc = searcher.doc(hits[i].doc);
          search.addResult(getEntityFromDocument(hitDoc, resultEntityClass, idFieldName));
        } catch(Exception ex) { log.error("Could not extract result from hitDoc", ex); }
      }
    } catch(Exception ex) {
      log.error("Could not execute Query " + query.toString(), ex);
      // TODO: set error flag in Search
    }

    search.fireSearchCompleted();
  }

  protected BaseEntity getEntityFromDocument(Document hitDoc, Class<? extends BaseEntity> resultEntityClass, String idFieldName) {
    Long entityId = hitDoc.getField(idFieldName).numericValue().longValue();
    return Application.getEntityManager().getEntityById(resultEntityClass, entityId);
//    if(resultEntityClass != ReferenceBase.class)
//      return Application.getEntityManager().getEntityById(resultEntityClass, entityId);
//    else { // TODO: this is quite a bad workaround, should actually be solved in OrmLite: if for a Inheritance Top Level entity is search, the concrete entity doesn't get created correctly
//      if(hitDoc.getField(FieldName.SeriesTitleTitle) != null)
//        return Application.getEntityManager().getEntityById(SeriesTitle.class, entityId);
//      else if(hitDoc.getField(FieldName.ReferenceSubDivisionTitle) != null)
//        return Application.getEntityManager().getEntityById(ReferenceSubDivision.class, entityId);
//      else
//        return Application.getEntityManager().getEntityById(Reference.class, entityId);
//    }
  }


  private static int idMock = 1;

  // TODO: remove
  public void index(String text) throws IOException {
    LanguageIdentifier languageIdentifier = new LanguageIdentifier(text);
    String language = languageIdentifier.getLanguage();
    boolean isCertain = languageIdentifier.isReasonablyCertain();

    GermanStemFilter filter = new GermanStemFilter(new GermanNormalizationFilter(new StopFilter(new StandardTokenizer(), GermanAnalyzer.getDefaultStopSet())));
    filter.setStemmer(new GermanStemmer());

    IndexWriterConfig config = new IndexWriterConfig(defaultAnalyzer);
    config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
    IndexWriter iwriter = new IndexWriter(directory, config);

    Document doc = new Document();
    doc.add(new Field(FieldName.Content, text, TextField.TYPE_STORED));
    doc.add(new IntField(FieldName.EntryId, idMock++, Field.Store.YES));
    iwriter.addDocument(doc);
    iwriter.close();

    DirectoryReader reader = DirectoryReader.open(directory);

//    try {
////    Analyzer analyzer = new GermanAnalyzer();
//      Analyzer analyzer = new StandardAnalyzer(GermanAnalyzer.getDefaultStopSet());
//    TokenStream tokenStream = analyzer.tokenStream("", new StringReader(text));
////      TokenStream tokenStream = new StandardTokenizer(Version.LUCENE_47, new StringReader(text));
////      TokenStream tokenStream = new StandardTokenizer(AttributeFactory.DEFAULT_ATTRIBUTE_FACTORY);
//      OffsetAttribute offsetAttribute = tokenStream.addAttribute(OffsetAttribute.class);
//      CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
//
//      tokenStream.reset();
//      while (tokenStream.incrementToken()) {
////        extractedKeywords.add(charTermAttribute.toString());
//        System.out.println(charTermAttribute.toString());
//      }
//    } catch(Exception ex) {
//      log.error("Could not extract keywords from text " + text, ex);
//    }


//    DocsEnum de = MultiFields.getTermDocsEnum(reader, MultiFields.getLiveDocs(reader), FieldName.Content.toString(), new BytesRef("run"));
//    if(de != null) {
//      int docNum;
//      while ((docNum = de.nextDoc()) != DocsEnum.NO_MORE_DOCS) {
//        System.out.println(de.freq());
//      }
//    }

    int numDocs = reader.numDocs();
    int refCount = reader.getRefCount();
//    Document lastInsertedDoc = reader.document(numDocs - 1);
    Fields fields = reader.getTermVectors(numDocs - 1);
    if(fields != null) {
      for (String field : fields) {
        Terms terms = fields.terms(field);
        long docFreq = terms.getSumDocFreq();
      }
    }

    reader.close();
  }


  public List<BaseEntity> search(String term, Class resultEntityType) throws IOException, ParseException {
    List<Long> searchResultIds = new ArrayList<>();

    IndexSearcher indexSearcher = getIndexSearcher();
    // Parse a simple query that searches for "text":
    // TODO: only Content and Abstract have language bases analyzers
//    QueryParser parser = new QueryParser(FieldName.Content, getAnalyzerForTextLanguage(term));
    QueryParser parser = new QueryParser(FieldName.Content, defaultAnalyzer);
    Query query = parser.parse(term);
    ScoreDoc[] hits = indexSearcher.search(query, null, 1000).scoreDocs;

    // Iterate through the results:
    System.out.println("Searching for term " + term + " resulted " + hits.length + " results:");
    for (int i = 0; i < hits.length; i++) {
      Document hitDoc = indexSearcher.doc(hits[i].doc);
      Long entityId = hitDoc.getField(FieldName.EntryId).numericValue().longValue();
      searchResultIds.add(entityId);
    }

    return getBaseEntitiesFromIds(resultEntityType, searchResultIds);
  }

  protected <T extends BaseEntity> List<T> getBaseEntitiesFromIds(Class<T> type, List<Long> searchResultIds) {
    List<T> resultEntities = new ArrayList<>();

    for(Long entityId : searchResultIds)
      resultEntities.add(Application.getEntityManager().getEntityById(type, entityId));

    return resultEntities;
  }

  public Collection<Entry> findEntriesWithTags(String[] tagNames) throws IOException, ParseException {
    Set<Entry> entriesWithTags = new HashSet<>();

    IndexSearcher searcher = getIndexSearcher();

    // find docs without tags
//    ScoreDoc[] hits = searcher.search(new TermQuery(new Term(FieldName.NoTags.toString(), NoTagsFieldValue)), 100000).scoreDocs;
    QueryParser parser = new QueryParser(FieldName.Tags, defaultAnalyzer);
    BooleanQuery query = new BooleanQuery();
    for(int i = 0; i < tagNames.length; i++) {
//      query.add(parser.parse(tagNames[i]), BooleanClause.Occur.MUST);
      query.add(new TermQuery(new Term(FieldName.Tags, tagNames[i])), BooleanClause.Occur.MUST);
    }
    ScoreDoc[] hits = searcher.search(query, 100000).scoreDocs;

    // Iterate through the results:
    for (int i = 0; i < hits.length; i++) {
      Document hitDoc = searcher.doc(hits[i].doc);
//      entriesWithTags.add(hitDoc.getField(FieldName.EntryId).numericValue().longValue());
      entriesWithTags.add((Entry)getEntityFromDocument(hitDoc, Entry.class, FieldName.EntryId));
    }

    return entriesWithTags;
  }

  public Collection<Entry> getEntriesWithoutTags() throws IOException, ParseException {
    Set<Entry> entriesWithoutTags = new HashSet<>();

    IndexSearcher searcher = getIndexSearcher();

    // find docs without tags
//    ScoreDoc[] hits = searcher.search(new TermQuery(new Term(FieldName.NoTags.toString(), NoTagsFieldValue)), 100000).scoreDocs;
//    QueryParser parser = new QueryParser(FieldName.NoTags, defaultAnalyzer);
//    Query query = parser.parse(NoTagsFieldValue);
    Query query = new TermQuery(new Term(FieldName.NoTags, NoTagsFieldValue.toLowerCase()));
    ScoreDoc[] hits = searcher.search(query, 100000).scoreDocs;

    // Iterate through the results:
    for (int i = 0; i < hits.length; i++) {
      Document hitDoc = searcher.doc(hits[i].doc);
//      entriesWithoutTags.add(hitDoc.getField(FieldName.EntryId).numericValue().longValue());
      entriesWithoutTags.add((Entry)getEntityFromDocument(hitDoc, Entry.class, FieldName.EntryId));
    }

    return entriesWithoutTags;
  }

//  public void test() throws IOException {
//    Analyzer analyzer = new StandardAnalyzer();
//
//    // Store the index in memory:
//    Directory directory = new RAMDirectory();
//    // To store an index on disk, use this instead:
//    //Directory directory = FSDirectory.open("/tmp/testindex");
//    IndexWriterConfig config = new IndexWriterConfig(analyzer);
//    IndexWriter iwriter = new IndexWriter(directory, config);
//    Document doc = new Document();
//    String text = "This is the text to be indexed.";
//    doc.add(new Field(FieldName.Content.toString(), text, TextField.TYPE_STORED));
//    iwriter.addDocument(doc);
//    iwriter.close();
//
//    // Now search the index:
//    DirectoryReader ireader = DirectoryReader.open(directory);
//    IndexSearcher isearcher = new IndexSearcher(ireader);
//    // Parse a simple query that searches for "text":
//    QueryParser parser = new QueryParser(FieldName.Content.toString(), analyzer);
//    Query query = parser.parse("text");
//    ScoreDoc[] hits = isearcher.search(query, null, 1000).scoreDocs;
//    assertEquals(1, hits.length);
//    // Iterate through the results:
//    for (int i = 0; i < hits.length; i++) {
//      Document hitDoc = isearcher.doc(hits[i].doc);
//      assertEquals("This is the text to be indexed.", hitDoc.get(FieldName.Content.toString()));
//    }
//    ireader.close();
//    directory.close();
//  }

  public List<IndexTerm> getAllTerms() throws IOException {
    List<IndexTerm> allTerms = new ArrayList<>();
    DirectoryReader reader = DirectoryReader.open(directory);
    Fields fields = MultiFields.getFields(reader);

    Bits liveDocs = MultiFields.getLiveDocs(reader);
    DocsEnum docsEnum = null;

    for (String field : fields) {
      Terms terms = fields.terms(field);
      TermsEnum termsEnum = terms.iterator(null);
      int count = 0;
      BytesRef text;
      while((text = termsEnum.next()) != null) {
        count++;
        IndexTerm indexTerm = new IndexTerm(text.utf8ToString(), termsEnum.docFreq());
        allTerms.add(indexTerm);

        docsEnum = termsEnum.docs(liveDocs, docsEnum, DocsEnum.FLAG_FREQS);
        if(docsEnum != null) {
          int docId;
          while ((docId = docsEnum.nextDoc()) != DocsEnum.NO_MORE_DOCS) {
            Document doc = reader.document(docId);
            IndexableField idField = doc.getField("id");
            String[] values = doc.getValues("id");
            indexTerm.addEntryContainingTerm(idField.numericValue().longValue());
          }
        }
      }

      System.out.println(count);
    }

    return allTerms;
  }


  protected BytesRef getByteRefFromLong(Long longValue) {
    BytesRefBuilder byteRefBuilder = new BytesRefBuilder();
    NumericUtils.longToPrefixCoded(longValue, 0, byteRefBuilder);
    return byteRefBuilder.toBytesRef();
  }

}
