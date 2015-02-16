package net.deepthought.data.contentextractor;

import java.net.URL;

/**
 * Created by ganymed on 15/01/15.
 */
public interface IContentExtractor {

  boolean canExtractContentFromUrl(URL url);

  void extractContentFromUrlAsync(URL url, ExtractContentListener listener);

  boolean canCreateEntryFromUrl(URL url);

  void createEntryFromUrlAsync(URL url, ExtractContentListener listener);

}
