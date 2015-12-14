package net.deepthought.data.contentextractor.file;

import net.deepthought.Application;
import net.deepthought.data.contentextractor.ContentExtractOption;
import net.deepthought.data.contentextractor.ContentExtractOptions;
import net.deepthought.data.contentextractor.CreateEntryListener;
import net.deepthought.data.contentextractor.EntryCreationResult;
import net.deepthought.data.contentextractor.ExtractContentAction;
import net.deepthought.data.contentextractor.ExtractContentActionResultListener;
import net.deepthought.data.contentextractor.IContentExtractor;
import net.deepthought.data.model.Entry;
import net.deepthought.data.model.FileLink;
import net.deepthought.util.file.FileUtils;

/**
 * Created by ganymed on 14/12/15.
 */
public abstract class FileContentExtractorBase implements IContentExtractor {

  @Override
  public ContentExtractOptions createExtractOptionsForUrl(String url) {
    ContentExtractOptions options = new ContentExtractOptions(url, FileUtils.getFileNameIncludingExtension(url));

    options.addContentExtractOption(createAttachFileToEntryOption(url));

    if(canSetFileAsEntryContent(url)) {
      options.addContentExtractOption(createSetFileAsEntryContentOption(url));
    }

    if(canExtractTextFromFile(url)) {
      options.addContentExtractOption(createTryToExtractTextFromFileOption(url));
      options.addContentExtractOption(createAttachAndTryToExtractTextFromFileOption(url));
    }

    return options;
  }

  protected ContentExtractOption createAttachFileToEntryOption(String url) {
    return new ContentExtractOption(this, url, true, "content.extractor.attach.file.to.entry", new ExtractContentAction() {
      @Override
      public void runExtraction(ContentExtractOption option, ExtractContentActionResultListener listener) {
        EntryCreationResult result = attachFileToNewEntry(option);
        listener.extractingContentDone(result);
      }
    });
  }

  protected EntryCreationResult attachFileToNewEntry(ContentExtractOption option) {
    return attachFileToNewEntry(option.getUrl());
  }

  protected EntryCreationResult attachFileToNewEntry(String url) {
    EntryCreationResult result = new EntryCreationResult(url, new Entry());

    result.addAttachedFile(new FileLink(url));

    return result;
  }


  protected boolean canSetFileAsEntryContent(String url) {
    return FileUtils.isImageFileSuitableForHtmlEditor(FileUtils.getMimeType(url));
  }

  protected ContentExtractOption createSetFileAsEntryContentOption(String url) {
    return new ContentExtractOption(this, url, true, "content.extractor.set.file.as.entry.content", new ExtractContentAction() {
      @Override
      public void runExtraction(ContentExtractOption option, ExtractContentActionResultListener listener) {
        EntryCreationResult result = setFileAsEntryContent(option);
        listener.extractingContentDone(result);
      }
    });
  }

  protected EntryCreationResult setFileAsEntryContent(ContentExtractOption option) {
    return setFileAsEntryContent(option.getUrl());
  }

  protected EntryCreationResult setFileAsEntryContent(String url) {
    Entry entry = new Entry(createContentFromUrl(url));

    return new EntryCreationResult(url, entry);
  }

  protected String createContentFromUrl(String url) {
    return "<html dir=\"ltr\"><head></head><body contenteditable=\"true\"><p><img src=\"file://" + url + "\" " +
        "/></p><p></p></body></html>";
  }


  protected boolean canExtractTextFromFile(String url) {
    // TODO: later on also check for other Content Extractors (or Content Converters) like MS Office, PDF, ... Content Extractors
    return Application.getContentExtractorManager() != null && Application.getContentExtractorManager().hasOcrContentExtractors() &&
        Application.getContentExtractorManager().getPreferredOcrContentExtractor().canCreateEntryFromUrl(url);
  }

  protected ContentExtractOption createTryToExtractTextFromFileOption(String url) {
    return new ContentExtractOption(this, url, true, "content.extractor.try.to.extract.text.from.file", new ExtractContentAction() {
      @Override
      public void runExtraction(ContentExtractOption option, final ExtractContentActionResultListener listener) {
        tryToExtractTextFromFile(option, new CreateEntryListener() {
          @Override
          public void entryCreated(EntryCreationResult creationResult) {
            listener.extractingContentDone(creationResult);
          }
        });
      }
    });
  }

  protected ContentExtractOption createAttachAndTryToExtractTextFromFileOption(String url) {
    return new ContentExtractOption(this, url, true, "content.extractor.attach.file.to.entry.and.try.to.extract.text.from.file", new ExtractContentAction() {
      @Override
      public void runExtraction(final ContentExtractOption option, final ExtractContentActionResultListener listener) {
        tryToExtractTextFromFile(option, new CreateEntryListener() {
          @Override
          public void entryCreated(EntryCreationResult creationResult) {
            creationResult.addAttachedFile(new FileLink(option.getUrl()));
            listener.extractingContentDone(creationResult);
          }
        });
      }
    });
  }

  protected void tryToExtractTextFromFile(ContentExtractOption option, CreateEntryListener listener) {
    tryToExtractTextFromFile(option.getUrl(), listener);
  }

  protected void tryToExtractTextFromFile(String url, CreateEntryListener listener) {
    Application.getContentExtractorManager().getPreferredOcrContentExtractor().createEntryFromUrlAsync(url, listener);
  }


  @Override
  public void createEntryFromUrlAsync(String url, CreateEntryListener listener) {

  }
}
