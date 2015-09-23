package net.deepthought.controls.html;

import net.deepthought.controller.ChildWindowsController;
import net.deepthought.controller.ChildWindowsControllerListener;
import net.deepthought.controller.Dialogs;
import net.deepthought.controller.enums.DialogResult;
import net.deepthought.data.model.Entry;
import net.deepthought.data.model.FileLink;

import javafx.stage.Stage;

/**
 * Created by ganymed on 22/09/15.
 */
public class EntryContentHtmlEditorListener implements IHtmlEditorListener {

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

    Dialogs.showEditFileDialog(newFile, new ChildWindowsControllerListener() {
      @Override
      public void windowClosing(Stage stage, ChildWindowsController controller) {

      }

      @Override
      public void windowClosed(Stage stage, ChildWindowsController controller) {
        if(controller.getDialogResult() == DialogResult.Ok) {
          if(entry != null) {
            entry.addFile(newFile);
            editor.insertHtml("<img src='" + newFile.getUriString() + "' imageid='" + newFile.getId() + "' alt='" + newFile.getDescription() + "'");
          }
        }
      }
    });

    return true;
  }


  public Entry getEntry() {
    return entry;
  }

  public void setEntry(Entry entry) {
    this.entry = entry;
  }

}
