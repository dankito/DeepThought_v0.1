package net.deepthought.data.model.enums;

/**
 * Created by ganymed on 15/12/14.
 */
public enum SelectedTab {

  Tags(0),
  Categories(1),
  AdvancedSearch(2),
  Unknown(-1);


  int value;

  SelectedTab(int value) {
    this.value = value;
  }


  public int getValue() {
    return value;
  }


  @Override
  public String toString() {
    switch(this) {
      case Tags:
        return "Tags";
      case Categories:
        return "Categories";
      case AdvancedSearch:
        return "AdvancedSearch";
      default:
        return "Unknown";
    }
  }


  public static SelectedTab fromOrdinal(int ordinal) {
    switch(ordinal) {
      case 0:
        return Tags;
      case 1:
        return Categories;
      case 2:
        return AdvancedSearch;
      default:
        return Unknown;
    }
  }

}
