package net.deepthought.controls.clipboard;

import net.deepthought.data.contentextractor.*;
import net.deepthought.util.Localization;

import javafx.scene.input.KeyCombination;

/**
 * Created by ganymed on 14/12/15.
 */
public class ContentExtractOptionForUi {

  protected ContentExtractOption contentExtractOption;

  protected String displayNameResourceKey;

  protected String displayName;

  protected KeyCombination shortCut;

  // in this way for UI a different or a wrapping action can be specified for {@link ContentExtractOption} one's
  protected ExtractContentAction action;


  public ContentExtractOptionForUi(ContentExtractOption contentExtractOption, String displayNameResourceKey, KeyCombination shortCut) {
    this.contentExtractOption = contentExtractOption;
    this.displayNameResourceKey = displayNameResourceKey;
    this.shortCut = shortCut;

    this.displayName = Localization.getLocalizedString(displayNameResourceKey);
  }

  public ContentExtractOptionForUi(ContentExtractOption contentExtractOption, String displayNameResourceKey, KeyCombination shortCut, ExtractContentAction action) {
    this(contentExtractOption, displayNameResourceKey, shortCut);
    this.action = action;
  }


  public ContentExtractOption getContentExtractOption() {
    return contentExtractOption;
  }

  public String getDisplayName() {
    return displayName;
  }

  public KeyCombination getShortCut() {
    return shortCut;
  }


  public void runAction(ExtractContentActionResultListener listener) {
    if(action != null) {
      action.runExtraction(getContentExtractOption(), listener);
    }
    else if(contentExtractOption != null) {
      contentExtractOption.runAction(listener);
    }
  }


}
