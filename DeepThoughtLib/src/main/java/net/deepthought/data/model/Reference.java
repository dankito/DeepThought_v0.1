package net.deepthought.data.model;

import net.deepthought.Application;
import net.deepthought.data.persistence.db.TableConfig;
import net.deepthought.util.Localization;
import net.deepthought.util.StringUtils;

import java.text.DateFormat;
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

  @Column(name = TableConfig.ReferenceIssueOrPublishingDateColumnName)
  protected String issueOrPublishingDate;

  @Column(name = TableConfig.ReferencePublishingDateColumnName)
  @Temporal(TemporalType.TIMESTAMP)
  protected Date publishingDate; // SQLite needs this, can't handle null dates

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = TableConfig.ReferenceDeepThoughtJoinColumnName)
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

//      if(subDivision.getDeepThought() == null && this.deepThought != null)
//        deepThought.addReferenceSubDivision(subDivision);

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

      if(deepThought != null)
        deepThought.removeReferenceSubDivision(subDivision);

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

  public String getIssueOrPublishingDate() {
    if(StringUtils.isNullOrEmpty(issueOrPublishingDate) && publishingDate != null)
      DateFormat.getDateInstance(DateFormat.MEDIUM, Localization.getLanguageLocale()).format(publishingDate);
    return issueOrPublishingDate;
  }

  public void setIssueOrPublishingDate(String issueOrPublishingDate) {
    Object previousValue = this.issueOrPublishingDate;
    this.issueOrPublishingDate = issueOrPublishingDate;
    preview = null;
    setPublishingDate(tryToParseIssueOrPublishingDateToDate(issueOrPublishingDate));
    callPropertyChangedListeners(TableConfig.ReferenceIssueOrPublishingDateColumnName, previousValue, issueOrPublishingDate);
  }

  public Date getPublishingDate() {
    if(publishingDate == null)
      publishingDate = tryToParseIssueOrPublishingDateToDate(issueOrPublishingDate);
    return publishingDate;
  }

  public void setPublishingDate(Date publishingDate) {
    this.publishingDate = publishingDate;
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
  }


  protected transient String preview = null;

  @Transient
  public String getPreview() {
    if(preview == null) {
//      preview = getTextRepresentation();
//      addCategorySpecificInfo();
      preview = title;
      if(StringUtils.isNullOrEmpty(title) && issueOrPublishingDate != null)
        preview = issueOrPublishingDate.toString();

      if (series != null)
        preview = series.getTextRepresentation() + (StringUtils.isNullOrEmpty(preview) ? "" : " " + preview);

      // TODO: what about Persons?
    }

    return preview;
  }



  public static Date tryToParseIssueOrPublishingDateToDate(String issueOrPublishingDate) {
    if(StringUtils.isNullOrEmpty(issueOrPublishingDate))
      return null;

    try {
      return DateFormat.getDateInstance(DateFormat.MEDIUM, Localization.getLanguageLocale()).parse(issueOrPublishingDate);
    } catch(Exception ex) {
      if(issueOrPublishingDate.length() == 4) { // only year is set
        try {
          int year = Integer.parseInt(issueOrPublishingDate) - 1900;
          return new Date(year, 0, 1);
        } catch(Exception ex2) { }
      }
      else if(issueOrPublishingDate.length() == 7) { // month and year - separated by any sign - are set
        try {
          int month = Integer.parseInt(issueOrPublishingDate.substring(0, 2)) - 1;
          int year = Integer.parseInt(issueOrPublishingDate.substring(3, 7)) - 1900;
          return new Date(year, month, 1);
        } catch(Exception ex2) { }
      }
      else { // if String has been set by DatePicker control
        try {
          return DateFormat.getDateInstance(DateFormat.LONG, Localization.getLanguageLocale()).parse(issueOrPublishingDate);
        } catch(Exception ex2) { }
      }
    }

    return null;
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
