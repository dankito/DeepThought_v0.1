package net.dankito.deepthought.controls.person;

import net.dankito.deepthought.data.model.Person;
import net.dankito.deepthought.data.model.listener.EntityListener;
import net.dankito.deepthought.data.persistence.db.BaseEntity;
import net.dankito.deepthought.util.localization.Localization;

import java.util.Collection;

import javafx.geometry.Side;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

/**
 * Created by ganymed on 01/02/15.
 */
public class PersonLabel extends net.dankito.deepthought.controls.CollectionItemLabel {

  protected Person person;

  protected ContextMenu contextMenu = null;


  public PersonLabel(Person person) {
    this.person = person;

    person.addEntityListener(personListener);

    this.setOnMousePressed(event -> onMousePressedOrReleased(event));
    this.setOnMouseReleased(event -> onMousePressedOrReleased(event));

    setUserData(person);
    itemDisplayNameUpdated();
  }


  @Override
  public void cleanUp() {
    super.cleanUp();

    if(person != null)
      person.removeEntityListener(personListener);
  }

  @Override
  protected String getItemDisplayName() {
    if(person != null)
      return person.getNameRepresentation();
    return "";
  }

  @Override
  protected String getToolTipText() {
    if(person != null)
      return person.getNameRepresentationStartingWithFirstName();
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
          net.dankito.deepthought.controller.Dialogs.showEditPersonDialog(this.person);
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
    net.dankito.deepthought.controls.utils.FXUtils.addStyleToCurrentStyle(editMenuItem, "-fx-font-weight: bold;");
    editMenuItem.setOnAction(event -> net.dankito.deepthought.controller.Dialogs.showEditPersonDialog(this.person));
    contextMenu.getItems().add(editMenuItem);

    contextMenu.getItems().add(new SeparatorMenuItem());

    MenuItem copyReferenceTextMenuItem = new MenuItem(Localization.getLocalizedString("copy.person.text.to.clipboard"));
    copyReferenceTextMenuItem.setOnAction(event -> net.dankito.deepthought.util.ClipboardHelper.copyStringToClipboard(getToolTipText()));
    contextMenu.getItems().add(copyReferenceTextMenuItem);

    return contextMenu;
  }


  EntityListener personListener = new EntityListener() {
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
