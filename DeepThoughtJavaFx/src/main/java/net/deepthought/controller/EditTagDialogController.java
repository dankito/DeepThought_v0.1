package net.deepthought.controller;

import net.deepthought.Application;
import net.deepthought.controller.enums.DialogResult;
import net.deepthought.controller.enums.FieldWithUnsavedChanges;
import net.deepthought.data.model.Tag;
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
public class EditTagDialogController extends EntityDialogFrameController implements Initializable {

  protected Tag tag = null;


  @FXML
  protected TextField txtfldName;
  @FXML
  protected TextField txtfldDescription;


  @Override
  protected String getEntityType() {
    return "tag";
  }


  @Override
  protected void setupControls() {
    super.setupControls();

    txtfldName.textProperty().addListener((observable, oldValue, newValue) -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.TagName));
    txtfldName.focusedProperty().addListener((observable, oldValue, newValue) -> fieldFocused("name"));

    txtfldDescription.textProperty().addListener((observable, oldValue, newValue) -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.TagDescription));
    txtfldDescription.focusedProperty().addListener((observable, oldValue, newValue) -> fieldFocused("description"));
  }

  protected void fieldFocused(String fieldName) {
    contextHelpControl.showContextHelpForResourceKey(fieldName);
  }


  public void setTagAndStage(Stage dialogStage, Tag tagToEdit) {
    this.tag = tagToEdit;
    setWindowStage(dialogStage, tagToEdit);

    tagToEditSet(tagToEdit);
    fieldsWithUnsavedChanges.clear();
    tag.addEntityListener(tagListener);

    txtfldName.selectAll();
    txtfldName.requestFocus();
  }

  protected void tagToEditSet(Tag tag) {
    txtfldName.setText(tag.getName());
    txtfldDescription.setText(tag.getDescription());
  }

  @Override
  protected void closeDialog() {
    if(tag != null)
      tag.removeEntityListener(tagListener);

    super.closeDialog();
  }

  protected void saveEntity() {
    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.TagName)) {
      tag.setName(txtfldName.getText());
      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.TagName);
    }

    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.TagDescription)) {
      tag.setDescription(txtfldDescription.getText());
      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.TagDescription);
    }

    if(tag.isPersisted() == false)
      Application.getDeepThought().addTag(tag);
  }


  protected EntityListener tagListener = new EntityListener() {
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
