package net.deepthought.controller.enums;

import net.deepthought.util.Localization;

/**
 * Created by ganymed on 01/01/15.
 */
public enum FileLinkOptions {

  Link,
  CopyToDataFolder,
  MoveToDataFolder;

  @Override
  public String toString() {
    switch(this) {
      case CopyToDataFolder:
        return Localization.getLocalizedString("file.link.options.copy.to.data.folder");
      case MoveToDataFolder:
        return Localization.getLocalizedString("file.link.options.move.to.data.folder");
      default:
        return Localization.getLocalizedString("file.link.options.link");
    }
  }
}
