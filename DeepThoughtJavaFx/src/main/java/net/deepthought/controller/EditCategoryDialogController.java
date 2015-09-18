package net.deepthought.controller;

import net.deepthought.Application;
import net.deepthought.controller.enums.DialogResult;
import net.deepthought.controller.enums.FieldWithUnsavedChanges;
import net.deepthought.controls.Constants;
import net.deepthought.controls.ContextHelpControl;
import net.deepthought.controls.FXUtils;
import net.deepthought.data.model.Category;
import net.deepthought.data.model.listener.EntityListener;
import net.deepthought.data.persistence.db.BaseEntity;
import net.deepthought.util.Alerts;
import net.deepthought.util.JavaFxLocalization;
import net.deepthought.util.Localization;

import java.net.URL;
import java.util.Collection;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * Created by ganymed on 31/12/14.
 */
public class EditCategoryDialogController extends ChildWindowsController implements Initializable {

  protected Category category = null;

  protected ObservableSet<FieldWithUnsavedChanges> fieldsWithUnsavedChanges = FXCollections.observableSet();


  @FXML
  protected BorderPane dialogPane;

  @FXML
  protected Button btnApply;

  @FXML
  protected ToggleButton tglbtnShowHideContextHelp;

  protected ContextHelpControl contextHelpControl;

  @FXML
  protected TextField txtfldName;
  @FXML
  protected TextField txtfldDescription;


  @Override
  public void initialize(URL location, ResourceBundle resources) {
    btnApply.managedProperty().bind(btnApply.visibleProperty());

    setupFields();

    fieldsWithUnsavedChanges.addListener(new SetChangeListener<FieldWithUnsavedChanges>() {
      @Override
      public void onChanged(Change<? extends FieldWithUnsavedChanges> c) {
        btnApply.setDisable(fieldsWithUnsavedChanges.size() == 0);
      }
    });
  }

  protected void setupFields() {
    txtfldName.textProperty().addListener((observable, oldValue, newValue) -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.CategoryName));
    txtfldName.focusedProperty().addListener((observable, oldValue, newValue) -> fieldFocused("name"));

    txtfldDescription.textProperty().addListener((observable, oldValue, newValue) -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.CategoryDescription));
    txtfldDescription.focusedProperty().addListener((observable, oldValue, newValue) -> fieldFocused("description"));

    contextHelpControl = new ContextHelpControl("context.help.category.");
    dialogPane.setRight(contextHelpControl);

    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(contextHelpControl);
    contextHelpControl.visibleProperty().bind(tglbtnShowHideContextHelp.selectedProperty());

    tglbtnShowHideContextHelp.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
    tglbtnShowHideContextHelp.setGraphic(new ImageView(Constants.ContextHelpIconPath));
  }

  protected void fieldFocused(String fieldName) {
    contextHelpControl.showContextHelpForResourceKey(fieldName);
  }


  public void setCategoryAndStage(Stage dialogStage, Category categoryToEdit) {
    setWindowStage(dialogStage);
    this.category = categoryToEdit;

    updateStageTitle();

    categoryToEditSet(categoryToEdit);
    fieldsWithUnsavedChanges.clear();
    category.addEntityListener(categoryListener);

    txtfldName.selectAll();
    txtfldName.requestFocus();
  }

  protected void categoryToEditSet(Category category) {
    txtfldName.setText(category.getName());
    txtfldDescription.setText(category.getDescription());
  }

  public boolean hasUnsavedChanges() {
    return fieldsWithUnsavedChanges.size() > 0;
  }

  protected void updateStageTitle() {
    if(category.isPersisted() == false)
      JavaFxLocalization.bindStageTitle(windowStage, "create.category");
    else
      JavaFxLocalization.bindStageTitle(windowStage, "edit.category", category.getTextRepresentation());
  }


  @FXML
  public void handleButtonApplyAction(ActionEvent actionEvent) {
    saveEditedFields();
  }

  @FXML
  public void handleButtonCancelAction(ActionEvent actionEvent) {
    closeDialog(DialogResult.Cancel);
  }

  @FXML
  public void handleButtonOkAction(ActionEvent actionEvent) {
    saveEditedFields();
    closeDialog(DialogResult.Ok);
  }

  @Override
  protected void closeDialog() {
    if(category != null)
      category.removeEntityListener(categoryListener);

    super.closeDialog();
  }

  protected void saveEditedFields() {
    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.CategoryName)) {
      category.setName(txtfldName.getText());
      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.CategoryName);
    }

    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.CategoryDescription)) {
      category.setDescription(txtfldDescription.getText());
      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.CategoryDescription);
    }

    if(category.isPersisted() == false)
      Application.getDeepThought().addCategory(category);
  }

  @Override
  protected boolean askIfStageShouldBeClosed() {
    if(hasUnsavedChanges()) {
      ButtonType result = Alerts.askUserIfEditedEntityShouldBeSaved(windowStage, "category");

      if(result.equals(ButtonType.CANCEL))
        return false;
      else if(result.equals(ButtonType.YES)) {
        saveEditedFields();
      }
    }

    return true;
  }


  protected EntityListener categoryListener = new EntityListener() {
    @Override
    public void propertyChanged(BaseEntity entity, String propertyName, Object previousValue, Object newValue) {

    }

    @Override
    public void entityAddedToCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity addedEntity) {

    }

    @Override
    public void entityOfCollectionUpdated(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity updatedEntity) {

    }

    @Override
    public void entityRemovedFromCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity removedEntity) {

    }
  };

}
