package net.deepthought.controller;

import net.deepthought.Application;
import net.deepthought.controller.enums.DialogResult;
import net.deepthought.controls.FXUtils;
import net.deepthought.data.contentextractor.IOnlineArticleContentExtractor;
import net.deepthought.data.model.Category;
import net.deepthought.data.model.Entry;
import net.deepthought.data.model.FileLink;
import net.deepthought.data.model.Person;
import net.deepthought.data.model.ReferenceBase;
import net.deepthought.data.model.settings.WindowSettings;
import net.deepthought.data.persistence.db.BaseEntity;
import net.deepthought.util.JavaFxLocalization;
import net.deepthought.util.Localization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

/**
 * Created by ganymed on 31/12/14.
 */
public class Dialogs {

  private final static Logger log = LoggerFactory.getLogger(Dialogs.class);

  public final static String DialogsBaseFolder = "dialogs/";

  public final static String ControlsBaseFolder = "controls/";


  protected static ObservableSet<Stage> openedChildWindows = FXCollections.observableSet();

  public static ObservableSet<Stage> getOpenedChildWindows() {
    return openedChildWindows;
  }

  public static void showEditCategoryDialog(final Category category) {
    showEditCategoryDialog(category, null);
  }

  public static void showEditCategoryDialog(final Category category, final ChildWindowsControllerListener listener) {
    try {
//      FXMLLoader loader = new FXMLLoader();
//      Stage dialogStage = createStage(loader, "EditCategoryDialog.fxml");
//
//      EditCategoryDialogController controller = loader.getController();
//      controller.setCategoryAndStage(category, dialogStage);
//
//      controller.setListener(new ChildWindowsControllerListener() {
//        @Override
//        public void windowClosing(Stage stage, ChildWindowsController controller) {
//          if(controller.getDialogResult() == DialogResult.Ok) {
//            if(category.getId() == null) { // a new Category
//              Application.getDeepThought().addCategory(category);
//            }
//          }
//
//          if(listener != null)
//            listener.windowClosing(stage, controller);
//        }
//
//        @Override
//        public void windowClosed(Stage stage, ChildWindowsController controller) {
//          removeClosedChildWindow(stage);
//
//          if(listener != null)
//            listener.windowClosed(stage, controller);
//        }
//      });
//
//      addOpenedChildWindow(dialogStage);
//
//      dialogStage.show();
//      dialogStage.requestFocus();
    } catch(Exception ex) {
      log.error("Could not load / show EditCategoryDialog", ex);
    }
  }


  public static void showEditEntryDialog(final Entry entry) {
    showEditEntryDialog(entry, null);
  }

  public static void showEditEntryDialog(final Entry entry, final ChildWindowsControllerListener listener) {
    if(Platform.isFxApplicationThread())
      showEditEntryDialogOnUiThread(entry, listener);
    else
      Platform.runLater(() -> showEditEntryDialogOnUiThread(entry, listener));
  }

  protected static void showEditEntryDialogOnUiThread(final Entry entry, final ChildWindowsControllerListener listener) {
    try {
      FXMLLoader loader = new FXMLLoader();
      Stage dialogStage = createStage(loader, "EditEntryDialog.fxml");
      dialogStage.setMinHeight(500);
      dialogStage.setMinWidth(500);
//      loader.setResources(Localization.getStringsResourceBundle());
//      loader.setLocation(Dialogs.class.getClassLoader().getResource(DialogsBaseFolder + "EditEntryDialog.fxml"));
//      Parent parent = loader.load();
//
//      // Create the dialog Stage.
//      Stage dialogStage = new Stage();
//      dialogStage.initModality(Modality.NONE);
////      windowStage.initOwner(stage);
//      Scene scene = new Scene(parent);
//      dialogStage.setScene(scene);

      // Set the person into the controller.
      EditEntryDialogController controller = loader.getController();
      controller.setWindowStageAndEntry(dialogStage, entry);

      controller.setListener(new ChildWindowsControllerListener() {
        @Override
        public void windowClosing(Stage stage, ChildWindowsController controller) {
          if(listener != null)
            listener.windowClosing(stage, controller);
        }

        @Override
        public void windowClosed(Stage stage, ChildWindowsController controller) {
          removeClosedChildWindow(stage);

          if(listener != null)
            listener.windowClosed(stage, controller);
        }
      });

      addOpenedChildWindow(dialogStage);

      dialogStage.show();
      dialogStage.requestFocus();
    } catch(Exception ex) {
      log.error("Could not load / show EditEntryDialog", ex);
    }
  }


  public static void showEditPersonDialog(Person person) {
    showEditPersonDialog(person, null);
  }

  public static void showEditPersonDialog(final Person person, final ChildWindowsControllerListener listener) {
    try {
      FXMLLoader loader = new FXMLLoader();
      Stage dialogStage = createStage(loader, "EditPersonDialog.fxml", StageStyle.UTILITY);

      // Set the person into the controller.
      EditPersonDialogController controller = loader.getController();
      controller.setPersonAndStage(dialogStage, person);

      controller.setListener(new ChildWindowsControllerListener() {
        @Override
        public void windowClosing(Stage stage, ChildWindowsController controller) {
          if(controller.getDialogResult() == DialogResult.Ok) {
            if(person.isPersisted() == false) { // a new Person
              Application.getDeepThought().addPerson(person);
            }
          }

          if(listener != null)
            listener.windowClosing(stage, controller);
        }

        @Override
        public void windowClosed(Stage stage, ChildWindowsController controller) {
          removeClosedChildWindow(stage);

          if(listener != null)
            listener.windowClosed(stage, controller);
        }
      });

      addOpenedChildWindow(dialogStage);

      dialogStage.show();
      dialogStage.requestFocus();
    } catch(Exception ex) {
      log.error("Could not load / show EditPersonDialog", ex);
    }
  }


  public static void showEditFileDialog(FileLink file) {
    showEditFileDialog(file, null);
  }

  public static void showEditFileDialog(final FileLink file, final ChildWindowsControllerListener listener) {
    try {
      FXMLLoader loader = new FXMLLoader();
      Stage dialogStage = createStage(loader, "EditFileDialog.fxml", StageStyle.UTILITY);

      // Set the file into the controller.
      EditFileDialogController controller = loader.getController();
      controller.setEditFile(dialogStage, file);

      controller.setListener(new ChildWindowsControllerListener() {
        @Override
        public void windowClosing(Stage stage, ChildWindowsController controller) {
          if(listener != null)
            listener.windowClosing(stage, controller);
        }

        @Override
        public void windowClosed(Stage stage, ChildWindowsController controller) {
          removeClosedChildWindow(stage);

          if(listener != null)
            listener.windowClosed(stage, controller);
        }
      });

      addOpenedChildWindow(dialogStage);

      dialogStage.show();
      dialogStage.requestFocus();
    } catch(Exception ex) {
      log.error("Could not load / show EditFileDialog", ex);
    }
  }


  public static void showEditReferenceDialog(ReferenceBase referenceBase) {
    showEditReferenceDialog(referenceBase, null);
  }

  public static void showEditReferenceDialog(final ReferenceBase referenceBase, final ChildWindowsControllerListener listener) {
    showEditReferenceDialog(referenceBase, null, listener);
  }

  public static void showEditReferenceDialog(final ReferenceBase referenceBase, ReferenceBase persistedParentReferenceBase, final ChildWindowsControllerListener listener) {
    try {
      FXMLLoader loader = new FXMLLoader();
      Stage dialogStage = createStage(loader, "EditReferenceDialog.fxml");
      dialogStage.setMinHeight(500);
      dialogStage.setMinWidth(500);

      // Set the referenceBase into the controller.
      EditReferenceDialogController controller = loader.getController();
      controller.setWindowStageAndReferenceBase(dialogStage, referenceBase, persistedParentReferenceBase);

      controller.setListener(new ChildWindowsControllerListener() {
        @Override
        public void windowClosing(Stage stage, ChildWindowsController controller) {
          if(listener != null)
            listener.windowClosing(stage, controller);
        }

        @Override
        public void windowClosed(Stage stage, ChildWindowsController controller) {
          removeClosedChildWindow(stage);

          if(listener != null)
            listener.windowClosed(stage, controller);
        }
      });

      addOpenedChildWindow(dialogStage);

      dialogStage.show();
      dialogStage.requestFocus();
    } catch(Exception ex) {
      log.error("Could not load / show EditReferenceDialog", ex);
    }
  }


  public static void showArticlesOverviewDialog(final IOnlineArticleContentExtractor articleContentExtractor) {
    showArticlesOverviewDialog(articleContentExtractor, null);
  }

  public static void showArticlesOverviewDialog(final IOnlineArticleContentExtractor articleContentExtractor, final ChildWindowsControllerListener listener) {
    try {
      FXMLLoader loader = new FXMLLoader();
      Stage dialogStage = createStage(loader, "ArticlesOverviewDialog.fxml");

      // Set the referenceBase into the controller.
      ArticlesOverviewDialogController controller = loader.getController();
      controller.setWindowStageAndArticleContentExtractor(dialogStage, articleContentExtractor);

      controller.setListener(new ChildWindowsControllerListener() {
        @Override
        public void windowClosing(Stage stage, ChildWindowsController controller) {
          if (listener != null)
            listener.windowClosing(stage, controller);
        }

        @Override
        public void windowClosed(Stage stage, ChildWindowsController controller) {
          removeClosedChildWindow(stage);

          if (listener != null)
            listener.windowClosed(stage, controller);
        }
      });

      addOpenedChildWindow(dialogStage);

      dialogStage.show();
      dialogStage.requestFocus();
    } catch(Exception ex) {
      log.error("Could not load / show EditReferenceDialog", ex);
    }
  }


  public static void showRestoreBackupDialog(Stage owner) {
    try {
      FXMLLoader loader = new FXMLLoader();
      Stage dialogStage = createStage(loader, "RestoreBackupDialog.fxml", StageStyle.DECORATED, Modality.APPLICATION_MODAL);

      dialogStage.initOwner(owner);

      RestoreBackupDialogController controller = loader.getController();
      controller.setWindowStage(dialogStage);

      dialogStage.show();
      dialogStage.requestFocus();
    } catch(Exception ex) {
      log.error("Could not load / show RestoreBackupDialog", ex);
    }
  }

  public static void showSelectEntitiesToImportDialog(final BaseEntity parentDataEntityToImport, Stage owner) {
    showSelectEntitiesToImportDialog(parentDataEntityToImport, owner, null);
  }

  public static void showSelectEntitiesToImportDialog(final BaseEntity parentDataEntityToImport, Stage owner, final ChildWindowsControllerListener listener) {
    try {
      FXMLLoader loader = new FXMLLoader();
      Stage dialogStage = createStage(loader, "SelectEntitiesToImportDialog.fxml", StageStyle.DECORATED, Modality.APPLICATION_MODAL);

      dialogStage.initOwner(owner);

      SelectEntitiesToImportDialogController controller = loader.getController();
      controller.setDataToImport(parentDataEntityToImport, dialogStage);

      controller.setListener(listener);

      dialogStage.show();
      dialogStage.requestFocus();
    } catch(Exception ex) {
      log.error("Could not load / show SelectEntitiesToImportDialog", ex);
    }
  }


  protected static Stage createStage(FXMLLoader loader, String dialogFilename) throws java.io.IOException {
    return createStage(loader, dialogFilename, StageStyle.DECORATED);
  }

  protected static Stage createStage(FXMLLoader loader, String dialogFilename, StageStyle stageStyle) throws java.io.IOException {
    return createStage(loader, dialogFilename, stageStyle, Modality.NONE);
  }

  protected static Stage createStage(FXMLLoader loader, String dialogFilename, StageStyle stageStyle, Modality modality) throws java.io.IOException {
    return createStage(loader, dialogFilename, stageStyle, modality, false);
  }

  protected static Stage createStage(FXMLLoader loader, String dialogFilename, StageStyle stageStyle, Modality modality, boolean isToolWindow) throws java.io.IOException {
    loader.setResources(Localization.getStringsResourceBundle());

    if(isToolWindow == false)
      loader.setLocation(Dialogs.class.getClassLoader().getResource(DialogsBaseFolder + dialogFilename));
    else
      loader.setLocation(Dialogs.class.getClassLoader().getResource(ControlsBaseFolder + dialogFilename));

    Parent parent = loader.load();

    // Create the dialog Stage.
    Stage dialogStage = new Stage();
//      dialogStage.initModality(Modality.WINDOW_MODAL);
    dialogStage.initModality(modality);
    dialogStage.initStyle(stageStyle);
//      dialogStage.initOwner(windowStage);

    Scene scene = new Scene(parent);
    dialogStage.setScene(scene);
    return dialogStage;
  }


  public static Stage createToolWindowStage(Parent parent, Window owner) {
    return createToolWindowStage(parent, owner, "");
  }

  public static Stage createToolWindowStage(Parent parent, Window owner, String windowTitle) {
    return createToolWindowStage(parent, owner, windowTitle, new WindowSettings());
  }

  public static Stage createToolWindowStage(Parent parent, Window owner, String windowTitle, WindowSettings settings) {
    Stage stage = new Stage(StageStyle.UTILITY);

    if(owner != null)
      stage.initOwner(owner);

    Scene scene = new Scene(parent);

    stage.setScene(scene);
    FXUtils.applyWindowSettingsAndListenToChanges(stage, settings);
    JavaFxLocalization.bindStageTitle(stage, windowTitle);

    return stage;
  }


  protected static void addOpenedChildWindow(final Stage childWindow) {
    openedChildWindows.add(childWindow);
  }

  protected static void removeClosedChildWindow(Stage childWindow) {
    openedChildWindows.remove(childWindow);
  }
}
