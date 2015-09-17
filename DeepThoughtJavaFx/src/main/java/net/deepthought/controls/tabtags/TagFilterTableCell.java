package net.deepthought.controls.tabtags;

import net.deepthought.controls.FXUtils;
import net.deepthought.data.model.Tag;
import net.deepthought.data.model.ui.SystemTag;
import net.deepthought.data.search.specific.FilterTagsSearchResults;

import java.util.Collection;

import javafx.beans.property.BooleanProperty;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableRow;

/**
 * Created by ganymed on 27/12/14.
 */
public class TagFilterTableCell extends TableCell<Tag, Boolean> {

  protected Tag tag = null;
  protected ObservableSet<Tag> tagsFilter = null;

  protected CheckBox checkBox = new CheckBox();

  protected FilterTagsSearchResults lastTagsSearchResults = null;

  // TODO: this is not that clever. Better create SelectionChanged listener and react on CheckBox SelectionChanged events. Remove CheckBox SelectionChanged before setting checkBox.setSelected()
  public BooleanProperty isFilteredProperty() {
    return checkBox.selectedProperty();
  }


  public TagFilterTableCell(TabTagsControl tabTagsControl) {
    this.tagsFilter = tabTagsControl.tagsFilter;
    tagsFilter.addListener((SetChangeListener<Tag>) (change) -> tagsFilterChanged(change)); // TODO: remove listeners in ICleanableControl implementation

    this.lastTagsSearchResults = tabTagsControl.lastTagsSearchResults;
    tabTagsControl.addFilteredTagsChangedListener(results -> {
      lastTagsSearchResults = results;
      setCellBackgroundColor();
    });

    setText(null);
    setGraphicTextGap(0);

    checkBox.setMinWidth(15);
    checkBox.setMaxWidth(15);
    checkBox.setAlignment(Pos.CENTER);
    setAlignment(Pos.CENTER);
  }

  protected void tagsFilterChanged(SetChangeListener.Change<? extends Tag> change) {
    Tag tag = getTableRowTag();
    updateItem(tagsFilter.contains(tag), tag == null);
  }


  @Override
  protected void updateItem(Boolean item, boolean empty) {
    Tag tagCheck = getTableRowTag();
    if(tagCheck != tag && (tagCheck instanceof Tag || tagCheck == null))
      tagChanged(tagCheck);

    super.updateItem(item, empty);

    if(empty || tag instanceof SystemTag) {
      setGraphic(null);
    }
    else {
      checkBox.setSelected(tagsFilter.contains(tag));
      setGraphic(checkBox);
    }

    setCellBackgroundColor();
  }

  protected Tag getTableRowTag() {
    return ((TableRow<Tag>)getTableRow()).getItem();
  }

  protected void tagChanged(Tag tag) {
    this.tag = tag;
    setCellBackgroundColor();
  }

  protected void setCellBackgroundColor() {
    FXUtils.setTagCellBackgroundColor(tag, lastTagsSearchResults, this);
  }


  public void uncheck() {
    checkBox.setSelected(false);
  }

  public Tag getTag() {
    return tag;
  }

}
