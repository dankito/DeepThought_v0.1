package net.dankito.deepthought.data.helper;

/**
 * Created by ganymed on 11/04/15.
 */
public class MockEntityManagerWithPredefinedData extends MockEntityManager {


  public MockEntityManagerWithPredefinedData() {
    persistEntity(DataHelper.createTestApplication());
  }

}
