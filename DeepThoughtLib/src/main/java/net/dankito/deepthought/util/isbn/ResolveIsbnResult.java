package net.dankito.deepthought.util.isbn;

import net.dankito.deepthought.data.model.ReferenceBase;

/**
 * Created by ganymed on 05/12/15.
 */
public class ResolveIsbnResult {

  protected boolean successful;

  protected Exception error;

  protected ReferenceBase resolvedReference;


  public ResolveIsbnResult(boolean successful) {
    this.successful = successful;
  }

  public ResolveIsbnResult(Exception error) {
    this(false);
    this.error = error;
  }

  public ResolveIsbnResult(ReferenceBase resolvedReference) {
    this(resolvedReference != null);
    this.resolvedReference = resolvedReference;
  }


  public boolean isSuccessful() {
    return successful;
  }

  public Exception getError() {
    return error;
  }

  public ReferenceBase getResolvedReference() {
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
