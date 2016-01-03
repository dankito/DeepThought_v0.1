package net.deepthought.javafx.dialogs.mainwindow.tabs.tags.table;

import net.deepthought.controls.Constants;
import net.deepthought.controls.LazyLoadingObservableList;
import net.deepthought.controls.utils.FXUtils;
import net.deepthought.data.model.Tag;
import net.deepthought.javafx.dialogs.mainwindow.tabs.tags.ISelectedTagsController;
import net.deepthought.javafx.dialogs.mainwindow.tabs.tags.ITagsFilter;
import net.deepthought.util.JavaFxLocalization;

import java.util.Collection;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.util.Callback;

/**
 * Created by ganymed on 03/01/16.
 */
public class TableViewTags extends TableView<Tag> {


  protected TableColumn<Tag, String> clmnTagName;

  protected TableColumn<Tag, Boolean> clmnTagFilter;


  protected ITagsFilter tagsFilter;
  protected ISelectedTagsController selectedTagsController;

  protected LazyLoadingObservableList<Tag> tableViewTagsItems = null;
  protected ObservableList<TagFilterTableCell> tagFilterTableCells = FXCollections.observableArrayList();


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
    this.selectionModelProperty().addListener(new ChangeListener<TableViewSelectionModel<Tag>>() {
      @Override
      public void changed(ObservableValue<? extends TableViewSelectionModel<Tag>> observable, TableViewSelectionModel<Tag> oldValue, TableViewSelectionModel<Tag> newValue) {
        getSelectionModel().selectedItemProperty().addListener(tableViewTagsSelectedItemChangedListener);
      }
    });
    this.getSelectionModel().selectedItemProperty().addListener(tableViewTagsSelectedItemChangedListener);

    this.setOnKeyReleased(event -> {
      if (event.getCode() == KeyCode.F2) {
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

    clmnTagName.setCellFactory(new Callback<TableColumn<Tag, String>, TableCell<Tag, String>>() {
      @Override
      public TableCell<Tag, String> call(TableColumn<Tag, String> param) {
        return new TagNameTableCell(tagsFilter);
      }
    });
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

    clmnTagFilter.setCellFactory(new Callback<TableColumn<Tag, Boolean>, TableCell<Tag, Boolean>>() {
      @Override
      public TableCell<Tag, Boolean> call(TableColumn<Tag, Boolean> param) {
        final TagFilterTableCell cell = new TagFilterTableCell(tagsFilter);
        tagFilterTableCells.add(cell);

        cell.isFilteredProperty().addListener(new ChangeListener<Boolean>() {
          @Override
          public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
            tagsFilter.setTagFilterState(cell.getTag(), newValue);
          }
        });

        return cell;
      }
    });
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
    getSelectionModel().select(tag);
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
