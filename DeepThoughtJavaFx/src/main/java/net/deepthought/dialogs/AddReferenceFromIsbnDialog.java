package net.deepthought.dialogs;

import net.deepthought.Application;
import net.deepthought.controller.ChildWindowsController;
import net.deepthought.controller.ChildWindowsControllerListener;
import net.deepthought.controller.Dialogs;
import net.deepthought.controller.enums.DialogResult;
import net.deepthought.data.model.DeepThought;
import net.deepthought.data.model.FileLink;
import net.deepthought.data.model.Person;
import net.deepthought.data.model.Reference;
import net.deepthought.data.model.ReferenceBase;
import net.deepthought.data.model.ReferenceSubDivision;
import net.deepthought.data.model.SeriesTitle;
import net.deepthought.util.Localization;
import net.deepthought.util.isbn.IsbnResolvingListener;
import net.deepthought.util.isbn.ResolveIsbnResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javafx.application.Platform;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * Created by ganymed on 07/12/15.
 */
public class AddReferenceFromIsbnDialog {

  protected IsbnResolvingListener listener = null;


  public AddReferenceFromIsbnDialog() {

  }


  public void showAsync(Window owner, IsbnResolvingListener listener) {
    this.listener = listener;
    showEnterIsbnDialog(owner, null, null);
  }

  protected void showEnterIsbnDialog(Window owner, final String lastEnteredIsbn, final String lastEnteredIsbnErrorText) {
    if(Platform.isFxApplicationThread())
      showEnterIsbnDialogOnUiThread(owner, lastEnteredIsbn, lastEnteredIsbnErrorText);
    else
      Platform.runLater(() -> showEnterIsbnDialogOnUiThread(owner, lastEnteredIsbn, lastEnteredIsbnErrorText));
  }

  protected void showEnterIsbnDialogOnUiThread(Window owner, String lastEnteredIsbn, String lastEnteredIsbnErrorText) {
    TextInputDialog dialog = new TextInputDialog(lastEnteredIsbn);
    dialog.initOwner(owner);
    dialog.setHeaderText(lastEnteredIsbnErrorText);
    dialog.setTitle(Localization.getLocalizedString("enter.isbn.dialog.title"));
    dialog.setContentText(Localization.getLocalizedString("enter.isbn"));

    waitForAndHandleUserIsbnInput(dialog);
  }

  protected void waitForAndHandleUserIsbnInput(TextInputDialog dialog) {
    Optional<String> result = dialog.showAndWait();

    if (result.isPresent()){
      getReferenceForIsbn(result.get(), dialog);
    }
    else if(listener != null) {
      listener.isbnResolvingDone(new ResolveIsbnResult(false));
    }
  }

  protected void getReferenceForIsbn(final String enteredIsbn, final TextInputDialog askForIsbnDialog) {
    Application.getIsbnResolver().resolveIsbnAsync(enteredIsbn, new IsbnResolvingListener() {
      @Override
      public void isbnResolvingDone(ResolveIsbnResult result) {
        if (result.isSuccessful()) {
          showEditReferenceDialog(result);
        } else {
          showEnterIsbnDialog(askForIsbnDialog.getOwner(), enteredIsbn, Localization.getLocalizedString("could.not.resolve.isbn", enteredIsbn));
        }
      }
    });
  }

  protected void showEditReferenceDialog(ResolveIsbnResult result) {
    Dialogs.showEditReferenceDialog(result.getResolvedReference(), new ChildWindowsControllerListener() {
      @Override
      public void windowClosing(Stage stage, ChildWindowsController controller) {
        mayPersistResolvedReferenceAndDispatchResult(controller, result);
      }

      @Override
      public void windowClosed(Stage stage, ChildWindowsController controller) {

      }
    });
  }

  protected void mayPersistResolvedReferenceAndDispatchResult(ChildWindowsController controller, ResolveIsbnResult result) {
    if(controller.getDialogResult() == DialogResult.Ok) {
      persistResolvedReference(result.getResolvedReference());
    }

    dispatchResult(result, controller);
  }

  protected void persistResolvedReference(ReferenceBase referenceBase) {
    if(referenceBase != null && referenceBase.isPersisted() == false) {
      if(referenceBase instanceof SeriesTitle) {
        persistSeriesTitle((SeriesTitle)referenceBase);
      }
      else if(referenceBase instanceof Reference) {
        persistReference((Reference)referenceBase);
      }
      else if(referenceBase instanceof ReferenceSubDivision) {
        persistReferenceSubDivision((ReferenceSubDivision)referenceBase);
      }
    }
  }

  protected void dispatchResult(ResolveIsbnResult result, ChildWindowsController controller) {
    if(listener != null) {
      if(controller.getDialogResult() == DialogResult.Ok) {
        listener.isbnResolvingDone(result);
      }
      else {
        listener.isbnResolvingDone(new ResolveIsbnResult(false));
      }
    }
  }


  // TODO: this code is duplicated from EditEntryDialogController -> find a common place

  protected void persistSeriesTitle(SeriesTitle seriesTitle) {
    DeepThought deepThought = Application.getDeepThought();

    mayPersistReferenceBaseRelations(seriesTitle, deepThought);

    deepThought.addSeriesTitle(seriesTitle);
  }

  protected void persistReference(Reference reference) {
    DeepThought deepThought = Application.getDeepThought();

    mayPersistReferenceBaseRelations(reference, deepThought);

    deepThought.addReference(reference);
  }

  protected void persistReferenceSubDivision(ReferenceSubDivision subDivision) {
    DeepThought deepThought = Application.getDeepThought();

    mayPersistReferenceBaseRelations(subDivision, deepThought);

    deepThought.addReferenceSubDivision(subDivision);
  }

  protected void mayPersistReferenceBaseRelations(ReferenceBase referenceBase, DeepThought deepThought) {
    mayPersistFiles(deepThought, referenceBase);
    mayPersistPersons(deepThought, referenceBase);
    mayPersistPreviewImage(deepThought, referenceBase);
  }

  protected void mayPersistPersons(DeepThought deepThought, ReferenceBase referenceBase) {
    List<Person> unpersistedPersons = new ArrayList<>();
    List<Person> referenceBasePersons = new ArrayList<>(referenceBase.getPersons()); // make a copy as Collection may gets changed

    for(Person person : referenceBasePersons) {
      if(person.isPersisted() == false) {
        unpersistedPersons.add(person);
        referenceBase.removePerson(person);
      }
    }

    for(Person unpersistedPerson : unpersistedPersons) {
      deepThought.addPerson(unpersistedPerson);
      referenceBase.addPerson(unpersistedPerson); // TODO: how to keep correct order of Persons?
    }
  }

  protected void mayPersistFiles(DeepThought deepThought, ReferenceBase referenceBase) {
    mayPersistAttachedFiles(deepThought, referenceBase);
    mayPersistEmbeddedFiles(deepThought, referenceBase);
  }

  protected void mayPersistAttachedFiles(DeepThought deepThought, ReferenceBase referenceBase) {
    List<FileLink> unpersistedFiles = new ArrayList<>();
    List<FileLink> referenceBaseAttachedFiles = new ArrayList<>(referenceBase.getAttachedFiles()); // make a copy as Collection may gets changed

    for(FileLink file : referenceBaseAttachedFiles) {
      if(file.isPersisted() == false) {
        unpersistedFiles.add(file);
        referenceBase.removeAttachedFile(file);
      }
    }

    for(FileLink unpersistedFile : unpersistedFiles) {
      deepThought.addFile(unpersistedFile);
      referenceBase.addAttachedFile(unpersistedFile); // TODO: how to keep correct order of Files?
    }
  }

  protected void mayPersistEmbeddedFiles(DeepThought deepThought, ReferenceBase referenceBase) {
    List<FileLink> unpersistedFiles = new ArrayList<>();
    List<FileLink> referenceBaseEmbeddedFiles = new ArrayList<>(referenceBase.getEmbeddedFiles()); // make a copy as Collection may gets changed

    for(FileLink file : referenceBaseEmbeddedFiles) {
      if(file.isPersisted() == false) {
        unpersistedFiles.add(file);
        referenceBase.removeEmbeddedFile(file);
      }
    }

    for(FileLink unpersistedFile : unpersistedFiles) {
      deepThought.addFile(unpersistedFile);
      referenceBase.addEmbeddedFile(unpersistedFile); // TODO: how to keep correct order of Files?
    }
  }

  protected void mayPersistPreviewImage(DeepThought deepThought, ReferenceBase referenceBase) {
    FileLink previewImage = referenceBase.getPreviewImage();
    if(previewImage != null && previewImage.isPersisted() == false) {
      deepThought.addFile(previewImage);
    }
  }

}
