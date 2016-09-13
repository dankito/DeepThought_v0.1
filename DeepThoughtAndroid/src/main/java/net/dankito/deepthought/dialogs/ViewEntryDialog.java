package net.dankito.deepthought.dialogs;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.dankito.deepthought.Application;
import net.dankito.deepthought.R;
import net.dankito.deepthought.activities.DialogParentActivity;
import net.dankito.deepthought.data.contentextractor.EntryCreationResult;
import net.dankito.deepthought.data.model.Entry;
import net.dankito.deepthought.data.model.ReferenceBase;
import net.dankito.deepthought.data.model.Tag;
import net.dankito.deepthought.data.model.listener.EntityListener;
import net.dankito.deepthought.data.persistence.db.BaseEntity;
import net.dankito.deepthought.data.persistence.db.TableConfig;
import net.dankito.deepthought.dialogs.enums.EditEntrySection;
import net.dankito.deepthought.listener.DialogListener;
import net.dankito.deepthought.ui.model.ReferenceBaseUtil;
import net.dankito.deepthought.ui.model.TagsUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by ganymed on 13/09/16.
 */
public class ViewEntryDialog extends FullscreenDialog {

  public final static int RequestCode = 1;

  private final static Logger log = LoggerFactory.getLogger(ViewEntryDialog.class);


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

  protected TagsUtil tagsUtil = new TagsUtil();

  protected ReferenceBaseUtil referenceBaseUtil = new ReferenceBaseUtil();


  public void setEntry(Entry entry) {
    this.entry = entry;
  }

  public void setEntryCreationResult(EntryCreationResult entryCreationResult) {
    this.entryCreationResult = entryCreationResult;
  }


  public void showDialog(DialogParentActivity activity, EntryCreationResult entryCreationResult) {
    setEntryCreationResult(entryCreationResult);

    super.showDialog(activity);
  }

  public void showDialog(DialogParentActivity activity, Entry entry) {
    setEntry(entry);

    super.showDialog(activity);
  }


  @Override
  protected int getLayoutId() {
    return R.layout.dialog_view_entry;
  }

  @Override
  protected void setupUi(View rootView) {
    setupAbstractSection(rootView);

    setupContentSection(rootView);

    setupTagsSection(rootView);

    setEntryValues();
  }

  protected void setupAbstractSection(View rootView) {
    rlytEntryAbstract = (RelativeLayout)rootView.findViewById(R.id.rlytEntryAbstract);
    rlytEntryAbstract.setOnClickListener(rlytEntryAbstractOnClickListener);

    txtvwEntryAbstractPreview = (TextView)rootView.findViewById(R.id.txtvwEntryAbstractPreview);
  }

  protected void setupContentSection(View rootView) {
    wbvwContent = (WebView)rootView.findViewById(R.id.wbvwContent);
    wbvwContent.setHorizontalScrollBarEnabled(true);
    wbvwContent.setVerticalScrollBarEnabled(true);

    WebSettings settings = wbvwContent.getSettings();
    settings.setDefaultTextEncodingName("utf-8"); // otherwise non ASCII text doesn't get displayed correctly
    settings.setDefaultFontSize(12); // default font is way to large
    settings.setJavaScriptEnabled(true); // so that embedded videos etc. work

    wbvwContent.setWebViewClient(webViewClient);
  }

  protected void setupTagsSection(View rootView) {
    RelativeLayout rlytTags = (RelativeLayout)rootView.findViewById(R.id.rlytTags);
    rlytTags.setOnClickListener(rlytTagsOnClickListener);

    txtvwEntryTagsPreview = (TextView)rootView.findViewById(R.id.txtvwEntryTagsPreview);
  }


  protected void setEntryValues() {
    List<Tag> entryEditedTags = new ArrayList<>(0);

    if(entry != null) {
      entryEditedTags = new ArrayList<>(entry.getTagsSorted());
    }

    if(entryCreationResult != null) {
      entry = entryCreationResult.getCreatedEntry();
      entryEditedTags = entryCreationResult.getTags();
    }

    if(entry != null) {
      setContentHtml(entry.getContent());
      setTextViewEntryTagsPreview(entryEditedTags);

      if(entry.hasAbstract()) {
        setAbstractPreview(entry.getAbstractAsPlainText()); // or use Html.fromHtml() ?
      }
      else {
        rlytEntryAbstract.setVisibility(View.GONE);
      }

      entry.addEntityListener(entryListener);
    }
  }


  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    menu.clear();
    inflater.inflate(R.menu.activity_edit_entry_menu, menu);

    MenuItem mnitmActionShareEntry = menu.findItem(R.id.mnitmActionShareEntry);

    shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(mnitmActionShareEntry);

    mnitmActionSaveEntry = menu.findItem(R.id.mnitmActionSaveEntry);
    mnitmActionSaveEntry.setVisible(entryCreationResult != null);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();

    if(isShowingEditEntryDialog) {
      return editEntryDialog.onOptionsItemSelected(item);
    }
    else if(id == R.id.mnitmActionEdit) {
      editEntry();
      return true;
    }
    else if(id == R.id.mnitmActionSaveEntry) {
      saveEntryAndCloseDialog();
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


  @Override
  protected int getAlertMessageIfChangesShouldGetSaved() {
    return R.string.alert_dialog_entry_has_unsaved_changes_text;
  }


  @Override
  protected void saveEntity() {
    saveEntryCreationResult();
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
    if(activity != null) {
      activity.invalidateOptionsMenu();
    }
  }


  protected void shareEntryWithOtherApps() {
    Intent shareIntent = new Intent();
    shareIntent.setAction(Intent.ACTION_SEND);

    String content = entry.getContentAsPlainText();
    content += appendReferenceInfo();

    shareIntent.putExtra(Intent.EXTRA_TEXT, content);
    shareIntent.putExtra(Intent.EXTRA_HTML_TEXT, entry.getContent());

    shareIntent.putExtra(Intent.EXTRA_SUBJECT, entry.getAbstractAsPlainText());

    shareIntent.setType("text/plain");

    setShareIntent(shareIntent);

    startActivity(shareIntent);
  }

  protected String appendReferenceInfo() {
    String referenceInfo = "";

    ReferenceBase entryReference = null;
    if(entryCreationResult != null) {
      entryReference = entryCreationResult.getLowestReferenceBase();
    }
    if(entryReference == null && entry.isAReferenceSet()) {
      entryReference = entry.getLowestReferenceBase();
    }

    if(entryReference != null) {
      referenceInfo += "\r\n\r\n(" + entryReference.getTextRepresentation(); // TODO: for entryCreationResult this returns only entryReference's text representation, not
      // including its parent references' text representation (e.g. Reference and SeriesTitle)

      String referenceUrl = referenceBaseUtil.getReferenceBaseUrl(entryReference);
      if(referenceUrl != null) {
        referenceInfo += ": " + referenceUrl;
      }

      referenceInfo += ")";
    }

    return referenceInfo;
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
      super.onBackPressed();
    }
  }

  protected void setAbstractPreviewFromHtmlThreadSafe(final String abstractHtml) {
    activity.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        setAbstractPreviewFromHtml(abstractHtml);
      }
    });
  }

  protected void setAbstractPreviewFromHtml(String abstractHtml) {
    setAbstractPreview(Application.getHtmlHelper().extractPlainTextFromHtmlBody(abstractHtml));
  }

  protected void setAbstractPreview(String abstractPlainText) {
    txtvwEntryAbstractPreview.setText(abstractPlainText);
  }

  protected void setContentHtmlThreadSafe(final String contentHtml) {
    activity.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        setContentHtml(contentHtml);
      }
    });
  }

  protected void setContentHtml(String contentHtml) {
    String formattedContentHtml = "<body style=\"font-family: serif, Georgia, Roboto, Helvetica, Arial; font-size:17;\"" + contentHtml + "</body>";
    wbvwContent.loadDataWithBaseURL(null, formattedContentHtml, "text/html; charset=utf-8", "utf-8", null); // otherwise non ASCII text doesn't get displayed correctly
  }

  protected void setTextViewEntryTagsPreviewThreadSafe(final Collection<Tag> tags) {
    activity.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        setTextViewEntryTagsPreview(tags);
      }
    });
  }

  protected void setTextViewEntryTagsPreview(Collection<Tag> tags) {
    String tagsPreview = tagsUtil.createTagsPreview(tags, true);

    txtvwEntryTagsPreview.setText(tagsPreview);
  }


  @Override
  public boolean canHandleActivityResult(int requestCode, int resultCode, Intent data) {
    if(editEntryDialog != null) {
      return editEntryDialog.canHandleActivityResult(requestCode, resultCode, data);
    }

    return false;
  }

  public void cleanUp() {
    if(editEntryDialog != null) {
      editEntryDialog.cleanUp();

      editEntryDialog = null;
    }

    entry.removeEntityListener(entryListener);

    super.cleanUp();
  }


  protected void showEditEntryDialog(EditEntrySection sectionToEdit) {
    if(editEntryDialog == null) { // on first display create EditEntryDialog and add it to transaction
      editEntryDialog = createEditEntryDialog();
    }

    editEntryDialog.showDialog(this.activity, sectionToEdit);

    isShowingEditEntryDialog = true;
  }

  protected EditEntryDialog createEditEntryDialog() {
    EditEntryDialog editEntryDialog = new EditEntryDialog();

    editEntryDialog.setEntry(entry);
    editEntryDialog.setHideOnClose(true);
    editEntryDialog.setDialogListener(editEntryDialogListener);

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

  protected DialogListener editEntryDialogListener = new DialogListener() {
    @Override
    public void dialogBecameHidden(boolean didSaveChanges) {
      isShowingEditEntryDialog = false;

      if(didSaveChanges && entryCreationResult != null) {
        entryCreationResultHasNowBeenSaved();
      }
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


  protected EntityListener entryListener = new EntityListener() {
    @Override
    public void propertyChanged(BaseEntity entity, String propertyName, Object previousValue, Object newValue) {
      checkForEntryChanges(propertyName, newValue);
    }

    @Override
    public void entityAddedToCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity addedEntity) {
      checkForEntrySubCollectionChanges(collection, addedEntity);
    }

    @Override
    public void entityOfCollectionUpdated(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity updatedEntity) {
      checkForEntrySubCollectionChanges(collection, updatedEntity);
    }

    @Override
    public void entityRemovedFromCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity removedEntity) {
      checkForEntrySubCollectionChanges(collection, removedEntity);
    }
  };

  protected void checkForEntryChanges(String propertyName, Object newValue) {
    if(TableConfig.EntryAbstractColumnName.equals(propertyName)) {
      rlytEntryAbstract.setVisibility(View.VISIBLE);
      setAbstractPreviewFromHtmlThreadSafe((String)newValue);
    }
    else if(TableConfig.EntryContentColumnName.equals(propertyName)) {
      setContentHtmlThreadSafe((String)newValue);
    }
  }

  protected void checkForEntrySubCollectionChanges(Collection<? extends BaseEntity> collection, BaseEntity editedEntity) {
    if(editedEntity instanceof Tag) {
      setTextViewEntryTagsPreviewThreadSafe((Collection<Tag>)collection);
    }
  }

}
