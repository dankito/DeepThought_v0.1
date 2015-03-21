package net.deepthought.data.search;

import net.deepthought.data.model.Entry;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.de.GermanAnalyzer;
import org.apache.lucene.analysis.de.GermanNormalizationFilter;
import org.apache.lucene.analysis.de.GermanStemFilter;
import org.apache.lucene.analysis.de.GermanStemmer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.DocsEnum;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Bits;
import org.apache.lucene.util.BytesRef;
import org.apache.tika.language.LanguageIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class LuceneSearchEngine implements IDeepThoughtSearchEngine {

  private final static Logger log = LoggerFactory.getLogger(LuceneSearchEngine.class);


  protected Analyzer analyzer;

  protected Directory directory;

  protected Set<String> supportedLanguages;

  // TODO:
  // - Directory where to store Index to

  public LuceneSearchEngine() {
    LanguageIdentifier.initProfiles();
    supportedLanguages = LanguageIdentifier.getSupportedLanguages();

    analyzer = new StandardAnalyzer(GermanAnalyzer.getDefaultStopSet());

    // Store the index in memory:
    directory = new RAMDirectory();
    // To store an index on disk, use this instead:
    //directory = FSDirectory.open("/tmp/testindex");
  }


  private static int idMock = 1;

  public void index(String text) throws IOException {
    LanguageIdentifier languageIdentifier = new LanguageIdentifier(text);
    String language = languageIdentifier.getLanguage();
    boolean isCertain = languageIdentifier.isReasonablyCertain();

    GermanStemFilter filter = new GermanStemFilter(new GermanNormalizationFilter(new StopFilter(new StandardTokenizer(), GermanAnalyzer.getDefaultStopSet())));
    filter.setStemmer(new GermanStemmer());

    IndexWriterConfig config = new IndexWriterConfig(analyzer);
    config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
    IndexWriter iwriter = new IndexWriter(directory, config);

    Document doc = new Document();
    doc.add(new Field("fieldname", text, TextField.TYPE_STORED));
    doc.add(new IntField("id", idMock++, Field.Store.YES));
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


//    DocsEnum de = MultiFields.getTermDocsEnum(reader, MultiFields.getLiveDocs(reader), "fieldname", new BytesRef("run"));
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

  public void updateIndex(Entry entry) {
//    writer.updateDocument(new Term("path", file.toString()), doc);
    // TODO: get Document by id field
//    writer.updateDocument(new Term("id", entry.getId()), doc);
  }

  public void search(String term) throws IOException, ParseException {
    DirectoryReader ireader = DirectoryReader.open(directory);
    IndexSearcher isearcher = new IndexSearcher(ireader);
    // Parse a simple query that searches for "text":
    QueryParser parser = new QueryParser("fieldname", analyzer);
    Query query = parser.parse(term);
    ScoreDoc[] hits = isearcher.search(query, null, 1000).scoreDocs;

    // Iterate through the results:
    System.out.println("Searching for term " + term + " resulted " + hits.length + " results:");
    for (int i = 0; i < hits.length; i++) {
      Document hitDoc = isearcher.doc(hits[i].doc);
      System.out.println(hitDoc.toString());
    }

    ireader.close();
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
//    doc.add(new Field("fieldname", text, TextField.TYPE_STORED));
//    iwriter.addDocument(doc);
//    iwriter.close();
//
//    // Now search the index:
//    DirectoryReader ireader = DirectoryReader.open(directory);
//    IndexSearcher isearcher = new IndexSearcher(ireader);
//    // Parse a simple query that searches for "text":
//    QueryParser parser = new QueryParser("fieldname", analyzer);
//    Query query = parser.parse("text");
//    ScoreDoc[] hits = isearcher.search(query, null, 1000).scoreDocs;
//    assertEquals(1, hits.length);
//    // Iterate through the results:
//    for (int i = 0; i < hits.length; i++) {
//      Document hitDoc = isearcher.doc(hits[i].doc);
//      assertEquals("This is the text to be indexed.", hitDoc.get("fieldname"));
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
}
