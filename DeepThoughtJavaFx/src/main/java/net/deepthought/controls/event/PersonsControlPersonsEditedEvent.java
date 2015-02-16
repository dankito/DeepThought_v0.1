package net.deepthought.controls.event;

import net.deepthought.controls.person.PersonsControl;
import net.deepthought.data.model.Person;
import net.deepthought.data.model.enums.PersonRole;

import javafx.event.Event;
import javafx.event.EventType;

/**
 * Created by ganymed on 30/11/14.
 */
public class PersonsControlPersonsEditedEvent extends Event {

  protected PersonsControl control;

  protected PersonRole role;
  protected Person person;

  public PersonsControlPersonsEditedEvent(PersonsControl control, PersonRole role, Person person) {
    super(control, control, EventType.ROOT);

    this.control = control;

    this.role = role;
    this.person = person;
  }


  public PersonsControl getControl() {
    return control;
  }

  public PersonRole getRole() {
    return role;
  }

  public Person getPerson() {
    return person;
  }

}
