package net.dankito.deepthought.controls.importdata;

import net.dankito.deepthought.data.compare.CompareResult;
import net.dankito.deepthought.data.compare.DataCompareResult;
import net.dankito.deepthought.data.persistence.db.BaseEntity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;

/**
 * Created by ganymed on 10/01/15.
 */
public class ImportDataCompareResultTreeTableCell extends ImportDataTreeTableCell {

  private final static Logger log = LoggerFactory.getLogger(ImportDataCompareResultTreeTableCell.class);


  protected final static Background CompareResultUnchangedCellBackground = new Background(new BackgroundFill(Color.BLUE, new CornerRadii(0), new Insets(0)));
  protected final static Background CompareResultNewerCellBackground = new Background(new BackgroundFill(Color.ORANGE, new CornerRadii(0), new Insets(0)));
  protected final static Background CompareResultOlderCellBackground = new Background(new BackgroundFill(Color.BROWN, new CornerRadii(0), new Insets(0)));
  protected final static Background CompareResultDeletedCellBackground = new Background(new BackgroundFill(Color.RED, new CornerRadii(0), new Insets(0)));
  protected final static Background CompareResultCreatedCellBackground = new Background(new BackgroundFill(Color.GREEN, new CornerRadii(0), new Insets(0)));
  protected final static Background CompareResultNoMatchingEntryFoundCellBackground = new Background(new BackgroundFill(Color.GRAY, new CornerRadii(0), new Insets(0)));


  protected String itemText = null;

  protected DataCompareResult dataCompareResult = null;

  protected boolean isCurrentData;

  protected Background defaultBackground = null;


  public ImportDataCompareResultTreeTableCell(DataCompareResult result, boolean isCurrentData) {
    this.dataCompareResult = result;
    this.isCurrentData = isCurrentData;

    defaultBackground = getBackground();

    setText(null);
    setGraphic(null);

    setAlignment(Pos.CENTER);
    setTextFill(Color.WHITE);
  }


  @Override
  protected void updateItem(String item, boolean empty) {
    super.updateItem(item, empty);

    if(empty) {
      setText(null);
    }
    else {
      setText(itemText);
    }
  }


  @Override
  protected void itemChangedToNull() {
    clearCell();
  }

  @Override
  protected void itemChangedToField(Field field) {
    // nothing to show for a non Entity cell
    clearCell();
  }

  @Override
  protected void itemChangedToEntity(BaseEntity entity) {
    CompareResult compareResult;
    if(isCurrentData == false)
      compareResult = dataCompareResult.getCompareResultForEntity(entity);
    else
      compareResult = dataCompareResult.getCompareResultForCurrentEntity(entity);

    itemText = compareResult.toString();
    setTooltip(new Tooltip(itemText));
    setBackgroundColorForCompareResult(compareResult);

    setText(itemText);

    updateItem(itemText, itemText != null);
  }

  protected void setBackgroundColorForCompareResult(CompareResult compareResult) {
    switch(compareResult) {
      case Unchanged:
        setBackground(CompareResultUnchangedCellBackground);
        break;
      case Newer:
        setBackground(CompareResultNewerCellBackground);
        break;
      case Older:
        setBackground(CompareResultOlderCellBackground);
        break;
      case Created:
        setBackground(CompareResultCreatedCellBackground);
        break;
      case Deleted:
        setBackground(CompareResultDeletedCellBackground);
        break;
      case NoMatchingEntityFound:
        setBackground(CompareResultNoMatchingEntryFoundCellBackground);
        break;
      default:
        setBackground(defaultBackground);
        break;
    }
  }

  protected void clearCell() {
    itemText = "";
    setBackground(defaultBackground);
  }

}
