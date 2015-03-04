package net.deepthought.persistence.hibernate;

import org.hibernate.event.spi.PostLoadEvent;
import org.hibernate.event.spi.PostLoadEventListener;

/**
* Created by ganymed on 30/11/14.
*/
public class LoadEventListener implements PostLoadEventListener {

  @Override
  public void onPostLoad(PostLoadEvent event) {
//    if(Application.getDataManager() != null)
//      Application.getDataManager().lazyLoadedEntityMapped((BaseEntity)event.getEntity());
//    if(Application.getDeepThought() != null)
//      Application.getDeepThought().lazyLoadedEntityMapped((BaseEntity)event.getEntity());
  }
}
