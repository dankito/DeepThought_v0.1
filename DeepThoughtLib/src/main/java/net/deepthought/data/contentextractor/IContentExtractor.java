package net.deepthought.data.contentextractor;

/**
 * Created by ganymed on 15/01/15.
 */
public interface IContentExtractor {

//  boolean canExtractContentFromUrl(URL url);
//
//  void extractContentFromUrlAsync(URL url, ExtractContentListener listener);

  boolean canCreateEntryFromUrl(String url);

  void createEntryFromUrlAsync(String url, CreateEntryListener listener);

  ContentExtractOption canCreateEntryFromClipboardContent(ClipboardContent clipboardContent);

  void createEntryFromClipboardContentAsync(ContentExtractOption contentExtractOption, CreateEntryListener listener);

}
