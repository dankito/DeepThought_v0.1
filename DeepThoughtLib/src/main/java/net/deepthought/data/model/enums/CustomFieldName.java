package net.deepthought.data.model.enums;

import net.deepthought.data.model.CustomField;
import net.deepthought.data.persistence.db.TableConfig;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;

/**
 * Created by ganymed on 16/03/15.
 */
@Entity(name = TableConfig.CustomFieldNameTableName)
public class CustomFieldName extends ExtensibleEnumeration {

  private static final long serialVersionUID = 744938613663917343L;


  @OneToMany(fetch = FetchType.LAZY, mappedBy = "name")
  protected Set<CustomField> customFields = new HashSet<>();


  public CustomFieldName() {
  }

  public CustomFieldName(String name) {
    super(name);
  }

}
