package net.deepthought.controls.html;

import net.deepthought.controls.utils.IEditedEntitiesHolder;
import net.deepthought.data.model.Entry;
import net.deepthought.data.model.FileLink;

import java.util.HashSet;
import java.util.Set;

import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;

/**
 * Created by ganymed on 22/09/15.
 */
public class EntryContentHtmlEditorListener extends HtmlEditorListenerBase implements IHtmlEditorListener, IEditedEntitiesHolder<FileLink> {

  protected Entry entry = null;


  public EntryContentHtmlEditorListener() {
    editedFilesHolder = this;
  }


  @Override
  public void editorHasLoaded(HtmlEditor editor) {

  }

  @Override
  public void htmlCodeUpdated(String updatedHtmlCode) {
    if(entry != null) {
      handleEditedEmbeddedFiles(entry.getContent(), updatedHtmlCode); // TODO: we should not call expensive functions in this listener method -> find a better place to check for embedded files changes
      entry.setContent(updatedHtmlCode);
    }
  }


  public Entry getEntry() {
    return entry;
  }

  public void setEntry(Entry entry) {
    this.entry = entry;
  }

  @Override
  public ObservableSet<FileLink> getEditedEntities() {
    return FXCollections.observableSet(new HashSet<FileLink>(entry.getEmbeddedFiles()));
  }

  @Override
  public Set<FileLink> getAddedEntities() {
    return new HashSet<>();
  }

  @Override
  public Set<FileLink> getRemovedEntities() {
    return new HashSet<>();
  }

  @Override
  public void addEntityToEntry(FileLink entity) {
    if(entry != null)
      entry.addAttachedFile(entity);
  }

  @Override
  public void removeEntityFromEntry(FileLink entity) {
    if(entry != null)
      entry.removeAttachedFile(entity);
  }

  @Override
  public boolean containsEditedEntity(FileLink entity) {
    if(entry != null)
      return entry.getEmbeddedFiles().contains(entity);
    return false;
  }

  @Override
  public void cleanUp() {

  }
}
