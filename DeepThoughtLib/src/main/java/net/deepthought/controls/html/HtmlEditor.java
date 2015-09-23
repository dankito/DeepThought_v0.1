package net.deepthought.controls.html;

import net.deepthought.Application;
import net.deepthought.controls.ICleanUp;
import net.deepthought.data.html.ImageElementData;
import net.deepthought.util.StringUtils;
import net.deepthought.util.file.FileUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.html.HTMLDocument;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
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
public class HtmlEditor implements ICleanUp {

  public final static String HtmlEditorFolderName = "htmleditor";

  public final static String HtmlEditorFileName = "CKEditor_start.html";

  public final static String HtmlEditorFolderAndFileName = new File(HtmlEditorFolderName, HtmlEditorFileName).getPath();


  protected final static String ImageIdDataKey = "ID";

  protected final static String ImageUrlDataKey = "URL";


  private final static Logger log = LoggerFactory.getLogger(HtmlEditor.class);


  protected static String unzippedHtmlEditorFilePath = null;


  protected IJavaScriptExecutor scriptExecutor = null;

  protected JSObject ckEditor = null;

  protected JSObject jqDocument = null;

  protected HTMLDocument document = null;

  protected String previousHtml = "";

  protected String htmlToSetWhenLoaded = null;

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
    try {
      JSObject win = (JSObject) scriptExecutor.executeScript("window");
      win.setMember("app", this);

      ckEditor = (JSObject)scriptExecutor.executeScript("CKEDITOR.instances.editor");

      document = (HTMLDocument)ckEditor.eval("document");

      if (htmlToSetWhenLoaded != null)
        setHtml(htmlToSetWhenLoaded);
    } catch(Exception ex) {
      log.error("Could not setup HtmlEditor in loaded event", ex);
    }
  }

  public void insertHtml(String html) {
    try {
      ckEditor.call("insertHtml", html, "unfiltered_html");
    } catch(Exception ex) {
      log.error("Could not insert Html " + html + " into CKEditor", ex);
    }
  }

  public void scrollTo(int scrollPosition) {
    try {
      if(jqDocument != null) {
        jqDocument.call("scrollTop", scrollPosition);
      }
    } catch(Exception ex) {
      log.error("Could not scroll document to " + scrollPosition, ex);
    }
  }


  public boolean isCKEditorLoaded() {
    return ckEditor != null;
  }

  public String getHtml() {
    try {
      if(isCKEditorLoaded()) {
        Object obj = ckEditor.call("getData");
        return obj.toString();
      }
    } catch(Exception ex) {
      log.error("Could not get HtmlEditor's html text", ex);
    }

    return "";
  }

  public void setHtml(String html) {
    previousHtml = html;

    try {
      if(isCKEditorLoaded() == false)
        htmlToSetWhenLoaded = html;
      else {
        ckEditor.call("setData", html);
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

  public void setListenerAndScrollToTop(IHtmlEditorListener listener) {
    scrollTo(0);
    setListener(listener);
  }


  @Override
  public void cleanUp() {
    setListener(null);

    try {
      JSObject win = (JSObject) scriptExecutor.executeScript("window");
      win.setMember("app", null);
    } catch(Exception ex) { }

    this.scriptExecutor = null;
    ckEditor = null;
  }


  /*    Methods over which JavaScript running in Browser communicates with Java code        */

  public void loaded() {
    jqDocument = (JSObject)scriptExecutor.executeScript("$(editor.document.$);");
  }

  public void htmlChanged(String newHtmlCode) {
    if(previousHtml.equals(newHtmlCode) == false) {
      if(hasAnImageBeenRemoved(previousHtml, newHtmlCode)) {
        if(listener != null) {
          List<Map<String, String>> imagesData = extractImageData(previousHtml, newHtmlCode);
          for(Map<String, String> imageData : imagesData)
            listener.imageHasBeenDeleted(imageData.get(ImageIdDataKey), imageData.get(ImageUrlDataKey)); // TODO: also check for undoing image deletion
        }
      }
    }

    if(listener != null)
      listener.htmlCodeUpdated(newHtmlCode); // TODO: may also pass previousHtml as parameter
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
          replaceElementWith(imgNode, createNewImageElement(newElement));
        }
      }
    } catch(Exception ex) {
      log.error("Could not replace <img> element", ex);
    }
  }

  protected void replaceElementWith(JSObject elementToBeReplaced, JSObject newElement) {
    elementToBeReplaced.call("insertBeforeMe", newElement);
    elementToBeReplaced.call("remove");
  }

  protected JSObject createNewImageElement(ImageElementData newElement) {
    JSObject createdElement = (JSObject)scriptExecutor.executeScript("new CKEDITOR.dom.element( 'img' );");

    createdElement.call("setAttribute", ImageElementData.SourceAttributeName, newElement.getSource());
    createdElement.call("setAttribute", ImageElementData.ImageIdAttributeName, newElement.getFileId());
    createdElement.call("setAttribute", ImageElementData.EmbeddingIdAttributeName, newElement.getEmbeddingId());
    createdElement.call("setAttribute", ImageElementData.WidthAttributeName, newElement.getWidth());
    createdElement.call("setAttribute", ImageElementData.HeightAttributeName, newElement.getHeight());
    createdElement.call("setAttribute", ImageElementData.AltAttributeName, newElement.getAlt());

    return createdElement;
  }

  protected boolean hasAnImageBeenRemoved(String previousHtml, String newHtml) {
    int countImgTagsInPreviousHtml = StringUtils.getNumberOfOccurrences("<img ", previousHtml);
    int countImgTagsInNewHtml = StringUtils.getNumberOfOccurrences("<img ", newHtml);

    return countImgTagsInPreviousHtml > countImgTagsInNewHtml;
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
      JarFile jar = getJarFilePath();

      File htmlEditorDirectory = new File(Application.getDataFolderPath());

      java.util.Enumeration enumEntries = jar.entries();
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
          writeHtmlEditorFileToTempDir(jar, entry, htmlEditorDirectory);
        }
      }
    } catch(Exception ex) {
      log.error("Could not extract Html Editor from .jar file", ex);
    }

    return htmlEditorPath;
  }

  protected static JarFile getJarFilePath() throws IOException {
    URL url = HtmlEditor.class.getClassLoader().getResource(HtmlEditorFolderAndFileName);
    String jarPathString = url.toExternalForm();
    jarPathString = jarPathString.replace("!/" + HtmlEditorFolderAndFileName, "");
    if(jarPathString.startsWith("jar:"))
      jarPathString = jarPathString.substring(4);
    if(jarPathString.startsWith("file:"))
      jarPathString = jarPathString.substring(5);
    return new JarFile(jarPathString);
  }

  protected static void writeHtmlEditorFileToTempDir(JarFile jar, JarEntry entry, File tempDir) throws IOException {
    File tempFile = new File(tempDir, entry.getName());
    try {
      tempFile.getParentFile().mkdirs();
      try { tempFile.createNewFile(); } catch(Exception ex) { }

      InputStream is = jar.getInputStream(entry); // get the input stream
      FileOutputStream fos = new FileOutputStream(tempFile);
      byte[] buf = new byte[4096];
      int r;

      while ((r = is.read(buf)) != -1) {
        fos.write(buf, 0, r);
      }

      fos.close();
      is.close();
    } catch(Exception ex) {
      log.error("Could not write Jar entry " + entry.getName() + " to temp file " + tempFile, ex);
    }
  }
}
