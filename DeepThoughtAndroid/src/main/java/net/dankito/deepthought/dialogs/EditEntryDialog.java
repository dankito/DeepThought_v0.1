package net.dankito.deepthought.dialogs;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.PopupMenu;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import net.dankito.deepthought.Application;
import net.dankito.deepthought.R;
import net.dankito.deepthought.activities.DialogParentActivity;
import net.dankito.deepthought.adapter.EntrySectionsSpinnerAdapter;
import net.dankito.deepthought.adapter.EntryTagsAdapter;
import net.dankito.deepthought.controls.html.AndroidHtmlEditor;
import net.dankito.deepthought.controls.html.AndroidHtmlEditorPool;
import net.dankito.deepthought.controls.html.HtmEditorCommand;
import net.dankito.deepthought.controls.html.HtmlEditor;
import net.dankito.deepthought.controls.html.IHtmlEditorListener;
import net.dankito.deepthought.data.contentextractor.EntryCreationResult;
import net.dankito.deepthought.data.html.ImageElementData;
import net.dankito.deepthought.data.model.Entry;
import net.dankito.deepthought.data.model.Tag;
import net.dankito.deepthought.data.search.ui.TagsSearcherButtonState;
import net.dankito.deepthought.dialogs.enums.EditEntrySection;
import net.dankito.deepthought.listener.EditEntityListener;
import net.dankito.deepthought.ui.enums.FieldWithUnsavedChanges;
import net.dankito.deepthought.ui.model.IEntityPreviewService;
import net.dankito.deepthought.util.InsertImageOrRecognizedTextHelper;
import net.dankito.deepthought.util.StringUtils;

import org.droidparts.widget.ClearableEditText;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by ganymed on 06/09/16.
 */
public class EditEntryDialog extends FullscreenDialog {


  protected Entry entry;

  protected EntryCreationResult entryCreationResult = null;

  protected List<FieldWithUnsavedChanges> editedFields = new ArrayList<>();

  protected EntryTagsAdapter entryTagsAdapter;


  protected RelativeLayout rlytEditAbstract;

  protected RelativeLayout rlytEditContent;

  protected RelativeLayout rlytEditTags;

  protected AndroidHtmlEditor abstractHtmlEditor = null;
  protected AndroidHtmlEditor contentHtmlEditor = null;

  protected TextView txtvwEntryTagsPreview = null;

  protected ClearableEditText edtxtEditEntrySearchTag = null;
  protected Button btnEditEntryCreateOrToggleTags = null;
  protected TagsSearcherButtonState btnEditEntryCreateOrToggleTagsState = TagsSearcherButtonState.DISABLED;

  protected ListView lstvwEditEntryTags = null;

  protected List<Tag> entryEditedTags = new ArrayList<>();

  protected Spinner spnSelectEntrySection;

  protected ImageButton imgbtnAddEntryField;

  protected MenuItem mnitmAddEntryAbstractField;

  protected EntrySectionsSpinnerAdapter entrySectionsSpinnerAdapter;

  protected boolean hasViewBeenCreated = false;

  protected EditEntrySection sectionToEditAfterLoading = null;

  protected MenuItem mnitmActionTakePhotoOrRecognizeText = null;

  protected InsertImageOrRecognizedTextHelper insertImageOrRecognizedTextHelper;

  protected EditEntityListener editEntityListener = null;

  protected IEntityPreviewService previewService = Application.getEntityPreviewService();


  public EditEntryDialog() {

  }


  protected void initEntryTagsAdapter(Entry entry) {
    entryTagsAdapter = new EntryTagsAdapter(getActivity(), this.entry, entryEditedTags, entryTagsAdapterListener);
  }

  protected EntryTagsAdapter.EntryTagsAdapterListener entryTagsAdapterListener = new EntryTagsAdapter.EntryTagsAdapterListener() {
    @Override
    public void entryTagsChanged(List<Tag> entryTags) {
      setEntryHasBeenEdited(FieldWithUnsavedChanges.EntryTags, entryTags);
      setTagsPreviewOnUiThread(entryTags);
    }

    @Override
    public void tagsSearchDone(TagsSearcherButtonState buttonState) {
      setBtnEditEntryNewTagStateOnUiThread(buttonState);
    }
  };


  public void setEntry(Entry entry) {
    this.entry = entry;
  }

  public void setEntryCreationResult(EntryCreationResult entryCreationResult) {
    this.entryCreationResult = entryCreationResult;

    setEntry(entryCreationResult.getCreatedEntry());
  }

  public void setEditEntityListener(EditEntityListener listener) {
    this.editEntityListener = listener;
  }

  protected void setSectionToEdit(EditEntrySection section) {
    if(hasViewBeenCreated) {
      applySectionToEdit(section);
    }
    else {
      sectionToEditAfterLoading = section;
    }
  }

  protected void applySectionToEdit(EditEntrySection section) {
    spnSelectEntrySection.setSelection(entrySectionsSpinnerAdapter.getIndexForSection(section), true);
  }


  @Override
  protected int getLayoutId() {
    return R.layout.dialog_edit_entry;
  }

  @Override
  protected void setupUi(View rootView) {
    insertImageOrRecognizedTextHelper = new InsertImageOrRecognizedTextHelper(getActivity());


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
  }


  @Override
  protected void customizeToolbar(View rootView, ActionBar actionBar) {
    actionBar.setDisplayShowCustomEnabled(true);
    actionBar.setCustomView(R.layout.dialog_edit_entry_custom_action_bar_view);

    spnSelectEntrySection = (Spinner)rootView.findViewById(R.id.spnSelectEntrySection);
    spnSelectEntrySection.setOnItemSelectedListener(spnSelectEntrySectionItemSelectedListener);

    entrySectionsSpinnerAdapter = new EntrySectionsSpinnerAdapter(getActivity(), entry);
    spnSelectEntrySection.setAdapter(entrySectionsSpinnerAdapter);

    imgbtnAddEntryField = (ImageButton)rootView.findViewById(R.id.imgbtnAddEntryField);

    initializePopupForButtonAddEntryField();

    setImgbtnAddEntryFieldAndMenuItemsVisiblity();
  }

  protected void initializePopupForButtonAddEntryField() {
    final PopupMenu popup = new PopupMenu(getActivity(), imgbtnAddEntryField);
    getActivity().getMenuInflater().inflate(R.menu.button_add_entry_field_context_menu, popup.getMenu());

    mnitmAddEntryAbstractField = popup.getMenu().findItem(R.id.mnitmAddEntryAbstractField);

    imgbtnAddEntryField.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        preparePopupForButtonAddEntryField();
        popup.show();
      }
    });

    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
      @Override
      public boolean onMenuItemClick(MenuItem item) {
        imgbtnAddEntryFieldPopupMenuItemSelected(item);
        return true;
      }
    });
  }

  public void setImgbtnAddEntryFieldAndMenuItemsVisiblity() {
    imgbtnAddEntryField.setVisibility(areAllEntryFieldsSelectable() ? View.GONE : View.VISIBLE);

    preparePopupForButtonAddEntryField();
  }

  protected void preparePopupForButtonAddEntryField() {
    mnitmAddEntryAbstractField.setVisible( ! isAbstractSectionSelectable() );
  }

  protected void imgbtnAddEntryFieldPopupMenuItemSelected(MenuItem item) {
    int id = item.getItemId();

    if(id == R.id.mnitmAddEntryAbstractField) {
      addAbstractSection();
    }
  }

  protected void addAbstractSection() {
    entrySectionsSpinnerAdapter.setShouldShowAbstractSection(true);
    applySectionToEdit(EditEntrySection.Abstract);
    showEditAbstractSection();
    setImgbtnAddEntryFieldAndMenuItemsVisiblity();
  }

  protected boolean areAllEntryFieldsSelectable() {
    return isAbstractSectionSelectable();
  }

  protected boolean isAbstractSectionSelectable() {
    return entrySectionsSpinnerAdapter.showsAbstractSection();
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

    txtvwEntryTagsPreview = (TextView)rootView.findViewById(R.id.txtvwEditEntryTagsPreview);

    edtxtEditEntrySearchTag = (ClearableEditText)rootView.findViewById(R.id.edtxtEditEntrySearchTag);
    edtxtEditEntrySearchTag.addTextChangedListener(edtxtEditEntrySearchTagTextChangedListener);
    edtxtEditEntrySearchTag.setOnEditorActionListener(edtxtEditEntrySearchTagActionListener);

    btnEditEntryCreateOrToggleTags = (Button)rootView.findViewById(R.id.btnEditEntryCreateOrToggleTags);
    btnEditEntryCreateOrToggleTags.setOnClickListener(btnEditEntryCreateOrToggleTagsOnClickListener);

    lstvwEditEntryTags = (ListView)rootView.findViewById(R.id.lstvwEditEntryTags);
  }


  protected void setEntryFieldValues(Entry entry) {
    if(entry.hasAbstract()) {
      abstractHtmlEditor.setHtml(entry.getAbstract());
    }

    contentHtmlEditor.setHtml(entry.getContent());

    if(entryCreationResult == null) {
      entryEditedTags = new ArrayList<>(entry.getTags());
    }
    else {
      entryEditedTags = entryCreationResult.getTags();
    }

    setTagsPreview(entryEditedTags);

    initEntryTagsAdapter(entry);
    lstvwEditEntryTags.setAdapter(entryTagsAdapter);
  }

  protected void setEntryHasBeenEdited(FieldWithUnsavedChanges editedField, Object editedFieldValue) {
    editedFields.add(editedField);
  }

  protected void unsetEntryHasBeenEdited() {
    editedFields.clear();
  }


  @Override
  public boolean canHandleActivityResult(int requestCode, int resultCode, Intent data) {
    return insertImageOrRecognizedTextHelper.canHandleActivityResult(requestCode, resultCode, data);
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
    public void afterTextChanged(Editable editable) {
      entryTagsAdapter.searchTags(editable.toString());
    }
  };

  protected TextView.OnEditorActionListener edtxtEditEntrySearchTagActionListener = new TextView.OnEditorActionListener() {
    @Override
    public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
      if (actionId == EditorInfo.IME_NULL
          && keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
        onSearchTagsControlAction();
        return true;
      }
      return false;
    }
  };


  protected View.OnClickListener btnEditEntryCreateOrToggleTagsOnClickListener = new View.OnClickListener() {
    @Override
    public void onClick(View v) {
      onSearchTagsControlAction();
    }
  };

  protected void onSearchTagsControlAction() {
    if(btnEditEntryCreateOrToggleTagsState == TagsSearcherButtonState.CREATE_TAG) {
      createNewTag();
    }
    else if(btnEditEntryCreateOrToggleTagsState == TagsSearcherButtonState.TOGGLE_TAGS) {
      entryTagsAdapter.toggleTags();
    }
  }

  protected void createNewTag() {
    String tagName = edtxtEditEntrySearchTag.getText().toString();

    if(StringUtils.isNullOrEmpty(tagName)) { // TODO: the first two options shouldn't be possible anymore
      Toast.makeText(getActivity(), getString(R.string.error_message_tag_name_must_be_a_non_empty_string), Toast.LENGTH_LONG).show();
    }
    else if(Application.getDeepThought().containsTagOfName(tagName)) {
      Toast.makeText(getActivity(), getString(R.string.error_message_tag_with_that_name_already_exists), Toast.LENGTH_LONG).show();
    }
    else {
      Tag newTag = new Tag(tagName);
      Application.getDeepThought().addTag(newTag);
      entryEditedTags.add(newTag);
      Collections.sort(entryEditedTags);
      setEntryHasBeenEdited(FieldWithUnsavedChanges.EntryTags, entryEditedTags);
      setTagsPreview(entryEditedTags);
    }
  }

  protected void setTagsPreviewOnUiThread(final List<Tag> tags) {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        setTagsPreview(tags);
      }
    });
  }

  protected void setTagsPreview(List<Tag> tags) {
    txtvwEntryTagsPreview.setText(previewService.getTagsPreview(tags, true));
  }

  protected void setBtnEditEntryNewTagStateOnUiThread(final TagsSearcherButtonState state) {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        setBtnEditEntryNewTagState(state);
      }
    });
  }

  protected void setBtnEditEntryNewTagState(TagsSearcherButtonState state) {
    this.btnEditEntryCreateOrToggleTagsState = state;

    btnEditEntryCreateOrToggleTags.setEnabled(state != TagsSearcherButtonState.DISABLED);

    if(state == TagsSearcherButtonState.CREATE_TAG) {
      btnEditEntryCreateOrToggleTags.setText(R.string.edit_entry_create_tag);
    }
    else if(state == TagsSearcherButtonState.TOGGLE_TAGS) {
      btnEditEntryCreateOrToggleTags.setText(R.string.edit_entry_toggle_tags);
    }
  }



  public void showDialog(DialogParentActivity activity) {
    showDialog(activity, EditEntrySection.Content);
  }

  public void showDialog(DialogParentActivity activity, EditEntrySection sectionToEdit) {
    super.showDialog(activity);

    setSectionToEdit(sectionToEdit);

    if(hasViewBeenCreated) {
      setEntryFieldValues(entry);
    }
  }


  @Override
  protected boolean shouldUserBeAskedToSaveChanges() {
    return editedFields.size() > 0;
  }

  @Override
  protected boolean hasUnsavedChangesThatShouldBeSaved() {
    return shouldUserBeAskedToSaveChanges() || entryCreationResult != null;
  }

  @Override
  protected int getAlertMessageIfChangesShouldGetSaved() {
    return R.string.alert_dialog_entry_has_unsaved_changes_text;
  }

  @Override
  protected void saveEntity() {
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

    // TODO: why setting Tags here and not above before saving?
    if(editedFields.contains(FieldWithUnsavedChanges.EntryTags)) {
      entry.setTags(entryEditedTags);
      notifyEditEntryListener(entry, FieldWithUnsavedChanges.EntryTags, entryEditedTags);
    }

    if(entry.isPersisted() == false) { // a new Entry
      Application.getDeepThought().addEntry(entry); // otherwise entry.id would be null when adding to Tags below
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


  @Override
  protected void resetEditedFieldsAndCloseDialog() {
    super.resetEditedFieldsAndCloseDialog();

    unsetEntryHasBeenEdited();
  }


  protected AdapterView.OnItemSelectedListener spnSelectEntrySectionItemSelectedListener = new AdapterView.OnItemSelectedListener() {
    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int index, long l) {
      EditEntrySection section = entrySectionsSpinnerAdapter.getSectionAtIndex(index);
      sectionSelected(section);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
  };

  protected void sectionSelected(EditEntrySection section) {
    switch(section) {
      case Abstract:
        showEditAbstractSection();
        break;
      case Content:
        showEditContentSection();
        break;
      case Tags:
        showEditTagsSection();
        break;
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

    entryTagsAdapter.cleanUp();

    editEntityListener = null;

    super.cleanUp();
  }

}
