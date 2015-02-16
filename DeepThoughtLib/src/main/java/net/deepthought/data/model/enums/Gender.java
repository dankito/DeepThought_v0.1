package net.deepthought.data.model.enums;

import net.deepthought.util.Localization;

/**
 * Created by ganymed on 31/12/14.
 */
public enum Gender {

  Unset,
  Female,
  Male;


  @Override
  public String toString() {
    switch(this) {
      case Female:
        return Localization.getLocalizedStringForResourceKey("gender.female");
      case Male:
        return Localization.getLocalizedStringForResourceKey("gender.male");
    }

    return Localization.getLocalizedStringForResourceKey("gender.unset");
  }
}
