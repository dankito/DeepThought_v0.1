package net.deepthought.data.contentextractor;

import net.deepthought.Application;
import net.deepthought.data.model.Entry;
import net.deepthought.data.model.Reference;
import net.deepthought.data.model.ReferenceSubDivision;
import net.deepthought.data.model.SeriesTitle;
import net.deepthought.util.DeepThoughtError;
import net.deepthought.util.Localization;
import net.deepthought.util.StringUtils;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SueddeutscheContentExtractor implements IOnlineArticleContentExtractor {

  private final static Logger log = LoggerFactory.getLogger(SueddeutscheContentExtractor.class);


  @Override
  public String getSiteBaseUrl() {
    return "Sueddeutsche.de";
  }

  @Override
  public boolean canCreateEntryFromUrl(String url) {
    return url.startsWith("http://www.sueddeutsche.de/") || url.startsWith("https://www.sueddeutsche.de/");
  }

  @Override
  public void createEntryFromUrlAsync(final String url, final CreateEntryListener listener) {
    if(canCreateEntryFromUrl(url) == false) {
      if(listener != null)
        listener.entryCreated(new EntryCreationResult(url, new DeepThoughtError(Localization.getLocalizedStringForResourceKey("can.not.create.entry.from.url"))));
    }
    else {
      new Thread(new Runnable() {
        @Override
        public void run() {
          createEntryFromUrl(url, listener);
        }
      }).start();
    }
  }

  public void createEntryFromUrl(String url, CreateEntryListener listener) {
    if(listener != null)
      listener.entryCreated(createEntryFromArticle(url));
  }

  @Override
  public ContentExtractOption canCreateEntryFromClipboardContent(ClipboardContent clipboardContent) {
    if(clipboardContent.hasUrl()) {
      if(canCreateEntryFromUrl(clipboardContent.getUrl()))
        return new ContentExtractOption(this, clipboardContent.getUrl(), true);
    }
    else if(clipboardContent.hasString()) {
      if(canCreateEntryFromUrl(clipboardContent.getString()))
        return new ContentExtractOption(this, clipboardContent.getString(), true);
    }

    return ContentExtractOption.CanNotExtractContent;
  }

  @Override
  public void createEntryFromClipboardContentAsync(ContentExtractOption contentExtractOption, CreateEntryListener listener) {
    if(contentExtractOption.isUrl())
      createEntryFromUrlAsync(contentExtractOption.getUrl(), listener);
    else if(listener != null)
      listener.entryCreated(new EntryCreationResult(contentExtractOption, new DeepThoughtError(Localization.getLocalizedStringForResourceKey("can.not.create.entry.from.clipboard.content"))));

  }


  public EntryCreationResult createEntryFromArticle(String articleUrl) {
    try {
      Connection connection = Jsoup.connect(articleUrl);
      connection.header("user-agent", "Mozilla/5.0 (Windows NT 6.3; rv:36.0) Gecko/20100101 Firefox/36.0");
      Connection.Response response = connection.execute();
      Document document = response.parse();
      return parseHtmlToEntry(document);
    } catch(Exception ex) {
      log.error("Could not retrieve Article's HTML Code from Url " + articleUrl, ex);
      return new EntryCreationResult(articleUrl, new DeepThoughtError(Localization.getLocalizedStringForResourceKey("could.not.retrieve.articles.html.code", articleUrl), ex));
    }
  }

  protected EntryCreationResult parseHtmlToEntry(Document document) {
    try {
      Element articleElement = null;
      Elements articleElements = document.body().getElementsByTag("article");
      if (articleElements.size() == 1)
        articleElement = articleElements.get(0);
      else
        articleElement = document.body().getElementById("sitecontent");

      ReferenceSubDivision reference = createReference(articleElement);

      Entry articleEntry = createEntry(articleElement);
      articleEntry.setReferenceSubDivision(reference);

      return new EntryCreationResult(document.baseUri(), articleEntry);
    } catch(Exception ex) {
      return new EntryCreationResult(document.baseUri(), new DeepThoughtError(Localization.getLocalizedStringForResourceKey("could.not.create.entry.from.article.html"), ex));
    }
  }

  protected Entry createEntry(Element articleElement) throws Exception {
    Elements bodyClassElements = articleElement.getElementsByClass("body");
    Element bodySection = null;
    for(Element element : bodyClassElements) {
      if("section".equals(element.tagName())) {
        bodySection = element;
        break;
      }
    }

    if(bodySection == null) {
      log.error("Could not find Article Body section for Sueddeutsche Article " + articleElement.baseUri());
      throw new Exception(Localization.getLocalizedStringForResourceKey("could.not.find.sueddeutsche.article.body.section", articleElement.baseUri()));
    }

    return extractEntryFromBodySection(bodySection);
  }

  protected Entry extractEntryFromBodySection(Element bodySection) throws Exception {
    Entry entry = new Entry();
    String content = "";

    // body section contains a lot of stuff we don't need but luckily all article (text) data is given in Paragraph elements
    for(Element bodyChild : bodySection.children()) {
      if("p".equals(bodyChild.tagName())) { // so check if child element is a Paragraph element or a (for us useless) other element
        if("article entry-summary".equals(bodyChild.className())) // there's only one special Paragraph element, the first one, with the article summary
          entry.setAbstract(bodyChild.text().trim());
        else
          content += "<p>" + bodyChild.html().trim() + "</p>";
      }
      else if("ul".endsWith(bodyChild.tagName())) {
        entry.setAbstract(bodyChild.outerHtml().replaceAll("\u00A0", " "));
      }
    }

    if(content.length() == 0) {
      log.error("Could not extract content from Body section for Sueddeutsche Article " + bodySection.baseUri());
      throw new Exception(Localization.getLocalizedStringForResourceKey("could.not.extract.content.from.sueddeutsche.article.body.section", bodySection.baseUri()));
    }

    entry.setContent(content.replaceAll("\u00A0", " ")); // Converting nbsp entities
//    Application.getDeepThought().addEntry(entry);

    return entry;
  }

  protected ReferenceSubDivision createReference(Element articleElement) {
    Elements headerClassElements = articleElement.getElementsByClass("header");
    Element headerSection = null;
    for(Element element : headerClassElements) {
      if("section".equals(element.tagName())) {
        headerSection = element;
        break;
      }
    }

    if(headerSection == null) {
      log.error("Could not find Article Header section for Sueddeutsche Article " + articleElement.baseUri());
      return null;
    }

    return extractReferenceFromHeaderSection(headerSection);
  }

  protected ReferenceSubDivision extractReferenceFromHeaderSection(Element headerSection) {
    ReferenceSubDivision articleReference = new ReferenceSubDivision();
    articleReference.setOnlineAddress(headerSection.baseUri());
    String articleDate = "";

    for(Element headerSectionChild : headerSection.children()) { // Header section has two children: time containing publishing time and a h2 element contain article title and subtitle
      if("time".equals(headerSectionChild.tagName()))
        articleDate = parseSueddeutscheHeaderDate(headerSectionChild.attributes().get("datetime"));
      if(headerSectionChild.tagName().startsWith("h")) {
        for(Node headerChild : headerSectionChild.childNodes()) {
          if(headerChild instanceof Element && "strong".equals(headerChild.nodeName()))
            articleReference.setSubTitle(((Element) headerChild).text().trim());
          else if(headerChild instanceof TextNode && StringUtils.isNotNullOrEmpty(headerChild.outerHtml().trim()))
            articleReference.setTitle(((TextNode)headerChild).text().trim());
        }
      }
    }

    Reference sueddeutscheDateReference = findOrCreateSueddeutscheReferenceForThatDate(articleDate);
    sueddeutscheDateReference.addSubDivision(articleReference);

    return articleReference;
  }

  protected DateFormat sueddeutscheHeaderDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

  protected String parseSueddeutscheHeaderDate(String datetime) {
    try {
      Date parsedDate = sueddeutscheHeaderDateFormat.parse(datetime);
      return DateFormat.getDateInstance(DateFormat.MEDIUM, Localization.getLanguageLocale()).format(parsedDate);
    } catch(Exception ex) { log.error("Could not parse Sueddeutsche Header Date " + datetime, ex); }
    return "";
  }

  private Reference findOrCreateSueddeutscheReferenceForThatDate(String articleDate) {
    SeriesTitle sueddeutsche = findOrCreateSueddeutscheSeriesTitle();
    for(Reference reference : sueddeutsche.getSerialParts()) {
      if(articleDate.equals(reference.getIssueOrPublishingDate()))
        return reference;
    }

    Reference sueddeutscheDateReference = new Reference();
    sueddeutscheDateReference.setIssueOrPublishingDate(articleDate);
//    Application.getDeepThought().addReference(sueddeutscheDateReference);

    sueddeutsche.addSerialPart(sueddeutscheDateReference);

    return sueddeutscheDateReference;
  }

  protected SeriesTitle cachedSueddeutscheSeriesTitle = null;

  protected SeriesTitle findOrCreateSueddeutscheSeriesTitle() {
    if(cachedSueddeutscheSeriesTitle != null)
      return cachedSueddeutscheSeriesTitle;

    for(SeriesTitle seriesTitle : Application.getDeepThought().getSeriesTitles()) {
      if("SZ".equals(seriesTitle.getTitle())) {
        cachedSueddeutscheSeriesTitle = seriesTitle;
        break;
      }
    }

    if(cachedSueddeutscheSeriesTitle == null) {
      cachedSueddeutscheSeriesTitle = new SeriesTitle("SZ");
      Application.getDeepThought().addSeriesTitle(cachedSueddeutscheSeriesTitle);

    }
    return cachedSueddeutscheSeriesTitle;
  }

}
