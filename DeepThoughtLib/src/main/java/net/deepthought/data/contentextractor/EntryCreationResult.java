package net.deepthought.data.contentextractor;

import net.deepthought.data.model.Entry;
import net.deepthought.util.DeepThoughtError;

/**
 * Created by ganymed on 24/04/15.
 */
public class EntryCreationResult {

  protected Object source;

  protected boolean successful = false;

  protected DeepThoughtError error = null;

  protected Entry createdEntry = null;


  public EntryCreationResult(Object source, DeepThoughtError error) {
    this.source = source;
    this.successful = false;
    this.error = error;
  }

  public EntryCreationResult(Object source, Entry createdEntry) {
    this.source = source;
    this.successful = true;
    this.createdEntry = createdEntry;
  }


  public Object getSource() {
    return source;
  }

  public boolean successful() {
    return successful;
  }

  public DeepThoughtError getError() {
    return error;
  }

  public Entry getCreatedEntry() {
    return createdEntry;
  }


  @Override
  public String toString() {
    String description = source + " Successful? " + successful + "; ";
    if(successful)
      description += createdEntry;
    else
      description += error;

    return description;
  }

}
