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
import net.dankito.deepthought.ui.model.IEntityPreviewService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by ganymed on 13/09/16.
 */
public class ViewEntryDialog extends EntryDialogBase {

  public final static int RequestCode = 1;


  private static final Logger log = LoggerFactory.getLogger(ViewEntryDialog.class);


  protected boolean hasViewBeenCreated = false;

  protected boolean showSaveAction = false;

  protected boolean isShowingEditEntryDialog = false;
  protected EditEntryDialog editEntryDialog = null;


  protected RelativeLayout rlytViewEntryAbstract;
  protected TextView txtvwViewEntryAbstractPreview;

  protected WebView wbvwViewEntryContent = null;

  protected TextView txtvwViewEntryTagsPreview;

  protected MenuItem mnitmActionSaveEntry;

  protected ShareActionProvider shareActionProvider;

  protected IEntityPreviewService previewService = Application.getEntityPreviewService();


  @Override
  public void setEntry(Entry entry) {
    super.setEntry(entry);

    if(hasViewBeenCreated) {
      setEntryValues();
    }
  }

  @Override
  public void setEntryCreationResult(EntryCreationResult entryCreationResult) {
    super.setEntryCreationResult(entryCreationResult);

    this.showSaveAction = entryCreationResult != null;

    if(hasViewBeenCreated) {
      setEntryValues();
    }
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

    hasViewBeenCreated = true;
  }

  protected void setupAbstractSection(View rootView) {
    rlytViewEntryAbstract = (RelativeLayout)rootView.findViewById(R.id.rlytViewEntryAbstract);
    rlytViewEntryAbstract.setOnClickListener(rlytViewEntryAbstractOnClickListener);

    txtvwViewEntryAbstractPreview = (TextView)rootView.findViewById(R.id.txtvwViewEntryAbstractPreview);
  }

  protected void setupContentSection(View rootView) {
    wbvwViewEntryContent = (WebView)rootView.findViewById(R.id.wbvwViewEntryContent);
    wbvwViewEntryContent.setHorizontalScrollBarEnabled(true);
    wbvwViewEntryContent.setVerticalScrollBarEnabled(true);

    WebSettings settings = wbvwViewEntryContent.getSettings();
    settings.setDefaultTextEncodingName("utf-8"); // otherwise non ASCII text doesn't get displayed correctly
    settings.setDefaultFontSize(11); // default font is way to large
    settings.setJavaScriptEnabled(true); // so that embedded videos etc. work

    wbvwViewEntryContent.setWebViewClient(webViewClient);
  }

  protected void setupTagsSection(View rootView) {
    RelativeLayout rlytViewEntryTags = (RelativeLayout)rootView.findViewById(R.id.rlytViewEntryTags);
    rlytViewEntryTags.setOnClickListener(rlytViewEntryTagsOnClickListener);

    txtvwViewEntryTagsPreview = (TextView)rootView.findViewById(R.id.txtvwViewEntryTagsPreview);
  }


  protected void setEntryValues() {
    previewService = Application.getEntityPreviewService(); // as when restoring Dialog, Application.getEntityPreviewService() hasn't been instantiated yet

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
        rlytViewEntryAbstract.setVisibility(View.GONE);
      }

      entry.addEntityListener(entryListener);
    }
  }


  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    menu.clear();
    inflater.inflate(R.menu.dialog_view_entry_menu, menu);

    MenuItem mnitmActionShareEntry = menu.findItem(R.id.mnitmActionShareEntry);

    shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(mnitmActionShareEntry);

    mnitmActionSaveEntry = menu.findItem(R.id.mnitmActionSaveEntry);
  }

  @Override
  public void onPrepareOptionsMenu(Menu menu) {
    super.onPrepareOptionsMenu(menu);

    mnitmActionSaveEntry = menu.findItem(R.id.mnitmActionSaveEntry);

    setMenuItemActionSaveEntryVisibility(mnitmActionSaveEntry);
  }

  protected void setMenuItemActionSaveEntryVisibility() {
    setMenuItemActionSaveEntryVisibility(mnitmActionSaveEntry);
  }

  protected void setMenuItemActionSaveEntryVisibility(MenuItem mnitmActionSaveEntry) {
    try {
      if(mnitmActionSaveEntry != null) {
        mnitmActionSaveEntry.setVisible(showSaveAction);
      }
    } catch(Exception e) {
      log.error("Could not set mnitmActionSaveEntry's visibility to " + showSaveAction, e);
    }
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
  protected boolean shouldUserBeAskedToSaveChanges() {
    return false;
  }

  @Override
  protected boolean hasUnsavedChangesThatShouldBeSaved() {
    return entryCreationResult != null;
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

    entryCreationResultHasNowBeenSavedOnUiThread();
  }

  protected void entryCreationResultHasNowBeenSavedOnUiThread() {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        entryCreationResultHasNowBeenSaved();
      }
    });
  }

  protected void entryCreationResultHasNowBeenSaved() {
    entryCreationResult = null;
    showSaveAction = false;

    setMenuItemActionSaveEntryVisibility();

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

      String referenceUrl = previewService.getReferenceBaseUrl(entryReference);
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
    runOnUiThread(new Runnable() {
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
    String abstractPreview = getString(R.string.view_entry_abstract_preview, abstractPlainText);
    txtvwViewEntryAbstractPreview.setText(abstractPreview);
  }

  protected void setContentHtmlThreadSafe(final String contentHtml) {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        setContentHtml(contentHtml);
      }
    });
  }

  protected void setContentHtml(String contentHtml) {
    String formattedContentHtml = "<body style=\"font-family: serif, Georgia, Roboto, Helvetica, Arial; font-size:17;\"" + contentHtml + "</body>";
    wbvwViewEntryContent.loadDataWithBaseURL(null, formattedContentHtml, "text/html; charset=utf-8", "utf-8", null); // otherwise non ASCII text doesn't get displayed correctly
  }

  protected void setTextViewEntryTagsPreviewThreadSafe(final Collection<Tag> tags) {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        setTextViewEntryTagsPreview(tags);
      }
    });
  }

  protected void setTextViewEntryTagsPreview(Collection<Tag> tags) {
    String tagsPreview = previewService.getTagsPreview(tags, true);

    txtvwViewEntryTagsPreview.setText(tagsPreview);
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

    if(activity != null) {
      editEntryDialog.showDialog(activity, sectionToEdit);

      isShowingEditEntryDialog = true;
    }
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


  protected View.OnClickListener rlytViewEntryAbstractOnClickListener = new View.OnClickListener() {
    @Override
    public void onClick(View v) {
      showEditEntryDialog(EditEntrySection.Abstract);
    }
  };

  protected View.OnClickListener rlytViewEntryTagsOnClickListener = new View.OnClickListener() {
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
        entryCreationResultHasNowBeenSavedOnUiThread();
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
      rlytViewEntryAbstract.setVisibility(View.VISIBLE);
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
