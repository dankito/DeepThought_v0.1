package net.deepthought.data.model.enums;

/**
 * <p>
 *  I really don't know how to solve this as the Android Application got some more / other tabs then the JavaFX Application.
 *  So i introduced a second Enumeration (it's not possible in Android to derive from an Enumeration) to keep track of Android specific selected tabs.
 *  If the selected tab also exists in this JavaFX application {@link SelectedTab} value will be set as well.
 * </p>
 * Created by ganymed on 15/12/14.
 */
public enum SelectedAndroidTab {

  Tags(0),
  Categories(1),
  AdvancedSearch(2),
  EntriesOverview(3),
  Unknown(-1);


  int value;

  SelectedAndroidTab(int value) {
    this.value = value;
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
      case EntriesOverview:
        return "EntriesOverview";
      default:
        return "Unknown";
    }
  }


  public static SelectedAndroidTab fromOrdinal(int ordinal) {
    switch(ordinal) {
      case 0:
        return Tags;
      case 1:
        return Categories;
      case 2:
        return AdvancedSearch;
      case 3:
        return EntriesOverview;
      default:
        return Unknown;
    }
  }

}
