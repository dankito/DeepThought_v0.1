package net.deepthought.adapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import net.deepthought.R;
import net.deepthought.controls.ICleanUp;
import net.deepthought.data.contentextractor.IOnlineArticleContentExtractor;
import net.deepthought.data.contentextractor.preview.ArticlesOverviewItem;
import net.deepthought.data.contentextractor.preview.ArticlesOverviewListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by ganymed on 25/09/15.
 */
public class ArticlesOverviewAdapter extends BaseAdapter implements ICleanUp {


  protected Activity context;

  protected IOnlineArticleContentExtractor extractorWithArticleOverview;

  protected List<ArticlesOverviewItem> articlesOverviewItems = new ArrayList<>();


  public ArticlesOverviewAdapter(Activity context, IOnlineArticleContentExtractor extractorWithArticleOverview) {
    this.context = context;
    this.extractorWithArticleOverview = extractorWithArticleOverview;

    retrieveArticles();
  }

  public void retrieveArticles() {
    articlesOverviewItems.clear();
    extractorWithArticleOverview.getArticlesOverviewAsync(new ArticlesOverviewListener() {
      @Override
      public void overviewItemsRetrieved(IOnlineArticleContentExtractor contentExtractor, Collection<ArticlesOverviewItem> items, boolean isDone) {
        articlesOverviewItems.addAll(items);
        notifyDataSetChangedThreadSafe();
      }
    });
  }


  @Override
  public void cleanUp() {
    context = null;
    extractorWithArticleOverview = null;

    articlesOverviewItems.clear();
    articlesOverviewItems = null;
  }


  @Override
  public int getCount() {
    return articlesOverviewItems.size();
  }

  public ArticlesOverviewItem getArticleAt(int position) {
    return articlesOverviewItems.get(position);
  }

  @Override
  public Object getItem(int position) {
    return getArticleAt(position);
  }

  @Override
  public long getItemId(int position) {
    return position;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    if(convertView == null)
      convertView = context.getLayoutInflater().inflate(R.layout.list_item_articles_overview_item, parent, false);

    ArticlesOverviewItem article = getArticleAt(position);

    ImageView imgvwArticlePreviewImage = (ImageView) convertView.findViewById(R.id.imgvwArticlePreviewImage);
    Picasso.with(context)
        .load(article.getPreviewImageUrl())
        .into(imgvwArticlePreviewImage);

    TextView txtvwArticleSubTitle = (TextView)convertView.findViewById(R.id.txtvwArticleSubTitle);
    if(article.hasSubTitle()) {
      txtvwArticleSubTitle.setVisibility(View.VISIBLE);
      txtvwArticleSubTitle.setText(article.getSubTitle());
    }
    else
      txtvwArticleSubTitle.setVisibility(View.GONE);

    TextView txtvwArticleTitle = (TextView)convertView.findViewById(R.id.txtvwArticleTitle);
    if(article.hasTitle()) {
      txtvwArticleTitle.setVisibility(View.VISIBLE);
      txtvwArticleTitle.setText(article.getTitle());
    }
    else
      txtvwArticleTitle.setVisibility(View.GONE);

    TextView txtvwArticleSummary = (TextView)convertView.findViewById(R.id.txtvwArticleSummary);
    if(article.hasSummary()) {
      txtvwArticleSummary.setVisibility(View.VISIBLE);
      txtvwArticleSummary.setText(article.getSummary());
    }
    else {
      txtvwArticleSummary.setVisibility(View.GONE);
    }

    return convertView;
  }


  protected void notifyDataSetChangedThreadSafe() {
    context.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        notifyDataSetChanged();
      }
    });
  }

}
