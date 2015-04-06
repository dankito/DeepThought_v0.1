package net.deepthought.data.model;

import net.deepthought.Application;
import net.deepthought.data.model.enums.Language;
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

  @Override
  public void setTitle(String title) {
    preview = null;
    super.setTitle(title);
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

  @Override
  @Transient
  public String getTextRepresentation() {
    return getPreview();
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

    return getPreview().compareTo(other.getPreview()); // TODO
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
