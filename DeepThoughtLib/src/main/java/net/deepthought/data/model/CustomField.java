package net.deepthought.data.model;

import net.deepthought.data.model.enums.CustomFieldName;
import net.deepthought.data.persistence.db.TableConfig;
import net.deepthought.data.persistence.db.UserDataEntity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

/**
 * Created by ganymed on 12/10/14.
 */
@Entity(name = TableConfig.CustomFieldTableName)
public class CustomField extends UserDataEntity implements Serializable {

  private static final long serialVersionUID = 8491752502400707819L;

  @ManyToOne
  @JoinColumn(name = TableConfig.CustomFieldNameJoinColumnName)
  protected CustomFieldName name;

  @Column(name = TableConfig.CustomFieldValueColumnName)
  protected String value = "";

  @ManyToOne
  @JoinColumn(name = TableConfig.CustomFieldEntryJoinColumnName)
  protected Entry entry;

  @ManyToOne
  @JoinColumn(name = TableConfig.CustomFieldReferenceBaseJoinColumnName)
  protected ReferenceBase referenceBase;

  @Column(name = TableConfig.CustomFieldOrderColumnName)
  protected int order = Integer.MAX_VALUE;



  public CustomField() {

  }

  public CustomField(CustomFieldName name) {
    this();
    this.name = name;
  }

  public CustomField(CustomFieldName name, String value) {
    this(name);
    this.value = value;
  }


  public CustomFieldName getName() {
    return name;
  }

  public void setName(CustomFieldName name) {
    CustomFieldName previousName = this.name;
    this.name = name;
    callPropertyChangedListeners(TableConfig.CustomFieldNameJoinColumnName, previousName, name);
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    String previousDescription = this.value;
    this.value = value;
    callPropertyChangedListeners(TableConfig.CustomFieldValueColumnName, previousDescription, value);
  }

  public Entry getEntry() {
    return entry;
  }

  protected void setEntry(Entry entry) {
    Entry previousValue = this.entry;
    this.entry = entry;
    callPropertyChangedListeners(TableConfig.CustomFieldEntryJoinColumnName, previousValue, value);

    if(entry != null)
      setOrder(entry.getCustomFields().size());
  }

  public ReferenceBase getReferenceBase() {
    return referenceBase;
  }

  protected void setReferenceBase(ReferenceBase referenceBase) {
    ReferenceBase previousValue = this.referenceBase;
    this.referenceBase = referenceBase;
    callPropertyChangedListeners(TableConfig.CustomFieldReferenceBaseJoinColumnName, previousValue, value);

    if(referenceBase != null)
      setOrder(referenceBase.getCustomFields().size());
  }

  public int getOrder() {
    return order;
  }

  public void setOrder(int order) {
    int previousValue = this.order;
    this.order = order;
    callPropertyChangedListeners(TableConfig.CustomFieldOrderColumnName, previousValue, value);
  }


  @Override
  @Transient
  public String getTextRepresentation() {
    return "CustomField " + name + ": " + value;
  }

  @Override
  public String toString() {
    return getTextRepresentation();
  }

}
