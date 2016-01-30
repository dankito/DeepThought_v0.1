package net.deepthought.controls.clipboard;

import net.deepthought.controller.Dialogs;
import net.deepthought.data.contentextractor.ContentExtractOption;
import net.deepthought.data.contentextractor.ContentExtractOptions;
import net.deepthought.data.contentextractor.EntryCreationResult;
import net.deepthought.data.contentextractor.ExtractContentActionResultListener;
import net.deepthought.util.Alerts;
import net.deepthought.util.DeepThoughtError;
import net.deepthought.util.InputManager;
import net.deepthought.util.localization.Localization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import javafx.application.Platform;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;

/**
 * Created by ganymed on 14/12/15.
 */
public class ContentExtractOptionForUiCreator {

  private static final Logger log = LoggerFactory.getLogger(ContentExtractOptionForUiCreator.class);


  protected Stage stage;


  public ContentExtractOptionForUiCreator(Stage stage) {
    this.stage = stage;
  }


  public List<ContentExtractOptionForUi> createOptions(ContentExtractOptions options) {
    List<ContentExtractOptionForUi> createdOptions = new ArrayList<>();

    for(ContentExtractOption option : options.getContentExtractOptions()) {
      createUiOptionsFromOption(createdOptions, option);
    }

    return createdOptions;
  }

  protected void createUiOptionsFromOption(List<ContentExtractOptionForUi> createdOptions, ContentExtractOption option) {
    createdOptions.add(new ContentExtractOptionForUi(option, option.getTranslatedOptionName(), getKeyShortCutForOption(createdOptions, option),
        (optionParam, listener) -> runAction(optionParam, listener)));
  }

  protected KeyCombination getKeyShortCutForOption(List<ContentExtractOptionForUi> createdOptions, ContentExtractOption option) {
    InputManager inputManager = InputManager.getInstance();

    if(createdOptions.size() == 0) {
      return inputManager.getFirstContentExtractOptionKeyCombination();
    }
    else if(createdOptions.size() == 1) {
      return inputManager.getSecondContentExtractOptionKeyCombination();
    }
    else if(createdOptions.size() == 2) {
      return inputManager.getThirdContentExtractOptionKeyCombination();
    }
    else if(createdOptions.size() == 3) {
      return inputManager.getForthContentExtractOptionKeyCombination();
    }

    return null;
  }

  protected void runAction(ContentExtractOption option, ExtractContentActionResultListener listener) {
    option.runAction(result -> handleEntryCreationResultThreadSafe(result, listener));
  }

  protected void handleEntryCreationResultThreadSafe(EntryCreationResult result, ExtractContentActionResultListener listener) {
    if(Platform.isFxApplicationThread())
      handleEntryCreationResult(result, listener);
    else
      Platform.runLater(() -> handleEntryCreationResult(result, listener));
  }

  protected void handleEntryCreationResult(EntryCreationResult result, ExtractContentActionResultListener listener) {
    if (result.successful())
      Dialogs.showEditEntryDialog(result);
    else
      showCouldNotCreateEntryError(result);

    if(listener != null)
      listener.extractingContentDone(result);
  }


  protected void showCouldNotCreateEntryError(EntryCreationResult result) {
    showCouldNotCreateEntryError(result.getSource(), result.getError());
  }

  protected void showCouldNotCreateEntryError(Object source, DeepThoughtError error) {
    log.error("Could not create Entry from Source " + source, error.getException());
    Alerts.showErrorMessage(stage, error.getNotificationMessage(), Localization.getLocalizedString("can.not.create.entry.from", source), error.getException());
  }

}
