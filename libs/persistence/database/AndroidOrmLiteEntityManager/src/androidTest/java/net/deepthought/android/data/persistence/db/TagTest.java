package net.deepthought.android.data.persistence.db;

import net.deepthought.Application;
import net.deepthought.data.model.DeepThought;
import net.deepthought.data.model.Tag;

import java.util.List;

/**
 * Created by ganymed on 10/11/14.
 */
public class TagTest extends EntitiesTestBase {

  public void testUpdateName_UpdatedNameGetsPersistedInDb() throws Exception {
    Tag tag = new Tag("test");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addTag(tag);

    String newName = "New name";
    tag.setName(newName);

    // assert name really got written to database
    List<String[]> queryResult = entityManager.<String[]>doNativeQuery("SELECT name FROM tag WHERE id=" + tag.getId());
    assertEquals(1, queryResult.size());
    assertEquals(1, queryResult.get(0).length);
    assertEquals(newName, queryResult.get(0)[0]);
  }
}
