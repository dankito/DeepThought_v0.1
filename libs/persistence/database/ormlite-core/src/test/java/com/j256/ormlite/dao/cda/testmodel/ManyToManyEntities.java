package com.j256.ormlite.dao.cda.testmodel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.MappedSuperclass;

/**
 * Created by ganymed on 02/11/14.
 */
public class ManyToManyEntities {

  @MappedSuperclass()
  public abstract static class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    @Column(name = "name")
    protected String name = "";

    protected BaseEntity() { }

    protected BaseEntity(String name) {
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

  @Entity()
  public static class EagerOwningSide extends BaseEntity {


    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    protected List<EagerInverseSide> inverseSides = new ArrayList<>();


    public EagerOwningSide() { this(""); }

    public EagerOwningSide(String name) {
      this.name = name;
    }


    public List<EagerInverseSide> getInverseSides() {
      return inverseSides;
    }
  }

  @Entity()
  public static class EagerInverseSide extends BaseEntity {


    @ManyToMany(mappedBy = "inverseSides", fetch = FetchType.EAGER)
    protected List<EagerOwningSide> owningSides = new ArrayList<>();


    public EagerInverseSide() { this("");}

    public EagerInverseSide(String name) {
      this.name = name;
    }


    public List<EagerOwningSide> getOwningSides() {
      return owningSides;
    }
  }

  @Entity()
  public static class LazyOwningSide extends BaseEntity {


    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    protected List<LazyInverseSide> inverseSides = new ArrayList<>();


    protected LazyOwningSide() { }

    public LazyOwningSide(String name) {
      this.name = name;
    }


    public List<LazyInverseSide> getInverseSides() {
      return inverseSides;
    }
  }

  @Entity()
  public static class LazyInverseSide extends BaseEntity {


    @ManyToMany(mappedBy = "inverseSides", fetch = FetchType.LAZY)
    protected List<LazyOwningSide> owningSides = new ArrayList<>();


    protected LazyInverseSide() { }

    public LazyInverseSide(String name) {
      this.name = name;
    }


    public List<LazyOwningSide> getOwningSides() {
      return owningSides;
    }
  }

  @Entity()
  public static class NonGenericSetOwningSide extends BaseEntity {

    @ManyToMany(targetEntity=NonGenericSetInverseSide.class, cascade = CascadeType.ALL)
    protected Set inverseSides = new HashSet();


    protected NonGenericSetOwningSide() { }

    public NonGenericSetOwningSide(String name) {
      super(name);
    }

    public Set getInverseSides() {
      return inverseSides;
    }
  }

  @Entity()
  public static class NonGenericSetInverseSide extends BaseEntity {


    @ManyToMany(targetEntity=NonGenericSetOwningSide.class, mappedBy = "inverseSides")
    protected Set owningSides = new HashSet();


    protected NonGenericSetInverseSide() { }

    public NonGenericSetInverseSide(String name) {
      this.name = name;
    }


    public Set getOwningSides() {
      return owningSides;
    }
  }

  @Entity()
  public static class NonGenericSetTargetEntityInAnnotationMissingOwningSide extends BaseEntity {

    @ManyToMany()
    protected Set inverseSides = new HashSet();


    protected NonGenericSetTargetEntityInAnnotationMissingOwningSide() { }

    public NonGenericSetTargetEntityInAnnotationMissingOwningSide(String name) {
      super(name);
    }

    public Set getInverseSides() {
      return inverseSides;
    }
  }

  @Entity()
  public static class NonGenericSetTargetEntityInAnnotationMissingInverseSide extends BaseEntity {


    @ManyToMany(mappedBy = "inverseSides")
    protected Set owningSides = new HashSet();


    protected NonGenericSetTargetEntityInAnnotationMissingInverseSide() { }

    public NonGenericSetTargetEntityInAnnotationMissingInverseSide(String name) {
      this.name = name;
    }


    public Set getOwningSides() {
      return owningSides;
    }
  }

  @Entity()
  public static class NonGenericSetMappedByInAnnotationMissingOwningSide extends BaseEntity {

    @ManyToMany(targetEntity=NonGenericSetMappedByInAnnotationMissingInverseSide.class)
    protected Set inverseSides = new HashSet();


    protected NonGenericSetMappedByInAnnotationMissingOwningSide() { }

    public NonGenericSetMappedByInAnnotationMissingOwningSide(String name) {
      super(name);
    }

    public Set getInverseSides() {
      return inverseSides;
    }
  }

  @Entity()
  public static class NonGenericSetMappedByInAnnotationMissingInverseSide extends BaseEntity {


    @ManyToMany(targetEntity =  NonGenericSetMappedByInAnnotationMissingOwningSide.class)
    protected Set owningSides = new HashSet();


    protected NonGenericSetMappedByInAnnotationMissingInverseSide() { }

    public NonGenericSetMappedByInAnnotationMissingInverseSide(String name) {
      this.name = name;
    }


    public Set getOwningSides() {
      return owningSides;
    }
  }

  @Entity()
  public static class GenericCollectionMappedByInAnnotationMissingOwningSide extends BaseEntity {

    @ManyToMany()
    protected Collection<GenericCollectionMappedByInAnnotationMissingInverseSide> inverseSides = new HashSet();


    protected GenericCollectionMappedByInAnnotationMissingOwningSide() { }

    public GenericCollectionMappedByInAnnotationMissingOwningSide(String name) {
      super(name);
    }

    public Collection<GenericCollectionMappedByInAnnotationMissingInverseSide> getInverseSides() {
      return inverseSides;
    }
  }

  @Entity()
  public static class GenericCollectionMappedByInAnnotationMissingInverseSide extends BaseEntity {


    @ManyToMany()
    protected Collection<GenericCollectionMappedByInAnnotationMissingOwningSide> owningSides = new HashSet();


    protected GenericCollectionMappedByInAnnotationMissingInverseSide() { }

    public GenericCollectionMappedByInAnnotationMissingInverseSide(String name) {
      this.name = name;
    }


    public Collection<GenericCollectionMappedByInAnnotationMissingOwningSide> getOwningSides() {
      return owningSides;
    }
  }
}
