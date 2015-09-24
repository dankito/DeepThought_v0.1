package net.deepthought.controls.event;

import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

/**
 * Created by ganymed on 24/09/15.
 */
public interface IMouseAndKeyEventReceiver {

  void onMouseEvent(MouseEvent event);

  void onKeyEvent(KeyEvent event);

}
