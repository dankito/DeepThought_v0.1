package net.deepthought.util;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

/**
 * Created by ganymed on 25/04/15.
 */
public class InputManager {

  protected static InputManager instance = null;

  public static InputManager getInstance() {
    if(instance == null)
      instance = new InputManager();
    return instance;
  }

  protected KeyCombination createEntryFromClipboardDirectlyAddEntryKeyCombination = new KeyCodeCombination(KeyCode.V, KeyCombination.SHORTCUT_DOWN, KeyCombination.ALT_DOWN);

  protected KeyCombination createEntryFromClipboardViewNewEntryFirstKeyCombination = new KeyCodeCombination(KeyCode.V, KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN);

  protected KeyCombination createEntryFromClipboardSetAsEntryContentKeyCombination = new KeyCodeCombination(KeyCode.V, KeyCombination.SHORTCUT_DOWN);

  protected KeyCombination createEntryFromClipboardAddAsFileAttachmentKeyCombination = new KeyCodeCombination(KeyCode.V, KeyCombination.SHORTCUT_DOWN, KeyCombination.ALT_DOWN);

  protected KeyCombination createEntryFromClipboardTryToExtractTextKeyCombination = new KeyCodeCombination(KeyCode.V, KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN);

  protected KeyCombination createEntryFromClipboardAddAsFileAttachmentAndTryToExtractTextKeyCombination = new KeyCodeCombination(KeyCode.V, KeyCombination.SHORTCUT_DOWN,
      KeyCombination.SHIFT_DOWN, KeyCombination.ALT_DOWN);


  protected InputManager() {

  }

  public KeyCombination getCreateEntryFromClipboardDirectlyAddEntryKeyCombination() {
    return createEntryFromClipboardDirectlyAddEntryKeyCombination;
  }

  public KeyCombination getCreateEntryFromClipboardViewNewEntryFirstKeyCombination() {
    return createEntryFromClipboardViewNewEntryFirstKeyCombination;
  }

  public KeyCombination getCreateEntryFromClipboardSetAsEntryContentKeyCombination() {
    return createEntryFromClipboardSetAsEntryContentKeyCombination;
  }

  public KeyCombination getCreateEntryFromClipboardAddAsFileAttachmentKeyCombination() {
    return createEntryFromClipboardAddAsFileAttachmentKeyCombination;
  }

  public KeyCombination getCreateEntryFromClipboardTryToExtractTextKeyCombination() {
    return createEntryFromClipboardTryToExtractTextKeyCombination;
  }

  public KeyCombination getCreateEntryFromClipboardAddAsFileAttachmentAndTryToExtractTextKeyCombination() {
    return createEntryFromClipboardAddAsFileAttachmentAndTryToExtractTextKeyCombination;
  }
}
