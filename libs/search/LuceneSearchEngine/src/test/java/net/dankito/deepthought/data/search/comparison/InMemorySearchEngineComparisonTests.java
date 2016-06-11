package net.dankito.deepthought.data.search.comparison;

import net.dankito.deepthought.data.search.InMemorySearchEngine;
import net.dankito.deepthought.data.search.ISearchEngine;

/**
 * Created by ganymed on 16/04/15.
 */
public class InMemorySearchEngineComparisonTests extends SearchComparisonTestBase {

  @Override
  protected ISearchEngine createSearchEngine() {
    return new InMemorySearchEngine();
  }
}
