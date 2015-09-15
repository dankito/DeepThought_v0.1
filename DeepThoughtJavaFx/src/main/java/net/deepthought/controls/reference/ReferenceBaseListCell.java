package net.deepthought.controls.reference;

import net.deepthought.controller.ChildWindowsController;
import net.deepthought.controller.ChildWindowsControllerListener;
import net.deepthought.controller.Dialogs;
import net.deepthought.controller.enums.DialogResult;
import net.deepthought.controls.FXUtils;
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
import javafx.scene.control.OverrunStyle;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
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
  protected Button btnEditReferenceBase = new Button();


  public ReferenceBaseListCell(ISelectedReferenceHolder selectedReferenceHolder) {
    this.selectedReferenceHolder = selectedReferenceHolder;

    setupGraphic();

    // bind ListView Item's width to ListView's width
    listViewProperty().addListener((observable, oldValue, newValue) -> graphicPane.maxWidthProperty().bind(newValue.widthProperty().subtract(35)));

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
    setText(null);
    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
    setAlignment(Pos.CENTER_LEFT);

    graphicPane.setAlignment(Pos.CENTER_LEFT);

    referencePreviewLabel.setTextOverrun(OverrunStyle.ELLIPSIS);
    referencePreviewLabel.setMaxWidth(Double.MAX_VALUE);
    graphicPane.getChildren().add(referencePreviewLabel);
    HBox.setHgrow(referencePreviewLabel, Priority.ALWAYS);
    HBox.setMargin(referencePreviewLabel, new Insets(0, 6, 0, 0));

    FXUtils.ensureNodeOnlyUsesSpaceIfVisible(btnAddReferenceOrReferenceSubDivisionToReferenceBase);
    btnAddReferenceOrReferenceSubDivisionToReferenceBase.setMinWidth(100);
    btnAddReferenceOrReferenceSubDivisionToReferenceBase.setPrefWidth(Region.USE_COMPUTED_SIZE);
    btnAddReferenceOrReferenceSubDivisionToReferenceBase.setMaxWidth(Double.MAX_VALUE);
    graphicPane.getChildren().add(btnAddReferenceOrReferenceSubDivisionToReferenceBase);
    HBox.setMargin(btnAddReferenceOrReferenceSubDivisionToReferenceBase, new Insets(0, 6, 0, 0));

    btnAddReferenceOrReferenceSubDivisionToReferenceBase.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent event) {
        selectCurrentCell();
        handleButtonAddReferenceOrReferenceSubDivisionToReferenceBaseAction();
      }
    });

    JavaFxLocalization.bindLabeledText(btnEditReferenceBase, "edit");
    btnEditReferenceBase.setMinWidth(100);
    graphicPane.getChildren().add(btnEditReferenceBase);
    btnEditReferenceBase.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent event) {
        selectCurrentCell();
        editReferenceBase();
      }
    });
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
      setButtonAddReferenceOrReferenceSubDivisionState();
    }
  }

  protected void setButtonAddReferenceOrReferenceSubDivisionState() {
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
      addNewReferenceToSeriesTitle((SeriesTitle)getItem());
    }
    else if(getItem() instanceof Reference) {
      addNewSubDivisionToReference((Reference)getItem());
    }
  }

  protected void addNewReferenceToSeriesTitle(final SeriesTitle seriesTitle) {
    final Reference newReference = new Reference();

    Dialogs.showEditReferenceDialog(newReference, seriesTitle, new ChildWindowsControllerListener() {
      @Override
      public void windowClosing(Stage stage, ChildWindowsController controller) {
        if (controller.getDialogResult() == DialogResult.Ok)
          selectReferenceBaseOnReferenceBaseHolder(newReference);
      }

      @Override
      public void windowClosed(Stage stage, ChildWindowsController controller) {

      }
    });
  }

  protected void addNewSubDivisionToReference(final Reference reference) {
    final ReferenceSubDivision newSubDivision = new ReferenceSubDivision();

    Dialogs.showEditReferenceDialog(newSubDivision, reference, new ChildWindowsControllerListener() {
      @Override
      public void windowClosing(Stage stage, ChildWindowsController controller) {
        if (controller.getDialogResult() == DialogResult.Ok)
          selectReferenceBaseOnReferenceBaseHolder(newSubDivision);
      }

      @Override
      public void windowClosed(Stage stage, ChildWindowsController controller) {

      }
    });
  }

  protected void addOrRemoveReferenceBase() {
    if(isReferenceBaseSetOnEntity(getItem()) == false)
      selectReferenceBaseOnReferenceBaseHolder(getItem());
    else
      deselectReferenceBaseOnReferenceBaseHolder(getItem());
  }

  protected void selectReferenceBaseOnReferenceBaseHolder(ReferenceBase referenceBase) {
    selectedReferenceHolder.selectedReferenceBaseChanged(referenceBase);
  }

  protected void deselectReferenceBaseOnReferenceBaseHolder(ReferenceBase referenceBase) {
    selectedReferenceHolder.selectedReferenceBaseChanged(null);
  }

  protected void editReferenceBase() {
    Dialogs.showEditReferenceDialog(getItem());
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
