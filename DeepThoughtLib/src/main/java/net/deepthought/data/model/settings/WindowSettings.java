package net.deepthought.data.model.settings;

/**
 * Created by ganymed on 28/07/15.
 */
public class WindowSettings {

  protected double x = -1;

  protected double y = -1;

  protected double width = -1;

  protected double height = -1;

  protected Long idOfShownEntity = null;


  public WindowSettings() {

  }

  public WindowSettings(double x, double y, double width, double height) {
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
  }


  public double getX() {
    return x;
  }

  public void setX(double x) {
    this.x = x;
  }

  public double getY() {
    return y;
  }

  public void setY(double y) {
    this.y = y;
  }

  public double getWidth() {
    return width;
  }

  public void setWidth(double width) {
    this.width = width;
  }

  public double getHeight() {
    return height;
  }

  public void setHeight(double height) {
    this.height = height;
  }


  @Override
  public String toString() {
    return "(" + x + ", " + y + "), (" + width + ", " + height + ")";
  }

}
