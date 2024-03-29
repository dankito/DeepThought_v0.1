package net.dankito.deepthought.data.model;

import net.dankito.deepthought.data.persistence.db.TableConfig;
import net.dankito.deepthought.data.persistence.db.UserDataEntity;

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
 * Created by ganymed on 03/01/15.
 */
@Entity(name = TableConfig.EntriesGroupTableName)
public class EntriesGroup extends UserDataEntity {

  private static final long serialVersionUID = -1858952131697371548L;


  @Column(name = TableConfig.EntriesGroupGroupNameColumnName)
  protected String groupName;

  @ManyToMany(fetch = FetchType.LAZY, mappedBy = "entryGroups")
  protected Set<Entry> entries = new HashSet<>();

  @Column(name = TableConfig.EntriesGroupNotesColumnName)
  protected String notes;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = TableConfig.EntriesGroupDeepThoughtJoinColumnName)
  protected DeepThought deepThought;


  public EntriesGroup() {

  }

  public EntriesGroup(String groupName) {
    this.groupName = groupName;
  }


  public String getGroupName() {
    return groupName;
  }

  public void setGroupName(String groupName) {
    this.groupName = groupName;
  }

  public Collection<Entry> getEntries() {
    return entries;
  }

  protected boolean addEntryToGroup(Entry entry) {
    boolean result = entries.add(entry);
    if(result)
      callEntryAddedToGroupListeners(entry);
    return result;
  }

  protected boolean removeEntryFromGroup(Entry entry) {
    boolean result = entries.remove(entry);
    if(result)
      callEntryRemovedFromGroupListeners(entry);
    return result;
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }


  public DeepThought getDeepThought() {
    return deepThought;
  }

  protected void setDeepThought(DeepThought deepThought) {
    Object previousValue = this.deepThought;
    this.deepThought = deepThought;
    callPropertyChangedListeners(TableConfig.EntriesGroupDeepThoughtJoinColumnName, previousValue, deepThought);
  }


  protected void callEntryAddedToGroupListeners(Entry entry) {

  }

  protected void callEntryRemovedFromGroupListeners(Entry entry) {

  }


  @Override
  @Transient
  public String getTextRepresentation() {
    return "EntriesGroup " + getGroupName();
  }

  @Override
  public String toString() {
    return "EntriesGroup " + getGroupName();
  }

}
