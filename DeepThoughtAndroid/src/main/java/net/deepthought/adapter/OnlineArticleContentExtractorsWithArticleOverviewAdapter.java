package net.deepthought.adapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import net.deepthought.R;
import net.deepthought.data.contentextractor.IOnlineArticleContentExtractor;
import net.deepthought.util.IconManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by ganymed on 25/09/15.
 */
public class OnlineArticleContentExtractorsWithArticleOverviewAdapter extends BaseAdapter {

  private final static Logger log = LoggerFactory.getLogger(OnlineArticleContentExtractorsWithArticleOverviewAdapter.class);


  protected Activity context;

  protected List<IOnlineArticleContentExtractor> extractorsWithArticleOverview;


  public OnlineArticleContentExtractorsWithArticleOverviewAdapter(Activity context, List<IOnlineArticleContentExtractor> extractorsWithArticleOverview) {
    this.context = context;
    this.extractorsWithArticleOverview = extractorsWithArticleOverview;
  }


  @Override
  public int getCount() {
    return extractorsWithArticleOverview.size();
  }

  public IOnlineArticleContentExtractor getExtractorAt(int position) {
    return extractorsWithArticleOverview.get(position);
  }

  @Override
  public Object getItem(int position) {
    return getExtractorAt(position);
  }

  @Override
  public long getItemId(int position) {
    return position;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    if(convertView == null)
      convertView = context.getLayoutInflater().inflate(R.layout.list_item_online_article_content_extractor, parent, false);

    IOnlineArticleContentExtractor extractor = getExtractorAt(position);

    ImageView imgvwExtractorIcon = (ImageView)convertView.findViewById(R.id.imgvwExtractorIcon);
    IconManager.getInstance().setImageViewToImageFromUrl(imgvwExtractorIcon, extractor.getIconUrl());

    TextView txtvwExtractorName = (TextView)convertView.findViewById(R.id.txtvwExtractorName);
    txtvwExtractorName.setText(extractor.getSiteBaseUrl());

    return convertView;
  }
}
