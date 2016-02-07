package net.deepthought.javafx.dialogs.mainwindow.tabs.tags.table;

import net.deepthought.controls.utils.FXUtils;
import net.deepthought.data.model.Tag;
import net.deepthought.data.model.ui.SystemTag;
import net.deepthought.data.search.specific.TagsSearchResults;
import net.deepthought.javafx.dialogs.mainwindow.tabs.tags.ITagsFilter;

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

  protected ITagsFilter tagsFilter;

  protected ObservableSet<Tag> filteredTags = null;
  protected TagsSearchResults lastTagsSearchResults = null;

  protected CheckBox checkBox = new CheckBox();


  public TagFilterTableCell(ITagsFilter tagsFilter) {
    this.tagsFilter = tagsFilter;

    this.filteredTags = tagsFilter.getTagsFilter();
    this.filteredTags.addListener((SetChangeListener<Tag>) (change) -> tagsFilterChanged(change)); // TODO: remove listeners in ICleanableControl implementation

    this.lastTagsSearchResults = tagsFilter.getLastTagsSearchResults();

    tagsFilter.addDisplayedTagsChangedListener(results -> {
      lastTagsSearchResults = results;
      setCellBackgroundColor();
    });

    setupCell();
  }

  protected void setupCell() {
    setText(null);
    setGraphicTextGap(0);

    setupCheckBox();
    setAlignment(Pos.CENTER);
  }

  protected void setupCheckBox() {
    checkBox.setId("chkbxFilterTag");

    checkBox.setMinWidth(15);
    checkBox.setMaxWidth(15);
    checkBox.setAlignment(Pos.CENTER);

    checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> tagsFilter.setTagFilterState(tag, newValue) );
  }

  protected void tagsFilterChanged(SetChangeListener.Change<? extends Tag> change) {
    Tag tag = getTableRowTag();
    updateItem(filteredTags.contains(tag), tag == null);
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
      checkBox.setSelected(filteredTags.contains(tag));
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

}
