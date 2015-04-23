package com.j256.ormlite.dao.cda.testmodel;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;

/**
 * Created by ganymed on 16/11/14.
 */
public class InheritanceModel {

  public final static String SingleTableInheritanceBaseEntityTableName = "SINGLETABLE";

  public final static String JoinedTableInheritanceBaseEntityTableName = "JOINED";
  public final static String JoinedTableInheritanceFirstDirectSubEntityTableName = "FIRST_DIRECT_SUB";
  public final static String JoinedTableInheritanceFirstDirectSubChildEntityTableName = "FIRST_DIRECT_SUB_CHILD";
  public final static String JoinedTableInheritanceSecondDirectSubEntityTableName = "SECOND_DIRECT_SUB";

  public final static int JoinedTableInheritanceBaseEntityColumnsCount = 2;
  public final static int JoinedTableInheritanceFirstDirectSubEntityColumnsCount = 1;
  public final static int JoinedTableInheritanceFirstDirectSubChildEntityColumnsCount = 1;
  public final static int JoinedTableInheritanceSecondDirectSubEntityColumnsCount = 1;

  public final static String DiscriminatorColumnName = "DISCRIMINATOR";

  public final static String JoinedTableInheritanceBaseEntityDiscriminatorValue = "BASE";
  public final static String JoinedTableInheritanceFirstDirectSubEntityDiscriminatorValue = "FIRST_SUB";
  public final static String JoinedTableInheritanceFirstDirectSubChildEntityDiscriminatorValue = "FIRST_SUB_CHILD";
  public final static String JoinedTableInheritanceSecondDirectSubEntityDiscriminatorValue = "SECOND_SUB";

  @Entity
  @Table(name = SingleTableInheritanceBaseEntityTableName)
  @Inheritance(strategy = InheritanceType.SINGLE_TABLE)
  public static class SingleTableInheritanceBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    @Column
    protected String name;


    public SingleTableInheritanceBaseEntity() {

    }

    public SingleTableInheritanceBaseEntity(String name) {
      this.name = name;
    }

    public Long getId() {
      return id;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    @Override
    public String toString() {
      return name;
    }
  }

  @Entity
  public static class SingleTableFirstDirectSubEntity extends SingleTableInheritanceBaseEntity {

    @Column
    protected boolean isSubEntity;


    public SingleTableFirstDirectSubEntity() {

    }

    public SingleTableFirstDirectSubEntity(String name) {
      super(name);
    }

  }

  @Entity
  public static class SingleTableFirstDirectSubEntityChild extends SingleTableFirstDirectSubEntity {

    @Column
    protected int iAmALeaf;


    public SingleTableFirstDirectSubEntityChild() {

    }

    public SingleTableFirstDirectSubEntityChild(String name) {
      super(name);
    }
  }

  @Entity
  public static class SingleTableSecondDirectSubEntity extends SingleTableInheritanceBaseEntity {

    @Column
    protected String title;


    public SingleTableSecondDirectSubEntity() {

    }

    public SingleTableSecondDirectSubEntity(String name) {
      super(name);
    }

  }


  @Entity
  @Table(name = JoinedTableInheritanceBaseEntityTableName)
  @Inheritance(strategy = InheritanceType.JOINED)
  @DiscriminatorColumn(name = DiscriminatorColumnName, discriminatorType = DiscriminatorType.STRING, length = 20)
  @DiscriminatorValue(JoinedTableInheritanceBaseEntityDiscriminatorValue)
  public static class JoinedTableInheritanceBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    @Column
    protected String name;


    public JoinedTableInheritanceBaseEntity() {

    }

    public JoinedTableInheritanceBaseEntity(String name) {
      this.name = name;
    }

    public Long getId() {
      return id;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    @Override
    public String toString() {
      return name;
    }
  }

  @Entity
  @Table(name = JoinedTableInheritanceFirstDirectSubEntityTableName)
  @DiscriminatorValue(JoinedTableInheritanceFirstDirectSubEntityDiscriminatorValue)
  public static class JoinedTableFirstDirectSubEntity extends JoinedTableInheritanceBaseEntity {

    @Column
    protected boolean isSubEntity;


    public JoinedTableFirstDirectSubEntity() {
      this.isSubEntity = true;
    }

    public JoinedTableFirstDirectSubEntity(String name) {
      super(name);
      this.isSubEntity = true;
    }

  }

  @Entity
  @Table(name = JoinedTableInheritanceFirstDirectSubChildEntityTableName)
  @DiscriminatorValue(JoinedTableInheritanceFirstDirectSubChildEntityDiscriminatorValue)
  public static class JoinedTableFirstDirectSubEntityChild extends JoinedTableFirstDirectSubEntity {

    @Column
    protected int iAmALeaf;


    public JoinedTableFirstDirectSubEntityChild() {

    }

    public JoinedTableFirstDirectSubEntityChild(String name) {
      super(name);
    }

    public JoinedTableFirstDirectSubEntityChild(String name, int iAmALeaf) {
      this(name);
      this.iAmALeaf = iAmALeaf;
    }
  }

  @Entity
  @Table(name = JoinedTableInheritanceSecondDirectSubEntityTableName)
  @DiscriminatorValue(JoinedTableInheritanceSecondDirectSubEntityDiscriminatorValue)
  public static class JoinedTableSecondDirectSubEntity extends JoinedTableInheritanceBaseEntity {

    @Column
    protected String title;


    public JoinedTableSecondDirectSubEntity() {

    }

    public JoinedTableSecondDirectSubEntity(String name) {
      super(name);
    }

    public JoinedTableSecondDirectSubEntity(String name, String title) {
      this(name);
      this.title = title;
    }

  }
}
