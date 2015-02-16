package net.deepthought.data.contentextractor;

/**
 * Created by ganymed on 15/01/15.
 */
public class MultimediaContentExtractPart extends ContentExtractPart {

  protected int startMillisecond;

  protected int endMillisecond;


  public MultimediaContentExtractPart(int endMillisecond) {
    this(0, endMillisecond);
  }

  public MultimediaContentExtractPart(int startMillisecond, int endMillisecond) {
    this.startMillisecond = startMillisecond;
    this.endMillisecond = endMillisecond;
  }


  @Override
  public String toString() {
    return "Start: " + startMillisecond + ", End: " + endMillisecond;
  }

}
