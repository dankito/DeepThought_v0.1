package net.deepthought.data.contentextractor;

/**
 * Created by ganymed on 25/04/15.
 */
public interface IRemoteFileContentExtractor extends ILocalFileContentExtractor {

  public boolean canDownloadFile(String url);

}
