package net.deepthought.controls.person;

import net.deepthought.Application;
import net.deepthought.controller.Dialogs;
import net.deepthought.controls.ICleanableControl;
import net.deepthought.controls.LazyLoadingObservableList;
import net.deepthought.controls.event.PersonsControlPersonsEditedEvent;
import net.deepthought.controls.tag.IEditedEntitiesHolder;
import net.deepthought.data.listener.ApplicationListener;
import net.deepthought.data.model.DeepThought;
import net.deepthought.data.model.Person;
import net.deepthought.data.model.Tag;
import net.deepthought.data.model.listener.EntityListener;
import net.deepthought.data.model.ui.SystemTag;
import net.deepthought.data.persistence.db.BaseEntity;
import net.deepthought.data.search.Search;
import net.deepthought.util.Alerts;
import net.deepthought.util.JavaFxLocalization;
import net.deepthought.util.Localization;
import net.deepthought.util.Notification;

import org.controlsfx.control.textfield.CustomTextField;
import org.controlsfx.control.textfield.TextFields;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
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
public class SearchAndSelectPersonsControl extends VBox implements ICleanableControl {

  protected final static Logger log = LoggerFactory.getLogger(SearchAndSelectPersonsControl.class);


  protected IEditedEntitiesHolder<Person> editedPersonsHolder = null;

  protected DeepThought deepThought = null;

  protected LazyLoadingObservableList<Person> listViewAllPersonsItems;

  protected Search<Person> filterPersonsSearch = null;

  protected List<PersonListCell> personListCells = new ArrayList<>();


  @FXML
  protected HBox hboxSearchForPerson;
  @FXML
  protected TextField txtfldSearchForPerson;
  @FXML
  protected Button btnNewPerson;
  @FXML
  protected ListView<Person> lstvwAllPersons;


  public SearchAndSelectPersonsControl(IEditedEntitiesHolder<Person> editedPersonsHolder) {
    this.editedPersonsHolder = editedPersonsHolder;

    deepThought = Application.getDeepThought();

    Application.addApplicationListener(applicationListener);

    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("controls/SearchAndSelectPersonsControl.fxml"));
    fxmlLoader.setRoot(this);
    fxmlLoader.setController(this);
    fxmlLoader.setResources(Localization.getStringsResourceBundle());

    try {
      fxmlLoader.load();
      setupControl();

      if(deepThought != null)
        deepThought.addEntityListener(deepThoughtListener);
    } catch (IOException ex) {
      log.error("Could not load SearchAndSelectPersonsControl", ex);
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
  public void cleanUpControl() {
    Application.removeApplicationListener(applicationListener);

    if(this.deepThought != null)
      this.deepThought.removeEntityListener(deepThoughtListener);

    listViewAllPersonsItems.clear();

    for(PersonListCell cell : personListCells)
      cell.cleanUpControl();
    personListCells.clear();

    editedPersonsHolder = null;
  }

  protected void deepThoughtChanged(DeepThought newDeepThought) {
    if(this.deepThought != null)
      this.deepThought.removeEntityListener(deepThoughtListener);

    this.deepThought = newDeepThought;

    listViewAllPersonsItems.clear();

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
    JavaFxLocalization.bindTextInputControlPromptText(txtfldSearchForPerson, "search.for.person");
    txtfldSearchForPerson.textProperty().addListener(new ChangeListener<String>() {
      @Override
      public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
        filterPersons();
      }
    });
    txtfldSearchForPerson.setOnAction((event) -> handleTextFieldSearchPersonsAction());

    listViewAllPersonsItems = new LazyLoadingObservableList<>();
    lstvwAllPersons.setItems(listViewAllPersonsItems);

    lstvwAllPersons.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    lstvwAllPersons.setOnKeyReleased(event -> {
      if(event.getCode() == KeyCode.ENTER)
        toggleSelectedPersonsAffiliation();
      else if(event.getCode() == KeyCode.DELETE)
        deleteSelectedPersons();
    });

    lstvwAllPersons.setCellFactory((listView) -> {
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
      if(Alerts.deletePersonWithUserConfirmationIfIsSetOnEntries(deepThought, selectedPerson)) {
        if(editedPersonsHolder != null && editedPersonsHolder.containsEditedEntity(selectedPerson))
          editedPersonsHolder.removeEntityFromEntry(selectedPerson);
      }
    }
  }

  protected Collection<Person> getSelectedPersons() {
    return new ArrayList<>(lstvwAllPersons.getSelectionModel().getSelectedItems()); // make a copy as when multiple Persons are selected after removing first one SelectionModel gets cleared
  }


  protected void filterPersons() {
    if(filterPersonsSearch != null && filterPersonsSearch.isCompleted() == false)
      filterPersonsSearch.interrupt();

    filterPersonsSearch = new Search<>(txtfldSearchForPerson.getText(), (results) -> {
      listViewAllPersonsItems.setUnderlyingCollection(results);
    });
    Application.getSearchEngine().filterPersons(filterPersonsSearch);
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

    Dialogs.showEditPersonDialog(newPerson, null);
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
    filterPersons();
  }


}
