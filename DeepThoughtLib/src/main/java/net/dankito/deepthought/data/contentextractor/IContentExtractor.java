package net.dankito.deepthought.data.contentextractor;

/**
 * Created by ganymed on 15/01/15.
 */
public interface IContentExtractor {

  boolean canCreateEntryFromUrl(String url);

  ContentExtractOptions createExtractOptionsForUrl(String url);

}
