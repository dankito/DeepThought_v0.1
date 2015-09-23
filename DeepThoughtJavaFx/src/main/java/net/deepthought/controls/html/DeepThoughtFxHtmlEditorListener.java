package net.deepthought.controls.html;

import net.deepthought.controller.Dialogs;
import net.deepthought.controller.enums.FieldWithUnsavedChanges;
import net.deepthought.controls.utils.IEditedEntitiesHolder;
import net.deepthought.data.model.FileLink;

import java.util.Collection;

/**
 * Created by ganymed on 22/09/15.
 */
public class DeepThoughtFxHtmlEditorListener implements IHtmlEditorListener {

  protected Collection<FieldWithUnsavedChanges> fieldsWithUnsavedChanges;

  protected FieldWithUnsavedChanges fieldToAddOnChanges;

  protected IEditedEntitiesHolder<FileLink> editedFilesHolder;


  public DeepThoughtFxHtmlEditorListener(Collection<FieldWithUnsavedChanges> fieldsWithUnsavedChanges, FieldWithUnsavedChanges fieldToAddOnChanges) {
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
  public void imageHasBeenDeleted(String imageId, String imageUrl) {
    Long fileId = Long.parseLong(imageId);

    for(FileLink file : editedFilesHolder.getEditedEntities()) {
      if(fileId.equals(file.getId())) { // TODO: what if the same image has been inserted multiple times into the document?
        editedFilesHolder.removeEntityFromEntry(file);
        break;
      }
    }
  }

  protected boolean handleImageCommand(HtmlEditor editor) {
    final FileLink newFile = new FileLink();

    Dialogs.showEditEmbeddedFileDialog(editor, newFile, null);

    return true;
  }


  public IEditedEntitiesHolder<FileLink> getEditedFilesHolder() {
    return editedFilesHolder;
  }

  public void setEditedFilesHolder(IEditedEntitiesHolder<FileLink> editedFilesHolder) {
    this.editedFilesHolder = editedFilesHolder;
  }
}
