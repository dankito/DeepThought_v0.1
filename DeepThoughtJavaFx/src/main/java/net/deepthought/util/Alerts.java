package net.deepthought.util;

import net.deepthought.Application;
import net.deepthought.communication.messages.AskForDeviceRegistrationRequest;
import net.deepthought.communication.messages.AskForDeviceRegistrationResponseMessage;
import net.deepthought.communication.model.DeviceInfo;
import net.deepthought.communication.model.UserInfo;
import net.deepthought.controls.utils.FXUtils;
import net.deepthought.data.model.Category;
import net.deepthought.data.model.DeepThought;
import net.deepthought.data.model.FileLink;
import net.deepthought.data.model.Person;
import net.deepthought.data.model.Reference;
import net.deepthought.data.model.ReferenceBase;
import net.deepthought.data.model.ReferenceSubDivision;
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
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 * Created by ganymed on 01/02/15.
 */
public class Alerts {

  public static boolean deleteTagWithUserConfirmationIfIsSetOnEntries(Tag tag) {
    return deleteTagWithUserConfirmationIfIsSetOnEntries(Application.getDeepThought(), tag);
  }

  public static boolean deleteTagWithUserConfirmationIfIsSetOnEntries(DeepThought deepThought, Tag tag) {
    if(tag.hasEntries()) {
      boolean confirmDeleteTag = showConfirmDeleteTagWithEntriesAlert(tag);
      if(confirmDeleteTag)
        return deepThought.removeTag(tag);
    }
    else
      return deepThought.removeTag(tag);

    return false;
  }

  public static boolean showConfirmDeleteTagWithEntriesAlert(Tag tag) {
    return showConfirmationDialog(Localization.getLocalizedString("alert.message.tag.is.set.on.entries", tag.getName(), tag.getEntries().size()),
        Localization.getLocalizedString("alert.title.confirm.delete"));
  }


  public static boolean deleteCategoryWithUserConfirmationIfHasSubCategoriesOrEntries(Category category) {
    return deleteCategoryWithUserConfirmationIfHasSubCategoriesOrEntries(Application.getDeepThought(), category);
  }

  public static boolean deleteCategoryWithUserConfirmationIfHasSubCategoriesOrEntries(DeepThought deepThought, Category category) {
    if(category.hasSubCategories() || category.hasEntries()) {
      Boolean deleteCategory = Alerts.showConfirmDeleteCategoryWithSubCategoriesOrEntries(category);
      if(deleteCategory)
        return deepThought.removeCategory(category);
    }
    else
      return deepThought.removeCategory(category);

    return false;
  }

  public static boolean showConfirmDeleteCategoryWithSubCategoriesOrEntries(Category category) {
    return showConfirmationDialog(Localization.getLocalizedString("alert.message.category.contains.entries.or.sub.categories", category.getName(), category.getName()),
        Localization.getLocalizedString("alert.title.confirm.delete"));
  }


  public static boolean deletePersonWithUserConfirmationIfIsSetOnEntries(Person person) {
    return deletePersonWithUserConfirmationIfIsSetOnEntries(Application.getDeepThought(), person);
  }

  public static boolean deletePersonWithUserConfirmationIfIsSetOnEntries(DeepThought deepThought, Person person) {
    if(person.isSetOnEntries()) {
      boolean confirmDeletePerson = showConfirmDeletePersonWithEntriesAlert(person);
      if(confirmDeletePerson)
        return deepThought.removePerson(person);
    }
    else
      return deepThought.removePerson(person);

    return false;
  }

  public static boolean showConfirmDeletePersonWithEntriesAlert(Person person) {
    return showConfirmationDialog(Localization.getLocalizedString("alert.message.person.is.set.on.entries", person.getNameRepresentation(), person.getAssociatedEntries().size()),
        Localization.getLocalizedString("alert.title.confirm.delete"));
  }


  public static boolean deleteReferenceBaseWithUserConfirmationIfIsSetOnEntries(ReferenceBase referenceBase) {
    return deleteReferenceBaseWithUserConfirmationIfIsSetOnEntries(Application.getDeepThought(), referenceBase);
  }

  public static boolean deleteReferenceBaseWithUserConfirmationIfIsSetOnEntries(DeepThought deepThought, ReferenceBase referenceBase) {
    if(referenceBase instanceof SeriesTitle)
      return deleteSeriesTitleWithUserConfirmationIfHasEntriesOrSubDivisions(deepThought, (SeriesTitle) referenceBase);
    else if(referenceBase instanceof ReferenceSubDivision)
      return deleteReferenceSubDivisionWithUserConfirmationIfHasEntriesOrSubDivisions(deepThought, (ReferenceSubDivision)referenceBase);
    else
      return deleteReferenceWithUserConfirmationIfHasEntriesOrSubDivisions(deepThought, (Reference) referenceBase);
  }

  public static boolean deleteSeriesTitleWithUserConfirmationIfHasEntriesOrSubDivisions(DeepThought deepThought, SeriesTitle seriesTitle) {
    if(seriesTitle.hasEntries() || seriesTitle.hasSerialParts()) {
      boolean confirmDeleteSeriesTitle = showConfirmDeleteSeriesTitleWithEntriesOrSerialPartsAlert(seriesTitle);
      if(confirmDeleteSeriesTitle)
        return deepThought.removeSeriesTitle(seriesTitle);
    } else
      return deepThought.removeSeriesTitle(seriesTitle);

    return false;
  }

  public static boolean showConfirmDeleteSeriesTitleWithEntriesOrSerialPartsAlert(SeriesTitle series) {
    return showConfirmationDialog(Localization.getLocalizedString("alert.message.series.title.contains.entries.or.serial.parts", series.getTextRepresentation(),
            series.getEntries().size(), series.getSerialParts().size()),
        Localization.getLocalizedString("alert.title.confirm.delete"));
  }

  public static boolean deleteReferenceWithUserConfirmationIfHasEntriesOrSubDivisions(DeepThought deepThought, Reference reference) {
    if(reference.hasEntries() || reference.hasSubDivisions()) {
      boolean confirmDeleteReference = showConfirmDeleteReferenceWithEntriesOrSubDivisionsAlert(reference);
      if(confirmDeleteReference)
        return deepThought.removeReference(reference);
    }
    else
      return deepThought.removeReference(reference);

    return false;
  }

  public static boolean showConfirmDeleteReferenceWithEntriesOrSubDivisionsAlert(Reference reference) {
    return showConfirmationDialog(Localization.getLocalizedString("alert.message.reference.contains.entries.or.sub.divisions", reference.getTextRepresentation(),
            reference.getEntries().size(), reference.getSubDivisions().size()),
        Localization.getLocalizedString("alert.title.confirm.delete"));
  }

  public static boolean deleteReferenceSubDivisionWithUserConfirmationIfHasEntriesOrSubDivisions(DeepThought deepThought, ReferenceSubDivision subDivision) {
    if(subDivision.hasEntries() || subDivision.hasSubDivisions()) {
      boolean confirmDeleteSubDivision = showConfirmDeleteReferenceSubDivisionWithEntriesOrSubDivisionsAlert(subDivision);
      if(confirmDeleteSubDivision)
        return deepThought.removeReferenceSubDivision(subDivision);
    }
    else
      return deepThought.removeReferenceSubDivision(subDivision);

    return false;
  }

  public static boolean showConfirmDeleteReferenceSubDivisionWithEntriesOrSubDivisionsAlert(ReferenceSubDivision subDivision) {
    return showConfirmationDialog(Localization.getLocalizedString("alert.message.reference.sub.division.contains.entries.or.sub.divisions", subDivision.getTextRepresentation(),
            subDivision.getEntries().size(), subDivision.getSubDivisions().size()),
        Localization.getLocalizedString("alert.title.confirm.delete"));
  }


  public static boolean deleteFileWithUserConfirmationIfIsSetOnEntriesOrReferenceBases(FileLink file) {
    return deleteFileWithUserConfirmationIfIsSetOnEntriesOrReferenceBases(Application.getDeepThought(), file);
  }

  public static boolean deleteFileWithUserConfirmationIfIsSetOnEntriesOrReferenceBases(DeepThought deepThought, FileLink file) {
    if(file.isAttachedToEntries() || file.isEmbeddedInEntries() || file.isAttachedToReferenceBases() || file.isEmbeddedInReferenceBases()) {
      boolean confirmDeleteFile = showConfirmDeleteFileWithEntriesOrReferenceBasesAlert(file);
      if(confirmDeleteFile)
        return deepThought.removeFile(file);
    }
    else
      return deepThought.removeFile(file);

    return false;
  }

  public static boolean showConfirmDeleteFileWithEntriesOrReferenceBasesAlert(FileLink file) {
    return showConfirmationDialog(Localization.getLocalizedString("alert.message.file.link.contains.entries.or.reference.bases", file.getTextRepresentation(),
            file.getEntriesAttachedTo().size() + file.getEntriesEmbeddedIn().size(), file.getReferenceBasesAttachedTo().size() + file.getReferenceBasesEmbeddedIn().size()),
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
    if(owner != null)
      alert.initOwner(owner);

    alert.setTitle(Localization.getLocalizedString("alert.title.entity.contains.unsaved.changes", translatedEntityName));
    setAlertContent(alert, Localization.getLocalizedString("alert.message.entity.contains.unsaved.changes", translatedEntityName));
    alert.setHeaderText(null);

    alert.getButtonTypes().clear();
//    alert.getButtonTypes().setAll(ButtonType.CANCEL, ButtonType.NO, ButtonType.YES);
    alert.getButtonTypes().add(0, ButtonType.CANCEL);
//    alert.getButtonTypes().addAll(ButtonType.NO, ButtonType.YES);
    alert.getButtonTypes().add(1, ButtonType.NO);
    alert.getButtonTypes().add(2, ButtonType.YES);

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
    if(owner != null)
      alert.initOwner(owner);

    alert.setTitle(alertTitle);
    setAlertContent(alert, message);
    alert.setHeaderText(null);

    alert.getButtonTypes().setAll(ButtonType.NO, ButtonType.YES);

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
    if(owner != null)
      alert.initOwner(owner);

    alert.setTitle(alertTitle);
    setAlertContent(alert, infoMessage);
    alert.setHeaderText(null);

    alert.getButtonTypes().setAll(ButtonType.OK);
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
    if(owner != null)
      alert.initOwner(owner);

    alert.setTitle(alertTitle);
    setAlertContent(alert, errorMessage);
    alert.setHeaderText(null);

    if(exception != null)
      createExpandableException(alert, exception);

    alert.getButtonTypes().setAll(ButtonType.OK);
    alert.showAndWait();
  }

  protected static void setAlertContent(Alert alert, String content) {
    double maxWidth = Screen.getPrimary().getVisualBounds().getWidth();
    if(alert.getOwner() != null) {
      Screen ownersScreen = FXUtils.getScreenWindowLeftUpperCornerIsIn(alert.getOwner());
      if(ownersScreen != null)
        maxWidth = ownersScreen.getVisualBounds().getWidth();
    }
    maxWidth *= 0.6; // set max width to 60 % of Screen width

    Label contentLabel = new Label(content);
    contentLabel.setWrapText(true);
    contentLabel.setPrefHeight(Region.USE_COMPUTED_SIZE);
    contentLabel.setMaxHeight(Double.MAX_VALUE);
    contentLabel.setMaxWidth(maxWidth);

    VBox contentPane = new VBox(contentLabel);
    contentPane.setPrefHeight(Region.USE_COMPUTED_SIZE);
    contentPane.setMaxHeight(Double.MAX_VALUE);
    VBox.setVgrow(contentLabel, Priority.ALWAYS);

    alert.getDialogPane().setPrefHeight(Region.USE_COMPUTED_SIZE);
    alert.getDialogPane().setMaxHeight(Double.MAX_VALUE);
    alert.getDialogPane().setMaxWidth(maxWidth);
    alert.getDialogPane().setContent(contentPane);
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
