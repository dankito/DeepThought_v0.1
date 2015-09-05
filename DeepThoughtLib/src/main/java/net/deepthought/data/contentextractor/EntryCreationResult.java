package net.deepthought.data.contentextractor;

import net.deepthought.data.model.Category;
import net.deepthought.data.model.Entry;
import net.deepthought.data.model.Person;
import net.deepthought.data.model.Reference;
import net.deepthought.data.model.ReferenceSubDivision;
import net.deepthought.data.model.SeriesTitle;
import net.deepthought.data.model.Tag;
import net.deepthought.util.DeepThoughtError;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ganymed on 24/04/15.
 */
public class EntryCreationResult {

  protected Object source;

  protected boolean successful = false;

  protected DeepThoughtError error = null;

  protected Entry createdEntry = null;

  protected List<Tag> entryTags = new ArrayList<>();

  protected List<Category> entryCategories = new ArrayList<>();

  protected SeriesTitle extractedSeriesTitle = null;

  protected Reference extractedReference = null;

  protected ReferenceSubDivision extractedSubDivision = null;

  protected List<Person> extractedPersons = new ArrayList<>();


  public EntryCreationResult(Object source, DeepThoughtError error) {
    this.source = source;
    this.successful = false;
    this.error = error;
  }

  public EntryCreationResult(Object source, Entry createdEntry) {
    this.source = source;
    this.successful = createdEntry != null;
    this.createdEntry = createdEntry;
  }


  public Object getSource() {
    return source;
  }

  public boolean successful() {
    return successful;
  }

  public DeepThoughtError getError() {
    return error;
  }

  public Entry getCreatedEntry() {
    return createdEntry;
  }

  public List<Tag> getEntryTags() {
    return entryTags;
  }

  public void setEntryTags(List<Tag> entryTags) {
    this.entryTags = entryTags;
  }

  public List<Category> getEntryCategories() {
    return entryCategories;
  }

  public void setEntryCategories(List<Category> entryCategories) {
    this.entryCategories = entryCategories;
  }

  public SeriesTitle getExtractedSeriesTitle() {
    return extractedSeriesTitle;
  }

  public void setExtractedSeriesTitle(SeriesTitle extractedSeriesTitle) {
    this.extractedSeriesTitle = extractedSeriesTitle;
  }

  public Reference getExtractedReference() {
    return extractedReference;
  }

  public void setExtractedReference(Reference extractedReference) {
    this.extractedReference = extractedReference;
  }

  public ReferenceSubDivision getExtractedSubDivision() {
    return extractedSubDivision;
  }

  public void setExtractedSubDivision(ReferenceSubDivision extractedSubDivision) {
    this.extractedSubDivision = extractedSubDivision;
  }

  public List<Person> getExtractedPersons() {
    return extractedPersons;
  }

  public void setExtractedPersons(List<Person> extractedPersons) {
    this.extractedPersons = extractedPersons;
  }


  @Override
  public String toString() {
    String description = source + " Successful? " + successful + "; ";
    if(successful)
      description += createdEntry;
    else
      description += error;

    return description;
  }

}
