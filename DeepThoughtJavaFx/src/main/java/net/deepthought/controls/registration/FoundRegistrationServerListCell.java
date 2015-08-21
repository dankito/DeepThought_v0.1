package net.deepthought.controls.registration;

import net.deepthought.Application;
import net.deepthought.communication.AskForDeviceRegistrationListener;
import net.deepthought.communication.model.HostInfo;
import net.deepthought.util.JavaFxLocalization;

import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;

/**
 * Created by ganymed on 21/08/15.
 */
public class FoundRegistrationServerListCell extends ListCell<HostInfo> {

  protected AskForDeviceRegistrationListener listener = null;


  protected GridPane graphicsPane = new GridPane();

  protected ImageView imgvwIcon = new ImageView();

  protected Label lblUserInfo = new Label();
  protected Label lblDeviceInfo = new Label();

  protected Button btnAskForRegistration = new Button();


  public FoundRegistrationServerListCell(AskForDeviceRegistrationListener listener) {
    this.listener = listener;

    setupGraphics();
  }

  protected void setupGraphics() {
    setText(null);
    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
    setAlignment(Pos.CENTER_LEFT);

    graphicsPane.getColumnConstraints().add(new ColumnConstraints(60, 60, 60, Priority.NEVER, HPos.CENTER, false));
    graphicsPane.getColumnConstraints().add(new ColumnConstraints(-1, -1, -1, Priority.ALWAYS, HPos.CENTER, true));
    graphicsPane.getColumnConstraints().add(new ColumnConstraints(180, 180, 180, Priority.SOMETIMES, HPos.CENTER, false));

    graphicsPane.getRowConstraints().add(new RowConstraints(30, 30, 30));
    graphicsPane.getRowConstraints().add(new RowConstraints(30, 30, 30));

    graphicsPane.add(imgvwIcon, 0, 0, 1, 2);
    graphicsPane.add(lblUserInfo, 1, 0);
    graphicsPane.add(lblDeviceInfo, 1, 1);
    graphicsPane.add(btnAskForRegistration, 2, 0, 1, 2);

    lblUserInfo.setMinHeight(26);
    lblUserInfo.setMaxHeight(26);
    lblUserInfo.setMaxWidth(Double.MAX_VALUE);

    lblDeviceInfo.setMinHeight(26);
    lblDeviceInfo.setMaxHeight(26);
    lblDeviceInfo.setMaxWidth(Double.MAX_VALUE);

    JavaFxLocalization.bindLabeledText(btnAskForRegistration, "ask.for.registration");
    btnAskForRegistration.setMinHeight(35);
    btnAskForRegistration.setMaxHeight(35);
    btnAskForRegistration.setMinWidth(180);
    btnAskForRegistration.setMaxWidth(180);
    btnAskForRegistration.setOnAction(event -> askForRegistration());
  }

  protected void askForRegistration() {
    HostInfo serverInfo = getItem();
    if(serverInfo != null) {
      Application.getDeepThoughtsConnector().getCommunicator().askForDeviceRegistration(serverInfo, listener);
    }
  }


  @Override
  protected void updateItem(HostInfo item, boolean empty) {
    super.updateItem(item, empty);

    if(empty || item == null)
      setGraphic(null);
    else {
      setGraphic(graphicsPane);

      lblUserInfo.setText(item.getUserName());
      lblDeviceInfo.setText(item.getPlatform() + " " + item.getOsVersion() + " (" + item.getIpAddress() + ")");
    }
  }
}
