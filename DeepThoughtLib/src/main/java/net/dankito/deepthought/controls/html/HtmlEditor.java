package net.dankito.deepthought.controls.html;

import net.dankito.deepthought.controls.ICleanUp;
import net.dankito.deepthought.data.html.ImageElementData;
import net.dankito.deepthought.Application;
import net.dankito.deepthought.util.ObjectHolder;
import net.dankito.deepthought.util.OsHelper;
import net.dankito.deepthought.util.file.FileUtils;

import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
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

  public static final String JavaScriptCommandGetHtml = "CKEDITOR.instances.editor.getData()";


  protected static String unzippedHtmlEditorFilePath = null;


  protected IJavaScriptExecutor scriptExecutor = null;

  protected boolean ckEditorLoaded = false;

  protected String htmlToSetWhenLoaded = null;

  protected boolean editorHasBeenNewlyInitialized = false;

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

  public void releaseData() {
    scriptExecutor.executeScript(CKEditorInstanceName + ".document.clearCustomData();");
    scriptExecutor.executeScript(CKEditorInstanceName + ".document.removeAllListeners();");
    scriptExecutor.executeScript(CKEditorInstanceName + ".window.getFrame().clearCustomData();");
    scriptExecutor.executeScript(CKEditorInstanceName + ".window.getFrame().removeAllListeners();");
  }


  public boolean isCKEditorLoaded() {
    return ckEditorLoaded;
  }

  public String getHtml() {
    return getHtmlAsyncViaJavaScript();
  }

  protected String getHtmlAsyncViaJavaScript() {
    try {
      final ObjectHolder<Object> htmlHolder = new ObjectHolder<>();
      final CountDownLatch waitForAsyncResponseLatch = new CountDownLatch(1);

      // TODO: this code is really ugly: As for Android Version 19 and above this method has to run on a different thread than the UI thread (otherwise the call to
      // evaluateScript() would block), but for JavaFX this method must run on UI thread an no new Thread may be created
      if(OsHelper.isRunningOnAndroid() == false) {
        getHtmlViaJavaScript(htmlHolder, waitForAsyncResponseLatch);
      }
      else {
        new Thread(new Runnable() {
          @Override
          public void run() {
            getHtmlViaJavaScript(htmlHolder, waitForAsyncResponseLatch);
          }
        }).start();
      }

      try { waitForAsyncResponseLatch.await(5, TimeUnit.SECONDS); } catch(Exception ex) { }

      if(htmlHolder.get() != null) {
        return getUnescapedResponse(htmlHolder.get());
      }
    } catch(Exception ex) {
      log.error("Could not get HtmlEditor's html text", ex);
    }

    return "";
  }

  protected void getHtmlViaJavaScript(final ObjectHolder<Object> htmlHolder, final CountDownLatch waitForAsyncResponseLatch) {
    scriptExecutor.executeScript(JavaScriptCommandGetHtml, new ExecuteJavaScriptResultListener() {
      @Override
      public void scriptExecuted(Object result) {
        if("\"\"".equals(result)) { // on empty html '""' is return -> make string empty
          result = "";
        }

        htmlHolder.set(result);
        waitForAsyncResponseLatch.countDown();
      }
    });
  }

  private String getUnescapedResponse(Object response) {
    String responseAsString = response instanceof String ? (String)response : response.toString();

    if(responseAsString.startsWith("\"") && responseAsString.endsWith("\"")) {
      responseAsString = responseAsString.substring(1, responseAsString.length() - 1);

      responseAsString = StringEscapeUtils.unescapeEcmaScript(responseAsString);
    }

    return responseAsString;
  }

  public void setHtml(String html) {
    setHtml(html, false);
  }

  public void setHtml(String html, boolean resetUndoStack) {
    if(html == null)
      html = "";

    try {
      if(isCKEditorLoaded() == false)
        htmlToSetWhenLoaded = html; // save html so that it can be set as soon as CKEditor is loaded
      else {
//        scriptExecutor.executeScript(CKEditorInstanceName + ".setData('" + StringEscapeUtils.escapeEcmaScript(html) + "')");
        scriptExecutor.executeScript("setHtml('" + StringEscapeUtils.escapeEcmaScript(html) + "', " + resetUndoStack + ")");
        htmlToSetWhenLoaded = null;
      }
    } catch(Exception ex) {
      log.error("Could not set HtmlEditor's html text", ex);
    }
  }

  public void setHtmlHasBeenSaved() {
    scriptExecutor.executeScript("setHtmlHasBeenSaved()");
  }

  public void showContextMenuAtPosition(int x, int y) {
    scriptExecutor.executeScript("showContextMenu(" + x + ", " + y + ")");
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

    scriptExecutor.setJavaScriptMember("app", null);

    this.scriptExecutor = null;
  }


  /*    Methods over which JavaScript running in Browser communicates with Java code        */

  public void ckEditorLoaded() {
    ckEditorLoaded = true;

    scriptExecutor.executeScript("resizeEditorToFitWindow()"); // don't know why but without calling it CKEditor doesn't size correctly

    if(htmlToSetWhenLoaded != null) {
      new Timer().schedule(new TimerTask() {
        @Override
        public void run() {
          setHtml(htmlToSetWhenLoaded, true);
        }
      }, 300); // i don't know why but executing Script immediately results in an error (maybe the JavaScript code is blocked till method is finished -> wait some (unrecognizable) time
    }

    if(listener != null)
      listener.editorHasLoaded(this);
  }

  public void htmlChanged() {
    if (listener != null) {
      listener.htmlCodeUpdated();
    }

    if(editorHasBeenNewlyInitialized == true) {
      editorHasBeenNewlyInitialized = false;
    }
  }

  public void htmlHasBeenReset() {
    if(listener != null) {
      listener.htmlCodeHasBeenReset();
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
    //FileUtils.deleteFile(htmlEditorDirectory); // if CKEditor_start.html has been updated

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

      JarFile jar = FileUtils.getJarFileForHtmlEditor();
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
