package net.deepthought.data.contentextractor;

/**
 * Created by ganymed on 24/04/15.
 */
public interface IContentExtractorManager {

//  public Collection<IContentExtractor> getContentExtractors();

  public boolean addContentExtractor(IContentExtractor contentExtractor);

//  public List<IContentExtractor> getContentExtractorsForUrl(String url);

  public ContentExtractOptions getContentExtractorOptionsForClipboardContent(ClipboardContent clipboardContent);

}
