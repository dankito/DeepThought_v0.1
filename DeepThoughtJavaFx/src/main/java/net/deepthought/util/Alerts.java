package net.deepthought.util;

import net.deepthought.Application;
import net.deepthought.communication.messages.AskForDeviceRegistrationRequest;
import net.deepthought.communication.messages.AskForDeviceRegistrationResponseMessage;
import net.deepthought.communication.model.DeviceInfo;
import net.deepthought.communication.model.UserInfo;
import net.deepthought.data.model.Category;
import net.deepthought.data.model.DeepThought;
import net.deepthought.data.model.Person;
import net.deepthought.data.model.Reference;
import net.deepthought.data.model.SeriesTitle;
import net.deepthought.data.model.Tag;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Created by ganymed on 01/02/15.
 */
public class Alerts {

  public static void deleteCategoryWithUserConfirmationIfHasSubCategoriesOrEntries(Category category) {
    if(category.hasSubCategories() || category.hasEntries()) {
      Boolean deleteCategory = Alerts.showConfirmDeleteCategoryWithSubCategoriesOrEntries(category);
      if(deleteCategory)
      Application.getDeepThought().removeCategory(category);
    }
    else
      Application.getDeepThought().removeCategory(category);
  }

  public static boolean showConfirmDeleteCategoryWithSubCategoriesOrEntries(Category category) {
    return showConfirmationDialog(Localization.getLocalizedString("alert.message.category.contains.entries.or.sub.categories", category.getName(), category.getName()),
        Localization.getLocalizedString("alert.title.confirm.delete"));
  }

  public static void deleteTagWithUserConfirmationIfIsSetOnEntries(Tag tag) {
    deleteTagWithUserConfirmationIfIsSetOnEntries(Application.getDeepThought(), tag);
  }

  public static void deleteTagWithUserConfirmationIfIsSetOnEntries(DeepThought deepThought, Tag tag) {
    if(tag.hasEntries()) {
      boolean confirmDeleteTag = showConfirmDeleteTagWithEntriesAlert(tag);
      if(confirmDeleteTag)
        deepThought.removeTag(tag);
    }
    else
      deepThought.removeTag(tag);
  }

  public static boolean showConfirmDeleteTagWithEntriesAlert(Tag tag) {
    return showConfirmationDialog(Localization.getLocalizedString("alert.message.tag.is.set.on.entries", tag.getName(), tag.getEntries().size()),
        Localization.getLocalizedString("alert.title.confirm.delete"));
  }

  public static void deletePersonWithUserConfirmationIfIsSetOnEntries(Person person) {
    deletePersonWithUserConfirmationIfIsSetOnEntries(Application.getDeepThought(), person);
  }

  public static void deletePersonWithUserConfirmationIfIsSetOnEntries(DeepThought deepThought, Person person) {
    if(person.isSetOnEntries()) {
      boolean confirmDeleteTag = showConfirmDeletePersonWithEntriesAlert(person);
      if(confirmDeleteTag)
        deepThought.removePerson(person);
    }
    else
      deepThought.removePerson(person);
  }

  public static boolean showConfirmDeletePersonWithEntriesAlert(Person person) {
    return showConfirmationDialog(Localization.getLocalizedString("alert.message.person.is.set.on.entries", person.getNameRepresentation(), person.getAssociatedEntries().size()),
        Localization.getLocalizedString("alert.title.confirm.delete"));
  }


  public static boolean askUserIfEditedSeriesTitleShouldBeSaved(SeriesTitle seriesTitle) {
    return askUserIfEditedReferenceBaseShouldBeSaved(seriesTitle.getTextRepresentation(), Localization.getLocalizedString("series.title"));
  }

  public static boolean askUserIfEditedReferenceShouldBeSaved(Reference reference) {
    return askUserIfEditedReferenceBaseShouldBeSaved(reference.getPreview(), Localization.getLocalizedString("reference"));
  }

  protected static boolean askUserIfEditedReferenceBaseShouldBeSaved(String referenceBasePreview, String localizedReferenceBaseName) {
    return showConfirmationDialog(Localization.getLocalizedString("alert.message.ask.save.entity", localizedReferenceBaseName, referenceBasePreview),
        Localization.getLocalizedString("alert.title.should.edited.data.be.saved"));
  }

  // TODO: merge with that ones above
  public static ButtonType askUserIfEditedEntityShouldBeSaved(Stage owner, String entityNameResourceKey) {
    String translatedEntityName = Localization.getLocalizedString(entityNameResourceKey);

    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    alert.setTitle(Localization.getLocalizedString("alert.title.entity.contains.unsaved.changes", translatedEntityName));
    setAlertContent(alert, Localization.getLocalizedString("alert.message.entity.contains.unsaved.changes", translatedEntityName));
    alert.setHeaderText(null);

    alert.getButtonTypes().clear();
//    alert.getButtonTypes().setAll(ButtonType.CANCEL, ButtonType.NO, ButtonType.YES);
    alert.getButtonTypes().add(0, ButtonType.CANCEL);
//    alert.getButtonTypes().addAll(ButtonType.NO, ButtonType.YES);
    alert.getButtonTypes().add(1, ButtonType.NO);
    alert.getButtonTypes().add(2, ButtonType.YES);
    if(owner != null)
      alert.initOwner(owner);

    Optional<ButtonType> result = alert.showAndWait();
    return result.get();
  }


  public static boolean showDeviceAsksForRegistrationAlert(AskForDeviceRegistrationRequest request, Stage windowStage) {
    windowStage.show();
    windowStage.requestFocus();
    windowStage.toFront();

    return showConfirmationDialog(Localization.getLocalizedString("alert.message.ask.for.device.registration", extractUserInfoString(request.getUser()), extractDeviceInfoString(request.getDevice())),
        Localization.getLocalizedString("alert.title.ask.for.device.registration"), windowStage);
  }

  public static void showDeviceRegistrationSuccessfulAlert(AskForDeviceRegistrationResponseMessage response, Stage windowStage) {
    windowStage.show();
    windowStage.requestFocus();
    windowStage.toFront();

    showInfoMessage(windowStage, Localization.getLocalizedString("alert.message.successfully.registered.at.device", extractDeviceInfoString(response.getDevice())),
        Localization.getLocalizedString("alert.title.device.registration.successful"));
  }

  public static void showServerDeniedDeviceRegistrationAlert(AskForDeviceRegistrationResponseMessage response, Stage windowStage) {
    windowStage.show();
    windowStage.requestFocus();
    windowStage.toFront();

    showInfoMessage(windowStage, Localization.getLocalizedString("alert.title.device.registration.denied"),
                                 Localization.getLocalizedString("alert.message.server.denied.device.registration"));
  }

  protected static String extractUserInfoString(UserInfo user) {
    String userInfo = user.getUserName();

    if(StringUtils.isNotNullOrEmpty(user.getFirstName()) || StringUtils.isNotNullOrEmpty(user.getLastName()))
      userInfo += " (" + user.getFirstName() + " " + user.getLastName() + ")";

    return userInfo;
  }

  protected static String extractDeviceInfoString(DeviceInfo device) {
    String deviceInfo = device.getPlatform() + " " + device.getOsVersion();
    return deviceInfo;
  }


  protected static boolean showConfirmationDialog(String message, String alertTitle) {
    return showConfirmationDialog(message, alertTitle, null);
  }

  protected static boolean showConfirmationDialog(String message, String alertTitle, Stage owner) {
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    alert.setTitle(alertTitle);
    setAlertContent(alert, message);
    alert.setHeaderText(null);

    alert.getButtonTypes().setAll(ButtonType.NO, ButtonType.YES);
    if(owner != null)
      alert.initOwner(owner);

    Optional<ButtonType> result = alert.showAndWait();
    return result.get() == ButtonType.YES;
  }


  public static void showInfoMessage(final Stage owner, final String infoMessage, final String alertTitle) {
    if(Platform.isFxApplicationThread())
      showInfoMessageOnUiThread(owner, infoMessage, alertTitle);
    else
      Platform.runLater(() -> showInfoMessageOnUiThread(owner, infoMessage, alertTitle));
  }

  protected static void showInfoMessageOnUiThread(Stage owner, String infoMessage, String alertTitle) {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle(alertTitle);
    setAlertContent(alert, infoMessage);
    alert.setHeaderText(null);

    alert.getButtonTypes().setAll(ButtonType.OK);
    alert.initOwner(owner);
    alert.showAndWait();
  }


  public static void showErrorMessage(final Stage owner, DeepThoughtError error) {
    String title = error.getNotificationMessageTitle();
    String message = error.getNotificationMessage();

    if(error.isSevere()) {
      title = Localization.getLocalizedString("alert.message.title.severe.error.occurred");
      message = Localization.getLocalizedString("alert.message.message.severe.error.occurred", error.getNotificationMessage());
    }
    else if(error.getNotificationMessageTitle() == null)
      title = Localization.getLocalizedString("alert.message.title.error.occurred");

    showErrorMessage(owner, message, title, error.getException());
  }

  public static void showErrorMessage(final Stage owner, final String errorMessage, final String alertTitle) {
    showErrorMessage(owner, errorMessage, alertTitle, null);
  }

  public static void showErrorMessage(final Stage owner, final String errorMessage, final String alertTitle, final Exception exception) {
    if(Platform.isFxApplicationThread())
      showErrorMessageOnUiThread(owner, errorMessage, alertTitle, exception);
    else
      Platform.runLater(() -> showErrorMessageOnUiThread(owner, errorMessage, alertTitle, exception));
  }

  protected static void showErrorMessageOnUiThread(Stage owner, String errorMessage, String alertTitle, Exception exception) {
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle(alertTitle);
    setAlertContent(alert, errorMessage);
    alert.setHeaderText(null);

    if(exception != null)
      createExpandableException(alert, exception);

    alert.getButtonTypes().setAll(ButtonType.OK);
    alert.initOwner(owner);
    alert.showAndWait();
  }


  protected static void setAlertContent(Alert alert, String content) {
    Label contentLabel = new Label(content);
    contentLabel.setWrapText(true);
    contentLabel.setPrefHeight(Region.USE_COMPUTED_SIZE);
    contentLabel.setMaxHeight(500);
    contentLabel.setMaxWidth(500);

    alert.getDialogPane().setPrefHeight(Region.USE_COMPUTED_SIZE);
    alert.getDialogPane().setMaxHeight(500);
    alert.getDialogPane().setMaxWidth(500);
    alert.getDialogPane().setContent(new VBox(contentLabel));
    VBox.setVgrow(contentLabel, Priority.ALWAYS);
  }

  protected static void createExpandableException(Alert alert, Exception exception) {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    exception.printStackTrace(pw);
    String exceptionText = sw.toString();

    Label label = new Label("The exception stacktrace was:");

    TextArea textArea = new TextArea(exceptionText);
    textArea.setEditable(false);
    textArea.setWrapText(true);

    textArea.setMaxWidth(Double.MAX_VALUE);
    textArea.setMaxHeight(Double.MAX_VALUE);
    GridPane.setVgrow(textArea, Priority.ALWAYS);
    GridPane.setHgrow(textArea, Priority.ALWAYS);

    GridPane expContent = new GridPane();
    expContent.setMaxWidth(Double.MAX_VALUE);
    expContent.add(label, 0, 0);
    expContent.add(textArea, 0, 1);

// Set expandable Exception into the dialog pane.
    alert.getDialogPane().setExpandableContent(expContent);
  }
}
