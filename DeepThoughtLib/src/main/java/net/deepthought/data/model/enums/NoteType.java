package net.deepthought.data.model.enums;

import net.deepthought.Application;
import net.deepthought.data.model.Note;
import net.deepthought.data.persistence.db.TableConfig;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;

/**
 * Created by ganymed on 03/01/15.
 */
@Entity(name = TableConfig.NoteTypeTableName)
public class NoteType extends ExtensibleEnumeration {

  private static final long serialVersionUID = 9127576069157935076L;


  @OneToMany(fetch = FetchType.LAZY, mappedBy = "type"/*, cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH }*/)
  protected Set<Note> notes = new HashSet<>();


  public NoteType() {

  }

  public NoteType(String name) {
    super(name);
  }

  public NoteType(String nameResourceKey, boolean isSystemValue, boolean isDeletable, int sortOrder) {
    super(nameResourceKey, isSystemValue, isDeletable, sortOrder);
  }


  public Set<Note> getNotes() {
    return notes;
  }

  public boolean addNote(Note note) {
    if(notes.add(note)) {
      callEntityAddedListeners(notes, note);
      return true;
    }

    return false;
  }

  public boolean removeNote(Note note) {
    if(notes.remove(note)) {
      callEntityRemovedListeners(notes, note);
      return true;
    }

    return false;
  }


  @Override
  public String toString() {
    return "NoteType " + getTextRepresentation();
  }


  public static void resetStaticCaches() {
    defaultNoteType = null;
  }

  protected static NoteType defaultNoteType = null;

  public static NoteType getDefaultNoteType() {
    if(defaultNoteType == null && Application.getDeepThought() != null) { // TODO: bad solution as then Note's NoteType stays null
      for(NoteType noteType : Application.getDeepThought().getNoteTypes()) {
        if("note.type.unset".equals(noteType.nameResourceKey)) {
          defaultNoteType = noteType;
          break;
        }
      }
    }

    return defaultNoteType;
  }

}
