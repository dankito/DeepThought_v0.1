package net.deepthought.controls.html;

import net.deepthought.Application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import netscape.javascript.JSObject;

/**
 * A Java Wrapper class for the JavaScript CKEditor.
 *
 * Created by ganymed on 28/08/15.
 */
public class HtmlEditor {

  public final static String HtmlEditorFolderName = "htmleditor";

  public final static String HtmlEditorFileName = "CKEditor_start.html";

  public final static String HtmlEditorFolderAndFileName = new File(HtmlEditorFolderName, HtmlEditorFileName).getPath();

  public final static int SizeAdjustment = 16;


  private final static Logger log = LoggerFactory.getLogger(HtmlEditor.class);


  protected static String unzippedHtmlEditorFilePath = null;


  protected IJavaScriptExecutor scriptExecutor = null;

  protected JSObject ckEditor = null;

  protected String htmlToSetWhenLoaded = null;

  protected HtmlEditorListener listener = null;



  public HtmlEditor(IJavaScriptExecutor scriptExecutor) {
    this.scriptExecutor = scriptExecutor;
  }

  public HtmlEditor(IJavaScriptExecutor scriptExecutor, HtmlEditorListener listener) {
    this(scriptExecutor);
    this.listener = listener;
  }


  public String getHtmlEditorPath() {
    return unzippedHtmlEditorFilePath;
  }

  public void editorLoaded(int width, int height) {
    try {
      JSObject win = (JSObject) scriptExecutor.executeScript("window");
      win.setMember("app", this);

      ckEditor = (JSObject)scriptExecutor.executeScript("CKEDITOR.instances.editor");

      scriptExecutor.executeScript("CKEDITOR.instances.editor.on('loaded', function() {" +
              "CKEDITOR.instances.editor.resize(" + (width - SizeAdjustment) + ", " + (height - SizeAdjustment) + ");" +
          "});");

      if (htmlToSetWhenLoaded != null)
        setHtml(htmlToSetWhenLoaded);
    } catch(Exception ex) {
      log.error("Could not setup HtmlEditor in loaded event", ex);
    }
  }


  public boolean isCKEditorLoaded() {
    return ckEditor != null;
  }

  public String getHtml() {
    try {
      if(isCKEditorLoaded()) {
//        Object obj = scriptExecutor.executeScript("CKEDITOR.instances.editor.getData();");
        Object obj = ckEditor.call("getData");
        return obj.toString();
      }
    } catch(Exception ex) {
      log.error("Could not get HtmlEditor's html text", ex);
    }

    return "";
  }

  public void setHtml(String html) {
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

  public void setSize(int width, int height) {
    if(isCKEditorLoaded())
//      scriptExecutor.executeScript("CKEDITOR.instances.editor.resize(" + width + ", " + height + ");");
      ckEditor.call("resize", width - SizeAdjustment, height - SizeAdjustment);
  }

  public HtmlEditorListener getListener() {
    return listener;
  }

  public void setListener(HtmlEditorListener listener) {
    this.listener = listener;
  }


  /*    Methods over which JavaScript running in Browser communicates with Java code        */

  public void htmlChanged(String newHtmlCode) {
    if(listener != null)
      listener.htmlCodeUpdated(newHtmlCode);
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
