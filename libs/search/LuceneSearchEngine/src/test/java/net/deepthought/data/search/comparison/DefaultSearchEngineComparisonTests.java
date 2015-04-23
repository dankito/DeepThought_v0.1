package net.deepthought.data.search.comparison;

import net.deepthought.data.search.DefaultSearchEngine;
import net.deepthought.data.search.ISearchEngine;

/**
 * Created by ganymed on 16/04/15.
 */
public class DefaultSearchEngineComparisonTests extends SearchComparisonTestBase {

  @Override
  protected ISearchEngine createSearchEngine() {
    return new DefaultSearchEngine();
  }
}
