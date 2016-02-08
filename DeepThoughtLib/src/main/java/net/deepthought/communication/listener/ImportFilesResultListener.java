package net.deepthought.communication.listener;

import net.deepthought.communication.messages.request.ImportFilesRequest;
import net.deepthought.communication.messages.response.ImportFilesResultResponse;

/**
 * Created by ganymed on 21/11/15.
 */
public interface ImportFilesResultListener extends AsynchronousResponseListener<ImportFilesRequest, ImportFilesResultResponse> {

}
