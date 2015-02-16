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
public abstract class PersonRoleTestBase extends EditableExtensibleEnumerationTestBase<PersonRole> {

  @Override
  protected ExtensibleEnumeration getExistingExtensibleEnumeration() {
    DeepThought deepThought = Application.getDeepThought();
    List<PersonRole> personRoles = new ArrayList<>(deepThought.getPersonRoles());

    return personRoles.get(0);
  }

  @Override
  protected String getEnumerationTableName() {
    return TableConfig.PersonRoleTableName;
  }


  @Override
  protected PersonRole createNewEnumValue() {
    return new PersonRole("Love");
  }

  @Override
  protected void addToEnumeration(PersonRole enumValue) {
    Application.getDeepThought().addPersonRole(enumValue);
  }

  @Override
  protected void removeFromEnumeration(PersonRole enumValue) {
    Application.getDeepThought().removePersonRole(enumValue);
  }

  @Override
  protected Collection<PersonRole> getEnumeration() {
    return Application.getDeepThought().getPersonRoles();
  }

}
