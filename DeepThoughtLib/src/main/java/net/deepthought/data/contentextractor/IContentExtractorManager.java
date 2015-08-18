package net.deepthought.data.contentextractor;

import net.deepthought.data.contentextractor.ocr.IOcrContentExtractor;

/**
 * Created by ganymed on 24/04/15.
 */
public interface IContentExtractorManager {

//  public Collection<IContentExtractor> getContentExtractors();

  public boolean addContentExtractor(IContentExtractor contentExtractor);

//  public List<IContentExtractor> getContentExtractorsForUrl(String url);

  public ContentExtractOptions getContentExtractorOptionsForClipboardContent(ClipboardContent clipboardContent);

  public boolean hasOcrContentExtractors();

  public IOcrContentExtractor getPreferredOcrContentExtractor();

}
