package net.deepthought.controls.html;

import net.deepthought.controller.enums.FieldWithUnsavedChanges;
import net.deepthought.controls.utils.EditedEntitiesHolder;
import net.deepthought.data.model.FileLink;

import java.util.Collection;

/**
 * Created by ganymed on 22/09/15.
 */
public class DeepThoughtFxHtmlEditorListener extends HtmlEditorListenerBase implements IHtmlEditorListener {

  protected Collection<FieldWithUnsavedChanges> fieldsWithUnsavedChanges;

  protected FieldWithUnsavedChanges fieldToAddOnChanges;


  public DeepThoughtFxHtmlEditorListener(EditedEntitiesHolder<FileLink> editedFiles, Collection<FieldWithUnsavedChanges> fieldsWithUnsavedChanges, FieldWithUnsavedChanges fieldToAddOnChanges) {
    super(editedFiles);
    this.fieldsWithUnsavedChanges = fieldsWithUnsavedChanges;
    this.fieldToAddOnChanges = fieldToAddOnChanges;
  }


  @Override
  public void editorHasLoaded(HtmlEditor editor) {

  }

  @Override
  public void htmlCodeUpdated(String updatedHtmlCode) {
    fieldsWithUnsavedChanges.add(fieldToAddOnChanges);
  }

}
