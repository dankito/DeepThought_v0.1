package net.dankito.deepthought.adapter.model;

import android.view.View;

/**
 * Created by ganymed on 21/10/16.
 */
public class LoadItemQueueItem {

  protected int position;

  protected View listItemView;


  public LoadItemQueueItem(int position, View listItemView) {
    this.position = position;
    this.listItemView = listItemView;
  }


  public int getPosition() {
    return position;
  }

  public View getListItemView() {
    return listItemView;
  }

}
