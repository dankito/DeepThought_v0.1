package net.dankito.deepthought.data.helper;

/**
 * Created by ganymed on 01/01/15.
 */
public class AssertSetToTrue {

  protected boolean value = false;


  public AssertSetToTrue() {

  }

  public AssertSetToTrue(boolean initialValue) {
    this.value = initialValue;
  }


  public boolean isSetToTrue() {
    return value == true;
  }

  public boolean getValue() {
    return value;
  }

  public void setValue(boolean value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return "Is set to: " + value;
  }
}
