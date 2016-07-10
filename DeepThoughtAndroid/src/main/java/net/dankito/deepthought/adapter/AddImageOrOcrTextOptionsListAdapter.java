package net.dankito.deepthought.adapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import net.dankito.deepthought.Application;
import net.dankito.deepthought.R;

/**
 * Created by ganymed on 10/07/16.
 */
public class AddImageOrOcrTextOptionsListAdapter extends BaseAdapter {

  protected String[] options;

  protected int countOptions;

  protected Activity context;


  public AddImageOrOcrTextOptionsListAdapter(Activity context) {
    this.context = context;

    this.options = context.getResources().getStringArray(R.array.add_image_or_ocr_text_options);

    this.countOptions = Application.getContentExtractorManager().hasOcrContentExtractors() ? options.length : 3; // all options or only that ones to insert image
  }


  @Override
  public int getViewTypeCount() {
    return 2;
  }

  @Override
  public int getItemViewType(int position) {
    if(position == 0 || position == 3) {
      return AdapterItemType.HEADER.ordinal();
    }

    return AdapterItemType.ITEM.ordinal();
  }

  @Override
  public boolean areAllItemsEnabled() {
    return false;
  }

  @Override
  public boolean isEnabled(int position) {
    return getItemViewType(position) == AdapterItemType.ITEM.ordinal();
  }

  @Override
  public int getCount() {
    return countOptions;
  }

  @Override
  public Object getItem(int index) {
    return options[index];
  }

  @Override
  public long getItemId(int index) {
    return index;
  }

  @Override
  public View getView(int index, View convertView, ViewGroup parent) {
    AdapterItemType type = AdapterItemType.values()[getItemViewType(index)];

    if(convertView == null) {
      if(type == AdapterItemType.HEADER) {
        convertView = context.getLayoutInflater().inflate(R.layout.view_adapter_item_type_header, parent, false);
      }
      else {
        convertView = context.getLayoutInflater().inflate(R.layout.view_adapter_item_type_item, parent, false);
      }
    }

    TextView textView = (TextView)convertView.findViewById(R.id.txtAdapterItemTypeText);
    textView.setText((String)getItem(index));

    return convertView;
  }

}
