package net.deepthought.data.model;

import net.deepthought.data.model.enums.NoteType;
import net.deepthought.data.persistence.db.TableConfig;
import net.deepthought.data.persistence.db.UserDataEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

/**
 * Created by ganymed on 22/12/14.
 */
@Entity(name = TableConfig.NoteTableName)
public class Note extends UserDataEntity {

  @Column(name = TableConfig.NoteNoteColumnName)
  @Lob
  protected String note;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = TableConfig.NoteNoteTypeJoinColumnName)
  protected NoteType type;

//  @JsonIgnore
  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = TableConfig.NoteEntryJoinColumnName)
  protected Entry entry;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = TableConfig.NoteDeepThoughtJoinColumnName)
  protected DeepThought deepThought;


  public Note() {
    this.type = NoteType.getDefaultNoteType();
  }

  public Note(String note) {
    this();
    this.note = note;
  }


  public String getNote() {
    return note;
  }

  public void setNote(String note) {
    Object previousValue = this.note;
    this.note = note;
    callPropertyChangedListeners(TableConfig.NoteNoteColumnName, previousValue, note);
  }

  public NoteType getType() {
    return type;
  }

  public void setType(NoteType type) {
    Object previousValue = this.type;

    if(this.type != type) {
      if (this.type != null)
        this.type.removeNote(this);

      this.type = type;

      if (type != null)
        type.addNote(this);

      callPropertyChangedListeners(TableConfig.NoteNoteTypeJoinColumnName, previousValue, note);
    }
  }

  public Entry getEntry() {
    return entry;
  }

  protected void setEntry(Entry entry) {
    Object previousValue = this.entry;
    this.entry = entry;
    callPropertyChangedListeners(TableConfig.NoteEntryJoinColumnName, previousValue, entry);
  }

  public DeepThought getDeepThought() {
    return deepThought;
  }


  @Override
  @Transient
  public String getTextRepresentation() {
    return "Note " + note;
  }

  @Override
  public String toString() {
    String description = getTextRepresentation();

    if(entry == null)
      description += " Entry == null, is Note deleted? " + isDeleted();
    else
      description += " (on " + entry + ")";

    return description;
  }

}
