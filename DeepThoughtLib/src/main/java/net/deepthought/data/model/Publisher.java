package net.deepthought.data.model;

import net.deepthought.data.persistence.db.TableConfig;
import net.deepthought.data.persistence.db.UserDataEntity;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

/**
 * Created by ganymed on 22/12/14.
 */
@Entity(name = TableConfig.PublisherTableName)
public class Publisher extends UserDataEntity implements Comparable<Publisher> {

  @Column(name = TableConfig.PublisherNameColumnName)
  protected String name;

  @Column(name = TableConfig.PublisherNotesColumnName)
  protected String notes;

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "publisher")
  protected Set<SeriesTitle> seriesTitles = new HashSet<>();

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "publisher")
  protected Set<Reference> references = new HashSet<>();

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = TableConfig.PublisherDeepThoughtJoinColumnName)
  protected DeepThought deepThought;


  public Publisher() {

  }

  public Publisher(String name) {
    this.name = name;
  }


  public String getName() {
    return name;
  }

  public void setName(String name) {
    Object previousValue = this.name;
    this.name = name;
    callPropertyChangedListeners(TableConfig.PublisherNameColumnName, previousValue, name);
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    Object previousValue = this.notes;
    this.notes = notes;
    callPropertyChangedListeners(TableConfig.PublisherNotesColumnName, previousValue, notes);
  }

  public Collection<Reference> getReferences() {
    return references;
  }

  protected boolean addReference(Reference reference) {
    boolean result = this.references.add(reference);

    if(result) {
      callEntityAddedListeners(references, reference);
    }

    return result;
  }

  protected boolean removeReference(Reference reference) {
    boolean result = this.references.remove(reference);

    if(result) {
      callEntityRemovedListeners(references, reference);
    }

    return result;
  }

  public Set<SeriesTitle> getSeriesTitle() {
    return seriesTitles;
  }

  protected boolean addSeriesTitle(SeriesTitle seriesTitle) {
    boolean result = this.seriesTitles.add(seriesTitle);

    if(result) {
      callEntityAddedListeners(seriesTitles, seriesTitle);
    }

    return result;
  }

  protected boolean removeSeriesTitle(SeriesTitle seriesTitle) {
    boolean result = this.seriesTitles.remove(seriesTitle);

    if(result) {
      callEntityRemovedListeners(seriesTitles, seriesTitle);
    }

    return result;
  }


  @Override
  @Transient
  public String getTextRepresentation() {
    return getName();
  }

  @Override
  public String toString() {
    return "Publisher " + getTextRepresentation();
  }

  @Override
  public int compareTo(Publisher other) {
    if(other == null)
      return 1;

    return getName().compareTo(other.getName());
  }
}
