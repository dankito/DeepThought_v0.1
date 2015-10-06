package net.deepthought.communication.messages;

/**
 * Created by ganymed on 05/10/15.
 */
public class MultipartPart<T> {

  protected String partName;

  protected MultipartType type;

  protected T data;


  public MultipartPart(String partName, MultipartType type, T data) {
    this.partName = partName;
    this.type = type;
    this.data = data;
  }


  public String getPartName() {
    return partName;
  }

  public MultipartType getType() {
    return type;
  }

  public T getData() {
    return data;
  }


  @Override
  public String toString() {
    return partName + ": " + type;
  }

}
