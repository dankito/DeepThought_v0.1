package net.deepthought.adapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import net.deepthought.Application;
import net.deepthought.R;
import net.deepthought.data.listener.ApplicationListener;
import net.deepthought.data.model.DeepThought;
import net.deepthought.data.model.Entry;
import net.deepthought.data.model.listener.EntityListener;
import net.deepthought.data.persistence.db.BaseEntity;
import net.deepthought.util.Notification;

import java.util.Collection;

/**
 * Created by ganymed on 01/10/14.
 */
public class EntriesOverviewAdapter extends BaseAdapter {

  protected DeepThought deepThought;
  protected Activity context;

  public EntriesOverviewAdapter(Activity context) {
    this.context = context;

    // TODO: unregister listeners again to avoid memory leaks
    Application.addApplicationListener(new ApplicationListener() {
      @Override
      public void deepThoughtChanged(DeepThought deepThought) {
        if (EntriesOverviewAdapter.this.deepThought != null)
          EntriesOverviewAdapter.this.deepThought.removeEntityListener(deepThoughtListener);

        EntriesOverviewAdapter.this.deepThought = deepThought;

        if (EntriesOverviewAdapter.this.deepThought != null)
          EntriesOverviewAdapter.this.deepThought.addEntityListener(deepThoughtListener);

        notifyDataSetChangedThreadSafe();
      }

      @Override
      public void notification(Notification notification) {

      }
    });

    deepThought = Application.getDeepThought();
    if(deepThought != null)
      deepThought.addEntityListener(deepThoughtListener);
  }


  @Override
  public int getCount() {
    if(deepThought != null)
      return deepThought.countEntries();
    return 0;
  }

  public Entry getEntryAt(int position) {
    return deepThought.entryAt(position);
  }

  @Override
  public Object getItem(int position) {
    return getEntryAt(position);
  }

  @Override
  public long getItemId(int position) {
    return position;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    if(convertView == null)
      convertView = context.getLayoutInflater().inflate(R.layout.list_item_entry, parent, false);

    Entry entry = getEntryAt(position);

//    TextView txtvwTitle = (TextView)convertView.findViewById(R.id.txtvwListItemEntryTitle);
//    txtvwTitle.setText(entry.getTitle());

    TextView txtvwPreview = (TextView)convertView.findViewById(R.id.txtvwListItemEntryPreview);
    txtvwPreview.setText(entry.getPreview());

    TextView txtvwTags = (TextView)convertView.findViewById(R.id.txtvwListItemEntryTags);
    txtvwTags.setText(entry.getTagsPreview());

    return convertView;
  }


  public void notifyDataSetChangedThreadSafe() {
    context.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        notifyDataSetChanged();
      }
    });
  }


//  @Override
//  public void entryAdded(Entry entry) {
//    notifyDataSetChangedThreadSafe();
//  }
//
//  @Override
//  public void entryUpdated(Entry entry) {
//    notifyDataSetChangedThreadSafe();
//  }
//
//  @Override
//  public void entryRemoved(Entry entry) {
//    notifyDataSetChangedThreadSafe();
//  }

  protected EntityListener deepThoughtListener = new EntityListener() {
    @Override
    public void propertyChanged(BaseEntity entity, String propertyName, Object previousValue, Object newValue) {
      // TODO: how to get when an Entry has been updated?
    }

    @Override
    public void entityAddedToCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity addedEntity) {
      if(collection == deepThought.getEntries())
        notifyDataSetChangedThreadSafe();
    }

    @Override
    public void entityOfCollectionUpdated(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity updatedEntity) {
      if(collection == deepThought.getEntries())
        notifyDataSetChangedThreadSafe();
    }

    @Override
    public void entityRemovedFromCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity removedEntity) {
      if(collection == deepThought.getEntries())
        notifyDataSetChangedThreadSafe();
    }
  };
}
