package net.dankito.deepthought.util.web.callbacks;

import net.dankito.deepthought.util.web.responses.WebClientResponse;

/**
 * Created by ganymed on 03/11/16.
 */

public interface RequestCallback {

  void completed(WebClientResponse response);

}
