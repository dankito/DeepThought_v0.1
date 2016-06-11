package net.dankito.deepthought.data.model.enums;

import net.dankito.deepthought.Application;
import net.dankito.deepthought.data.model.DeepThought;
import net.dankito.deepthought.data.persistence.db.TableConfig;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by ganymed on 10/11/14.
 */
public abstract class NoteTypeTestBase extends EditableExtensibleEnumerationTestBase<NoteType> {

  @Override
  protected ExtensibleEnumeration getExistingExtensibleEnumeration() {
    DeepThought deepThought = Application.getDeepThought();
    List<NoteType> noteTypes = new ArrayList<>(deepThought.getNoteTypes());

    return noteTypes.get(0);
  }

  @Override
  protected String getEnumerationTableName() {
    return TableConfig.NoteTypeTableName;
  }


  @Override
  protected NoteType createNewEnumValue() {
    return new NoteType("Love");
  }

  @Override
  protected void addToEnumeration(NoteType enumValue) {
    Application.getDeepThought().addNoteType(enumValue);
  }

  @Override
  protected void removeFromEnumeration(NoteType enumValue) {
    Application.getDeepThought().removeNoteType(enumValue);
  }

  @Override
  protected Collection<NoteType> getEnumeration() {
    return Application.getDeepThought().getNoteTypes();
  }

}
