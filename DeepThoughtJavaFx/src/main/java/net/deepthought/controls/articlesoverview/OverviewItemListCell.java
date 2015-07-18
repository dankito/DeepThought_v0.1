package net.deepthought.controls.articlesoverview;

import net.deepthought.controls.FXUtils;
import net.deepthought.data.contentextractor.preview.ArticlesOverviewItem;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableSet;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;

/**
 * Created by ganymed on 17/07/15.
 */
public class OverviewItemListCell extends ListCell<ArticlesOverviewItem> {

  public interface ItemSelectionChangedEventHandler {
    void itemSelectionChanged(ArticlesOverviewItem item, boolean isSelected);
  }

  public interface OnItemClickedEventHandler {
    void onItemClicked(ArticlesOverviewItem item, MouseEvent event);
  }


  protected ItemSelectionChangedEventHandler itemSelectionChangedEventHandler = null;

  protected OnItemClickedEventHandler onItemClickedEventHandler = null;


  protected ArticlesOverviewItem item = null;

  protected ObservableSet<ArticlesOverviewItem> selectedItems = null;


//  protected HBox graphicPane = new HBox();
  protected GridPane graphicPane = new GridPane();

  protected CheckBox chkbxSelectItem = new CheckBox();

  protected ImageView imgvwItemPreviewImage = new ImageView();

  protected Label lblItemCategoryOrLabel = new Label();

  protected VBox itemTextLinesPane = new VBox();

  protected Label lblItemTitle = new Label();

  protected Label lblItemSubTitle = new Label();

  protected Label lblItemSummary = new Label();


  public OverviewItemListCell(ObservableSet<ArticlesOverviewItem> selectedItems) {
    this.selectedItems = selectedItems;
    setText(null);
    setupGraphic();

    itemProperty().addListener(new ChangeListener<ArticlesOverviewItem>() {
      @Override
      public void changed(ObservableValue<? extends ArticlesOverviewItem> observable, ArticlesOverviewItem oldValue, ArticlesOverviewItem newValue) {
        itemChanged(newValue);
      }
    });
    listViewProperty().addListener((observable, oldValue, newValue) -> listViewChanged(newValue));

    setOnMouseClicked(event -> mouseClicked(event));
  }

  protected void listViewChanged(ListView<ArticlesOverviewItem> listView) {
    if(listView != null && graphicPane != null)
      graphicPane.prefWidthProperty().bind(listView.widthProperty().subtract(18));
  }

  protected void setupGraphic() {
    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
    setAlignment(Pos.CENTER_LEFT);

    graphicPane.setPrefHeight(100);
//    graphicPane.setMaxHeight(Region.USE_PREF_SIZE);
    graphicPane.setAlignment(Pos.CENTER_LEFT);

    graphicPane.getColumnConstraints().clear();
    graphicPane.getColumnConstraints().add(new ColumnConstraints(30, 30, 30, Priority.NEVER, HPos.CENTER, false));
    graphicPane.getColumnConstraints().add(new ColumnConstraints(120, 120, 120, Priority.NEVER, HPos.CENTER, false));
    graphicPane.getColumnConstraints().add(new ColumnConstraints(-1, -1, -1, Priority.ALWAYS, HPos.LEFT, true));
    graphicPane.getRowConstraints().add(new RowConstraints(-1, -1, -1, Priority.ALWAYS, VPos.CENTER, false));
    graphicPane.getRowConstraints().add(new RowConstraints(30, 30, 30, Priority.NEVER, VPos.CENTER, false));

    graphicPane.add(chkbxSelectItem, 0, 0);
    chkbxSelectItem.setText(null);
    chkbxSelectItem.setPrefWidth(30);
    chkbxSelectItem.setOnAction(chkbxSelectItemOnAction);

    graphicPane.add(imgvwItemPreviewImage, 1, 0);
    imgvwItemPreviewImage.setPreserveRatio(true);
    imgvwItemPreviewImage.setFitHeight(100);
    imgvwItemPreviewImage.setFitWidth(120);

    graphicPane.add(lblItemCategoryOrLabel, 0, 1, 2, 1);
    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(lblItemCategoryOrLabel);

    graphicPane.add(itemTextLinesPane, 2, 0, 1, 2);

    GridPane.setMargin(itemTextLinesPane, new Insets(0, 9, 0, 6));

    itemTextLinesPane.setPrefHeight(VBox.USE_COMPUTED_SIZE);
    itemTextLinesPane.setMaxHeight(Double.MAX_VALUE);
    itemTextLinesPane.setAlignment(Pos.TOP_LEFT);

    itemTextLinesPane.getChildren().add(lblItemSubTitle);
    lblItemSubTitle.setMaxHeight(20);
    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(lblItemSubTitle);
    VBox.setMargin(lblItemSubTitle, new Insets(0, 0, 6, 0));

    itemTextLinesPane.getChildren().add(lblItemTitle);
    lblItemTitle.setMaxHeight(20);
    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(lblItemTitle);
    VBox.setMargin(lblItemTitle, new Insets(0, 0, 6, 0));

    itemTextLinesPane.getChildren().add(lblItemSummary);
    VBox.setVgrow(lblItemSummary, Priority.ALWAYS);
    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(lblItemSummary);
    lblItemSummary.setWrapText(true);
  }

  protected EventHandler<ActionEvent> chkbxSelectItemOnAction = new EventHandler<ActionEvent>() {
    @Override
    public void handle(ActionEvent event) {
      if(itemSelectionChangedEventHandler != null)
        itemSelectionChangedEventHandler.itemSelectionChanged(item, chkbxSelectItem.isSelected());
    }
  };


  @Override
  protected void updateItem(ArticlesOverviewItem item, boolean empty) {
    if(item != this.item)
      this.item = item;
    super.updateItem(item, empty);

    if(empty || item == null) {
      setGraphic(null);
    }
    else {
      setGraphic(graphicPane);

      chkbxSelectItem.setOnAction(null);
      chkbxSelectItem.setSelected(selectedItems.contains(item));
      chkbxSelectItem.setOnAction(chkbxSelectItemOnAction);

      imgvwItemPreviewImage.setVisible(item.hasPreviewImageUrl());
      if(item.hasPreviewImageUrl()) {
        Platform.runLater(() -> {
          imgvwItemPreviewImage.setImage(new Image(item.getPreviewImageUrl()));
        });
      }

      lblItemSubTitle.setVisible(item.hasSubTitle());
      if(item.hasSubTitle())
        lblItemSubTitle.setText(item.getSubTitle());

      lblItemTitle.setVisible(item.hasTitle());
      if(item.hasTitle())
        lblItemTitle.setText(item.getTitle());

      lblItemSummary.setVisible(item.hasSummary());
      if(item.hasSummary()) {
        lblItemSummary.setText(item.getSummary());
//        if(item.getSummary().length() > 400)
//          graphicPane.setMaxHeight(170);
//        else
//          graphicPane.setMaxHeight(100);
      }

      lblItemCategoryOrLabel.setVisible(item.hasLabel() || item.hasCategories());
      if(item.hasLabel() || item.hasCategories()) {
        lblItemCategoryOrLabel.setText(item.getCategoriesAndLabel());
        GridPane.setRowSpan(chkbxSelectItem, 1);
        GridPane.setRowSpan(imgvwItemPreviewImage, 1);
      }
      else {
        GridPane.setRowSpan(chkbxSelectItem, 2);
        GridPane.setRowSpan(imgvwItemPreviewImage, 2);
      }
    }
  }

  protected void itemChanged(ArticlesOverviewItem newValue) {
    updateItem(newValue, newValue == null);
  }


  protected void mouseClicked(MouseEvent event) {
    if(onItemClickedEventHandler != null)
      onItemClickedEventHandler.onItemClicked(item, event);
  }


  public ItemSelectionChangedEventHandler getItemSelectionChangedEventHandler() {
    return itemSelectionChangedEventHandler;
  }

  public void setItemSelectionChangedEventHandler(ItemSelectionChangedEventHandler itemSelectionChangedEventHandler) {
    this.itemSelectionChangedEventHandler = itemSelectionChangedEventHandler;
  }

  public OnItemClickedEventHandler getOnItemClicked() {
    return onItemClickedEventHandler;
  }

  public void setOnItemClicked(OnItemClickedEventHandler onItemClickedEventHandler) {
    this.onItemClickedEventHandler = onItemClickedEventHandler;
  }

}
