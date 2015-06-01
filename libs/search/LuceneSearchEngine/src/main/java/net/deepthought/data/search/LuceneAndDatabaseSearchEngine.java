package net.deepthought.data.search;

import net.deepthought.data.model.Entry;
import net.deepthought.data.model.Person;
import net.deepthought.data.model.Tag;

import org.apache.lucene.store.Directory;

/**
 * <p>
 *  Uses that methods from LuceneSearchEngine and DatabaseSearchEngine which in my tests turned out to be the fastest for a specific task
 *  in order to get the fastest and most efficient search engine possible.
 * </p>
 */
public class LuceneAndDatabaseSearchEngine extends LuceneSearchEngine {

  protected DatabaseSearchEngine databaseSearchEngine = new DatabaseSearchEngine();


  public LuceneAndDatabaseSearchEngine() {
  }

  public LuceneAndDatabaseSearchEngine(Directory directory) {
    super(directory);
  }

  @Override
  public void getEntriesWithoutTags(SearchCompletedListener<Entry> listener) {
    databaseSearchEngine.getEntriesWithoutTags(listener);
  }

  @Override
  protected void filterTags(Search<Tag> search, String[] tagNamesToFilterFor) {
    databaseSearchEngine.filterTags(search, tagNamesToFilterFor);
  }

  @Override
  protected void filterPersons(Search<Person> search, String lastNameFilter, String firstNameFilter) {
    databaseSearchEngine.filterPersons((Search<Person>) search, (String) lastNameFilter, (String) firstNameFilter);
  }

  @Override
  protected void filterPersons(Search<Person> search, String personFilter) {
    databaseSearchEngine.filterPersons(search, personFilter);
  }
}
