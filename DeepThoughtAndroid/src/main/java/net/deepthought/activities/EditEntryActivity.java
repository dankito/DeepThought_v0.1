package net.deepthought.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import net.deepthought.Application;
import net.deepthought.R;
import net.deepthought.adapter.EntryTagsAdapter;
import net.deepthought.data.model.Entry;
import net.deepthought.data.model.Tag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by ganymed on 01/10/14.
 */
public class EditEntryActivity extends Activity {

  public final static String EntryArgumentKey = "EntryArgument";
  public final static int RequestCode = 1;
  public final static String ResultKey = "EntryResult";

  protected Entry entry;
  protected List<Tag> entryTags = new ArrayList<>();

  protected EditText edtxtEditEntryAbstract;
  protected EditText edtxtEditEntryText;

  protected RelativeLayout rlydTags;
  protected RelativeLayout rlydEditEntryEditTags;
  protected TextView txtvwEditEntryTags;
  protected EditText edtxtEditEntrySearchTag;
  protected ListView lstvwEditEntryTags;


  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_edit_entry);

    getActionBar().setTitle(getString(R.string.edit_entry_action_bar_title_new_entry));

    edtxtEditEntryAbstract = (EditText)findViewById(R.id.edtxtEditEntryAbstract);
    edtxtEditEntryText = (EditText)findViewById(R.id.edtxtEditEntryText);

    rlydTags = (RelativeLayout)findViewById(R.id.rlydTags);
    rlydTags.setOnClickListener(rlydTagsOnClickListener);

    rlydEditEntryEditTags = (RelativeLayout)findViewById(R.id.rlydEditEntryEditTags);
    rlydEditEntryEditTags.setVisibility(View.GONE);

    txtvwEditEntryTags = (TextView)findViewById(R.id.txtvwEditEntryTags);
    edtxtEditEntrySearchTag = (EditText)findViewById(R.id.edtxtEditEntrySearchTag);
    edtxtEditEntrySearchTag.addTextChangedListener(edtxtEditEntrySearchTagTextChangedListener);
    edtxtEditEntrySearchTag.setOnEditorActionListener(edtxtEditEntrySearchTagActionListener);

    Button btnEditEntryNewTag = (Button)findViewById(R.id.btnEditEntryNewTag);
    btnEditEntryNewTag.setOnClickListener(btnEditEntryNewTagOnClickListener);

    lstvwEditEntryTags = (ListView)findViewById(R.id.lstvwEditEntryTags);

    Button btnEditEntryOk = (Button)findViewById(R.id.btnEditEntryOk);
    btnEditEntryOk.setOnClickListener(btnOkOnClickListener);

    Button btnEditEntryCancel = (Button)findViewById(R.id.btnEditEntryCancel);
    btnEditEntryCancel.setOnClickListener(btnCancelOnClickListener);

    entry = ActivityManager.getInstance().getEntryToBeEdited();

    if(entry != null) {
      entryTags = new ArrayList<>(entry.getTagsSorted());
      setTextViewEditEntryTags();

      edtxtEditEntryAbstract.setText(entry.getAbstract());
      edtxtEditEntryText.setText(entry.getContent());
      lstvwEditEntryTags.setAdapter(new EntryTagsAdapter(this, entry, entryTags, new EntryTagsAdapter.EntryTagsChangedListener() {
        @Override
        public void entryTagsChanged(List<Tag> entryTags) {
          setTextViewEditEntryTags();
        }
      }));

      if(entry.isPersisted()) // not a new entry, entry is already persisted in db
        getActionBar().setTitle(getString(R.string.edit_entry_action_bar_title_entry_format, entry.getPreview()));
    }
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

  protected View.OnClickListener btnOkOnClickListener = new View.OnClickListener() {
    @Override
    public void onClick(View v) {
      Intent resultIntent = new Intent();
      entry.setAbstract(edtxtEditEntryAbstract.getText().toString());
      entry.setContent(edtxtEditEntryText.getText().toString());

      if(entry.isPersisted() == false) // a new Entry
        Application.getDeepThought().addEntry(entry); // otherwise entry.id would be null when adding to Tags below

      entry.setTags(entryTags);

      setResult(Activity.RESULT_OK, resultIntent);
      finish();
    }
  };

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
