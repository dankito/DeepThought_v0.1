package net.deepthought.controls.registration;

import net.deepthought.Application;
import net.deepthought.communication.listener.AskForDeviceRegistrationResultListener;
import net.deepthought.communication.model.HostInfo;
import net.deepthought.controls.utils.FXUtils;
import net.deepthought.util.IconManager;
import net.deepthought.util.JavaFxLocalization;

import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;

/**
 * Created by ganymed on 21/08/15.
 */
public class FoundRegistrationServerListCell extends ListCell<HostInfo> {

  protected AskForDeviceRegistrationResultListener listener = null;


  protected GridPane graphicsPane = new GridPane();

  protected ImageView imgvwOsLogo = new ImageView();

  protected Label lblUserInfo = new Label();
  protected Label lblDeviceInfo = new Label();

  protected Button btnAskForRegistration = new Button();


  public FoundRegistrationServerListCell(AskForDeviceRegistrationResultListener listener) {
    this.listener = listener;

    setupGraphics();
  }

  protected void setupGraphics() {
    setText(null);
    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
    setAlignment(Pos.CENTER_LEFT);

    graphicsPane.getColumnConstraints().add(new ColumnConstraints(60, 60, 60, Priority.NEVER, HPos.CENTER, false));
    graphicsPane.getColumnConstraints().add(new ColumnConstraints(-1, -1, -1, Priority.ALWAYS, HPos.LEFT, true));
    graphicsPane.getColumnConstraints().add(new ColumnConstraints(180, 180, 180, Priority.SOMETIMES, HPos.RIGHT, false));

    graphicsPane.getRowConstraints().add(new RowConstraints(30, 30, 30));
    graphicsPane.getRowConstraints().add(new RowConstraints(30, 30, 30));

    graphicsPane.add(imgvwOsLogo, 0, 0, 1, 2);
    graphicsPane.add(lblUserInfo, 1, 0);
    graphicsPane.add(lblDeviceInfo, 1, 1);
    graphicsPane.add(btnAskForRegistration, 2, 0, 1, 2);

    imgvwOsLogo.maxWidth(56);
    imgvwOsLogo.maxHeight(56);
    imgvwOsLogo.setPreserveRatio(true);
    imgvwOsLogo.setFitWidth(56);
    imgvwOsLogo.setFitHeight(56);
    GridPane.setHalignment(imgvwOsLogo, HPos.CENTER);
    GridPane.setValignment(imgvwOsLogo, VPos.CENTER);

    lblUserInfo.setMinHeight(26);
    lblUserInfo.setMaxHeight(26);
    lblUserInfo.setMaxWidth(FXUtils.SizeMaxValue);

    lblDeviceInfo.setMinHeight(26);
    lblDeviceInfo.setMaxHeight(26);
    lblDeviceInfo.setMaxWidth(FXUtils.SizeMaxValue);

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
      Application.getDeepThoughtsConnector().getCommunicator().askForDeviceRegistration(serverInfo, Application.getLoggedOnUser(), Application.getApplication().getLocalDevice(), listener);
    }
  }


  @Override
  protected void updateItem(HostInfo item, boolean empty) {
    super.updateItem(item, empty);

    if(empty || item == null)
      setGraphic(null);
    else {
      setGraphic(graphicsPane);

      String logoPath = IconManager.getInstance().getLogoForOperatingSystem(item.getPlatform(), item.getOsVersion(), item.getPlatformArchitecture());
      if(logoPath != null)
        imgvwOsLogo.setImage(new Image(logoPath));
      else
        imgvwOsLogo.setVisible(false);

      lblUserInfo.setText(item.getUserName());
      lblDeviceInfo.setText(item.getPlatform() + " " + item.getOsVersion() + " (" + item.getIpAddress() + ")");
    }
  }
}
