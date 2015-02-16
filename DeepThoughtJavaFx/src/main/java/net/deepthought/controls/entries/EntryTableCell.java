package net.deepthought.controls.entries;

import net.deepthought.data.model.Entry;
import net.deepthought.data.model.IndexTerm;
import net.deepthought.data.model.Tag;
import net.deepthought.data.model.listener.EntityListener;
import net.deepthought.data.persistence.db.BaseEntity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableRow;

/**
 * Created by ganymed on 28/11/14.
 */
public abstract class EntryTableCell extends TableCell<Entry, String> {

  private final static Logger log = LoggerFactory.getLogger(EntryTableCell.class);


  protected Entry entry;
  protected String textRepresentation = "";


  public EntryTableCell() {

  }

  protected void entryChanged(Entry entry) {
    if(this.entry != null) {
      this.entry.removeEntityListener(entryListener);
    }

    this.entry = entry;

    if(entry != null) {
      entry.addEntityListener(entryListener);
    }

    entryUpdated(entry);
  }

  protected void entryUpdated(Entry entry) {
    this.textRepresentation = getTextRepresentationForCell(entry);

    setItem(textRepresentation);
    updateItem(textRepresentation, textRepresentation.isEmpty());
  }

  protected abstract String getTextRepresentationForCell(Entry entry);

  protected String getItemTextRepresentation() {
//    return getItem(); // don't know why but item is always null
    return textRepresentation;
  }


  @Override
  public void updateItem(String item, boolean empty) {
    Object entryCheck = ((TableRow<Entry>)getTableRow()).getItem();
    if(entryCheck != entry && entryCheck instanceof Entry)
      entryChanged((Entry)entryCheck);

    super.updateItem(item, empty);

    if (empty) {
      setText(null);
      setGraphic(null);
    }
    else {
//      if (isEditing()) {
//        if (textField != null) {
//          textField.setText(getItemTextRepresentation());
//        }
//        setText(null);
//        setGraphic(textField);
//      } else {
//        setText(getItemTextRepresentation());
//        setGraphic(getTreeItem().getGraphic());
//
//        if(hasAContextMenu)
//          setContextMenu(contextMenu);
//      }

      setGraphic(null);
      setText(getItemTextRepresentation());
    }
  }


  protected EntityListener entryListener = new EntityListener() {
    @Override
    public void propertyChanged(BaseEntity entity, String propertyName, Object previousValue, Object newValue) {
      entryUpdated((Entry)entity);
    }

    @Override
    public void entityAddedToCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity addedEntity) {
      Entry entry = (Entry)collectionHolder;
      if(collection == entry.getTags())
        tagHasBeenAdded(entry, (Tag)addedEntity);
      if(collection == entry.getIndexTerms())
        indexTermHasBeenAdded(entry, (IndexTerm) addedEntity);
    }

    @Override
    public void entityOfCollectionUpdated(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity updatedEntity) {

    }

    @Override
    public void entityRemovedFromCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity removedEntity) {
      Entry entry = (Entry)collectionHolder;
      if(collection == entry.getTags())
        tagHasBeenRemoved(entry, (Tag) removedEntity);
      if(collection == entry.getIndexTerms())
        indexTermHasBeenRemoved(entry, (IndexTerm) removedEntity);
    }
  };



  protected void tagHasBeenAdded(Entry entry, Tag tag) {
    // nothing to do here but may in subclasses
  }

  protected void tagHasBeenRemoved(Entry entry, Tag tag) {
    // nothing to do here but may in subclasses
  }

  protected void indexTermHasBeenAdded(Entry entry, IndexTerm indexTerm) {
    // nothing to do here but may in subclasses
  }

  protected void indexTermHasBeenRemoved(Entry entry, IndexTerm indexTerm) {
    // nothing to do here but may in subclasses
  }
}

