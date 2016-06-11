package net.dankito.deepthought.util;

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


  protected KeyCombination firstContentExtractOptionKeyCombination = new KeyCodeCombination(KeyCode.V, KeyCombination.SHORTCUT_DOWN);

  protected KeyCombination secondContentExtractOptionKeyCombination = new KeyCodeCombination(KeyCode.V, KeyCombination.SHORTCUT_DOWN, KeyCombination.ALT_DOWN);

  protected KeyCombination thirdContentExtractOptionKeyCombination = new KeyCodeCombination(KeyCode.V, KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN);

  protected KeyCombination forthContentExtractOptionKeyCombination = new KeyCodeCombination(KeyCode.V, KeyCombination.SHORTCUT_DOWN,
      KeyCombination.SHIFT_DOWN, KeyCombination.ALT_DOWN);


  protected InputManager() {

  }


  public KeyCombination getFirstContentExtractOptionKeyCombination() {
    return firstContentExtractOptionKeyCombination;
  }

  public KeyCombination getSecondContentExtractOptionKeyCombination() {
    return secondContentExtractOptionKeyCombination;
  }

  public KeyCombination getThirdContentExtractOptionKeyCombination() {
    return thirdContentExtractOptionKeyCombination;
  }

  public KeyCombination getForthContentExtractOptionKeyCombination() {
    return forthContentExtractOptionKeyCombination;
  }
}
