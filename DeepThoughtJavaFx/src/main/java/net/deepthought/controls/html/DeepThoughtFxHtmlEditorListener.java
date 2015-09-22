package net.deepthought.controls.html;

import net.deepthought.controller.ChildWindowsController;
import net.deepthought.controller.ChildWindowsControllerListener;
import net.deepthought.controller.Dialogs;
import net.deepthought.controller.enums.DialogResult;
import net.deepthought.controller.enums.FieldWithUnsavedChanges;
import net.deepthought.controls.utils.IEditedEntitiesHolder;
import net.deepthought.data.model.FileLink;

import java.util.Collection;

import javafx.stage.Stage;

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

    Dialogs.showEditFileDialog(newFile, new ChildWindowsControllerListener() {
      @Override
      public void windowClosing(Stage stage, ChildWindowsController controller) {

      }

      @Override
      public void windowClosed(Stage stage, ChildWindowsController controller) {
        if(controller.getDialogResult() == DialogResult.Ok) {
          if(editedFilesHolder != null) {
            editedFilesHolder.addEntityToEntry(newFile);
            editor.insertHtml("<img src='" + newFile.getUriString() + "' imageid='" + newFile.getId() + "' alt='" + newFile.getNotes() + "' ></img>");
          }
        }
      }
    });

    return true;
  }


  public IEditedEntitiesHolder<FileLink> getEditedFilesHolder() {
    return editedFilesHolder;
  }

  public void setEditedFilesHolder(IEditedEntitiesHolder<FileLink> editedFilesHolder) {
    this.editedFilesHolder = editedFilesHolder;
  }
}
