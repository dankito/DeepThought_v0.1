package net.dankito.deepthought.communication.messages;

/**
 * Created by ganymed on 05/10/15.
 */
public class MultipartPart<T> {

  protected String partName;

  protected MultipartType type;

  protected Class dataType;

  protected T data;


  public MultipartPart(String partName, MultipartType type, Class dataType) {
    this.partName = partName;
    this.type = type;
    this.dataType = dataType;
  }

  public MultipartPart(String partName, MultipartType type, T data) {
    this(partName, type, data.getClass());
    this.data = data;
  }


  public String getPartName() {
    return partName;
  }

  public MultipartType getType() {
    return type;
  }

  public Class getDataType() {
    return dataType;
  }

  public T getData() {
    return data;
  }

  public void setData(T data) {
    this.data = data;
  }

  @Override
  public String toString() {
    return partName + ": " + type;
  }

}
