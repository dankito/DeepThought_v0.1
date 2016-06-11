package net.dankito.deepthought.communication.listener;

import net.dankito.deepthought.communication.messages.request.ImportFilesRequest;
import net.dankito.deepthought.communication.messages.response.ImportFilesResultResponse;

/**
 * Created by ganymed on 21/11/15.
 */
public interface ImportFilesResultListener extends AsynchronousResponseListener<ImportFilesRequest, ImportFilesResultResponse> {

}
