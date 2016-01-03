package net.deepthought.controls;

import net.deepthought.data.model.DeepThought;

/**
 * Created by ganymed on 16/03/15.
 */
public interface IMainWindowControl {

  void deepThoughtChanged(DeepThought newDeepThought);

  void clearData();

}
