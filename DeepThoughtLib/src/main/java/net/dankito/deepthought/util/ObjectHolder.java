package net.dankito.deepthought.util;

/**
 * Created by ganymed on 03/10/15.
 */
public class ObjectHolder<T> {

  protected T object;


  public ObjectHolder() {
    this.object = null;
  }

  public ObjectHolder(T object) {
    this.object = object;
  }


  public T get() {
    return object;
  }

  public void set(T object) {
    this.object = object;
  }


  @Override
  public String toString() {
    return object == null ? "null" : object.toString();
  }

}
