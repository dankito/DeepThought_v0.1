package net.deepthought.controller;

import net.deepthought.Application;
import net.deepthought.controller.enums.DialogResult;
import net.deepthought.controls.articlesoverview.OverviewItemListCell;
import net.deepthought.controls.person.PersonListCell;
import net.deepthought.data.contentextractor.EntryCreationResult;
import net.deepthought.data.contentextractor.IOnlineArticleContentExtractor;
import net.deepthought.data.contentextractor.preview.ArticlesOverviewItem;
import net.deepthought.data.contentextractor.preview.ArticlesOverviewListener;
import net.deepthought.util.Alerts;
import net.deepthought.util.DeepThoughtError;
import net.deepthought.util.Localization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Collection;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.util.Callback;

/**
 * Created by ganymed on 17/07/15.
 */
public class ArticlesOverviewDialogController extends ChildWindowsController implements Initializable {

  private final static Logger log = LoggerFactory.getLogger(ArticlesOverviewDialogController.class);


  protected IOnlineArticleContentExtractor articleContentExtractor = null;

  protected ObservableSet<ArticlesOverviewItem> selectedItems = FXCollections.observableSet();


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

  protected void setupControls() {
    lstvwArticleOverviewItems.setCellFactory((listView) -> {
      final OverviewItemListCell cell = new OverviewItemListCell(selectedItems);
      cell.setItemSelectionChangedEventHandler((item, isSelected) -> itemSelectionChanged(item, isSelected));
      cell.setOnItemClicked((item, event) -> onItemClicked(item, event));
      return cell;
    });
  }

  public void setWindowStageAndArticleContentExtractor(Stage dialogStage, IOnlineArticleContentExtractor articleContentExtractor) {
    super.setWindowStage(dialogStage);

    this.articleContentExtractor = articleContentExtractor;
    dialogStage.setTitle(articleContentExtractor.getSiteBaseUrl());

    articleContentExtractor.getArticlesOverviewAsync(new ArticlesOverviewListener() {
      @Override
      public void overviewItemsRetrieved(IOnlineArticleContentExtractor contentExtractor, final Collection<ArticlesOverviewItem> items, boolean isDone) {
        Platform.runLater(() -> {
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

    lblCountSelectedItems.setText(Localization.getLocalizedStringForResourceKey("count.items.selected", selectedItems.size()));

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
  public void handleButtonCancelAction(ActionEvent actionEvent) {
    setDialogResult(DialogResult.Cancel);
    closeDialog();
  }

  @FXML
  public void handleButtonAddSelectedAction(ActionEvent actionEvent) {
    for(ArticlesOverviewItem item : selectedItems)
      extractAndAddEntryToDeepThought(item);
    selectedItems.clear();

//    setDialogResult(DialogResult.Ok);
//    closeDialog();
  }

  @FXML
  public void handleButtonViewSelectedAction(ActionEvent actionEvent) {
    for(ArticlesOverviewItem item : selectedItems)
      extractEntryAndShowInEditEntryDialog(item);
    selectedItems.clear();

//    setDialogResult(DialogResult.Ok);
//    closeDialog();
  }

  protected void extractAndAddEntryToDeepThought(ArticlesOverviewItem item) {
    item.getArticleContentExtractor().createEntryFromUrlAsync(item.getUrl(), creationResult -> {
      // TODO: this is the same code as in CreateEntryFromClipboardContentPopup.directlyAddEntryFromOnlineArticle() -> unify
      if (creationResult.successful())
        Application.getDeepThought().addEntry(creationResult.getCreatedEntry());
      else
        showCouldNotCreateEntryError(creationResult);
    });
  }

  protected void extractEntryAndShowInEditEntryDialog(ArticlesOverviewItem item) {
    item.getArticleContentExtractor().createEntryFromUrlAsync(item.getUrl(), creationResult -> {
      // TODO: this is the same code as in CreateEntryFromClipboardContentPopup.createEntryFromOnlineArticleButViewFirst() -> unify
      if (creationResult.successful())
        Dialogs.showEditEntryDialog(creationResult.getCreatedEntry());
      else
        showCouldNotCreateEntryError(creationResult);
    });
  }

  protected void showCouldNotCreateEntryError(EntryCreationResult result) {
    showCouldNotCreateEntryError(result.getSource(), result.getError());
  }

  protected void showCouldNotCreateEntryError(Object source, DeepThoughtError error) {
    log.error("Could not create Entry from Source " + source, error.getException());
    Alerts.showErrorMessage(windowStage, error.getNotificationMessage(), Localization.getLocalizedStringForResourceKey("can.not.create.entry.from", source));
  }
}
