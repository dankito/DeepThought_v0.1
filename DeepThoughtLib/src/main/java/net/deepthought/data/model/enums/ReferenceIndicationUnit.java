package net.deepthought.data.model.enums;

import net.deepthought.data.persistence.db.TableConfig;

import javax.persistence.Entity;

/**
 * Created by ganymed on 21/01/15.
 */
@Entity(name = TableConfig.ReferenceIndicationUnitTableName)
public class ReferenceIndicationUnit extends ExtensibleEnumeration {

  private static final long serialVersionUID = -994702637647472197L;


  protected transient Class lengthTypeClass; // e.g. int (pages, ...), Time (Minutes:Seconds in a movie, ...), String (e.g. Roman Numbers)

  // TODO: Converter (e.g. for Time, Roman Numbers, ...)


  public ReferenceIndicationUnit() {

  }

  public ReferenceIndicationUnit(String name) {
    super(name);
  }

  public ReferenceIndicationUnit(String nameResourceKey, boolean isSystemValue, boolean isDeletable, int sortOrder) {
    super(nameResourceKey, isSystemValue, isDeletable, sortOrder);
  }


  @Override
  public String toString() {
    return "ReferenceIndicationUnit " + getTextRepresentation();
  }

}
