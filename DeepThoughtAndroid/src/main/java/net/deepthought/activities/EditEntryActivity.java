package net.deepthought.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import net.deepthought.Application;
import net.deepthought.R;
import net.deepthought.adapter.EntryTagsAdapter;
import net.deepthought.data.contentextractor.ocr.RecognizeTextListener;
import net.deepthought.data.contentextractor.ocr.TextRecognitionResult;
import net.deepthought.data.model.Entry;
import net.deepthought.data.model.Tag;
import net.deepthought.helper.AlertHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by ganymed on 01/10/14.
 */
public class EditEntryActivity extends AppCompatActivity {

  public final static String EntryArgumentKey = "EntryArgument";
  public final static int RequestCode = 1;
  public final static String ResultKey = "EntryResult";

  public final static int RecognizeTextFromCameraPhotoRequestCode = 2;

  private final static Logger log = LoggerFactory.getLogger(EditEntryActivity.class);


  protected Entry entry;
  protected List<Tag> entryTags = new ArrayList<>();

  protected EditText edtxtEditEntryAbstract;
  protected EditText edtxtEditEntryContent;

  protected RelativeLayout rlydTags;
  protected RelativeLayout rlydEditEntryEditTags;
  protected TextView txtvwEditEntryTags;
  protected EditText edtxtEditEntrySearchTag;
  protected ListView lstvwEditEntryTags;


  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setupUi();

    setEntryValues();
  }

  protected void setupUi() {
    try {
      setContentView(R.layout.activity_edit_entry);

      Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
      setSupportActionBar(toolbar);

      ActionBar actionBar = getSupportActionBar();
      if(actionBar != null) {
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
      }

      edtxtEditEntryAbstract = (EditText) findViewById(R.id.edtxtEditEntryAbstract);
      edtxtEditEntryContent = (EditText) findViewById(R.id.edtxtEditEntryContent);

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
    } catch(Exception ex) {
      log.error("Could not setup UI", ex);
      AlertHelper.showErrorMessage(this, getString(R.string.error_message_could_not_show_activity, ex.getLocalizedMessage()));
      finish();
    }
  }

  protected void setEntryValues() {
    entry = ActivityManager.getInstance().getEntryToBeEdited();

    if(entry != null) {
      entryTags = new ArrayList<>(entry.getTagsSorted());
      setTextViewEditEntryTags();

      edtxtEditEntryAbstract.setText(Html.fromHtml(entry.getAbstract()));
      edtxtEditEntryContent.setText(Html.fromHtml(entry.getContent()));
      lstvwEditEntryTags.setAdapter(new EntryTagsAdapter(this, entry, entryTags, new EntryTagsAdapter.EntryTagsChangedListener() {
        @Override
        public void entryTagsChanged(List<Tag> entryTags) {
          setTextViewEditEntryTags();
        }
      }));
    }
  }


  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.activity_edit_entry_menu, menu);

    if(Application.getContentExtractorManager().hasOcrContentExtractors()) {
      MenuItem mnitmActionAddContentFromOcr = menu.findItem(R.id.mnitmActionAddContentFromOcr);
      mnitmActionAddContentFromOcr.setVisible(true);

      mnitmActionAddContentFromOcr.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
          addContentFromOcr();
          return true;
        }
      });
    }

    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();

    if(id == android.R.id.home) {
      return ifHasUnsavedChangesAskUserToSave();
    }
    if (id == R.id.mnitmActionSaveEntry) {
      saveEntry();
      return true;
    }
    else if (id == R.id.mnitmActionAddContentFromOcr) {
      addContentFromOcr();
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onBackPressed() {
    ifHasUnsavedChangesAskUserToSave();
    super.onBackPressed();
  }

  protected boolean ifHasUnsavedChangesAskUserToSave() {
    // TODO:
    return false;
  }

  protected void setTextViewEditEntryTags() {
    String tags = "";

    for(Tag tag : entryTags)
      tags += tag.getName() + ", ";

    if(tags.length() > 1)
      tags = tags.substring(0, tags.length() - 2);

    if(tags.length() == 0)
      tags = getString(R.string.edit_entry_no_tags_set);

    txtvwEditEntryTags.setText(tags);
  }


  protected void addContentFromOcr() {
    if(Application.getContentExtractorManager().hasOcrContentExtractors() == false)
      return;

    Application.getContentExtractorManager().getPreferredOcrContentExtractor().captureImagesAndRecognizeTextAsync(new RecognizeTextListener() {
      @Override
      public void textRecognized(TextRecognitionResult result) {
        EditEntryActivity.this.textRecognized(result);
      }
    });
  }

  protected void textRecognized(TextRecognitionResult result) {
    try {
      if (result.isUserCancelled()) {
        // nothing to do, user knows that he/she cancelled capturing/recognition
      } else if (result.recognitionSuccessful() == false) {
        AlertHelper.showErrorMessage(this, result.getErrorMessage());
      } else {
        edtxtEditEntryContent.getText().insert(edtxtEditEntryContent.getSelectionEnd(), Html.fromHtml(result.getRecognizedText()));
      }
    } catch(Exception ex) {
      log.error("Could not handle TextRecognitionResult " + result, ex);
    }
  }

  protected View.OnClickListener btnOkOnClickListener = new View.OnClickListener() {
    @Override
    public void onClick(View v) {
      saveEntry();

      Intent resultIntent = new Intent();
      setResult(Activity.RESULT_OK, resultIntent);
      finish();
    }
  };

  protected void saveEntry() {
    entry.setAbstract(Html.toHtml(edtxtEditEntryAbstract.getText()));
    entry.setContent(Html.toHtml(edtxtEditEntryContent.getText()));

    entry.setTags(entryTags);

    if(entry.isPersisted() == false) // a new Entry
      Application.getDeepThought().addEntry(entry); // otherwise entry.id would be null when adding to Tags below
  }

  protected View.OnClickListener btnCancelOnClickListener = new View.OnClickListener() {
    @Override
    public void onClick(View v) {
      setResult(RESULT_CANCELED);
      finish();
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

    if(tagName == null || tagName.isEmpty())
      Toast.makeText(this, getString(R.string.error_message_tag_name_must_be_a_non_empty_string), Toast.LENGTH_LONG).show();
    else if(Application.getDeepThought().containsTagOfName(tagName))
      Toast.makeText(this, getString(R.string.error_message_tag_with_that_name_already_exists), Toast.LENGTH_LONG).show();
    else {
      Tag newTag = new Tag(tagName);
      Application.getDeepThought().addTag(newTag);
      entryTags.add(newTag);
      Collections.sort(entryTags);
      setTextViewEditEntryTags();
    }
  }
}
