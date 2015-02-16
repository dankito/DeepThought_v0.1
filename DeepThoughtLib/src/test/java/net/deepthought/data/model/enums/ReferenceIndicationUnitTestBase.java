package net.deepthought.data.model.enums;

import net.deepthought.Application;
import net.deepthought.data.model.DeepThought;
import net.deepthought.data.persistence.db.TableConfig;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by ganymed on 10/11/14.
 */
public abstract class ReferenceIndicationUnitTestBase extends EditableExtensibleEnumerationTestBase<ReferenceIndicationUnit> {

  @Override
  protected ExtensibleEnumeration getExistingExtensibleEnumeration() {
    DeepThought deepThought = Application.getDeepThought();
    List<ReferenceIndicationUnit> referenceIndicationUnits = new ArrayList<>(deepThought.getReferenceIndicationUnits());

    return referenceIndicationUnits.get(0);
  }

  @Override
  protected String getEnumerationTableName() {
    return TableConfig.ReferenceIndicationUnitTableName;
  }


  @Override
  protected ReferenceIndicationUnit createNewEnumValue() {
    return new ReferenceIndicationUnit("Love");
  }

  @Override
  protected void addToEnumeration(ReferenceIndicationUnit enumValue) {
    Application.getDeepThought().addReferenceIndicationUnit(enumValue);
  }

  @Override
  protected void removeFromEnumeration(ReferenceIndicationUnit enumValue) {
    Application.getDeepThought().removeReferenceIndicationUnit(enumValue);
  }

  @Override
  protected Collection<ReferenceIndicationUnit> getEnumeration() {
    return Application.getDeepThought().getReferenceIndicationUnits();
  }

}
