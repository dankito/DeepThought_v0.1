package net.deepthought.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import net.deepthought.AndroidHelper;
import net.deepthought.Application;
import net.deepthought.R;
import net.deepthought.adapter.EntryTagsAdapter;
import net.deepthought.communication.model.DoOcrConfiguration;
import net.deepthought.communication.model.OcrSource;
import net.deepthought.controls.ICleanUp;
import net.deepthought.controls.html.AndroidHtmlEditor;
import net.deepthought.controls.html.AndroidHtmlEditorPool;
import net.deepthought.controls.html.HtmEditorCommand;
import net.deepthought.controls.html.HtmlEditor;
import net.deepthought.controls.html.IHtmlEditorListener;
import net.deepthought.data.contentextractor.EntryCreationResult;
import net.deepthought.data.contentextractor.ocr.RecognizeTextListener;
import net.deepthought.data.contentextractor.ocr.TextRecognitionResult;
import net.deepthought.data.html.ImageElementData;
import net.deepthought.data.model.Entry;
import net.deepthought.data.model.FileLink;
import net.deepthought.data.model.Tag;
import net.deepthought.helper.AlertHelper;
import net.deepthought.util.StringUtils;
import net.deepthought.util.file.FileUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by ganymed on 01/10/14.
 */
public class EditEntryActivity extends AppCompatActivity implements ICleanUp {

  public final static String EntryArgumentKey = "EntryArgument";
  public final static int RequestCode = 1;
  public final static String ResultKey = "EntryResult";

  public final static int TakePhotoRequestCode = 3;

  public final static int RecognizeTextFromCameraPhotoRequestCode = 2;

  private final static Logger log = LoggerFactory.getLogger(EditEntryActivity.class);


  protected Entry entry = null;
  protected EntryCreationResult entryCreationResult = null;
  protected List<Tag> entryEditedTags = new ArrayList<>();

  protected TextView txtvwEditEntryAbstract;
  protected RelativeLayout rlydEntryAbstract;

  protected RelativeLayout rlydTags;
  protected RelativeLayout rlydEditEntryEditTags;
  protected TextView txtvwEditEntryTags;
  protected EditText edtxtEditEntrySearchTag;
  protected ListView lstvwEditEntryTags;

  protected AndroidHtmlEditor abstractHtmlEditor = null;
  protected AndroidHtmlEditor contentHtmlEditor = null;

  protected boolean hasEntryBeenEdited = false;

  protected FileLink takenPhotoTempFile = null;


  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setupUi();

    setEntryValues();

    unsetEntryHasBeenEdited();
  }

  protected void setupUi() {
    try {
      setContentView(R.layout.activity_edit_entry);

      Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
      setSupportActionBar(toolbar);
      setLogo(toolbar);

      ActionBar actionBar = getSupportActionBar();
      if(actionBar != null) {
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
      }

      contentHtmlEditor = AndroidHtmlEditorPool.getInstance().getHtmlEditor(this, contentListener);
      abstractHtmlEditor = AndroidHtmlEditorPool.getInstance().getHtmlEditor(this, abstractListener);

      txtvwEditEntryAbstract = (TextView) findViewById(R.id.txtvwEntryAbstractPreview);

      rlydEntryAbstract = (RelativeLayout)findViewById(R.id.rlydEntryAbstract);
      rlydEntryAbstract.setOnClickListener(rlydEntryAbstractOnClickListener);
      rlydEntryAbstract.addView(abstractHtmlEditor, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 500));

      RelativeLayout.LayoutParams abstractEditorParams = (RelativeLayout.LayoutParams)abstractHtmlEditor.getLayoutParams();
      abstractEditorParams.addRule(RelativeLayout.BELOW, R.id.txtvwEditEntryAbstractLabel);
      abstractEditorParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
      abstractEditorParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
      abstractEditorParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

      abstractHtmlEditor.setLayoutParams(abstractEditorParams);
      abstractHtmlEditor.setVisibility(View.GONE);

      rlydTags = (RelativeLayout) findViewById(R.id.rlydTags);
      rlydTags.setOnClickListener(rlydTagsOnClickListener);

      rlydEditEntryEditTags = (RelativeLayout) findViewById(R.id.rlydEditEntryEditTags);
      rlydEditEntryEditTags.setVisibility(View.GONE);

      txtvwEditEntryTags = (TextView) findViewById(R.id.txtvwEditEntryTags);
      edtxtEditEntrySearchTag = (EditText) findViewById(R.id.edtxtEditEntrySearchTag);
      edtxtEditEntrySearchTag.addTextChangedListener(edtxtEditEntrySearchTagTextChangedListener);
      edtxtEditEntrySearchTag.setOnEditorActionListener(edtxtEditEntrySearchTagActionListener);

      Button btnEditEntryNewTag = (Button) findViewById(R.id.btnEditEntryNewTag);
      btnEditEntryNewTag.setOnClickListener(btnEditEntryNewTagOnClickListener);

      lstvwEditEntryTags = (ListView) findViewById(R.id.lstvwEditEntryTags);

      RelativeLayout rlydContent = (RelativeLayout)findViewById(R.id.rlydContent);
      contentHtmlEditor.setVisibility(View.VISIBLE);
      rlydContent.addView(contentHtmlEditor, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

      RelativeLayout.LayoutParams contentEditorParams = (RelativeLayout.LayoutParams)contentHtmlEditor.getLayoutParams();
      contentEditorParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
      contentEditorParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
      contentEditorParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
      contentEditorParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

      contentHtmlEditor.setLayoutParams(contentEditorParams);
    } catch(Exception ex) {
      log.error("Could not setup UI", ex);
      AlertHelper.showErrorMessage(this, getString(R.string.error_message_could_not_show_activity, ex.getLocalizedMessage()));
      finish();
    }
  }

  protected void setLogo(Toolbar toolbar) {
    ImageView logoView = new ImageView(this);
    logoView.setImageResource(android.R.drawable.ic_menu_save);
    toolbar.addView(logoView);

    logoView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        saveEntryAndCloseActivity();
      }
    });
  }

  protected boolean hasAbstractManuallyBeenChanged = false;
  protected boolean hasContentManuallyBeenChanged = false;

  protected void setEntryValues() {
    entry = ActivityManager.getInstance().getEntryToBeEdited();
    if(entry != null) {
      entryEditedTags = new ArrayList<>(entry.getTagsSorted());
    }

    entryCreationResult = ActivityManager.getInstance().getEntryCreationResultToBeEdited();
    if(entryCreationResult != null) {
      entry = entryCreationResult.getCreatedEntry();
      entryEditedTags = entryCreationResult.getTags();
    }

    if(entry != null) {
      txtvwEditEntryAbstract.setText(entry.getAbstractAsPlainText());
      hasAbstractManuallyBeenChanged = true;
      abstractHtmlEditor.setHtml(entry.getAbstract());

      hasContentManuallyBeenChanged = true;
      contentHtmlEditor.setHtml(entry.getContent());

      lstvwEditEntryTags.setAdapter(new EntryTagsAdapter(this, entry, entryEditedTags, new EntryTagsAdapter.EntryTagsChangedListener() {
        @Override
        public void entryTagsChanged(List<Tag> entryTags) {
          setEntryHasBeenEdited();
          setTextViewEditEntryTags();
        }
      }));

      setTextViewEditEntryTags();
    }
  }

  @Override
  protected void onDestroy() {
    if(lstvwEditEntryTags.getAdapter() instanceof EntryTagsAdapter)
      ((EntryTagsAdapter)lstvwEditEntryTags.getAdapter()).cleanUp();

    super.onDestroy();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.activity_edit_entry_menu, menu);

    if(Application.getContentExtractorManager().hasOcrContentExtractors()) {
      MenuItem mnitmActionAddContentFromOcr = menu.findItem(R.id.mnitmActionAddContentFromOcr);
      mnitmActionAddContentFromOcr.setVisible(true);
    }

    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();

    if(id == android.R.id.home) {
      saveEntryAndCloseActivity();
      return true;
    }
    else if (id == R.id.mnitmActionCancel) {
      finish();
      return true;
    }
    else if (id == R.id.mnitmActionAddContentFromOcr) {
      addContentFromOcr();
//      insertPhotoFromCamera();
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
    if(hasEntryBeenEdited == true)
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

  protected void setTextViewEditEntryTags() {
    String tags = "";

    for(Tag tag : entryEditedTags)
      tags += tag.getName() + ", ";

    if(tags.length() > 1)
      tags = tags.substring(0, tags.length() - 2);

    if(tags.length() == 0)
      tags = getString(R.string.edit_entry_no_tags_set);

    txtvwEditEntryTags.setText(tags);
  }


  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == TakePhotoRequestCode && resultCode == RESULT_OK) {
      if(takenPhotoTempFile != null) {
        FileLink imageFile = FileUtils.moveFileToCapturedImagesFolder(takenPhotoTempFile);
        Application.getDeepThought().addFile(imageFile);
        ImageElementData imageData = new ImageElementData(imageFile);
        contentHtmlEditor.insertHtml(imageData.createHtmlCode());
      }
    }

    takenPhotoTempFile = null;
  }

  protected void insertPhotoFromCamera() {
    takenPhotoTempFile = AndroidHelper.takePhoto(this, TakePhotoRequestCode);
  }

  protected void addContentFromOcr() {
    if(Application.getContentExtractorManager().hasOcrContentExtractors() == false) {
      return;
    }

    Application.getContentExtractorManager().getPreferredOcrContentExtractor().recognizeTextAsync(new DoOcrConfiguration(OcrSource.CaptureImage), new RecognizeTextListener() {
      @Override
      public void textRecognized(TextRecognitionResult result) {
        EditEntryActivity.this.textRecognized(result);
      }
    });
  }
  protected void textRecognized(TextRecognitionResult result) {
    try {
      if (result.isUserCancelled() || (result.isDone() && result.getRecognizedText() == null)) {
        // nothing to do, user knows that he/she cancelled capturing/recognition
      }
      else if (result.recognitionSuccessful() == false) {
        AlertHelper.showErrorMessage(this, result.getErrorMessage());
      } else {
        contentHtmlEditor.setHtml(contentHtmlEditor.getHtml() + result.getRecognizedText()); // TODO: insert at cursor position
      }
    } catch(Exception ex) {
      log.error("Could not handle TextRecognitionResult " + result, ex);
    }
  }

  protected void saveEntryAndCloseActivity() {
    new Thread(new Runnable() {
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
    }).start();
  }

  @Override
  public void finish() {
    cleanUp();
    super.finish();
  }

  public void cleanUp() {
    AndroidHtmlEditorPool.getInstance().htmlEditorReleased(abstractHtmlEditor);
    AndroidHtmlEditorPool.getInstance().htmlEditorReleased(contentHtmlEditor);
  }

  protected void saveEntryIfNeeded() {
    if(hasEntryBeenEdited == true || (entryCreationResult != null && entry.isPersisted() == false))
      saveEntry();
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
    // TODO: only save changed fields
    String abstractString = abstractHtmlEditor.getHtml();
    String content = contentHtmlEditor.getHtml();

    entry.setAbstract(abstractString);
    entry.setContent(content);

    if(entryCreationResult != null) {
      entryCreationResult.saveCreatedEntities(abstractString, content);
    }

    if(entry.isPersisted() == false) // a new Entry
      Application.getDeepThought().addEntry(entry); // otherwise entry.id would be null when adding to Tags below

    entry.setTags(entryEditedTags);

    unsetEntryHasBeenEdited();
  }


  protected View.OnClickListener rlydEntryAbstractOnClickListener = new View.OnClickListener() {
    @Override
    public void onClick(View v) {
      if(abstractHtmlEditor.getVisibility() == View.GONE)
        abstractHtmlEditor.setVisibility(View.VISIBLE);
      else
        abstractHtmlEditor.setVisibility(View.GONE);
    }
  };

  protected View.OnClickListener rlydTagsOnClickListener = new View.OnClickListener() {
    @Override
    public void onClick(View v) {
      if(rlydEditEntryEditTags.getVisibility() == View.GONE)
        rlydEditEntryEditTags.setVisibility(View.VISIBLE);
      else
        rlydEditEntryEditTags.setVisibility(View.GONE);
    }
  };

  protected TextWatcher edtxtEditEntrySearchTagTextChangedListener = new TextWatcher() {
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
      ((EntryTagsAdapter) lstvwEditEntryTags.getAdapter()).getFilter().filter(s.toString());
    }
  };

  protected TextView.OnEditorActionListener edtxtEditEntrySearchTagActionListener = new TextView.OnEditorActionListener() {
    @Override
    public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
      if (actionId == EditorInfo.IME_NULL
          && keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
        createNewTag();
        return true;
      }
      return false;
    }
  };


  protected View.OnClickListener btnEditEntryNewTagOnClickListener = new View.OnClickListener() {
    @Override
    public void onClick(View v) {
      createNewTag();
    }
  };

  protected void createNewTag() {
    String tagName = edtxtEditEntrySearchTag.getText().toString();

    if(StringUtils.isNullOrEmpty(tagName))
      Toast.makeText(this, getString(R.string.error_message_tag_name_must_be_a_non_empty_string), Toast.LENGTH_LONG).show();
    else if(Application.getDeepThought().containsTagOfName(tagName))
      Toast.makeText(this, getString(R.string.error_message_tag_with_that_name_already_exists), Toast.LENGTH_LONG).show();
    else {
      Tag newTag = new Tag(tagName);
      Application.getDeepThought().addTag(newTag);
      entryEditedTags.add(newTag);
      setEntryHasBeenEdited();
      Collections.sort(entryEditedTags);
      setTextViewEditEntryTags();
    }
  }


  protected IHtmlEditorListener abstractListener = new IHtmlEditorListener() {
    @Override
    public void editorHasLoaded(HtmlEditor editor) {

    }

    @Override
    public void htmlCodeUpdated() {
      if(hasAbstractManuallyBeenChanged == false)
        setEntryHasBeenEdited();
      else
        hasAbstractManuallyBeenChanged = false; // reset
    }

    @Override
    public void htmlCodeHasBeenReset() {
      // Changes to Abstract have been undone
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

  protected IHtmlEditorListener contentListener = new IHtmlEditorListener() {
    @Override
    public void editorHasLoaded(HtmlEditor editor) {

    }

    @Override
    public void htmlCodeUpdated() {
      if(hasContentManuallyBeenChanged == false)
        setEntryHasBeenEdited();
      else
        hasContentManuallyBeenChanged = false; // reset
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
}
