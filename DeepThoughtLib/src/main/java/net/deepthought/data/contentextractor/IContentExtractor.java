package net.deepthought.data.contentextractor;

import net.deepthought.plugin.IPlugin;

/**
 * Created by ganymed on 15/01/15.
 */
public interface IContentExtractor extends IPlugin {

//  boolean canExtractContentFromUrl(URL url);
//
//  void extractContentFromUrlAsync(URL url, ExtractContentListener listener);

  boolean canCreateEntryFromUrl(String url);

  void createEntryFromUrlAsync(String url, CreateEntryListener listener);

  ContentExtractOption canCreateEntryFromClipboardContent(ClipboardContent clipboardContent);

  void createEntryFromClipboardContentAsync(ContentExtractOption contentExtractOption, CreateEntryListener listener);

}
