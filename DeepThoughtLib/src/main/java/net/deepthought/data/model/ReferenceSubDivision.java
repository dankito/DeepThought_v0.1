package net.deepthought.data.model;

import net.deepthought.data.persistence.db.TableConfig;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Transient;

/**
 * Created by ganymed on 21/01/15.
 */
@Entity(name = TableConfig.ReferenceSubDivisionTableName)
@DiscriminatorValue(TableConfig.ReferenceSubDivisionDiscriminatorValue)
public class ReferenceSubDivision extends ReferenceBase implements Comparable<ReferenceSubDivision> {

  private static final long serialVersionUID = -967783983570745569L;


  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = TableConfig.ReferenceSubDivisionReferenceJoinColumnName)
  protected Reference reference;

  @Column(name = TableConfig.ReferenceSubDivisionOrderColumnName)
  protected int subDivisionOrder; // Chapter number, number of Article, ...

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = TableConfig.ReferenceSubDivisionParentSubDivisionJoinColumnName)
  protected ReferenceSubDivision parentSubDivision;

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "parentSubDivision"/*, cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }*/)
  @OrderBy("subDivisionOrder ASC") // TODO: subDivisions afterwards don't get sorted
  protected Collection<ReferenceSubDivision> subDivisions = new ArrayList<>();

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "referenceSubDivision")
  protected Collection<Entry> entries = new HashSet<>();

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = TableConfig.ReferenceSubDivisionDeepThoughtJoinColumnName)
  protected DeepThought deepThought;



  public ReferenceSubDivision() {

  }

  public ReferenceSubDivision(String title) {
    super(title);
  }

  public ReferenceSubDivision(String title, String subTitle) {
    super(title, subTitle);
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

  public Reference getReference() {
    return reference;
  }

  public void setReference(Reference reference) {
    if(this.reference == reference)
      return;

    Object previousValue = this.reference;
    List<Entry> entriesBackup = new ArrayList<>(entries); // after calling reference.removeSubDivision() entries will be empty
    if(this.reference != null && this.reference.equals(reference) == false)
      this.reference.removeSubDivision(this);

    this.reference = reference;

    if(reference != null)
      reference.addSubDivision(this);

    for(Entry entry : entriesBackup)
//      entry.setReference(reference);
      entry.setReferenceSubDivision(this);

    callPropertyChangedListeners(TableConfig.ReferenceSubDivisionReferenceJoinColumnName, previousValue, reference);
  }

  public ReferenceSubDivision getParentSubDivision() {
    return parentSubDivision;
  }

  public boolean hasSubDivisions() {
    return getSubDivisions().size() > 0;
  }

  public Collection<ReferenceSubDivision> getSubDivisions() {
    return subDivisions;
  }

  public boolean addSubDivision(ReferenceSubDivision subDivision) {
    subDivision.parentSubDivision = this;
    subDivision.subDivisionOrder = subDivisions.size();
    if(subDivision.reference == null && this.reference != null)
      subDivision.reference = this.reference;

    if(subDivision.getDeepThought() == null && this.deepThought != null)
      deepThought.addReferenceSubDivision(subDivision);

    boolean result = subDivisions.add(subDivision);
    if(result) {
      callEntityAddedListeners(subDivisions, subDivision);
    }

    return result;
  }

  public boolean removeSubDivision(ReferenceSubDivision subDivision) {
    int removeSubDivisionIndex = subDivision.getSubDivisionOrder();

    boolean result = subDivisions.remove(subDivision);
    if(result) {
      subDivision.parentSubDivision = null;

      for(Entry entry : new ArrayList<>(subDivision.getEntries()))
        entry.setReferenceSubDivision(null);

      for(ReferenceSubDivision subDivisionEnum : subDivisions) {
        if(subDivisionEnum.getSubDivisionOrder() >= removeSubDivisionIndex)
          subDivisionEnum.setSubDivisionOrder(subDivisionEnum.getSubDivisionOrder() - 1);
      }

      callEntityRemovedListeners(subDivisions, subDivision);
    }

    return result;
  }

  public int getSubDivisionOrder() {
    return subDivisionOrder;
  }

  public void setSubDivisionOrder(int subDivisionOrder) {
    Object previousValue = this.subDivisionOrder;
    this.subDivisionOrder = subDivisionOrder;
    callPropertyChangedListeners(TableConfig.ReferenceSubDivisionOrderColumnName, previousValue, subDivisionOrder);
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
    callPropertyChangedListeners(TableConfig.ReferenceSubDivisionDeepThoughtJoinColumnName, previousValue, deepThought);
  }

  @Override
  @Transient
  public String getTextRepresentation() {
    if(reference != null)
      return reference.getTextRepresentation() + " - " + super.getTextRepresentation();

    return super.getTextRepresentation();
  }

  @Override
  public String toString() {
    return "ReferenceSubDivision " + getTextRepresentation();
  }

  @Override
  public int compareTo(ReferenceSubDivision other) {
    if(other == null)
      return 1;

    return ((Integer) getSubDivisionOrder()).compareTo(other.getSubDivisionOrder());
  }
}
