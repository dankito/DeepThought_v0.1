package net.dankito.deepthought.util.web;

import net.dankito.deepthought.util.web.callbacks.RequestCallback;
import net.dankito.deepthought.util.web.responses.WebClientResponse;

/**
 * Created by ganymed on 03/11/16.
 */

public interface IWebClient {

  WebClientResponse get(RequestParameters parameters);
  void getAsync(RequestParameters parameters, final RequestCallback callback);

  WebClientResponse post(RequestParameters parameters);
  void postAsync(RequestParameters parameters, final RequestCallback callback);

}
