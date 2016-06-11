package net.dankito.deepthought.controls.clipboard;

import net.dankito.deepthought.clipboard.ClipboardContentChangedListener;
import net.dankito.deepthought.clipboard.IClipboardWatcher;
import net.dankito.deepthought.controls.ICleanUp;
import net.dankito.deepthought.data.contentextractor.ContentExtractOptions;
import net.dankito.deepthought.data.contentextractor.EntryCreationResult;
import net.dankito.deepthought.data.contentextractor.ExtractContentActionResultListener;
import net.dankito.deepthought.data.contentextractor.OptionInvokedListener;
import net.dankito.deepthought.util.localization.Localization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.PopupControl;
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
public class CreateEntryFromClipboardContentPopup extends PopupControl implements ICleanUp {

  private final static Logger log = LoggerFactory.getLogger(CreateEntryFromClipboardContentPopup.class);


  protected ContentExtractOptionForUiCreator uiOptionsCreator = null;

  protected Stage stageToShowIn = null;

  protected IClipboardWatcher clipboardWatcher = null;

  protected VBox contentPane;

  protected VBox optionsPane;

  protected Label headerLabel;

  protected Button hidePopupButton;


  public CreateEntryFromClipboardContentPopup(Stage stageToShowIn, IClipboardWatcher clipboardWatcher) {
    this.stageToShowIn = stageToShowIn;
    this.clipboardWatcher = clipboardWatcher;
    this.uiOptionsCreator = new ContentExtractOptionForUiCreator(stageToShowIn);

    createPopupFrame();

    clipboardWatcher.addClipboardContentChangedExternallyListener(clipboardContentChangedExternallyListener);
  }

  @Override
  public void cleanUp() {
    clipboardWatcher.removeClipboardContentChangedExternallyListener(clipboardContentChangedExternallyListener);
    uiOptionsCreator = null;
  }


  protected void createPopupFrame() {
    setAutoHide(true);

    setMinWidth(150);
    setMaxWidth(stageToShowIn.getWidth() - 12);

    contentPane = new VBox(0);
    contentPane.setBackground(net.dankito.deepthought.controls.Constants.ClipboardContentPopupBackground);
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

  protected void showCreateEntryFromClipboardContentPopupThreadSafe(final ContentExtractOptions contentExtractOptions) {
    if(Platform.isFxApplicationThread()) {
      showCreateEntryFromClipboardContentPopup(contentExtractOptions);
    }
    else {
      Platform.runLater(() -> showCreateEntryFromClipboardContentPopup(contentExtractOptions));
    }
  }

  protected void showCreateEntryFromClipboardContentPopup(ContentExtractOptions contentExtractOptions) {
    if(isShowing())
      hideThreadSafe();

    optionsPane.getChildren().clear();

    if(contentExtractOptions.hasContentExtractOptions()) {
      showPopupForContentExtractOptions(contentExtractOptions);
    }
  }

  protected void showPopupForContentExtractOptions(final ContentExtractOptions contentExtractOptions) {
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

    optionLink.setOnMouseEntered(event -> optionLink.setBackground(net.dankito.deepthought.controls.Constants.ClipboardContentPopupOptionMouseOverBackground));
    optionLink.setOnMouseExited(event -> optionLink.setBackground(Background.EMPTY));

    return optionLink;
  }


  protected ClipboardContentChangedListener clipboardContentChangedExternallyListener = new ClipboardContentChangedListener() {
    @Override
    public void clipboardContentChanged(ContentExtractOptions contentExtractOptions) {
      showCreateEntryFromClipboardContentPopupThreadSafe(contentExtractOptions);
    }
  };

}
