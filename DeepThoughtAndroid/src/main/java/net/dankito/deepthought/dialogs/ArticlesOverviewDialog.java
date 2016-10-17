package net.dankito.deepthought.dialogs;

import android.content.Intent;
import android.support.v7.app.ActionBar;
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
import net.dankito.deepthought.data.contentextractor.IOnlineArticleContentExtractor;
import net.dankito.deepthought.data.contentextractor.preview.ArticlesOverviewItem;
import net.dankito.deepthought.helper.AlertHelper;
import net.dankito.deepthought.util.localization.Localization;

/**
 * Created by ganymed on 25/09/15.
 */
public class ArticlesOverviewDialog extends FullscreenDialog {


  protected ListView lstvwArticlesOverview = null;
  protected ArticlesOverviewAdapter articlesOverviewAdapter = null;

  protected IOnlineArticleContentExtractor contentExtractor = null;

  protected ViewEntryDialog lastShownViewEntryDialog = null;


  public void setContentExtractor(IOnlineArticleContentExtractor contentExtractor) {
    this.contentExtractor = contentExtractor;
  }


  @Override
  protected int getLayoutId() {
    return R.layout.dialog_articles_overview;
  }

  @Override
  protected void setupUi(View rootView) {
    articlesOverviewAdapter = new ArticlesOverviewAdapter(getActivity(), contentExtractor);

    lstvwArticlesOverview = (ListView)rootView.findViewById(R.id.lstvwArticlesOverview);
    lstvwArticlesOverview.setAdapter(articlesOverviewAdapter);
    lstvwArticlesOverview.setOnItemClickListener(lstvwArticlesOverviewOnItemClickListener);

    registerForContextMenu(lstvwArticlesOverview);
  }

  @Override
  protected void customizeToolbar(View rootView, ActionBar actionBar) {
    setActionBarTitle(actionBar);
  }

  protected void setActionBarTitle(ActionBar actionBar) {
    if(contentExtractor != null) {
      actionBar.setTitle(contentExtractor.getSiteBaseUrl());
    }
  }

  @Override
  public void cleanUp() {
    lstvwArticlesOverview.setAdapter(null);
    lstvwArticlesOverview.setOnItemClickListener(null);

    articlesOverviewAdapter.cleanUp();
    articlesOverviewAdapter = null;
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

    if(isViewEntryDialogVisible()) {
      return lastShownViewEntryDialog.onOptionsItemSelected(item);
    }
    else if(id == R.id.mnitmActionUpdateArticlesOverview) {
      articlesOverviewAdapter.retrieveArticlesOnUiThread();
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
    if(activity != null) {
      activity.runOnUiThread(new Runnable() {
        @Override
        public void run() {
          if(activity != null) {
            String successfullySavedMessage = activity.getString(R.string.articles_overview_article_saved, article.getTitle());
            Toast.makeText(activity, successfullySavedMessage, Toast.LENGTH_LONG).show();
          }
        }
      });
    }
  }

  protected void showCouldNotExtractArticleMessage(final EntryCreationResult creationResult, final ArticlesOverviewItem article) {
    if(activity != null) {
      activity.runOnUiThread(new Runnable() {
        @Override
        public void run() {
          if(activity != null) {
            String couldNotExtractArticleMessage = activity.getString(R.string.articles_overview_could_not_extract_article, article.getTitle(), creationResult.getError());
            Toast.makeText(activity, couldNotExtractArticleMessage, Toast.LENGTH_LONG).show();
          }
        }
      });
    }
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
        else
          AlertHelper.showErrorMessage(activity, creationResult.getError(),
              Localization.getLocalizedString("can.not.create.entry.from", creationResult.getSource()));
        }
      });
    }
  };

  protected void showViewEntryDialog(EntryCreationResult creationResult) {
    ViewEntryDialog viewEntryDialog = createViewEntryDialog();

    viewEntryDialog.showDialog(activity, creationResult);

    lastShownViewEntryDialog = viewEntryDialog;
  }

  protected ViewEntryDialog createViewEntryDialog() {
    ViewEntryDialog viewEntryDialog = new ViewEntryDialog();
//    viewEntryDialog.setHideOnClose(true);

    return viewEntryDialog;
  }

  @Override
  public boolean canHandleActivityResult(int requestCode, int resultCode, Intent data) {
    if(isViewEntryDialogVisible()) {
      return lastShownViewEntryDialog.canHandleActivityResult(requestCode, resultCode, data);
    }

    return false;
  }

  @Override
  public void onBackPressed() {
    if(isViewEntryDialogVisible()) {
      lastShownViewEntryDialog.onBackPressed();
    }
    else {
      super.onBackPressed();
    }
  }

  protected boolean isViewEntryDialogVisible() {
    return lastShownViewEntryDialog != null && activity.isDialogVisible(lastShownViewEntryDialog);
  }

}
