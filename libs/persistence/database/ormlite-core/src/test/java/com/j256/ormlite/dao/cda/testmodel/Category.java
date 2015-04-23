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
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

/**
 * Created by ganymed on 16/10/14.
 */
@Entity(name = "category")
public class Category {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  protected Long id;

  @Column(name = "name")
  protected String name = "";

  @OneToMany(targetEntity = Entry.class, mappedBy = "category", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
  protected Collection<Entry> entries = new ArrayList<>();

  @OneToMany(targetEntity = Category.class, mappedBy = "parentCategory", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  protected Collection<Category> subCategories = new ArrayList<>();

  @ManyToOne(targetEntity = Category.class, fetch = FetchType.EAGER)
  @JoinColumn(name = "parent_category_id")
  protected Category parentCategory;


  public Category() { }

  public Category(String name) {
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

  public Collection<Entry> getEntries() {
    return entries;
  }

  public Collection<Category> getSubCategories() {
    return subCategories;
  }

  public Category getParentCategory() {
    return parentCategory;
  }


  @Override
  public String toString() {
    return name + " (" + entries.size() + " entries)";
  }

}
