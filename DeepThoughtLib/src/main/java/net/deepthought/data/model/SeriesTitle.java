package net.deepthought.data.model;

import net.deepthought.data.persistence.db.TableConfig;

import java.io.Serializable;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

/**
 * Created by ganymed on 16/12/14.
 */
@Entity(name = TableConfig.SeriesTitleTableName)
@DiscriminatorValue(TableConfig.SeriesTitleDiscriminatorValue)
public class SeriesTitle extends ReferenceBase implements Serializable, Comparable<SeriesTitle> {

  private static final long serialVersionUID = 876365664840769897L;


  @OneToMany(fetch = FetchType.LAZY, mappedBy = "series")
  protected Set<Reference> serialParts = new HashSet<>();

  protected transient SortedSet<Reference> serialPartsSorted = null;

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "series")
  protected Set<Entry> entries = new HashSet<>();

  @Column(name = TableConfig.SeriesTitleTableOfContentsColumnName)
  @Lob
  protected String tableOfContents;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = TableConfig.SeriesTitleDeepThoughtJoinColumnName)
  protected DeepThought deepThought;


  public SeriesTitle() {

  }

  public SeriesTitle(String title) {
    super(title);
  }

  public SeriesTitle(String title, String subTitle) {
    super(title, subTitle);
  }


  public boolean hasSerialParts() {
    return getSerialParts().size() > 0;
  }

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


      callEntityAddedListeners(serialParts, serialPart);
      return true;
    }

    return false;
  }

  public boolean removeSerialPart(Reference serialPart) {
    if(containsSerialParts(serialPart) == false)
      return false;

    int removeIndex = serialPart.getSeriesOrder();

    if(serialParts.remove(serialPart)) {
      serialPart.setSeries(null);
      serialPartsSorted = null;

      for(Reference serialPartEnum : serialParts) {
        if(serialPartEnum.getSeriesOrder() > removeIndex)
          serialPartEnum.setSeriesOrder(serialPartEnum.getSeriesOrder() - 1);
      }

      callEntityRemovedListeners(serialParts, serialPart);
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


  public boolean hasEntries() {
    return getEntries().size() > 0;
  }

  public Set<Entry> getEntries() {
    return entries;
  }

  protected boolean addEntry(Entry entry) {
    if(entries.add(entry)) {
      callEntityAddedListeners(entries, entry);
      return true;
    }

    return false;
  }

  protected boolean removeEntry(Entry entry) {
    if(entries.remove(entry)) {
      callEntityRemovedListeners(entries, entry);
      return true;
    }

    return false;
  }


  public String getTableOfContents() {
    return tableOfContents;
  }

  public void setTableOfContents(String tableOfContents) {
    Object previousValue = this.tableOfContents;
    this.tableOfContents = tableOfContents;
    callPropertyChangedListeners(TableConfig.SeriesTitleTableOfContentsColumnName, previousValue, tableOfContents);
  }

  @Override
  public boolean addAttachedFile(FileLink file) {
    if(super.addAttachedFile(file)) {
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
  }

  protected void setDeepThought(DeepThought deepThought) {
    Object previousValue = this.deepThought;
    this.deepThought = deepThought;
    callPropertyChangedListeners(TableConfig.SeriesTitleDeepThoughtJoinColumnName, previousValue, deepThought);
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
