package net.dankito.deepthought.data.contentextractor;

import net.dankito.deepthought.communication.model.DoOcrConfiguration;
import net.dankito.deepthought.data.contentextractor.ContentExtractOption;
import net.dankito.deepthought.data.contentextractor.ContentExtractOptions;
import net.dankito.deepthought.data.contentextractor.EntryCreationResult;
import net.dankito.deepthought.data.contentextractor.ExtractContentAction;
import net.dankito.deepthought.data.contentextractor.ExtractContentActionResultListener;
import net.dankito.deepthought.data.contentextractor.ocr.OcrContentExtractorBase;
import net.dankito.deepthought.data.contentextractor.ocr.RecognizeTextListener;
import net.dankito.deepthought.data.contentextractor.ocr.TextRecognitionResult;
import net.dankito.deepthought.data.model.Entry;
import net.dankito.deepthought.util.DeepThoughtError;
import net.dankito.deepthought.util.localization.Localization;

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
  public int getSupportedPluginSystemVersion() {
    return 1;
  }

  @Override
  public String getPluginVersion() {
    return "0.1";
  }


  @Override
  public ContentExtractOptions createExtractOptionsForUrl(String url) {
    ContentExtractOptions options = new ContentExtractOptions(url, getName());

    options.addContentExtractOption(new ContentExtractOption(this, url, true, "content.extractor.try.to.extract.text.from.file", new ExtractContentAction() {
      @Override
      public void runExtraction(ContentExtractOption option, ExtractContentActionResultListener listener) {
        if(listener != null) {
          EntryCreationResult result = tryToExtractText(option.getUrl());
          listener.extractingContentDone(result);
        }
      }
    }));

    return options;
  }


  private EntryCreationResult tryToExtractText(String url) {
    String extractedText = url;

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
      if(url.startsWith("file:"))
        url = url.substring("file:".length());
      parser.parse(new FileInputStream(url), contentHandler, metadata, parseContext);

      extractedText = writer.toString();

      return new EntryCreationResult(url, new Entry(extractedText));
    } catch(Exception ex) {
      return new EntryCreationResult(url, new DeepThoughtError(Localization.getLocalizedString("error.could.not.extract.text.from.file", url), ex));
    }
  }

  @Override
  protected void recognizeText(DoOcrConfiguration configuration, RecognizeTextListener listener) {
    listener.textRecognized(TextRecognitionResult.createErrorOccurredResult("Not implemented yet"));
  }
}
