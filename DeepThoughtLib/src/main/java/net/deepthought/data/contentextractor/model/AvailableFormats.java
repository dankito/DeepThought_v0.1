package net.deepthought.data.contentextractor.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ganymed on 15/12/15.
 */
public class AvailableFormats {

  protected String url;

  protected String title;

  protected String previewImageUrl;

  protected List<AvailableFormat> formats = new ArrayList<>();


  public AvailableFormats() {
    this("", "", "");
  }

  public AvailableFormats(String url, String title, String previewImageUrl) {
    this.url = url;
    this.title = title;
    this.previewImageUrl = previewImageUrl;
  }


  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getTitle() {
    return title;
  }

  public String getPreviewImageUrl() {
    return previewImageUrl;
  }

  public List<AvailableFormat> getFormats() {
    return formats;
  }

  public boolean addAvailableFormat(AvailableFormat format) {
    return formats.add(format);
  }


  @Override
  public String toString() {
    return title + " (" + url + ")";
  }

}
