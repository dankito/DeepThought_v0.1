package net.deepthought.controls.person;

import net.deepthought.controller.Dialogs;
import net.deepthought.controls.ICleanUp;
import net.deepthought.controls.utils.IEditedEntitiesHolder;
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
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.MenuItem;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

/**
 * Created by ganymed on 27/12/14.
 */
public class PersonListCell extends ListCell<Person> implements ICleanUp {

  private final static Logger log = LoggerFactory.getLogger(PersonListCell.class);


  protected Person person = null;

  protected IEditedEntitiesHolder<Person> editedPersonsHolder;

  protected HBox graphicPane = new HBox();

  protected CheckBox chkbxIsPersonSelected = new CheckBox();

  protected Label personDisplayNameLabel = new Label();

  protected Button btnEditPerson = new Button();


  // making Cell editable on F2 press is not senseful for Persons as they have a First and a Last Name

  public PersonListCell() {
    this(null);
  }

  public PersonListCell(IEditedEntitiesHolder<Person> editedPersonsHolder) {
    this.editedPersonsHolder = editedPersonsHolder;

    setupGraphic();

    itemProperty().addListener(new ChangeListener<Person>() {
      @Override
      public void changed(ObservableValue<? extends Person> observable, Person oldValue, Person newValue) {
        personChanged(newValue);
      }
    });

    // bind ListView Item's width to ListView's width
    listViewProperty().addListener((observable, oldValue, newValue) -> graphicPane.maxWidthProperty().bind(newValue.widthProperty().subtract(35)));

    if(editedPersonsHolder != null) {
      editedPersonsHolder.getEditedEntities().addListener(editedPersonsChangedListener);

      setOnMouseClicked(event -> mouseClicked(event));
    }

    setOnContextMenuRequested(event -> showContextMenu(event));
  }

  protected SetChangeListener<Person> editedPersonsChangedListener = change -> personUpdated();

  @Override
  public void cleanUp() {
    removeListener();

    if(editedPersonsHolder != null) {
      editedPersonsHolder.getEditedEntities().removeListener(editedPersonsChangedListener);

      editedPersonsHolder = null;
    }
  }

  protected void removeListener() {
    if(getItem() != null) {
      getItem().removeEntityListener(personListener);
    }

    if(person != null) { // don't know why but sometimes getItem() == null and person isn't
      person.removeEntityListener(personListener);
    }
  }

  protected void setupGraphic() {
    setText(null);
    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
    setAlignment(Pos.CENTER_LEFT);

    graphicPane.setAlignment(Pos.CENTER_LEFT);

    if(editedPersonsHolder != null) {
      graphicPane.getChildren().add(chkbxIsPersonSelected);
      chkbxIsPersonSelected.selectedProperty().addListener(checkBoxIsPersonSelectedChangeListener);
    }

    personDisplayNameLabel.setTextOverrun(OverrunStyle.ELLIPSIS);
    personDisplayNameLabel.setMaxWidth(Double.MAX_VALUE);
    graphicPane.getChildren().add(personDisplayNameLabel);

    HBox.setHgrow(personDisplayNameLabel, Priority.ALWAYS);
    HBox.setMargin(personDisplayNameLabel, new Insets(0, 6, 0, 0));

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
  }


  @Override
  protected void updateItem(Person item, boolean empty) {
    super.updateItem(item, empty);

    if(empty || item == null) {
      setGraphic(null);
    }
    else {
      personDisplayNameLabel.setText(item.getNameRepresentation());

      if(editedPersonsHolder != null) {
        chkbxIsPersonSelected.selectedProperty().removeListener(checkBoxIsPersonSelectedChangeListener);

        chkbxIsPersonSelected.setSelected(editedPersonsHolder.containsEditedEntity(item));

        chkbxIsPersonSelected.selectedProperty().addListener(checkBoxIsPersonSelectedChangeListener);
      }

      setGraphic(graphicPane);
    }
  }

  protected boolean isPersonSetOnEntity(Person person) {
    return editedPersonsHolder.containsEditedEntity(person);
  }


  protected void handleCheckBoxSelectedChanged(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
    if(newValue == true)
      editedPersonsHolder.addEntityToEntry(getItem());
    else
      editedPersonsHolder.removeEntityFromEntry(getItem());
  }

  protected void mouseClicked(MouseEvent event) {
    if(event.getClickCount() == 2 && event.getButton() == MouseButton.PRIMARY) {
      if(getItem() != null) {
        togglePersonAffiliation();
      }
    }
  }


  protected void selectCurrentCell() {
    getListView().getSelectionModel().select(getIndex());
  }

  protected void personChanged(Person newValue) {
    removeListener();

    person = newValue;

    if(newValue != null) {
      newValue.addEntityListener(personListener);
    }

    personUpdated();
  }

  protected void personUpdated() {
    updateItem(person, person == null);
  }


  protected void togglePersonAffiliation() {
    if(isPersonSetOnEntity(getItem()) == false)
      addPersonToEntity(getItem());
    else
      removePersonFromEntity(getItem());
  }

  protected void addPersonToEntity(Person person) {
    editedPersonsHolder.addEntityToEntry(person);
  }

  protected void removePersonFromEntity(Person person) {
    editedPersonsHolder.removeEntityFromEntry(person);
  }


  protected void showContextMenu(ContextMenuEvent event) {
    ContextMenu contextMenu = createContextMenu();

    contextMenu.show(event.getPickResult().getIntersectedNode(), event.getScreenX(), event.getScreenY());
  }

  protected ContextMenu createContextMenu() {
    ContextMenu contextMenu = new ContextMenu();

    MenuItem editTagItem = new MenuItem();
    JavaFxLocalization.bindMenuItemText(editTagItem, "edit...");
    editTagItem.setOnAction(actionEvent -> Dialogs.showEditPersonDialog(getItem() == null ? person : getItem()));
    contextMenu.getItems().add(editTagItem);

    contextMenu.getItems().add(new SeparatorMenuItem());

    MenuItem deleteTagItem = new MenuItem();
    JavaFxLocalization.bindMenuItemText(deleteTagItem, "delete");
    deleteTagItem.setOnAction(event -> deletePerson());
    contextMenu.getItems().add(deleteTagItem);

    return contextMenu;
  }

  protected void deletePerson() {
    Person personToDelete = getItem() == null ? person : getItem();

    if(Alerts.deletePersonWithUserConfirmationIfIsSetOnEntries(personToDelete)) {
      if(editedPersonsHolder != null && editedPersonsHolder.containsEditedEntity(personToDelete))
        editedPersonsHolder.removeEntityFromEntry(personToDelete);
    }
  }


  protected ChangeListener<Boolean> checkBoxIsPersonSelectedChangeListener = new ChangeListener<Boolean>() {
    @Override
    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
      handleCheckBoxSelectedChanged(observable, oldValue, newValue);
    }
  };

  protected void handleButtonEditPersonAction() {
    Dialogs.showEditPersonDialog(getItem());
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
