package net.deepthought.javafx.dialogs.mainwindow.tabs.tags.table;

import net.deepthought.Application;
import net.deepthought.controls.Constants;
import net.deepthought.controls.LazyLoadingObservableList;
import net.deepthought.controls.utils.FXUtils;
import net.deepthought.data.model.Tag;
import net.deepthought.javafx.dialogs.mainwindow.tabs.tags.ISelectedTagsController;
import net.deepthought.javafx.dialogs.mainwindow.tabs.tags.ITagsFilter;
import net.deepthought.util.localization.JavaFxLocalization;

import java.util.Collection;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;

/**
 * Created by ganymed on 03/01/16.
 */
public class TableViewTags extends TableView<Tag> {


  protected TableColumn<Tag, String> clmnTagName;

  protected TableColumn<Tag, Boolean> clmnTagFilter;


  protected ITagsFilter tagsFilter;
  protected ISelectedTagsController selectedTagsController;

  protected LazyLoadingObservableList<Tag> tableViewTagsItems = null;


  public TableViewTags(ITagsFilter tagsFilter, ISelectedTagsController selectedTagsController) {
    this.tagsFilter = tagsFilter;
    this.selectedTagsController = selectedTagsController;

    setupControl();
  }

  protected void setupControl() {
    setupTableView();

    setupColumnTagName();

    setupColumnTagFilter();
  }

  protected void setupTableView() {
    setId("tblvwTags");
    setEditable(true);
    setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY);

    // TODO: isn't this setting listener twice?
    this.selectionModelProperty().addListener((observable, oldValue, newValue) -> getSelectionModel().selectedItemProperty().addListener(tableViewTagsSelectedItemChangedListener));
    this.getSelectionModel().selectedItemProperty().addListener(tableViewTagsSelectedItemChangedListener);

    this.setOnKeyReleased(event -> {
      if (event.getCode() == KeyCode.DELETE) {
        selectedTagsController.removeSelectedTags();
      } else if (event.getCode() == KeyCode.F2) {
        this.edit(this.getSelectionModel().getSelectedIndex(), clmnTagName);
      }
    });

    tableViewTagsItems = new LazyLoadingObservableList<>();
    this.setItems(tableViewTagsItems);
  }

  protected void setupColumnTagName() {
    clmnTagName = new TableColumn<>();
    clmnTagName.setId("clmnTagName");
    JavaFxLocalization.bindTableColumnText(clmnTagName, "name");
    getColumns().add(clmnTagName);

    clmnTagName.setEditable(true);
    clmnTagName.setSortable(false);

    clmnTagName.setResizable(true);
    clmnTagName.setMinWidth(10);
    clmnTagName.setPrefWidth(80);
    clmnTagName.setMaxWidth(FXUtils.SizeMaxValue);

    clmnTagName.setCellFactory(param -> new TagNameTableCell(tagsFilter));
  }

  protected void setupColumnTagFilter() {
    clmnTagFilter = new TableColumn<>();
    clmnTagName.setId("clmnTagFilter");
    clmnTagFilter.setText(null);
    getColumns().add(clmnTagFilter);

    clmnTagFilter.setResizable(false);
    clmnTagFilter.setMinWidth(35);
    clmnTagFilter.setMaxWidth(35);

    ImageView columnFilterGraphic = new ImageView(Constants.FilterIconPath);
    Label columnFilterGraphicLabel = new Label(null, columnFilterGraphic); // wrap Image in a Label so that a Tooltip can be set
    columnFilterGraphicLabel.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
    JavaFxLocalization.bindControlToolTip(columnFilterGraphicLabel, "filter.tags.tool.tip");
    clmnTagFilter.setGraphic(columnFilterGraphicLabel);

    clmnTagFilter.setCellFactory(param -> new TagFilterTableCell(tagsFilter));
  }


  public void setTags(Collection<Tag> tags) {
//    tableViewTagsItems.setUnderlyingCollection(tags);
    tableViewTagsItems = new LazyLoadingObservableList<>(tags);
    this.setItems(tableViewTagsItems);
  }

  public void clearTags() {
    tableViewTagsItems.clear(); // TODO: is it so clever calling clear on LazyLoadingObservableList?
  }

  public void selectTag(Tag tag) {
    if(tag == null) {
      getSelectionModel().clearSelection();
    }
    else if(Application.getDeepThought() != null) {
      if(tag == Application.getDeepThought().AllEntriesSystemTag()) {
        getSelectionModel().select(0);
      }
      else if(tag == Application.getDeepThought().EntriesWithoutTagsSystemTag()) {
        getSelectionModel().select(1);
      }
      else {
        getSelectionModel().select(tag);
      }
    }
  }

  public void selectTagAtIndex(int index) {
    getSelectionModel().select(index);
  }

  public int getTagsSize() {
    return tableViewTagsItems.size();
  }


  protected ChangeListener<Tag> tableViewTagsSelectedItemChangedListener = new ChangeListener<Tag>() {
    @Override
    public void changed(ObservableValue<? extends Tag> observable, Tag oldValue, Tag newValue) {
      selectedTagsController.selectedTagChanged(newValue);
    }
  };


}
