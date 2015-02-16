package net.deepthought.data.model.enums;

import net.deepthought.data.model.SeriesTitle;
import net.deepthought.data.persistence.db.TableConfig;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;

/**
 * Created by ganymed on 21/01/15.
 */
@Entity(name = TableConfig.SeriesTitleCategoryTableName)
public class SeriesTitleCategory extends ExtensibleEnumeration {

  private static final long serialVersionUID = -202919015655774272L;


  @OneToMany(fetch = FetchType.LAZY, mappedBy = "category")
  protected Set<SeriesTitle> series = new HashSet<>();


  public SeriesTitleCategory() {

  }

  public SeriesTitleCategory(String name) {
    super(name);
  }

  public SeriesTitleCategory(String nameResourceKey, boolean isSystemValue, boolean isDeletable, int sortOrder) {
    super(nameResourceKey, isSystemValue, isDeletable, sortOrder);
  }


  public Set<SeriesTitle> getSeries() {
    return series;
  }

  public boolean addSeries(SeriesTitle series) {
    if(this.series.add(series)) {
      callEntityAddedListeners(this.series, series);
      return true;
    }

    return false;
  }

  public boolean removeSeries(SeriesTitle series) {
    if(this.series.remove(series)) {
      callEntityRemovedListeners(this.series, series);
      return true;
    }

    return false;
  }


  @Override
  public String toString() {
    return "SeriesTitleCategory " + getTextRepresentation();
  }

}
