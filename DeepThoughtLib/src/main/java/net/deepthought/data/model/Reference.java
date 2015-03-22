package net.deepthought.data.model;

import net.deepthought.Application;
import net.deepthought.data.model.enums.Language;
import net.deepthought.data.model.enums.ReferenceCategory;
import net.deepthought.data.model.enums.ReferenceIndicationUnit;
import net.deepthought.data.persistence.db.TableConfig;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

/**
 * Created by ganymed on 21/01/15.
 */
@Entity(name = TableConfig.ReferenceTableName)
@DiscriminatorValue(TableConfig.ReferenceDiscriminatorValue)
public class Reference extends ReferenceBase implements Comparable<Reference> {

  private static final long serialVersionUID = -7176298227016698447L;


  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = TableConfig.ReferenceCategoryJoinColumnName)
  protected ReferenceCategory category;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = TableConfig.ReferenceSeriesTitleJoinColumnName)
  protected SeriesTitle series;

  @Column(name = TableConfig.ReferenceSeriesTitleOrderColumnName)
  protected int seriesOrder = Integer.MAX_VALUE;

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "reference", cascade = CascadeType.PERSIST)
  protected Set<ReferenceSubDivision> subDivisions = new HashSet<>();

  protected transient SortedSet<ReferenceSubDivision> subDivisionsSorted = null;

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "reference")
  protected Set<Entry> entries = new HashSet<>();


  @Column(name = TableConfig.ReferenceTitleSupplementColumnName)
  protected String titleSupplement;

  @Column(name = TableConfig.ReferenceTableOfContentsColumnName)
  @Lob
  protected String tableOfContents;

  @Column(name = TableConfig.ReferencePublishingDateColumnName)
  @Temporal(TemporalType.TIMESTAMP)
  protected Date publishingDate; // SQLite needs this, can't handle null dates

  @Column(name = TableConfig.ReferenceIsbnOrIssnColumnName)
  protected String isbnOrIssn;

  @Column(name = TableConfig.ReferenceIssueColumnName)
  protected String issue;

  @Column(name = TableConfig.ReferenceYearColumnName)
  protected Integer year;

  @Column(name = TableConfig.ReferenceDoiColumnName)
  protected String doi;

  @Column(name = TableConfig.ReferenceEditionColumnName)
  protected String edition;

  @Column(name = TableConfig.ReferenceVolumeColumnName)
  protected String volume;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = TableConfig.ReferencePublisherJoinColumnName)
  protected Publisher publisher;

  @Column(name = TableConfig.ReferencePlaceOfPublicationColumnName)
  protected String placeOfPublication;

  @Column(name = TableConfig.ReferenceLengthColumnName)
  protected String length;

  @OneToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = TableConfig.ReferenceLengthUnitJoinColumnName)
  protected ReferenceIndicationUnit lengthUnit;

  @Column(name = TableConfig.ReferencePriceColumnName)
  protected String price;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = TableConfig.ReferenceLanguageJoinColumnName)
  protected Language language;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = TableConfig.PersonDeepThoughtJoinColumnName)
  protected DeepThought deepThought;



  public Reference() {

  }

  public Reference(String title) {
    super(title);
  }

  public Reference(String title, String subTitle) {
    super(title, subTitle);
  }



  public SeriesTitle getSeries() {
    return series;
  }

  public void setSeries(SeriesTitle series) {
    Object previousValue = this.series;
    if(this.series != null)
      this.series.removeSerialPart(this);

    this.series = series;
    preview = null;

    if(series != null) {
      series.addSerialPart(this); // causes a Stackoverflow
//      series.serialParts.add(this);
    }

    for(Entry entry : new ArrayList<>(entries))
      entry.setSeries(series);

    callPropertyChangedListeners(TableConfig.ReferenceSeriesTitleJoinColumnName, previousValue, series);
  }

  public int getSeriesOrder() {
    return seriesOrder;
  }

  public void setSeriesOrder(int seriesOrder) {
    this.seriesOrder = seriesOrder;
  }

  public Collection<ReferenceSubDivision> getSubDivisions() {
    return subDivisions;
  }

  public boolean addSubDivision(ReferenceSubDivision subDivision) {
    if(subDivisions.add(subDivision)) {
      subDivision.setSubDivisionOrder(subDivisions.size() - 1);
      subDivision.setReference(this);
      subDivisionsSorted = null;

      callEntityAddedListeners(subDivisions, subDivision);
      return true;
    }

    return false;
  }

  public boolean removeSubDivision(ReferenceSubDivision subDivision) {
    int removeSubDivisionOrder = subDivision.getSubDivisionOrder();
    if(subDivisions.remove(subDivision)) {
      // TODO: remove from database
      subDivision.setReference(null);
      subDivisionsSorted = null;

      for(Entry entry : new ArrayList<>(subDivision.getEntries()))
        entry.setReferenceSubDivision(null);

      for(ReferenceSubDivision subDivisionEnum : getSubDivisions()) {
        if(subDivisionEnum.getSubDivisionOrder() > removeSubDivisionOrder)
          subDivisionEnum.setSubDivisionOrder(subDivisionEnum.getSubDivisionOrder() - 1);
      }

      callEntityRemovedListeners(subDivisions, subDivision);
      return true;
    }

    return false;
  }

  public SortedSet<ReferenceSubDivision> getSubDivisionsSorted() {
    if(subDivisionsSorted == null)
      subDivisionsSorted = new TreeSet<>(getSubDivisions());

    return subDivisionsSorted;
  }


  public Collection<Entry> getEntries() {
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

  public ReferenceCategory getCategory() {
    return category;
  }

  public void setCategory(ReferenceCategory category) {
    if(this.category != null)
      this.category.removeReference(this);

    Object previousValue = this.category;
    this.category = category;
    preview = null;

    if(this.category != null)
      this.category.addReference(this);

    callPropertyChangedListeners(TableConfig.ReferenceCategoryJoinColumnName, previousValue, category);
  }

  @Override
  public void setTitle(String title) {
    preview = null;
    super.setTitle(title);
  }

  public String getTitleSupplement() {
    return titleSupplement;
  }

  public void setTitleSupplement(String titleSupplement) {
    Object previousValue = this.titleSupplement;
    this.titleSupplement = titleSupplement;
    callPropertyChangedListeners(TableConfig.ReferenceTitleSupplementColumnName, previousValue, titleSupplement);
  }

  public String getTableOfContents() {
    return tableOfContents;
  }

  public void setTableOfContents(String tableOfContents) {
    Object previousValue = this.tableOfContents;
    this.tableOfContents = tableOfContents;
    callPropertyChangedListeners(TableConfig.ReferenceTableOfContentsColumnName, previousValue, tableOfContents);
  }

  public Date getPublishingDate() {
    return publishingDate;
  }

  public void setPublishingDate(Date publishingDate) {
    Object previousValue = this.publishingDate;
    this.publishingDate = publishingDate;
    callPropertyChangedListeners(TableConfig.ReferencePublishingDateColumnName, previousValue, publishingDate);
  }

  public String getIsbnOrIssn() {
    return isbnOrIssn;
  }

  public void setIsbnOrIssn(String isbnOrIssn) {
    Object previousValue = this.isbnOrIssn;
    this.isbnOrIssn = isbnOrIssn;
    callPropertyChangedListeners(TableConfig.ReferenceIsbnOrIssnColumnName, previousValue, isbnOrIssn);
  }

  public String getIssue() {
    return issue;
  }

  public void setIssue(String issue) {
    Object previousValue = this.issue;
    this.issue = issue;
    callPropertyChangedListeners(TableConfig.ReferenceIssueColumnName, previousValue, issue);
  }

  public Integer getYear() {
    return year;
  }

  public void setYear(Integer year) {
    Object previousValue = this.year;
    this.year = year;
    callPropertyChangedListeners(TableConfig.ReferenceYearColumnName, previousValue, year);
  }

  public String getDoi() {
    return doi;
  }

  public void setDoi(String doi) {
    Object previousValue = this.doi;
    this.doi = doi;
    callPropertyChangedListeners(TableConfig.ReferenceDoiColumnName, previousValue, doi);
  }

  public String getEdition() {
    return edition;
  }

  public void setEdition(String edition) {
    Object previousValue = this.edition;
    this.edition = edition;
    callPropertyChangedListeners(TableConfig.ReferenceEditionColumnName, previousValue, edition);
  }

  public String getVolume() {
    return volume;
  }

  public void setVolume(String volume) {
    Object previousValue = this.volume;
    this.volume = volume;
    callPropertyChangedListeners(TableConfig.ReferenceVolumeColumnName, previousValue, volume);
  }

  public Publisher getPublisher() {
    return publisher;
  }

  public void setPublisher(Publisher publisher) {
    Object previousValue = this.publisher;
    if(this.publisher != null)
      this.publisher.removeReference(this);

    this.publisher = publisher;

    if(publisher != null)
      publisher.addReference(this);

    callPropertyChangedListeners(TableConfig.ReferencePublisherJoinColumnName, previousValue, publisher);
  }

  public String getPlaceOfPublication() {
    return placeOfPublication;
  }

  public void setPlaceOfPublication(String placeOfPublication) {
    Object previousValue = this.placeOfPublication;
    this.placeOfPublication = placeOfPublication;
    callPropertyChangedListeners(TableConfig.ReferencePlaceOfPublicationColumnName, previousValue, placeOfPublication);
  }

  public String getLength() {
    return length;
  }

  public void setLength(String length) {
    Object previousValue = this.length;
    this.length = length;
    callPropertyChangedListeners(TableConfig.ReferenceLengthColumnName, previousValue, length);
  }

  public ReferenceIndicationUnit getLengthUnit() {
    return lengthUnit;
  }

  public void setLengthUnit(ReferenceIndicationUnit lengthUnit) {
    Object previousValue = this.lengthUnit;
    this.lengthUnit = lengthUnit;
    callPropertyChangedListeners(TableConfig.ReferenceLengthUnitJoinColumnName, previousValue, lengthUnit);
  }

  public String getPrice() {
    return price;
  }

  public void setPrice(String price) {
    Object previousValue = this.price;
    this.price = price;
    callPropertyChangedListeners(TableConfig.ReferencePriceColumnName, previousValue, price);
  }

  public Language getLanguage() {
    return language;
  }

  public void setLanguage(Language language) {
    Object previousValue = this.language;
    this.language = language;
    callPropertyChangedListeners(TableConfig.ReferenceLanguageJoinColumnName, previousValue, language);
  }

  public DeepThought getDeepThought() {
    return deepThought;
  }


  protected transient String preview = null;

  @Transient
  public String getPreview() {
    if(preview == null) {
//      preview = getTextRepresentation();
//      addCategorySpecificInfo();
      preview = title;
      if (issue != null && year != null)
        preview = issue + "/" + year;
      else if(title == null && publishingDate != null)
        preview = publishingDate.toString();

      if (series != null)
        preview = series.getTextRepresentation() + " " + preview;
    }

    return preview;
  }

  public void addCategorySpecificInfo() {
    if (category != null) {
      if (title == null) {
        // for Newpapers and Magazine, if no Title for this reference is set but Issue and Year, display <Series> <Issus>/<Year> as preview
        // TODO: for Radio and Television broadcasts as well?
        if (category == ReferenceCategory.getNewsPaperIssueCategory() || category == ReferenceCategory.getMagazineIssueCategory()) {
          if (issue != null && year != null) {
            preview = issue + "/" + year;

            if (series != null)
              preview = series.getTextRepresentation() + " " + preview;
          }
          else if(publishingDate != null && series != null)
            preview = series.getTitle() + publishingDate;
        }
      }
    }
  }

  @Override
  public String toString() {
    return "Reference " + getTextRepresentation();
  }

  public static Reference createReferenceFromStringRepresentation(String stringRepresentation) {
    Reference newReference = new Reference(stringRepresentation); // TODO
    return newReference;
  }

  @Override
  public int compareTo(Reference other) {
    if(other == null)
      return 1;

    return getTitle().compareTo(other.getTitle()); // TODO
  }

  public static Reference findReferenceFromStringRepresentation(String stringRepresentation) {
    // TODO:
    for(Reference reference : Application.getDeepThought().getReferences()) {
      if(reference.getTitle().equals(stringRepresentation))
        return reference;
    }
    return null;
  }
}
