package net.dankito.deepthought.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.dankito.deepthought.Application;
import net.dankito.deepthought.R;
import net.dankito.deepthought.controls.ICleanUp;
import net.dankito.deepthought.data.contentextractor.EntryCreationResult;
import net.dankito.deepthought.data.model.Entry;
import net.dankito.deepthought.data.model.Tag;
import net.dankito.deepthought.data.persistence.db.BaseEntity;
import net.dankito.deepthought.dialogs.EditEntryDialog;
import net.dankito.deepthought.dialogs.enums.EditEntrySection;
import net.dankito.deepthought.helper.AlertHelper;
import net.dankito.deepthought.listener.DialogListener;
import net.dankito.deepthought.listener.EditEntityListener;
import net.dankito.deepthought.ui.enums.FieldWithUnsavedChanges;
import net.dankito.deepthought.ui.model.TagsUtil;
import net.dankito.deepthought.util.InsertImageOrRecognizedTextHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ganymed on 01/10/14.
 */
public class EditEntryActivity extends AppCompatActivity implements ICleanUp {

  public final static int RequestCode = 1;

  private final static Logger log = LoggerFactory.getLogger(EditEntryActivity.class);


  protected Entry entry = null;
  protected EntryCreationResult entryCreationResult = null;

  protected boolean isShowingEditEntryDialog = false;
  protected EditEntryDialog editEntryDialog = null;


  protected RelativeLayout rlytEntryAbstract;
  protected TextView txtvwEntryAbstractPreview;

  protected WebView wbvwContent = null;

  protected TextView txtvwEntryTagsPreview;

  protected MenuItem mnitmActionSaveEntry;

  protected ShareActionProvider shareActionProvider;

  protected InsertImageOrRecognizedTextHelper insertImageOrRecognizedTextHelper;

  protected TagsUtil tagsUtil = new TagsUtil();



  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    insertImageOrRecognizedTextHelper = new InsertImageOrRecognizedTextHelper(this);

    setupUi();

    setEntryValues();
  }

  protected void setupUi() {
    try {
      setContentView(R.layout.activity_edit_entry);

      setupToolbar();

      setupAbstractSection();

      setupContentSection();

      setupTagsSection();
    } catch(Exception ex) {
      log.error("Could not setup UI", ex);
      AlertHelper.showErrorMessage(this, getString(R.string.error_message_could_not_show_activity, ex.getLocalizedMessage()));
      finish();
    }
  }

  protected void setupToolbar() {
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    ActionBar actionBar = getSupportActionBar();
    if(actionBar != null) {
      actionBar.setDisplayShowTitleEnabled(false);
      actionBar.setHomeButtonEnabled(true);
    }
  }

  protected void setupAbstractSection() {
    rlytEntryAbstract = (RelativeLayout)findViewById(R.id.rlytEntryAbstract);
    rlytEntryAbstract.setOnClickListener(rlytEntryAbstractOnClickListener);

    txtvwEntryAbstractPreview = (TextView) findViewById(R.id.txtvwEntryAbstractPreview);
  }

  protected void setupContentSection() {
    wbvwContent = (WebView)findViewById(R.id.wbvwContent);
    wbvwContent.setHorizontalScrollBarEnabled(true);
    wbvwContent.setVerticalScrollBarEnabled(true);

    WebSettings settings = wbvwContent.getSettings();
    settings.setDefaultTextEncodingName("utf-8"); // otherwise non ASCII text doesn't get displayed correctly
    settings.setDefaultFontSize(12); // default font is way to large
    settings.setJavaScriptEnabled(true); // so that embedded videos etc. work

    wbvwContent.setWebViewClient(webViewClient);
  }

  protected void setupTagsSection() {
    RelativeLayout rlytTags = (RelativeLayout) findViewById(R.id.rlytTags);
    rlytTags.setOnClickListener(rlytTagsOnClickListener);

    txtvwEntryTagsPreview = (TextView) findViewById(R.id.txtvwEntryTagsPreview);
  }


  protected void setEntryValues() {
    entry = ActivityManager.getInstance().getEntryToBeEdited();
    List<Tag> entryEditedTags = new ArrayList<>(0);

    if(entry != null) {
      entryEditedTags = new ArrayList<>(entry.getTagsSorted());
      setContentHtml(entry.getContent());
    }

    entryCreationResult = ActivityManager.getInstance().getEntryCreationResultToBeEdited();
    if(entryCreationResult != null) {
      // TODO: why also instantiating contentHtmlEditor if it shouldn't be displayed at all?
      entry = entryCreationResult.getCreatedEntry();
      entryEditedTags = entryCreationResult.getTags();
      setContentHtml(entry.getContent());
    }

    if(entry != null) {
      if(entry.hasAbstract()) {
        setAbstractPreview(entry.getAbstractAsPlainText()); // or use Html.fromHtml() ?
      }
      else {
        rlytEntryAbstract.setVisibility(View.GONE);
      }

      setTextViewEntryTagsPreview(entryEditedTags);
    }
  }


  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.activity_edit_entry_menu, menu);

    MenuItem mnitmActionShareEntry = menu.findItem(R.id.mnitmActionShareEntry);

    shareActionProvider = (ShareActionProvider)MenuItemCompat.getActionProvider(mnitmActionShareEntry);

    mnitmActionSaveEntry = menu.findItem(R.id.mnitmActionSaveEntry);
    mnitmActionSaveEntry.setVisible(entryCreationResult != null);

    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();

    if(id == android.R.id.home) {
      if(isShowingEditEntryDialog == false) {
        finish();
        return true;
      }
    }
    else if(id == R.id.mnitmActionEdit) {
      editEntry();
      return true;
    }
    else if(id == R.id.mnitmActionSaveEntry) {
      saveEntryCreationResultAndCloseActivity();
      return true;
    }
    else if(id == R.id.mnitmActionShareEntry) {
      shareEntryWithOtherApps();
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  protected void editEntry() {
    showEditEntryDialog(EditEntrySection.Content);
  }


  protected void saveEntryCreationResultAndCloseActivity() {
    saveEntryCreationResult();

    finish();
  }

  protected void saveEntryCreationResult() {
    if(entryCreationResult != null) {
      entryCreationResult.saveCreatedEntities();
    }

    entryCreationResultHasNowBeenSaved();
  }

  protected void entryCreationResultHasNowBeenSaved() {
    entryCreationResult = null;

    mnitmActionSaveEntry.setVisible(false);
    invalidateOptionsMenu();
  }

  protected void shareEntryWithOtherApps() {
    Intent shareIntent = new Intent();
    shareIntent.setAction(Intent.ACTION_SEND);

    shareIntent.putExtra(Intent.EXTRA_TEXT, entry.getContentAsPlainText()); // TODO: Serialize Entry to Json or similar
    shareIntent.putExtra(Intent.EXTRA_HTML_TEXT, entry.getContent());

    shareIntent.putExtra(Intent.EXTRA_SUBJECT, entry.getAbstractAsPlainText());

    // TODO: may also add Reference URL (as EXTRA_TITLE)

    shareIntent.setType("text/plain"); // TODO: set correct MIME Type then

    setShareIntent(shareIntent);

    startActivity(shareIntent);
  }

  protected void setShareIntent(Intent shareIntent) {
    if (shareActionProvider != null) {
      shareActionProvider.setShareIntent(shareIntent);
    }
  }


  @Override
  public void onBackPressed() {
    if(isShowingEditEntryDialog) {
      editEntryDialog.onBackPressed();
    }
    else {
      cleanUp();
      super.onBackPressed();
    }
  }

  protected void setAbstractPreviewFromHtml(String abstractHtml) {
    setAbstractPreview(Application.getHtmlHelper().extractPlainTextFromHtmlBody(abstractHtml));
  }

  protected void setAbstractPreview(String abstractPlainText) {
    txtvwEntryAbstractPreview.setText(abstractPlainText);
  }

  protected void setContentHtml(String contentHtml) {
    String formattedContentHtml = "<body style=\"font-family: serif, Georgia, Roboto, Helvetica, Arial; font-size:17;\"" + contentHtml + "</body>";
    wbvwContent.loadDataWithBaseURL(null, formattedContentHtml, "text/html; charset=utf-8", "utf-8", null); // otherwise non ASCII text doesn't get displayed correctly
  }

  protected void setTextViewEntryTagsPreview(List<Tag> tags) {
    String tagsPreview = tagsUtil.createTagsPreview(tags, true);

    txtvwEntryTagsPreview.setText(tagsPreview);
  }


  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if(insertImageOrRecognizedTextHelper.canHandleActivityResult(requestCode, resultCode, data) == false) {

    }
  }

  @Override
  public void finish() {
    cleanUp();
    super.finish();
  }

  public void cleanUp() {
    if(editEntryDialog != null) {
      editEntryDialog.cleanUp();
    }
  }


  protected void showEditEntryDialog(EditEntrySection sectionToEdit) {
    if(editEntryDialog == null) { // on first display create EditEntryDialog and add it to transaction
      editEntryDialog = createEditEntryDialog();
    }

    editEntryDialog.showDialog(this, sectionToEdit);

    isShowingEditEntryDialog = true;
  }

  protected EditEntryDialog createEditEntryDialog() {
    EditEntryDialog editEntryDialog = new EditEntryDialog();

    editEntryDialog.setEditEntityListener(editEntryListener);
    editEntryDialog.setDialogListener(editEntryDialogListener);
    editEntryDialog.setInsertImageOrRecognizedTextHelper(insertImageOrRecognizedTextHelper);
    editEntryDialog.setCleanUpOnClose(false);
    editEntryDialog.setEntry(entry);

    if(entryCreationResult != null) {
      editEntryDialog.setEntryCreationResult(entryCreationResult);
    }

    return editEntryDialog;
  }


  protected View.OnClickListener rlytEntryAbstractOnClickListener = new View.OnClickListener() {
    @Override
    public void onClick(View v) {
      showEditEntryDialog(EditEntrySection.Abstract);
    }
  };

  protected View.OnClickListener rlytTagsOnClickListener = new View.OnClickListener() {
    @Override
    public void onClick(View v) {
      showEditEntryDialog(EditEntrySection.Tags);
    }
  };


  protected EditEntityListener editEntryListener = new EditEntityListener() {
    @Override
    public void entityEdited(BaseEntity entity, FieldWithUnsavedChanges changedField, Object newFieldValue) {
      if(changedField == FieldWithUnsavedChanges.EntryAbstract) {
        setAbstractPreviewFromHtml((String)newFieldValue);
      }
      else if(changedField == FieldWithUnsavedChanges.EntryContent) {
        setContentHtml((String)newFieldValue);
      }
      else if(changedField == FieldWithUnsavedChanges.EntryTags) {
        List<Tag> entryEditedTags = (List<Tag>)newFieldValue;
        setTextViewEntryTagsPreview(entryEditedTags);
      }

      entryCreationResultHasNowBeenSaved();
    }
  };

  protected DialogListener editEntryDialogListener = new DialogListener() {
    @Override
    public void dialogBecameHidden() {
      isShowingEditEntryDialog = false;
    }
  };



  protected WebViewClient webViewClient = new WebViewClient() {
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
      Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
      startActivity(intent);

      return true;
    }
  };

}
