package net.deepthought.data.model;

import net.deepthought.data.persistence.db.TableConfig;
import net.deepthought.data.persistence.db.UserDataEntity;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

/**
 * Created by ganymed on 12/10/14.
 */
@Entity(name = TableConfig.IndexTermTableName)
public class IndexTerm extends UserDataEntity implements Comparable<IndexTerm>, Serializable {

  private static final long serialVersionUID = -6701001789812666295L;


  @Column(name = TableConfig.IndexTermNameColumnName)
  protected String name = "";

  @Column(name = TableConfig.IndexTermDescriptionColumnName)
  protected String description = "";

  @ManyToMany(fetch = FetchType.LAZY, mappedBy = "indexTerms") // TODO: has cascade also to be set to { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH } as in Entry?
  protected Set<Entry> entries = new HashSet<>();

//  @JsonIgnore
  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = TableConfig.IndexTermDeepThoughtJoinColumnName)
  protected DeepThought deepThought;



  public IndexTerm() {

  }

  public IndexTerm(String name) {
    this();
    this.name = name;
  }

  public IndexTerm(String name, String description) {
    this(name);
    this.description = description;
  }


  public String getName() {
    return name;
  }

  public void setName(String name) {
    String previousName = this.name;
    this.name = name;
    callPropertyChangedListeners(TableConfig.IndexTermNameColumnName, previousName, name);
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    String previousDescription = this.description;
    this.description = description;
    callPropertyChangedListeners(TableConfig.IndexTermDescriptionColumnName, previousDescription, description);
  }

  public boolean hasEntries() {
    return getEntries().size() > 0;
  }

  public Collection<Entry> getEntries() {
    return entries;
  }

  protected boolean addEntry(Entry entry) {
    boolean result = entries.add(entry);
    callEntityAddedListeners(entries, entry);
    return result;
  }

  protected boolean removeEntry(Entry entry) {
    boolean result = entries.remove(entry);
    callEntityRemovedListeners(entries, entry);
    return result;
  }


  // TODO: remove again, only for unit tests
  public DeepThought getDeepThought() {
    return deepThought;
  }

  @Override
  public int compareTo(IndexTerm other) {
    return name.toLowerCase().compareTo(other.getName().toLowerCase());
  }



  @Override
  @Transient
  public String getTextRepresentation() {
    return "Keyword " + name + " (" + entries.size() + ")";
  }

  @Override
  public String toString() {
    return getTextRepresentation();
  }

}
