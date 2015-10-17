package net.deepthought.controls.html;

import net.deepthought.Application;
import net.deepthought.controls.ICleanUp;
import net.deepthought.data.html.ImageElementData;
import net.deepthought.util.file.FileUtils;

import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * A Java Wrapper class for the JavaScript CKEditor.
 *
 * Created by ganymed on 28/08/15.
 */
public class HtmlEditor implements IJavaScriptBridge, ICleanUp {

  private final static Logger log = LoggerFactory.getLogger(HtmlEditor.class);


  public final static String HtmlEditorFolderName = "htmleditor";

  public final static String HtmlEditorFileName = "CKEditor_start.html";

  public final static String HtmlEditorFolderAndFileName = new File(HtmlEditorFolderName, HtmlEditorFileName).getPath();


  protected static final String CKEditorInstanceName = "CKEDITOR.instances.editor";


  protected static String unzippedHtmlEditorFilePath = null;


  protected IJavaScriptExecutor scriptExecutor = null;

  protected boolean ckEditorLoaded = false;

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

  /**
   * When the Web Browser Control (WebView) has loaded, this is the earliest point of time to execute JavaScript
   */
  public void webControlLoaded() {
    scriptExecutor.setJavaScriptMember("app", this);
  }

  public void insertHtml(String html) {
    scriptExecutor.executeScript(CKEditorInstanceName + ".insertHtml('" + StringEscapeUtils.escapeEcmaScript(html) + "', 'unfiltered_html')");
  }

  public void scrollTo(int scrollPosition) {
    scriptExecutor.executeScript("$(" + CKEditorInstanceName + ".document.$).scrollTop(" + scrollPosition + ");");
  }

  public void resetUndoStack() {
    scriptExecutor.executeScript(CKEditorInstanceName + ".resetUndo()");
  }


  public boolean isCKEditorLoaded() {
    return ckEditorLoaded;
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
        htmlToSetWhenLoaded = html; // save html so that it can be set as soon as CKEditor is loaded
      else {
        scriptExecutor.executeScript(CKEditorInstanceName + ".setData('" + StringEscapeUtils.escapeEcmaScript(html) + "')");
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

    scriptExecutor.setJavaScriptMember("app", null);

    this.scriptExecutor = null;
  }


  /*    Methods over which JavaScript running in Browser communicates with Java code        */

  public void ckEditorLoaded() {
    scriptExecutor.executeScript("resizeEditorToFitWindow()"); // don't know why but without calling it CKEditor doesn't size correctly

    ckEditorLoaded = true;

    if (htmlToSetWhenLoaded != null) {
      new Timer().schedule(new TimerTask() {
        @Override
        public void run() {
          setHtml(htmlToSetWhenLoaded);
        }
      }, 100); // i don't know why but executing Script immediately results in an error (maybe the JavaScript code is blocked till method is finished -> wait some (unrecognizable) time
    }

    if(listener != null)
      listener.editorHasLoaded(this);
  }

  public void htmlChanged(String newHtmlCode) {
    if(previousHtml.equals(newHtmlCode) == false) {
      if (listener != null) {
        listener.htmlCodeUpdated(newHtmlCode); // TODO: may also pass previousHtml as parameter
      }
    }

    previousHtml = newHtmlCode;

    if(editorHasBeenNewlyInitialized == true || resetUndoStack == true) {
      resetUndoStack();
      editorHasBeenNewlyInitialized = false;
      resetUndoStack = false;
    }
  }

  public boolean elementClicked(String element, int button, int clickX, int clickY) {
    return true;
  }

  public boolean elementDoubleClicked(String element) {
    if(listener != null) {
      if (isImageElement(element)) {
        List<ImageElementData> imageElements = Application.getHtmlHelper().extractAllImageElementsFromHtml(element);
        if (imageElements.size() > 0) {
          ImageElementData imgElement = imageElements.get(0);
          return listener.elementDoubleClicked(this, imgElement);
        }
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
    scriptExecutor.executeScript("replaceImageElement(" + previousElement.getEmbeddingId() + ", '" + StringEscapeUtils.escapeEcmaScript(newElement.createHtmlCode()) + "')");
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
//    FileUtils.deleteFile(htmlEditorDirectory); // if CKEditor_start.html has been updated

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
