package net.dankito.deepthought.javafx.dialogs.mainwindow.tabs.tags.filterpanel;

import net.dankito.deepthought.controls.utils.FXUtils;
import net.dankito.deepthought.data.model.Tag;

import org.controlsfx.control.textfield.CustomTextField;
import org.controlsfx.control.textfield.TextFields;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

/**
 * Created by ganymed on 03/01/16.
 */
public class TagsFilterPanel extends HBox {

  protected net.dankito.deepthought.javafx.dialogs.mainwindow.tabs.tags.ITagsFilter tagsFilter;

  protected net.dankito.deepthought.javafx.dialogs.mainwindow.tabs.tags.ISelectedTagsController selectedTagsController;


  @FXML
  protected TextField txtfldSearchTags;
  @FXML
  protected Button btnRemoveTagsFilter;
  @FXML
  protected Button btnRemoveSelectedTag;
  @FXML
  protected Button btnAddTag;


  public TagsFilterPanel(net.dankito.deepthought.javafx.dialogs.mainwindow.tabs.tags.ITagsFilter tagsFilter, net.dankito.deepthought.javafx.dialogs.mainwindow.tabs.tags.ISelectedTagsController selectedTagsController) {
    this.tagsFilter = tagsFilter;
    this.selectedTagsController = selectedTagsController;

    if(FXUtils.loadControl(this, "TagsFilterPanel"))
      setupControl();
  }

  protected void setupControl() {
    setupTextFieldSearchTags();

    setupButtonRemoveTagsFilter();

    setupButtonRemoveSelectedTag();

    setupButtonAddTag();
  }

  protected void setupTextFieldSearchTags() {
    // replace normal TextField txtfldSearchTags with a SearchTextField (with a cross to clear selection)
    getChildren().remove(txtfldSearchTags);
    txtfldSearchTags = (CustomTextField) TextFields.createClearableTextField();
    txtfldSearchTags.setId("txtfldSearchTags");
    net.dankito.deepthought.util.localization.JavaFxLocalization.bindTextInputControlPromptText(txtfldSearchTags, "search.tags.prompt.text");
    getChildren().add(1, txtfldSearchTags);
    HBox.setHgrow(txtfldSearchTags, Priority.ALWAYS);
    txtfldSearchTags.setMinWidth(60);
    txtfldSearchTags.setPrefWidth(Region.USE_COMPUTED_SIZE);

    txtfldSearchTags.textProperty().addListener((observable, oldValue, newValue) -> tagsFilter.searchTags());
    txtfldSearchTags.setOnAction(event -> tagsFilter.toggleCurrentTagsTagsFilter());

    txtfldSearchTags.setOnKeyReleased(event -> {
      if (event.getCode() == KeyCode.ESCAPE)
        txtfldSearchTags.clear();
    });
  }

  protected void setupButtonRemoveTagsFilter() {
    btnRemoveTagsFilter.setGraphic(new ImageView(net.dankito.deepthought.controls.Constants.FilterDeleteIconPath));
    net.dankito.deepthought.util.localization.JavaFxLocalization.bindControlToolTip(btnRemoveTagsFilter, "button.remove.tags.filter.tool.tip");
  }

  protected void setupButtonRemoveSelectedTag() {
    btnRemoveSelectedTag.setTextFill(net.dankito.deepthought.controls.Constants.RemoveEntityButtonTextColor);
    net.dankito.deepthought.util.localization.JavaFxLocalization.bindControlToolTip(btnRemoveSelectedTag, "delete.selected.tags.tool.tip");
  }

  protected void setupButtonAddTag() {
    btnAddTag.setTextFill(net.dankito.deepthought.controls.Constants.AddEntityButtonTextColor);
    net.dankito.deepthought.util.localization.JavaFxLocalization.bindControlToolTip(btnAddTag, "add.new.tag.tool.tip");
  }


  public String getTagsSearchText() {
    return txtfldSearchTags.getText();
  }

  public void disableButtonRemoveTagsFilter(boolean disable) {
    btnRemoveTagsFilter.setDisable(disable);
  }

  public void disableButtonRemoveSelectedTag(boolean disable) {
    btnRemoveSelectedTag.setDisable(disable);
  }



  @FXML
  protected void handleButtonRemoveTagsFilterAction(ActionEvent event) {
    tagsFilter.clearTagFilter();
  }

  @FXML
  protected void handleButtonRemoveSelectedTagsAction(ActionEvent event) {
    selectedTagsController.removeSelectedTags();
  }

  @FXML
  protected void handleButtonAddTagAction(ActionEvent event) {
    addNewTag();
  }

  protected void addNewTag() {
    Point2D buttonCoordinates = FXUtils.getNodeScreenCoordinates(btnAddTag);

    final double centerX = buttonCoordinates.getX() + btnAddTag.getWidth() / 2;
    final double y = buttonCoordinates.getY() + btnAddTag.getHeight() + 6;

    net.dankito.deepthought.controller.Dialogs.showEditTagDialog(new Tag(), centerX, y, getScene().getWindow(), true);
  }

}
