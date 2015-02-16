package net.deepthought.data.compare;

import net.deepthought.util.Localization;

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
        return Localization.getLocalizedStringForResourceKey("compare.result.unchanged");
      case NoMatchingEntityFound:
        return Localization.getLocalizedStringForResourceKey("compare.result.no.matching.entity.found");
      case Newer:
        return Localization.getLocalizedStringForResourceKey("compare.result.newer");
      case Older:
        return Localization.getLocalizedStringForResourceKey("compare.result.older");
      case Created:
        return Localization.getLocalizedStringForResourceKey("compare.result.created");
      case Deleted:
        return Localization.getLocalizedStringForResourceKey("compare.result.deleted");
      default:
        return Localization.getLocalizedStringForResourceKey("compare.result.unknown");
    }
  }
}
