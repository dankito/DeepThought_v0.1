package net.dankito.deepthought.data.contentextractor.model;

/**
 * Created by ganymed on 15/12/15.
 */
public class AvailableFormat {

  protected String url;

  protected String description;

  protected Container container;

  protected Encoding videoEncoding;

  protected Encoding audioEncoding;

  protected VideoQuality videoQuality;

  protected AudioQuality audioQuality;


  public AvailableFormat(String url, String description) {
    this.url = url;
    this.description = description;
  }

  public AvailableFormat(String url, Container container, Encoding videoEncoding, Encoding audioEncoding, VideoQuality videoQuality, AudioQuality audioQuality) {
    this.url = url;
    this.container = container;
    this.videoEncoding = videoEncoding;
    this.audioEncoding = audioEncoding;
    this.videoQuality = videoQuality;
    this.audioQuality = audioQuality;
  }


  public String getUrl() {
    return url;
  }

  public String getDescription() {
    if(description == null)
      return createDescriptionFromAudioAndVideoInfo();
    return description;
  }

  protected String createDescriptionFromAudioAndVideoInfo() {
    return container + " (Video: " + videoEncoding + ", Audio: " + audioEncoding + ")";
  }

  public Container getContainer() {
    return container;
  }

  public Encoding getVideoEncoding() {
    return videoEncoding;
  }

  public Encoding getAudioEncoding() {
    return audioEncoding;
  }

  public VideoQuality getVideoQuality() {
    return videoQuality;
  }

  public AudioQuality getAudioQuality() {
    return audioQuality;
  }


  @Override
  public String toString() {
    return getDescription();
  }

}
