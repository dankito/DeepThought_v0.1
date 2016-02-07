package net.deepthought.data.contentextractor;

/**
 * Created by ganymed on 15/01/15.
 */
public class ImageContentExtractPart extends ContentExtractPart {

  protected int startX;
  protected int startY;

  protected int endX;
  protected int endY;


  public ImageContentExtractPart(int startX, int startY, int endX, int endY) {
    this.startX = startX;
    this.startY = startY;
    this.endX = endX;
    this.endY = endY;
  }


  @Override
  public String toString() {
    return "Start: x: " + startX + ", y: " + startY + ": End: x: " + endX + ", y: " + endY;
  }

}
