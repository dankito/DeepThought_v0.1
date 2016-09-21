package net.dankito.deepthought.javafx.dialogs.mainwindow.tabs.tags.table;

import net.dankito.deepthought.Application;
import net.dankito.deepthought.data.listener.AllEntitiesListener;
import net.dankito.deepthought.data.model.Tag;
import net.dankito.deepthought.data.model.ui.SystemTag;
import net.dankito.deepthought.data.persistence.db.BaseEntity;
import net.dankito.deepthought.data.search.specific.TagsSearchResults;
import net.dankito.deepthought.util.localization.Localization;

import java.util.Collection;

import javafx.application.Platform;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.ContextMenuEvent;

/**
 * Created by ganymed on 27/12/14.
 */
public class TagNameTableCell extends net.dankito.deepthought.controls.TextFieldTableCell<Tag> {

  protected Tag tag = null;

  protected TagsSearchResults lastTagsSearchResults = null;


  public TagNameTableCell(net.dankito.deepthought.javafx.dialogs.mainwindow.tabs.tags.ITagsFilter tagsFilter) {
    this.lastTagsSearchResults = tagsFilter.getLastTagsSearchResults();
    tagsFilter.addDisplayedTagsChangedListener(results -> {
      lastTagsSearchResults = results;
      setCellBackgroundColor();
    });

    Application.getEntityChangesService().addAllEntitiesListener(allEntitiesListener);

    setOnContextMenuRequested(event -> showContextMenu(event));
  }


  @Override
  protected void editingItemDone(String newValue, String oldValue) {
    if(tag != null)
      tag.setName(newValue);
  }

  @Override
  public String getItemTextRepresentation() {
    return tag == null ? "" : tag.getTextRepresentation();
  }

  @Override
  protected String getItemEditingTextFieldText() {
    return tag == null ? "" : tag.getName();
  }

  @Override
  protected void newItemSet(Tag newValue) {
    super.newItemSet(newValue);
    tagChanged(newValue);
    setCellBackgroundColor();

    setEditable(newValue instanceof SystemTag);
  }

  protected void tagChanged(Tag tag) {
    this.tag = tag;

    setItem(getItemTextRepresentation());
    tagUpdated();
  }

  @Override
  public void reallyStartEdit() {
    if(tag instanceof SystemTag == false)
      super.reallyStartEdit();
  }

  @Override
  public void updateItem(String item, boolean empty) {
    if(getTableRow() != null && getTableRow().getItem() != tag)
      newItemSet((Tag) getTableRow().getItem());

    super.updateItem(item, empty);
  }

  protected void tagUpdated() {
    updateItem(getItemTextRepresentation(), getItemTextRepresentation().isEmpty());
  }


  protected void showContextMenu(ContextMenuEvent event) {
    if(tag instanceof SystemTag)
      return;

    ContextMenu contextMenu = createContextMenu();

    contextMenu.show(event.getPickResult().getIntersectedNode(), event.getScreenX(), event.getScreenY());
  }

  protected ContextMenu createContextMenu() {
    ContextMenu contextMenu = new ContextMenu();

    MenuItem renameTagMenuItem = new MenuItem(Localization.getLocalizedString("rename"));
    renameTagMenuItem.setId("mnitmRename");
    contextMenu.getItems().add(renameTagMenuItem);

    renameTagMenuItem.setOnAction((event) -> {
      reallyStartEdit();
    });

    contextMenu.getItems().add(new SeparatorMenuItem());

    MenuItem deleteTagMenuItem = new MenuItem(Localization.getLocalizedString("delete"));
    deleteTagMenuItem.setId("mnitmDelete");
    contextMenu.getItems().add(deleteTagMenuItem);

    deleteTagMenuItem.setOnAction((event) -> {
      net.dankito.deepthought.util.Alerts.deleteTagWithUserConfirmationIfIsSetOnEntries(tag);
    });

    return contextMenu;
  }

  protected void setCellBackgroundColor() {
    net.dankito.deepthought.controls.utils.FXUtils.setTagCellBackgroundColor(tag, lastTagsSearchResults, this);
  }


  protected AllEntitiesListener allEntitiesListener = new AllEntitiesListener() {
    @Override
    public void entityCreated(BaseEntity entity) {

    }

    @Override
    public void entityUpdated(BaseEntity entity, String propertyName, Object previousValue, Object newValue) {
      if(entity == tag) {
        callTagUpdatedThreadSafe();
      }
    }

    @Override
    public void entityDeleted(BaseEntity entity) {

    }

    @Override
    public void entityAddedToCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity addedEntity) {
      if(collectionHolder.equals(tag)) {
        callTagUpdatedThreadSafe();
      }
    }

    @Override
    public void entityRemovedFromCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity removedEntity) {
      if(collectionHolder.equals(tag)) {
        callTagUpdatedThreadSafe();
      }
    }
  };

  protected void callTagUpdatedThreadSafe() {
    if(Platform.isFxApplicationThread()) {
      tagUpdated();
    }
    else {
      Platform.runLater(() -> tagUpdated());
    }
  }

}
