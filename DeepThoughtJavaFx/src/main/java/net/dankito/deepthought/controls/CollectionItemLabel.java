package net.dankito.deepthought.controls;

import net.dankito.deepthought.controls.event.CollectionItemLabelEvent;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * Created by ganymed on 30/11/14.
 */
public abstract class CollectionItemLabel extends HBox implements ICleanUp {

  protected final static Background DefaultBackground = new Background(new BackgroundFill(Color.TRANSPARENT, new CornerRadii(0), new Insets(0)));

  protected final static Background MouseOverBackground = new Background(new BackgroundFill(Constants.ClipboardContentPopupBackgroundColor, new CornerRadii(4), new Insets(0)));


  protected Button btnRemoveItemFromCollection;
  protected Label lblItemName;

  protected EventHandler<CollectionItemLabelEvent> onLabelClickedEventHandler = null;
  protected EventHandler<CollectionItemLabelEvent> onButtonRemoveItemFromCollectionEventHandler = null;


  public CollectionItemLabel() {
    setupItemLabel();
  }

  public CollectionItemLabel(EventHandler<CollectionItemLabelEvent> onButtonRemoveItemFromCollectionEventHandler) {
    this();

    this.onButtonRemoveItemFromCollectionEventHandler = onButtonRemoveItemFromCollectionEventHandler;
  }



  @Override
  public void cleanUp() {
    setOnButtonRemoveItemFromCollectionEventHandler(null);
    setOnLabelClickedEventHandler(null);
  }


  protected void setupItemLabel() {
    setAlignment(Pos.CENTER_LEFT);
    this.setMinHeight(22);
    this.setMaxHeight(22);
    this.setBackground(DefaultBackground);
    this.hoverProperty().addListener((observable, oldValue, newValue) -> { // set a hover effect
      if(newValue == true)
        setBackground(MouseOverBackground);
      else
        setBackground(DefaultBackground);
    });
    this.setCursor(Cursor.HAND);
    FlowPane.setMargin(this, new Insets(0, 6, 0, 0));

    btnRemoveItemFromCollection = new Button("x");
    btnRemoveItemFromCollection.setFont(new Font(11));
    btnRemoveItemFromCollection.setMaxSize(10, 10);
    btnRemoveItemFromCollection.setOnAction((event) -> onButtonRemoveItemFromCollectionAction(event));
    this.getChildren().add(btnRemoveItemFromCollection);

    lblItemName = new Label(getItemDisplayName());
    lblItemName.setTooltip(new Tooltip(getToolTipText()));
    this.getChildren().add(lblItemName);
    HBox.setHgrow(lblItemName, Priority.ALWAYS);

    HBox.setMargin(btnRemoveItemFromCollection, new Insets(0, 6, 0, 0));

    this.setOnMouseClicked((event) -> onLabelClickedAction(event));
  }

  protected abstract String getItemDisplayName();

  protected String getToolTipText() { return ""; } // may be overwritten in sub class

  protected void itemDisplayNameUpdated() {
    lblItemName.setText(getItemDisplayName());
    lblItemName.getTooltip().setText(getToolTipText());
  }

  protected void onLabelClickedAction(MouseEvent event) {
    if(onLabelClickedEventHandler != null)
      onLabelClickedEventHandler.handle(new CollectionItemLabelEvent(event, this));
  }

  public void onButtonRemoveItemFromCollectionAction(ActionEvent event) {
    if(onButtonRemoveItemFromCollectionEventHandler != null)
      onButtonRemoveItemFromCollectionEventHandler.handle(new CollectionItemLabelEvent(event, this));
  }

  public EventHandler<CollectionItemLabelEvent> getOnLabelClickedEventHandler() {
    return onLabelClickedEventHandler;
  }

  public void setOnLabelClickedEventHandler(EventHandler<CollectionItemLabelEvent> onLabelClickedEventHandler) {
    this.onLabelClickedEventHandler = onLabelClickedEventHandler;
  }

  public EventHandler<CollectionItemLabelEvent> getOnButtonRemoveItemFromCollectionEventHandler() {
    return onButtonRemoveItemFromCollectionEventHandler;
  }

  public void setOnButtonRemoveItemFromCollectionEventHandler(EventHandler<CollectionItemLabelEvent> onButtonRemoveItemFromCollectionEventHandler) {
    this.onButtonRemoveItemFromCollectionEventHandler = onButtonRemoveItemFromCollectionEventHandler;
  }
}
