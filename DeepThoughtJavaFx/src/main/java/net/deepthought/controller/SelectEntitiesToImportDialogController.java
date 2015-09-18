package net.deepthought.controller;

import net.deepthought.Application;
import net.deepthought.controller.enums.DialogResult;
import net.deepthought.controls.importdata.ImportDataCompareResultTreeTableCell;
import net.deepthought.controls.importdata.ImportDataEntityTreeTableCell;
import net.deepthought.controls.importdata.ImportDataTreeItem;
import net.deepthought.data.compare.DataCompareResult;
import net.deepthought.data.listener.ApplicationListener;
import net.deepthought.data.model.DeepThought;
import net.deepthought.data.persistence.db.BaseEntity;
import net.deepthought.util.JavaFxLocalization;
import net.deepthought.util.Notification;
import net.deepthought.util.ReflectionHelper;

import org.controlsfx.control.PropertySheet;
import org.controlsfx.property.BeanProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.util.Callback;

/**
 * Created by ganymed on 10/01/15.
 */
public class SelectEntitiesToImportDialogController extends ChildWindowsController implements Initializable {

  private final static Logger log = LoggerFactory.getLogger(SelectEntitiesToImportDialogController.class);


  protected BaseEntity parentDataEntityToImport;

  protected DataCompareResult dataCompareResult;

  protected List<BaseEntity> selectedEntitiesToRestore = new ArrayList<>();


  @FXML
  protected TreeTableView<BaseEntity> trtblvwDataToImportEntities;
  @FXML
  protected TreeTableColumn clmDataToImportEntity;
  @FXML
  protected TreeTableColumn clmDataToImportState;

  @FXML
  protected BorderPane brdpnDataToImportSelectedEntityProperties;
  @FXML
  protected Label lblDataToImportSelectedEntityName;

  protected PropertySheet dataToImportSelectedEntityPropertySheet;

  @FXML
  protected TreeTableView<BaseEntity> trtblvwCurrentDataEntities;
  @FXML
  protected TreeTableColumn clmCurrentDataEntity;
  @FXML
  protected TreeTableColumn clmCurrentDataState;

  @FXML
  protected BorderPane brdpnCurrentDataSelectedEntityProperties;
  @FXML
  protected Label lblCurrentDataSelectedEntityName;

  protected PropertySheet currentDataSelectedEntityPropertySheet;

  @FXML
  protected Button btnOk;


  @Override
  public void initialize(URL location, ResourceBundle resources) {
    setupControls();

    deepThoughtChanged(Application.getDeepThought());

    Application.addApplicationListener(new ApplicationListener() {
      @Override
      public void deepThoughtChanged(final DeepThought deepThought) {
        Platform.runLater(new Runnable() {
          @Override
          public void run() {
            SelectEntitiesToImportDialogController.this.deepThoughtChanged(deepThought);
          }
        });
      }

      @Override
      public void notification(Notification notification) {

      }
    });
  }

  protected void deepThoughtChanged(DeepThought deepThought) {
    if(deepThought == null)
      trtblvwCurrentDataEntities.setRoot(null);
    else {
      trtblvwCurrentDataEntities.setRoot(new ImportDataTreeItem(deepThought));
      trtblvwCurrentDataEntities.getRoot().setExpanded(true);
    }
  }

  private void setupControls() {
    clmDataToImportEntity.setCellFactory(new Callback<TreeTableColumn, TreeTableCell>() {
      @Override
      public TreeTableCell call(TreeTableColumn param) {
        return new ImportDataEntityTreeTableCell();
      }
    });

    clmDataToImportState.setCellFactory(new Callback<TreeTableColumn, TreeTableCell>() {
      @Override
      public TreeTableCell call(TreeTableColumn param) {
        return new ImportDataCompareResultTreeTableCell(dataCompareResult, false);
      }
    });

    trtblvwDataToImportEntities.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TreeItem<BaseEntity>>() {
      @Override
      public void changed(ObservableValue<? extends TreeItem<BaseEntity>> observable, TreeItem<BaseEntity> oldValue, TreeItem<BaseEntity> newValue) {
        if(newValue != null && newValue.getValue() instanceof BaseEntity)
          showDataToImportSelectedEntityProperties(newValue.getValue());
        else
          clearDataToImportSelectedEntityProperties();
      }
    });

    dataToImportSelectedEntityPropertySheet = new PropertySheet();
    brdpnDataToImportSelectedEntityProperties.setCenter(dataToImportSelectedEntityPropertySheet);


    clmCurrentDataEntity.setCellFactory(new Callback<TreeTableColumn, TreeTableCell>() {
      @Override
      public TreeTableCell call(TreeTableColumn param) {
        return new ImportDataEntityTreeTableCell();
      }
    });

    clmCurrentDataState.setCellFactory(new Callback<TreeTableColumn, TreeTableCell>() {
      @Override
      public TreeTableCell call(TreeTableColumn param) {
        return new ImportDataCompareResultTreeTableCell(dataCompareResult, true);
      }
    });

    trtblvwCurrentDataEntities.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TreeItem<BaseEntity>>() {
      @Override
      public void changed(ObservableValue<? extends TreeItem<BaseEntity>> observable, TreeItem<BaseEntity> oldValue, TreeItem<BaseEntity> newValue) {
        if(newValue != null && newValue.getValue() instanceof BaseEntity)
          showCurrentDataSelectedEntityProperties(newValue.getValue());
        else
          clearCurrentDataSelectedEntityProperties();
      }
    });

    currentDataSelectedEntityPropertySheet = new PropertySheet();
    brdpnCurrentDataSelectedEntityProperties.setCenter(currentDataSelectedEntityPropertySheet);
  }

  protected void clearDataToImportSelectedEntityProperties() {
    lblDataToImportSelectedEntityName.setText("");

    ObservableList<PropertySheet.Item> propertyItems = dataToImportSelectedEntityPropertySheet.getItems();

    propertyItems.removeAll(propertyItems);
  }

  protected void showDataToImportSelectedEntityProperties(BaseEntity datumToImport) {
    clearDataToImportSelectedEntityProperties();

    lblDataToImportSelectedEntityName.setText(datumToImport.getClass().getSimpleName());

    showSelectedEntityProperties(datumToImport, dataToImportSelectedEntityPropertySheet);
  }

  protected void clearCurrentDataSelectedEntityProperties() {
    lblCurrentDataSelectedEntityName.setText("");

    ObservableList<PropertySheet.Item> propertyItems = currentDataSelectedEntityPropertySheet.getItems();

    propertyItems.removeAll(propertyItems);
  }

  protected void showCurrentDataSelectedEntityProperties(BaseEntity currentData) {
    clearCurrentDataSelectedEntityProperties();

    lblCurrentDataSelectedEntityName.setText(currentData.getClass().getSimpleName());

    showSelectedEntityProperties(currentData, currentDataSelectedEntityPropertySheet);
  }

  protected void showSelectedEntityProperties(BaseEntity entity, PropertySheet propertySheet) {
    ObservableList<PropertySheet.Item> propertyItems = propertySheet.getItems();

    List<Field> propertyFields = ReflectionHelper.findEntityProperties(entity.getClass());
    for(Field propertyField : propertyFields) {
      try {
        Method getMethod = ReflectionHelper.findPropertyGetMethod(entity, propertyField);
        if(getMethod != null)
          propertyItems.add(new BeanProperty(entity, new PropertyDescriptor(ReflectionHelper.getFieldName(propertyField), getMethod, null)));
        else
          propertyItems.add(new BeanProperty(entity, new PropertyDescriptor(ReflectionHelper.getFieldName(propertyField), propertyField.getType())));
      } catch(Exception ex) {
//        log.error("Could not add Property " + propertyField + " to PropertySheet for entity " + entity, ex);
      }
    }
  }


  @FXML
  public void handleButtonCancelAction(ActionEvent event) {
    closeDialog(DialogResult.Cancel);
  }

  @FXML
  public void handleButtonOkAction(ActionEvent event) {
    // TODO: set really selected data
    selectedEntitiesToRestore.add(parentDataEntityToImport);
    closeDialog(DialogResult.Ok);
  }


  public void setDataToImport(BaseEntity parentDataEntityToImport, Stage dialogStage) {
    setWindowStage(dialogStage);
    this.parentDataEntityToImport = parentDataEntityToImport;

    JavaFxLocalization.bindStageTitle(dialogStage, "select.entities.to.import.dialog.title", parentDataEntityToImport);

    trtblvwDataToImportEntities.setRoot(new ImportDataTreeItem(parentDataEntityToImport));
    trtblvwDataToImportEntities.getRoot().setExpanded(true);

    dataCompareResult = Application.getDataComparer().compareDataToCurrent(parentDataEntityToImport);
  }


  public List<BaseEntity> getSelectedEntitiesToRestore() {
    return selectedEntitiesToRestore;
  }
}
