package net.dankito.deepthought.adapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import net.dankito.deepthought.Application;
import net.dankito.deepthought.R;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by ganymed on 20/09/16.
 */
public abstract class AsyncLoadingAdapter extends BaseAdapter {

  protected Activity context;

  protected int listItemLayoutId;

  protected Map<Integer, Object> loadedItems = new ConcurrentHashMap<>();


  public AsyncLoadingAdapter(Activity context, int listItemLayoutId) {
    this.context = context;
    this.listItemLayoutId = listItemLayoutId;
  }


  @Override
  public Object getItem(int position) {
    Object loadedItem = loadedItems.get(position);

    if(loadedItem == null) {

    }
    return null;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    if(convertView == null) {
      convertView = context.getLayoutInflater().inflate(listItemLayoutId, parent, false);
    }

    Object loadedItem = loadedItems.get(position);

    if(loadedItem != null) {
      customizeViewForItem(convertView, loadedItem);
    }
    else {
      showPlaceholderView(convertView);
      loadItemAsync(position, convertView);
    }

    return convertView;
  }

  protected abstract void customizeViewForItem(View listItemView, Object item);

  protected abstract void showPlaceholderView(View listItemView);

  protected void loadItemAsync(final int position, final View listItemView) {
    listItemView.setTag(R.id.ASYNC_ITEM_TO_LOAD_POSITION, position);

    Application.getThreadPool().runTaskAsync(new Runnable() {
      @Override
      public void run() {
        final Object loadedItem = loadItemInBackgroundThread(position);
        itemForListItemLoaded(loadedItem, position, listItemView);
      }
    });
  }

  protected void itemForListItemLoaded(final Object loadedItem, int position, final View listItemView) {
    if(loadedItem == null) {
      return; // TODO: what to do in this case?
    }

    loadedItems.put(position, loadedItem);

    int currentItemPosition = (int)listItemView.getTag(R.id.ASYNC_ITEM_TO_LOAD_POSITION); // if list item should show another item in the mean time, tag for ITEM_TO_LOAD_POSITION_KEY has changed
    if(currentItemPosition == position) {
      context.runOnUiThread(new Runnable() {
        @Override
        public void run() {
          customizeViewForItem(listItemView, loadedItem);
        }
      });
    }
  }

  protected Object loadItemInBackgroundThread(int position) {
    return getItem(position);
  }


  protected <T> List<T> getListFromCollection(Collection<T> collection) {
    if(collection == null) {
      return null;
    }
    else if(collection instanceof List) {
      return  (List<T>)collection;
    }
    else {
      return new ArrayList<T>(collection); // TODO: use lazy loading list
    }
  }


  @Override
  public void notifyDataSetInvalidated() {
    loadedItems.clear();

    super.notifyDataSetInvalidated();
  }

  @Override
  public void notifyDataSetChanged() {
    loadedItems.clear();

    super.notifyDataSetChanged();
  }

  public void notifyDataSetChangedThreadSafe() {
    context.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        notifyDataSetChanged();
      }
    });
  }

}
