package net.dankito.deepthought.ui.model;

import net.dankito.deepthought.data.model.Person;

import java.util.Set;

/**
 * Created by ganymed on 22/09/16.
 */
public class PersonPreviewService {

  /**
   * Returns the last name of the first person.
   * @param persons
   * @return
   */
  public String getShortPersonsPreview(Set<Person> persons) {
    String preview = "";

    if(persons.size() > 0) {
      Person[] personsArray = persons.toArray(new Person[persons.size()]);
      preview = personsArray[0].getLastName();
    }

    return preview;
  }

  public String getLongPersonsPreview(Set<Person> persons) {
    String preview = "";

    for(Person person : persons) {
      preview += person.getNameRepresentation() + "; ";
    }

    if(preview.length() > 2) {
      preview = preview.substring(0, preview.length() - "; ".length()); // remove last "; "
    }

    return preview;
  }

}
