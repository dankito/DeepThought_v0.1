package net.dankito.deepthought.data.contentextractor;

import net.dankito.deepthought.Application;
import net.dankito.deepthought.data.contentextractor.preview.ArticlesOverviewItem;
import net.dankito.deepthought.data.contentextractor.preview.ArticlesOverviewListener;
import net.dankito.deepthought.data.model.Category;
import net.dankito.deepthought.data.model.Reference;
import net.dankito.deepthought.data.model.ReferenceSubDivision;
import net.dankito.deepthought.data.model.SeriesTitle;
import net.dankito.deepthought.data.model.Tag;
import net.dankito.deepthought.plugin.IPlugin;
import net.dankito.deepthought.util.StringUtils;
import net.dankito.deepthought.util.localization.Localization;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.security.CodeSource;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public abstract class OnlineNewspaperContentExtractorBase extends OnlineArticleContentExtractorBase implements IPlugin {

  public final static String DefaultUserAgent = "Mozilla/5.0 (Windows NT 6.3; rv:36.0) Gecko/20100101 Firefox/36.0";


  private final static Logger log = LoggerFactory.getLogger(OnlineNewspaperContentExtractorBase.class);


  public abstract String getNewspaperName();

  public String getIconUrl() {
    return IOnlineArticleContentExtractor.NoIcon;
  }

  protected String tryToLoadIconFile(String logoFilename) {
    String logoResourcePath = new File("logos", logoFilename).getPath();

    try {
      URL url = OnlineNewspaperContentExtractorBase.class.getClassLoader().getResource(logoResourcePath);
      return url.toExternalForm();
      //return url.toString();
    } catch(Exception ex) {
      String iconFile = tryToManuallyLoadIcon(OnlineNewspaperContentExtractorBase.class, logoResourcePath);
      if (iconFile != IOnlineArticleContentExtractor.NoIcon)
        return iconFile;
      else
        log.error("Could not load " + logoFilename + " from Resources", ex);
    }

    return IOnlineArticleContentExtractor.NoIcon;
  }


  @Override
  public ContentExtractOptions createExtractOptionsForUrl(String url) {
    ContentExtractOptions options = new ContentExtractOptions(url, getSiteBaseUrl());

    options.addContentExtractOption(new ContentExtractOption(this, url, true, "create.entry.from.online.article", new ExtractContentAction() {
      @Override
      public void runExtraction(ContentExtractOption option, ExtractContentActionResultListener listener) {
        listener.extractingContentDone(createEntryFromArticle(option.getUrl()));
      }
    }));

    return options;
  }

  public boolean hasArticlesOverview() {
    return false;
  }

  @Override
  public void getArticlesOverviewAsync(final ArticlesOverviewListener listener) {
    if(hasArticlesOverview()) {
      Application.getThreadPool().runTaskAsync(new Runnable() {
        @Override
        public void run() {
          getArticlesOverview(listener);
        }
      });
    }
    else
      listener.overviewItemsRetrieved(this, new ArrayList<ArticlesOverviewItem>(), true);
  }

  protected void getArticlesOverview(ArticlesOverviewListener listener) {
    // may be overwritten in subclass (if hasArticlesOverview() is set to true)
  }

  @Override
  public String getName() {
    return Localization.getLocalizedString("named.content.extractor", getNewspaperName());
  }


  protected void setArticleReference(EntryCreationResult creationResult, ReferenceSubDivision articleReference, String articleDate) {
    creationResult.setReferenceSubDivision(articleReference);

    SeriesTitle newspaperSeries = findOrCreateNewspaperSeries();
    creationResult.setSeriesTitle(newspaperSeries);

    if(newspaperSeries != null && StringUtils.isNotNullOrEmpty(articleDate)) {
      Reference dateReference = findOrCreateReferenceForDate(newspaperSeries, articleDate);
      creationResult.setReference(dateReference);
    }
  }

  public SeriesTitle findOrCreateNewspaperSeries() {
    return Application.getEntitiesSearcherAndCreator().findOrCreateSeriesTitleForTitle(getNewspaperName());
  }

  protected Reference findOrCreateReferenceForDate(SeriesTitle newspaperSeries, String articleDate) {
    return Application.getEntitiesSearcherAndCreator().findOrCreateReferenceForDate(newspaperSeries, articleDate);
  }

  protected void findOrCreateTagAndAddToCreationResult(EntryCreationResult creationResult) {
    findOrCreateTagAndAddToCreationResult(creationResult, getNewspaperName());
  }

  protected void findOrCreateTagAndAddToCreationResult(EntryCreationResult creationResult, String newspaperName) {
    Tag newspaperTag = findOrCreateTagForName(newspaperName);
    creationResult.addTag(newspaperTag);
  }

  protected Tag findOrCreateTagForName(String tagName) {
    return Application.getEntitiesSearcherAndCreator().findOrCreateTagForName(tagName);
  }

  protected void addNewspaperCategory(EntryCreationResult creationResult, boolean isOnlineArticle) {
    addNewspaperCategory(creationResult, getNewspaperName(), isOnlineArticle);
  }

  protected void addNewspaperCategory(EntryCreationResult creationResult, String newspaperName, boolean isOnlineArticle) {
    // TODO: here sub categories getting added directly to their (may already saved) parent categories and so also get stored in database whether user likes to save this Article
    // or not, but i can live with that right now
    // Currently there's no other way to solve it as if parent category doesn't get set, on save it gets added to TopLevelCategory -> it will be added a lot of times
    Category periodicalsCategory = Application.getEntitiesSearcherAndCreator().findOrCreateTopLevelCategoryForName(Localization.getLocalizedString("periodicals"));
    if(periodicalsCategory.isPersisted() == false && Application.getDeepThought() != null) {
      Application.getDeepThought().addCategory(periodicalsCategory);
    }

    Category newspaperCategory = Application.getEntitiesSearcherAndCreator().findOrCreateSubCategoryForName(periodicalsCategory, newspaperName);
    if(newspaperCategory.isPersisted() == false && Application.getDeepThought() != null) {
      Application.getDeepThought().addCategory(newspaperCategory);
      periodicalsCategory.addSubCategory(newspaperCategory);
    }

    if(isOnlineArticle == false)
      creationResult.addCategory(newspaperCategory);
    else {
      Category newspaperOnlineCategory = Application.getEntitiesSearcherAndCreator().findOrCreateSubCategoryForName(newspaperCategory, newspaperName + " " + Localization.getLocalizedString("online"));
      if(newspaperOnlineCategory.isPersisted() == false && Application.getDeepThought() != null) {
        Application.getDeepThought().addCategory(newspaperOnlineCategory);
        newspaperCategory.addSubCategory(newspaperOnlineCategory);
      }

      creationResult.addCategory(newspaperOnlineCategory);
    }
  }


  protected Element getFirstElementWithNodeName(Element parentElement, String nodeName) {
    Elements classElements = parentElement.getElementsByTag(nodeName);
    if(classElements.size() > 0) {
      return classElements.get(0);
    }

    log.error("Could not find a <" + nodeName + "> node for parent Element " + parentElement);
    return null;
  }

  protected Element getElementByClassAndNodeName(Element parentElement, String nodeName, String className) {
    Elements classElements = parentElement.getElementsByClass(className);
    for(Element classElement : classElements) {
      if(nodeName.equals(classElement.nodeName())) {
        return classElement;
      }
    }

    log.error("Could not find a <" + nodeName + "> node of class " + className);
    return null;
  }

  protected String getElementHtmlByClassAndNodeName(Element parentElement, String nodeName, String className) {
    Element element = getElementByClassAndNodeName(parentElement, nodeName, className);
    if(element != null)
      return element.html();

    return "";
  }

  protected String getElementOwnTextByClassAndNodeName(Element parentElement, String nodeName, String className) {
    Element element = getElementByClassAndNodeName(parentElement, nodeName, className);
    if(element != null)
      return element.ownText();

    return "";
  }

  protected String tryToManuallyLoadIcon(Class classInJarWithIcon, String iconName) {
    try {
      CodeSource source = classInJarWithIcon.getProtectionDomain().getCodeSource();
      URL codeLocation = source.getLocation();
      String location = codeLocation.toExternalForm();
      location = location.replace("/classes/main/", "/resources/main/");
      if(location.startsWith("file:"))
        location = location.substring("file:".length());

      File iconFile = new File(location, iconName);
      if(iconFile.exists())
        return iconFile.toURI().toURL().toExternalForm();
    } catch(Exception ex2) { }

    return IOnlineArticleContentExtractor.NoIcon;
  }


  protected DateFormat isoDateTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

  protected DateFormat isoDateTimeFormatWithoutTimezone = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

  protected String parseIsoDateTimeString(String isoDateTimeString, String publishingDateString) {
    try {
      if(':' == isoDateTimeString.charAt(isoDateTimeString.length() - 3)) { // remove colon from time zone, Java DateFormat is not able to parse it
        isoDateTimeString = isoDateTimeString.substring(0, isoDateTimeString.length() - 3) + isoDateTimeString.substring(isoDateTimeString.length() - 2);
      }

      Date publishingDate = isoDateTimeFormat.parse(isoDateTimeString);
      if (publishingDate != null) {
        publishingDateString = formatDateToDeepThoughtDateString(publishingDate);
      }
    } catch(Exception ex) { }
    return publishingDateString;
  }

  protected String parseIsoDateTimeWithoutTimezoneStringWithoutTimezone(String isoDateTimeString , String publishingDateString) {
    try {
      Date publishingDate = isoDateTimeFormatWithoutTimezone.parse(isoDateTimeString);
      if (publishingDate != null) {
        publishingDateString = formatDateToDeepThoughtDateString(publishingDate);
      }
    } catch(Exception ex) { }
    return publishingDateString;
  }


  protected String formatDateToDeepThoughtDateString(Date parsedDate) {
    return DateFormat.getDateInstance(Reference.PublishingDateFormat, Localization.getLanguageLocale()).format(parsedDate);
  }

}
