package net.deepthought.data.contentextractor;

import net.deepthought.data.model.FileLink;

/**
 * Created by ganymed on 25/04/15.
 */
public interface ILocalFileContentExtractor extends IContentExtractor {

  public boolean canAttachFileToEntry(String url);

  public boolean canSetFileAsEntryContent(String url);

//  public boolean canExtractTextFromFile();

  public FileLink createFileLink(ContentExtractOption contentExtractOption);

}
