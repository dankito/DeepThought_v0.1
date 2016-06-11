package net.dankito.deepthought.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import net.dankito.deepthought.Application;
import net.dankito.deepthought.adapter.ArticlesOverviewAdapter;
import net.dankito.deepthought.helper.AlertHelper;
import net.dankito.deepthought.R;
import net.dankito.deepthought.data.contentextractor.CreateEntryListener;
import net.dankito.deepthought.data.contentextractor.EntryCreationResult;
import net.dankito.deepthought.data.contentextractor.IOnlineArticleContentExtractor;
import net.dankito.deepthought.data.contentextractor.preview.ArticlesOverviewItem;
import net.dankito.deepthought.data.model.DeepThought;
import net.dankito.deepthought.util.localization.Localization;

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

    registerForContextMenu(lstvwArticlesOverview);
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


  @Override
  public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
    super.onCreateContextMenu(menu, v, menuInfo);
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.list_item_article_overview_menu, menu);
  }

  @Override
  public boolean onContextItemSelected(MenuItem item) {
    AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
    DeepThought deepThought = Application.getDeepThought();

    switch(item.getItemId()) {
      case R.id.list_item_articles_overview_menu_save:
        ArticlesOverviewItem selectedArticle = articlesOverviewAdapter.getArticleAt(info.position);
        saveArticle(selectedArticle);
        return true;
      default:
        return super.onContextItemSelected(item);
    }
  }

  protected void saveArticle(final ArticlesOverviewItem article) {
    article.getArticleContentExtractor().createEntryFromUrlAsync(article.getUrl(), new CreateEntryListener() {
      @Override
      public void entryCreated(EntryCreationResult creationResult) {
        handleCreateEntryResult(creationResult, article);
      }
    });
  }

  protected void handleCreateEntryResult(final EntryCreationResult creationResult, final ArticlesOverviewItem article) {
    if(creationResult.successful()) {
      creationResult.saveCreatedEntities();

      runOnUiThread(new Runnable() {
        @Override
        public void run() {
          String successfullySavedMessage = getString(R.string.articles_overview_article_saved, article.getTitle());
          Toast.makeText(ArticlesOverviewActivity.this, successfullySavedMessage, Toast.LENGTH_LONG).show();
        }
      });
    }
    else {
      runOnUiThread(new Runnable() {
        @Override
        public void run() {
          String couldNotExtractArticleMessage = getString(R.string.articles_overview_could_not_extract_article, article.getTitle(), creationResult.getError());
          Toast.makeText(ArticlesOverviewActivity.this, couldNotExtractArticleMessage, Toast.LENGTH_LONG).show();
        }
      });
    }
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
