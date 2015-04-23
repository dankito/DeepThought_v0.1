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
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;

/**
 * Created by ganymed on 16/11/14.
 */
public class CascadeEntities {

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
  
  
  @Entity
  public static class CascadeAllEagerOneSide extends BaseEntity {


    @OneToMany(mappedBy = "oneSide", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    protected List<CascadeAllEagerManySide> manySides = new ArrayList<>();


    public CascadeAllEagerOneSide() { this(""); }

    public CascadeAllEagerOneSide(String name) {
      this.name = name;
    }


    public List<CascadeAllEagerManySide> getManySides() {
      return manySides;
    }
  }

  @Entity
  public static class CascadeAllEagerManySide extends BaseEntity {

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "one_side_id")
    protected CascadeAllEagerOneSide oneSide;


    public CascadeAllEagerManySide() { this(""); }

    public CascadeAllEagerManySide(String name) {
      this.name = name;
    }


    public CascadeAllEagerOneSide getOneSide() {
      return oneSide;
    }
  }

  @Entity
  public static class CascadeAllLazyOneSide extends BaseEntity {


    @OneToMany(mappedBy = "oneSide", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    protected List<CascadeAllLazyManySide> manySides = new ArrayList<>();


    public CascadeAllLazyOneSide() { this(""); }

    public CascadeAllLazyOneSide(String name) {
      this.name = name;
    }


    public List<CascadeAllLazyManySide> getManySides() {
      return manySides;
    }
  }

  @Entity
  public static class CascadeAllLazyManySide extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "one_side_id")
    protected CascadeAllLazyOneSide oneSide;


    public CascadeAllLazyManySide() { this(""); }

    public CascadeAllLazyManySide(String name) {
      this.name = name;
    }


    public CascadeAllLazyOneSide getOneSide() {
      return oneSide;
    }
  }

  @Entity
  public static class CascadeAllEagerOwningSide extends BaseEntity {


    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    protected List<CascadeAllEagerInverseSide> inverseSides = new ArrayList<>();


    public CascadeAllEagerOwningSide() { this(""); }

    public CascadeAllEagerOwningSide(String name) {
      this.name = name;
    }


    public List<CascadeAllEagerInverseSide> getInverseSides() {
      return inverseSides;
    }
  }

  @Entity
  public static class CascadeAllEagerInverseSide extends BaseEntity {


    @ManyToMany(mappedBy = "inverseSides", fetch = FetchType.EAGER)
    protected List<CascadeAllEagerOwningSide> owningSides = new ArrayList<>();


    public CascadeAllEagerInverseSide() { this(""); }

    public CascadeAllEagerInverseSide(String name) {
      this.name = name;
    }


    public List<CascadeAllEagerOwningSide> getOwningSides() {
      return owningSides;
    }
  }

  @Entity
  public static class CascadeAllLazyOwningSide extends BaseEntity {


    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    protected List<CascadeAllLazyInverseSide> inverseSides = new ArrayList<>();


    public CascadeAllLazyOwningSide() { this(""); }

    public CascadeAllLazyOwningSide(String name) {
      this.name = name;
    }


    public List<CascadeAllLazyInverseSide> getInverseSides() {
      return inverseSides;
    }
  }

  @Entity
  public static class CascadeAllLazyInverseSide extends BaseEntity {


    @ManyToMany(mappedBy = "inverseSides", fetch = FetchType.LAZY)
    protected List<CascadeAllLazyOwningSide> owningSides = new ArrayList<>();


    public CascadeAllLazyInverseSide() { this(""); }

    public CascadeAllLazyInverseSide(String name) {
      this.name = name;
    }


    public List<CascadeAllLazyOwningSide> getOwningSides() {
      return owningSides;
    }
  }
}
