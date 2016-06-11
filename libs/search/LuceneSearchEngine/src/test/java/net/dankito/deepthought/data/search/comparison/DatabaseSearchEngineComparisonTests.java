package net.dankito.deepthought.data.search.comparison;

import net.dankito.deepthought.data.search.DatabaseSearchEngine;
import net.dankito.deepthought.data.search.ISearchEngine;

/**
 * Created by ganymed on 16/04/15.
 */
public class DatabaseSearchEngineComparisonTests extends SearchComparisonTestBase {

  @Override
  protected ISearchEngine createSearchEngine() {
    return new DatabaseSearchEngine();
  }

}
