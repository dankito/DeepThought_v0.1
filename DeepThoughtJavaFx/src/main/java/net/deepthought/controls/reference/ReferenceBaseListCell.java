package net.deepthought.controls.reference;

import net.deepthought.controller.ChildWindowsController;
import net.deepthought.controller.ChildWindowsControllerListener;
import net.deepthought.controller.Dialogs;
import net.deepthought.controller.enums.DialogResult;
import net.deepthought.controls.Constants;
import net.deepthought.controls.ICleanableControl;
import net.deepthought.data.model.Reference;
import net.deepthought.data.model.ReferenceBase;
import net.deepthought.data.model.ReferenceSubDivision;
import net.deepthought.data.model.SeriesTitle;
import net.deepthought.data.model.listener.EntityListener;
import net.deepthought.data.persistence.db.BaseEntity;
import net.deepthought.util.JavaFxLocalization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

/**
 * Created by ganymed on 27/12/14.
 */
public class ReferenceBaseListCell extends ListCell<ReferenceBase> implements ICleanableControl {

  private final static Logger log = LoggerFactory.getLogger(ReferenceBaseListCell.class);


  protected ReferenceBase referenceBase = null;

  protected ISelectedReferenceHolder selectedReferenceHolder;

  protected HBox graphicPane = new HBox();

  protected Label referencePreviewLabel = new Label();

  protected Button btnAddReferenceOrReferenceSubDivisionToReferenceBase = new Button();
  protected Button btnAddOrRemoveReferenceBaseFromEntity = new Button();
  protected Button btnEditReferenceBase = new Button();
  protected Button btnDeleteReferenceBase = new Button();


  public ReferenceBaseListCell(ISelectedReferenceHolder selectedReferenceHolder) {
    this.selectedReferenceHolder = selectedReferenceHolder;
    selectedReferenceHolder.addFieldChangedEvent(event -> setButtonAddOrRemoveReferenceBaseState());

    setText(null);
    setupGraphic();

    // bind ListView Item's width to ListView's width
    listViewProperty().addListener((observable, oldValue, newValue) -> prefWidthProperty().bind(newValue.widthProperty().subtract(15)));

    itemProperty().addListener(new ChangeListener<ReferenceBase>() {
      @Override
      public void changed(ObservableValue<? extends ReferenceBase> observable, ReferenceBase oldValue, ReferenceBase newValue) {
        itemChanged(newValue);
      }
    });

    setOnMouseClicked(event -> mouseClicked(event));
  }

  @Override
  public void cleanUpControl() {
    if(getItem() != null)
      getItem().removeEntityListener(referenceBaseListener);
    if(referenceBase != null)
      referenceBase.removeEntityListener(referenceBaseListener);

    selectedReferenceHolder = null;
  }

  protected void setupGraphic() {
    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
    setAlignment(Pos.CENTER_LEFT);

    graphicPane.setAlignment(Pos.CENTER_LEFT);

    HBox.setHgrow(referencePreviewLabel, Priority.ALWAYS);
    HBox.setMargin(referencePreviewLabel, new Insets(0, 6, 0, 0));

//    referencePreviewLabel.setTextOverrun(OverrunStyle.CENTER_ELLIPSIS);
    referencePreviewLabel.setMaxWidth(Double.MAX_VALUE);
    graphicPane.getChildren().add(referencePreviewLabel);

    btnAddReferenceOrReferenceSubDivisionToReferenceBase.setMinWidth(100);
    HBox.setMargin(btnAddReferenceOrReferenceSubDivisionToReferenceBase, new Insets(0, 6, 0, 0));
    graphicPane.getChildren().add(btnAddReferenceOrReferenceSubDivisionToReferenceBase);

    btnAddReferenceOrReferenceSubDivisionToReferenceBase.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent event) {
        selectCurrentCell();
        handleButtonAddReferenceOrReferenceSubDivisionToReferenceBaseAction();
      }
    });

    JavaFxLocalization.bindLabeledText(btnAddOrRemoveReferenceBaseFromEntity, "add");
    btnAddOrRemoveReferenceBaseFromEntity.setMinWidth(100);
    HBox.setMargin(btnAddOrRemoveReferenceBaseFromEntity, new Insets(0, 6, 0, 0));
    graphicPane.getChildren().add(btnAddOrRemoveReferenceBaseFromEntity);

    btnAddOrRemoveReferenceBaseFromEntity.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent event) {
        selectCurrentCell();
        handleButtonAddOrRemoveReferenceBaseAction();
      }
    });

    JavaFxLocalization.bindLabeledText(btnEditReferenceBase, "edit");
    btnEditReferenceBase.setMinWidth(100);
    graphicPane.getChildren().add(btnEditReferenceBase);
    btnEditReferenceBase.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent event) {
        selectCurrentCell();
        handleButtonEditReferenceBaseAction();
      }
    });

    btnDeleteReferenceBase.setText("-");
    btnDeleteReferenceBase.setTextFill(Constants.RemoveEntityButtonTextColor);
    btnDeleteReferenceBase.setFont(new Font(15));
    HBox.setMargin(btnDeleteReferenceBase, new Insets(0, 0, 0, 6));
    graphicPane.getChildren().add(btnDeleteReferenceBase);
    btnDeleteReferenceBase.setOnAction((event) -> handleButtonDeleteReferenceBaseAction(event));
    btnDeleteReferenceBase.setDisable(true); // TODO: enable again as soon as deleting is possible
  }


  @Override
  protected void updateItem(ReferenceBase item, boolean empty) {
    super.updateItem(item, empty);

    if(empty || item == null) {
      setGraphic(null);
    }
    else {
      setGraphic(graphicPane);
      referencePreviewLabel.setText(item.getTextRepresentation());
      setButtonAddOrRemoveReferenceBaseState();
      setButtonAddReferenceOrReferenceSubDivisionToReferenceBaseState();
    }
  }

  protected void setButtonAddOrRemoveReferenceBaseState() {
    btnAddOrRemoveReferenceBaseFromEntity.setVisible(getItem() != null);

    if(getItem() != null) {
      if(isReferenceBaseSetOnEntity(getItem()) == false)
        JavaFxLocalization.bindLabeledText(btnAddOrRemoveReferenceBaseFromEntity, "add");
      else
        JavaFxLocalization.bindLabeledText(btnAddOrRemoveReferenceBaseFromEntity, "remove");
    }
  }

  protected void setButtonAddReferenceOrReferenceSubDivisionToReferenceBaseState() {
    btnAddReferenceOrReferenceSubDivisionToReferenceBase.setVisible(getItem() != null);

    if(getItem() != null) {
      if(getItem() instanceof SeriesTitle)
        JavaFxLocalization.bindLabeledText(btnAddReferenceOrReferenceSubDivisionToReferenceBase, "series.title.add.reference...");
      else if(getItem() instanceof Reference)
        JavaFxLocalization.bindLabeledText(btnAddReferenceOrReferenceSubDivisionToReferenceBase, "reference.add.reference.sub.division...");
      else
        btnAddReferenceOrReferenceSubDivisionToReferenceBase.setVisible(false);
    }
  }

  protected boolean isReferenceBaseSetOnEntity(ReferenceBase referenceBase) {
    return referenceBase.equals(selectedReferenceHolder.getSelectedReferenceBase());
  }


  protected void mouseClicked(MouseEvent event) {
    if(event.getClickCount() == 2 && event.getButton() == MouseButton.PRIMARY) {
      if(getItem() != null) {
        addOrRemoveReferenceBase();
      }
    }
  }


  protected void selectCurrentCell() {
    getListView().getSelectionModel().select(getIndex());
  }

  protected void itemChanged(ReferenceBase newValue) {
    if(referenceBase != null)
      referenceBase.removeEntityListener(referenceBaseListener);

    referenceBase = newValue;

    if(newValue != null) {
      newValue.addEntityListener(referenceBaseListener);
    }

    updateItem(newValue, newValue == null);
  }


  protected void handleButtonAddReferenceOrReferenceSubDivisionToReferenceBaseAction() {
    if(getItem() instanceof SeriesTitle) {
      final SeriesTitle seriesTitle = (SeriesTitle)getItem();
      final Reference newReference = new Reference();
//      seriesTitle.addSerialPart(newReference);

      Dialogs.showEditReferenceDialog(newReference, seriesTitle, new ChildWindowsControllerListener() {
        @Override
        public void windowClosing(Stage stage, ChildWindowsController controller) {
          if(controller.getDialogResult() == DialogResult.Ok)
            addReferenceBaseToEntry(newReference);
//          else
//            seriesTitle.removeSerialPart(newReference);
        }

        @Override
        public void windowClosed(Stage stage, ChildWindowsController controller) {

        }
      });
    }
    else if(getItem() instanceof Reference) {
      final Reference reference = (Reference)getItem();
      final ReferenceSubDivision newSubDivision = new ReferenceSubDivision();
//      reference.addSubDivision(newSubDivision);

      Dialogs.showEditReferenceDialog(newSubDivision, reference, new ChildWindowsControllerListener() {
        @Override
        public void windowClosing(Stage stage, ChildWindowsController controller) {
          if(controller.getDialogResult() == DialogResult.Ok)
            addReferenceBaseToEntry(newSubDivision);
//          else
//            reference.removeSubDivision(newSubDivision);
        }

        @Override
        public void windowClosed(Stage stage, ChildWindowsController controller) {

        }
      });
    }
  }

  protected void handleButtonAddOrRemoveReferenceBaseAction() {
    addOrRemoveReferenceBase();
  }

  protected void addOrRemoveReferenceBase() {
    if(isReferenceBaseSetOnEntity(getItem()) == false)
      addReferenceBaseToEntry(getItem());
    else
      removeReferenceBaseFromEntry(getItem());
  }

  protected void addReferenceBaseToEntry(ReferenceBase referenceBase) {
    selectedReferenceHolder.selectedReferenceBaseChanged(referenceBase);
    setButtonAddOrRemoveReferenceBaseState();
  }

  protected void removeReferenceBaseFromEntry(ReferenceBase referenceBase) {
    selectedReferenceHolder.selectedReferenceBaseChanged(null);
    setButtonAddOrRemoveReferenceBaseState();
  }

  protected void handleButtonEditReferenceBaseAction() {
    Dialogs.showEditReferenceDialog(getItem());
  }

  protected void handleButtonDeleteReferenceBaseAction(ActionEvent event) {
    // TODO: implement Alert to delete ReferenceBase
    //Alerts.deletePersonWithUserConfirmationIfIsSetOnEntries(getItem());
  }



  protected EntityListener referenceBaseListener = new EntityListener() {
    @Override
    public void propertyChanged(BaseEntity entity, String propertyName, Object previousValue, Object newValue) {
      if(entity == getItem())
        itemChanged((ReferenceBase)entity);
    }

    @Override
    public void entityAddedToCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity addedEntity) {

    }

    @Override
    public void entityOfCollectionUpdated(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity updatedEntity) {

    }

    @Override
    public void entityRemovedFromCollection(BaseEntity collectionHolder, Collection<? extends BaseEntity> collection, BaseEntity removedEntity) {

    }
  };

}
