package net.dankito.deepthought.controls.reference;

import net.dankito.deepthought.Application;
import net.dankito.deepthought.controls.utils.FXUtils;
import net.dankito.deepthought.data.contentextractor.EntryCreationResult;
import net.dankito.deepthought.data.model.Reference;
import net.dankito.deepthought.data.model.ReferenceBase;
import net.dankito.deepthought.data.model.ReferenceSubDivision;
import net.dankito.deepthought.data.model.enums.ApplicationLanguage;
import net.dankito.deepthought.data.model.listener.EntityListener;
import net.dankito.deepthought.data.persistence.db.BaseEntity;
import net.dankito.deepthought.util.StringUtils;
import net.dankito.deepthought.util.localization.LanguageChangedListener;
import net.dankito.deepthought.util.localization.Localization;

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
public class EntryReferenceBaseLabel extends net.dankito.deepthought.controls.CollectionItemLabel {

  protected ReferenceBase referenceBase;

  protected EntryCreationResult creationResult = null;

  protected String parentReferenceBaseInfo = null;

  protected ContextMenu contextMenu = null;


  public EntryReferenceBaseLabel(ReferenceBase referenceBase, EventHandler<net.dankito.deepthought.controls.event.CollectionItemLabelEvent> onButtonRemoveItemFromCollectionEventHandler) {
    super(onButtonRemoveItemFromCollectionEventHandler);
    this.referenceBase = referenceBase;

    referenceBase.addEntityListener(referenceBaseListener);

    Localization.addLanguageChangedListener(languageChangedListener);

    this.setOnMousePressed(event -> onMousePressedOrReleased(event));
    this.setOnMouseReleased(event -> onMousePressedOrReleased(event));

    itemDisplayNameUpdated();
  }

  public EntryReferenceBaseLabel(ReferenceBase referenceBase, EntryCreationResult creationResult, EventHandler<net.dankito.deepthought.controls.event.CollectionItemLabelEvent> onButtonRemoveItemFromCollectionEventHandler) {
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

    if(referenceBase != null) {
      referenceBase.removeEntityListener(referenceBaseListener);
    }

    this.creationResult = null;

    Localization.removeLanguageChangedListener(languageChangedListener);
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
      net.dankito.deepthought.controller.Dialogs.showEditReferenceDialog(this.referenceBase);
    else
      net.dankito.deepthought.controller.Dialogs.showEditReferenceDialog(creationResult);
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
    copyReferenceTextMenuItem.setOnAction(event -> Application.getClipboardHelper().copyStringToClipboard(getItemDisplayName()));
    contextMenu.getItems().add(copyReferenceTextMenuItem);

    final String referenceUrl = getReferenceUrl(); // TODO: probably wrong as Reference Url may gets set later after creating this ContextMenu
    if(referenceUrl != null) {
      MenuItem copyReferenceUrlMenuItem = new MenuItem(Localization.getLocalizedString("copy.reference.url.to.clipboard"));
      copyReferenceUrlMenuItem.setOnAction(event -> Application.getClipboardHelper().copyUrlToClipboard(referenceUrl));
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


  protected LanguageChangedListener languageChangedListener = new LanguageChangedListener() {
    @Override
    public void languageChanged(ApplicationLanguage newLanguage) {
      EntryReferenceBaseLabel.this.languageChanged();
    }
  };

  protected void languageChanged() {
    if(referenceBase instanceof Reference) { // right now only Reference has a Locale dependent preview (when displaying Dates)
      ((Reference)referenceBase).resetPreview();
    }

    itemDisplayNameUpdated();
  }

}
