package net.deepthought.data.search.comparison;

import net.deepthought.data.search.ISearchEngine;
import net.deepthought.data.search.LuceneSearchEngine;

import org.junit.Assert;

/**
 * Created by ganymed on 16/04/15.
 */
public class LuceneSearchEngineComparisonTests extends SearchComparisonTestBase {

  @Override
  protected ISearchEngine createSearchEngine() {
    try {
      return new LuceneSearchEngine();

//      LuceneSearchEngine searchEngine = new LuceneSearchEngine();
//      searchEngine.rebuildIndex();
//      return searchEngine;
    } catch(Exception ex) {
      log.error("Could not create LuceneSearchEngine", ex);
      Assert.fail("Could not create LuceneSearchEngine: " + ex);
    }

    return null;
  }

}
