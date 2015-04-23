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
import javax.persistence.ManyToMany;

/**
 * Created by ganymed on 16/10/14.
 */
@Entity(name = "keyword")
public class Keyword {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  protected Long id;

  @Column(name = "name")
  protected String name = "";


  @ManyToMany(fetch = FetchType.LAZY, mappedBy = "keywords", cascade = CascadeType.ALL)
  protected List<Entry> entries = new ArrayList<>();


  public Keyword() {

  }

  public Keyword(String name) {
    this.name = name;
  }


  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public List<Entry> getEntries() {
    return entries;
  }


  @Override
  public String toString() {
    return name + " (" + entries.size() + " entries)";
  }

}
