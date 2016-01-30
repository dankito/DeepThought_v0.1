package net.deepthought.util.localization;

import net.deepthought.controls.utils.FXUtils;
import net.deepthought.data.model.enums.ApplicationLanguage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.ResourceBundle;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Control;
import javafx.scene.control.Labeled;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumnBase;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * Created by ganymed on 22/12/14.
 */
public class JavaFxLocalization {

  private final static Logger log = LoggerFactory.getLogger(JavaFxLocalization.class);


  static {
    Localization.addLanguageChangedListener(language -> setLocale(Localization.getLanguageLocale()));
  }


  public final static ResourceBundle Resources = new AvoidFxmlLoaderTranslatesResourceKeysResourceBundle(net.deepthought.util.localization.Localization.getStringsResourceBundle());

  private final static ObjectProperty<Locale> locale = new SimpleObjectProperty<>(Locale.getDefault());

  public static ObjectProperty<Locale> localeProperty() {
    return locale ;
  }

  public static Locale getLocale() {
    return locale.get();
  }

  public static void setLocale(Locale locale) {
    FXUtils.runOnUiThread(() -> localeProperty().set(locale));
  }

  public static void setLocaleForLanguage(ApplicationLanguage language) {
    try {
      setLocale(Locale.forLanguageTag(language.getLanguageKey()));
    } catch(Exception ex) {
      log.error("Could not find Locale for ApplicationLanguage's LanguageKey " + language.getLanguageKey() + " of ApplicationLanguage " + language.getName(), ex);
    }
  }


  public static void bindLabeledText(Labeled labeled, final String key, final Object... formatArguments) {
    labeled.textProperty().bind(Bindings.createStringBinding(
        () -> net.deepthought.util.localization.Localization.getLocalizedString(key, formatArguments), JavaFxLocalization.localeProperty()));
  }

  public static void bindMenuItemText(MenuItem menuItem, final String key, final Object... formatArguments) {
    menuItem.textProperty().bind(Bindings.createStringBinding(
        () -> net.deepthought.util.localization.Localization.getLocalizedString(key, formatArguments), JavaFxLocalization.localeProperty()));
  }

  public static void bindTableColumnBaseText(TableColumnBase column, final String key, final Object... formatArguments) {
    column.textProperty().bind(Bindings.createStringBinding(
        () -> net.deepthought.util.localization.Localization.getLocalizedString(key, formatArguments), JavaFxLocalization.localeProperty()));
  }

  public static void bindTabText(Tab tab, final String key, final Object... formatArguments) {
    tab.textProperty().bind(Bindings.createStringBinding(
        () -> net.deepthought.util.localization.Localization.getLocalizedString(key, formatArguments), JavaFxLocalization.localeProperty()));
  }

  public static void bindTableColumnText(TableColumnBase tableColumn, final String key, final Object... formatArguments) {
    tableColumn.textProperty().bind(Bindings.createStringBinding(
        () -> net.deepthought.util.localization.Localization.getLocalizedString(key, formatArguments), JavaFxLocalization.localeProperty()));
  }

  public static void bindControlToolTip(Control control, String key, Object... formatArguments) {
    control.setTooltip(createBoundTooltip(key, formatArguments));
  }

  // usage: myButton.setTooltip(createBoundTooltip("mybutton"));
  public static Tooltip createBoundTooltip(final String key, final Object[] formatArguments) {
    Tooltip tooltip = new Tooltip();
    tooltip.textProperty().bind(Bindings.createStringBinding(
        () -> net.deepthought.util.localization.Localization.getLocalizedString(key, formatArguments), JavaFxLocalization.localeProperty()));
    return tooltip ;
  }

  public static void bindTextInputControlPromptText(TextInputControl control, final String key, final Object... formatArguments) {
    control.promptTextProperty().bind(Bindings.createStringBinding(
        () -> net.deepthought.util.localization.Localization.getLocalizedString(key, formatArguments), JavaFxLocalization.localeProperty()));
  }

  public static void bindStageTitle(Stage stage, final String key, final Object... formatArguments) {
    stage.titleProperty().bind(Bindings.createStringBinding(
        () -> net.deepthought.util.localization.Localization.getLocalizedString(key, formatArguments), JavaFxLocalization.localeProperty()));
  }


  public static void resolveResourceKeys(Node node) {
    resolveNodeResourceKeys(node);
  }

  protected static void resolveNodeResourceKeys(Node node) {
    if(node instanceof Labeled)
      resolveLabeledResourceKeys((Labeled)node);
    else if(node instanceof TextInputControl)
      resolveTextInputControlResourceKeys((TextInputControl) node);
    else if(node instanceof TableView) {
      for(TableColumn column : (ObservableList<TableColumn>)((TableView) node).getColumns())
        resolveTableColumnBaseResourceKeys(column);
    }
    else if(node instanceof TreeTableView) {
      for(TreeTableColumn column : (ObservableList<TreeTableColumn>)((TreeTableView)node).getColumns())
        resolveTableColumnBaseResourceKeys(column);
    }
    else if(node instanceof ScrollPane) {
      if(((ScrollPane)node).getContent() != null)
        resolveNodeResourceKeys(((ScrollPane)node).getContent());
    }
    else if(node instanceof SplitPane) {
      for(Node child : ((SplitPane)node).getItems())
        resolveNodeResourceKeys(child);
    }
    else if(node instanceof TabPane) {
      for(Tab tab : ((TabPane)node).getTabs())
        resolveTabResourceKeys(tab);
    }
    else if(node instanceof MenuBar) {
      for(Menu menu : ((MenuBar)node).getMenus())
        resolveMenuItemResourceKeys(menu);
    }
    else if(node instanceof BorderPane) {
      resolveBorderPaneResourceKeys((BorderPane) node);
    }
    else if(node instanceof Parent)
      resolveChildrenResourceKeys((Parent) node);
  }

  protected static void resolveChildrenResourceKeys(Parent parent) {
    for(Node child : parent.getChildrenUnmodifiable()) {
      resolveNodeResourceKeys(child);
    }
  }

  protected static void resolveLabeledResourceKeys(Labeled labeled) {
    if(hasResourceKeyPrefix(labeled.getText()))
      JavaFxLocalization.bindLabeledText(labeled, extractResourceKey(labeled.getText()));

    if(labeled.getGraphic() != null)
      resolveNodeResourceKeys(labeled.getGraphic());

    if(labeled instanceof TitledPane)
      resolveNodeResourceKeys(((TitledPane) labeled).getContent());
  }

  protected static void resolveTextInputControlResourceKeys(TextInputControl textInputControl) {
    if(hasResourceKeyPrefix(textInputControl.getPromptText()))
      JavaFxLocalization.bindTextInputControlPromptText(textInputControl, extractResourceKey(textInputControl.getPromptText()));
  }

  protected static void resolveTableColumnBaseResourceKeys(TableColumnBase column) {
    if(hasResourceKeyPrefix(column.getText()))
      JavaFxLocalization.bindTableColumnBaseText(column, extractResourceKey(column.getText()));

    if(column.getGraphic() != null)
      resolveNodeResourceKeys(column.getGraphic());
  }

  protected static void resolveMenuItemResourceKeys(MenuItem item) {
    if(hasResourceKeyPrefix(item.getText()))
      JavaFxLocalization.bindMenuItemText(item, extractResourceKey(item.getText()));

    if(item instanceof Menu) {
      Menu menu = (Menu)item;
      for(MenuItem subItem : menu.getItems())
        resolveMenuItemResourceKeys(subItem);
    }
  }

  protected static void resolveTabResourceKeys(Tab tab) {
    if(hasResourceKeyPrefix(tab.getText()))
      JavaFxLocalization.bindTabText(tab, extractResourceKey(tab.getText()));

    if (tab.getGraphic() != null)
      resolveNodeResourceKeys(tab.getGraphic());
    resolveNodeResourceKeys(tab.getContent());
  }

  protected static void resolveBorderPaneResourceKeys(BorderPane node) {
    BorderPane borderPane = node;

    if(borderPane.getTop() != null)
      resolveNodeResourceKeys(borderPane.getTop());
    if(borderPane.getLeft() != null)
      resolveNodeResourceKeys(borderPane.getLeft());
    if(borderPane.getBottom() != null)
      resolveNodeResourceKeys(borderPane.getBottom());
    if(borderPane.getRight() != null)
      resolveNodeResourceKeys(borderPane.getRight());
    if(borderPane.getCenter() != null)
      resolveNodeResourceKeys(borderPane.getCenter());
  }


  protected static boolean hasResourceKeyPrefix(String text) {
    return text != null && text.startsWith(FXMLLoader.RESOURCE_KEY_PREFIX);
  }

  protected static String extractResourceKey(String internationalizationKey) {
    if(internationalizationKey.startsWith(FXMLLoader.RESOURCE_KEY_PREFIX))
      return internationalizationKey.substring(FXMLLoader.RESOURCE_KEY_PREFIX.length());

    return internationalizationKey;
  }

}
