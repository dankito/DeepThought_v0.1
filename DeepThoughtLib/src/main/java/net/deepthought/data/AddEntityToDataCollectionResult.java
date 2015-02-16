package net.deepthought.data;

import net.deepthought.data.persistence.db.BaseEntity;
import net.deepthought.util.DeepThoughtError;

/**
 * Created by ganymed on 07/01/15.
 */
public class AddEntityToDataCollectionResult {

  protected BaseEntity entity;

  protected boolean successful;

  protected DeepThoughtError error;


  public AddEntityToDataCollectionResult(BaseEntity entity, DeepThoughtError error) {
    this.entity = entity;
    this.successful = error == DeepThoughtError.Success;
    this.error = error;
  }

  public AddEntityToDataCollectionResult(BaseEntity entity, boolean successful) {
    this.entity = entity;
    this.successful = true;
    this.error = DeepThoughtError.Success;
  }


  public BaseEntity getEntity() {
    return entity;
  }

  public boolean isSuccessful() {
    return successful;
  }

  public DeepThoughtError getError() {
    return error;
  }


  @Override
  public String toString() {
    String description = "Successful? " + successful + "; " + entity;

    if(successful == false)
      description += error;

    return description;
  }

}
