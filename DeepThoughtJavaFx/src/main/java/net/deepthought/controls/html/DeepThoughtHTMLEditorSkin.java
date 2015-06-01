package net.deepthought.controls.html;

import com.sun.javafx.scene.web.skin.HTMLEditorSkin;
import com.sun.javafx.webkit.Accessor;
import com.sun.webkit.WebPage;

import org.w3c.dom.html.HTMLDocument;
import org.w3c.dom.html.HTMLElement;

import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.GridPane;
import javafx.scene.web.HTMLEditor;
import javafx.scene.web.WebView;

/**
 * Created by ganymed on 24/05/15.
 */
public class DeepThoughtHTMLEditorSkin extends HTMLEditorSkin {

  protected static final String FORMAT_COMMAND = "formatblock";
  protected static final String FONT_FAMILY_COMMAND = "fontname";
  protected static final String FONT_SIZE_COMMAND = "fontsize";


  protected HTMLEditor htmlEditor = null;

  protected ResourceBundle resources = null;

  protected boolean isFirstRun = true;

  protected WebView webView = null;
  protected WebPage webPage = null;

  protected ToolBar topToolBar = null;

  protected ToolBar bottomToolBar = null;

  protected ComboBox formatComboBox = null;
  protected ComboBox fontFamilyComboBox = null;
  protected ComboBox fontSizeComboBox = null;


  public DeepThoughtHTMLEditorSkin(HTMLEditor htmlEditor) {
    super(htmlEditor);

    this.htmlEditor = htmlEditor;
    resources = ResourceBundle.getBundle(HTMLEditorSkin.class.getName());
  }

  @Override
  protected void layoutChildren(double x, double y, double w, double h) {
    super.layoutChildren(x, y, w, h);

    if(isFirstRun) {
      isFirstRun = false;
      adjustHtmlEditor();
    }
  }

  protected void adjustHtmlEditor() {
    retrieveControls();

    setDefaultValues();

    adjustToolBars();
  }

  protected void retrieveControls() {
    setWebView((WebView) htmlEditor.lookup("WebView"));

    for (Node node : htmlEditor.lookupAll("ToolBar")) { // HTMLEditor has two Toolbars
      ToolBar toolBar = (ToolBar)node;
      // Top Toolbar has 17 items, Bottom Toolbar has 10 items (but may be subject to changes in the future)
//      boolean isTopToolbar = toolBar.getStyleClass().contains("top-toolbar"); // Top Toolbar contains 'top-toolbar', Bottom Toolbar 'bottom-toolbar'
      int rowIndex = GridPane.getRowIndex(toolBar); // Top Toolbar has a GridPane row index of 0, Bottom Toolbar of 1
      if(rowIndex == 0)
        retrieveTopToolbarControls(toolBar);
      else if(rowIndex == 1)
        retrieveBottomToolbarControls(toolBar);
    }
  }

  protected void retrieveTopToolbarControls(ToolBar topToolBar) {
    this.topToolBar = topToolBar;
  }

  protected void retrieveBottomToolbarControls(ToolBar bottomToolBar) {
    this.bottomToolBar = bottomToolBar;

    for(Node child : bottomToolBar.getItems()) {
      if(child instanceof ComboBox) {
        ComboBox comboBox = (ComboBox)child;
        ObservableList items = comboBox.getItems();
        if(items.contains(resources.getString("paragraph")))
          setFormatComboBox(comboBox);
        else if(items.contains(resources.getString("small")))
          setFontSizeComboBox(comboBox);
        else if(comboBox.getTooltip() != null && comboBox.getTooltip().getText().equals(resources.getString("fontFamily"))) // fontFamilyComboBox has no items at this point of time
          setFontFamilyComboBox(comboBox);
      }
    }
  }

  protected void setWebView(WebView webView) {
    this.webView = webView;
    this.webPage = Accessor.getPageFor(webView.getEngine());
  }

  protected void setFormatComboBox(ComboBox comboBox) {
    formatComboBox = comboBox;
  }

  protected void setFontFamilyComboBox(ComboBox comboBox) {
    fontFamilyComboBox = comboBox;
  }

  protected void setFontSizeComboBox(ComboBox comboBox) {
    fontSizeComboBox = comboBox;
    Platform.runLater(() -> {
      executeCommand(FONT_SIZE_COMMAND, resources.getString("extraSmall"));
      fontSizeComboBox.setValue(resources.getString("extraSmall"));
      executeCommand(FONT_SIZE_COMMAND, resources.getString("extraSmall"));
    });
  }

  protected void setDefaultValues() {
    htmlEditor.setMinHeight(120);
    htmlEditor.setPrefHeight(150);

    executeCommand(FONT_SIZE_COMMAND, resources.getString("extraSmall"));
    htmlEditor.setStyle(htmlEditor.getStyle() + " -fx-font: 11 arial;");
  }

  protected void adjustToolBars() {

  }

  protected boolean executeCommand(String command, String value) {
    return webPage.executeCommand(command, value);
  }

  protected boolean isCommandEnabled(String command) {
    return webPage.queryCommandEnabled(command);
  }

  protected boolean getCommandState(String command) {
    return webPage.queryCommandState(command);
  }

  protected String getCommandValue(String command) {
    return webPage.queryCommandValue(command);
  }

}
