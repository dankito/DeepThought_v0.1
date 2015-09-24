package net.deepthought.controls.html;

import net.deepthought.Application;
import net.deepthought.controller.Dialogs;
import net.deepthought.controller.enums.FieldWithUnsavedChanges;
import net.deepthought.controls.utils.EditedEntitiesHolder;
import net.deepthought.controls.utils.IEditedEntitiesHolder;
import net.deepthought.data.html.ImageElementData;
import net.deepthought.data.model.FileLink;

import java.util.Collection;

/**
 * Created by ganymed on 22/09/15.
 */
public class DeepThoughtFxHtmlEditorListener implements IHtmlEditorListener {

  protected IEditedEntitiesHolder<FileLink> editedFilesHolder;

  protected Collection<FieldWithUnsavedChanges> fieldsWithUnsavedChanges;

  protected FieldWithUnsavedChanges fieldToAddOnChanges;


  public DeepThoughtFxHtmlEditorListener(EditedEntitiesHolder<FileLink> editedFiles, Collection<FieldWithUnsavedChanges> fieldsWithUnsavedChanges, FieldWithUnsavedChanges fieldToAddOnChanges) {
    this.editedFilesHolder = editedFiles;
    this.fieldsWithUnsavedChanges = fieldsWithUnsavedChanges;
    this.fieldToAddOnChanges = fieldToAddOnChanges;
  }


  @Override
  public void htmlCodeUpdated(String newHtmlCode) {
    fieldsWithUnsavedChanges.add(fieldToAddOnChanges);
  }

  @Override
  public boolean handleCommand(HtmlEditor editor, HtmEditorCommand command) {
    if(command == HtmEditorCommand.Image)
      return handleImageCommand(editor);
    return false;
  }

  @Override
  public boolean elementDoubleClicked(HtmlEditor editor, ImageElementData elementData) {
    FileLink file = getEditedFileById(elementData.getFileId());
    if(file != null) {
      Dialogs.showEditEmbeddedFileDialog(editor, editedFilesHolder, file, elementData);
      return true;
    }

    return false;
  }

  @Override
  public void imageAdded(ImageElementData addedImage) {
    FileLink file = getFileById(addedImage.getFileId());
    if(file != null) {
      editedFilesHolder.addEntityToEntry(file);
    }
  }

  @Override
  public void imageHasBeenDeleted(ImageElementData deletedImage, boolean isStillInAnotherInstanceOnHtml) {
    if(isStillInAnotherInstanceOnHtml == false) {
      FileLink file = getEditedFileById(deletedImage.getFileId());
      if(file != null) {
        editedFilesHolder.removeEntityFromEntry(file);
      }
    }
  }

  protected boolean handleImageCommand(HtmlEditor editor) {
    final FileLink newFile = new FileLink();

    Dialogs.showEditEmbeddedFileDialog(editor, editedFilesHolder, newFile);

    return true;
  }

  protected FileLink getEditedFileById(long fileId) {
    for(FileLink file : editedFilesHolder.getEditedEntities()) {
      if(file.getId().equals(fileId))
        return file;
    }

    return null;
  }

  protected FileLink getFileById(long fileId) {
    for(FileLink file : Application.getDeepThought().getFiles()) {
      if(file.getId().equals(fileId))
        return file;
    }

    return null;
  }


  public IEditedEntitiesHolder<FileLink> getEditedFilesHolder() {
    return editedFilesHolder;
  }

}
