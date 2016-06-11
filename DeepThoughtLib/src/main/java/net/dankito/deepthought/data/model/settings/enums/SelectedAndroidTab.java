package net.dankito.deepthought.data.model.settings.enums;

/**
 * <p>
 *  I really don't know how to solve this as the Android Application got some more / other tabs then the JavaFX Application.
 *  So i introduced a second Enumeration (it's not possible in Android to derive from an Enumeration) to keep track of Android specific selected tabs.
 *  If the selected tab also exists in this JavaFX application {@link SelectedTab} value will be set as well.
 * </p>
 * Created by ganymed on 15/12/14.
 */
public enum SelectedAndroidTab {

  Categories(0),
  Tags(1),
  Search(2),
  Lists(3),
  EntriesOverview(100),
  Unknown(-1);


  int value;

  SelectedAndroidTab(int value) {
    this.value = value;
  }


  @Override
  public String toString() {
    switch(this) {
      case Categories:
        return "Categories";
      case Tags:
        return "Tags";
      case Search:
        return "Search";
      case Lists:
        return "Lists";
      case EntriesOverview:
        return "EntriesOverview";
      default:
        return "Unknown";
    }
  }


  public static SelectedAndroidTab fromOrdinal(int ordinal) {
    for(SelectedAndroidTab tab : SelectedAndroidTab.values()) {
      if(((Integer)tab.value).equals(ordinal))
        return tab;
    }

    return Unknown;
  }

}
