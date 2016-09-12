package net.dankito.deepthought.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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
import net.dankito.deepthought.util.InsertImageOrRecognizedTextHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ganymed on 01/10/14.
 */
public class EditEntryActivity extends AppCompatActivity implements ICleanUp {

  public final static int RequestCode = 1;

  private final static Logger log = LoggerFactory.getLogger(EditEntryActivity.class);


  protected Entry entry = null;
  protected EntryCreationResult entryCreationResult = null;
  protected List<Tag> entryEditedTags = new ArrayList<>();

  protected Map<FieldWithUnsavedChanges, Object> editedFields = new HashMap<>();

  protected boolean isShowingEditEntryDialog = false;
  protected EditEntryDialog editEntryDialog = null;


  protected RelativeLayout rlytEntryAbstract;
  protected TextView txtvwEditEntryAbstract;

  protected WebView wbvwContent = null;

  protected RelativeLayout rlytTags;
  protected TextView txtvwEntryTagsPreview;

  protected ShareActionProvider shareActionProvider;

  protected InsertImageOrRecognizedTextHelper insertImageOrRecognizedTextHelper;



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

    txtvwEditEntryAbstract = (TextView) findViewById(R.id.txtvwEntryAbstractPreview);
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
    rlytTags = (RelativeLayout) findViewById(R.id.rlytTags);
    rlytTags.setOnClickListener(rlytTagsOnClickListener);

    txtvwEntryTagsPreview = (TextView) findViewById(R.id.txtvwEntryTagsPreview);
  }


  protected void setEntryValues() {
    entry = ActivityManager.getInstance().getEntryToBeEdited();
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

      setTextViewEditEntryTags(entryEditedTags);
    }
  }


  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.activity_edit_entry_menu, menu);

    MenuItem mnitmActionShareEntry = menu.findItem(R.id.mnitmActionShareEntry);

    shareActionProvider = (ShareActionProvider)MenuItemCompat.getActionProvider(mnitmActionShareEntry);

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
    else if(id == R.id.mnitmActionShareEntry) {
      shareEntryWithOtherApps();
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  protected void shareEntryWithOtherApps() {
    Intent shareIntent = new Intent();
    shareIntent.setAction(Intent.ACTION_SEND);

    setContentOnShareIntent(shareIntent);

    setAbstractOnShareIntent(shareIntent);

    // TODO: may also add Reference URL (as EXTRA_TITLE)

    shareIntent.setType("text/plain"); // TODO: set correct MIME Type then

    setShareIntent(shareIntent);

    startActivity(shareIntent);
  }

  protected void setContentOnShareIntent(Intent shareIntent) {
    String contentPlain = entry.getContentAsPlainText();
    String contentHtml = entry.getContent();

    if(editedFields.containsKey(FieldWithUnsavedChanges.EntryContent)) {
      contentHtml = (String)editedFields.get(FieldWithUnsavedChanges.EntryContent);
      contentPlain = Application.getHtmlHelper().extractPlainTextFromHtmlBody(contentHtml);
    }

    shareIntent.putExtra(Intent.EXTRA_TEXT, contentPlain); // TODO: Serialize Entry to Json or similar
    shareIntent.putExtra(Intent.EXTRA_HTML_TEXT, contentHtml);
  }

  protected void setAbstractOnShareIntent(Intent shareIntent) {
    String abstractPlain = entry.getAbstractAsPlainText();

    if(editedFields.containsKey(FieldWithUnsavedChanges.EntryAbstract)) {
      abstractPlain = (String)editedFields.get(FieldWithUnsavedChanges.EntryAbstract);
    }

    if(abstractPlain != null) {
      shareIntent.putExtra(Intent.EXTRA_SUBJECT, abstractPlain);
    }
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
    txtvwEditEntryAbstract.setText(abstractPlainText);
  }

  protected void setContentHtml(String contentHtml) {
    String formattedContentHtml = "<body style=\"font-family: serif, Georgia, Roboto, Helvetica, Arial; font-size:17;\"" + contentHtml + "</body>";
    wbvwContent.loadDataWithBaseURL(null, formattedContentHtml, "text/html; charset=utf-8", "utf-8", null); // otherwise non ASCII text doesn't get displayed correctly
  }

  protected void setTextViewEditEntryTags(List<Tag> tags) {
    String tagsString = "";

    for(Tag tag : tags) {
      tagsString += tag.getName() + ", ";
    }

    if(tagsString.length() > 1) {
      tagsString = tagsString.substring(0, tagsString.length() - 2);
    }

    if(tagsString.length() == 0) {
      tagsString = getString(R.string.edit_entry_no_tags_set);
    }

    txtvwEntryTagsPreview.setText(tagsString);
  }


  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if(insertImageOrRecognizedTextHelper.canHandleActivityResult(requestCode, resultCode, data) == false) {

    }
  }

  protected void editEntry() {
    showEditEntryDialog(EditEntrySection.Content);
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
    FragmentManager fragmentManager = getSupportFragmentManager();
    FragmentTransaction transaction = fragmentManager.beginTransaction();
    transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);

    if(editEntryDialog == null) { // on first display create EditEntryDialog and add it to transaction
      editEntryDialog = new EditEntryDialog();
      editEntryDialog.setEditEntityListener(editEntryListener);
      editEntryDialog.setDialogListener(editEntryDialogListener);
      editEntryDialog.setInsertImageOrRecognizedTextHelper(insertImageOrRecognizedTextHelper);
      editEntryDialog.setCleanUpOnClose(false);
      editEntryDialog.setEntry(entry);
      if(entryCreationResult != null) {
        editEntryDialog.setEntryCreationResult(entryCreationResult);
      }

      transaction.add(android.R.id.content, editEntryDialog);
    }
    else { // on subsequent displays we only have to call show() on the then hidden Dialog
      transaction.show(editEntryDialog);
    }

    transaction.commit();

    editEntryDialog.setSectionToEdit(sectionToEdit);
    passEntryFieldsToEditEntryDialog();

    isShowingEditEntryDialog = true;
  }

  protected void passEntryFieldsToEditEntryDialog() {
    final Map<FieldWithUnsavedChanges, Object> entryFieldValues = getCurrentEditedEntryFieldValues();

    editEntryDialog.setCurrentEntryFieldValues(entryFieldValues);
  }

  @NonNull
  protected Map<FieldWithUnsavedChanges, Object> getCurrentEditedEntryFieldValues() {
    final Map<FieldWithUnsavedChanges, Object> entryFieldValues = new HashMap<>();

    if(editedFields.containsKey(FieldWithUnsavedChanges.EntryAbstract)) {
      entryFieldValues.put(FieldWithUnsavedChanges.EntryAbstract, editedFields.get(FieldWithUnsavedChanges.EntryAbstract));
    }
    else {
      entryFieldValues.put(FieldWithUnsavedChanges.EntryAbstract, entry.getAbstract());
    }

    if(editedFields.containsKey(FieldWithUnsavedChanges.EntryContent)) {
      entryFieldValues.put(FieldWithUnsavedChanges.EntryContent, editedFields.get(FieldWithUnsavedChanges.EntryContent));
    }
    else {
      entryFieldValues.put(FieldWithUnsavedChanges.EntryContent, entry.getContent());
    }

    if(editedFields.containsKey(FieldWithUnsavedChanges.EntryTags)) {
      entryFieldValues.put(FieldWithUnsavedChanges.EntryTags, editedFields.get(FieldWithUnsavedChanges.EntryTags));
    }
    else {
      entryFieldValues.put(FieldWithUnsavedChanges.EntryTags, entryEditedTags);
    }

    return entryFieldValues;
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
        entryEditedTags = (List<Tag>)newFieldValue;
        setTextViewEditEntryTags(entryEditedTags);
      }

      editedFields.put(changedField, newFieldValue);

      entryCreationResult = null; // Entry is saved now
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
