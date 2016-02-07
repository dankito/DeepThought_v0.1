package net.deepthought.controller;

import net.deepthought.controller.enums.DialogResult;
import net.deepthought.controls.Constants;
import net.deepthought.controls.articlesoverview.OverviewItemListCell;
import net.deepthought.data.contentextractor.EntryCreationResult;
import net.deepthought.data.contentextractor.IOnlineArticleContentExtractor;
import net.deepthought.data.contentextractor.preview.ArticlesOverviewItem;
import net.deepthought.data.contentextractor.preview.ArticlesOverviewListener;
import net.deepthought.util.Alerts;
import net.deepthought.util.DeepThoughtError;
import net.deepthought.util.localization.Localization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicBoolean;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

/**
 * Created by ganymed on 17/07/15.
 */
public class ArticlesOverviewDialogController extends ChildWindowsController implements Initializable {

  private final static Logger log = LoggerFactory.getLogger(ArticlesOverviewDialogController.class);


  protected IOnlineArticleContentExtractor articleContentExtractor = null;

  protected List<OverviewItemListCell> overviewItemListCells = new ArrayList<>();

  protected ObservableSet<ArticlesOverviewItem> selectedItems = FXCollections.observableSet();


  @FXML
  protected Pane pnTopBar;
  @FXML
  protected Button btnUpdateArticlesOverview;
  @FXML
  protected Label lblLastUpdateTime;

  @FXML
  protected ListView<ArticlesOverviewItem> lstvwArticleOverviewItems;

  @FXML
  protected Button btnAddSelected;

  @FXML
  protected Button btnViewSelected;

  @FXML
  protected Label lblCountSelectedItems;


  @Override
  public void initialize(URL location, ResourceBundle resources) {
    setupControls();
  }

  @Override
  protected void closeDialog() {
    clearSelectedItems();
    lstvwArticleOverviewItems.getItems().clear();

    articleContentExtractor = null;

    for(OverviewItemListCell cell : overviewItemListCells) {
      cell.cleanUp();
    }
    overviewItemListCells.clear();

    super.closeDialog();
  }

  protected void setupControls() {
    btnUpdateArticlesOverview.setGraphic(new ImageView(Constants.UpdateIconPath));

    lstvwArticleOverviewItems.setCellFactory((listView) -> {
      final OverviewItemListCell cell = new OverviewItemListCell(selectedItems);
      cell.setItemSelectionChangedEventHandler((item, isSelected) -> itemSelectionChanged(item, isSelected));
      cell.setOnItemClicked((item, event) -> onItemClicked(item, event));
      overviewItemListCells.add(cell);
      return cell;
    });
  }

  public void setWindowStageAndArticleContentExtractor(Stage dialogStage, IOnlineArticleContentExtractor articleContentExtractor) {
    super.setWindowStage(dialogStage);

    this.articleContentExtractor = articleContentExtractor;
    dialogStage.setTitle(articleContentExtractor.getSiteBaseUrl());

    getArticlesOverview();
  }

  protected void getArticlesOverview() {
    final AtomicBoolean articlesOverviewUpdateStarted = new AtomicBoolean(true);

    this.articleContentExtractor.getArticlesOverviewAsync(new ArticlesOverviewListener() {
      @Override
      public void overviewItemsRetrieved(IOnlineArticleContentExtractor contentExtractor, final Collection<ArticlesOverviewItem> items, boolean isDone) {
        Platform.runLater(() -> {
          if (articlesOverviewUpdateStarted.get() == true) { // if articles are being updated, don't clear previous articles till new ones are retrieved. Else in case of error an empty ListView would be shown
            articlesOverviewUpdateStarted.set(false);
            lstvwArticleOverviewItems.getItems().clear();
            clearSelectedItems();
          }
          // TODO: show error message if retrieving Article Overview Items failed

          lblLastUpdateTime.setText(DateFormat.getDateTimeInstance().format(new Date()));
          addOverviewItemsToListView(items);
        });
      }
    });
  }

  protected void addOverviewItemsToListView(Collection<ArticlesOverviewItem> items) {
    lstvwArticleOverviewItems.getItems().addAll(items);
  }


  protected void itemSelectionChanged(ArticlesOverviewItem item, boolean isSelected) {
    if(isSelected)
      selectedItems.add(item);
    else
      selectedItems.remove(item);

    setControlsDependingOnSelectedItems();
  }

  protected void clearSelectedItems() {
    selectedItems.clear();

    setControlsDependingOnSelectedItems();
  }

  protected void setControlsDependingOnSelectedItems() {
    lblCountSelectedItems.setText(Localization.getLocalizedString("count.items.selected", selectedItems.size()));

    btnAddSelected.setDisable(selectedItems.size() == 0);
    btnViewSelected.setDisable(selectedItems.size() == 0);
  }

  protected void onItemClicked(ArticlesOverviewItem item, MouseEvent event) {
    if(item != null) {
      if(event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) { // double click with left mouse button
        extractEntryAndShowInEditEntryDialog(item);
      }
    }
  }

  @FXML
  public void handleButtonUpdateArticlesOverviewAction(ActionEvent actionEvent) {
    getArticlesOverview();
  }

  @FXML
  public void handleButtonCloseAction(ActionEvent actionEvent) {
    closeDialog(DialogResult.Cancel);
  }

  @FXML
  public void handleButtonAddSelectedItemsToDeepThoughtAction(ActionEvent actionEvent) {
    for(ArticlesOverviewItem item : selectedItems)
      extractAndAddEntryToDeepThought(item);
    clearSelectedItems();
  }

  @FXML
  public void handleButtonViewSelectedAction(ActionEvent actionEvent) {
    for(ArticlesOverviewItem item : selectedItems)
      extractEntryAndShowInEditEntryDialog(item);
    clearSelectedItems();
  }

  protected void extractAndAddEntryToDeepThought(ArticlesOverviewItem item) {
    item.getArticleContentExtractor().createEntryFromUrlAsync(item.getUrl(), creationResult -> {
      // TODO: this is the same code as in CreateEntryFromClipboardContentPopup.directlyAddEntryFromOnlineArticle() -> unify
      if (creationResult.successful())
        creationResult.saveCreatedEntities();
      else
        showCouldNotCreateEntryError(creationResult);
    });
  }

  protected void extractEntryAndShowInEditEntryDialog(ArticlesOverviewItem item) {
    item.getArticleContentExtractor().createEntryFromUrlAsync(item.getUrl(), creationResult -> {
      // TODO: this is the same code as in CreateEntryFromClipboardContentPopup.createEntryFromOnlineArticleButViewFirst() -> unify
      if (creationResult.successful())
        Dialogs.showEditEntryDialog(creationResult);
      else
        showCouldNotCreateEntryError(creationResult);
    });
  }

  protected void showCouldNotCreateEntryError(EntryCreationResult result) {
    showCouldNotCreateEntryError(result.getSource(), result.getError());
  }

  protected void showCouldNotCreateEntryError(Object source, DeepThoughtError error) {
    log.error("Could not create Entry from Source " + source, error.getException());
    Alerts.showErrorMessage(windowStage, error, Localization.getLocalizedString("can.not.create.entry.from", source));
  }
}
