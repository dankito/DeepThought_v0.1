package net.deepthought.data.model.enums;

import net.deepthought.data.model.DeepThought;
import net.deepthought.data.persistence.db.TableConfig;
import net.deepthought.data.persistence.db.UserDataEntity;
import net.deepthought.util.Localization;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

/**
 * Created by ganymed on 29/01/15.
 */
@MappedSuperclass
public class ExtensibleEnumeration extends UserDataEntity implements Comparable<ExtensibleEnumeration> {

  private static final long serialVersionUID = -5370585730042175143L;


  @Column(name = TableConfig.ExtensibleEnumerationNameColumnName)
  protected String name;

  @Column(name = TableConfig.ExtensibleEnumerationNameResourceKeyColumnName)
  protected String nameResourceKey;

  @Column(name = TableConfig.ExtensibleEnumerationDescriptionColumnName)
  protected String description;

  @Column(name = TableConfig.ExtensibleEnumerationSortOrderColumnName)
  protected int sortOrder = Integer.MAX_VALUE;

  @Column(name = TableConfig.ExtensibleEnumerationIsSystemValueColumnName)
  protected boolean isSystemValue;

  @Column(name = TableConfig.ExtensibleEnumerationIsDeletableColumnName)
  protected boolean isDeletable;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = TableConfig.ExtensibleEnumerationDeepThoughtJoinColumnName)
  protected DeepThought deepThought;


  public ExtensibleEnumeration() {
    this.isSystemValue = false;
    this.isDeletable = true;
  }

  public ExtensibleEnumeration(String name) {
    this();
    this.name = name;
  }

  public ExtensibleEnumeration(String nameResourceKey, boolean isSystemValue, boolean isDeletable) {
    this.nameResourceKey = nameResourceKey;
    this.isSystemValue = isSystemValue;
    this.isDeletable = isDeletable;
  }

  public ExtensibleEnumeration(String nameResourceKey, boolean isSystemValue, boolean isDeletable, int sortOrder) {
    this(nameResourceKey, isSystemValue, isDeletable);
    this.sortOrder = sortOrder;
  }



  public String getName() {
    if(name != name && name.isEmpty() == false) // if name is explicitly set, use that value
      return name;
    else if(nameResourceKey != null) // but usually a resource key is set, so translate name to User's language
      return Localization.getLocalizedString(nameResourceKey);

    return name;
  }

  public void setName(String name) {
    Object previousValue = this.name;
    this.name = name;
    callPropertyChangedListeners(TableConfig.ExtensibleEnumerationNameColumnName, previousValue, name);
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    Object previousValue = this.description;
    this.description = description;
    callPropertyChangedListeners(TableConfig.ExtensibleEnumerationDescriptionColumnName, previousValue, description);
  }

  public int getSortOrder() {
    return sortOrder;
  }

  public void setSortOrder(int sortOrder) {
    Object previousValue = this.sortOrder;
    this.sortOrder = sortOrder;
    callPropertyChangedListeners(TableConfig.ExtensibleEnumerationSortOrderColumnName, previousValue, sortOrder);
  }

  public boolean isSystemValue() {
    return isSystemValue;
  }

  public boolean isDeletable() {
    return isDeletable;
  }

  public DeepThought getDeepThought() {
    return deepThought;
  }

  public void setDeepThought(DeepThought deepThought) {
    this.deepThought = deepThought;
  }

  @Override
  @Transient
  public String getTextRepresentation() {
    return getName();
  }

  @Override
  public int compareTo(ExtensibleEnumeration other) {
    if(other == null)
      return 1;

    return ((Integer)sortOrder).compareTo(other.getSortOrder());
  }
}
