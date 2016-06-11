package net.dankito.deepthought.controls.file;

import net.dankito.deepthought.data.model.FileLink;
import net.dankito.deepthought.data.model.listener.EntityListener;
import net.dankito.deepthought.data.persistence.db.BaseEntity;
import net.dankito.deepthought.util.localization.Localization;

import java.util.Collection;

import javafx.geometry.Side;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

/**
 * Created by ganymed on 01/02/15.
 */
public class FileLabel extends net.dankito.deepthought.controls.CollectionItemLabel {

  protected FileLink file;

  protected ContextMenu contextMenu = null;


  public FileLabel(FileLink file) {
    this.file = file;

    file.addEntityListener(fileListener);

    this.setOnMousePressed(event -> onMousePressedOrReleased(event));
    this.setOnMouseReleased(event -> onMousePressedOrReleased(event));

    setUserData(file);
    itemDisplayNameUpdated();
  }


  @Override
  public void cleanUp() {
    super.cleanUp();

    if(file != null)
      file.removeEntityListener(fileListener);
  }

  @Override
  protected String getItemDisplayName() {
    if(file != null)
      return file.getName();
    return "";
  }

  @Override
  protected String getToolTipText() {
    if(file != null)
      return file.getUriString();
    return "";
  }

  protected void onMousePressedOrReleased(MouseEvent event) {
    if(event.isPopupTrigger()) {
      event.consume();
      showContextMenu();
    }
  }

  @Override
  protected void onLabelClickedAction(MouseEvent event) {
    super.onLabelClickedAction(event);

    if(onLabelClickedEventHandler == null) {
      if(event.getButton() == MouseButton.PRIMARY) {
        if(event.getClickCount() == 1) {
          event.consume();
          net.dankito.deepthought.controller.Dialogs.showEditFileDialog(this.file);
        }
      }
      else if(event.isPopupTrigger()) {
        event.consume();
        showContextMenu();
      }
    }
  }

  protected void showContextMenu() {
    if(contextMenu == null)
      contextMenu = createContextMenu();

    contextMenu.show(this, Side.BOTTOM, 0, 0);
  }

  protected ContextMenu createContextMenu() {
    ContextMenu contextMenu = new ContextMenu();

    MenuItem editMenuItem = new MenuItem(Localization.getLocalizedString("edit"));
    editMenuItem.setOnAction(event -> net.dankito.deepthought.controller.Dialogs.showEditFileDialog(this.file));
    contextMenu.getItems().add(editMenuItem);

//    contextMenu.getItems().add(new SeparatorMenuItem());
//
//    MenuItem copyReferenceTextMenuItem = new MenuItem(Localization.getLocalizedString("copy.person.text.to.clipboard"));
//    copyReferenceTextMenuItem.setOnAction(event -> ClipboardHelper.copyStringToClipboard(getToolTipText()));
//    contextMenu.getItems().add(copyReferenceTextMenuItem);

    return contextMenu;
  }


  EntityListener fileListener = new EntityListener() {
    @Override
    public void propertyChanged(BaseEntity entity, String propertyName, Object previousValue, Object newValue) {
      itemDisplayNameUpdated();
    }

    @Override
    public void entityAddedToCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity addedEntity) {

    }

    @Override
    public void entityOfCollectionUpdated(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity updatedEntity) {

    }

    @Override
    public void entityRemovedFromCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity removedEntity) {

    }
  };

}
