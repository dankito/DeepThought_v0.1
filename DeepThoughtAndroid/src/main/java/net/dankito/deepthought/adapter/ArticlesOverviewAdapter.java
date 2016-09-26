package net.dankito.deepthought.adapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import net.dankito.deepthought.R;
import net.dankito.deepthought.controls.ICleanUp;
import net.dankito.deepthought.data.contentextractor.IOnlineArticleContentExtractor;
import net.dankito.deepthought.data.contentextractor.preview.ArticlesOverviewItem;
import net.dankito.deepthought.data.contentextractor.preview.ArticlesOverviewListener;

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

    retrieveArticlesOnUiThread();
  }

  public void retrieveArticlesOnUiThread() {
    articlesOverviewItems.clear();
    notifyDataSetChanged();

    extractorWithArticleOverview.getArticlesOverviewAsync(new ArticlesOverviewListener() {
      @Override
      public void overviewItemsRetrieved(IOnlineArticleContentExtractor contentExtractor, final Collection<ArticlesOverviewItem> items, boolean isDone) {
        updateArticlesOverviewItemsThreadSafe(items);
      }
    });
  }

  protected void updateArticlesOverviewItemsThreadSafe(final Collection<ArticlesOverviewItem> items) {
    context.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        articlesOverviewItems.addAll(items);
        notifyDataSetChanged();
      }
    });
  }


  @Override
  public void cleanUp() {
    context = null;
    extractorWithArticleOverview = null;

    articlesOverviewItems.clear();
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
    if(article.hasPreviewImageUrl()) {
      Picasso.with(context)
          .load(article.getPreviewImageUrl())
          .into(imgvwArticlePreviewImage);
    }
    else {
      imgvwArticlePreviewImage.setVisibility(View.INVISIBLE);
    }

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

}
