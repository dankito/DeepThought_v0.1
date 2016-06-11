package net.dankito.deepthought.filter;

import android.widget.Filter;

import net.dankito.deepthought.Application;
import net.dankito.deepthought.data.model.Tag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by ganymed on 13/10/14.
 */
public class TagsFilter extends Filter {

  public interface TagsFilterListener {
    public void publishResults(List<Tag> filteredTags);
  }

  protected String filterConstraint = "";

  protected TagsFilterListener filterListener = null;


  public TagsFilter() {

  }

  public TagsFilter(TagsFilterListener listener) {
    this();
    setTagsFilterListener(listener);
  }


  public void setTagsFilterListener(TagsFilterListener listener) {
    this.filterListener = listener;
  }


  @Override
  protected FilterResults performFiltering(CharSequence constraint) {
    filterConstraint = constraint.toString().toLowerCase();
    List<Tag> filteredTags = new ArrayList<>();

    if(Application.getDeepThought() != null) {
      List<Tag> allTags = new ArrayList<>(Application.getDeepThought().getTags());

      if (constraint == null || constraint.length() == 0) {
        filteredTags = allTags;
      } else {
        for (Tag tag : allTags) {
          if (tag.getName().toLowerCase().contains(filterConstraint))
            filteredTags.add(tag);
        }
      }
    }

    FilterResults results = new FilterResults();
    Collections.sort(filteredTags);
    results.values = filteredTags;
    results.count = filteredTags.size();

    return results;
  }

  @Override
  protected void publishResults(CharSequence constraint, FilterResults results) {
    if(filterListener != null)
      if(results != null && results.values instanceof List)
        filterListener.publishResults((List<Tag>)results.values);
  }

  public void reapplyFilter() {
    filter(filterConstraint);
  }
}
