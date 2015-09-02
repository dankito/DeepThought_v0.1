package net.deepthought.controls;

import net.deepthought.Application;
import net.deepthought.controls.event.HtmlEditorTextChangedEvent;
import net.deepthought.data.model.Tag;
import net.deepthought.data.model.settings.ColumnSettings;
import net.deepthought.data.model.settings.WindowSettings;
import net.deepthought.data.search.specific.FilterTagsSearchResults;
import net.deepthought.util.Localization;
import net.deepthought.util.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.css.Styleable;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Cell;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToolBar;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.web.HTMLEditor;
import javafx.stage.Stage;
import javafx.util.Callback;

/**
 * Created by ganymed on 04/02/15.
 */
public class FXUtils {

  public final static String HtmlEditorDefaultText = "<html dir=\"ltr\"><head></head><body contenteditable=\"true\"></body></html>";


  private final static Logger log = LoggerFactory.getLogger(FXUtils.class);


  public static void ensureNodeOnlyUsesSpaceIfVisible(Node node) {
    node.managedProperty().bind(node.visibleProperty());
  }

  public static void setBackgroundToColor(Region region, Color color) {
    region.setBackground(new Background(new BackgroundFill(color, new CornerRadii(0), new Insets(0))));
  }

  public static void setTagCellBackgroundColor(Tag tag, FilterTagsSearchResults filterTagsSearchResults, Cell cell) {
    if(tag != null && filterTagsSearchResults.getResults().size() > 0) {
      if(filterTagsSearchResults.isExactMatchOfLastSearchTerm(tag))
        cell.setBackground(Constants.FilteredTagsLastSearchTermExactMatchBackground);
      else if(filterTagsSearchResults.isRelevantMatchOfLastSearchTerm(tag))
//        cell.setBackground(Constants.FilteredTagsLastSearchTermRelevantMatchBackground);
        cell.setBackground(Constants.FilteredTagsDefaultBackground);
      else if(filterTagsSearchResults.isExactMatch(tag) || filterTagsSearchResults.isSingleMatchOfASearchTerm(tag))
        cell.setBackground(Constants.FilteredTagsExactMatchBackground);
      else if(filterTagsSearchResults.isRelevantMatch(tag))
        cell.setBackground(Constants.FilteredTagsRelevantMatchBackground);
      else
        cell.setBackground(Constants.FilteredTagsDefaultBackground);
    } else
      cell.setBackground(Constants.FilteredTagsDefaultBackground);
  }

  public static void applyWindowSettingsAndListenToChanges(Stage stage, WindowSettings settings) {
    if(settings == null)
      return;

    if(settings.getX() > 0)
      stage.setX(settings.getX());
    if(settings.getY() > 0)
      stage.setY(settings.getY());
    if(settings.getWidth() > 0)
      stage.setWidth(settings.getWidth());
    if(settings.getHeight() > 0)
      stage.setHeight(settings.getHeight());

    // TODO: remove these listeners if DeepThought changes
    stage.xProperty().addListener(((observableValue, oldValue, newValue) -> settings.setX(newValue.doubleValue())));
    stage.yProperty().addListener(((observableValue, oldValue, newValue) -> settings.setY(newValue.doubleValue())));
    stage.widthProperty().addListener(((observableValue, oldValue, newValue) -> settings.setWidth(newValue.doubleValue())));
    stage.heightProperty().addListener(((observableValue, oldValue, newValue) -> settings.setHeight(newValue.doubleValue())));
  }

  public static void applyColumnSettingsAndListenToChanges(TableColumn column, ColumnSettings settings) {
    column.setVisible(settings.isVisible());
    column.setPrefWidth(settings.getWidth());

    column.visibleProperty().addListener(((observableValue, oldValue, newValue) -> settings.setIsVisible(newValue)));
    column.widthProperty().addListener(((observableValue, oldValue, newValue) -> settings.setWidth(newValue.intValue())));
  }


  public static boolean loadControl(Object controller, String controlName) {
    FXMLLoader fxmlLoader = new FXMLLoader(controller.getClass().getClassLoader().getResource("controls/" + controlName + ".fxml"));
    fxmlLoader.setRoot(controller);
    fxmlLoader.setController(controller);
    fxmlLoader.setResources(Localization.getStringsResourceBundle());

    try {
      fxmlLoader.load();
      return true;
    } catch (IOException ex) {
      log.error("Could not load " + controlName, ex);
    }

    return false;
  }


  public static void cleanUpChildrenAndClearPane(Pane pane) {
    for(Node node : pane.getChildren()) {
      if(node instanceof ICleanableControl) {
        ICleanableControl label = (ICleanableControl)node;
        label.cleanUpControl();
      }
    }

    pane.getChildren().clear();
  }


  /**
   * A bit hackish, but i didn't find another way to focus a node (see http://stackoverflow.com/questions/20594035/how-to-focus-a-specific-node-when-selected-tab-changes-in-javafx)
   * @param nodeToFocus
   */
  public static void focusNode(final Node nodeToFocus) {
    Application.getThreadPool().runTaskAsync(new Task<Void>() {
      @Override
      protected Void call() throws Exception {// This is NOT on FX thread
        Thread.sleep(100);
        return null;
      }

      @Override
      public void succeeded() { // This is called on FX thread.
        nodeToFocus.requestFocus();
      }
    });
  }


  public static void showSplitPaneDividers(SplitPane splitPane, boolean show) {
    splitPane.lookupAll(".split-pane-divider").stream()
        .forEach(div ->  div.setMouseTransparent(!show) );
  }


  public static boolean hasHtmlEditorDefaultText(HTMLEditor htmlEditor) {
    return FXUtils.HtmlEditorDefaultText.equals(htmlEditor.getHtmlText());
  }

  public static boolean htmlTextIsNullOrEmptyOrHasHtmlEditorDefaultText(HTMLEditor htmlEditor) {
    return htmlTextIsNullOrEmptyOrHasHtmlEditorDefaultText(htmlEditor.getHtmlText());
  }

  public static boolean htmlTextIsNullOrEmptyOrHasHtmlEditorDefaultText(String htmlText) {
    return StringUtils.isNullOrEmpty(htmlText) || FXUtils.HtmlEditorDefaultText.equals(htmlText);
  }

  public static void addHtmlEditorTextChangedListener(final HTMLEditor editor, final HtmlEditorTextChangedEvent textChangedEvent) {
    editor.setOnKeyReleased(new EventHandler<KeyEvent>() {
      @Override
      public void handle(KeyEvent event) {
        if (isValidEvent(event)) {
          textChangedEvent.textChanged(editor);
        }
      }
    });

    // Also fire Event when one of the Toolbar Buttons has been pressed
    final Map<Node, EventHandler> mapOriginalEvents = new HashMap<>();
    final List<ComboBox> comboBoxesSelectedItemChangedCalledYet = new ArrayList<>();
    for (Node node : editor.lookupAll("ToolBar")) { // HTMLEditor has two Toolbars, initially their items are empty -> add ChangeListener to items
      ToolBar toolBar = (ToolBar)node;
      toolBar.getItems().addListener(new ListChangeListener<Node>() {
        @Override
        public void onChanged(Change<? extends Node> c) {
          if(c.next() && c.wasAdded()) {
            Node addedNode = c.getAddedSubList().get(0);
            if(addedNode instanceof Button || addedNode instanceof ToggleButton) {
              final ButtonBase button = (ButtonBase) addedNode;
              mapOriginalEvents.put(button, button.getOnAction());
              button.setOnAction(event -> {
                textChangedEvent.textChanged(editor);
                if(mapOriginalEvents.get(button) != null)
                  mapOriginalEvents.get(button).handle(event);
              });
              button.setOnMouseClicked(event -> textChangedEvent.textChanged(editor));
            }
            else if(addedNode instanceof ComboBox) {
              final ComboBox comboBox = (ComboBox)addedNode;
              comboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                if(comboBoxesSelectedItemChangedCalledYet.contains(comboBox) == false) // when SelectedItem Change Listener gets called for the first time, ComboBox' default item gets set
                  comboBoxesSelectedItemChangedCalledYet.add(comboBox);
                else
                  textChangedEvent.textChanged(editor);
                });
            }
            else if(addedNode instanceof ColorPicker) {
              addedNode.setOnMouseClicked(event -> textChangedEvent.textChanged(editor));
            }
          }
        }
      });
    }
  }

  protected static boolean isValidEvent(KeyEvent event) {
    return !isSelectAllEvent(event)
        && ((isPasteEvent(event)) || isCharacterKeyReleased(event));
  }

  protected static boolean isSelectAllEvent(KeyEvent event) {
    return event.isShortcutDown() && event.getCode() == KeyCode.A;
  }

  protected static boolean isPasteEvent(KeyEvent event) {
    return event.isShortcutDown() && event.getCode() == KeyCode.V;
  }

  protected static boolean isCharacterKeyReleased(KeyEvent event) {
    // Make custom changes here..
    switch (event.getCode())
    {
      case ALT:
      case COMMAND:
      case CONTROL:
      case SHIFT:
        return false;
      default:
        return true;
    }
  }


  public static void addStyleToCurrentStyle(Node node, String styleToAdd) {
    String style = addStyleToCurrentStyleString(node, styleToAdd);
    node.setStyle(style);
  }

  public static void addStyleToCurrentStyle(MenuItem item, String styleToAdd) {
    String style = addStyleToCurrentStyleString(item, styleToAdd);
    item.setStyle(style);
  }

  protected static String addStyleToCurrentStyleString(Styleable styleable, String styleToAdd) {
    String style = styleable.getStyle();
    style = style == null ? styleToAdd : style + " " + styleToAdd;
    return style;
  }

  public static boolean isNoModifierPressed(KeyEvent event) {
    return event.isControlDown() == false && event.isShiftDown() == false && event.isAltDown() == false && event.isMetaDown() == false && event.isShortcutDown() == false;
  }


}
