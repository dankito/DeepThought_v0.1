package net.dankito.deepthought.controls.html;

import net.dankito.deepthought.Application;
import net.dankito.deepthought.data.html.ImageElementData;
import net.dankito.deepthought.data.model.FileLink;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ganymed on 17/10/15.
 */
public abstract class HtmlEditorListenerBase implements IHtmlEditorListener {

  protected net.dankito.deepthought.controls.utils.IEditedEntitiesHolder<FileLink> editedFilesHolder;


  protected HtmlEditorListenerBase() {

  }

  public HtmlEditorListenerBase(net.dankito.deepthought.controls.utils.EditedEntitiesHolder<FileLink> editedFiles) {
    this.editedFilesHolder = editedFiles;
  }


  @Override
  public boolean handleCommand(HtmlEditor editor, HtmEditorCommand command) {
    if(command == HtmEditorCommand.Image)
      return handleImageCommand(editor);
    return false;
  }

  @Override
  public boolean elementDoubleClicked(HtmlEditor editor, ImageElementData elementData) {
    FileLink file = getEditedFileById(elementData.getFileId());
    if(file != null) {
      net.dankito.deepthought.controller.Dialogs.showEditEmbeddedFileDialog(editor, editedFilesHolder, file, elementData);
      return true;
    }

    return false;
  }

  protected boolean handleImageCommand(HtmlEditor editor) {
    final FileLink newFile = new FileLink();

    net.dankito.deepthought.controller.Dialogs.showEditEmbeddedFileDialog(editor, editedFilesHolder, newFile);

    return true;
  }

  protected FileLink getEditedFileById(String fileId) {
    for(FileLink file : editedFilesHolder.getEditedEntities()) {
      if(file.getId().equals(fileId))
        return file;
    }

    return null;
  }


  public String handleEditedEmbeddedFiles(String previousHtml, String newHtml) {
    List<ImageElementData> addedImages = new ArrayList<>();
    List<ImageElementData> removedImages = new ArrayList<>();

    if(haveEmbeddedImagesChanged(previousHtml, newHtml, addedImages, removedImages)) {
      if(removedImages.size() > 0)
        handleRemovedEmbeddedImages(removedImages);

      if(addedImages.size() > 0)
        return handleAddedEmbeddedImages(addedImages, newHtml);
    }

    return newHtml;
  }

  protected void handleRemovedEmbeddedImages(List<ImageElementData> removedImages) {
    for(ImageElementData removedImage : removedImages) {
      FileLink file = getEditedFileById(removedImage.getFileId());
      if(file != null) {
        editedFilesHolder.removeEntityFromEntry(file);
      }
    }
  }

  protected String handleAddedEmbeddedImages(List<ImageElementData> addedImages, String newHtml) {
    for(ImageElementData addedImage : addedImages) {
      FileLink file = Application.getDeepThought().getFileById(addedImage.getFileId());
      if(file == null) { // a new file
        file = addedImage.createFile();
        Application.getDeepThought().addFile(file);
        addedImage.setFileId(file.getId());
        newHtml = newHtml.replace(addedImage.getOriginalImgElementHtmlCode(), addedImage.createHtmlCode());
      }

      if (file != null) {
        editedFilesHolder.addEntityToEntry(file);
      }
    }

    return newHtml;
  }

  protected boolean haveEmbeddedImagesChanged(String previousHtml, String newHtml, List<ImageElementData> addedImages, List<ImageElementData> removedImages) {
    List<ImageElementData> previousImages = Application.getHtmlHelper().extractAllImageElementsFromHtml(previousHtml);
    List<ImageElementData> currentImages = Application.getHtmlHelper().extractAllImageElementsFromHtml(newHtml);

    for(ImageElementData previousImage : previousImages) {
      if(isImageInList(previousImage, currentImages) == false) {
        if(isStillInAnotherInstanceOnHtml(newHtml, previousImage) == false)
          removedImages.add(previousImage);
      }
    }

    for(ImageElementData currentImage : currentImages) {
      if(isImageInList(currentImage, previousImages) == false)
        addedImages.add(currentImage);
    }

    return addedImages.size() > 0 || removedImages.size() > 0;
  }

  protected boolean isImageInList(ImageElementData imageToTest, List<ImageElementData> imageList) {
    for(ImageElementData image : imageList) {
      if(imageToTest.getFileId() != null && imageToTest.getFileId().equals(image.getFileId()) && imageToTest.getEmbeddingId().equals(image.getEmbeddingId()))
        return true;
    }

    return false;
  }

  protected boolean isStillInAnotherInstanceOnHtml(String html, ImageElementData image) {
    return html.contains(ImageElementData.ImageIdAttributeName + "=\"" + image.getFileId());
  }

}
