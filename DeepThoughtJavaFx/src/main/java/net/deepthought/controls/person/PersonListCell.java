package net.deepthought.controls.person;

import net.deepthought.controller.Dialogs;
import net.deepthought.controls.ICleanableControl;
import net.deepthought.controls.tag.IEditedEntitiesHolder;
import net.deepthought.data.model.Person;
import net.deepthought.data.model.listener.EntityListener;
import net.deepthought.data.persistence.db.BaseEntity;
import net.deepthought.util.Alerts;
import net.deepthought.util.JavaFxLocalization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.SetChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * Created by ganymed on 27/12/14.
 */
public class PersonListCell extends ListCell<Person> implements ICleanableControl {

  private final static Logger log = LoggerFactory.getLogger(PersonListCell.class);


  protected Person person = null;

  protected IEditedEntitiesHolder<Person> editedPersonsHolder;

  protected HBox graphicPane = new HBox();

  protected Label personDisplayNameLabel = new Label();

  protected Button btnAddOrRemovePerson = new Button();
  protected Button btnEditPerson = new Button();
  protected Button btnDeletePerson = new Button();


  public PersonListCell(IEditedEntitiesHolder<Person> editedPersonsHolder) {
    this.editedPersonsHolder = editedPersonsHolder;

    editedPersonsHolder.getEditedEntities().addListener(editedPersonsChangedListener);

    setText(null);
    setupGraphic();

    itemProperty().addListener(new ChangeListener<Person>() {
      @Override
      public void changed(ObservableValue<? extends Person> observable, Person oldValue, Person newValue) {
        personChanged(newValue);
      }
    });

    setOnMouseClicked(event -> mouseClicked(event));
  }

  protected SetChangeListener<Person> editedPersonsChangedListener = change -> personUpdated();

  @Override
  public void cleanUpControl() {
    if(getItem() != null) {
      getItem().removeEntityListener(personListener);
    }

    if(person != null) { // don't know why but sometimes getItem() == null and person isn't
      person.removeEntityListener(personListener);
    }

    editedPersonsHolder.getEditedEntities().removeListener(editedPersonsChangedListener);

    editedPersonsHolder = null;
  }

  protected void setupGraphic() {
    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
    setAlignment(Pos.CENTER_LEFT);

    graphicPane.setAlignment(Pos.CENTER_LEFT);

    HBox.setHgrow(personDisplayNameLabel, Priority.ALWAYS);
    HBox.setMargin(personDisplayNameLabel, new Insets(0, 6, 0, 0));

    personDisplayNameLabel.setMaxWidth(Double.MAX_VALUE);
    graphicPane.getChildren().add(personDisplayNameLabel);

    JavaFxLocalization.bindLabeledText(btnAddOrRemovePerson, "add");
    btnAddOrRemovePerson.setMinWidth(100);
    HBox.setMargin(btnAddOrRemovePerson, new Insets(0, 6, 0, 0));
    graphicPane.getChildren().add(btnAddOrRemovePerson);

    btnAddOrRemovePerson.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent event) {
        selectCurrentCell();
        handleButtonAddOrRemovePersonAction();
      }
    });

    JavaFxLocalization.bindLabeledText(btnEditPerson, "edit");
    btnEditPerson.setMinWidth(100);
    graphicPane.getChildren().add(btnEditPerson);
    btnEditPerson.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent event) {
        selectCurrentCell();
        handleButtonEditPersonAction();
      }
    });

    btnDeletePerson.setText("-");
    btnDeletePerson.setTextFill(Color.RED);
    btnDeletePerson.setFont(new Font(15));
    HBox.setMargin(btnDeletePerson, new Insets(0, 0, 0, 6));
    graphicPane.getChildren().add(btnDeletePerson);
    btnDeletePerson.setOnAction((event) -> handleButtonDeletePersonAction(event));
  }


  @Override
  protected void updateItem(Person item, boolean empty) {
    super.updateItem(item, empty);

    if(empty || item == null) {
      setGraphic(null);
    }
    else {
      setGraphic(graphicPane);
      personDisplayNameLabel.setText(item.getNameRepresentation());
      setButtonAddOrRemovePersonState();
    }
  }

  protected void setButtonAddOrRemovePersonState() {
    btnAddOrRemovePerson.setVisible(getItem() != null);

    if(getItem() != null) {
      if(isPersonSetOnEntity(getItem()) == false)
        JavaFxLocalization.bindLabeledText(btnAddOrRemovePerson, "add");
      else
        JavaFxLocalization.bindLabeledText(btnAddOrRemovePerson, "remove");
    }
  }

  protected boolean isPersonSetOnEntity(Person person) {
    return editedPersonsHolder.containsEditedEntity(person);
  }


  protected void mouseClicked(MouseEvent event) {
    if(event.getClickCount() == 2 && event.getButton() == MouseButton.PRIMARY) {
      //Dialogs.showEditPersonDialog(getItem());

      if(getItem() != null) {
        if (isPersonSetOnEntity(getItem()) == false)
          addPersonToEntity(getItem());
        else
          removePersonFromEntity(getItem());
      }
    }
  }


  protected void selectCurrentCell() {
    getListView().getSelectionModel().select(getIndex());
  }

  protected void personChanged(Person newValue) {
    if(person != null) {
      person.removeEntityListener(personListener);
    }

    person = newValue;

    if(newValue != null) {
      newValue.addEntityListener(personListener);
    }

    personUpdated();
  }

  protected void personUpdated() {
    updateItem(person, person == null);
  }


  protected void handleButtonAddOrRemovePersonAction() {
    if(isPersonSetOnEntity(getItem()) == false)
      addPersonToEntity(getItem());
    else
      removePersonFromEntity(getItem());
  }

  protected void addPersonToEntity(Person person) {
    editedPersonsHolder.addEntityToEntry(person);
    setButtonAddOrRemovePersonState();
  }

  protected void removePersonFromEntity(Person person) {
    editedPersonsHolder.removeEntityFromEntry(person);
    setButtonAddOrRemovePersonState();
  }



  protected void handleButtonEditPersonAction() {
    Dialogs.showEditPersonDialog(getItem());
  }

  protected void handleButtonDeletePersonAction(ActionEvent event) {
    Alerts.deletePersonWithUserConfirmationIfIsSetOnEntries(getItem());
  }



  protected EntityListener personListener = new EntityListener() {
    @Override
    public void propertyChanged(BaseEntity entity, String propertyName, Object previousValue, Object newValue) {
//      personDisplayNameLabel.setText(((Person)entity).getNameRepresentation());
      if(entity == getItem())
        personChanged((Person) entity);
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
