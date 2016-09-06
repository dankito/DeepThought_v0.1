package net.dankito.deepthought.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import net.dankito.deepthought.Application;
import net.dankito.deepthought.R;
import net.dankito.deepthought.adapter.EntryTagsAdapter;
import net.dankito.deepthought.controls.ICleanUp;
import net.dankito.deepthought.controls.html.AndroidHtmlEditor;
import net.dankito.deepthought.controls.html.AndroidHtmlEditorPool;
import net.dankito.deepthought.controls.html.HtmEditorCommand;
import net.dankito.deepthought.controls.html.HtmlEditor;
import net.dankito.deepthought.controls.html.IHtmlEditorListener;
import net.dankito.deepthought.data.html.ImageElementData;
import net.dankito.deepthought.data.model.Entry;
import net.dankito.deepthought.data.model.Tag;
import net.dankito.deepthought.listener.EditEntityListener;
import net.dankito.deepthought.ui.enums.FieldWithUnsavedChanges;
import net.dankito.deepthought.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by ganymed on 06/09/16.
 */
public class EditEntryDialog extends DialogFragment implements ICleanUp {


  protected Entry entry;

  protected RelativeLayout rlytEditAbstract;

  protected RelativeLayout rlytEditContent;

  protected RelativeLayout rlytEditTags;

  protected AndroidHtmlEditor abstractHtmlEditor = null;
  protected AndroidHtmlEditor contentHtmlEditor = null;

  protected EditText edtxtEditEntrySearchTag = null;

  protected ListView lstvwEditEntryTags = null;

  protected List<Tag> entryEditedTags = new ArrayList<>();

  protected EditEntityListener listener = null;


  public EditEntryDialog() {

  }

  public void setEntry(Entry entry) {
    this.entry = entry;
  }

  public void setEditEntityListener(EditEntityListener listener) {
    this.listener = listener;
  }


  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.dialog_edit_entry, container, false);

    setupToolbar(rootView);

    setHasOptionsMenu(true);


    setupEditAbstractRegion(rootView);

    setupEditContentRegion(rootView);

    setupEditTagsRegion(rootView);


    setEntryValues(entry);

    return rootView;
  }

  @NonNull
  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    Dialog dialog = super.onCreateDialog(savedInstanceState);
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
    return dialog;
  }


  protected void setupToolbar(View rootView) {
    Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
    toolbar.setTitle("");

    ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

    ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
    if (actionBar != null) {
      actionBar.setDisplayHomeAsUpEnabled(true);
      actionBar.setHomeButtonEnabled(true);
      actionBar.setHomeAsUpIndicator(android.R.drawable.ic_menu_close_clear_cancel);
    }
  }

  protected void setupEditAbstractRegion(View rootView) {
    rlytEditAbstract = (RelativeLayout)rootView.findViewById(R.id.rlytEditAbstract);

    abstractHtmlEditor = AndroidHtmlEditorPool.getInstance().getHtmlEditor(getActivity(), abstractListener);

    rlytEditAbstract.addView(abstractHtmlEditor, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 500));

    RelativeLayout.LayoutParams abstractEditorParams = (RelativeLayout.LayoutParams)abstractHtmlEditor.getLayoutParams();
    abstractEditorParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
    abstractEditorParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
    abstractEditorParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
    abstractEditorParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

    abstractHtmlEditor.setLayoutParams(abstractEditorParams);

  }

  protected void setupEditContentRegion(View rootView) {
    rlytEditContent = (RelativeLayout)rootView.findViewById(R.id.rlytEditContent);

    contentHtmlEditor = AndroidHtmlEditorPool.getInstance().getHtmlEditor(getActivity(), contentListener);

    rlytEditContent.addView(contentHtmlEditor, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

    RelativeLayout.LayoutParams contentEditorParams = (RelativeLayout.LayoutParams)contentHtmlEditor.getLayoutParams();
    contentEditorParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
    contentEditorParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
    contentEditorParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
    contentEditorParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

    contentHtmlEditor.setLayoutParams(contentEditorParams);
  }

  protected void setupEditTagsRegion(View rootView) {
    rlytEditTags = (RelativeLayout)rootView.findViewById(R.id.rlytEditTags);

    edtxtEditEntrySearchTag = (EditText)rootView.findViewById(R.id.edtxtEditEntrySearchTag);
    edtxtEditEntrySearchTag.addTextChangedListener(edtxtEditEntrySearchTagTextChangedListener);
    edtxtEditEntrySearchTag.setOnEditorActionListener(edtxtEditEntrySearchTagActionListener);

    Button btnEditEntryNewTag = (Button)rootView.findViewById(R.id.btnEditEntryNewTag);
    btnEditEntryNewTag.setOnClickListener(btnEditEntryNewTagOnClickListener);

    lstvwEditEntryTags = (ListView)rootView.findViewById(R.id.lstvwEditEntryTags);
  }


  protected void setEntryValues(Entry entry) {
    if(entry != null) {
      abstractHtmlEditor.setHtml(entry.getAbstract());

      contentHtmlEditor.setHtml(entry.getContent());

      entryEditedTags = new ArrayList<>(entry.getTagsSorted());

      lstvwEditEntryTags.setAdapter(new EntryTagsAdapter(getActivity(), entry, entryEditedTags, new EntryTagsAdapter.EntryTagsChangedListener() {
        @Override
        public void entryTagsChanged(List<Tag> entryTags) {
          setEntryHasBeenEdited(FieldWithUnsavedChanges.EntryTags, entryTags);
        }
      }));
    }
  }

  protected void setEntryHasBeenEdited(FieldWithUnsavedChanges editedField, Object editedFieldValue) {
    if(listener != null) {
      listener.entityEdited(entry, editedField, editedFieldValue);
    }
  }


  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    menu.clear();
    inflater.inflate(R.menu.dialog_edit_entry_menu, menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();

    if(id == R.id.mnitmActionEditAbstract) {
      showEditAbstract();
    }
    else if(id == R.id.mnitmActionEditContent) {
      showEditContent();
    }
    else if(id == R.id.mnitmActionEditTags) {
      showEditTags();
    }
    else if (id == R.id.action_save) {
      // handle confirmation button click here
      return true;
    }
    else if (id == android.R.id.home) {
      // is handled in EditEntryActivity

      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  protected void showEditAbstract() {
    rlytEditAbstract.setVisibility(View.VISIBLE);
    rlytEditContent.setVisibility(View.GONE);
    rlytEditTags.setVisibility(View.GONE);
  }

  protected void showEditContent() {
    rlytEditAbstract.setVisibility(View.GONE);
    rlytEditContent.setVisibility(View.VISIBLE);
    rlytEditTags.setVisibility(View.GONE);
  }

  protected void showEditTags() {
    rlytEditAbstract.setVisibility(View.GONE);
    rlytEditContent.setVisibility(View.GONE);
    rlytEditTags.setVisibility(View.VISIBLE);
  }


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
      Toast.makeText(getActivity(), getString(R.string.error_message_tag_name_must_be_a_non_empty_string), Toast.LENGTH_LONG).show();
    else if(Application.getDeepThought().containsTagOfName(tagName))
      Toast.makeText(getActivity(), getString(R.string.error_message_tag_with_that_name_already_exists), Toast.LENGTH_LONG).show();
    else {
      Tag newTag = new Tag(tagName);
      Application.getDeepThought().addTag(newTag);
      entryEditedTags.add(newTag);
      Collections.sort(entryEditedTags);
      setEntryHasBeenEdited(FieldWithUnsavedChanges.EntryTags, entryEditedTags);
    }
  }


  protected IHtmlEditorListener abstractListener = new IHtmlEditorListener() {
    @Override
    public void editorHasLoaded(HtmlEditor editor) {

    }

    @Override
    public void htmlCodeUpdated() {
      setEntryHasBeenEdited(FieldWithUnsavedChanges.EntryAbstract, null);
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
      setEntryHasBeenEdited(FieldWithUnsavedChanges.EntryContent, null);
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

  @Override
  public void cleanUp() {
    AndroidHtmlEditorPool.getInstance().htmlEditorReleased(abstractHtmlEditor);
    AndroidHtmlEditorPool.getInstance().htmlEditorReleased(contentHtmlEditor);

    if(lstvwEditEntryTags.getAdapter() instanceof EntryTagsAdapter) {
      ((EntryTagsAdapter) lstvwEditEntryTags.getAdapter()).cleanUp();
    }
  }
}
