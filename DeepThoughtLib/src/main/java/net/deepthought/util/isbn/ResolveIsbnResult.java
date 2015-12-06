package net.deepthought.util.isbn;

import net.deepthought.data.model.Reference;

/**
 * Created by ganymed on 05/12/15.
 */
public class ResolveIsbnResult {

  protected boolean successful;

  protected Exception error;

  protected Reference resolvedReference;


  public ResolveIsbnResult(Exception error) {
    this.successful = false;
    this.error = error;
  }

  public ResolveIsbnResult(Reference resolvedReference) {
    this.successful = resolvedReference != null;
    this.resolvedReference = resolvedReference;
  }


  public boolean isSuccessful() {
    return successful;
  }

  public Exception getError() {
    return error;
  }

  public Reference getResolvedReference() {
    return resolvedReference;
  }


  @Override
  public String toString() {
    String description = "Successful? " + successful + ": ";

    if(successful) {
      description += resolvedReference;
    }
    else {
      description += error;
    }

    return description;
  }
}
