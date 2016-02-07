package net.deepthought.controls.event;

import net.deepthought.controls.person.PersonsControl;
import net.deepthought.data.model.Person;

import javafx.event.Event;
import javafx.event.EventType;

/**
 * Created by ganymed on 30/11/14.
 */
public class PersonsControlPersonsEditedEvent extends Event {

  protected PersonsControl control;

  protected Person person;

  public PersonsControlPersonsEditedEvent(PersonsControl control, Person person) {
    super(control, control, EventType.ROOT);

    this.control = control;

    this.person = person;
  }


  public PersonsControl getControl() {
    return control;
  }

  public Person getPerson() {
    return person;
  }

}
