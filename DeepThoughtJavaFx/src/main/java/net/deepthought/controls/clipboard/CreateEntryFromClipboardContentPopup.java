package net.deepthought.controls.clipboard;

import net.deepthought.Application;
import net.deepthought.controls.Constants;
import net.deepthought.data.contentextractor.ClipboardContent;
import net.deepthought.data.contentextractor.ContentExtractOptions;
import net.deepthought.data.contentextractor.EntryCreationResult;
import net.deepthought.data.contentextractor.ExtractContentActionResultListener;
import net.deepthought.data.contentextractor.JavaFxClipboardContent;
import net.deepthought.data.contentextractor.OptionInvokedListener;
import net.deepthought.util.Localization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.PopupControl;
import javafx.scene.input.Clipboard;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * Created by ganymed on 25/04/15.
 */
public class CreateEntryFromClipboardContentPopup extends PopupControl {

  private final static Logger log = LoggerFactory.getLogger(CreateEntryFromClipboardContentPopup.class);


  protected ContentExtractOptionForUiCreator uiOptionsCreator = null;

  protected Stage stageToShowIn = null;

  protected VBox contentPane;

  protected VBox optionsPane;

  protected Label headerLabel;

  protected Button hidePopupButton;

  protected Object sourceOfLastShownPopup = null;


  public CreateEntryFromClipboardContentPopup(Stage stageToShowIn) {
    this.stageToShowIn = stageToShowIn;
    this.uiOptionsCreator = new ContentExtractOptionForUiCreator(stageToShowIn);

    createPopupFrame();

    stageToShowIn.focusedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        if (newValue == true) { // Window got focused
          checkIfEntryCanBeCreatedFromClipboardContent();
        }
      }
    });
  }


  protected void createPopupFrame() {
    setAutoHide(true);

    setMinWidth(150);
    setMaxWidth(stageToShowIn.getWidth() - 12);

    contentPane = new VBox(0);
    contentPane.setBackground(Constants.ClipboardContentPopupBackground);
    getScene().setRoot(contentPane);

    hidePopupButton = new Button("x");
    AnchorPane.setTopAnchor(hidePopupButton, 0d);
    AnchorPane.setRightAnchor(hidePopupButton, 0d);
    AnchorPane.setBottomAnchor(hidePopupButton, 4d);
    hidePopupButton.setOnAction(action -> hideThreadSafe());

    headerLabel = new Label();
    AnchorPane.setTopAnchor(headerLabel, 8d);
    AnchorPane.setLeftAnchor(headerLabel, 10d);
    AnchorPane.setBottomAnchor(headerLabel, 4d);
    AnchorPane.setRightAnchor(headerLabel, hidePopupButton.getWidth() + 26 + 6); // 26 = hidePopupButton width

    contentPane.getChildren().add(new AnchorPane(headerLabel, hidePopupButton));

    optionsPane = new VBox();
    contentPane.getChildren().add(optionsPane);
  }

  protected void checkIfEntryCanBeCreatedFromClipboardContent() {
    ClipboardContent clipboardContent = new JavaFxClipboardContent(Clipboard.getSystemClipboard());
    Application.getContentExtractorManager().getContentExtractorOptionsForClipboardContent(clipboardContent, contentExtractOptions -> {
      if(contentExtractOptions.hasContentExtractOptions()) {
        if(contentExtractOptions.getSource().equals(sourceOfLastShownPopup) == false) {
          sourceOfLastShownPopup = contentExtractOptions.getSource();
          showCreateEntryFromClipboardContentPopupThreadSafe(contentExtractOptions);
        }
      }
    });
  }

  protected void showCreateEntryFromClipboardContentPopupThreadSafe(final ContentExtractOptions contentExtractOptions) {
    if(Platform.isFxApplicationThread()) {
      showCreateEntryFromClipboardContentPopup(contentExtractOptions);
    }
    else {
      Platform.runLater(() -> showCreateEntryFromClipboardContentPopup(contentExtractOptions));
    }
  }

  protected void showCreateEntryFromClipboardContentPopup(ContentExtractOptions contentExtractOptions) {
    optionsPane.getChildren().clear();

    showPopupForContentExtractOptions(contentExtractOptions);
  }

  protected void showPopupForContentExtractOptions(final ContentExtractOptions contentExtractOptions) {
    if(isShowing())
      hideThreadSafe();

    List<ContentExtractOptionForUi> uiOptions = uiOptionsCreator.createOptions(contentExtractOptions);
    addContentExtractOptionsToPopup(contentExtractOptions, uiOptions);

    showInStage();
  }

  protected void showInStage() {
    contentPane.setDisable(false); // TODO: this is not fully correctly implemented: if an option button is pressed and the process is still working, while the window gets
    // moved, than all options get enabled again

    show(stageToShowIn.getScene().getRoot(), 0, 0);
    // ok, PopupWindow is too stupid to set its position to right bottom window corner by itself, so i have to do it on my own
    setX(stageToShowIn.getX() + stageToShowIn.getWidth() - getWidth() - 6);
    setY(stageToShowIn.getY() + stageToShowIn.getHeight() - getHeight() - getAdjustmentForStatusBar() - 6);
  }

  protected double getAdjustmentForStatusBar() {
    Parent root = stageToShowIn.getScene().getRoot();
    if(root instanceof BorderPane) {
      BorderPane rootPane = (BorderPane)root;
      if(rootPane.getBottom() instanceof Region)
        return ((Region)rootPane.getBottom()).getHeight();
    }

    return 0;
  }

  protected void hideThreadSafe() {
    if(Platform.isFxApplicationThread())
      hide();
    else
      Platform.runLater(() -> hide());
  }

  protected void addContentExtractOptionsToPopup(ContentExtractOptions contentExtractOptions, List<ContentExtractOptionForUi> uiOptions) {
    headerLabel.setText(Localization.getLocalizedString("ask.extract.content.from", contentExtractOptions.getSourceShortName()));

    for(ContentExtractOptionForUi uiOption : uiOptions) {
      addOptionToCreateEntryFromContentExtractorPopup(contentExtractOptions, uiOption.getDisplayName(), uiOption.getShortCut(), options -> optionInvoked(uiOption));
    }
  }

  protected void optionInvoked(ContentExtractOptionForUi uiOption) {
    uiOption.runAction(new ExtractContentActionResultListener() {
      @Override
      public void extractingContentDone(EntryCreationResult result) {
        hideThreadSafe();
      }
    });
  }

  protected void addOptionToCreateEntryFromContentExtractorPopup(ContentExtractOptions contentExtractOptions, String optionNameResourceKey, OptionInvokedListener listener) {
    addOptionToCreateEntryFromContentExtractorPopup(contentExtractOptions, optionNameResourceKey, null, listener);
  }

  protected Control addOptionToCreateEntryFromContentExtractorPopup(final ContentExtractOptions contentExtractOptions,
                                                                    String optionNameResourceKey, KeyCombination optionKeyCombination, final OptionInvokedListener listener) {
    String optionText = Localization.getLocalizedString(optionNameResourceKey);
    if(optionKeyCombination != null)
      optionText += " (" + optionKeyCombination.getDisplayText() + ")";

//    final Hyperlink optionLink = new Hyperlink(optionText);
    final Button optionLink = new Button(optionText);
    optionLink.setUnderline(true);
    optionLink.setTextFill(Color.BLACK);
    optionLink.setBackground(Background.EMPTY);
    optionLink.setCursor(Cursor.HAND);
//    optionLink.setBorder(new Border(new BorderStroke(Color.DARKSLATEGRAY, BorderStrokeStyle.SOLID, new CornerRadii(4), new BorderWidths(2))));
    optionLink.setPrefHeight(24);
    optionsPane.getChildren().add(optionLink);
    VBox.setMargin(optionLink, new Insets(0, 10, 6, 18));

    if(optionKeyCombination != null)
      optionLink.getScene().getAccelerators().put(optionKeyCombination, () -> optionLink.fire());

    optionLink.setOnAction(action -> {
      CreateEntryFromClipboardContentPopup.this.contentPane.setDisable(true);
      if(listener != null)
        listener.optionInvoked(contentExtractOptions);
    });

    optionLink.setOnMouseEntered(event -> optionLink.setBackground(Constants.ClipboardContentPopupOptionMouseOverBackground));
    optionLink.setOnMouseExited(event -> optionLink.setBackground(Background.EMPTY));

    return optionLink;
  }

}
