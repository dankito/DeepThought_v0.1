package net.dankito.deepthought.adapter;

import android.app.Activity;
import android.support.v7.widget.PopupMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckedTextView;

import net.dankito.deepthought.Application;
import net.dankito.deepthought.R;
import net.dankito.deepthought.activities.ActivityManager;
import net.dankito.deepthought.data.model.DeepThought;
import net.dankito.deepthought.data.model.Entry;
import net.dankito.deepthought.data.model.Tag;
import net.dankito.deepthought.data.model.listener.EntityListener;
import net.dankito.deepthought.data.persistence.db.BaseEntity;
import net.dankito.deepthought.data.search.ui.TagSearchResultState;
import net.dankito.deepthought.data.search.ui.TagsSearchResultListener;
import net.dankito.deepthought.data.search.ui.TagsSearcher;
import net.dankito.deepthought.data.search.ui.TagsSearcherButtonState;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by ganymed on 12/10/14.
 */
public class EntryTagsAdapter extends AsyncLoadingEntityAdapter {

  public interface EntryTagsAdapterListener {
    void entryTagsChanged(List<Tag> entryTags);
    void tagsSearchDone(TagsSearcherButtonState buttonState);
  }

  protected Entry entry;
  protected List<Tag> entryTags;

  protected List<Tag> searchTagsResult;

  protected TagsSearcher tagsSearcher;

  protected EntryTagsAdapterListener entryTagsAdapterListener = null;


  public EntryTagsAdapter(Activity context, Entry entry, List<Tag> entryTags) {
    super(context, R.layout.list_item_entry_tag);
    this.entry = entry;
    this.entryTags = entryTags;

    tagsSearcher = new TagsSearcher(Application.getSearchEngine());
    searchTagsResult = new ArrayList<>();

    entry.addEntityListener(entryListener);

    searchTags("");
  }

  public EntryTagsAdapter(Activity context, Entry entry, List<Tag> entryTags, EntryTagsAdapterListener entryTagsAdapterListener) {
    this(context, entry, entryTags);
    this.entryTagsAdapterListener = entryTagsAdapterListener;
  }

  @Override
  public void cleanUp() {
    entry.removeEntityListener(entryListener);

    super.cleanUp();
  }

  @Override
  public int getCount() {
    return searchTagsResult.size();
  }

  public Tag getTagAt(int position) {
    return searchTagsResult.get(position);
  }

  @Override
  public Object getItem(int position) {
    return getTagAt(position);
  }

  @Override
  public long getItemId(int position) {
    return position;
  }


  @Override
  protected void customizeViewForItem(View listItemView, Object item) {
    Tag tag = (Tag)item;

    CheckedTextView chktxtvwListItemEntryTag = (CheckedTextView)listItemView.findViewById(R.id.chktxtvwListItemEntryTag);

    chktxtvwListItemEntryTag.setText(tag.getName());
    chktxtvwListItemEntryTag.setChecked(entryTags.contains(tag));
    chktxtvwListItemEntryTag.setOnClickListener(chktxtvwListItemEntryTagOnClickListener);
    chktxtvwListItemEntryTag.setOnLongClickListener(chktxtvwListItemEntryTagOnLongClickListener);
    chktxtvwListItemEntryTag.setTag(tag);

    setBackgroundColorForTag(tag, listItemView);
  }

  @Override
  protected void showPlaceholderView(View listItemView) {
    CheckedTextView chktxtvwListItemEntryTag = (CheckedTextView)listItemView.findViewById(R.id.chktxtvwListItemEntryTag);

    chktxtvwListItemEntryTag.setText("");
    chktxtvwListItemEntryTag.setChecked(false);
    chktxtvwListItemEntryTag.setOnClickListener(null);
    chktxtvwListItemEntryTag.setOnLongClickListener(null);
    chktxtvwListItemEntryTag.setTag(null);

    setBackgroundColorForTagState(TagSearchResultState.DEFAULT, listItemView);
  }

  protected void setBackgroundColorForTag(Tag tag, View itemView) {
    TagSearchResultState state = tagsSearcher.getTagSearchResultState(tag);

    setBackgroundColorForTagState(state, itemView);
  }

  protected void setBackgroundColorForTagState(TagSearchResultState state, View itemView) {
    int colorResource = getBackgroundColorForTagState(state);

    itemView.setBackgroundResource(colorResource);
  }

  protected int getBackgroundColorForTagState(TagSearchResultState state) {
    switch(state) {
      case EXACT_OR_SINGLE_MATCH_BUT_NOT_OF_LAST_RESULT:
        return R.color.tagStateExactOrSingleMatchButNotOfLastResult;
      case MATCH_BUT_NOT_OF_LAST_RESULT:
        return R.color.tagStateMatchButNotOfLastResult;
      case EXACT_MATCH_OF_LAST_RESULT:
        return R.color.tagStateExactMatchOfLastResult;
      case SINGLE_MATCH_OF_LAST_RESULT:
        return R.color.tagStateSingleMatchOfLastResult;
    }

    return R.color.tagStateDefault;
  }


  View.OnClickListener chktxtvwListItemEntryTagOnClickListener = new View.OnClickListener() {
    @Override
    public void onClick(View view) {
      CheckedTextView chktxtvwListItemEntryTag = (CheckedTextView)view;
      Tag tag = (Tag)chktxtvwListItemEntryTag.getTag();

      chktxtvwListItemEntryTag.toggle();

      if(chktxtvwListItemEntryTag.isChecked()) {
        entryTags.add(tag);
        Collections.sort(entryTags);
      }
      else {
        entryTags.remove(tag);
      }

      callEntryTagsChangedListener();
    }
  };

  View.OnLongClickListener chktxtvwListItemEntryTagOnLongClickListener = new View.OnLongClickListener() {
    @Override
    public boolean onLongClick(View view) {
      showContextMenuPopup(view);
      return true;
    }
  };

  protected void showContextMenuPopup(final View view) {
    PopupMenu popup = new PopupMenu(context, view);

    MenuInflater inflater = popup.getMenuInflater();
    inflater.inflate(R.menu.list_item_tag_context_menu, popup.getMenu());

    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
      @Override
      public boolean onMenuItemClick(MenuItem item) {
        return contextMenuItemClicked(item, (Tag)view.getTag());
      }
    });

    popup.show();
  }

  protected boolean contextMenuItemClicked(MenuItem menuItem, Tag tag) {
    switch(menuItem.getItemId()) {
      case R.id.list_item_tag_context_menu_edit:
        editTag(tag);
        return true;
      case R.id.list_item_tag_context_menu_delete:
        DeepThought deepThought = Application.getDeepThought();
        deepThought.removeTag(tag);
        return true;
      default:
        return false;
    }
  }

  protected void editTag(Tag tagToEdit) {
    ActivityManager.getInstance().showEditTagAlert(context, tagToEdit);
  }


  public void searchTags(String searchTerm) {
    tagsSearcher.search(searchTerm, searchResultListener);
  }

  protected TagsSearchResultListener searchResultListener = new TagsSearchResultListener() {
    @Override
    public void completed(final List<Tag> searchResult) {
      context.runOnUiThread(new Runnable() {
        @Override
        public void run() {
          searchingTagsDone(searchResult);
        }
      });
    }
  };

  protected void searchingTagsDone(List<Tag> searchResult) {
    searchTagsResult = searchResult;

    notifyDataSetChanged();

    callTagsSearchDoneListeners(searchResult);
  }


  public void toggleTags() {
    for(Tag foundTag : searchTagsResult) {
      if(entryTags.contains(foundTag)) {
        entryTags.remove(foundTag);
      }
      else {
        entryTags.add(foundTag);
      }
    }

    notifyDataSetChanged();

    callEntryTagsChangedListener();
  }


  protected void callEntryTagsChangedListener() {
    if(entryTagsAdapterListener != null) {
      entryTagsAdapterListener.entryTagsChanged(entryTags);
    }
  }

  protected void callTagsSearchDoneListeners(List<Tag> searchResult) {
    if(entryTagsAdapterListener != null) {
      entryTagsAdapterListener.tagsSearchDone(tagsSearcher.getButtonStateForSearchResult());
    }
  }


  @Override
  protected void checkIfRelevantEntityHasChanged(BaseEntity entity) {
    if(entity instanceof Tag) {
      tagsSearcher.researchTagsWithLastSearchTerm(searchResultListener);

      Tag tag = (Tag)entity;

      if(entryTags.contains(tag)) {
        if(tag.isDeleted()) {
          entryTags.remove(tag);
        }

        callEntryTagsChangedListener();
      }
    }
  }


  protected EntityListener entryListener = new EntityListener() {
    @Override
    public void propertyChanged(BaseEntity entity, String propertyName, Object previousValue, Object newValue) {
      notifyDataSetChangedThreadSafe();
    }

    @Override
    public void entityAddedToCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity addedEntity) {

    }

    @Override
    public void entityOfCollectionUpdated(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity updatedEntity) {

    }

    @Override
    public void entityRemovedFromCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity removedEntity) {

    }
  };


}
