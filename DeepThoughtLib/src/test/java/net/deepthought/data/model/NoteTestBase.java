package net.deepthought.data.model;

import net.deepthought.Application;
import net.deepthought.data.model.enums.NoteType;
import net.deepthought.data.persistence.db.TableConfig;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ganymed on 14/02/15.
 */
public abstract class NoteTestBase extends DataModelTestBase {

  @Test
  public void updateNote_UpdatedValueGetsPersistedInDb() throws Exception {
    Note note = new Note("test note");
    Entry entry = new Entry("test");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addEntry(entry);
    entry.addNote(note);

    String newValue = "Updated note";
    note.setNote(newValue);

    String actual = getClobFromTable(TableConfig.NoteTableName, TableConfig.NoteNoteColumnName, note.getId());
    Assert.assertEquals(newValue, actual);
  }

  @Test
  public void setNoteWithMoreThan2048Characters_UpdatedValueGetsPersistedInDb() throws Exception {
    Note note = new Note("test note");
    Entry entry = new Entry("test");

    DeepThought deepThought = Application.getDeepThought();
    deepThought.addEntry(entry);
    entry.addNote(note);

    String longNote = DataModelTestBase.StringWithMoreThan2048CharactersLength;
    note.setNote(longNote);

    String actual = getClobFromTable(TableConfig.NoteTableName, TableConfig.NoteNoteColumnName, note.getId());
    Assert.assertEquals(longNote, actual);
  }

  @Test
  public void setNoteType_RelationGetsPersistedInDb() throws Exception {
    Note note = new Note("test note");
    Entry entry = new Entry("test");

    DeepThought deepThought = Application.getDeepThought();
    List<NoteType> noteTypes = new ArrayList<>(deepThought.getNoteTypes());
    deepThought.addEntry(entry);
    entry.addNote(note);

    NoteType noteType = noteTypes.get(0);
    note.setType(noteType);

    Assert.assertTrue(doIdsEqual(noteType.getId(), getValueFromTable(TableConfig.NoteTableName, TableConfig.NoteNoteTypeJoinColumnName, note.getId())));
  }

  @Test
  public void updateNoteType_RelationGetsPersistedInDb() throws Exception {
    Note note = new Note("test note");
    Entry entry = new Entry("test");

    DeepThought deepThought = Application.getDeepThought();
    List<NoteType> noteTypes = new ArrayList<>(deepThought.getNoteTypes());
    note.setType(noteTypes.get(0));
    deepThought.addEntry(entry);
    entry.addNote(note);

    NoteType updatedNoteType = noteTypes.get(1);
    note.setType(updatedNoteType);

    Assert.assertTrue(doIdsEqual(updatedNoteType.getId(), getValueFromTable(TableConfig.NoteTableName, TableConfig.NoteNoteTypeJoinColumnName, note.getId())));
  }

  @Test
  public void updateNoteType_NoteGetsNotDeletedFromDb() throws Exception {
    Note note = new Note("test note");
    Entry entry = new Entry("test");

    DeepThought deepThought = Application.getDeepThought();
    List<NoteType> noteTypes = new ArrayList<>(deepThought.getNoteTypes());
    NoteType firstNoteType = noteTypes.get(0);
    note.setType(firstNoteType);
    deepThought.addEntry(entry);
    entry.addNote(note);

    NoteType updatedNoteType = noteTypes.get(1);
    note.setType(updatedNoteType);

    Assert.assertFalse(note.isDeleted());
    Assert.assertNotNull(note.getId());
  }

  @Test
  public void updateNoteType_PreviousNoteTypeGetsNotDeletedFromDb() throws Exception {
    Note note = new Note("test note");
    Entry entry = new Entry("test");

    DeepThought deepThought = Application.getDeepThought();
    List<NoteType> noteTypes = new ArrayList<>(deepThought.getNoteTypes());
    NoteType firstNoteType = noteTypes.get(0);
    note.setType(firstNoteType);
    deepThought.addEntry(entry);
    entry.addNote(note);

    NoteType updatedNoteType = noteTypes.get(1);
    note.setType(updatedNoteType);

    Assert.assertFalse(firstNoteType.isDeleted());
    Assert.assertNotNull(firstNoteType.getId());
  }

}
