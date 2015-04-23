package net.deepthought.controls.person;

import net.deepthought.Application;
import net.deepthought.controls.event.PersonsControlPersonsEditedEvent;
import net.deepthought.data.listener.ApplicationListener;
import net.deepthought.data.model.DeepThought;
import net.deepthought.data.model.Entry;
import net.deepthought.data.model.Person;
import net.deepthought.data.model.ReferenceBase;
import net.deepthought.data.model.listener.EntityListener;
import net.deepthought.data.persistence.db.BaseEntity;
import net.deepthought.data.search.Search;
import net.deepthought.util.DeepThoughtError;
import net.deepthought.util.JavaFxLocalization;
import net.deepthought.util.Localization;

import org.controlsfx.control.textfield.CustomTextField;
import org.controlsfx.control.textfield.TextFields;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * Created by ganymed on 01/02/15.
 */
public abstract class PersonsControl extends TitledPane {

  protected final static Logger log = LoggerFactory.getLogger(PersonsControl.class);


  protected Entry entry = null;

  protected DeepThought deepThought = null;

  protected ObservableList<Person> listViewAllPersonsItems;
  protected FilteredList<Person> filteredPersons;
  protected SortedList<Person> sortedFilteredPersons;

  protected Search<Person> filterPersonsSearch = null;

  protected Set<Person> currentlySetPersonsOnEntity = new HashSet<>();
  protected Set<Person> addedPersons = new HashSet<>();
  protected Set<Person> removedPersons = new HashSet<>();
  protected Set<Person> editedEntityPersons = new HashSet<>();

  protected List<PersonListCell> personListCells = new ArrayList<>();

  protected EventHandler<PersonsControlPersonsEditedEvent> personAddedEventHandler = null;
  protected EventHandler<PersonsControlPersonsEditedEvent> personRemovedEventHandler = null;


  @FXML
  protected Pane pnPersonsGraphic;
  @FXML
  protected VBox pnSelectedPersonsPreview;
  @FXML
  protected HBox hboxSearchForPerson;
  @FXML
  protected TextField txtfldSearchForPerson;
  @FXML
  protected Button btnNewPerson;
  @FXML
  protected ListView<Person> lstvwAllPersons;


  public PersonsControl() {
    deepThought = Application.getDeepThought();

    Application.addApplicationListener(new ApplicationListener() {
      @Override
      public void deepThoughtChanged(DeepThought deepThought) {
        PersonsControl.this.deepThoughtChanged(deepThought);
      }

      @Override
      public void errorOccurred(DeepThoughtError error) {

      }
    });

    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("controls/PersonsControl.fxml"));
    fxmlLoader.setRoot(this);
    fxmlLoader.setController(this);
    fxmlLoader.setResources(Localization.getStringsResourceBundle());

    try {
      fxmlLoader.load();
      setupControl();

      if(deepThought != null)
        deepThought.addEntityListener(deepThoughtListener);
    } catch (IOException ex) {
      log.error("Could not load PersonsControl", ex);
    }
  }

  protected void deepThoughtChanged(DeepThought newDeepThought) {
    if(this.deepThought != null)
      this.deepThought.removeEntityListener(deepThoughtListener);

    this.deepThought = newDeepThought;

    listViewAllPersonsItems.clear();

    if(newDeepThought != null) {
      newDeepThought.addEntityListener(deepThoughtListener);
      listViewAllPersonsItems.addAll(deepThought.getPersons());
      filterPersons();
    }
  }

  protected void setupControl() {
    this.setExpanded(false);
    this.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

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
    txtfldSearchForPerson.setOnAction((event) -> handleTextFieldSearchAuthorsAction());

    listViewAllPersonsItems = lstvwAllPersons.getItems();
    filteredPersons = new FilteredList<>(listViewAllPersonsItems, tag -> true);
    sortedFilteredPersons = new SortedList<>(filteredPersons, personComparator);
    lstvwAllPersons.setItems(sortedFilteredPersons);
    if(deepThought != null)
      listViewAllPersonsItems.addAll(deepThought.getPersonsSorted());

    lstvwAllPersons.setCellFactory((listView) -> {
      final PersonListCell cell = createPersonListCell();
      personListCells.add(cell);
      return cell;
    });
  }

  protected abstract PersonListCell createPersonListCell();
//  protected PersonListCell createPersonListCell() {
//    return new PersonListCell(this);
//  }

  protected void setEntityPersons(Set<Person> persons) {
    currentlySetPersonsOnEntity = persons;
    editedEntityPersons = persons;

    addedPersons.clear();
    removedPersons.clear();

    updatePersonsSetOnEntityPreview();
  }

  protected void updatePersonsSetOnEntityPreview() {
    pnSelectedPersonsPreview.getChildren().clear();


    for(Person person : new TreeSet<>(editedEntityPersons)) {
      pnSelectedPersonsPreview.getChildren().add(createPersonPreviewLabel(person));
    }
  }

  protected PersonLabel createPersonPreviewLabel(Person person) {
    PersonLabel label = new PersonLabel(person);
    label.setOnButtonRemoveItemFromCollectionEventHandler((event) -> removePersonFromEntity(person));
    return label;
  }

  protected void addPersonToEntity(Person person) {
    if(removedPersons.contains(person)) {
      removedPersons.remove(person);
    }
    else {
      addedPersons.add(person);
    }

    editedEntityPersons.add(person);

    updatePersonsSetOnEntityPreview();
    firePersonAddedEvent(person);

  }

  protected void removePersonFromEntity(Person person) {
    if(addedPersons.contains(person)) {
      addedPersons.remove(person);
    }
    else {
      removedPersons.add(person);
    }

    editedEntityPersons.remove(person);

    updatePersonsSetOnEntityPreview();
    firePersonRemovedEvent(person);
  }

  protected void filterPersons() {
    if(filterPersonsSearch != null)
      filterPersonsSearch.interrupt();

    filterPersonsSearch = new Search<>(txtfldSearchForPerson.getText(), (results) -> {
      filteredPersons.setPredicate((person) -> results.contains(person));
    });
    Application.getSearchEngine().filterPersons(filterPersonsSearch);

//    filterPersonsManually();
  }

  protected void filterPersonsManually() {
    String filter = txtfldSearchForPerson.getText();
    String lowerCaseFilter = filter == null ? "" : filter.toLowerCase();
    final boolean filterForFirstAndLastName = lowerCaseFilter.contains(",");
    String lastNameFilterTemp, firstNameFilterTemp = null;

    if(filterForFirstAndLastName == false)
      lastNameFilterTemp = firstNameFilterTemp = lowerCaseFilter;
    else {
      lastNameFilterTemp = lowerCaseFilter.substring(0, lowerCaseFilter.indexOf(",")).trim();
      firstNameFilterTemp = lowerCaseFilter.substring(lowerCaseFilter.indexOf(","));
      firstNameFilterTemp = firstNameFilterTemp.substring(1).trim();
    }

    final String lastNameFilter = lastNameFilterTemp;
    final String firstNameFilter = firstNameFilterTemp;

    filteredPersons.setPredicate((person) -> {
      // If filter text is empty, display all Persons.
      if (filter == null || filter.isEmpty()) {
        return true;
      }


      if(filterForFirstAndLastName == false) {
        if (person.getLastName().toLowerCase().contains(lowerCaseFilter)) {
          return true; // Filter matches last name
        } else if (person.getFirstName().toLowerCase().contains(lowerCaseFilter)) {
          return true; // Filter matches first name
        }
      }
      else {
        return person.getLastName().toLowerCase().contains(lastNameFilter) && person.getFirstName().toLowerCase().contains(firstNameFilter);
      }

      return false; // Does not match.
    });
  }

  public void close() {
    if(deepThought != null)
      deepThought.removeEntityListener(deepThoughtListener);
  }


  protected void handleTextFieldSearchAuthorsAction() {
    final Person newPerson = Person.createPersonFromStringRepresentation(txtfldSearchForPerson.getText());

    if(deepThought != null)
      deepThought.addPerson(newPerson);

    addPersonToEntity(newPerson);
  }

  @FXML
  public void handleButtonNewPersonAction(ActionEvent event) {
    final Person newPerson = Person.createPersonFromStringRepresentation(txtfldSearchForPerson.getText());

    net.deepthought.controller.Dialogs.showEditPersonDialog(newPerson, null);
  }


  protected Comparator<Person> personComparator = new Comparator<Person>() {
    @Override
    public int compare(Person o1, Person o2) {
      return o1.compareTo(o2);
    }
  };


  protected EntityListener deepThoughtListener = new EntityListener() {
    @Override
    public void propertyChanged(BaseEntity entity, String propertyName, Object previousValue, Object newValue) {

    }

    @Override
    public void entityAddedToCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity addedEntity) {
      if(collection == Application.getDeepThought().getPersons()) {
        resetListViewAllPersonsItems();
      }
    }

    @Override
    public void entityOfCollectionUpdated(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity updatedEntity) {
      if(collection == Application.getDeepThought().getPersons()) {
        resetListViewAllPersonsItems();
        updatePersonsSetOnEntityPreview();
      }
    }

    @Override
    public void entityRemovedFromCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity removedEntity) {
      if(collection == Application.getDeepThought().getPersons()) {
        resetListViewAllPersonsItems();
      }
    }
  };

  protected void resetListViewAllPersonsItems() {
    listViewAllPersonsItems.clear();
    listViewAllPersonsItems.addAll(deepThought.getPersonsSorted());
    filterPersons();
  }


  protected void firePersonAddedEvent(Person person) {
    if(personAddedEventHandler != null)
      personAddedEventHandler.handle(new PersonsControlPersonsEditedEvent(this, person));
  }

  protected void firePersonRemovedEvent(Person person) {
    if(personRemovedEventHandler != null)
      personRemovedEventHandler.handle(new PersonsControlPersonsEditedEvent(this, person));
  }

  public EventHandler<PersonsControlPersonsEditedEvent> getPersonAddedEventHandler() {
    return personAddedEventHandler;
  }

  public void setPersonAddedEventHandler(EventHandler<PersonsControlPersonsEditedEvent> personAddedEventHandler) {
    this.personAddedEventHandler = personAddedEventHandler;
  }

  public EventHandler<PersonsControlPersonsEditedEvent> getPersonRemovedEventHandler() {
    return personRemovedEventHandler;
  }

  public void setPersonRemovedEventHandler(EventHandler<PersonsControlPersonsEditedEvent> personRemovedEventHandler) {
    this.personRemovedEventHandler = personRemovedEventHandler;
  }


  public Set<Person> getEditedEntityPersons() {
    return editedEntityPersons;
  }

  public Set<Person> getRemovedPersons() {
    return removedPersons;
  }

  public Set<Person> getAddedPersons() {
    return addedPersons;
  }

  public Set<Person> getCopyOfRemovedPersonsAndClear() {
    Set<Person> copy = new HashSet<>(removedPersons);
    removedPersons.clear();
    return copy;
  }

  public Set<Person> getCopyOfAddedPersonsAndClear() {
    Set<Person> copy = new HashSet<>(addedPersons);
    addedPersons.clear();
    return copy;
  }

}
