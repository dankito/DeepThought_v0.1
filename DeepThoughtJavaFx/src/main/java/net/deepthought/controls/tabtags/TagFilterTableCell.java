package net.deepthought.controls.tabtags;

import net.deepthought.controls.FXUtils;
import net.deepthought.data.model.Tag;
import net.deepthought.data.model.ui.SystemTag;
import net.deepthought.data.search.FilterTagsSearchResults;

import java.util.Collection;

import javafx.beans.property.BooleanProperty;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableRow;

/**
 * Created by ganymed on 27/12/14.
 */
public class TagFilterTableCell extends TableCell<Tag, Boolean> {

  protected Tag tag = null;
  protected Collection<Tag> tagsToFilterFor = null;

  protected CheckBox checkBox = new CheckBox();

  protected FilterTagsSearchResults filterTagsSearchResults = null;


//  private ReadOnlyObjectWrapper<Boolean> isChecked = new ReadOnlyObjectWrapper<Boolean>() {
//    @Override protected void invalidated() {
//      updateItem(isChecked(), tag != null);
//    }
//
//    @Override public Object getBean() {
//      return TagFilterTableCell.this;
//    }
//
//    @Override public String getName() {
//      return "isChecked";
//    }
//  };
//
//  public final ReadOnlyObjectProperty<Boolean> isCheckedProperty() { return isChecked.getReadOnlyProperty(); }
//  private void setIsChecked(Boolean value) { isChecked.set(value); }
//  public final Boolean isChecked() { return isChecked.get(); }

  public BooleanProperty isFilteredProperty() {
    return checkBox.selectedProperty();
  }


  public TagFilterTableCell(TabTagsControl tabTagsControl) {
    this.tagsToFilterFor = tabTagsControl.tagsToFilterFor;
    this.filterTagsSearchResults = tabTagsControl.lastFilterTagsResults;
    tabTagsControl.addFilteredTagsChangedListener(results -> {
      filterTagsSearchResults = results;
      setCellBackgroundColor();
    });

    setText(null);
    setAlignment(Pos.CENTER);
  }


  @Override
  protected void updateItem(Boolean item, boolean empty) {
    Object tagCheck = ((TableRow<Tag>)getTableRow()).getItem();
    if(tagCheck != tag && (tagCheck instanceof Tag || tagCheck == null))
      tagChanged((Tag) tagCheck);

    super.updateItem(item, empty);

    if(empty || tag instanceof SystemTag) {
      setGraphic(null);
    }
    else {
      checkBox.setSelected(tagsToFilterFor.contains(tag));
      setGraphic(checkBox);
    }

    setCellBackgroundColor();
  }

  protected void tagChanged(Tag tag) {
    this.tag = tag;
    setCellBackgroundColor();
  }

  protected void setCellBackgroundColor() {
    FXUtils.setTagCellBackgroundColor(tag, filterTagsSearchResults, this);
  }


  public void uncheck() {
    checkBox.setSelected(false);
  }

  public Tag getTag() {
    return tag;
  }

}
