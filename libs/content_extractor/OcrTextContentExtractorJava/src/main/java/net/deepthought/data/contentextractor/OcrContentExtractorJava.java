package net.deepthought.data.contentextractor;

import net.deepthought.data.contentextractor.ocr.OcrContentExtractorBase;
import net.deepthought.data.model.Entry;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.ocr.TesseractOCRConfig;
import org.apache.tika.parser.ocr.TesseractOCRParser;
import org.apache.tika.sax.WriteOutContentHandler;

import java.io.FileInputStream;
import java.io.StringWriter;

/**
 * Created by ganymed on 25/04/15.
 */
public class OcrContentExtractorJava extends OcrContentExtractorBase {

  @Override
  protected void createEntryFromUrl(String url, CreateEntryListener listener) {
    if(listener != null)
      listener.entryCreated(new EntryCreationResult(url, new Entry(url)));
  }

  @Override
  protected void createEntryFromClipboardContent(ContentExtractOption contentExtractOption, CreateEntryListener listener) {
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
