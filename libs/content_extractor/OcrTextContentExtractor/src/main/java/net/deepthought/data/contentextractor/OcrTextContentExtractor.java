package net.deepthought.data.contentextractor;

import net.deepthought.data.model.Entry;
import net.deepthought.util.Localization;
import net.deepthought.util.file.FileUtils;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.ocr.TesseractOCRConfig;
import org.apache.tika.parser.ocr.TesseractOCRParser;
import org.apache.tika.sax.WriteOutContentHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.StringWriter;

import javafx.scene.image.Image;

/**
 * Created by ganymed on 25/04/15.
 */
public class OcrTextContentExtractor implements ITextContentExtractor {

  @Override
  public String getName() {
    return Localization.getLocalizedStringForResourceKey("ocr.text.content.extractor");
  }

  @Override
  public boolean canCreateEntryFromUrl(String url) {
    return FileUtils.isImageFile(FileUtils.getMimeType(url));
  }

  @Override
  public void createEntryFromUrlAsync(String url, CreateEntryListener listener) {
    if(listener != null)
      listener.entryCreated(new EntryCreationResult(url, new Entry(url)));
  }

  @Override
  public ContentExtractOption canCreateEntryFromClipboardContent(ClipboardContent clipboardContent) {
    if(clipboardContent.hasImage()) {
      Image image = clipboardContent.getImage();
      return new ContentExtractOption(this, image, true);
    }

    if(clipboardContent.hasFiles()) {
      for(File file : clipboardContent.getFiles()) { // TODO: what about other files if one of the first files already succeed?
        if(canCreateEntryFromUrl(file.getAbsolutePath()))
          return new ContentExtractOption(this, file.getAbsolutePath(), true);
      }
    }

    if(clipboardContent.hasUrl()) {
      if (canCreateEntryFromUrl(clipboardContent.getUrl()))
        return new ContentExtractOption(this, clipboardContent.getUrl(), true);
    }

    if(clipboardContent.hasString()) {
        if (canCreateEntryFromUrl(clipboardContent.getString()))
          return new ContentExtractOption(this, clipboardContent.getString(), true);
      }

    return ContentExtractOption.CanNotExtractContent;
  }

  @Override
  public void createEntryFromClipboardContentAsync(ContentExtractOption contentExtractOption, CreateEntryListener listener) {
    String extractedText = contentExtractOption.getUrl();

    TesseractOCRConfig config = new TesseractOCRConfig();
//Needed if tesseract is not on system path
//    config.setTesseractPath(tesseractFolder);
    config.setLanguage("eng+de");
    ParseContext parseContext = new ParseContext();
    parseContext.set(TesseractOCRConfig.class, config);

//    ContentHandler contentHandler = new BodyContentHandler();
    StringWriter writer=new StringWriter();
    WriteOutContentHandler contentHandler=new WriteOutContentHandler(writer);
    Metadata metadata = new Metadata();

    TesseractOCRParser parser = parseContext.get(TesseractOCRParser.class);
    parser = new TesseractOCRParser();
    try {
      String url = contentExtractOption.getUrl(); // TODO: remove again, is now integrated in ContentExtractOptions
      if(url.startsWith("file:"))
        url = url.substring("file:".length());
      parser.parse(new FileInputStream(url), contentHandler, metadata, parseContext);

      extractedText = writer.toString();
    } catch(Exception ex) {
      String message = ex.getMessage();
    }

    if(listener != null)
      listener.entryCreated(new EntryCreationResult(contentExtractOption.getSource(), new Entry(extractedText)));
  }
}
