package net.deepthought.controller;

import net.deepthought.Application;
import net.deepthought.controller.enums.DialogResult;
import net.deepthought.controls.html.HtmlEditor;
import net.deepthought.controls.utils.FXUtils;
import net.deepthought.controls.utils.IEditedEntitiesHolder;
import net.deepthought.data.contentextractor.EntryCreationResult;
import net.deepthought.data.contentextractor.IOnlineArticleContentExtractor;
import net.deepthought.data.html.ImageElementData;
import net.deepthought.data.model.Category;
import net.deepthought.data.model.Entry;
import net.deepthought.data.model.FileLink;
import net.deepthought.data.model.Person;
import net.deepthought.data.model.ReferenceBase;
import net.deepthought.data.model.Tag;
import net.deepthought.data.model.settings.WindowSettings;
import net.deepthought.data.persistence.db.BaseEntity;
import net.deepthought.util.JavaFxLocalization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
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


  public static void showEditEntryDialog(final Entry entry) {
    if(Platform.isFxApplicationThread())
      showEditEntryDialogOnUiThread(entry, null, null);
    else
      Platform.runLater(() -> showEditEntryDialogOnUiThread(entry, null, null));
  }

  public static void showEditEntryDialog(final EntryCreationResult creationResult) {
    if(Platform.isFxApplicationThread())
      showEditEntryDialogOnUiThread(null, creationResult, null);
    else
      Platform.runLater(() -> showEditEntryDialogOnUiThread(null, creationResult, null));
  }

  protected static void showEditEntryDialogOnUiThread(final Entry entry, final EntryCreationResult creationResult, final ChildWindowsControllerListener listener) {
    try {
      FXMLLoader loader = new FXMLLoader();
      Stage dialogStage = createStageForEntityDialog(loader, "EditEntryDialog.fxml");
      dialogStage.setMinHeight(500);
      dialogStage.setMinWidth(500);
//
      EditEntryDialogController controller = loader.getController();
      if(entry != null)
        controller.setWindowStageAndEntry(dialogStage, entry);
      else if(creationResult != null)
        controller.setWindowStageAndEntryCreationResult(dialogStage, creationResult);

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


  public static void showEditTagDialog(Tag tag) {
    showEditTagDialog(tag, -1, -1, null, false);
  }

  public static void showEditTagDialog(Tag tag,  Window window, boolean modal) {
    showEditTagDialog(tag, window.getX() + window.getWidth() / 2, window.getY() + (window.getHeight() - 146) / 2, window, modal); // 146 = EditTagDialog's height
  }

  public static void showEditTagDialog(Tag tag, double centerX, double y, Window window, boolean modal) {
    showEditTagDialog(tag, centerX, y, window, modal, null);
  }

  public static void showEditTagDialog(final Tag tag, double centerX, double y, Window window, boolean modal, final ChildWindowsControllerListener listener) {
    try {
      FXMLLoader loader = new FXMLLoader();
      Stage dialogStage = createStageForEntityDialog(loader, "EditTagDialog.fxml", StageStyle.UTILITY, modal ? Modality.WINDOW_MODAL : Modality.NONE, window);

      // Set the tag into the controller.
      EditTagDialogController controller = loader.getController();
      controller.setTagAndStage(dialogStage, tag);

      controller.setListener(new ChildWindowsControllerListener() {
        @Override
        public void windowClosing(Stage stage, ChildWindowsController controller) {
          if (controller.getDialogResult() == DialogResult.Ok) {
            if (tag.isPersisted() == false) { // a new Tag
              Application.getDeepThought().addTag(tag);
            }
          }

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

      if(centerX > 0) {
        double x = centerX - dialogStage.getWidth() / 2;
        dialogStage.setX(x);
      }
      if(y > 0)
        dialogStage.setY(y);
    } catch(Exception ex) {
      log.error("Could not load / show EditTagDialog", ex);
    }
  }


  public static void showEditCategoryDialog(Category category) {
    showEditCategoryDialog(category, -1, -1, null, false);
  }

  public static void showEditCategoryDialog(Category category,  Window window, boolean modal) {
    showEditCategoryDialog(category, window, modal, null);
  }

  public static void showEditCategoryDialog(Category category,  Window window, boolean modal, ChildWindowsControllerListener listener) {
    showEditCategoryDialog(category, null, window.getX() + window.getWidth() / 2, window.getY() + (window.getHeight() - 146) / 2, window, modal, listener); // 146 = EditTagDialog's height
  }

  public static void showEditCategoryDialog(Category category, Category parentCategory,  Window window, boolean modal) {
    showEditCategoryDialog(category, parentCategory, window.getX() + window.getWidth() / 2, window.getY() + (window.getHeight() - 146) / 2, window, modal, null); // 146 =
    // EditTagDialog's height
  }

  public static void showEditCategoryDialog(Category category, double centerX, double y, Window window, boolean modal) {
    showEditCategoryDialog(category, null, centerX, y, window, modal, null);
  }

  public static void showEditCategoryDialog(final Category category, final Category parentCategory, double centerX, double y, Window window, boolean modal, final ChildWindowsControllerListener listener) {
    try {
      FXMLLoader loader = new FXMLLoader();
      Stage dialogStage = createStageForEntityDialog(loader, "EditCategoryDialog.fxml", StageStyle.UTILITY, modal ? Modality.WINDOW_MODAL : Modality.NONE, window);

      // Set the category into the controller.
      EditCategoryDialogController controller = loader.getController();
      controller.setCategoryAndStage(dialogStage, category);

      controller.setListener(new ChildWindowsControllerListener() {
        @Override
        public void windowClosing(Stage stage, ChildWindowsController controller) {
          if (controller.getDialogResult() == DialogResult.Ok) {
            if (category.isPersisted() == false) { // a new Tag
              Application.getDeepThought().addCategory(category);
              if(parentCategory != null)
                parentCategory.addSubCategory(category);
            }
          }

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

      if(centerX > 0) {
        double x = centerX - dialogStage.getWidth() / 2;
        dialogStage.setX(x);
      }
      if(y > 0)
        dialogStage.setY(y);
    } catch(Exception ex) {
      log.error("Could not load / show EditCategoryDialog", ex);
    }
  }


  public static void showEditPersonDialog(Person person) {
    showEditPersonDialog(person, null);
  }

  public static void showEditPersonDialog(final Person person, final ChildWindowsControllerListener listener) {
    try {
      FXMLLoader loader = new FXMLLoader();
      Stage dialogStage = createStageForEntityDialog(loader, "EditPersonDialog.fxml", StageStyle.UTILITY);

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
      Stage dialogStage = createStageForEntityDialog(loader, "EditFileDialog.fxml", StageStyle.UTILITY);

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


  public static void showEditEmbeddedFileDialog(HtmlEditor editor, IEditedEntitiesHolder<FileLink> editedFiles, FileLink file) {
    showEditEmbeddedFileDialog(editor, editedFiles, file, null);
  }

  public static void showEditEmbeddedFileDialog(HtmlEditor editor, IEditedEntitiesHolder<FileLink> editedFiles, FileLink file, ImageElementData imgElement) {
    showEditEmbeddedFileDialog(editor, editedFiles, file, imgElement, null);
  }

  public static void showEditEmbeddedFileDialog(HtmlEditor editor, IEditedEntitiesHolder<FileLink> editedFiles, FileLink file, ImageElementData imgElement, final ChildWindowsControllerListener listener) {
    try {
      FXMLLoader loader = new FXMLLoader();
      Stage dialogStage = createStageForEntityDialog(loader, "EditEmbeddedFileDialog.fxml", StageStyle.UTILITY);

      EditEmbeddedFileDialogController controller = loader.getController();
      controller.setEditFile(dialogStage, editor, editedFiles, file, imgElement);

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
      log.error("Could not load / show EditEmbeddedFileDialog", ex);
    }
  }


  public static void showEditReferenceDialog(EntryCreationResult creationResult) {
    showEditReferenceDialog(null, null, creationResult, null);
  }

  public static void showEditReferenceDialog(ReferenceBase referenceBase) {
    showEditReferenceDialog(referenceBase, null);
  }

  public static void showEditReferenceDialog(final ReferenceBase referenceBase, final ChildWindowsControllerListener listener) {
    showEditReferenceDialog(referenceBase, null, listener);
  }

  public static void showEditReferenceDialog(final ReferenceBase referenceBase, ReferenceBase persistedParentReferenceBase, final ChildWindowsControllerListener listener) {
    showEditReferenceDialog(referenceBase, persistedParentReferenceBase, null, listener);
  }

  protected static void showEditReferenceDialog(final ReferenceBase referenceBase, ReferenceBase persistedParentReferenceBase, EntryCreationResult creationResult, final ChildWindowsControllerListener listener) {
    if(Platform.isFxApplicationThread())
      showEditReferenceDialogOnUiThread(referenceBase, persistedParentReferenceBase, creationResult, listener);
    else
      Platform.runLater(() -> showEditReferenceDialogOnUiThread(referenceBase, persistedParentReferenceBase, creationResult, listener));
  }

  protected static void showEditReferenceDialogOnUiThread(final ReferenceBase referenceBase, ReferenceBase persistedParentReferenceBase, EntryCreationResult creationResult, final
  ChildWindowsControllerListener listener) {
    try {
      FXMLLoader loader = new FXMLLoader();
      Stage dialogStage = createStageForEntityDialog(loader, "EditReferenceDialog.fxml");
      dialogStage.setMinHeight(500);
      dialogStage.setMinWidth(500);

      EditReferenceDialogController controller = loader.getController();
      if(creationResult != null)
        controller.setWindowStageAndReferenceBase(dialogStage, creationResult);
      else
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
      log.error("Could not load / show ArticlesOverviewDialog", ex);
    }
  }


  public static void showRegisterUserDevicesDialog(Stage owner) {
    try {
      FXMLLoader loader = new FXMLLoader();
      Stage dialogStage = createStage(loader, "RegisterUserDevicesDialog.fxml");

      dialogStage.initOwner(owner);

      RegisterUserDevicesDialogController controller = loader.getController();
      controller.setWindowStage(dialogStage);

      controller.setListener(new ChildWindowsControllerListener() {
        @Override
        public void windowClosing(Stage stage, ChildWindowsController controller) {

        }

        @Override
        public void windowClosed(Stage stage, ChildWindowsController controller) {
          removeClosedChildWindow(stage);
        }
      });

      addOpenedChildWindow(dialogStage);

      dialogStage.show();
      dialogStage.requestFocus();
    } catch(Exception ex) {
      log.error("Could not load / show RegisterUserDevicesDialog", ex);
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


  protected static Stage createStageForEntityDialog(FXMLLoader loader, String dialogFilename) throws IOException {
    return createStageForEntityDialog(loader, dialogFilename, StageStyle.DECORATED);
  }

  protected static Stage createStageForEntityDialog(FXMLLoader loader, String dialogFilename, StageStyle stageStyle) throws IOException {
    return createStageForEntityDialog(loader, dialogFilename, stageStyle, Modality.NONE, null);
  }

  protected static Stage createStageForEntityDialog(FXMLLoader loader, String dialogFilename, StageStyle stageStyle, Modality modality, Window owner) throws IOException {
    return createStage(loader, dialogFilename, stageStyle, modality, owner, true);
  }

  protected static Stage createStage(FXMLLoader loader, String dialogFilename) throws IOException {
    return createStage(loader, dialogFilename, StageStyle.DECORATED);
  }

  protected static Stage createStage(FXMLLoader loader, String dialogFilename, StageStyle stageStyle) throws IOException {
    return createStage(loader, dialogFilename, stageStyle, Modality.NONE);
  }

  protected static Stage createStage(FXMLLoader loader, String dialogFilename, StageStyle stageStyle, Modality modality) throws IOException {
    return createStage(loader, dialogFilename, stageStyle, modality, null, false);
  }

  protected static Stage createStage(FXMLLoader loader, String dialogFilename, StageStyle stageStyle, Modality modality, Window owner, boolean createInEntityDialogFrame) throws IOException {
    loader.setResources(JavaFxLocalization.Resources);
    loader.setLocation(Dialogs.class.getClassLoader().getResource(DialogsBaseFolder + dialogFilename));

    Parent parent = loader.load();
    return createStage(parent, stageStyle, modality, owner, createInEntityDialogFrame, loader);
  }

  protected static Stage createStage(Parent rootNode, StageStyle stageStyle, Modality modality, Window owner) throws IOException {
    return createStage(rootNode, stageStyle, modality, owner, false, null);
  }

  protected static Stage createStage(Parent rootNode, StageStyle stageStyle, Modality modality, Window owner, boolean createInEntityDialogFrame, FXMLLoader loader) throws IOException {
    JavaFxLocalization.resolveResourceKeys(rootNode);

    Stage dialogStage = new Stage();
    if(owner != null)
      dialogStage.initOwner(owner);
    dialogStage.initModality(modality);
    dialogStage.initStyle(stageStyle);

    Scene scene = null;
    if(createInEntityDialogFrame) {
      BorderPane dialogFrame = loadDialogFrame(loader, rootNode);
      scene = new Scene(dialogFrame);
    }
    else
      scene = new Scene(rootNode);

    dialogStage.setScene(scene);
    return dialogStage;
  }

  protected static BorderPane loadDialogFrame(FXMLLoader loader, Parent parent) throws IOException {
    FXMLLoader fxmlLoader = new FXMLLoader(Dialogs.class.getClassLoader().getResource(Dialogs.DialogsBaseFolder + "EntityDialogFrame.fxml"));
    fxmlLoader.setController(loader.getController());
    fxmlLoader.setResources(JavaFxLocalization.Resources);

    BorderPane dialogFrame = fxmlLoader.load();
    JavaFxLocalization.resolveResourceKeys(dialogFrame);
    dialogFrame.setCenter(parent);
    return dialogFrame;
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
