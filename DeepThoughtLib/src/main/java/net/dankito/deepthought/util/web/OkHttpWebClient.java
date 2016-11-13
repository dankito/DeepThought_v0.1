package net.dankito.deepthought.util.web;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import net.dankito.deepthought.util.web.callbacks.DownloadProgressListener;
import net.dankito.deepthought.util.web.callbacks.RequestCallback;
import net.dankito.deepthought.util.web.responses.WebClientResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

/**
 * Created by ganymed on 03/11/16.
 */

public class OkHttpWebClient implements IWebClient {

  protected static final MediaType FORM_URL_ENCODED_MEDIA_TYPE = MediaType.parse("application/x-www-form-urlencoded; charset=UTF-8");
  protected static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=UTF-8");

  protected static final int DEFAULT_CONNECTION_TIMEOUT_MILLIS = 10000;

  private static final Logger log = LoggerFactory.getLogger(OkHttpWebClient.class);


  // avoid creating several instances, should be singleton
  protected OkHttpClient client = new OkHttpClient();


  public OkHttpWebClient() {
    client.setFollowRedirects(true);
    client.setRetryOnConnectionFailure(true);
  }


  public WebClientResponse get(RequestParameters parameters) {
    try {
      Request request = createGetRequest(parameters);

      Response response = executeRequest(parameters, request);

      return getResponse(parameters, response);
    } catch(Exception e) {
      return getRequestFailed(parameters, e);
    }
  }

  public void getAsync(RequestParameters parameters, final RequestCallback callback) {
    try {
      Request request = createGetRequest(parameters);

      executeRequestAsync(parameters, request, callback);
    } catch(Exception e) {
      asyncGetRequestFailed(parameters, e, callback);
    }
  }

  protected Request createGetRequest(RequestParameters parameters) {
    Request.Builder requestBuilder = new Request.Builder();

    applyParameters(requestBuilder, parameters);

    return requestBuilder.build();
  }


  public WebClientResponse post(RequestParameters parameters) {
    try {
      Request request = createPostRequest(parameters);

      Response response = executeRequest(parameters, request);

      return getResponse(parameters, response);
    } catch(Exception e) {
      return postRequestFailed(parameters, e);
    }
  }

  public void postAsync(RequestParameters parameters, final RequestCallback callback) {
    try {
      Request request = createPostRequest(parameters);

      executeRequestAsync(parameters, request, callback);
    } catch(Exception e) {
      asyncPostRequestFailed(parameters, e, callback);
    }
  }

  protected Request createPostRequest(RequestParameters parameters) {
    Request.Builder requestBuilder = new Request.Builder();

    setPostBody(requestBuilder, parameters);

    applyParameters(requestBuilder, parameters);

    return requestBuilder.build();
  }

  protected void setPostBody(Request.Builder requestBuilder, RequestParameters parameters) {
    if(parameters.isBodySet()) {
      MediaType mediaType = parameters.getContentType() == ContentType.JSON ? JSON_MEDIA_TYPE : FORM_URL_ENCODED_MEDIA_TYPE;
      RequestBody postBody = RequestBody.create(mediaType, parameters.getBody());

      requestBuilder.post(postBody);
    }
  }

  protected void applyParameters(Request.Builder requestBuilder, RequestParameters parameters) {
    requestBuilder.url(parameters.getUrl());

    if(parameters.isUserAgentSet()) {
      requestBuilder.header("User-Agent", parameters.getUserAgent());
    }

    if(parameters.isConnectionTimeoutSet()) {
      client.setConnectTimeout(parameters.getConnectionTimeoutMillis(), TimeUnit.MILLISECONDS);
    }
    else {
      client.setConnectTimeout(DEFAULT_CONNECTION_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
    }
  }

  protected Response executeRequest(final RequestParameters parameters, Request request) throws Exception {
    Response response = client.newCall(request).execute();

    if(response.isSuccessful() == false && parameters.isCountConnectionRetriesSet()) {
      parameters.decrementCountConnectionRetries();
      return executeRequest(parameters, request);
    }
    else {
      return response;
    }
  }

  protected void executeRequestAsync(final RequestParameters parameters, Request request, final RequestCallback callback) {
    client.newCall(request).enqueue(new Callback() {
      @Override
      public void onFailure(Request request, IOException e) {
        asyncRequestFailed(parameters, request, e, callback);
      }

      @Override
      public void onResponse(Response response) throws IOException {
        callback.completed(getResponse(parameters, response));
      }
    });
  }

  protected WebClientResponse getRequestFailed(RequestParameters parameters, Exception e) {
    if(shouldRetryConnection(parameters, e)) {
      parameters.decrementCountConnectionRetries();
      return get(parameters);
    }
    else {
      return new WebClientResponse(e.getLocalizedMessage());
    }
  }

  protected void asyncGetRequestFailed(RequestParameters parameters, Exception e, RequestCallback callback) {
    if(shouldRetryConnection(parameters, e)) {
      parameters.decrementCountConnectionRetries();
      getAsync(parameters, callback);
    }
    else {
      callback.completed(new WebClientResponse(e.getLocalizedMessage()));
    }
  }

  protected WebClientResponse postRequestFailed(RequestParameters parameters, Exception e) {
    if(shouldRetryConnection(parameters, e)) {
      parameters.decrementCountConnectionRetries();
      return post(parameters);
    }
    else {
      return new WebClientResponse(e.getLocalizedMessage());
    }
  }

  protected void asyncPostRequestFailed(RequestParameters parameters, Exception e, RequestCallback callback) {
    if(shouldRetryConnection(parameters, e)) {
      parameters.decrementCountConnectionRetries();
      postAsync(parameters, callback);
    }
    else {
      callback.completed(new WebClientResponse(e.getLocalizedMessage()));
    }
  }

  protected void asyncRequestFailed(RequestParameters parameters, Request request, Exception e, RequestCallback callback) {
    if(shouldRetryConnection(parameters, e)) {
      parameters.decrementCountConnectionRetries();
      executeRequestAsync(parameters, request, callback);
    }
    else {
      log.error("Failure on Request to " + request.urlString(), e);
      callback.completed(new WebClientResponse(e.getLocalizedMessage()));
    }
  }

  protected boolean shouldRetryConnection(RequestParameters parameters, Exception e) {
    return parameters.isCountConnectionRetriesSet() && isConnectionException(e);
  }

  protected boolean isConnectionException(Exception e) {
    return e.getMessage().toLowerCase().contains("timeout");
  }

  protected WebClientResponse getResponse(RequestParameters parameters, Response response) throws IOException {
    if(parameters.hasStringResponse()) {
      return new WebClientResponse(true, response.body().string());
    }
    else {
      return streamBinaryResponse(parameters, response);
    }
  }

  protected WebClientResponse streamBinaryResponse(RequestParameters parameters, Response response) {
    InputStream inputStream = null;
    try {
      inputStream = response.body().byteStream();

      byte[] buffer = new byte[parameters.getDownloadBufferSize()];
      long downloaded = 0;
      long contentLength = response.body().contentLength();

      publishProgress(parameters, buffer, 0L, contentLength);
      while(true) {
        int read = inputStream.read(buffer);
        if(read == -1){
          break;
        }

        downloaded += read;
        publishProgress(parameters, buffer, downloaded, contentLength);
        if(isCancelled(parameters)) {
          return new WebClientResponse(false);
        }
      }

      return new WebClientResponse(true);
    } catch (IOException e) {
      log.error("Could not download binary Response for Url " + parameters.getUrl(), e);
      return new WebClientResponse(e.getLocalizedMessage());
    } finally {
      if (inputStream != null) {
        try { inputStream.close(); } catch(Exception ignored) { }
      }
    }
  }

  protected boolean isCancelled(RequestParameters parameters) {
    return false; // TODO: implement mechanism to abort download
  }

  protected void publishProgress(RequestParameters parameters, byte[] downloadedChunk, long currentlyDownloaded, long total) {
    DownloadProgressListener progressListener = parameters.getDownloadProgressListener();

    if(progressListener != null) {
      float progress = total <= 0 ? Float.NaN : currentlyDownloaded / (float)total;
      progressListener.progress(progress, downloadedChunk);
    }
  }

}
