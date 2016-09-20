package net.dankito.deepthought.data.listener;

/**
 * Created by ganymed on 20/09/16.
 */
public interface IEntityChangesService {

  boolean addAllEntitiesListener(AllEntitiesListener listener);
  boolean removeAllEntitiesListener(AllEntitiesListener listener);

}
