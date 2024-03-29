package net.dankito.deepthought.controls.person;

import net.dankito.deepthought.Application;
import net.dankito.deepthought.controls.ICleanUp;
import net.dankito.deepthought.controls.utils.FXUtils;
import net.dankito.deepthought.data.listener.ApplicationListener;
import net.dankito.deepthought.data.model.DeepThought;
import net.dankito.deepthought.data.model.Person;
import net.dankito.deepthought.data.model.listener.EntityListener;
import net.dankito.deepthought.data.persistence.db.BaseEntity;
import net.dankito.deepthought.data.search.Search;
import net.dankito.deepthought.util.Notification;

import org.controlsfx.control.textfield.CustomTextField;
import org.controlsfx.control.textfield.TextFields;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * Created by ganymed on 01/02/15.
 */
public class SearchAndSelectPersonsControl extends VBox implements ICleanUp {

  protected final static Logger log = LoggerFactory.getLogger(SearchAndSelectPersonsControl.class);


  protected net.dankito.deepthought.controls.utils.IEditedEntitiesHolder<Person> editedPersonsHolder = null;

  protected DeepThought deepThought = null;

  protected net.dankito.deepthought.controls.LazyLoadingObservableList<Person> listViewPersonsItems;

  protected Search<Person> lastPersonsSearch = null;

  protected List<PersonListCell> personListCells = new ArrayList<>();


  @FXML
  protected HBox hboxSearchForPerson;
  @FXML
  protected TextField txtfldSearchForPerson;
  @FXML
  protected Button btnNewPerson;
  @FXML
  protected ListView<Person> lstvwPersons;


  public SearchAndSelectPersonsControl(net.dankito.deepthought.controls.utils.IEditedEntitiesHolder<Person> editedPersonsHolder) {
    this.editedPersonsHolder = editedPersonsHolder;

    deepThought = Application.getDeepThought();

    Application.addApplicationListener(applicationListener);

    if(FXUtils.loadControl(this, "SearchAndSelectPersonsControl")) {
      setupControl();

      if(deepThought != null)
        deepThought.addEntityListener(deepThoughtListener);
    }
  }

  protected ApplicationListener applicationListener = new ApplicationListener() {
    @Override
    public void deepThoughtChanged(DeepThought deepThought) {
      SearchAndSelectPersonsControl.this.deepThoughtChanged(deepThought);
    }

    @Override
    public void notification(Notification notification) {

    }
  };


  @Override
  public void cleanUp() {
    Application.removeApplicationListener(applicationListener);

    if(this.deepThought != null)
      this.deepThought.removeEntityListener(deepThoughtListener);

    listViewPersonsItems.clear();

    for(PersonListCell cell : personListCells)
      cell.cleanUp();
    personListCells.clear();

    editedPersonsHolder = null;
  }

  protected void deepThoughtChanged(DeepThought newDeepThought) {
    if(this.deepThought != null)
      this.deepThought.removeEntityListener(deepThoughtListener);

    this.deepThought = newDeepThought;

    listViewPersonsItems.clear();

    if(newDeepThought != null) {
      newDeepThought.addEntityListener(deepThoughtListener);
      resetListViewAllPersonsItems();
    }
  }

  protected void setupControl() {
    // replace normal TextField txtfldSearchForPerson with a SearchTextField (with a cross to clear selection)
    hboxSearchForPerson.getChildren().remove(txtfldSearchForPerson);
    txtfldSearchForPerson = (CustomTextField) TextFields.createClearableTextField();
    txtfldSearchForPerson.setId("txtfldSearchForPerson");
    hboxSearchForPerson.getChildren().add(1, txtfldSearchForPerson);
    HBox.setHgrow(txtfldSearchForPerson, Priority.ALWAYS);
    net.dankito.deepthought.util.localization.JavaFxLocalization.bindTextInputControlPromptText(txtfldSearchForPerson, "search.person.prompt.text");
    txtfldSearchForPerson.textProperty().addListener(new ChangeListener<String>() {
      @Override
      public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
        searchPersons();
      }
    });
    txtfldSearchForPerson.setOnAction((event) -> handleTextFieldSearchPersonsAction());
    txtfldSearchForPerson.setOnKeyReleased(event -> {
      if (event.getCode() == KeyCode.ESCAPE) {
        txtfldSearchForPerson.clear();
        event.consume();
      }
    });

    listViewPersonsItems = new net.dankito.deepthought.controls.LazyLoadingObservableList<>();
    lstvwPersons.setItems(listViewPersonsItems);

    lstvwPersons.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    lstvwPersons.setOnKeyPressed(event -> {
      if (event.getCode() == KeyCode.ENTER) {
        toggleSelectedPersonsAffiliation();
        event.consume();
      } else if (event.getCode() == KeyCode.DELETE) {
        deleteSelectedPersons();
        event.consume();
      }
    });

    lstvwPersons.setCellFactory((listView) -> {
      final PersonListCell cell = createPersonListCell();
      personListCells.add(cell);
      return cell;
    });

    resetListViewAllPersonsItems();
  }

  protected PersonListCell createPersonListCell() {
    return new PersonListCell(editedPersonsHolder);
  }

  protected void togglePersonAffiliation(Person person) {
    if(editedPersonsHolder.containsEditedEntity(person))
      editedPersonsHolder.removeEntityFromEntry(person);
    else
      editedPersonsHolder.addEntityToEntry(person);
  }

  protected void toggleSelectedPersonsAffiliation() {
    for(Person selectedPerson : getSelectedPersons()) {
      togglePersonAffiliation(selectedPerson);
    }
  }

  protected void deleteSelectedPersons() {
    for(Person selectedPerson : getSelectedPersons()) {
      if(net.dankito.deepthought.util.Alerts.deletePersonWithUserConfirmationIfIsSetOnEntries(deepThought, selectedPerson)) {
        if(editedPersonsHolder != null && editedPersonsHolder.containsEditedEntity(selectedPerson))
          editedPersonsHolder.removeEntityFromEntry(selectedPerson);
      }
    }
  }

  protected Collection<Person> getSelectedPersons() {
    return new ArrayList<>(lstvwPersons.getSelectionModel().getSelectedItems()); // make a copy as when multiple Persons are selected after removing first one SelectionModel gets cleared
  }


  protected void searchPersons() {
    if(lastPersonsSearch != null && lastPersonsSearch.isCompleted() == false)
      lastPersonsSearch.interrupt();

    lastPersonsSearch = new Search<>(txtfldSearchForPerson.getText(), (results) -> {
      listViewPersonsItems.setUnderlyingCollection(results);
    });
    Application.getSearchEngine().searchPersons(lastPersonsSearch);
  }


  protected void handleTextFieldSearchPersonsAction() {
    // TODO: check if person of that Name exists and if so don't create a new one but add existing one
    final Person newPerson = Person.createPersonFromStringRepresentation(txtfldSearchForPerson.getText());

    if(deepThought != null)
      deepThought.addPerson(newPerson);

    togglePersonAffiliation(newPerson);
  }

  @FXML
  public void handleButtonNewPersonAction(ActionEvent event) {
    final Person newPerson = Person.createPersonFromStringRepresentation(txtfldSearchForPerson.getText());

    net.dankito.deepthought.controller.Dialogs.showEditPersonDialog(newPerson, null);
  }


  protected EntityListener deepThoughtListener = new EntityListener() {
    @Override
    public void propertyChanged(BaseEntity entity, String propertyName, Object previousValue, Object newValue) {

    }

    @Override
    public void entityAddedToCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity addedEntity) {
      if(collection == deepThought.getPersons()) {
        resetListViewAllPersonsItems();
      }
    }

    @Override
    public void entityOfCollectionUpdated(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity updatedEntity) {
      if(collection == deepThought.getPersons()) {
        resetListViewAllPersonsItems();
      }
    }

    @Override
    public void entityRemovedFromCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity removedEntity) {
      if(collection == deepThought.getPersons()) {
        resetListViewAllPersonsItems();
      }
    }
  };

  protected void resetListViewAllPersonsItems() {
    searchPersons();
  }


}
