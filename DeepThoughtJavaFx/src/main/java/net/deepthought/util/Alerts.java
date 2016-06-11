package net.deepthought.util;

import net.deepthought.Application;
import net.deepthought.communication.messages.request.AskForDeviceRegistrationRequest;
import net.deepthought.communication.messages.response.AskForDeviceRegistrationResponse;
import net.deepthought.communication.model.HostInfo;
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
import net.deepthought.util.localization.Localization;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
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
    return showConfirmationDialog(net.deepthought.util.localization.Localization.getLocalizedString("alert.message.tag.is.set.on.entries", tag.getName(), tag.getEntries().size()),
        net.deepthought.util.localization.Localization.getLocalizedString("alert.title.confirm.delete"));
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
    return showConfirmationDialog(net.deepthought.util.localization.Localization.getLocalizedString("alert.message.category.contains.entries.or.sub.categories", category.getName(), category.getName()),
        net.deepthought.util.localization.Localization.getLocalizedString("alert.title.confirm.delete"));
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
    return showConfirmationDialog(net.deepthought.util.localization.Localization.getLocalizedString("alert.message.person.is.set.on.entries", person.getNameRepresentation(), person.getAssociatedEntries().size()),
        net.deepthought.util.localization.Localization.getLocalizedString("alert.title.confirm.delete"));
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
    return showConfirmationDialog(net.deepthought.util.localization.Localization.getLocalizedString("alert.message.series.title.contains.entries.or.serial.parts", series.getTextRepresentation(),
            series.getEntries().size(), series.getSerialParts().size()),
        net.deepthought.util.localization.Localization.getLocalizedString("alert.title.confirm.delete"));
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
    return showConfirmationDialog(net.deepthought.util.localization.Localization.getLocalizedString("alert.message.reference.contains.entries.or.sub.divisions", reference.getTextRepresentation(),
            reference.getEntries().size(), reference.getSubDivisions().size()),
        net.deepthought.util.localization.Localization.getLocalizedString("alert.title.confirm.delete"));
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
    return showConfirmationDialog(net.deepthought.util.localization.Localization.getLocalizedString("alert.message.reference.sub.division.contains.entries.or.sub.divisions", subDivision.getTextRepresentation(),
            subDivision.getEntries().size(), subDivision.getSubDivisions().size()),
        net.deepthought.util.localization.Localization.getLocalizedString("alert.title.confirm.delete"));
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
    return showConfirmationDialog(net.deepthought.util.localization.Localization.getLocalizedString("alert.message.file.link.contains.entries.or.reference.bases", file.getTextRepresentation(),
            file.getEntriesAttachedTo().size() + file.getEntriesEmbeddedIn().size(), file.getReferenceBasesAttachedTo().size() + file.getReferenceBasesEmbeddedIn().size()),
        net.deepthought.util.localization.Localization.getLocalizedString("alert.title.confirm.delete"));
  }


  public static boolean askUserIfEditedSeriesTitleShouldBeSaved(SeriesTitle seriesTitle) {
    return askUserIfEditedReferenceBaseShouldBeSaved(seriesTitle.getTextRepresentation(), net.deepthought.util.localization.Localization.getLocalizedString("series.title"));
  }

  public static boolean askUserIfEditedReferenceShouldBeSaved(Reference reference) {
    return askUserIfEditedReferenceBaseShouldBeSaved(reference.getPreview(), net.deepthought.util.localization.Localization.getLocalizedString("reference"));
  }

  protected static boolean askUserIfEditedReferenceBaseShouldBeSaved(String referenceBasePreview, String localizedReferenceBaseName) {
    return showConfirmationDialog(net.deepthought.util.localization.Localization.getLocalizedString("alert.message.ask.save.entity", localizedReferenceBaseName, referenceBasePreview),
        net.deepthought.util.localization.Localization.getLocalizedString("alert.title.should.edited.data.be.saved"));
  }

  // TODO: merge with that ones above
  public static ButtonType askUserIfEditedEntityShouldBeSaved(Stage owner, String entityNameResourceKey) {
    String translatedEntityName = net.deepthought.util.localization.Localization.getLocalizedString(entityNameResourceKey);

    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    if(owner != null)
      alert.initOwner(owner);

    alert.setTitle(net.deepthought.util.localization.Localization.getLocalizedString("alert.title.entity.contains.unsaved.changes", translatedEntityName));
    setAlertContent(alert, net.deepthought.util.localization.Localization.getLocalizedString("alert.message.entity.contains.unsaved.changes", translatedEntityName));
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


  public static Alert createUnregisteredDeviceFoundAlert(HostInfo device, Stage windowStage) {
    windowStage.show();
    windowStage.requestFocus();
    windowStage.toFront();

    String message = Localization.getLocalizedString("alert.message.unregistered.device.found");
    if(StringUtils.isNotNullOrEmpty(device.getUserName())) {
      message += Localization.getLocalizedString("user.info", device.getUserName());
    }
    message += Localization.getLocalizedString("device.info", device.getDeviceInfoString());

    message += Localization.getLocalizedString("ip.address", device.getAddress());

//    String logoPath = IconManager.getInstance().getLogoForOperatingSystem(item.getPlatform(), item.getOsVersion(), item.getPlatformArchitecture());
//    if(logoPath != null)
//      imgvwOsLogo.setImage(new Image(logoPath));
//    else
//      imgvwOsLogo.setVisible(false);

    return createConfirmationDialog(message,
        Localization.getLocalizedString("alert.title.unregistered.device.found"), windowStage);
  }

  public static boolean showDeviceAsksForRegistrationAlert(AskForDeviceRegistrationRequest request, Stage windowStage) {
    windowStage.show();
    windowStage.requestFocus();
    windowStage.toFront();

    String message = Localization.getLocalizedString("alert.message.ask.for.device.registration");
    if(StringUtils.isNotNullOrEmpty(request.getUser().getUserInfoString())) {
      message += Localization.getLocalizedString("user.info", request.getUser().getUserInfoString());
    }
    message += Localization.getLocalizedString("device.info", request.getDevice().getDeviceInfoString());

    return showConfirmationDialog(message,
        Localization.getLocalizedString("alert.title.ask.for.device.registration"), windowStage);
  }

  public static void showDeviceRegistrationSuccessfulAlert(AskForDeviceRegistrationResponse response, Stage windowStage) {
    windowStage.show();
    windowStage.requestFocus();
    windowStage.toFront();

    showInfoMessage(windowStage, Localization.getLocalizedString("alert.message.successfully.registered.at.device", response.getDevice()),
        Localization.getLocalizedString("alert.title.device.registration.successful"));
  }

  public static void showServerDeniedDeviceRegistrationAlert(AskForDeviceRegistrationResponse response, Stage windowStage) {
    windowStage.show();
    windowStage.requestFocus();
    windowStage.toFront();

    showInfoMessage(windowStage, net.deepthought.util.localization.Localization.getLocalizedString("alert.title.device.registration.denied"),
                                 net.deepthought.util.localization.Localization.getLocalizedString("alert.message.server.denied.device.registration"));
  }


  protected static boolean showConfirmationDialog(String message, String alertTitle) {
    return showConfirmationDialog(message, alertTitle, null);
  }

  protected static boolean showConfirmationDialog(String message, String alertTitle, Stage owner) {
    Alert alert = createConfirmationDialog(message, alertTitle, owner);

    Optional<ButtonType> result = alert.showAndWait();
    return result.get() == ButtonType.YES;
  }

  protected static Alert createConfirmationDialog(String message, String alertTitle, Stage owner) {
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    if(owner != null)
      alert.initOwner(owner);

    alert.setTitle(alertTitle);
    setAlertContent(alert, message);
    alert.setHeaderText(null);

    alert.getButtonTypes().setAll(ButtonType.NO, ButtonType.YES);

    return alert;
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
    if(error.getNotificationMessageTitle() == null)
      title = net.deepthought.util.localization.Localization.getLocalizedString("alert.message.title.error.occurred");

    showErrorMessage(owner, error, title);
  }

  public static void showErrorMessage(final Stage owner, DeepThoughtError error, String alertTitle) {
    String message = error.getNotificationMessage();

    if(error.isSevere()) {
      if(StringUtils.isNullOrEmpty(alertTitle))
        alertTitle = net.deepthought.util.localization.Localization.getLocalizedString("alert.message.title.severe.error.occurred");
      message = net.deepthought.util.localization.Localization.getLocalizedString("alert.message.message.severe.error.occurred", error.getNotificationMessage());
    }

    showErrorMessage(owner, message, alertTitle, error.getException());
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
    contentLabel.setMaxHeight(FXUtils.SizeMaxValue);
    contentLabel.setMaxWidth(maxWidth);

    VBox contentPane = new VBox(contentLabel);
    contentPane.setPrefHeight(Region.USE_COMPUTED_SIZE);
    contentPane.setMaxHeight(FXUtils.SizeMaxValue);
    VBox.setVgrow(contentLabel, Priority.ALWAYS);

    alert.getDialogPane().setPrefHeight(Region.USE_COMPUTED_SIZE);
    alert.getDialogPane().setMaxHeight(FXUtils.SizeMaxValue);
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

    textArea.setMaxWidth(FXUtils.SizeMaxValue);
    textArea.setMaxHeight(FXUtils.SizeMaxValue);
    GridPane.setVgrow(textArea, Priority.ALWAYS);
    GridPane.setHgrow(textArea, Priority.ALWAYS);

    GridPane expContent = new GridPane();
    expContent.setMaxWidth(FXUtils.SizeMaxValue);
    expContent.add(label, 0, 0);
    expContent.add(textArea, 0, 1);

// Set expandable Exception into the dialog pane.
    alert.getDialogPane().setExpandableContent(expContent);
  }


  public static String askForTextInput(String questionText, String alertTitleText, String defaultValue) {
    TextInputDialog dialog = new TextInputDialog(defaultValue);
    dialog.setHeaderText(null);
    dialog.setTitle(alertTitleText);
    dialog.setContentText(questionText);

    Optional<String> result = dialog.showAndWait();
    return result.get();
  }
}
