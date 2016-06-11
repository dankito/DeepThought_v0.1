package net.dankito.deepthought.data.model.settings;

/**
 * Created by ganymed on 28/07/15.
 */
public class ColumnSettings {

  protected boolean isVisible;

  protected int width;


  public ColumnSettings() {

  }

  public ColumnSettings(boolean isVisible) {
    this();
    this.isVisible = isVisible;
  }

  public ColumnSettings(boolean isVisible, int width) {
    this(isVisible);
    this.width = width;
  }


  public boolean isVisible() {
    return isVisible;
  }

  public void setIsVisible(boolean isVisible) {
    this.isVisible = isVisible;
  }

  public int getWidth() {
    return width;
  }

  public void setWidth(int width) {
    this.width = width;
  }


  @Override
  public String toString() {
    return "Visible? " + isVisible + ", width = " + width;
  }

}
