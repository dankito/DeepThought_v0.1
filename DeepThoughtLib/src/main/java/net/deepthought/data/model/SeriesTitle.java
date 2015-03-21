package net.deepthought.data.model;

import net.deepthought.data.model.enums.SeriesTitleCategory;
import net.deepthought.data.persistence.db.TableConfig;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Date;
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
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * Created by ganymed on 16/12/14.
 */
@Entity(name = TableConfig.SeriesTitleTableName)
@DiscriminatorValue(TableConfig.SeriesTitleDiscriminatorValue)
public class SeriesTitle extends ReferenceBase implements Serializable, Comparable<SeriesTitle> {

  private static final long serialVersionUID = 876365664840769897L;


  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = TableConfig.SeriesTitleCategoryJoinColumnName)
  protected SeriesTitleCategory category;

  @OneToMany(fetch = FetchType.EAGER, mappedBy = "series")
  protected Set<Reference> serialParts = new HashSet<>();

  protected transient SortedSet<Reference> serialPartsSorted = null;

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "series")
  protected Set<Entry> entries = new HashSet<>();

  @Column(name = TableConfig.SeriesTitleTitleSupplementColumnName)
  protected String titleSupplement;

  @Column(name = TableConfig.SeriesTitleTableOfContentsColumnName)
  @Lob
  protected String tableOfContents;

  @Column(name = TableConfig.SeriesTitleFirstDayOfPublicationColumnName)
  @Temporal(TemporalType.TIMESTAMP)
  protected Date firstDayOfPublication; // SQLite needs this, can't handle null dates

  @Column(name = TableConfig.SeriesTitleLastDayOfPublicationColumnName)
  @Temporal(TemporalType.TIMESTAMP)
  protected Date lastDayOfPublication; // SQLite needs this, can't handle null dates

  @Column(name = TableConfig.SeriesTitleStandardAbbreviationColumnName)
  protected String standardAbbreviation;

  @Column(name = TableConfig.SeriesTitleUserAbbreviation1ColumnName)
  protected String userAbbreviation1;

  @Column(name = TableConfig.SeriesTitleUserAbbreviation2ColumnName)
  protected String userAbbreviation2;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = TableConfig.SeriesTitlePublisherJoinColumnName)
  protected Publisher publisher;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = TableConfig.PersonDeepThoughtJoinColumnName)
  protected DeepThought deepThought;


  public SeriesTitle() {

  }

  public SeriesTitle(String title) {
    super(title);
  }

  public SeriesTitle(String title, String subTitle) {
    super(title, subTitle);
  }


  public SeriesTitleCategory getCategory() {
    return category;
  }

  public void setCategory(SeriesTitleCategory category) {
    Object previousValue = this.category;
    if(this.category != null)
      this.category.removeSeries(this);

    this.category = category;

    if(this.category != null)
      this.category.addSeries(this);

    callPropertyChangedListeners(TableConfig.SeriesTitleCategoryJoinColumnName, previousValue, category);
  }

  public Set<Reference> getSerialParts() {
    return serialParts;
  }

  public boolean addSerialPart(Reference serialPart) {
    if(containsSerialParts(serialPart) == false && serialParts.add(serialPart)) {
      serialPartsSorted = null;
      if(this.equals(serialPart.getSeries()) == false) {
        serialPart.setSeries(this); // causes a Stackoverflow
//      serialPart.series = this;
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


  public String getTitleSupplement() {
    return titleSupplement;
  }

  public void setTitleSupplement(String titleSupplement) {
    Object previousValue = this.titleSupplement;
    this.titleSupplement = titleSupplement;
    callPropertyChangedListeners(TableConfig.SeriesTitleTitleSupplementColumnName, previousValue, titleSupplement);
  }

  public String getTableOfContents() {
    return tableOfContents;
  }

  public void setTableOfContents(String tableOfContents) {
    Object previousValue = this.tableOfContents;
    this.tableOfContents = tableOfContents;
    callPropertyChangedListeners(TableConfig.SeriesTitleTableOfContentsColumnName, previousValue, tableOfContents);
  }

  public Date getFirstDayOfPublication() {
    return firstDayOfPublication;
  }

  public void setFirstDayOfPublication(Date firstDayOfPublication) {
    Object previousValue = this.firstDayOfPublication;
    this.firstDayOfPublication = firstDayOfPublication;
    callPropertyChangedListeners(TableConfig.SeriesTitleFirstDayOfPublicationColumnName, previousValue, firstDayOfPublication);
  }

  public Date getLastDayOfPublication() {
    return lastDayOfPublication;
  }

  public void setLastDayOfPublication(Date lastDayOfPublication) {
    Object previousValue = this.lastDayOfPublication;
    this.lastDayOfPublication = lastDayOfPublication;
    callPropertyChangedListeners(TableConfig.SeriesTitleLastDayOfPublicationColumnName, previousValue, lastDayOfPublication);
  }

  public String getStandardAbbreviation() {
    return standardAbbreviation;
  }

  public void setStandardAbbreviation(String standardAbbreviation) {
    Object previousValue = this.standardAbbreviation;
    this.standardAbbreviation = standardAbbreviation;
    callPropertyChangedListeners(TableConfig.SeriesTitleStandardAbbreviationColumnName, previousValue, standardAbbreviation);
  }

  public String getUserAbbreviation1() {
    return userAbbreviation1;
  }

  public void setUserAbbreviation1(String userAbbreviation1) {
    Object previousValue = this.userAbbreviation1;
    this.userAbbreviation1 = userAbbreviation1;
    callPropertyChangedListeners(TableConfig.SeriesTitleUserAbbreviation1ColumnName, previousValue, userAbbreviation1);
  }

  public String getUserAbbreviation2() {
    return userAbbreviation2;
  }

  public void setUserAbbreviation2(String userAbbreviation2) {
    Object previousValue = this.userAbbreviation2;
    this.userAbbreviation2 = userAbbreviation2;
    callPropertyChangedListeners(TableConfig.SeriesTitleUserAbbreviation2ColumnName, previousValue, userAbbreviation2);
  }

  public Publisher getPublisher() {
    return publisher;
  }

  public void setPublisher(Publisher publisher) {
    Object previousValue = this.publisher;
    if(this.publisher != null)
      this.publisher.removeSeriesTitle(this);

    this.publisher = publisher;

    if(publisher != null)
      publisher.addSeriesTitle(this);

    callPropertyChangedListeners(TableConfig.SeriesTitlePublisherJoinColumnName, previousValue, publisher);
  }

  public DeepThought getDeepThought() {
    return deepThought;
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
