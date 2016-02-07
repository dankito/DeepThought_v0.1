package net.deepthought.data.contentextractor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ganymed on 15/01/15.
 */
public class ExtractOptions {

  protected ContentExtractQuality quality;

  protected List<ContentExtractPart> partsToExtract;


  public ExtractOptions(ContentExtractQuality quality, final ContentExtractPart partToExtract) {
    this(quality, new ArrayList<ContentExtractPart>() {{ add(partToExtract); }});
  }

  public ExtractOptions(ContentExtractQuality quality, List<ContentExtractPart> partsToExtract) {
    this.quality = quality;
    this.partsToExtract = partsToExtract;
  }

}
