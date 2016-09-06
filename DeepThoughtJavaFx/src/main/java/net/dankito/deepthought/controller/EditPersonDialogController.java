package net.dankito.deepthought.controller;

import net.dankito.deepthought.ui.enums.FieldWithUnsavedChanges;
import net.dankito.deepthought.data.model.Person;
import net.dankito.deepthought.data.model.listener.EntityListener;
import net.dankito.deepthought.data.persistence.db.BaseEntity;

import java.util.Collection;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Created by ganymed on 31/12/14.
 */
public class EditPersonDialogController extends EntityDialogFrameController implements Initializable {

  protected Person person = null;


  @FXML
  protected TextField txtfldFirstName;
  @FXML
  protected TextField txtfldLastName;
  @FXML
  protected TextArea txtarNotes;



  @Override
  protected String getEntityType() {
    return "person";
  }

  @Override
  protected String getEntityPreview() {
    return person.getNameRepresentation();
  }

  @Override
  protected void setupControls() {
    super.setupControls();

    txtfldFirstName.textProperty().addListener((observable, oldValue, newValue) -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.PersonFirstName));
    txtfldFirstName.focusedProperty().addListener((observable, oldValue, newValue) -> fieldFocused("first.name"));

    txtfldLastName.textProperty().addListener((observable, oldValue, newValue) -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.PersonLastName));
    txtfldLastName.focusedProperty().addListener((observable, oldValue, newValue) -> fieldFocused("last.name"));

    txtarNotes.textProperty().addListener((observable, oldValue, newValue) -> fieldsWithUnsavedChanges.add(FieldWithUnsavedChanges.PersonNotes));
    txtarNotes.focusedProperty().addListener((observable, oldValue, newValue) -> fieldFocused("notes"));
  }

  protected void fieldFocused(String fieldName) {
    contextHelpControl.showContextHelpForResourceKey(fieldName);
  }


  public void setPersonAndStage(Stage dialogStage, Person personToEdit) {
    this.person = personToEdit;
    setWindowStage(dialogStage, personToEdit);

    personToEditSet(personToEdit);
    fieldsWithUnsavedChanges.clear();
    person.addEntityListener(personListener);

    txtfldFirstName.selectAll();
    txtfldFirstName.requestFocus();
  }

  protected void personToEditSet(Person person) {
    txtfldFirstName.setText(person.getFirstName());
    txtfldLastName.setText(person.getLastName());
    txtarNotes.setText(person.getNotes());
  }

  @Override
  protected void closeDialog() {
    if(person != null)
      person.removeEntityListener(personListener);

    super.closeDialog();
  }

  @Override
  protected void saveEntity() {
    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.PersonFirstName)) {
      person.setFirstName(txtfldFirstName.getText());
      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.PersonFirstName);
    }
    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.PersonLastName)) {
      person.setLastName(txtfldLastName.getText());
      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.PersonLastName);
    }

    if(fieldsWithUnsavedChanges.contains(FieldWithUnsavedChanges.PersonNotes)) {
      person.setNotes(txtarNotes.getText());
      fieldsWithUnsavedChanges.remove(FieldWithUnsavedChanges.PersonNotes);
    }
  }


  protected EntityListener personListener = new EntityListener() {
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
