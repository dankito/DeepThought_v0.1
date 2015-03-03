package net.deepthought.controls.person;

import net.deepthought.Application;
import net.deepthought.controls.event.PersonsControlPersonsEditedEvent;
import net.deepthought.data.listener.ApplicationListener;
import net.deepthought.data.model.DeepThought;
import net.deepthought.data.model.Entry;
import net.deepthought.data.model.Person;
import net.deepthought.data.model.enums.PersonRole;
import net.deepthought.data.model.listener.EntityListener;
import net.deepthought.data.persistence.db.BaseEntity;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.FlowPane;
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

  protected Map<PersonRole, Set<Person>> currentlySetPersonsOnEntity = new HashMap<>();
  protected Map<PersonRole, Set<Person>> addedPersons = new HashMap<>();
  protected Map<PersonRole, Set<Person>> removedPersons = new HashMap<>();
  protected Map<PersonRole, Set<Person>> editedEntityPersons = new HashMap<>();

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

  protected void setEntityPersons(Map<PersonRole, Set<Person>> persons) {
    currentlySetPersonsOnEntity = persons;
    editedEntityPersons = persons;

    addedPersons.clear();
    removedPersons.clear();

    updatePersonsSetOnEntityPreview();
  }

  protected void updatePersonsSetOnEntityPreview() {
    pnSelectedPersonsPreview.getChildren().clear();

    for(PersonRole role : new TreeSet<>(editedEntityPersons.keySet())) {
      addPersonRolePreviewPane(role);
    }
  }

  protected void addPersonRolePreviewPane(PersonRole role) {
    HBox rolePreviewPane = new HBox();
    rolePreviewPane.setAlignment(Pos.CENTER_LEFT);
    if(role != PersonRole.getWithoutRolePersonRole())
      pnSelectedPersonsPreview.getChildren().add(rolePreviewPane);
    else
      pnSelectedPersonsPreview.getChildren().add(0, rolePreviewPane);

    if(role != PersonRole.getWithoutRolePersonRole()) {
      Label roleLabel = new Label(role.getName());
      HBox.setMargin(roleLabel, new Insets(0, 6, 0, 0));
      rolePreviewPane.getChildren().add(roleLabel);
    }

    FlowPane roleFlowPane = new FlowPane(Orientation.HORIZONTAL);
    roleFlowPane.setRowValignment(VPos.CENTER);
    roleFlowPane.setColumnHalignment(HPos.LEFT);
    roleFlowPane.setVgap(6);
    roleFlowPane.setMaxWidth(Double.MAX_VALUE);
    roleFlowPane.setUserData(role);
    HBox.setHgrow(roleFlowPane, Priority.ALWAYS);
    rolePreviewPane.getChildren().add(roleFlowPane);

    for(Person person : new TreeSet<>(editedEntityPersons.get(role))) {
      roleFlowPane.getChildren().add(createPersonInRolePreviewLabel(role, person));
    }
  }

  protected PersonLabel createPersonInRolePreviewLabel(PersonRole role, Person person) {
    PersonLabel label = new PersonLabel(role, person);
    label.setOnButtonRemoveItemFromCollectionEventHandler((event) -> removePersonFromEntity(role, person));
    return label;
  }

  protected void addPersonToEntity(PersonRole role, Person person) {
    if(removedPersons.containsKey(role) && removedPersons.get(role).contains(person)) {
      removedPersons.get(role).remove(person);
      if (removedPersons.get(role).size() == 0)
        removedPersons.remove(role);
    }
    else {
      if (addedPersons.containsKey(role) == false)
        addedPersons.put(role, new HashSet<Person>());
      addedPersons.get(role).add(person);
    }

    if (editedEntityPersons.containsKey(role) == false)
      editedEntityPersons.put(role, new HashSet<Person>());
    editedEntityPersons.get(role).add(person);

    updatePersonsSetOnEntityPreview();
    firePersonAddedEvent(role, person);

  }

  protected void removePersonFromEntity(PersonRole role, Person person) {
    if(addedPersons.containsKey(role) && addedPersons.get(role).contains(person)) {
      addedPersons.get(role).remove(person);
      if (addedPersons.get(role).size() == 0)
        addedPersons.remove(role);
    }
    else {

      if(removedPersons.containsKey(role) == false)
        removedPersons.put(role, new HashSet<Person>());
      removedPersons.get(role).add(person);
    }

    if(editedEntityPersons.containsKey(role)) { // should actually never be false
      editedEntityPersons.get(role).remove(person);

      if (editedEntityPersons.get(role).size() == 0)
        editedEntityPersons.remove(role);
    }

    updatePersonsSetOnEntityPreview();
    firePersonRemovedEvent(role, person);
  }

  protected void filterPersons() {
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


  protected void firePersonAddedEvent(PersonRole role, Person person) {
    if(personAddedEventHandler != null)
      personAddedEventHandler.handle(new PersonsControlPersonsEditedEvent(this, role, person));
  }

  protected void firePersonRemovedEvent(PersonRole role, Person person) {
    if(personRemovedEventHandler != null)
      personRemovedEventHandler.handle(new PersonsControlPersonsEditedEvent(this, role, person));
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


  public Map<PersonRole, Set<Person>> getEditedEntityPersons() {
    return editedEntityPersons;
  }

  public Map<PersonRole, Set<Person>> getRemovedPersons() {
    return removedPersons;
  }

  public Map<PersonRole, Set<Person>> getAddedPersons() {
    return addedPersons;
  }

}
