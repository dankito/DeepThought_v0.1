<<<<<<< HEAD:src/test/java/com/j256/ormlite/jpa/testmodel/ReferenceSubDivision.java
package com.j256.ormlite.jpa.testmodel;
=======
package net.deepthought.data.model;

import net.deepthought.data.persistence.db.TableConfig;
>>>>>>> 3bed7c67baed58edad591a0868ec5f516d3ad1e6:DeepThoughtLib/src/main/java/net/deepthought/data/model/ReferenceSubDivision.java

import java.util.ArrayList;
import java.util.Collection;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Transient;

/**
 * Created by ganymed on 21/01/15.
 */
@Entity(name = TableConfig.ReferenceSubDivisionTableName)
@DiscriminatorValue(TableConfig.ReferenceSubDivisionDiscriminatorValue)
public class ReferenceSubDivision extends ReferenceBase implements Comparable<ReferenceSubDivision> {

  private static final long serialVersionUID = -967783983570745569L;


<<<<<<< HEAD:src/test/java/com/j256/ormlite/jpa/testmodel/ReferenceSubDivision.java
//  @ManyToOne(fetch = FetchType.EAGER)
//  @JoinColumn(name = TableConfig.ReferenceSubDivisionCategoryJoinColumnName)
//  protected ReferenceSubDivisionCategory category;

=======
>>>>>>> 3bed7c67baed58edad591a0868ec5f516d3ad1e6:DeepThoughtLib/src/main/java/net/deepthought/data/model/ReferenceSubDivision.java
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = TableConfig.ReferenceSubDivisionReferenceJoinColumnName)
  protected Reference reference;

  @Column(name = TableConfig.ReferenceSubDivisionOrderColumnName)
  protected int subDivisionOrder; // Chapter number, number of Article, ...

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = TableConfig.ReferenceSubDivisionParentSubDivisionJoinColumnName)
  protected ReferenceSubDivision parentSubDivision;

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "parentSubDivision"/*, cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }*/)
  @OrderBy("subDivisionOrder ASC") // TODO: subDivisions afterwards don't get sorted
  protected Collection<ReferenceSubDivision> subDivisions = new ArrayList<>();

<<<<<<< HEAD:src/test/java/com/j256/ormlite/jpa/testmodel/ReferenceSubDivision.java
//  @OneToMany(fetch = FetchType.LAZY, mappedBy = "referenceSubDivision")
//  protected Collection<Entry> entries = new HashSet<>();
=======
  @OneToMany(fetch = FetchType.LAZY, mappedBy = "referenceSubDivision")
  protected Collection<Entry> entries = new HashSet<>();

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = TableConfig.ReferenceSubDivisionDeepThoughtJoinColumnName)
  protected DeepThought deepThought;
>>>>>>> 3bed7c67baed58edad591a0868ec5f516d3ad1e6:DeepThoughtLib/src/main/java/net/deepthought/data/model/ReferenceSubDivision.java



  public ReferenceSubDivision() {

  }

  public ReferenceSubDivision(String title) {
    super(title);
  }

<<<<<<< HEAD:src/test/java/com/j256/ormlite/jpa/testmodel/ReferenceSubDivision.java
=======
  public ReferenceSubDivision(String title, String subTitle) {
    super(title, subTitle);
  }



  public Collection<Entry> getEntries() {
    return entries;
  }

  protected boolean addEntry(Entry entry) {
    if(entries.add(entry)) {
      callEntityAddedListeners(entries, entry);
      return true;
    }
>>>>>>> 3bed7c67baed58edad591a0868ec5f516d3ad1e6:DeepThoughtLib/src/main/java/net/deepthought/data/model/ReferenceSubDivision.java


//  public ReferenceSubDivisionCategory getCategory() {
//    return category;
//  }
//
//  public void setCategory(ReferenceSubDivisionCategory category) {if(this.category != null)
//    this.category.removeReferenceSubDivision(this);
//
//    Object previousValue = this.category;
//    this.category = category;
//
//    if(this.category != null)
//      this.category.addReferenceSubDivision(this);
//
//    callPropertyChangedListeners(TableConfig.ReferenceCategoryJoinColumnName, previousValue, category);
//  }

//  public Collection<Entry> getEntries() {
//    return entries;
//  }
//
//  protected boolean addEntry(Entry entry) {
//    if(entries.add(entry)) {
//      return true;
//    }
//
//    return false;
//  }
//
//  protected boolean removeEntry(Entry entry) {
//    if(entries.remove(entry)) {
//      return true;
//    }
//
//    return false;
//  }

  public Reference getReference() {
    return reference;
  }

  protected void setReference(Reference reference) {
    this.reference = reference;
  }

  public ReferenceSubDivision getParentSubDivision() {
    return parentSubDivision;
  }

  public boolean hasSubDivisions() {
    return getSubDivisions().size() > 0;
  }

  public Collection<ReferenceSubDivision> getSubDivisions() {
    return subDivisions;
  }

  public boolean addSubDivision(ReferenceSubDivision subDivision) {
    subDivision.parentSubDivision = this;
    subDivision.subDivisionOrder = subDivisions.size();
    if(subDivision.reference == null && this.reference != null)
      subDivision.reference = this.reference;

    if(subDivision.getDeepThought() == null && this.deepThought != null)
      deepThought.addReferenceSubDivision(subDivision);

    boolean result = subDivisions.add(subDivision);

    return result;
  }

  public boolean removeSubDivision(ReferenceSubDivision subDivision) {
    int removeSubDivisionIndex = subDivision.getSubDivisionOrder();

    boolean result = subDivisions.remove(subDivision);
    if(result) {
      subDivision.parentSubDivision = null;

//      for(Entry entry : new ArrayList<>(subDivision.getEntries()))
//        entry.setReferenceSubDivision(null);

      for(ReferenceSubDivision subDivisionEnum : subDivisions) {
        if(subDivisionEnum.getSubDivisionOrder() >= removeSubDivisionIndex)
          subDivisionEnum.setSubDivisionOrder(subDivisionEnum.getSubDivisionOrder() - 1);
      }
    }

    return result;
  }

  public int getSubDivisionOrder() {
    return subDivisionOrder;
  }

  public void setSubDivisionOrder(int subDivisionOrder) {
    this.subDivisionOrder = subDivisionOrder;
<<<<<<< HEAD:src/test/java/com/j256/ormlite/jpa/testmodel/ReferenceSubDivision.java
=======
    callPropertyChangedListeners(TableConfig.ReferenceSubDivisionOrderColumnName, previousValue, subDivisionOrder);
  }

  @Override
  public boolean addFile(FileLink file) {
    if(super.addFile(file)) {
      if(file.getDeepThought() == null && this.deepThought != null)
        deepThought.addFile(file);
      return true;
    }
    return false;
  }

  @Override
  public void setPreviewImage(FileLink previewImage) {
    if(previewImage.getDeepThought() == null && this.deepThought != null)
      deepThought.addFile(previewImage);

    super.setPreviewImage(previewImage);
  }

  public DeepThought getDeepThought() {
    return deepThought;
>>>>>>> 3bed7c67baed58edad591a0868ec5f516d3ad1e6:DeepThoughtLib/src/main/java/net/deepthought/data/model/ReferenceSubDivision.java
  }


  @Override
  @Transient
  public String getTextRepresentation() {
<<<<<<< HEAD:src/test/java/com/j256/ormlite/jpa/testmodel/ReferenceSubDivision.java
    return "ReferenceSubDivision " + title;
=======
    if(reference != null)
      return reference.getTextRepresentation() + " - " + super.getTextRepresentation();

    return super.getTextRepresentation();
  }

  @Override
  public String toString() {
    return "ReferenceSubDivision " + getTextRepresentation();
>>>>>>> 3bed7c67baed58edad591a0868ec5f516d3ad1e6:DeepThoughtLib/src/main/java/net/deepthought/data/model/ReferenceSubDivision.java
  }

  @Override
  public int compareTo(ReferenceSubDivision other) {
    if(other == null)
      return 1;

    return ((Integer) getSubDivisionOrder()).compareTo(other.getSubDivisionOrder());
  }
}
