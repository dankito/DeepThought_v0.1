package net.deepthought.data.model;

import net.deepthought.Application;
import net.deepthought.data.persistence.db.TableConfig;
import net.deepthought.data.persistence.db.UserDataEntity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OrderBy;
import javax.persistence.PostPersist;
import javax.persistence.Transient;

/**
 * Created by ganymed on 12/10/14.
 */
@Entity(name = TableConfig.TagTableName)
public class Tag extends UserDataEntity implements Comparable<Tag>, Serializable {

  private static final long serialVersionUID = 1204202485407318615L;


  @Column(name = TableConfig.TagNameColumnName)
  protected String name = "";

  @Column(name = TableConfig.TagDescriptionColumnName)
  protected String description = "";

  @ManyToMany(fetch = FetchType.LAZY, mappedBy = "tags") // TODO: has cascade also to be set to { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH } as in Entry?
  @OrderBy("entryIndex DESC")
  protected Collection<Entry> entries = new ArrayList<>();

//  @JsonIgnore
  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = TableConfig.TagDeepThoughtJoinColumnName)
  protected DeepThought deepThought;



  public Tag() {

  }

  public Tag(String name) {
    this();
    this.name = name;
  }

  public Tag(String name, String description) {
    this(name);
    this.description = description;
  }


  public String getName() {
    return name;
  }

  public void setName(String name) {
    String previousName = this.name;
    this.name = name;
    callPropertyChangedListeners(TableConfig.TagNameColumnName, previousName, name);
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    String previousDescription = this.description;
    this.description = description;
    callPropertyChangedListeners(TableConfig.TagDescriptionColumnName, previousDescription, description);
  }

  public boolean hasEntries() {
    return getEntries().size() > 0;
  }

  public Collection<Entry> getEntries() {
    return entries;
  }

  protected boolean addEntry(Entry entry) {
//    boolean result = entries.add(entry);
//    callEntryAddedListeners(entry);
//    return result;

    if(entries instanceof List)
      ((List)entries).add(0, entry);
    else
      entries.add(entry);

    callEntityAddedListeners(entries, entry);
    return true;
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
  public int compareTo(Tag other) {
    return name.toLowerCase().compareTo(other.getName().toLowerCase());
  }



  @Override
  @Transient
  public String getTextRepresentation() {
    return "Tag " + name + " (" + entries.size() + ")";
  }

  @Override
  public String toString() {
    return getTextRepresentation();
  }


  @PostPersist
  protected void postPersist() {
    if(Application.getSearchEngine() != null)
      Application.getSearchEngine().indexEntity(this);
  }

  @Override
  protected void callPropertyChangedListeners(String propertyName, Object previousValue, Object newValue) {
    super.callPropertyChangedListeners(propertyName, previousValue, newValue);

    Application.getSearchEngine().updateIndexForEntity(this, propertyName);
  }

}
