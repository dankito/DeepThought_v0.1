package net.deepthought.controls.html;

import net.deepthought.Application;
import net.deepthought.controls.ICleanUp;
import net.deepthought.data.html.ImageElementData;
import net.deepthought.util.StringUtils;
import net.deepthought.util.file.FileUtils;

import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import netscape.javascript.JSObject;

/**
 * A Java Wrapper class for the JavaScript CKEditor.
 *
 * Created by ganymed on 28/08/15.
 */
public class HtmlEditor implements IJavaScriptBridge, ICleanUp {

  public final static String HtmlEditorFolderName = "htmleditor";

  public final static String HtmlEditorFileName = "CKEditor_start.html";

  public final static String HtmlEditorFolderAndFileName = new File(HtmlEditorFolderName, HtmlEditorFileName).getPath();


  private final static Logger log = LoggerFactory.getLogger(HtmlEditor.class);


  protected static String unzippedHtmlEditorFilePath = null;


  protected IJavaScriptExecutor scriptExecutor = null;

  protected boolean editorLoaded = false;

  protected JSObject ckEditor = null;

  protected String previousHtml = "";

  protected String htmlToSetWhenLoaded = null;

  protected boolean editorHasBeenNewlyInitialized = false;
  protected boolean resetUndoStack = false;

  protected IHtmlEditorListener listener = null;



  public HtmlEditor(IJavaScriptExecutor scriptExecutor) {
    this.scriptExecutor = scriptExecutor;
  }

  public HtmlEditor(IJavaScriptExecutor scriptExecutor, IHtmlEditorListener listener) {
    this(scriptExecutor);
    this.listener = listener;
  }


  public String getHtmlEditorPath() {
    return unzippedHtmlEditorFilePath;
  }

  public void editorLoaded() {
    scriptExecutor.setJavaScriptMember("app", this);

    editorLoaded = true;

    if (htmlToSetWhenLoaded != null)
      setHtml(htmlToSetWhenLoaded);
  }

  public void insertHtml(String html) {
    scriptExecutor.executeScript("CKEDITOR.instances.editor.insertHtml('" + StringEscapeUtils.escapeEcmaScript(html) + "', 'unfiltered_html')");
  }

  public void scrollTo(int scrollPosition) {
    scriptExecutor.executeScript("$(CKEDITOR.instances.editor.document.$).scrollTop(" + scrollPosition + ");");
  }

  public void resetUndoStack() {
    scriptExecutor.executeScript("CKEDITOR.instances.editor.resetUndo()");
  }


  public boolean isCKEditorLoaded() {
    return editorLoaded;
  }

  public String getHtml() {
    try {
      // no need for calling a JavaScript function as each time HTML updates htmlChanged() is called -> we're always up to date
//      scriptExecutor.executeScript("CKEDITOR.instances.editor.getData()", new ExecuteJavaScriptResultListener() {
//        @Override
//        public void scriptExecuted(Object result) {
//
//        }
//      });
      return previousHtml;
    } catch(Exception ex) {
      log.error("Could not get HtmlEditor's html text", ex);
    }

    return "";
  }

  public void setHtml(String html) {
    setHtml(html, false);
  }

  public void setHtml(String html, boolean resetUndoStack) {
    if(html == null)
      html = "";
    previousHtml = html;
    this.resetUndoStack = resetUndoStack;

    try {
      if(isCKEditorLoaded() == false)
        htmlToSetWhenLoaded = html;
      else {
        scriptExecutor.executeScript("CKEDITOR.instances.editor.setData('" + StringEscapeUtils.escapeEcmaScript(html) + "')");
        htmlToSetWhenLoaded = null;
      }

    } catch(Exception ex) {
      log.error("Could not set HtmlEditor's html text", ex);
    }
  }

  public IHtmlEditorListener getListener() {
    return listener;
  }

  public void setListener(IHtmlEditorListener listener) {
    this.listener = listener;
  }

  public void reInitHtmlEditor(IHtmlEditorListener listener) {
    scrollTo(0);
    resetUndoStack();
    setListener(listener);

    editorHasBeenNewlyInitialized = true;
  }


  @Override
  public void cleanUp() {
    setListener(null);
    previousHtml = "";

    try {
      scriptExecutor.executeScript("window", new ExecuteJavaScriptResultListener() {
        @Override
        public void scriptExecuted(Object result) {
//          JSObject win = (JSObject) result;
//          win.setMember("app", null);
        }
      });
    } catch(Exception ex) { }

    this.scriptExecutor = null;
  }


  /*    Methods over which JavaScript running in Browser communicates with Java code        */

  public void loaded() {
    scriptExecutor.executeScript("resizeEditorToFitWindow()"); // don't know why but without calling it CKEditor doesn't size correctly
  }

  public void htmlChanged(String newHtmlCode) {
    if(previousHtml.equals(newHtmlCode) == false) {
      if (listener != null) {
        if(hasCountImagesChanged(previousHtml, newHtmlCode)) {
          imageAddedOrRemoved(previousHtml, newHtmlCode, listener);
        }
      }

      listener.htmlCodeUpdated(newHtmlCode); // TODO: may also pass previousHtml as parameter
    }

    previousHtml = newHtmlCode;

    if(editorHasBeenNewlyInitialized == true || resetUndoStack == true) {
      resetUndoStack();
      editorHasBeenNewlyInitialized = false;
      resetUndoStack = false;
    }
  }

  protected void imageAddedOrRemoved(String previousHtml, String newHtmlCode, IHtmlEditorListener listener) {
    List<ImageElementData> previousImages = Application.getHtmlHelper().extractAllImageElementsFromHtml(previousHtml);
    List<ImageElementData> currentImages = Application.getHtmlHelper().extractAllImageElementsFromHtml(newHtmlCode);

    for(ImageElementData previousImage : previousImages) {
      if(isImageInList(previousImage, currentImages) == false)
        listener.imageHasBeenDeleted(previousImage, isStillInAnotherInstanceOnHtml(newHtmlCode, previousImage));
    }

    for(ImageElementData currentImage : currentImages) {
      if(isImageInList(currentImage, previousImages) == false)
        listener.imageAdded(currentImage);
    }
  }

  protected boolean isImageInList(ImageElementData imageToTest, List<ImageElementData> imageList) {
    for(ImageElementData image : imageList) {
      if(((Long)imageToTest.getFileId()).equals(image.getFileId()) && ((Long)imageToTest.getEmbeddingId()).equals(image.getEmbeddingId()))
        return true;
    }

    return false;
  }

  protected boolean isStillInAnotherInstanceOnHtml(String html, ImageElementData image) {
    return html.contains(ImageElementData.ImageIdAttributeName + "=\"" + image.getFileId());
  }

  protected List<Map<String, String>> extractImageData(String previousHtml, String newHtmlCode) {
    List<Map<String, String>> imagesData = new ArrayList<>();

    return imagesData;
  }

  public boolean elementClicked(String element, int button, int clickX, int clickY) {
    boolean dummy = false;
    return true;
  }

  public boolean elementDoubleClicked(String element) {
    if(isImageElement(element)) {
      List<ImageElementData> imageElements = Application.getHtmlHelper().extractAllImageElementsFromHtml(element);
      if(imageElements.size() > 0) {
        ImageElementData imgElement = imageElements.get(0);
        if(listener != null)
          return listener.elementDoubleClicked(this, imgElement);
      }
    }

    return false;
  }

  protected boolean isImageElement(String element) {
    return element != null && element.toLowerCase().startsWith("<img ") && element.contains("class=\"cke_anchor\"") == false;
  }

  public boolean beforeCommandExecution(String commandName) {
    if(listener != null) {
      if (commandName.toLowerCase().equals("image")) {
        return !listener.handleCommand(this, HtmEditorCommand.Image);
      }
    }

    return true;
  }

  public void replaceImageElement(ImageElementData previousElement, ImageElementData newElement) {
    try {
      JSObject doc = (JSObject)ckEditor.getMember("document");
      JSObject nodeList = (JSObject)doc.call("getElementsByTag", "img");
      int length = (Integer)nodeList.call("count");

      for(int i = 0; i < length; i++) {
        JSObject imgNode = (JSObject)nodeList.call("getItem", i);
        String idString = (String)imgNode.call("getAttribute", ImageElementData.EmbeddingIdAttributeName);
        Long id = Long.parseLong(idString);

        if(id.equals(previousElement.getEmbeddingId())) {
          replaceElementWithImageElement(imgNode, newElement);
        }
      }
    } catch(Exception ex) {
      log.error("Could not replace <img> element", ex);
    }
  }

  protected void replaceElementWithImageElement(final JSObject elementToBeReplaced, ImageElementData newElement) {
    createNewImageElement(newElement, new ExecuteJavaScriptResultListener() {
      @Override
      public void scriptExecuted(Object result) {
        replaceElementWith(elementToBeReplaced, (JSObject) result);
      }
    });
  }

  protected void replaceElementWith(JSObject elementToBeReplaced, JSObject newElement) {
    elementToBeReplaced.call("insertBeforeMe", newElement);
    elementToBeReplaced.call("remove");
  }

  protected void createNewImageElement(final ImageElementData newElement, final ExecuteJavaScriptResultListener listener) {
    scriptExecutor.executeScript("new CKEDITOR.dom.element( 'img' );", new ExecuteJavaScriptResultListener() {
      @Override
      public void scriptExecuted(Object result) {
        if(result instanceof JSObject) {
          JSObject createdElement = (JSObject) result;

          createdElement.call("setAttribute", ImageElementData.SourceAttributeName, newElement.getSource());
          createdElement.call("setAttribute", ImageElementData.ImageIdAttributeName, newElement.getFileId());
          createdElement.call("setAttribute", ImageElementData.EmbeddingIdAttributeName, newElement.getEmbeddingId());
          createdElement.call("setAttribute", ImageElementData.WidthAttributeName, newElement.getWidth());
          createdElement.call("setAttribute", ImageElementData.HeightAttributeName, newElement.getHeight());
          createdElement.call("setAttribute", ImageElementData.AltAttributeName, newElement.getAlt());

          listener.scriptExecuted(createdElement);
        }
        else {
          log.error("Could not create a new CKEDITOR.dom.element. Return value was " + result);
//          listener.scriptExecuted(null);
        }
      }
    });
  }

  protected boolean hasCountImagesChanged(String previousHtml, String newHtml) {
    int countImgTagsInPreviousHtml = StringUtils.getNumberOfOccurrences("<img ", previousHtml);
    int countImgTagsInNewHtml = StringUtils.getNumberOfOccurrences("<img ", newHtml);

    return countImgTagsInPreviousHtml != countImgTagsInNewHtml;
  }


  public static void extractHtmlEditorIfNeededAsync() {
    Application.getThreadPool().runTaskAsync(new Runnable() {
      @Override
      public void run() {
        extractHtmlEditorIfNeeded();
      }
    });
  }

  public static void extractHtmlEditorIfNeeded() {
    File htmlEditorDirectory = new File(Application.getDataFolderPath(), HtmlEditorFolderName);
    FileUtils.deleteFile(htmlEditorDirectory); // if CKEditor_start.html has been updated

    if(htmlEditorDirectory.exists() == false /*|| htmlEditorDirectory.*/) { // TODO: check if folder has correct size
      unzippedHtmlEditorFilePath = extractCKEditorToHtmlEditorFolder();
    }
    else {
      try {
        unzippedHtmlEditorFilePath = new File(htmlEditorDirectory, HtmlEditorFileName).toURI().toURL().toExternalForm();
      } catch (Exception ex) { log.error("Could not build  from " + htmlEditorDirectory + " and " + HtmlEditorFileName, ex); } // TODO: what to do in error case?
    }
  }

  protected static String extractCKEditorToHtmlEditorFolder() {
    String htmlEditorPath = null;

    try {
      String htmlEditorDirectory = Application.getDataFolderPath();

      JarFile jar = FileUtils.getDeepThoughtLibJarFile();
      Enumeration enumEntries = jar.entries();

      while (enumEntries.hasMoreElements()) {
        JarEntry entry = (JarEntry)enumEntries.nextElement();
        if (entry.isDirectory()) {
          continue;
        }

        if(entry.getName().equals(HtmlEditorFolderAndFileName)) {
          File file = new File(htmlEditorDirectory, entry.getName());
          URL url = file.toURI().toURL();
          htmlEditorPath = url.toExternalForm();
        }

        if(entry.getName().startsWith(HtmlEditorFolderName)) {
          FileUtils.extractJarFileEntry(jar, entry, htmlEditorDirectory);
        }
      }
    } catch(Exception ex) {
      log.error("Could not extract Html Editor from .jar file", ex);
    }

    return htmlEditorPath;
  }
}
