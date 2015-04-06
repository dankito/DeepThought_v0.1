package net.deepthought.data.search;

/**
 * Created by ganymed on 01/04/15.
 */
public enum FieldName {

  Id("id"),
  Abstract("abstract"),
  Content("content"),
  Tags("tags"),
  NoTags("no_tags"),
  ;

  protected String name;

  FieldName(String name) {
    this.name = name;
  }


  @Override
  public String toString() {
    return this.name;
  }

}
