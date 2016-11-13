package net.dankito.deepthought.dialogs;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import net.dankito.deepthought.Application;
import net.dankito.deepthought.R;
import net.dankito.deepthought.adapter.ArticlesOverviewAdapter;
import net.dankito.deepthought.data.contentextractor.CreateEntryListener;
import net.dankito.deepthought.data.contentextractor.EntryCreationResult;
import net.dankito.deepthought.data.contentextractor.IContentExtractorManager;
import net.dankito.deepthought.data.contentextractor.IOnlineArticleContentExtractor;
import net.dankito.deepthought.data.contentextractor.preview.ArticlesOverviewItem;
import net.dankito.deepthought.data.contentextractor.preview.ArticlesOverviewListener;
import net.dankito.deepthought.data.contentextractor.preview.GetArticlesOverviewItemsResponse;
import net.dankito.deepthought.data.listener.ApplicationListener;
import net.dankito.deepthought.data.model.DeepThought;
import net.dankito.deepthought.data.persistence.deserializer.DeserializationResult;
import net.dankito.deepthought.data.persistence.json.JsonIoJsonHelper;
import net.dankito.deepthought.data.persistence.serializer.SerializationResult;
import net.dankito.deepthought.helper.AlertHelper;
import net.dankito.deepthought.listener.DialogListener;
import net.dankito.deepthought.util.Notification;
import net.dankito.deepthought.util.NotificationType;
import net.dankito.deepthought.util.localization.Localization;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by ganymed on 25/09/15.
 */
public class ArticlesOverviewDialog extends FullscreenDialog {

  protected static final String CONTENT_EXTRACTOR_URL_BUNDLE_KEY = "ContentExtractorUrl";

  protected static final String ARTICLE_OVERVIEW_ITEMS_BUNDLE_KEY = "ArticleOverviewItems";


  protected ListView lstvwArticlesOverview = null;
  protected ArticlesOverviewAdapter articlesOverviewAdapter = null;

  protected IOnlineArticleContentExtractor contentExtractor = null;

  protected List<ViewEntryDialog> activatedViewEntryDialogs = new CopyOnWriteArrayList<>();


  public void setContentExtractor(IOnlineArticleContentExtractor contentExtractor) {
    this.contentExtractor = contentExtractor;
  }


  @Override
  public void onSaveInstanceState(Bundle outState) {
    if(contentExtractor != null) {
      outState.putString(CONTENT_EXTRACTOR_URL_BUNDLE_KEY, contentExtractor.getSiteBaseUrl());
    }

    SerializationResult result = JsonIoJsonHelper.generateJsonString(articlesOverviewAdapter.getArticlesOverviewItems());
    if(result.successful()) {
      outState.putString(ARTICLE_OVERVIEW_ITEMS_BUNDLE_KEY, result.getSerializationResult());
    }

    super.onSaveInstanceState(outState);
  }

  @Override
  protected void restoreSavedInstance(Bundle savedInstanceState) {
    if(savedInstanceState != null) {
      tryToRestoreArticleOverviewItems(savedInstanceState);

      String contentExtractorUrl = savedInstanceState.getString(CONTENT_EXTRACTOR_URL_BUNDLE_KEY);

      if(contentExtractorUrl != null) {
        tryToRestoreContentExtractorTitle(contentExtractorUrl);

        tryToRestoreContentExtractor(contentExtractorUrl);
      }
    }
  }

  protected void tryToRestoreArticleOverviewItems(Bundle savedInstanceState) {
    String articleOverviewItemsJsonString = savedInstanceState.getString(ARTICLE_OVERVIEW_ITEMS_BUNDLE_KEY);

    if(articleOverviewItemsJsonString != null) {
      DeserializationResult<List> result = JsonIoJsonHelper.parseJsonString(articleOverviewItemsJsonString, List.class);

      if(result.successful()) {
        initializeArticlesOverviewAdapter();
        articlesOverviewAdapter.appendArticlesOverviewItems((List<ArticlesOverviewItem>)result.getResult());
      }
    }
  }

  protected void tryToRestoreContentExtractorTitle(String contentExtractorUrl) {
    ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
    if(actionBar != null) {
      setActionBarTitle(actionBar, contentExtractorUrl);
    }
  }

  protected void tryToRestoreContentExtractor(String contentExtractorUrl) {
    IContentExtractorManager contentExtractorManager = Application.getContentExtractorManager();
    if(contentExtractorManager != null) {
      List<IOnlineArticleContentExtractor> onlineArticleContentExtractors = contentExtractorManager.getOnlineArticleContentExtractors();

      for(IOnlineArticleContentExtractor contentExtractor : onlineArticleContentExtractors) {
        if(contentExtractorUrl.equals(contentExtractor.getSiteBaseUrl())) {
          this.contentExtractor = contentExtractor;

          retrieveArticles();
          break;
        }
      }

      if(this.contentExtractor == null) {
        tryToRestoreContentExtractorByApplicationListener(contentExtractorUrl);
      }
    }
  }

  protected void tryToRestoreContentExtractorByApplicationListener(final String contentExtractorUrl) {
    ApplicationListener listener = new ApplicationListener() {
      @Override
      public void deepThoughtChanged(DeepThought deepThought) {

      }

      @Override
      public void notification(Notification notification) {
        if(notification.getType() == NotificationType.PluginLoaded) {
          if(notification.getParameter() instanceof IOnlineArticleContentExtractor) {
            IOnlineArticleContentExtractor contentExtractor = (IOnlineArticleContentExtractor)notification.getParameter();
            if(contentExtractorUrl.equals(contentExtractor.getSiteBaseUrl())) {
              Application.removeApplicationListener(this);

              ArticlesOverviewDialog.this.contentExtractor = contentExtractor;

              retrieveArticlesThreadSafe();
            }
          }
        }
      }
    };

    Application.addApplicationListener(listener);
  }


  @Override
  protected int getLayoutId() {
    return R.layout.dialog_articles_overview;
  }

  @Override
  protected void setupUi(View rootView) {
    initializeArticlesOverviewAdapter();

    lstvwArticlesOverview = (ListView)rootView.findViewById(R.id.lstvwArticlesOverview);
    lstvwArticlesOverview.setAdapter(articlesOverviewAdapter);
    lstvwArticlesOverview.setOnItemClickListener(lstvwArticlesOverviewOnItemClickListener);

    registerForContextMenu(lstvwArticlesOverview);

    retrieveArticles();
  }

  protected void initializeArticlesOverviewAdapter() {
    if(articlesOverviewAdapter == null) {
      articlesOverviewAdapter = new ArticlesOverviewAdapter(getActivity());
    }
  }

  @Override
  protected void customizeToolbar(View rootView, ActionBar actionBar) {
    setActionBarTitle(actionBar);
  }

  protected void setActionBarTitle(ActionBar actionBar) {
    if(contentExtractor != null) {
      setActionBarTitle(actionBar, contentExtractor.getSiteBaseUrl());
    }
  }

  protected void setActionBarTitle(ActionBar actionBar, String contentExtractorTitle) {
    actionBar.setTitle(contentExtractorTitle);
  }

  @Override
  public void cleanUp() {
    lstvwArticlesOverview.setAdapter(null);
    lstvwArticlesOverview.setOnItemClickListener(null);

    articlesOverviewAdapter.cleanUp();
    lstvwArticlesOverviewOnItemClickListener = null;

    super.cleanUp();
  }


  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    menu.clear();

    inflater.inflate(R.menu.dialog_articles_overview_menu, menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();

    ViewEntryDialog lastShownViewEntryDialog = getLastShownViewEntryDialog();
    if(lastShownViewEntryDialog != null) {
      return lastShownViewEntryDialog.onOptionsItemSelected(item);
    }
    else if(id == R.id.mnitmActionUpdateArticlesOverview) {
      retrieveArticles();
      return true;
    }

    return super.onOptionsItemSelected(item);
  }


  @Override
  public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
    super.onCreateContextMenu(menu, v, menuInfo);
    MenuInflater inflater = getActivity().getMenuInflater();
    inflater.inflate(R.menu.list_item_article_overview_menu, menu);
  }

  @Override
  public boolean onContextItemSelected(MenuItem item) {
    AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
    ArticlesOverviewItem selectedArticle = articlesOverviewAdapter.getArticleAt(info.position);

    switch(item.getItemId()) {
      case R.id.list_item_articles_overview_menu_save:
        saveArticle(selectedArticle);
        return true;
      case R.id.list_item_articles_overview_menu_copy_url_to_clipboard:
        copyArticleUrlToClipboard(selectedArticle);
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

      showSuccessfullySavedArticleMessage(article);
    }
    else {
      showCouldNotExtractArticleMessage(creationResult, article);
    }
  }

  protected void showSuccessfullySavedArticleMessage(final ArticlesOverviewItem article) {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        Activity activity = getDialogOrParentActivity();
        if(activity != null) {
          String successfullySavedMessage = activity.getString(R.string.articles_overview_article_saved, article.getTitle());
          Toast.makeText(activity, successfullySavedMessage, Toast.LENGTH_LONG).show();
        }
      }
    });
  }

  protected void showCouldNotExtractArticleMessage(final EntryCreationResult creationResult, final ArticlesOverviewItem article) {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        Activity activity = getDialogOrParentActivity();
        if(activity != null) {
          String couldNotExtractArticleMessage = activity.getString(R.string.articles_overview_could_not_extract_article, article.getTitle(), creationResult.getError());
          Toast.makeText(activity, couldNotExtractArticleMessage, Toast.LENGTH_LONG).show();
        }
      }
    });
  }


  protected void copyArticleUrlToClipboard(ArticlesOverviewItem article) {
    Application.getClipboardHelper().copyUrlToClipboard(article.getUrl());
  }


  protected AdapterView.OnItemClickListener lstvwArticlesOverviewOnItemClickListener = new AdapterView.OnItemClickListener() {
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
      ArticlesOverviewItem clickedItem = articlesOverviewAdapter.getArticleAt(position);
      clickedItem.getArticleContentExtractor().createEntryFromUrlAsync(clickedItem.getUrl(), new CreateEntryListener() {
        @Override
        public void entryCreated(EntryCreationResult creationResult) {
          // TODO: this is the same code as in CreateEntryFromClipboardContentPopup.createEntryFromOnlineArticleButViewFirst() and in ArticlesOverviewDialogController -> unify
          if (creationResult.successful()) {
            showViewEntryDialog(creationResult);
          }
          else {
            final Activity activity = getDialogOrParentActivity();
            if(activity != null) {
              AlertHelper.showErrorMessage(activity, creationResult.getError(),
                  Localization.getLocalizedString("can.not.create.entry.from", creationResult.getSource()));
            }
          }
        }
      });
    }
  };

  protected void showViewEntryDialog(EntryCreationResult creationResult) {
    synchronized(activatedViewEntryDialogs) {
      if(activity != null) {
        ViewEntryDialog viewEntryDialog = createViewEntryDialog();

        if(hasOnSaveInstanceBeenCalled == false) {
          viewEntryDialog.showDialog(activity, creationResult);

          activatedViewEntryDialogs.add(0, viewEntryDialog);
        }
      }
    }
  }

  protected ViewEntryDialog createViewEntryDialog() {
    final ViewEntryDialog viewEntryDialog = new ViewEntryDialog();
//    viewEntryDialog.setHideOnClose(true);

    viewEntryDialog.setDialogListener(new DialogListener() {
      @Override
      public void dialogBecameHidden(boolean didSaveChanges) {
        activatedViewEntryDialogs.remove(viewEntryDialog);
      }
    });

    return viewEntryDialog;
  }

  @Override
  public boolean canHandleActivityResult(int requestCode, int resultCode, Intent data) {
    ViewEntryDialog lastShownViewEntryDialog = getLastShownViewEntryDialog();
    if(lastShownViewEntryDialog != null) {
      return lastShownViewEntryDialog.canHandleActivityResult(requestCode, resultCode, data);
    }

    return false;
  }

  @Override
  public void onBackPressed() {
    ViewEntryDialog lastShownViewEntryDialog = getLastShownViewEntryDialog();
    if(lastShownViewEntryDialog != null) {
      lastShownViewEntryDialog.onBackPressed();
    }
    else {
      super.onBackPressed();
    }
  }

  protected ViewEntryDialog getLastShownViewEntryDialog() {
    synchronized(activatedViewEntryDialogs) {
      if(activatedViewEntryDialogs.size() > 0) {
        // TODO: may also check: activity != null && activity.isDialogVisible(lastShownViewEntryDialog)
        return activatedViewEntryDialogs.get(0);
      }
    }

    return null;
  }



  protected void retrieveArticlesThreadSafe() {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        retrieveArticles();
      }
    });
  }

  protected void retrieveArticles() {
    articlesOverviewAdapter.clearArticlesOverviewItems();

    if(contentExtractor != null) {
      contentExtractor.getArticlesOverviewAsync(new ArticlesOverviewListener() {
        @Override
        public void overviewItemsRetrieved(GetArticlesOverviewItemsResponse response) {
          articlesOverviewItemsResponseRetrievedThreadSafe(response);
        }
      });
    }
  }

  protected void articlesOverviewItemsResponseRetrievedThreadSafe(final GetArticlesOverviewItemsResponse response) {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        articlesOverviewItemsResponseRetrieved(response);
      }
    });
  }

  protected void articlesOverviewItemsResponseRetrieved(GetArticlesOverviewItemsResponse response) {
    if(response.isSuccessful() == false) {
      AlertHelper.showErrorMessage(activity, response.getError(), Localization.getLocalizedString("alert.title.could.not.get.articles.overview"));
    }
    else {
      updateArticlesOverviewItems(response.getItems());
    }
  }

  protected void updateArticlesOverviewItems(final List<ArticlesOverviewItem> items) {
    articlesOverviewAdapter.appendArticlesOverviewItems(items);
  }

}
