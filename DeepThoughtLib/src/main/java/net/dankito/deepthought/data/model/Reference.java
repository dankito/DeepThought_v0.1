package net.dankito.deepthought.data.model;

import net.dankito.deepthought.data.persistence.db.TableConfig;
import net.dankito.deepthought.util.localization.Localization;
import net.dankito.deepthought.util.StringUtils;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
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

  public static final int PublishingDateFormat = DateFormat.SHORT;


  @ManyToOne(fetch = FetchType.EAGER)
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

  @Column(name = TableConfig.ReferenceIsbnOrIssnColumnName)
  protected String isbnOrIssn;

  @Column(name = TableConfig.ReferencePublishingDateColumnName)
  @Temporal(TemporalType.TIMESTAMP)
  protected Date publishingDate;

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
    if(this.series == series)
      return;

    Object previousValue = this.series;
    List<Entry> entriesBackup = new ArrayList<>(entries);
    if(this.series != null)
      this.series.removeSerialPart(this);

    this.series = series;
    resetPreview();

    if(series != null) {
      series.addSerialPart(this); // causes a Stackoverflow
    }

    for(Entry entry : entriesBackup)
      entry.setSeries(series);

    callPropertyChangedListeners(TableConfig.ReferenceSeriesTitleJoinColumnName, previousValue, series);
  }

  public int getSeriesOrder() {
    return seriesOrder;
  }

  public void setSeriesOrder(int seriesOrder) {
    this.seriesOrder = seriesOrder;
  }


  public boolean hasSubDivisions() {
    return getSubDivisions().size() > 0;
  }

  public Collection<ReferenceSubDivision> getSubDivisions() {
    return subDivisions;
  }

  protected boolean containsSubDivision(ReferenceSubDivision subDivision) {
    return subDivisions.contains(subDivision);
  }

  public boolean addSubDivision(ReferenceSubDivision subDivision) {
    if(containsSubDivision(subDivision) == false && subDivisions.add(subDivision)) {
      subDivisionsSorted = null;
      if(this.equals(subDivision.getReference()) == false) {
        subDivision.setReference(this);
        subDivision.setSubDivisionOrder(subDivisions.size() - 1);
      }

      callEntityAddedListeners(subDivisions, subDivision);
      return true;
    }

    return false;
  }

  public boolean removeSubDivision(ReferenceSubDivision subDivision) {
    if(containsSubDivision(subDivision) == false)
      return false;

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


  public boolean hasEntries() {
    return getEntries().size() > 0;
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
    resetPreview();
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
      return formatPublishingDate();
    return issueOrPublishingDate;
  }

  public void setIssueOrPublishingDate(String issueOrPublishingDate) {
    Object previousValue = this.issueOrPublishingDate;
    if(publishingDateEqualsIssueOrPublishingDate(this.issueOrPublishingDate)) {
      this.publishingDate = null;
    }

    this.issueOrPublishingDate = issueOrPublishingDate;
    resetPreview();

    if(publishingDate == null) {
      setPublishingDate(tryToParseIssueOrPublishingDateToDate(issueOrPublishingDate));
    }

    callPropertyChangedListeners(TableConfig.ReferenceIssueOrPublishingDateColumnName, previousValue, issueOrPublishingDate);
  }

  public Date getPublishingDate() {
    if(publishingDate == null)
      publishingDate = tryToParseIssueOrPublishingDateToDate(issueOrPublishingDate);
    return publishingDate;
  }

  public void setPublishingDate(Date publishingDate) {
    Object previousValue = this.publishingDate;
    this.publishingDate = publishingDate;
    resetPreview();
    callPropertyChangedListeners(TableConfig.ReferencePublishingDateColumnName, previousValue, publishingDate);
  }

  protected boolean publishingDateEqualsIssueOrPublishingDate(String issueOrPublishingDate) {
    if(publishingDate != null) {
      Date parsedDate = tryToParseIssueOrPublishingDateToDate(issueOrPublishingDate);
      if (parsedDate != null && parsedDate.equals(this.publishingDate)) {
        return true;
      }
    }

    return false;
  }

  public String getIsbnOrIssn() {
    return isbnOrIssn;
  }

  public void setIsbnOrIssn(String isbnOrIssn) {
    Object previousValue = this.isbnOrIssn;
    this.isbnOrIssn = isbnOrIssn;
    callPropertyChangedListeners(TableConfig.ReferenceIsbnOrIssnColumnName, previousValue, isbnOrIssn);
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
    callPropertyChangedListeners(TableConfig.ReferenceDeepThoughtJoinColumnName, previousValue, deepThought);
  }


  protected transient String preview = null;

  @Transient
  public String getPreview() {
    if(preview == null) {
      preview = title;
      if(StringUtils.isNullOrEmpty(title)) {
        if(publishingDate != null) {
          preview = formatPublishingDate();
        }
        else if(issueOrPublishingDate != null) {
          preview = issueOrPublishingDate.toString();
        }
      }

      if (series != null)
        preview = series.getTextRepresentation() + (StringUtils.isNullOrEmpty(preview) ? "" : " " + preview);

      // TODO: what about Persons?
    }

    return preview;
  }

  public void resetPreview() {
    preview = null;
  }



  public String formatPublishingDate() {
    return DateFormat.getDateInstance(PublishingDateFormat, Localization.getLanguageLocale()).format(publishingDate);
  }

  public static Date tryToParseIssueOrPublishingDateToDate(String issueOrPublishingDate) {
    if(StringUtils.isNullOrEmpty(issueOrPublishingDate))
      return null;

    try {
      return DateFormat.getDateInstance(DateFormat.SHORT, Localization.getLanguageLocale()).parse(issueOrPublishingDate);
    } catch(Exception ex) {
      try {
        return DateFormat.getDateInstance(DateFormat.MEDIUM, Localization.getLanguageLocale()).parse(issueOrPublishingDate);
      } catch(Exception ex1) {
        if (issueOrPublishingDate.length() == 4) { // only year is set
          try {
            int year = Integer.parseInt(issueOrPublishingDate) - 1900;
            return new Date(year, 0, 1);
          } catch (Exception ex2) {
          }
        } else if (issueOrPublishingDate.length() == 7) { // month and year - separated by any sign - are set
          try {
            int month = Integer.parseInt(issueOrPublishingDate.substring(0, 2)) - 1;
            if(month < 12) { // TODO: how to get if its a week or a month?
              int year = Integer.parseInt(issueOrPublishingDate.substring(3, 7)) - 1900;
              return new Date(year, month, 1);
            }
          } catch (Exception ex2) {
          }
        }else if (issueOrPublishingDate.length() == 5) { // may month and short year (only two figures) - separated by any sign - are set
          try {
            int month = Integer.parseInt(issueOrPublishingDate.substring(0, 2)) - 1;
            if(month < 12) {
              int year = Integer.parseInt(issueOrPublishingDate.substring(3, 5)) - 1900;
              return new Date(year, month, 1);
            }
          } catch (Exception ex2) {
          }
        } else { // if String has been set by DatePicker control
          try {
            return DateFormat.getDateInstance(DateFormat.LONG, Localization.getLanguageLocale()).parse(issueOrPublishingDate);
          } catch (Exception ex2) {
          }
        }
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


  @Override
  public int compareTo(Reference other) {
    if(other == null)
      return 1;

    return getPreview().compareTo(other.getPreview()); // TODO
  }

}
