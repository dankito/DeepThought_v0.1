package com.j256.ormlite.dao.cda.testmodel;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;

/**
 * Created by ganymed on 02/11/14.
 */
public class RelationEntities {

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
  public static class EagerOneSide extends BaseEntity {


    @OneToMany(targetEntity = EagerManySide.class, mappedBy = "oneSide", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    protected List<EagerManySide> manySides = new ArrayList<>();


    public EagerOneSide() { this(""); }

    public EagerOneSide(String name) {
      this.name = name;
    }


    public List<EagerManySide> getManySides() {
      return manySides;
    }
  }

  @Entity()
  public static class EagerManySide extends BaseEntity {

    @ManyToOne(targetEntity = EagerOneSide.class, fetch = FetchType.EAGER)
    @JoinColumn(name = "one_side_id")
    protected transient EagerOneSide oneSide;


    public EagerManySide() { this(""); }

    public EagerManySide(String name) {
      this.name = name;
    }


    public EagerOneSide getOneSide() {
      return oneSide;
    }
  }

  @Entity()
  public static class LazyOneSide extends BaseEntity {


    @OneToMany(targetEntity = LazyManySide.class, mappedBy = "oneSide", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    protected List<LazyManySide> manySides = new ArrayList<>();


    public LazyOneSide() { this(""); }

    public LazyOneSide(String name) {
      this.name = name;
    }


    public List<LazyManySide> getManySides() {
      return manySides;
    }
  }

  @Entity()
  public static class LazyManySide extends BaseEntity {

    @ManyToOne(targetEntity = LazyOneSide.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "one_side_id")
    protected transient LazyOneSide oneSide;


    public LazyManySide() { this(""); }

    public LazyManySide(String name) {
      this.name = name;
    }


    public LazyOneSide getOneSide() {
      return oneSide;
    }
  }

}
