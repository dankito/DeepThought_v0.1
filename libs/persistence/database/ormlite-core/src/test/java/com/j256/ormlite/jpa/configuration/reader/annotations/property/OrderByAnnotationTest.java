package com.j256.ormlite.jpa.configuration.reader.annotations.property;

import com.j256.ormlite.jpa.PropertyConfig;
import com.j256.ormlite.jpa.configuration.reader.JpaConfigurationReaderTestBase;

import org.junit.Assert;
import org.junit.Test;

import java.sql.SQLException;
import java.util.Collection;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;

/**
 * Created by ganymed on 07/03/15.
 */
public class OrderByAnnotationTest extends JpaConfigurationReaderTestBase {

  @Entity
  static class EntityWithoutOrderByAnnotation {
    @Id
    protected Long id;

    @OneToMany(mappedBy = "parent")
    protected Collection<EntityWithoutOrderByAnnotation> relations;
    @ManyToOne
    protected EntityWithoutOrderByAnnotation parent;
  }

  @Test
  public void orderByAnnotationNotSet_IsVersionIsSetToFalse() throws SQLException, NoSuchFieldException {
    entityConfigurationReader.readConfiguration(new Class[] { EntityWithoutOrderByAnnotation.class });
    PropertyConfig relationsPropertyConfig = getPropertyConfigurationForField(EntityWithoutOrderByAnnotation.class, "relations");

    Assert.assertFalse(relationsPropertyConfig.hasOrderColumns());
  }


  @Entity
  static class EntityWithOrderByAnnotation {
    @Id protected Long id;
    protected String lastName;

    @OneToMany(mappedBy = "parent")
    @OrderBy("lastName ASC, id DESC")
    protected Collection<EntityWithOrderByAnnotation> relations;
    @ManyToOne
    protected EntityWithOrderByAnnotation parent;
  }

  @Test
  public void orderByAnnotationSet_RelationsGetOrderedByLastNameAscendingAndIdDescending() throws SQLException, NoSuchFieldException {
    entityConfigurationReader.readConfiguration(new Class[] { EntityWithOrderByAnnotation.class });
    PropertyConfig relationsPropertyConfig = getPropertyConfigurationForField(EntityWithOrderByAnnotation.class, "relations");

    Assert.assertTrue(relationsPropertyConfig.hasOrderColumns());
    Assert.assertEquals(2, relationsPropertyConfig.getOrderColumns().size());

    com.j256.ormlite.stmt.query.OrderBy lastNameOrderBy = relationsPropertyConfig.getOrderColumns().get(0);
    Assert.assertEquals("lastName".toUpperCase(), lastNameOrderBy.getColumnName());
    Assert.assertTrue(lastNameOrderBy.isAscending());

    com.j256.ormlite.stmt.query.OrderBy idNameOrderBy = relationsPropertyConfig.getOrderColumns().get(1);
    Assert.assertEquals("id".toUpperCase(), idNameOrderBy.getColumnName());
    Assert.assertFalse(idNameOrderBy.isAscending());
  }


  @Entity
  static class ManyToManyEntityWithOrderByAnnotationButWithoutSpecifyingOrderColumnOwning {
    @Id protected Long id;

    @ManyToMany
    @OrderBy()
    protected Collection<ManyToManyEntityWithOrderByAnnotationButWithoutSpecifyingOrderColumnInverse> inverseSides;
  }

  @Entity
  static class ManyToManyEntityWithOrderByAnnotationButWithoutSpecifyingOrderColumnInverse {
    @Id protected Long id;
    protected String lastName;

    @ManyToMany(mappedBy = "inverseSides")
    protected Collection<ManyToManyEntityWithOrderByAnnotationButWithoutSpecifyingOrderColumnOwning> owningSides;
  }

  @Test
  public void manyToManyOrderByAnnotationSetButWithoutSpecifyingOrderColumn_RelationsGetOrderedByLastNameAscendingAndIdDescending() throws SQLException, NoSuchFieldException {
    entityConfigurationReader.readConfiguration(new Class[] { ManyToManyEntityWithOrderByAnnotationButWithoutSpecifyingOrderColumnOwning.class, ManyToManyEntityWithOrderByAnnotationButWithoutSpecifyingOrderColumnInverse.class });
    PropertyConfig relationsPropertyConfig = getPropertyConfigurationForField(ManyToManyEntityWithOrderByAnnotationButWithoutSpecifyingOrderColumnOwning.class, "inverseSides");

    Assert.assertTrue(relationsPropertyConfig.hasOrderColumns());
    Assert.assertEquals(1, relationsPropertyConfig.getOrderColumns().size());

    com.j256.ormlite.stmt.query.OrderBy lastNameOrderBy = relationsPropertyConfig.getOrderColumns().get(0);
    Assert.assertEquals("id".toUpperCase(), lastNameOrderBy.getColumnName()); // per default entities get ordered by their identity column name
    Assert.assertTrue(lastNameOrderBy.isAscending());
  }


  @Entity
  static class ManyToManyEntityWithOrderByAnnotationOwning {
    @Id protected Long id;

    @ManyToMany
    @OrderBy("lastName ASC, id DESC")
    protected Collection<ManyToManyEntityWithOrderByAnnotationInverse> inverseSides;
  }

  @Entity
  static class ManyToManyEntityWithOrderByAnnotationInverse {
    @Id protected Long id;
    protected String lastName;

    @ManyToMany(mappedBy = "inverseSides")
    protected Collection<ManyToManyEntityWithOrderByAnnotationOwning> owningSides;
  }

  @Test
  public void manyToManyOrderByAnnotationSet_RelationsGetOrderedByLastNameAscendingAndIdDescending() throws SQLException, NoSuchFieldException {
    entityConfigurationReader.readConfiguration(new Class[] { ManyToManyEntityWithOrderByAnnotationOwning.class, ManyToManyEntityWithOrderByAnnotationInverse.class });
    PropertyConfig relationsPropertyConfig = getPropertyConfigurationForField(ManyToManyEntityWithOrderByAnnotationOwning.class, "inverseSides");

    Assert.assertTrue(relationsPropertyConfig.hasOrderColumns());
    Assert.assertEquals(2, relationsPropertyConfig.getOrderColumns().size());

    com.j256.ormlite.stmt.query.OrderBy lastNameOrderBy = relationsPropertyConfig.getOrderColumns().get(0);
    Assert.assertEquals("lastName".toUpperCase(), lastNameOrderBy.getColumnName());
    Assert.assertTrue(lastNameOrderBy.isAscending());

    com.j256.ormlite.stmt.query.OrderBy idNameOrderBy = relationsPropertyConfig.getOrderColumns().get(1);
    Assert.assertEquals("id".toUpperCase(), idNameOrderBy.getColumnName());
    Assert.assertFalse(idNameOrderBy.isAscending());
  }

}
