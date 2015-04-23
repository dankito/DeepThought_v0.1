package com.j256.ormlite.jpa.testmodel;


<<<<<<< HEAD:src/test/java/com/j256/ormlite/jpa/testmodel/Tag.java
=======
import net.deepthought.Application;
import net.deepthought.data.persistence.db.TableConfig;
import net.deepthought.data.persistence.db.UserDataEntity;
>>>>>>> 3bed7c67baed58edad591a0868ec5f516d3ad1e6:DeepThoughtLib/src/main/java/net/deepthought/data/model/Tag.java

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.OrderBy;
import javax.persistence.PostPersist;
import javax.persistence.Transient;

/**
 * Created by ganymed on 12/10/14.
 */
@Entity(name = TableConfig.TagTableName)
public class Tag extends UserDataEntity implements Comparable<Tag>, Serializable {

  private static final long serialVersionUID = 1204202485407318615L;


  @Column(name = TableConfig.TagNameColumnName)
  protected String name = "";

  @ManyToMany(fetch = FetchType.LAZY, mappedBy = "tags")
  @OrderBy("entryIndex DESC")
  protected Collection<Entry> entries = new ArrayList<>();

//  @ManyToOne(fetch = FetchType.EAGER)
//  @JoinColumn(name = TableConfig.TagDeepThoughtJoinColumnName)
//  protected DeepThought deepThought;



  public Tag() {

  }

  public Tag(String name) {
    this();
    this.name = name;
  }


  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public boolean hasEntries() {
    return getEntries().size() > 0;
  }

  public Collection<Entry> getEntries() {
    return entries;
  }

  protected boolean addEntry(Entry entry) {
//    boolean result = entries.add(entry);
//    callEntryAddedListeners(entry);
//    return result;

    if(entries instanceof List)
      ((List)entries).add(0, entry);
    else
      entries.add(entry);

    return true;
  }

  protected boolean removeEntry(Entry entry) {
    boolean result = entries.remove(entry);
    return result;
  }


//  public DeepThought getDeepThought() {
//    return deepThought;
//  }

  @Override
  public int compareTo(Tag other) {
    return name.toLowerCase().compareTo(other.getName().toLowerCase());
  }



  @Override
  @Transient
  public String getTextRepresentation() {
    return "Tag " + name + " (" + entries.size() + ")";
  }

  @Override
  public String toString() {
    return getTextRepresentation();
  }


  @PostPersist
  protected void postPersist() {
    if(Application.getSearchEngine() != null)
      Application.getSearchEngine().indexEntity(this);
  }

  @Override
  protected void callPropertyChangedListeners(String propertyName, Object previousValue, Object newValue) {
    super.callPropertyChangedListeners(propertyName, previousValue, newValue);

    Application.getSearchEngine().updateIndexForEntity(this, propertyName);
  }

}
