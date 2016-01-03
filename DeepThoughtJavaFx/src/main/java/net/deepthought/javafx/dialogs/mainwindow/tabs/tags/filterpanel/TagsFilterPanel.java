package net.deepthought.javafx.dialogs.mainwindow.tabs.tags.filterpanel;

import net.deepthought.controller.Dialogs;
import net.deepthought.controls.Constants;
import net.deepthought.controls.utils.FXUtils;
import net.deepthought.data.model.Tag;
import net.deepthought.javafx.dialogs.mainwindow.tabs.tags.ISelectedTagsController;
import net.deepthought.javafx.dialogs.mainwindow.tabs.tags.ITagsFilter;
import net.deepthought.util.JavaFxLocalization;

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

  protected ITagsFilter tagsFilter;

  protected ISelectedTagsController selectedTagsController;


  @FXML
  protected TextField txtfldSearchTags;
  @FXML
  protected Button btnRemoveTagsFilter;
  @FXML
  protected Button btnRemoveSelectedTag;
  @FXML
  protected Button btnAddTag;


  public TagsFilterPanel(ITagsFilter tagsFilter, ISelectedTagsController selectedTagsController) {
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
    JavaFxLocalization.bindTextInputControlPromptText(txtfldSearchTags, "search.tags.prompt.text");
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
    btnRemoveTagsFilter.setGraphic(new ImageView(Constants.FilterDeleteIconPath));
    JavaFxLocalization.bindControlToolTip(btnRemoveTagsFilter, "button.remove.tags.filter.tool.tip");
  }

  protected void setupButtonRemoveSelectedTag() {
    btnRemoveSelectedTag.setTextFill(Constants.RemoveEntityButtonTextColor);
    JavaFxLocalization.bindControlToolTip(btnRemoveSelectedTag, "delete.selected.tags.tool.tip");
  }

  protected void setupButtonAddTag() {
    btnAddTag.setTextFill(Constants.AddEntityButtonTextColor);
    JavaFxLocalization.bindControlToolTip(btnAddTag, "add.new.tag.tool.tip");
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

    Dialogs.showEditTagDialog(new Tag(), centerX, y, getScene().getWindow(), true);
  }

}
