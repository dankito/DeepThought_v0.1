package net.dankito.deepthought.controls.event;

import net.dankito.deepthought.data.model.Person;

import javafx.event.Event;
import javafx.event.EventType;

/**
 * Created by ganymed on 30/11/14.
 */
public class PersonsControlPersonsEditedEvent extends Event {

  protected net.dankito.deepthought.controls.person.PersonsControl control;

  protected Person person;

  public PersonsControlPersonsEditedEvent(net.dankito.deepthought.controls.person.PersonsControl control, Person person) {
    super(control, control, EventType.ROOT);

    this.control = control;

    this.person = person;
  }


  public net.dankito.deepthought.controls.person.PersonsControl getControl() {
    return control;
  }

  public Person getPerson() {
    return person;
  }

}
