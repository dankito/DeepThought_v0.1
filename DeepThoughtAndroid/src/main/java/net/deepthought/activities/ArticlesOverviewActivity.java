package net.deepthought.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import net.deepthought.R;
import net.deepthought.adapter.ArticlesOverviewAdapter;
import net.deepthought.data.contentextractor.CreateEntryListener;
import net.deepthought.data.contentextractor.EntryCreationResult;
import net.deepthought.data.contentextractor.IOnlineArticleContentExtractor;
import net.deepthought.data.contentextractor.preview.ArticlesOverviewItem;
import net.deepthought.helper.AlertHelper;
import net.deepthought.util.localization.Localization;

/**
 * Created by ganymed on 25/09/15.
 */
public class ArticlesOverviewActivity extends AppCompatActivity {


  protected ListView lstvwArticlesOverview = null;
  protected ArticlesOverviewAdapter articlesOverviewAdapter = null;


  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setupUi();
  }

  protected void setupUi() {
    setContentView(R.layout.activity_articles_overview);

    IOnlineArticleContentExtractor contentExtractor = ActivityManager.getInstance().getExtractorToShowArticlesOverviewActivityFor();

    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
//      setLogo(toolbar);

    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.setDisplayShowTitleEnabled(true);
      actionBar.setDisplayHomeAsUpEnabled(true);
      actionBar.setHomeButtonEnabled(true);

//      if(contentExtractor.getIconUrl() != IOnlineArticleContentExtractor.NoIcon) {
//        ImageView iconView = new ImageView(this);
//        toolbar.addView(iconView);
//        IconManager.getInstance().setImageViewToImageFromUrl(iconView, contentExtractor.getIconUrl());
//      }
      actionBar.setTitle(contentExtractor.getSiteBaseUrl());
    }

    articlesOverviewAdapter = new ArticlesOverviewAdapter(this, contentExtractor);

    lstvwArticlesOverview = (ListView) findViewById(R.id.lstvwArticlesOverview);
    lstvwArticlesOverview.setAdapter(articlesOverviewAdapter);
    lstvwArticlesOverview.setOnItemClickListener(lstvwArticlesOverviewOnItemClickListener);
  }


  @Override
  public void finish() {
    cleanUp();
    super.finish();
  }

  protected void cleanUp() {
    lstvwArticlesOverview.setAdapter(null);
    lstvwArticlesOverview.setOnItemClickListener(null);

    articlesOverviewAdapter.cleanUp();
    articlesOverviewAdapter = null;
    lstvwArticlesOverviewOnItemClickListener = null;

    ActivityManager.getInstance().resetShowArticlesOverviewActivityCachedData();
  }


  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.activity_articles_overview_menu, menu);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();

    if(id == R.id.mnitmActionUpdateArticlesOverview) {
      articlesOverviewAdapter.retrieveArticles();
      return true;
    }

    return super.onOptionsItemSelected(item);
  }


  protected AdapterView.OnItemClickListener lstvwArticlesOverviewOnItemClickListener = new AdapterView.OnItemClickListener() {
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
      ArticlesOverviewItem clickedItem = articlesOverviewAdapter.getArticleAt(position);
      clickedItem.getArticleContentExtractor().createEntryFromUrlAsync(clickedItem.getUrl(), new CreateEntryListener() {
        @Override
        public void entryCreated(EntryCreationResult creationResult) {
          // TODO: this is the same code as in CreateEntryFromClipboardContentPopup.createEntryFromOnlineArticleButViewFirst() and in ArticlesOverviewDialogController -> unify
          if (creationResult.successful())
            ActivityManager.getInstance().showEditEntryActivity(ArticlesOverviewActivity.this, creationResult);
        else
          AlertHelper.showErrorMessage(ArticlesOverviewActivity.this, creationResult.getError(),
              Localization.getLocalizedString("can.not.create.entry.from", creationResult.getSource()));
        }
      });
    }
  };
}
