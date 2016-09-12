package net.dankito.deepthought.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
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
import android.widget.ImageButton;
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
import net.dankito.deepthought.data.contentextractor.EntryCreationResult;
import net.dankito.deepthought.data.html.ImageElementData;
import net.dankito.deepthought.data.model.Entry;
import net.dankito.deepthought.data.model.Tag;
import net.dankito.deepthought.dialogs.enums.EditEntrySection;
import net.dankito.deepthought.listener.DialogListener;
import net.dankito.deepthought.listener.EditEntityListener;
import net.dankito.deepthought.ui.enums.FieldWithUnsavedChanges;
import net.dankito.deepthought.util.InsertImageOrRecognizedTextHelper;
import net.dankito.deepthought.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by ganymed on 06/09/16.
 */
public class EditEntryDialog extends DialogFragment implements ICleanUp {


  protected Entry entry;

  protected EntryCreationResult entryCreationResult = null;

  protected List<FieldWithUnsavedChanges> editedFields = new ArrayList<>();

  protected RelativeLayout rlytEditAbstract;

  protected RelativeLayout rlytEditContent;

  protected RelativeLayout rlytEditTags;

  protected AndroidHtmlEditor abstractHtmlEditor = null;
  protected AndroidHtmlEditor contentHtmlEditor = null;

  protected EditText edtxtEditEntrySearchTag = null;

  protected ListView lstvwEditEntryTags = null;

  protected List<Tag> entryEditedTags = new ArrayList<>();

  protected boolean hasViewBeenCreated = false;

  protected EditEntrySection sectionToEditAfterLoading = null;

  protected MenuItem mnitmActionTakePhotoOrRecognizeText = null;

  protected InsertImageOrRecognizedTextHelper insertImageOrRecognizedTextHelper;

  protected boolean cleanUpOnClose = false;

  protected boolean hasDialogPreviouslyBeenShown = false;

  protected EditEntityListener editEntityListener = null;

  protected DialogListener dialogListener = null;


  public EditEntryDialog() {

  }


  public void setEntry(Entry entry) {
    this.entry = entry;
  }

  public void setEntryCreationResult(EntryCreationResult entryCreationResult) {
    this.entryCreationResult = entryCreationResult;
  }

  public void setCleanUpOnClose(boolean cleanUpOnClose) {
    this.cleanUpOnClose = cleanUpOnClose;
  }

  public void setEditEntityListener(EditEntityListener listener) {
    this.editEntityListener = listener;
  }

  public void setDialogListener(DialogListener dialogListener) {
    this.dialogListener = dialogListener;
  }

  public void setInsertImageOrRecognizedTextHelper(InsertImageOrRecognizedTextHelper insertImageOrRecognizedTextHelper) {
    this.insertImageOrRecognizedTextHelper = insertImageOrRecognizedTextHelper;
  }

  public void setSectionToEdit(EditEntrySection section) {
    if(hasViewBeenCreated) {
      applySectionToEdit(section);
    }
    else {
      sectionToEditAfterLoading = section;
    }
  }

  protected void applySectionToEdit(EditEntrySection section) {
    if(section == EditEntrySection.Abstract) {
      showEditAbstractSection();
    }
    else if(section == EditEntrySection.Content) {
      showEditContentSection();
    }
    else if(section == EditEntrySection.Tags) {
      showEditTagsSection();
    }
  }


  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.dialog_edit_entry, container, false);

    setupToolbar(rootView);

    setHasOptionsMenu(true);


    setupEditAbstractSection(rootView);

    setupEditContentSection(rootView);

    setupEditTagsSection(rootView);


    hasViewBeenCreated = true;

    if(sectionToEditAfterLoading != null) {
      applySectionToEdit(sectionToEditAfterLoading);
      sectionToEditAfterLoading = null;
    }

    if(entry != null) {
      setEntryFieldValues(entry);
    }

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

      actionBar.setDisplayShowCustomEnabled(true);
      actionBar.setCustomView(R.layout.dialog_edit_entry_custom_action_bar_view);

      ImageButton imgbtnShowEditAbstractSection = (ImageButton)rootView.findViewById(R.id.imgbtnShowEditAbstractSection);
      imgbtnShowEditAbstractSection.setOnClickListener(imgbtnShowEditAbstractSectionClickListener);

      ImageButton imgbtnShowEditContentSection = (ImageButton)rootView.findViewById(R.id.imgbtnShowEditContentSection);
      imgbtnShowEditContentSection.setOnClickListener(imgbtnShowEditContentSectionClickListener);

      ImageButton imgbtnShowEditTagsSection = (ImageButton)rootView.findViewById(R.id.imgbtnShowEditTagsSection);
      imgbtnShowEditTagsSection.setOnClickListener(imgbtnShowEditTagsSectionClickListener);
    }
  }

  protected void setupEditAbstractSection(View rootView) {
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

  protected void setupEditContentSection(View rootView) {
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

  protected void setupEditTagsSection(View rootView) {
    rlytEditTags = (RelativeLayout)rootView.findViewById(R.id.rlytEditTags);

    edtxtEditEntrySearchTag = (EditText)rootView.findViewById(R.id.edtxtEditEntrySearchTag);
    edtxtEditEntrySearchTag.addTextChangedListener(edtxtEditEntrySearchTagTextChangedListener);
    edtxtEditEntrySearchTag.setOnEditorActionListener(edtxtEditEntrySearchTagActionListener);

    Button btnEditEntryNewTag = (Button)rootView.findViewById(R.id.btnEditEntryNewTag);
    btnEditEntryNewTag.setOnClickListener(btnEditEntryNewTagOnClickListener);

    lstvwEditEntryTags = (ListView)rootView.findViewById(R.id.lstvwEditEntryTags);
  }


  protected void setEntryFieldValues(Entry entry) {
    if(entry.hasAbstract()) {
      abstractHtmlEditor.setHtml(entry.getAbstract());
    }

    contentHtmlEditor.setHtml(entry.getContent());

    entryEditedTags = new ArrayList<>(entry.getTags());

    lstvwEditEntryTags.setAdapter(new EntryTagsAdapter(getActivity(), this.entry, entryEditedTags, new EntryTagsAdapter.EntryTagsChangedListener() {
      @Override
      public void entryTagsChanged(List<Tag> entryTags) {
        setEntryHasBeenEdited(FieldWithUnsavedChanges.EntryTags, entryTags);
      }
    }));
  }

  protected void setEntryHasBeenEdited(FieldWithUnsavedChanges editedField, Object editedFieldValue) {
    editedFields.add(editedField);
  }

  protected void unsetEntryHasBeenEdited() {
    editedFields.clear();
  }


  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    menu.clear();
    inflater.inflate(R.menu.dialog_edit_entry_menu, menu);

    mnitmActionTakePhotoOrRecognizeText = menu.findItem(R.id.mnitmActionTakePhotoOrRecognizeText);
    setActionTakePhotoOrRecognizeTextVisibility();
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();

    if(id == R.id.mnitmActionSaveEditedFields) {
      saveEntryAndCloseDialog();
      return true;
    }
    else if(id == R.id.mnitmActionTakePhotoOrRecognizeText) {
      handleTakePhotoOrRecognizeText();
      return true;
    }
    else if (id == android.R.id.home) {
      checkForUnsavedChangesAndCloseDialog();
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  protected void handleTakePhotoOrRecognizeText() {
    if(isEditAbstractSectionVisible()) {
      insertImageOrRecognizedTextHelper.addImageOrOcrTextToHtmlEditor(abstractHtmlEditor);
    }
    else if(isEditContentSectionVisible()) {
      insertImageOrRecognizedTextHelper.addImageOrOcrTextToHtmlEditor(contentHtmlEditor);
    }
  }


  protected boolean isEditAbstractSectionVisible() {
    return rlytEditAbstract.getVisibility() == View.VISIBLE;
  }

  protected boolean isEditContentSectionVisible() {
    return rlytEditContent.getVisibility() == View.VISIBLE;
  }

  protected boolean isEditTagsSectionVisible() {
    return rlytEditTags.getVisibility() == View.VISIBLE;
  }

  protected void showEditAbstractSection() {
    rlytEditAbstract.setVisibility(View.VISIBLE);
    rlytEditContent.setVisibility(View.GONE);
    rlytEditTags.setVisibility(View.GONE);

    setActionTakePhotoOrRecognizeTextVisibility(true);
  }

  protected void showEditContentSection() {
    rlytEditAbstract.setVisibility(View.GONE);
    rlytEditContent.setVisibility(View.VISIBLE);
    rlytEditTags.setVisibility(View.GONE);

    setActionTakePhotoOrRecognizeTextVisibility(true);
  }

  protected void showEditTagsSection() {
    rlytEditAbstract.setVisibility(View.GONE);
    rlytEditContent.setVisibility(View.GONE);
    rlytEditTags.setVisibility(View.VISIBLE);

    setActionTakePhotoOrRecognizeTextVisibility(false);
  }

  protected void setActionTakePhotoOrRecognizeTextVisibility() {
    if(isEditAbstractSectionVisible() || isEditContentSectionVisible()) {
      setActionTakePhotoOrRecognizeTextVisibility(true);
    }
    else {
      setActionTakePhotoOrRecognizeTextVisibility(false);
    }
  }

  protected void setActionTakePhotoOrRecognizeTextVisibility(boolean show) {
    if(mnitmActionTakePhotoOrRecognizeText != null) {
      mnitmActionTakePhotoOrRecognizeText.setVisible(show);
    }
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


  public void onBackPressed() {
    checkForUnsavedChangesAndCloseDialog();
  }

  protected void checkForUnsavedChangesAndCloseDialog() {
    if(hasUnsavedChanges() == true) {
      askUserIfChangesShouldBeSaved();
    }
    else {
      closeDialog();
    }
  }

  protected boolean hasUnsavedChanges() {
    return editedFields.size() > 0;
  }



  public void showDialog(AppCompatActivity activity, EditEntrySection sectionToEdit) {
    FragmentManager fragmentManager = activity.getSupportFragmentManager();
    FragmentTransaction transaction = fragmentManager.beginTransaction();
    transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);

    if(hasDialogPreviouslyBeenShown == false) { // on first display create EditEntryDialog and add it to transaction
      transaction.add(android.R.id.content, this);
    }
    else { // on subsequent displays we only have to call show() on the then hidden Dialog
      transaction.show(this);
    }

    transaction.commit();

    hasDialogPreviouslyBeenShown = true;

    setSectionToEdit(sectionToEdit);

    if(hasViewBeenCreated) {
      setEntryFieldValues(entry);
    }
  }

  protected void hideDialog() {
    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
    FragmentTransaction transaction = fragmentManager.beginTransaction();
    transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
    transaction.hide(this);
    transaction.commit();

    callDialogBecameHiddenListener();
  }

  protected void callDialogBecameHiddenListener() {
    if(dialogListener != null) {
      dialogListener.dialogBecameHidden();
    }
  }


  protected void askUserIfChangesShouldBeSaved() {
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    TextView view = new TextView(getActivity());
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
        resetEditedFieldsAndCloseDialog();
      }
    });

    builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialogInterface, int i) {
        saveEntryAndCloseDialog();
      }
    });

    builder.create().show();
  }

  protected void saveEntryAndCloseDialog() {
    saveEntryAsyncIfNeeded();

    closeDialog();
  }

  protected void saveEntryAsyncIfNeeded() {
    if(hasUnsavedChanges() == true || (entryCreationResult != null && entry.isPersisted() == false)) {
      saveEntryAsync();
    }
  }

  protected void saveEntryAsync() {
    // why do i run this little code on a new Thread? Getting HTML from AndroidHtmlEditor has to be done from a different one than main thread,
    // as async JavaScript response is dispatched to the main thread, therefore waiting for it as well on the main thread would block JavaScript response listener
    Application.getThreadPool().runTaskAsync(new Runnable() {
      @Override
      public void run() {
        saveEntry();
      }
    });
  }

  protected void saveEntry() {
    if(editedFields.contains(FieldWithUnsavedChanges.EntryAbstract)) {
      String abstractHtml = abstractHtmlEditor.getHtml();
      entry.setAbstract(abstractHtml);
      notifyEditEntryListener(entry, FieldWithUnsavedChanges.EntryAbstract, abstractHtml);
    }

    if(editedFields.contains(FieldWithUnsavedChanges.EntryContent)) {
      String contentHtml = contentHtmlEditor.getHtml();
      entry.setContent(contentHtml);
      notifyEditEntryListener(entry, FieldWithUnsavedChanges.EntryContent, contentHtml);
    }

    if(entryCreationResult != null) {
      entryCreationResult.saveCreatedEntities();
      entryCreationResult = null;
    }

    if(entry.isPersisted() == false) { // a new Entry
      Application.getDeepThought().addEntry(entry); // otherwise entry.id would be null when adding to Tags below
    }

    // TODO: why setting Tags here and not above before saving?
    if(editedFields.contains(FieldWithUnsavedChanges.EntryTags)) {
      entry.setTags(entryEditedTags);
      notifyEditEntryListener(entry, FieldWithUnsavedChanges.EntryTags, entryEditedTags);
    }

    unsetEntryHasBeenEdited();
  }

  protected void notifyEditEntryListener(final Entry entry, final FieldWithUnsavedChanges editedField, final Object editedFieldValue) {
    if(editEntityListener != null) {
      getActivity().runOnUiThread(new Runnable() {
        @Override
        public void run() {
          editEntityListener.entityEdited(entry, editedField, editedFieldValue);
        }
      });
    }
  }

  protected void resetEditedFieldsAndCloseDialog() {
    if(cleanUpOnClose == false) { // an instance of this Dialog is held somewhere
      // TODO: unset controls with edited fields
    }

    unsetEntryHasBeenEdited();

    closeDialog();
  }

  public void closeDialog() {
    if(cleanUpOnClose) { // if calling Activity / Dialog keeps an instance of this Dialog, that one will call cleanUp(), don't do it itself
      cleanUp();
    }

    hideDialog();
  }


  protected View.OnClickListener imgbtnShowEditAbstractSectionClickListener = new View.OnClickListener() {
    @Override
    public void onClick(View view) {
      showEditAbstractSection();
    }
  };

  protected View.OnClickListener imgbtnShowEditContentSectionClickListener = new View.OnClickListener() {
    @Override
    public void onClick(View view) {
      showEditContentSection();
    }
  };

  protected View.OnClickListener imgbtnShowEditTagsSectionClickListener = new View.OnClickListener() {
    @Override
    public void onClick(View view) {
      showEditTagsSection();
    }
  };


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
