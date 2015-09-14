package net.deepthought.controls;

import javafx.beans.DefaultProperty;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * Created by ganymed on 13/09/15.
 */
@DefaultProperty("content")
public class CollapsiblePane extends GridPane {

  public final static String ExpandedText = "▼";

  public final static String CollapsedText = "▶";


  protected GridPane layoutPane = new GridPane();

  protected Button expandButton = new Button();

  protected ColumnConstraints clmcstrTitle;

  protected RowConstraints rwcstrTitle;

  protected RowConstraints rwcstrContent;


  protected BooleanProperty expanded;
  public final void setExpanded(boolean value) {
    expandedProperty().set(value);
  }
  public final boolean isExpanded() {
    return expanded == null ? false : expanded.get();
  }

  public final BooleanProperty expandedProperty() {
    if (expanded == null) {
      expanded = new BooleanPropertyBase(true) {
        @Override protected void invalidated() {
//          pseudoClassStateChanged(PSEUDO_CLASS_CANCEL, get());
          setExpandedState();
        }

        @Override
        public Object getBean() {
          return CollapsiblePane.this;
        }

        @Override
        public String getName() {
          return "expanded";
        }
      };
    }
    return expanded;
  }


  // --- Title
  private ObjectProperty<Node> title;

  /**
   * <p> The title of the TitlePane which can be any Node
   * such as UI controls or groups of nodes added to a layout container.
   *
   * @param value The title for this TitlePane.
   */
  public final void setTitle(Node value) {
    titleProperty().set(value);
  }

  /**
   * The title of the TitledPane.  {@code Null} is returned when
   * if there is no title.
   *
   * @return The title of this TitledPane.
   */
  public final Node getTitle() {
    return title == null ? null : title.get();
  }

  /**
   * The title of the TitledPane.
   *
   * @return The title of the TitlePane.
   */
  public final ObjectProperty<Node> titleProperty() {
    if (title == null) {
      title = new SimpleObjectProperty<Node>(this, "content");
      title.addListener((observable, oldValue, newValue) -> titleChanged(oldValue, newValue));
    }
    return title;
  }


  // --- Content
  private ObjectProperty<Node> content;

  /**
   * <p> The content of the TitlePane which can be any Node
   * such as UI controls or groups of nodes added to a layout container.
   *
   * @param value The content for this TitlePane.
   */
  public final void setContent(Node value) {
    contentProperty().set(value);
  }

  /**
   * The content of the TitledPane.  {@code Null} is returned when
   * if there is no content.
   *
   * @return The content of this TitledPane.
   */
  public final Node getContent() {
    return content == null ? null : content.get();
  }

  /**
   * The content of the TitledPane.
   *
   * @return The content of the TitlePane.
   */
  public final ObjectProperty<Node> contentProperty() {
    if (content == null) {
      content = new SimpleObjectProperty<Node>(this, "content");
      content.addListener((observable, oldValue, newValue) -> contentChanged(oldValue, newValue));
    }
    return content;
  }


  public CollapsiblePane() {
    setupPane();
  }

  protected void setupPane() {
    setMaxHeight(Double.MAX_VALUE);
    setMaxWidth(Double.MAX_VALUE);

    this.getColumnConstraints().add(new ColumnConstraints(28));
    clmcstrTitle = new ColumnConstraints(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE, Double.MAX_VALUE, Priority.ALWAYS, HPos.LEFT, true);
    this.getColumnConstraints().add(clmcstrTitle);

    rwcstrTitle = new RowConstraints(24, USE_COMPUTED_SIZE, Double.MAX_VALUE, Priority.SOMETIMES, VPos.CENTER, false);
    this.getRowConstraints().add(rwcstrTitle);
    rwcstrContent = new RowConstraints(0, Region.USE_COMPUTED_SIZE, Double.MAX_VALUE, Priority.ALWAYS, VPos.TOP, true);
    this.getRowConstraints().add(rwcstrContent);

    expandButton.setContentDisplay(ContentDisplay.TEXT_ONLY);
    expandButton.setFont(new Font(expandButton.getFont().getName(), 9));
    expandButton.setText(ExpandedText);
    expandButton.setMinHeight(24);
    expandButton.setMaxHeight(24);
    expandButton.setMinWidth(24);
    expandButton.setMaxWidth(24);
    expandButton.setOnAction(event -> toggleExpandedState());
    this.getChildren().add(expandButton);
    GridPane.setConstraints(expandButton, 0, 0);

    setExpanded(true);
  }

  @Override
  protected double computeMinHeight(double width) {
    double height = getTitleHeight();

    if(isExpanded())
      height += getContent() instanceof Region ? ((Region)getContent()).getMinHeight() : 0;

    return height;
  }

  @Override
  protected double computePrefHeight(double width) {
    double height = getTitleHeight();

    if(isExpanded())
      height += getContent() instanceof Region ? ((Region)getContent()).getPrefHeight() : 0;

    return height;
  }

  @Override
  protected double computeMaxHeight(double width) {
    double height = getTitleHeight();

    if(isExpanded())
      height += getContent() instanceof Region ? ((Region)getContent()).getMaxHeight() : 0;

    return height;
  }

  protected double getTitleHeight() {
    return getTitle() instanceof Region ? ((Region)getTitle()).getHeight() : 22; // TODO: make general, don't depend on Region
  }

  //  @Override protected double computeMinHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
//    double headerHeight = snapSize(titleRegion.prefHeight(width));
//    double contentHeight = contentContainer.minHeight(width) * getTransition();
//    return headerHeight + snapSize(contentHeight) + topInset + bottomInset;
//  }

  public void toggleExpandedState() {
    setExpanded(!isExpanded());
  }

  protected void setExpandedState() {
    if(getContent() != null)
      getContent().setVisible(isExpanded());

    if(isExpanded()) {
      setMaxHeight(Double.MAX_VALUE);
      getRowConstraints().add(rwcstrContent);
      expandButton.setText(ExpandedText);
    }
    else {
      setMaxHeight(computeMinHeight(getWidth()));
      getRowConstraints().remove(rwcstrContent);
      expandButton.setText(CollapsedText);
    }

    setHeight();
  }

  protected void setHeight() {
    setMinHeight(computeMinHeight(this.getWidth()));
    setPrefHeight(computePrefHeight(this.getWidth()));

    layout();
//    requestParentLayout();
    if(getScene() != null && getScene().getRoot() != null)
      getScene().getRoot().layout();
  }

  protected void titleChanged(Node oldTitle, Node newTitle) {
    if(oldTitle != null) {
      this.getChildren().remove(oldTitle);
      if (oldTitle instanceof Region)
        ((Region) oldTitle).heightProperty().removeListener(titleHeightChangedListener);
    }

    if(newTitle != null) {
      this.getChildren().add(newTitle);
      GridPane.setConstraints(newTitle, 1, 0, 1, 1, HPos.LEFT, VPos.CENTER, Priority.ALWAYS, Priority.ALWAYS);

      if(newTitle instanceof Region)
        ((Region)newTitle).heightProperty().addListener(titleHeightChangedListener);
    }

    setHeight();
  }

  protected void contentChanged(Node oldContent, Node newContent) {
    if(oldContent != null) {
      this.getChildren().remove(oldContent);
      if(oldContent instanceof Region)
        ((Region)oldContent).heightProperty().removeListener(contentHeightChangedListener);
    }

    GridPane.setConstraints(newContent, 0, 1, 2, 1);

    if(newContent != null) {
      FXUtils.ensureNodeOnlyUsesSpaceIfVisible(newContent);
      this.getChildren().add(newContent);
      newContent.setVisible(isExpanded());

      if(newContent instanceof Region)
        ((Region)newContent).heightProperty().addListener(contentHeightChangedListener);
    }

    setHeight();
  }


  protected ChangeListener<Number> titleHeightChangedListener = new ChangeListener<Number>() {
    @Override
    public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
      setHeight();
    }
  };

  protected ChangeListener<Number> contentHeightChangedListener = new ChangeListener<Number>() {
    @Override
    public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
      setHeight();
    }
  };
}
