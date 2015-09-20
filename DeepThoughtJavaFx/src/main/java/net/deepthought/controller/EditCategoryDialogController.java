package net.deepthought.controller;

import net.deepthought.Application;
import net.deepthought.controller.enums.DialogResult;
import net.deepthought.controller.enums.FieldWithUnsavedChanges;
import net.deepthought.data.model.Category;
import net.deepthought.data.model.listener.EntityListener;
import net.deepthought.data.persistence.db.BaseEntity;
import net.deepthought.util.Alerts;

import java.util.Collection;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Created by ganymed on 31/12/14.
 */
public class EditCategoryDialogController extends EntityDialogFrameController implements Initializable {


  protected Category category = null;


  @FXML
  protected TextField txtfldName;
  @FXML
  protected TextField txtfldDescription;


  @Override
  protected String getEntityType() {
    return "category";
  }


  @Override
  protected void setupControls() {
    super.setupControls();

    txtfldName.textProperty().addListener((observable, oldValue, newValue) -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.CategoryName));
    txtfldName.focusedProperty().addListener((observable, oldValue, newValue) -> fieldFocused("name"));

    txtfldDescription.textProperty().addListener((observable, oldValue, newValue) -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.CategoryDescription));
    txtfldDescription.focusedProperty().addListener((observable, oldValue, newValue) -> fieldFocused("description"));
  }

  protected void fieldFocused(String fieldName) {
    contextHelpControl.showContextHelpForResourceKey(fieldName);
  }


  public void setCategoryAndStage(Stage dialogStage, Category categoryToEdit) {
    this.category = categoryToEdit;
    setWindowStage(dialogStage, categoryToEdit);

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

  @Override
  protected void closeDialog() {
    if(category != null)
      category.removeEntityListener(categoryListener);

    super.closeDialog();
  }

  @Override
  protected void saveEntity() {
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
