package net.deepthought.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import net.deepthought.R;
import net.deepthought.adapter.ArticlesOverviewAdapter;
import net.deepthought.data.contentextractor.CreateEntryListener;
import net.deepthought.data.contentextractor.EntryCreationResult;
import net.deepthought.data.contentextractor.preview.ArticlesOverviewItem;
import net.deepthought.helper.AlertHelper;
import net.deepthought.util.Localization;

/**
 * Created by ganymed on 25/09/15.
 */
public class ArticlesOverviewActivity extends AppCompatActivity {


  protected ArticlesOverviewAdapter articlesOverviewAdapter = null;


  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setupUi();
  }

  protected void setupUi() {
    setContentView(R.layout.activity_articles_overview);

    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
//      setLogo(toolbar);

    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.setDisplayShowTitleEnabled(false);
      actionBar.setDisplayHomeAsUpEnabled(true);
      actionBar.setHomeButtonEnabled(true);
    }

    articlesOverviewAdapter = new ArticlesOverviewAdapter(this, ActivityManager.getInstance().getExtractorToShowArticlesOverviewActivityFor());

    ListView lstvwArticlesOverview = (ListView) findViewById(R.id.lstvwArticlesOverview);
    lstvwArticlesOverview.setAdapter(articlesOverviewAdapter);
    lstvwArticlesOverview.setOnItemClickListener(lstvwArticlesOverviewOnItemClickListener);
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
            ActivityManager.getInstance().showEditEntryActivity(creationResult);
        else
          AlertHelper.showErrorMessage(ArticlesOverviewActivity.this, creationResult.getError(),
              Localization.getLocalizedString("can.not.create.entry.from", creationResult.getSource()));
        }
      });
    }
  };
}
