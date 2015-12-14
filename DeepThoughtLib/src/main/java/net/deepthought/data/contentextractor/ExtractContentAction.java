package net.deepthought.data.contentextractor;

/**
 * Created by ganymed on 14/12/15.
 */
public interface ExtractContentAction {

  void runExtraction(ContentExtractOption option, ExtractContentActionResultListener listener);

}
