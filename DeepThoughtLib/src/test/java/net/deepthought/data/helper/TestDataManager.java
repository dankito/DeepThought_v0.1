package net.deepthought.data.helper;

import net.deepthought.data.DefaultDataManager;
import net.deepthought.data.model.DeepThought;
import net.deepthought.data.persistence.IEntityManager;

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
    getSettings().setAutoSaveChangesAfterMilliseconds(0);

    return deepThought;
  }

  @Override
  public String getDataFolderPath() {
    return "data/tests";
  }

}
