package com.j256.ormlite.jpa.testmodel;

import java.io.Serializable;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

/**
 * Created by ganymed on 16/12/14.
 */
@Entity(name = TableConfig.SeriesTitleTableName)
@DiscriminatorValue(TableConfig.SeriesTitleDiscriminatorValue)
public class SeriesTitle extends ReferenceBase implements Serializable, Comparable<SeriesTitle> {

  private static final long serialVersionUID = 876365664840769897L;


//  @ManyToOne(fetch = FetchType.EAGER)
//  @JoinColumn(name = TableConfig.SeriesTitleCategoryJoinColumnName)
//  protected SeriesTitleCategory category;

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "series", cascade = {CascadeType.PERSIST })
  protected Set<Reference> serialParts = new HashSet<>();

  protected transient SortedSet<Reference> serialPartsSorted = null;

//  @OneToMany(fetch = FetchType.LAZY, mappedBy = "series")
//  protected Set<Entry> entries = new HashSet<>();

  @Column(name = TableConfig.SeriesTitleTableOfContentsColumnName)
  @Lob
  protected String tableOfContents;

//  @ManyToOne(fetch = FetchType.EAGER)
//  @JoinColumn(name = TableConfig.PersonDeepThoughtJoinColumnName)
//  protected DeepThought deepThought;


  public SeriesTitle() {

  }

  public SeriesTitle(String title) {
    super(title);
  }


//  public SeriesTitleCategory getCategory() {
//    return category;
//  }
//
//  public void setCategory(SeriesTitleCategory category) {
//    Object previousValue = this.category;
//    if(this.category != null)
//      this.category.removeSeries(this);
//
//    this.category = category;
//
//    if(this.category != null)
//      this.category.addSeries(this);
//
//    callPropertyChangedListeners(TableConfig.SeriesTitleCategoryJoinColumnName, previousValue, category);
//  }

  public Set<Reference> getSerialParts() {
    return serialParts;
  }

  public boolean addSerialPart(Reference serialPart) {
    if(containsSerialParts(serialPart) == false && serialParts.add(serialPart)) {
      serialPartsSorted = null;
      if(this.equals(serialPart.getSeries()) == false) {
        serialPart.setSeries(this);
        serialPart.setSeriesOrder(serialParts.size() - 1);
      }

      return true;
    }

    return false;
  }

  public boolean removeSerialPart(Reference serialPart) {
    int removeIndex = serialPart.getSeriesOrder();

    if(serialParts.remove(serialPart)) {
      serialPart.setSeries(null);
      serialPartsSorted = null;

      for(Reference serialPartEnum : serialParts) {
        if(serialPartEnum.getSeriesOrder() > removeIndex)
          serialPartEnum.setSeriesOrder(serialPartEnum.getSeriesOrder() - 1);
      }
      return true;
    }

    return false;
  }

  public boolean containsSerialParts(Reference reference) {
    return serialParts.contains(reference);
  }

  public SortedSet<Reference> getSerialPartsSorted() {
    if(serialPartsSorted == null) {
      serialPartsSorted = new TreeSet<>(SerialPartsBySeriesOrderComparator);
      serialPartsSorted.addAll(getSerialParts());
    }
    return serialPartsSorted;
  }

//  public Set<Entry> getEntries() {
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


  public String getTableOfContents() {
    return tableOfContents;
  }

  public void setTableOfContents(String tableOfContents) {
    this.tableOfContents = tableOfContents;
  }

//  public DeepThought getDeepThought() {
//    return deepThought;
//  }

  @Override
  @Transient
  public String getTextRepresentation() {
    return title;
  }

  @Override
  public String toString() {
    return "SeriesTitle " + getTextRepresentation();
  }

  @Override
  public int compareTo(SeriesTitle other) {
    if(other == null)
      return 1;

    return getTitle().compareTo(other.getTitle());
  }

  public final static Comparator<Reference> SerialPartsBySeriesOrderComparator = new Comparator<Reference>() {
    @Override
    public int compare(Reference reference1, Reference reference2) {
      return ((Integer)reference1.getSeriesOrder()).compareTo(reference2.getSeriesOrder());
    }
  };

}
