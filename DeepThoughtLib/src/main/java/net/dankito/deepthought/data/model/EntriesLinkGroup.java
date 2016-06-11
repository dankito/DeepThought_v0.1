package net.dankito.deepthought.data.model;

import net.dankito.deepthought.data.persistence.db.TableConfig;
import net.dankito.deepthought.data.persistence.db.UserDataEntity;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.Transient;

/**
 * Created by ganymed on 03/01/15.
 */
@Entity(name = TableConfig.EntriesLinkGroupTableName)
public class EntriesLinkGroup extends UserDataEntity {

  private static final long serialVersionUID = -1858952131697371548L;


  @Column(name = TableConfig.EntriesLinkGroupGroupNameColumnName)
  protected String groupName;

  @ManyToMany(fetch = FetchType.LAZY, mappedBy = "linkGroups")
  protected Set<Entry> entries = new HashSet<>();

  @Column(name = TableConfig.EntriesLinkGroupNotesColumnName)
  protected String notes;


  public EntriesLinkGroup() {

  }

  public EntriesLinkGroup(String groupName) {
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


  protected void callEntryAddedToGroupListeners(Entry entry) {

  }

  protected void callEntryRemovedFromGroupListeners(Entry entry) {

  }


  @Override
  @Transient
  public String getTextRepresentation() {
    return "Link " + getGroupName();
  }

  @Override
  public String toString() {
    return "EntriesLinkGroup " + getGroupName();
  }

}
