package net.deepthought.controls.tabtags;

import net.deepthought.Application;
import net.deepthought.controls.TextFieldTableCell;
import net.deepthought.data.model.Tag;
import net.deepthought.data.model.listener.EntityListener;
import net.deepthought.data.persistence.db.BaseEntity;
import net.deepthought.data.model.ui.SystemTag;
import net.deepthought.util.Alerts;

import java.util.Collection;

import javafx.application.Platform;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;

/**
 * Created by ganymed on 27/12/14.
 */
//public class TagNameTableCell extends TableCell<Tag, String> {
public class TagNameTableCell extends TextFieldTableCell<Tag> {

  protected Tag tag = null;


  public TagNameTableCell() {

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

    setEditable(newValue instanceof SystemTag);
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

    setItem(getItemTextRepresentation());
    tagUpdated();
  }

  @Override
  public void startEdit() {
    if(tag instanceof SystemTag == false)
      super.startEdit();
  }

  @Override
  public void updateItem(String item, boolean empty) {
    if(getTableRow() != null && getTableRow().getItem() != tag)
      newItemSet((Tag)getTableRow().getItem());

    super.updateItem(item, empty);
  }

  protected void tagUpdated() {
    updateItem(getItemTextRepresentation(), getItemTextRepresentation().isEmpty());
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
      if(Platform.isFxApplicationThread())
        tagUpdated();
      else {
        Platform.runLater(new Runnable() {
          @Override
          public void run() {
            tagUpdated();
          }
        });
      }
    }

    @Override
    public void entityAddedToCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity addedEntity) {
      if(collectionHolder.equals(tag)) {
        if(Platform.isFxApplicationThread())
          tagUpdated();
        else {
          Platform.runLater(new Runnable() {
            @Override
            public void run() {
              tagUpdated();
            }
          });
        }
      }
    }

    @Override
    public void entityOfCollectionUpdated(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity updatedEntity) {

    }

    @Override
    public void entityRemovedFromCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity removedEntity) {
      if(collection == tag.getEntries()) {
        if(Platform.isFxApplicationThread())
          tagUpdated();
        else {
          Platform.runLater(new Runnable() {
            @Override
            public void run() {
              tagUpdated();
            }
          });
        }
      }
    }
  };

}
