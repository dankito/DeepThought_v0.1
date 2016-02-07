package net.deepthought.data.listener;

import net.deepthought.data.model.DeepThought;
import net.deepthought.util.Notification;

/**
 * Created by ganymed on 11/01/15.
 */
public interface ApplicationListener {

  public void deepThoughtChanged(DeepThought deepThought);

  public void notification(Notification notification);

}
