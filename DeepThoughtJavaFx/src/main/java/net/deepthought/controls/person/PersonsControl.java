package net.deepthought.controls.person;

import net.deepthought.Application;
import net.deepthought.controls.CollapsiblePane;
import net.deepthought.controls.FXUtils;
import net.deepthought.controls.ICleanableControl;
import net.deepthought.controls.event.PersonsControlPersonsEditedEvent;
import net.deepthought.controls.tag.IEditedEntitiesHolder;
import net.deepthought.data.listener.ApplicationListener;
import net.deepthought.data.model.DeepThought;
import net.deepthought.data.model.Entry;
import net.deepthought.data.model.Person;
import net.deepthought.data.model.listener.EntityListener;
import net.deepthought.data.persistence.db.BaseEntity;
import net.deepthought.util.JavaFxLocalization;
import net.deepthought.util.Notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

/**
 * Created by ganymed on 01/02/15.
 */
public abstract class PersonsControl extends CollapsiblePane implements IEditedEntitiesHolder<Person>, ICleanableControl {

  protected final static Logger log = LoggerFactory.getLogger(PersonsControl.class);


  protected Entry entry = null;

  protected DeepThought deepThought = null;

  protected Set<Person> currentlySetPersonsOnEntity = new HashSet<>();
  protected Set<Person> addedPersons = new HashSet<>();
  protected Set<Person> removedPersons = new HashSet<>();
  protected ObservableSet<Person> editedEntityPersons = FXCollections.observableSet();

  protected EventHandler<PersonsControlPersonsEditedEvent> personAddedEventHandler = null;
  protected EventHandler<PersonsControlPersonsEditedEvent> personRemovedEventHandler = null;


  protected FlowPane pnSelectedPersonsPreview;

  protected SearchAndSelectPersonsControl searchAndSelectPersonsControl = null;


  public PersonsControl() {
    deepThought = Application.getDeepThought();

    Application.addApplicationListener(applicationListener);

    setupControl();

    if(deepThought != null)
      deepThought.addEntityListener(deepThoughtListener);
  }

  protected ApplicationListener applicationListener = new ApplicationListener() {
    @Override
    public void deepThoughtChanged(DeepThought deepThought) {
      PersonsControl.this.deepThoughtChanged(deepThought);
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

    personAddedEventHandler = null;
    personRemovedEventHandler = null;
  }

  protected void deepThoughtChanged(DeepThought newDeepThought) {
    if(this.deepThought != null)
      this.deepThought.removeEntityListener(deepThoughtListener);

    this.deepThought = newDeepThought;

    if(newDeepThought != null) {
      newDeepThought.addEntityListener(deepThoughtListener);
    }
  }

  protected void setupControl() {
    setMinHeight(22);

    setupTitle();
    this.setExpanded(false);

    searchAndSelectPersonsControl = new SearchAndSelectPersonsControl(this);
//    searchAndSelectPersonsControl.setPrefHeight(250);
//    searchAndSelectPersonsControl.setMaxHeight(200);
    searchAndSelectPersonsControl.setMaxHeight(Double.MAX_VALUE);
    this.setContent(searchAndSelectPersonsControl);
  }

  protected void setupTitle() {
    HBox titlePane = new HBox();
    titlePane.setAlignment(Pos.CENTER_LEFT);
//    titlePane.setMinHeight(22);
    titlePane.setMaxHeight(Double.MAX_VALUE);
    titlePane.setMaxWidth(Double.MAX_VALUE);

    Label lblPersons = new Label();
    JavaFxLocalization.bindLabeledText(lblPersons, "persons");
    lblPersons.setPrefWidth(USE_COMPUTED_SIZE);
    lblPersons.setMinWidth(USE_PREF_SIZE);
    lblPersons.setMaxWidth(USE_PREF_SIZE);
    titlePane.getChildren().add(lblPersons);
    HBox.setMargin(lblPersons, new Insets(0, 6, 0, 0));

    pnSelectedPersonsPreview = new FlowPane();
    pnSelectedPersonsPreview.setMaxWidth(Double.MAX_VALUE);
    pnSelectedPersonsPreview.setVgap(2);
    pnSelectedPersonsPreview.setAlignment(Pos.CENTER_LEFT);
    titlePane.getChildren().add(pnSelectedPersonsPreview);
    HBox.setHgrow(pnSelectedPersonsPreview, Priority.ALWAYS);

    setTitle(titlePane);
  }

  protected void setEntityPersons(Set<Person> persons) {
    currentlySetPersonsOnEntity = persons;

    if(persons instanceof ObservableSet)
      editedEntityPersons = (ObservableSet<Person>)persons;
    else
      editedEntityPersons = FXCollections.observableSet(persons);

    addedPersons.clear();
    removedPersons.clear();

    updatePersonsSetOnEntityPreview();
  }

  protected void updatePersonsSetOnEntityPreview() {
    FXUtils.cleanUpChildrenAndClearPane(pnSelectedPersonsPreview);

    for(Person person : new TreeSet<>(editedEntityPersons)) {
      pnSelectedPersonsPreview.getChildren().add(createPersonPreviewLabel(person));
    }
  }

  protected PersonLabel createPersonPreviewLabel(Person person) {
    PersonLabel label = new PersonLabel(person);
    label.setOnButtonRemoveItemFromCollectionEventHandler((event) -> removeEntityFromEntry(person));
    return label;
  }

  public ObservableSet<Person> getEditedEntities() {
    return editedEntityPersons;
  }

  public boolean containsEditedEntity(Person person) {
    return editedEntityPersons.contains(person);
  }

  public void addEntityToEntry(Person person) {
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

  public void removeEntityFromEntry(Person person) {
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



  protected EntityListener deepThoughtListener = new EntityListener() {
    @Override
    public void propertyChanged(BaseEntity entity, String propertyName, Object previousValue, Object newValue) {

    }

    @Override
    public void entityAddedToCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity addedEntity) {

    }

    @Override
    public void entityOfCollectionUpdated(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity updatedEntity) {
      if(collection == deepThought.getPersons()) {
        updatePersonsSetOnEntityPreview();
      }
    }

    @Override
    public void entityRemovedFromCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity removedEntity) {

    }
  };


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
