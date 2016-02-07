package net.deepthought.data.search.comparison;

import net.deepthought.data.search.ISearchEngine;
import net.deepthought.data.search.LuceneSearchEngine;

import org.junit.Assert;
import org.junit.BeforeClass;

/**
 * Created by ganymed on 16/04/15.
 */
public class LuceneSearchEngineComparisonTests extends SearchComparisonTestBase {

  @BeforeClass
  public static void suiteSetup() throws Exception {
    SearchComparisonTestBase.suiteSetup();

//    LuceneSearchEngine rebuildIndexSearchEngine = new LuceneSearchEngine();
//    rebuildIndexSearchEngine.rebuildIndex();
//    rebuildIndexSearchEngine.close();
  }

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
