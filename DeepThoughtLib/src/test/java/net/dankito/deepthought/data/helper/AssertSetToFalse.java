package net.dankito.deepthought.data.helper;

/**
 * Created by ganymed on 01/01/15.
 */
public class AssertSetToFalse {

  protected boolean value = true;


  public AssertSetToFalse() {

  }

  public AssertSetToFalse(boolean initialValue) {
    this.value = initialValue;
  }

  public boolean isSetToFalse() {
    return value == false;
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
