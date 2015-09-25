package net.deepthought.data.contentextractor;

import net.deepthought.data.contentextractor.ocr.IOcrContentExtractor;

import java.util.Collection;

/**
 * Created by ganymed on 24/04/15.
 */
public interface IContentExtractorManager {

//  Collection<IContentExtractor> getContentExtractors();

  boolean addContentExtractor(IContentExtractor contentExtractor);

//  List<IContentExtractor> getContentExtractorsForUrl(String url);

  ContentExtractOptions getContentExtractorOptionsForClipboardContent(ClipboardContent clipboardContent);

  boolean hasOcrContentExtractors();

  IOcrContentExtractor getPreferredOcrContentExtractor();

  boolean hasOnlineArticleContentExtractors();

  Collection<IOnlineArticleContentExtractor> getOnlineArticleContentExtractors();

  boolean hasOnlineArticleContentExtractorsWithArticleOverview();

   Collection<IOnlineArticleContentExtractor> getOnlineArticleContentExtractorsWithArticleOverview();

}
