package net.dankito.deepthought.adapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import net.dankito.deepthought.R;
import net.dankito.deepthought.data.model.Entry;
import net.dankito.deepthought.dialogs.enums.EditEntrySection;

/**
 * Created by ganymed on 12/09/16.
 */
public class EntrySectionsSpinnerAdapter extends BaseAdapter {

  protected Entry entry;

  protected Activity context;

  protected boolean shouldShowAbstractSection = false;


  public EntrySectionsSpinnerAdapter(Activity context, Entry entry) {
    this.context = context;
    this.entry = entry;
  }


  public boolean showsAbstractSection() {
    return shouldShowAbstractSection || entry.hasAbstract();
  }

  @Override
  public int getCount() {
    int count = 2;

    if(showsAbstractSection()) {
      count++;
    }

    return count;
  }

  @Override
  public Object getItem(int index) {
    return getSectionAtIndex(index);
  }

  public EditEntrySection getSectionAtIndex(int index) {
    if(showsAbstractSection()) {
      switch(index) {
        case 0:
          return EditEntrySection.Abstract;
        case 2:
          return EditEntrySection.Tags;
      }
    }
    else {
      if(index == 1) {
        return EditEntrySection.Tags;
      }
    }

    return EditEntrySection.Content;
  }

  public int getIndexForSection(EditEntrySection section) {
    if(showsAbstractSection()) {
      switch(section) {
        case Abstract:
          return 0;
        case Content:
          return 1;
        case Tags:
          return 2;
      }
    }
    else {
      switch(section) {
        case Content:
          return 0;
        case Tags:
          return 1;
      }
    }

    return -1;
  }

  @Override
  public long getItemId(int index) {
    return index;
  }

  @Override
  public View getView(int index, View convertView, ViewGroup viewGroup) {
    if(convertView == null) {
      convertView = context.getLayoutInflater().inflate(R.layout.spinner_item_entry_section, viewGroup, false);
    }

    EditEntrySection section = getSectionAtIndex(index);

    ImageView imgvwEntrySectionIcon = (ImageView)convertView.findViewById(R.id.imgvwEntrySectionIcon);
    imgvwEntrySectionIcon.setImageResource(getIconForSection(section));

    TextView txtwvEntrySectionName = (TextView)convertView.findViewById(R.id.txtwvEntrySectionName);
    txtwvEntrySectionName.setText(getNameForSection(section));

    return convertView;
  }

  protected int getIconForSection(EditEntrySection section) {
    switch(section) {
      case Abstract:
        return R.drawable.ic_abstract_white;
      case Content:
        return R.drawable.ic_content_white;
      case Tags:
        return R.drawable.ic_tag_white;
    }

    return 0;
  }

  protected String getNameForSection(EditEntrySection section) {
    switch(section) {
      case Abstract:
        return context.getString(R.string.edit_entry_section_abstract);
      case Content:
        return context.getString(R.string.edit_entry_section_content);
      case Tags:
        return context.getString(R.string.edit_entry_section_tags);
    }

    return "";
  }


  public void setShouldShowAbstractSection(boolean shouldShowAbstractSection) {
    this.shouldShowAbstractSection = shouldShowAbstractSection;

    notifyDataSetChanged();
  }

}
