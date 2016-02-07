package net.deepthought.data.model.enums;

import net.deepthought.Application;
import net.deepthought.data.persistence.db.TableConfig;

import org.junit.Assert;
import org.junit.Test;

import java.util.Collection;

/**
 * Created by ganymed on 14/02/15.
 */
public abstract class EditableExtensibleEnumerationTestBase<T extends ExtensibleEnumeration> extends ExtensibleEnumerationTestBase {

  protected abstract T createNewEnumValue();

  protected abstract void addToEnumeration(T enumValue);
  protected abstract void removeFromEnumeration(T enumValue);

  protected abstract Collection<T> getEnumeration();


  @Test
  public void addToEnumeration_RelationGetsPersisted() throws Exception {
    T enumValue = createNewEnumValue();
    addToEnumeration(enumValue);

    Assert.assertNotNull(enumValue.getId());
    Assert.assertTrue(doIdsEqual(Application.getDeepThought().getId(), getValueFromTable(getEnumerationTableName(), TableConfig.ExtensibleEnumerationDeepThoughtJoinColumnName, enumValue.getId())));
  }

  @Test
  public void addToEnumeration_EntitiesGetAddedToRelatedCollections() throws Exception {
    T enumValue = createNewEnumValue();
    addToEnumeration(enumValue);

    Assert.assertEquals(Application.getDeepThought(), enumValue.getDeepThought());
    Assert.assertTrue(getEnumeration().contains(enumValue));
  }

  @Test
  public void removeFromEnumeration_RelationGetsPersisted() throws Exception {
    T enumValue = createNewEnumValue();
    addToEnumeration(enumValue);

    removeFromEnumeration(enumValue);

    Assert.assertTrue(enumValue.isDeleted());
    Assert.assertNull(getValueFromTable(getEnumerationTableName(), TableConfig.ExtensibleEnumerationDeepThoughtJoinColumnName, enumValue.getId()));
  }

  @Test
  public void removeFromEnumeration_EntitiesGetAddedToRelatedCollections() throws Exception {
    T enumValue = createNewEnumValue();
    addToEnumeration(enumValue);

    removeFromEnumeration(enumValue);

    Assert.assertNull(enumValue.getDeepThought());
    Assert.assertFalse(getEnumeration().contains(enumValue));
  }

}
