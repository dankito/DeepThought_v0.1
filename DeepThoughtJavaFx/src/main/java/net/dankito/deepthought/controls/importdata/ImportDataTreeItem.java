package net.dankito.deepthought.controls.importdata;

import net.dankito.deepthought.data.persistence.db.BaseEntity;
import net.dankito.deepthought.util.ReflectionHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;

/**
 * Created by ganymed on 10/01/15.
 */
public class ImportDataTreeItem extends TreeItem<BaseEntity> {

  private final static Logger log = LoggerFactory.getLogger(ImportDataTreeItem.class);


  protected static Map<Class<? extends BaseEntity>, List<Class<? extends BaseEntity>>> mapManyToManyCollectionsShown = new HashMap<>();

  protected static Map<BaseEntity, List<Class<? extends BaseEntity>>> mapShownCollectionsForEntity = new HashMap<>();


  protected BaseEntity datum;

  protected List<Field> collectionFields = new ArrayList<>();

  protected boolean childrenHaveBeenLoaded = false;


  public ImportDataTreeItem(BaseEntity datum) {
    super(datum);
    this.datum = datum;

    collectionFields = ReflectionHelper.findCollectionsChildrenFields(datum.getClass());
  }

  @Override
  public boolean isLeaf() {
    return collectionFields.size() == 0;
  }

  @Override
  public ObservableList<TreeItem<BaseEntity>> getChildren() {
    if(childrenHaveBeenLoaded == false) {
      childrenHaveBeenLoaded = true;
      createChildren(collectionFields);
    }

    return super.getChildren();
  }

  protected void createChildren(List<Field> collectionFields) {
    for(Field collectionField : collectionFields) {
      addCollectionChild(collectionField);
    }
  }

  protected void addCollectionChild(Field collectionChild) {
    try {
      Collection collection = ReflectionHelper.getCollectionFieldValue(datum, collectionChild);

      getChildren().add(new ImportDataCollectionChildTreeItem(collection, collectionChild));
    } catch(Exception ex) {
      log.error("Could not get Collection from Entity " + datum + " field " + collectionChild);
    }
  }
}
