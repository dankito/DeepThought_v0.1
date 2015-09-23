package net.deepthought.data.html;

import net.deepthought.data.model.FileLink;

/**
 * Created by ganymed on 23/09/15.
 */
public class ImageElementData {

  public final static int DefaultImageWidth = 400;

  public final static int DefaultImageHeight = 300;

  public final static String SourceAttributeName = "src";

  public final static String ImageIdAttributeName = "imageid";

  public final static String EmbeddingIdAttributeName = "embeddingid";

  public final static String WidthAttributeName = "width";

  public final static String HeightAttributeName = "height";

  public final static String AltAttributeName = "alt";


  protected String imgElementHtmlCode = null;

  protected String originalImgElementHtmlCode = null;

  
  protected String source;

  protected long fileId;

  protected long embeddingId;
  
  protected int width = DefaultImageWidth;

  protected int height = DefaultImageHeight;

  protected String alt;


  public ImageElementData() {

  }

  public ImageElementData(FileLink file, int width, int height) {
    this(file.getUriString(), file.getId(), createUniqueEmbeddingId(), width, height, file.getDescription());
  }

  public ImageElementData(String source, long fileId, long embeddingId, int width, int height, String alt) {
    this.source = source;
    this.fileId = fileId;
    this.embeddingId = embeddingId;
    this.width = width;
    this.height = height;
    this.alt = alt;
  }


  public String getHtmlCode() {
    if(imgElementHtmlCode == null)
      imgElementHtmlCode = "<img src=\"" + source + "\" " + ImageIdAttributeName + "=\"" + fileId + "\" " + EmbeddingIdAttributeName + "=\"" + embeddingId + "\" width=\"" +
                           width + "\" height=\"" + height + "\" alt=\"" + alt + "\" />";
    return imgElementHtmlCode;
  }

  public String getOriginalImgElementHtmlCode() {
    return originalImgElementHtmlCode;
  }

  public void setOriginalImgElementHtmlCode(String originalImgElementHtmlCode) {
    this.originalImgElementHtmlCode = originalImgElementHtmlCode;
  }


  public String getSource() {
    return source;
  }

  public void setSource(String source) {
    this.source = source;
    imgElementHtmlCode = null;
  }

  public long getFileId() {
    return fileId;
  }

  public void setFileId(long fileId) {
    this.fileId = fileId;
    imgElementHtmlCode = null;
  }

  public long getEmbeddingId() {
    return embeddingId;
  }

  public void setEmbeddingId(long embeddingId) {
    this.embeddingId = embeddingId;
    imgElementHtmlCode = null;
  }

  public int getWidth() {
    return width;
  }

  public void setWidth(int width) {
    this.width = width;
    imgElementHtmlCode = null;
  }

  public int getHeight() {
    return height;
  }

  public void setHeight(int height) {
    this.height = height;
    imgElementHtmlCode = null;
  }

  public String getAlt() {
    return alt;
  }

  public void setAlt(String alt) {
    this.alt = alt;
    imgElementHtmlCode = null;
  }


  protected static long createUniqueEmbeddingId() {
    String milliseconds = Long.toString(System.currentTimeMillis());
    milliseconds = milliseconds.substring(milliseconds.length() - 7);

    return Long.parseLong(milliseconds);
  }
  
}
