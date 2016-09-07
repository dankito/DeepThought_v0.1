package net.dankito.deepthought.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.dankito.deepthought.Application;
import net.dankito.deepthought.R;
import net.dankito.deepthought.controls.ICleanUp;
import net.dankito.deepthought.controls.html.AndroidHtmlEditor;
import net.dankito.deepthought.controls.html.AndroidHtmlEditorPool;
import net.dankito.deepthought.controls.html.HtmEditorCommand;
import net.dankito.deepthought.controls.html.HtmlEditor;
import net.dankito.deepthought.controls.html.IHtmlEditorListener;
import net.dankito.deepthought.data.contentextractor.EntryCreationResult;
import net.dankito.deepthought.data.html.ImageElementData;
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

  protected boolean hasEntryBeenEdited = false;
  protected boolean hasContentBeenEditedInActivity = false;
  protected Map<FieldWithUnsavedChanges, Object> editedFields = new HashMap<>();

  protected boolean isShowingEditEntryDialog = false;
  protected EditEntryDialog editEntryDialog = null;


  protected RelativeLayout rlydEntryAbstract;
  protected TextView txtvwEditEntryAbstract;

  protected WebView wbvwContent = null;
  protected AndroidHtmlEditor contentHtmlEditor = null;

  protected RelativeLayout rlydTags;
  protected TextView txtvwEntryTagsPreview;

  protected Uri takenPhotoTempFile = null;

  protected InsertImageOrRecognizedTextHelper insertImageOrRecognizedTextHelper;



  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    insertImageOrRecognizedTextHelper = new InsertImageOrRecognizedTextHelper(this);

    setupUi();

    setEntryValues();

    unsetEntryHasBeenEdited();
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
    rlydEntryAbstract = (RelativeLayout)findViewById(R.id.rlydEntryAbstract);
    rlydEntryAbstract.setOnClickListener(rlydEntryAbstractOnClickListener);

    txtvwEditEntryAbstract = (TextView) findViewById(R.id.txtvwEntryAbstractPreview);
  }

  protected void setupContentSection() {
    RelativeLayout rlydContent = (RelativeLayout)findViewById(R.id.rlydContent);

    contentHtmlEditor = AndroidHtmlEditorPool.getInstance().getHtmlEditor(this, contentListener);
    contentHtmlEditor.setVisibility(View.GONE);
    rlydContent.addView(contentHtmlEditor, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

    RelativeLayout.LayoutParams contentEditorParams = (RelativeLayout.LayoutParams)contentHtmlEditor.getLayoutParams();
    contentEditorParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
    contentEditorParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
    contentEditorParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
    contentEditorParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

    contentHtmlEditor.setLayoutParams(contentEditorParams);

    wbvwContent = (WebView)findViewById(R.id.wbvwContent);
    wbvwContent.setHorizontalScrollBarEnabled(true);
    wbvwContent.setVerticalScrollBarEnabled(true);

    WebSettings settings = wbvwContent.getSettings();
    settings.setDefaultTextEncodingName("utf-8"); // otherwise non ASCII text doesn't get displayed correctly
    settings.setDefaultFontSize(12); // default font is way to large
    settings.setJavaScriptEnabled(true); // so that embedded videos etc. work
  }

  protected void setupTagsSection() {
    rlydTags = (RelativeLayout) findViewById(R.id.rlydTags);
    rlydTags.setOnClickListener(rlydTagsOnClickListener);

    txtvwEntryTagsPreview = (TextView) findViewById(R.id.txtvwEntryTagsPreview);
  }


  protected void setEntryValues() {
    entry = ActivityManager.getInstance().getEntryToBeEdited();
    if(entry != null) {
      entryEditedTags = new ArrayList<>(entry.getTagsSorted());
      wbvwContent.setVisibility(View.GONE);
      contentHtmlEditor.setVisibility(View.VISIBLE);
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
        rlydEntryAbstract.setVisibility(View.GONE);
      }

      setTextViewEditEntryTags(entryEditedTags);
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.activity_edit_entry_menu, menu);

    if(Application.getPlatformConfiguration().hasCaptureDevice() || Application.getContentExtractorManager().hasOcrContentExtractors()) {
      if(contentHtmlEditor != null && contentHtmlEditor.getVisibility() == View.VISIBLE) {
        MenuItem mnitmActionAddContentFromOcr = menu.findItem(R.id.mnitmActionAddImageOrOCRText);
        mnitmActionAddContentFromOcr.setVisible(true);
      }
    }

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
    else if (id == R.id.mnitmActionSave) {
      saveEntryAndCloseActivity();
      return true;
    }
    else if (id == R.id.mnitmActionAddImageOrOCRText) {
      insertImageOrRecognizedTextHelper.addImageOrOcrTextToHtmlEditor(contentHtmlEditor);
      return true;
    }

    return super.onOptionsItemSelected(item);
  }


  protected void setEntryHasBeenEdited() {
    hasEntryBeenEdited = true;
  }

  protected void unsetEntryHasBeenEdited() {
    hasEntryBeenEdited = false;
  }


  @Override
  public void onBackPressed() {
    if(isShowingEditEntryDialog) {
      editEntryDialog.onBackPressed();
    }
    else if(hasEntryBeenEdited == true)
      askUserIfChangesShouldBeSaved();
    else {
      cleanUp();
      super.onBackPressed();
    }
  }

  protected void askUserIfChangesShouldBeSaved() {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    TextView view = new TextView(this);
    view.setText(R.string.alert_dialog_entry_has_unsaved_changes_text);
    builder.setView(view);

    builder.setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {

      }
    });

    builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialogInterface, int i) {
        finish();
      }
    });

    builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialogInterface, int i) {
        saveEntryAndCloseActivity();
      }
    });

    builder.create().show();
  }

  protected void setAbstractPreviewFromHtml(String abstractHtml) {
    setAbstractPreview(Application.getHtmlHelper().extractPlainTextFromHtmlBody(abstractHtml));
  }

  protected void setAbstractPreview(String abstractPlainText) {
    txtvwEditEntryAbstract.setText(abstractPlainText);
  }

  protected void setContentHtml(String contentHtml) {
    if(contentHtml != null && contentHtmlEditor.getVisibility() == View.VISIBLE) {
      contentHtmlEditor.setHtml(contentHtml);
    }
    else {
      wbvwContent.loadDataWithBaseURL(null, contentHtml, "text/html; charset=utf-8", "utf-8", null); // otherwise non ASCII text doesn't get displayed correctly
    }
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

  protected void saveEntryAndCloseActivity() {
    // why do i run this little code on a new Thread? Getting HTML from AndroidHtmlEditor has to be done from a different one than main thread,
    // as async JavaScript response is dispatched to the main thread, therefore waiting for it as well on the main thread would block JavaScript response listener
    Application.getThreadPool().runTaskAsync(new Runnable() {
      @Override
      public void run() {
        saveEntryIfNeeded();

        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            finish();
          }
        });
      }
    });
  }

  @Override
  public void finish() {
    cleanUp();
    super.finish();
  }

  public void cleanUp() {
    if(contentHtmlEditor != null) {
      AndroidHtmlEditorPool.getInstance().htmlEditorReleased(contentHtmlEditor);
    }

    if(editEntryDialog != null) {
      editEntryDialog.cleanUp();
    }
  }

  protected void saveEntryIfNeeded() {
    if(hasEntryBeenEdited == true || (entryCreationResult != null && entry.isPersisted() == false)) {
      saveEntry();
    }
  }

  protected void saveEntryAsync() {
    new Thread(new Runnable() {
      @Override
      public void run() {
        saveEntry();
      }
    }).start();
  }

  protected void saveEntry() {
    String abstractHtml = (String)editedFields.get(FieldWithUnsavedChanges.EntryAbstract);
    if(abstractHtml != null) {
      entry.setAbstract(abstractHtml);
    }

    String contentHtml = (String)editedFields.get(FieldWithUnsavedChanges.EntryContent);
    if(hasContentBeenEditedInActivity) {
      contentHtml = contentHtmlEditor.getHtml();
    }
    if(contentHtml != null) {
      entry.setContent(contentHtml);
    }

    if(entryCreationResult != null) {
      entryCreationResult.saveCreatedEntities(abstractHtml, contentHtml);
    }

    if(entry.isPersisted() == false) { // a new Entry
      Application.getDeepThought().addEntry(entry); // otherwise entry.id would be null when adding to Tags below
    }

    // TODO: why setting Tags here and not above before saving?
    List<Tag> entryEditedTags = (List<Tag>)editedFields.get(FieldWithUnsavedChanges.EntryTags);
    if(entryEditedTags != null) {
      entry.setTags(entryEditedTags);
    }

    unsetEntryHasBeenEdited();
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
      editEntryDialog.setEntry(entry);

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

    if(hasContentBeenEditedInActivity) {
      entryFieldValues.remove(FieldWithUnsavedChanges.EntryContent);
      editEntryDialog.setCurrentEntryFieldValues(entryFieldValues);

      Application.getThreadPool().runTaskAsync(new Runnable() {
        @Override
        public void run() {
          // getHtml() has to be called on a other then main thread
          final String contentHtml = contentHtmlEditor.getHtml();
          hasContentBeenEditedInActivity = false;

          entryFieldValues.put(FieldWithUnsavedChanges.EntryContent, contentHtml);
          editedFields.put(FieldWithUnsavedChanges.EntryContent, contentHtml);

          EditEntryActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() { // don't know why but when calling over a new thread and than man thread again, Html Editors don't load
              editEntryDialog.updateContentHtml(contentHtml);
            }
          });
        }
      });
    }

    else {
      editEntryDialog.setCurrentEntryFieldValues(entryFieldValues);
    }
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


  protected View.OnClickListener rlydEntryAbstractOnClickListener = new View.OnClickListener() {
    @Override
    public void onClick(View v) {
      showEditEntryDialog(EditEntrySection.Abstract);
    }
  };

  protected View.OnClickListener rlydTagsOnClickListener = new View.OnClickListener() {
    @Override
    public void onClick(View v) {
      showEditEntryDialog(EditEntrySection.Tags);
    }
  };


  protected IHtmlEditorListener contentListener = new IHtmlEditorListener() {
    @Override
    public void editorHasLoaded(HtmlEditor editor) {

    }

    @Override
    public void htmlCodeUpdated() {
      hasContentBeenEditedInActivity = true;
      setEntryHasBeenEdited();
    }

    @Override
    public void htmlCodeHasBeenReset() {
      // Changes to Content have been undone
      // TODO: how to check now if Entry has been edited or not?
    }

    @Override
    public boolean handleCommand(HtmlEditor editor, HtmEditorCommand command) {
      return false;
    }

    @Override
    public boolean elementDoubleClicked(HtmlEditor editor, ImageElementData elementData) {
      return false;
    }
  };


  protected EditEntityListener editEntryListener = new EditEntityListener() {
    @Override
    public void entityEdited(BaseEntity entity, FieldWithUnsavedChanges changedField, Object newFieldValue) {
      setEntryHasBeenEdited();

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
    }
  };

  protected DialogListener editEntryDialogListener = new DialogListener() {
    @Override
    public void dialogBecameHidden() {
      isShowingEditEntryDialog = false;
    }
  };

}
