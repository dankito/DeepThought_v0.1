package net.dankito.deepthought.util.web;

import net.dankito.deepthought.util.IThreadPool;
import net.dankito.deepthought.util.StringUtils;
import net.dankito.deepthought.util.web.callbacks.RequestCallback;
import net.dankito.deepthought.util.web.responses.WebClientResponse;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.nio.charset.Charset;

/**
 * Created by ganymed on 03/11/16.
 */

public class ApacheHttpClientWebClient implements IWebClient {

  public final static Charset MESSAGES_CHARSET = Charset.forName("utf8");
  public final static String MESSAGES_CHARSET_NAME = HTTP.UTF_8;

  public final static String JSON_CONTENT_TYPE = "application/json";
  public final static String FORM_URLENCODED_CONTENT_TYPE = "application/x-www-form-urlencoded";

  public static final int TIMEOUT_MILLIS = 10000;


  protected IThreadPool threadPool;

  protected RequestConfig requestConfig = null;


  public ApacheHttpClientWebClient(IThreadPool threadPool) {
    this.threadPool = threadPool;
  }


  @Override
  public void getAsync(final RequestParameters parameters, final RequestCallback callback) {
    threadPool.runTaskAsync(new Runnable() {
      @Override
      public void run() {
        callback.completed(get(parameters));
      }
    });
  }

  public WebClientResponse get(RequestParameters parameters) {
    try {
      HttpClient httpClient = new DefaultHttpClient();

      HttpGet request = new HttpGet(parameters.getUrl());
      request.setConfig(getRequestConfig());

      HttpResponse response = httpClient.execute(request);

      HttpEntity entity = response.getEntity();
      String responseString = EntityUtils.toString(entity);

      httpClient.getConnectionManager().shutdown();

      return new WebClientResponse(true, responseString);
    } catch(Exception e) {
      return new WebClientResponse(e.getLocalizedMessage());
    }
  }

  @Override
  public void postAsync(final RequestParameters parameters, final RequestCallback callback) {
    threadPool.runTaskAsync(new Runnable() {
      @Override
      public void run() {
        callback.completed(post(parameters));
      }
    });
  }

  public WebClientResponse post(RequestParameters parameters) {
    try {
      DefaultHttpClient httpClient = new DefaultHttpClient();
      HttpPost postRequest = new HttpPost(parameters.getUrl());

      if(StringUtils.isNotNullOrEmpty(parameters.getBody())) {
        postRequest.setEntity(createPostBody(parameters));
      }

      postRequest.setConfig(getRequestConfig());

      HttpResponse response = httpClient.execute(postRequest);

      HttpEntity entity = response.getEntity();
      String responseString = EntityUtils.toString(entity);

      httpClient.getConnectionManager().shutdown();

      return new WebClientResponse(true, responseString);
    } catch(Exception e) {
      return new WebClientResponse(e.getLocalizedMessage());
    }
  }

  protected HttpEntity createPostBody(RequestParameters parameters) {
    StringEntity postEntity = new StringEntity(parameters.getBody(), MESSAGES_CHARSET_NAME);

    if(parameters.getContentType() == ContentType.FORM_URL_ENCODED) {
      postEntity.setContentType(FORM_URLENCODED_CONTENT_TYPE);
    }
    else if(parameters.getContentType() == ContentType.JSON) {
      postEntity.setContentType(JSON_CONTENT_TYPE);
    }

    return postEntity;
  }

  protected RequestConfig getRequestConfig() {
    if(requestConfig == null) {
      requestConfig = RequestConfig.custom()
          .setSocketTimeout(TIMEOUT_MILLIS)
          .setConnectTimeout(TIMEOUT_MILLIS)
          .setConnectionRequestTimeout(TIMEOUT_MILLIS)
          .build();
    }

    return requestConfig;
  }

}
