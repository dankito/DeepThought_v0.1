package net.deepthought.controls.file.cells;

import net.deepthought.controls.utils.IEditedEntitiesHolder;
import net.deepthought.data.model.FileLink;
import net.deepthought.util.localization.Localization;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;

/**
 * Created by ganymed on 24/09/15.
 */
public abstract class EntityFilesOverviewTreeTableCellBase extends FileTreeTableCellBase {

  protected IEditedEntitiesHolder<FileLink> editedFiles = null;


  public EntityFilesOverviewTreeTableCellBase(IEditedEntitiesHolder<FileLink> editedFiles) {
    this.editedFiles = editedFiles;
  }


  @Override
  protected ContextMenu createContextMenu() {
    ContextMenu contextMenu = super.createContextMenu();

    MenuItem removeFromEntityItem = new MenuItem(Localization.getLocalizedString("remove.from.entity"));
    removeFromEntityItem.setOnAction(event -> removeFileFromEntity());
    int index = contextMenu.getItems().size() - 1; // insert before Delete from DeepThought menu item
    contextMenu.getItems().add(index, removeFromEntityItem);

    return contextMenu;
  }

  protected void removeFileFromEntity() {
    if(editedFiles != null) {
      editedFiles.removeEntityFromEntry(file);
    }
  }
}
