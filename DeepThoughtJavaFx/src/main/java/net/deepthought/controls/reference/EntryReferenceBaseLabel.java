package net.deepthought.controls.reference;

import net.deepthought.controller.Dialogs;
import net.deepthought.controls.CollectionItemLabel;
import net.deepthought.controls.utils.FXUtils;
import net.deepthought.controls.event.CollectionItemLabelEvent;
import net.deepthought.data.contentextractor.EntryCreationResult;
import net.deepthought.data.model.Reference;
import net.deepthought.data.model.ReferenceBase;
import net.deepthought.data.model.ReferenceSubDivision;
import net.deepthought.data.model.listener.EntityListener;
import net.deepthought.data.persistence.db.BaseEntity;
import net.deepthought.util.ClipboardHelper;
import net.deepthought.util.Localization;
import net.deepthought.util.StringUtils;

import java.util.Collection;

import javafx.event.EventHandler;
import javafx.geometry.Side;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

/**
 * Created by ganymed on 08/04/15.
 */
public class EntryReferenceBaseLabel extends CollectionItemLabel {

  protected ReferenceBase referenceBase;

  protected EntryCreationResult creationResult = null;

  protected String parentReferenceBaseInfo = null;

  protected ContextMenu contextMenu = null;


  public EntryReferenceBaseLabel(ReferenceBase referenceBase, EventHandler<CollectionItemLabelEvent> onButtonRemoveItemFromCollectionEventHandler) {
    super(onButtonRemoveItemFromCollectionEventHandler);
    this.referenceBase = referenceBase;
    referenceBase.addEntityListener(referenceBaseListener);

    this.setOnMousePressed(event -> onMousePressedOrReleased(event));
    this.setOnMouseReleased(event -> onMousePressedOrReleased(event));

    itemDisplayNameUpdated();
  }

  public EntryReferenceBaseLabel(ReferenceBase referenceBase, EntryCreationResult creationResult, EventHandler<CollectionItemLabelEvent> onButtonRemoveItemFromCollectionEventHandler) {
    this(referenceBase, onButtonRemoveItemFromCollectionEventHandler);

    this.creationResult = creationResult;

    if(creationResult != null) {
      parentReferenceBaseInfo = createParentReferenceBaseInfo(creationResult, referenceBase);

      itemDisplayNameUpdated();
    }
  }


  protected String createParentReferenceBaseInfo(EntryCreationResult creationResult, ReferenceBase selectedReferenceBase) {
    if(selectedReferenceBase.isPersisted() == false) {
      if (selectedReferenceBase instanceof ReferenceSubDivision && ((ReferenceSubDivision)selectedReferenceBase).getReference() == null) {
        if(creationResult.getReference() != null) {
          Reference reference = creationResult.getReference();
          String parentInfo = reference.getTextRepresentation();

          if(reference.getSeries() == null && creationResult.getSeriesTitle() != null)
            parentInfo = creationResult.getSeriesTitle().getTextRepresentation() + (StringUtils.isNullOrEmpty(parentInfo) ? "" : " " + parentInfo);

          return parentInfo;
        }
      }
      else if(selectedReferenceBase instanceof Reference && ((Reference)selectedReferenceBase).getSeries() == null) {
        if(creationResult.getSeriesTitle() != null)
          return creationResult.getSeriesTitle().getTextRepresentation();
      }
    }

    return null;
  }



  @Override
  public void cleanUp() {
    super.cleanUp();

    if(referenceBase != null)
      referenceBase.removeEntityListener(referenceBaseListener);
  }

  @Override
  protected String getItemDisplayName() {
    if(referenceBase != null) {
      if(referenceBase.isPersisted() == false && StringUtils.isNotNullOrEmpty(parentReferenceBaseInfo))
        return parentReferenceBaseInfo + " " + referenceBase.getTextRepresentation();

      return referenceBase.getTextRepresentation();
    }
    return null;
  }

  @Override
  protected String getToolTipText() {
    return getItemDisplayName();
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
          showEditReferenceDialog();
        }
      }
      else if(event.isPopupTrigger()) {
        event.consume();
        showContextMenu();
      }
    }
  }

  protected void showEditReferenceDialog() {
    if(creationResult == null)
      Dialogs.showEditReferenceDialog(this.referenceBase);
    else
      Dialogs.showEditReferenceDialog(creationResult);
  }

  protected void showContextMenu() {
    if(contextMenu == null)
      contextMenu = createContextMenu();

    contextMenu.show(this, Side.BOTTOM, 0, 0);
  }

  protected ContextMenu createContextMenu() {
    ContextMenu contextMenu = new ContextMenu();

    MenuItem editMenuItem = new MenuItem(Localization.getLocalizedString("edit"));
    FXUtils.addStyleToCurrentStyle(editMenuItem, "-fx-font-weight: bold;");
    editMenuItem.setOnAction(event -> showEditReferenceDialog());
    contextMenu.getItems().add(editMenuItem);

    contextMenu.getItems().add(new SeparatorMenuItem());

    MenuItem copyReferenceTextMenuItem = new MenuItem(Localization.getLocalizedString("copy.reference.text.to.clipboard"));
    copyReferenceTextMenuItem.setOnAction(event -> ClipboardHelper.copyStringToClipboard(getItemDisplayName()));
    contextMenu.getItems().add(copyReferenceTextMenuItem);

    final String referenceUrl = getReferenceUrl();
    if(referenceUrl != null) {
      MenuItem copyReferenceUrlMenuItem = new MenuItem(Localization.getLocalizedString("copy.reference.url.to.clipboard"));
      copyReferenceUrlMenuItem.setOnAction(event -> ClipboardHelper.copyStringToClipboard(referenceUrl));
      contextMenu.getItems().add(copyReferenceUrlMenuItem);
    }

    return contextMenu;
  }

  protected String getReferenceUrl() {
    if(referenceBase != null && StringUtils.isNotNullOrEmpty(referenceBase.getOnlineAddress()))
      return referenceBase.getOnlineAddress();
    else if(referenceBase instanceof ReferenceSubDivision) {
      ReferenceSubDivision subDivision = (ReferenceSubDivision)referenceBase;
      if(subDivision.getReference() != null && StringUtils.isNotNullOrEmpty(subDivision.getReference().getOnlineAddress()))
        return subDivision.getReference().getOnlineAddress();
    }
    else if(referenceBase instanceof Reference) {
      Reference reference = (Reference)referenceBase;
      if(reference.getSeries() != null && StringUtils.isNotNullOrEmpty(reference.getSeries().getOnlineAddress()))
        return reference.getSeries().getOnlineAddress();
    }

    return null;
  }


  protected EntityListener referenceBaseListener = new EntityListener() {
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
