package net.dankito.deepthought.data.contentextractor;

import net.dankito.deepthought.util.DeepThoughtError;

import java.net.URL;
import java.util.List;

/**
 * Created by ganymed on 15/01/15.
 */
public interface ExtractContentListener {


  ExtractOptions chooseExtractOptions(URL url, List<ContentExtractQuality> availableQualities, ContentExtractPart wholePartToExtract);

  void partExtracted(URL url, String extractedPart, float percentage);

  void extractionDone(URL url, boolean successful, DeepThoughtError error, String extractedContent);

}
