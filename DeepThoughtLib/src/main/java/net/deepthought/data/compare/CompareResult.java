package net.deepthought.data.compare;

import net.deepthought.util.localization.Localization;

/**
 * Created by ganymed on 10/01/15.
 */
public enum CompareResult {

  Unchanged,
  NoMatchingEntityFound,
  Newer,
  Older,
  Created,
  Deleted,
  Unknown;


  @Override
  public String toString() {
    switch(this) {
      case Unchanged:
        return Localization.getLocalizedString("compare.result.unchanged");
      case NoMatchingEntityFound:
        return Localization.getLocalizedString("compare.result.no.matching.entity.found");
      case Newer:
        return Localization.getLocalizedString("compare.result.newer");
      case Older:
        return Localization.getLocalizedString("compare.result.older");
      case Created:
        return Localization.getLocalizedString("compare.result.created");
      case Deleted:
        return Localization.getLocalizedString("compare.result.deleted");
      default:
        return Localization.getLocalizedString("compare.result.unknown");
    }
  }
}
