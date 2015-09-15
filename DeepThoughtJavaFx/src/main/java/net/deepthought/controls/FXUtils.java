package net.deepthought.controls;

import net.deepthought.Application;
import net.deepthought.data.model.Tag;
import net.deepthought.data.model.settings.ColumnSettings;
import net.deepthought.data.model.settings.WindowSettings;
import net.deepthought.data.search.specific.FilterTagsSearchResults;
import net.deepthought.util.Localization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

import javafx.concurrent.Task;
import javafx.css.Styleable;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Cell;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.Window;

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
      // TODO: logic is wrong: if exact or relevant match of last search term is already included in result list (e.g. exact match of previous term) then it gets to wrong color
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
    }
    else
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
        .forEach(div -> div.setMouseTransparent(!show));
  }

  public static Point2D getNodeScreenCoordinates(Node node) {
    // thanks for pointing me in the right direction how to calculate a Node's Screen position to: http://blog.crisp.se/2012/08/29/perlundholm/window-scene-and-node-coordinates-in-javafx
    Scene scene = node.getScene();
    Window window = scene.getWindow();

    Point2D windowCoord = new Point2D(window.getX(), window.getY());
    Point2D sceneCoord = new Point2D(scene.getX(), scene.getY());
    Point2D nodeCoord = node.localToScene(0, 0);

    return new Point2D(Math.round(windowCoord.getX() + sceneCoord.getX() + nodeCoord.getX()),
                       Math.round(windowCoord.getY() + sceneCoord.getY() + nodeCoord.getY()));
  }

  public static Screen getScreenWindowLeftUpperCornerIsIn(Window window) {
    List<Screen> screens = Screen.getScreensForRectangle(window.getX(), window.getY(), 1, 1);
    if(screens.size() > 0)
      return screens.get(0);

    return null;
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
