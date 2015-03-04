package net.deepthought.data.model.settings.enums;

/**
 * Created by ganymed on 15/12/14.
 */
public enum SelectedTab {

  Categories(0),
  Tags(1),
  Search(2),
  Lists(3),
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
      case Categories:
        return "Categories";
      case Tags:
        return "Tags";
      case Search:
        return "Search";
      case Lists:
        return "Lists";
      default:
        return "Unknown";
    }
  }


  public static SelectedTab fromOrdinal(int ordinal) {
    for(SelectedTab tab : SelectedTab.values()) {
      if(((Integer)tab.value).equals(ordinal))
        return tab;
    }

    return Unknown;
  }

}
