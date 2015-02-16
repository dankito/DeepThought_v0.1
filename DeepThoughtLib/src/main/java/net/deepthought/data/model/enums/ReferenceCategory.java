package net.deepthought.data.model.enums;

import net.deepthought.data.model.Reference;
import net.deepthought.data.persistence.db.TableConfig;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;

/**
 * Created by ganymed on 21/01/15.
 */
@Entity(name = TableConfig.ReferenceCategoryTableName)
public class ReferenceCategory extends ExtensibleEnumeration {

  private static final long serialVersionUID = -7101946749967624895L;


  @OneToMany(fetch = FetchType.LAZY, mappedBy = "category")
  protected Set<Reference> references = new HashSet<>();


  public ReferenceCategory() {

  }

  public ReferenceCategory(String name) {
    super(name);
  }

  public ReferenceCategory(String nameResourceKey, boolean isSystemValue, boolean isDeletable, int sortOrder) {
    super(nameResourceKey, isSystemValue, isDeletable, sortOrder);
  }


  public Set<Reference> getReferences() {
    return references;
  }

  public boolean addReference(Reference reference) {
    if(references.add(reference)) {
      callEntityAddedListeners(references, reference);
      return true;
    }

    return false;
  }

  public boolean removeReference(Reference reference) {
    if(references.remove(reference)) {
      callEntityRemovedListeners(references, reference);
      return true;
    }

    return false;
  }


  @Override
  public String toString() {
    return "ReferenceCategory " + getTextRepresentation();
  }

}
