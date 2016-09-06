package net.dankito.deepthought.controls.html;

import net.dankito.deepthought.data.model.FileLink;
import net.dankito.deepthought.ui.enums.FieldWithUnsavedChanges;

import java.util.Collection;

/**
 * Created by ganymed on 22/09/15.
 */
public class DeepThoughtFxHtmlEditorListener extends HtmlEditorListenerBase implements IHtmlEditorListener {

  protected Collection<FieldWithUnsavedChanges> fieldsWithUnsavedChanges;

  protected FieldWithUnsavedChanges fieldToAddOnChanges;


  public DeepThoughtFxHtmlEditorListener(net.dankito.deepthought.controls.utils.EditedEntitiesHolder<FileLink> editedFiles, Collection<FieldWithUnsavedChanges> fieldsWithUnsavedChanges, FieldWithUnsavedChanges fieldToAddOnChanges) {
    super(editedFiles);
    this.fieldsWithUnsavedChanges = fieldsWithUnsavedChanges;
    this.fieldToAddOnChanges = fieldToAddOnChanges;
  }


  @Override
  public void editorHasLoaded(HtmlEditor editor) {

  }

  @Override
  public void htmlCodeUpdated() {
    fieldsWithUnsavedChanges.add(fieldToAddOnChanges);
  }

  @Override
  public void htmlCodeHasBeenReset() {
    fieldsWithUnsavedChanges.remove(fieldToAddOnChanges);
  }

}
