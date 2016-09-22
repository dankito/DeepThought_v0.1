package net.dankito.deepthought.ui.model;

import net.dankito.deepthought.data.model.Tag;
import net.dankito.deepthought.util.localization.Localization;

import java.util.Collection;

/**
 * Created by ganymed on 12/09/16.
 */
public class TagPreviewService {

  public static final int NO_MAX_PREVIEW_LENGTH = -1;


  public String createTagsPreview(Collection<Tag> tags, boolean showNoTagsSetMessage) {
    return createTagsPreview(tags, showNoTagsSetMessage, NO_MAX_PREVIEW_LENGTH);
  }

  public String createTagsPreview(Collection<Tag> tags, boolean showNoTagsSetMessage, int maxPreviewLength) {
    String tagsPreview = "";

    if(tags.size() == 0) {
      if(showNoTagsSetMessage) {
        tagsPreview = Localization.getLocalizedString("no.tags.set");
      }
    }
    else {
      for (Tag tag : tags) {
        tagsPreview += tag.getName() + ", ";
      }

      if (tagsPreview.length() > 1) { // remove last ", "
        tagsPreview = tagsPreview.substring(0, tagsPreview.length() - 2);
      }

      if (maxPreviewLength > 0 && tagsPreview.length() >= maxPreviewLength) {
        tagsPreview = tagsPreview.substring(0, maxPreviewLength) + " ...";
      }

      tagsPreview = tagsPreview.replace("\r", "").replace("\n", ""); // if a Tag contains a new line symbol, replace that one for preview
    }

    return tagsPreview;
  }
}
