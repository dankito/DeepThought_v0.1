package net.deepthought.controls.tabtags;

import net.deepthought.Application;
import net.deepthought.controls.TextFieldListCell;
import net.deepthought.data.model.Tag;
import net.deepthought.data.model.listener.EntityListener;
import net.deepthought.data.persistence.db.BaseEntity;
import net.deepthought.data.model.ui.SystemTag;
import net.deepthought.util.Alerts;

import java.util.Collection;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;

/**
 * Created by ganymed on 29/11/14.
 */
public class TagListCell extends TextFieldListCell<Tag> {

  protected Tag tag = null;


  @Override
  protected void editingItemDone(String newValue, String oldValue) {
    getItem().setName(newValue);
  }

  @Override
  public String getItemTextRepresentation() {
    return tag == null ? "" : tag.getName() + " (" + tag.getEntries().size() + ")";
  }

  @Override
  protected String getItemEditingTextFieldText() {
    return tag == null ? "" : tag.getName();
  }

  @Override
  protected void newItemSet(Tag newValue) {
    super.newItemSet(newValue);
    tagChanged(newValue);
  }

  protected void tagChanged(Tag tag) {
    if(this.tag != null) {
      this.tag.removeEntityListener(tagListener);
      setContextMenu(null);
    }

    this.tag = tag;

    if(tag != null) {
      tag.addEntityListener(tagListener);
      if(tag instanceof SystemTag == false)
        setContextMenu(createContextMenu());
    }

    setItem(tag);
    tagUpdated();
  }

  protected void tagUpdated() {
    updateItem(tag, getItemTextRepresentation().isEmpty());
  }

  protected ContextMenu createContextMenu() {
    ContextMenu contextMenu = new ContextMenu();

    MenuItem renameTagMenuItem = new MenuItem("Rename");
    contextMenu.getItems().add(renameTagMenuItem);

    renameTagMenuItem.setOnAction((event) -> {
      startEdit();
    });

    contextMenu.getItems().add(new SeparatorMenuItem());

    MenuItem deleteTagMenuItem = new MenuItem("Delete");
    contextMenu.getItems().add(deleteTagMenuItem);

    deleteTagMenuItem.setOnAction((event) -> {
      Alerts.deleteTagWithUserConfirmationIfIsSetOnEntries(tag);
    });

    return contextMenu;
  }

  protected void deleteTag(Tag tag) {
    Application.getDeepThought().removeTag(tag);
  }

  protected EntityListener tagListener = new EntityListener() {
    @Override
    public void propertyChanged(BaseEntity entity, String propertyName, Object previousValue, Object newValue) {
      tagUpdated();
    }

    @Override
    public void entityAddedToCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity addedEntity) {
      tagUpdated();
    }

    @Override
    public void entityOfCollectionUpdated(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity updatedEntity) {
      tagUpdated();
    }

    @Override
    public void entityRemovedFromCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity removedEntity) {
      tagUpdated();
    }
  };

}
