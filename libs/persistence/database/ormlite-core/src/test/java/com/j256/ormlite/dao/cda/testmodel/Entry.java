package com.j256.ormlite.dao.cda.testmodel;

import java.util.ArrayList;
import java.util.Collection;

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

/**
 * Created by ganymed on 16/10/14.
 */
@Entity(name = "entry")
public class Entry {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  protected Long id;

  @Column(name = "name")
  protected String name = "";

  @ManyToOne(targetEntity = Category.class, fetch = FetchType.EAGER, cascade = CascadeType.ALL)
  @JoinColumn(name = "category_id")
  protected Category category;

  @ManyToMany(fetch = FetchType.LAZY, targetEntity = Keyword.class, cascade = CascadeType.ALL)
  protected Collection keywords = new ArrayList<>();

  public Entry() { }

  public Entry(String name) {
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

  public Category getCategory() {
    return category;
  }

  public Collection<Keyword> getKeywords() {
    return keywords;
  }


  @Override
  public String toString() {
    return name + " (" + keywords.size() + " keywords)";
  }

}
