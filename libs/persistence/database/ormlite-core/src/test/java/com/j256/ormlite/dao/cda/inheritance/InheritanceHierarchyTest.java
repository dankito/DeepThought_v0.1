package com.j256.ormlite.dao.cda.inheritance;

import com.j256.ormlite.BaseCoreTest;
import com.j256.ormlite.dao.cda.testmodel.InheritanceModel;
import com.j256.ormlite.jpa.EntityConfig;
import com.j256.ormlite.jpa.SingleTableEntityConfig;
import com.j256.ormlite.jpa.inheritance.EntityInheritance;
import com.j256.ormlite.jpa.inheritance.InheritanceHierarchy;
import com.j256.ormlite.misc.JavaxPersistenceConfigurer;
import com.j256.ormlite.misc.JavaxPersistenceImpl;
import com.j256.ormlite.misc.TableInfoRegistry;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;

import javax.persistence.InheritanceType;

/**
 * Created by ganymed on 16/11/14.
 */
public class InheritanceHierarchyTest extends BaseCoreTest {

  JavaxPersistenceConfigurer javaxPersistence = null;

  @Before
  public void setup() {
    javaxPersistence = new JavaxPersistenceImpl();
  }

  @Test
  public void singleTableInheritance_InheritanceHierarchyGetCreatedCorrectly() {
    InheritanceHierarchy hierarchy = JavaxPersistenceImpl.getInheritanceHierarchyForClass(InheritanceModel.SingleTableFirstDirectSubEntityChild.class);

    Assert.assertEquals(1, hierarchy.getHierarchy().size());

    EntityInheritance entityInheritance = hierarchy.getEntityInheritanceAtTop();
    Assert.assertEquals(InheritanceType.SINGLE_TABLE, entityInheritance.getInheritanceType());
    Assert.assertEquals(InheritanceModel.SingleTableInheritanceBaseEntity.class, entityInheritance.getEntity());
    Assert.assertFalse(entityInheritance.hasDiscriminatorColumn());
    Assert.assertEquals(2, entityInheritance.getInheritanceLevelSubclasses().size());
  }

  @Test
  public void singleTableInheritance_SingleTableTableInfoGetCreatedCorrectly() throws SQLException {
    EntityConfig[] entityConfigs = TableInfoRegistry.getInstance().createTableInfos(connectionSource, SingleTableTest.singleTableEntities);

    Assert.assertEquals(4, entityConfigs.length);

    EntityConfig singleTableBaseEntityEntityConfig = TableInfoRegistry.getInstance().getTableInfoForClass(InheritanceModel.SingleTableInheritanceBaseEntity.class);
    Assert.assertTrue(singleTableBaseEntityEntityConfig instanceof SingleTableEntityConfig);

    SingleTableEntityConfig singleTableTableInfo = (SingleTableEntityConfig) singleTableBaseEntityEntityConfig;
    Assert.assertEquals(5, singleTableTableInfo.getAllTableFieldsList().size());
    Assert.assertEquals(2, singleTableTableInfo.getChildEntityConfigs().size());

    for(EntityConfig entityConfig : entityConfigs) {
      Assert.assertEquals(InheritanceType.SINGLE_TABLE, entityConfig.getInheritance());
      Assert.assertEquals(singleTableTableInfo.getTableName(), entityConfig.getTableName());
    }
  }
}
