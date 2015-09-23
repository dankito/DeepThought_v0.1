package net.deepthought.controls.html;

import net.deepthought.controller.Dialogs;
import net.deepthought.controls.utils.IEditedEntitiesHolder;
import net.deepthought.data.html.ImageElementData;
import net.deepthought.data.model.Entry;
import net.deepthought.data.model.FileLink;

import java.util.HashSet;
import java.util.Set;

import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;

/**
 * Created by ganymed on 22/09/15.
 */
public class EntryContentHtmlEditorListener implements IHtmlEditorListener, IEditedEntitiesHolder<FileLink> {

  protected Entry entry = null;


  public EntryContentHtmlEditorListener() {

  }


  @Override
  public void htmlCodeUpdated(String newHtmlCode) {
    if(entry != null)
      entry.setContent(newHtmlCode);
  }

  @Override
  public boolean handleCommand(HtmlEditor editor, HtmEditorCommand command) {
    if(command == HtmEditorCommand.Image)
      return handleImageCommand(editor);
    return false;
  }

  @Override
  public boolean elementDoubleClicked(HtmlEditor editor, ImageElementData elementData) {
    FileLink file = getEmbeddedFileById(elementData.getFileId());
    if(file != null) {
      Dialogs.showEditEmbeddedFileDialog(editor, this, file, elementData);
      return true;
    }

    return false;
  }

  @Override
  public void imageHasBeenDeleted(String imageId, String imageUrl) {
    if(entry != null) {
      Long fileId = Long.parseLong(imageId);

      for (FileLink file : entry.getFiles()) { // TODO: use embedded files
        if (fileId.equals(file.getId())) { // TODO: what if the same image has been inserted multiple times into the document?
          entry.removeFile(file);
          break;
        }
      }
    }
  }

  protected boolean handleImageCommand(HtmlEditor editor) {
    final FileLink newFile = new FileLink();

    Dialogs.showEditEmbeddedFileDialog(editor, this, newFile);

    return true;
  }

  protected FileLink getEmbeddedFileById(long fileId) {
    for(FileLink file : entry.getFiles()) {
      if(file.getId().equals(fileId))
        return file;
    }

    return null;
  }


  public Entry getEntry() {
    return entry;
  }

  public void setEntry(Entry entry) {
    this.entry = entry;
  }

  @Override
  public ObservableSet<FileLink> getEditedEntities() {
    return FXCollections.observableSet(new HashSet<FileLink>(entry.getFiles()));
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
      entry.addFile(entity);
  }

  @Override
  public void removeEntityFromEntry(FileLink entity) {
    if(entry != null)
      entry.removeFile(entity);
  }

  @Override
  public boolean containsEditedEntity(FileLink entity) {
    if(entry != null)
      return entry.getFiles().contains(entity);
    return false;
  }

  @Override
  public void cleanUp() {

  }
}
