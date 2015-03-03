package net.deepthought.controls.person;

import net.deepthought.Application;
import net.deepthought.controller.Dialogs;
import net.deepthought.data.model.Person;
import net.deepthought.data.model.enums.PersonRole;
import net.deepthought.data.model.listener.EntityListener;
import net.deepthought.data.persistence.db.BaseEntity;
import net.deepthought.util.Alerts;
import net.deepthought.util.JavaFxLocalization;
import net.deepthought.util.Localization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * Created by ganymed on 27/12/14.
 */
public class PersonListCell extends ListCell<Person> {

  private final static Logger log = LoggerFactory.getLogger(PersonListCell.class);


  protected PersonsControl personsControl;

  protected PersonRole defaultPersonRole = null;

  protected HBox graphicPane = new HBox();

  protected Label personDisplayNameLabel = new Label();

  protected Button btnAddPersonWithoutRole = new Button();
  protected Button btnAddPersonInDefaultRole = new Button();
  protected MenuButton btnSelectPersonRole = new MenuButton();
  protected Button btnEditPerson = new Button();
  protected Button btnDeletePerson = new Button();


  public PersonListCell(PersonsControl personsControl) {
    this.personsControl = personsControl;

    this.defaultPersonRole = PersonRole.getAuthorPersonRole();

    setText(null);
    setupGraphic();

    itemProperty().addListener(new ChangeListener<Person>() {
      @Override
      public void changed(ObservableValue<? extends Person> observable, Person oldValue, Person newValue) {
        itemChanged(newValue);
      }
    });

    setOnMouseClicked(event -> mouseClicked(event));
  }

  protected void setupGraphic() {
    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
    setAlignment(Pos.CENTER_LEFT);

    graphicPane.setAlignment(Pos.CENTER_LEFT);

    HBox.setHgrow(personDisplayNameLabel, Priority.ALWAYS);
    HBox.setMargin(personDisplayNameLabel, new Insets(0, 6, 0, 0));

    personDisplayNameLabel.setMaxWidth(Double.MAX_VALUE);
    graphicPane.getChildren().add(personDisplayNameLabel);

    JavaFxLocalization.bindLabeledText(btnAddPersonWithoutRole, "person.role.without.role");
    btnAddPersonWithoutRole.setMinWidth(80);
    HBox.setMargin(btnAddPersonWithoutRole, new Insets(0, 6, 0, 0));
    graphicPane.getChildren().add(btnAddPersonWithoutRole);

    btnAddPersonWithoutRole.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent event) {
        selectCurrentCell();
        handleButtonAddPersonWithoutRoleAction();
      }
    });

    if(defaultPersonRole != null)
      JavaFxLocalization.bindLabeledText(btnAddPersonInDefaultRole, "add.as.default.role", defaultPersonRole.getName());
    btnAddPersonInDefaultRole.setVisible(defaultPersonRole != null);
    btnAddPersonInDefaultRole.setMinWidth(80);
    HBox.setMargin(btnAddPersonInDefaultRole, new Insets(0, 6, 0, 0));
    graphicPane.getChildren().add(btnAddPersonInDefaultRole);

    btnAddPersonInDefaultRole.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent event) {
        selectCurrentCell();
        handleButtonAddPersonInDefaultRoleAction();
      }
    });

    JavaFxLocalization.bindLabeledText(btnSelectPersonRole, "as...");
    btnSelectPersonRole.setMinWidth(80);
    HBox.setMargin(btnSelectPersonRole, new Insets(0, 6, 0, 0));
    graphicPane.getChildren().add(btnSelectPersonRole);

    btnSelectPersonRole.showingProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        createButtonSelectPersonRoleItems();

        selectCurrentCell();
      }
    });

    JavaFxLocalization.bindLabeledText(btnEditPerson, "edit");
    btnEditPerson.setMinWidth(80);
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


  protected void setDefaultPersonRole(PersonRole defaultPersonRole) {
    this.defaultPersonRole = defaultPersonRole;

    if(defaultPersonRole != null)
      JavaFxLocalization.bindLabeledText(btnAddPersonInDefaultRole, "add.as.default.role", defaultPersonRole.getName());
    setDefaultRoleButtonsVisibleState();
  }


  @Override
  protected void updateItem(Person item, boolean empty) {
    super.updateItem(item, empty);

    if(empty) {
      setGraphic(null);
    }
    else {
      setGraphic(graphicPane);
      personDisplayNameLabel.setText(item.getNameRepresentation());
      setDefaultRoleButtonsVisibleState();
    }
  }

  protected void setDefaultRoleButtonsVisibleState() {
    btnAddPersonWithoutRole.setVisible(getItem() != null && isPersonSetInPersonRoleOnEntity(PersonRole.getWithoutRolePersonRole(), getItem()) == false);
    btnAddPersonInDefaultRole.setVisible(defaultPersonRole != null && getItem() != null && isPersonSetInPersonRoleOnEntity(defaultPersonRole, getItem()) == false);
  }

  protected boolean isPersonSetInPersonRoleOnEntity(PersonRole role, Person person) {
    return personsControl.getEditedEntityPersons().containsKey(role) && personsControl.getEditedEntityPersons().get(role).contains(person);
  }


  protected void mouseClicked(MouseEvent event) {
    if(event.getClickCount() == 2 && event.getButton() == MouseButton.PRIMARY) {
      Dialogs.showEditPersonDialog(getItem());
    }
  }


  protected void selectCurrentCell() {
    getListView().getSelectionModel().select(getIndex());
  }

  protected void itemChanged(Person newValue) {
    if(getItem() != null) {
      getItem().removeEntityListener(personListener);
    }

    if(newValue != null) {
      newValue.addEntityListener(personListener);
    }

    updateItem(newValue, newValue == null);
  }


  protected void handleButtonAddPersonWithoutRoleAction() {
    addPersonInRoleToEntity(getItem(), PersonRole.getWithoutRolePersonRole());
  }

  protected void handleButtonAddPersonInDefaultRoleAction() {
    addPersonInRoleToEntity(getItem(), defaultPersonRole);
  }


  protected void createButtonSelectPersonRoleItems() {
    btnSelectPersonRole.getItems().clear();

    for(final PersonRole role : Application.getDeepThought().getPersonRolesSorted()) {
      MenuItem roleMenuItem = new MenuItem(role.getName());
      roleMenuItem.setDisable(isPersonSetInPersonRoleOnEntity(role, getItem()));
      btnSelectPersonRole.getItems().add(roleMenuItem);

      roleMenuItem.setOnAction(new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
          addPersonInRoleToEntity(getItem(), role);
        }
      });
    }

    btnSelectPersonRole.getItems().add(new SeparatorMenuItem());

    MenuItem createNewRoleMenuItem = new MenuItem(Localization.getLocalizedStringForResourceKey("create.new.role"));
    createNewRoleMenuItem.setDisable(true);
    btnSelectPersonRole.getItems().add(createNewRoleMenuItem);

//    createNewRoleMenuItem.setOnAction((event) -> );
  }

  protected void addPersonInRoleToEntity(Person person, PersonRole role) {
    personsControl.addPersonToEntity(role, person);
    setDefaultRoleButtonsVisibleState();
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
        itemChanged((Person)entity);
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
