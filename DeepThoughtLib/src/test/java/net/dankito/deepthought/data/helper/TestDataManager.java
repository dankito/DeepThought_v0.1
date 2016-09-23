package net.dankito.deepthought.data.helper;

import net.dankito.deepthought.data.DefaultDataManager;
import net.dankito.deepthought.data.model.DeepThought;
import net.dankito.deepthought.data.persistence.IEntityManager;

/**
 * Created by ganymed on 17/01/15.
 */
public class TestDataManager extends DefaultDataManager {

  public TestDataManager(IEntityManager entityManager) {
    super(entityManager);
  }

  @Override
  public DeepThought retrieveDeepThoughtApplication() {
    DeepThought deepThought = super.retrieveDeepThoughtApplication();

    // ensure that changes get persisted at once
    try {
      loggedOnUser.getSettings().setAutoSaveChangesAfterMilliseconds(0);
    } catch(Exception ex) { }

    return deepThought;
  }

  @Override
  public String getDataFolderPath() {
    return "data/tests";
  }

}
