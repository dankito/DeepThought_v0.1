package net.deepthought.data.contentextractor;

import net.deepthought.Application;
import net.deepthought.data.contentextractor.preview.ArticlesOverviewItem;
import net.deepthought.data.contentextractor.preview.ArticlesOverviewListener;
import net.deepthought.data.model.Category;
import net.deepthought.data.model.Reference;
import net.deepthought.data.model.ReferenceSubDivision;
import net.deepthought.data.model.SeriesTitle;
import net.deepthought.data.model.Tag;
import net.deepthought.util.Localization;
import net.deepthought.util.StringUtils;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

public abstract class OnlineNewspaperContentExtractorBase extends OnlineArticleContentExtractorBase {

  public final static String DefaultUserAgent = "Mozilla/5.0 (Windows NT 6.3; rv:36.0) Gecko/20100101 Firefox/36.0";


  private final static Logger log = LoggerFactory.getLogger(OnlineNewspaperContentExtractorBase.class);


  public abstract String getNewspaperName();

  public String getIconUrl() {
    return IOnlineArticleContentExtractor.NoIcon;
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
      Reference dateReference = findOrCreateReferenceForThatDate(newspaperSeries, articleDate);
      creationResult.setReference(dateReference);
    }
  }

  public SeriesTitle findOrCreateNewspaperSeries() {
    SeriesTitle newspaperSeries = null;

    if(Application.getDeepThought() != null) {
      newspaperSeries = Application.getDeepThought().findOrCreateSeriesTitleForTitle(getNewspaperName());
    }

    return newspaperSeries;
  }

  protected Reference findOrCreateReferenceForThatDate(SeriesTitle newspaperSeries, String articleDate) {
    if(newspaperSeries != null) {
      for (Reference reference : newspaperSeries.getSerialParts()) {
        if (articleDate.equals(reference.getIssueOrPublishingDate()))
          return reference;
      }
    }

    Reference newspaperDateReference = new Reference();
    newspaperDateReference.setIssueOrPublishingDate(articleDate);

    return newspaperDateReference;
  }

  protected void addNewspaperTag(EntryCreationResult creationResult) {
    addNewspaperTag(creationResult, getNewspaperName());
  }

  protected void addNewspaperTag(EntryCreationResult creationResult, String newspaperName) {
    Tag newspaperTag = Application.getDeepThought().findOrCreateTagForName(newspaperName);
    creationResult.addTag(newspaperTag);
  }

  protected void addNewspaperCategory(EntryCreationResult creationResult, boolean isOnlineArticle) {
    addNewspaperCategory(creationResult, getNewspaperName(), isOnlineArticle);
  }

  protected void addNewspaperCategory(EntryCreationResult creationResult, String newspaperName, boolean isOnlineArticle) {
    // TODO: here sub categories getting added directly to their (may already saved) parent categories and so also get stored in database whether user likes to save this Article
    // or not, but i can live with that right now
    Category periodicalsCategory = Application.getDeepThought().findOrCreateTopLevelCategoryForName(Localization.getLocalizedString("periodicals"));
    Category newspaperCategory = Application.getDeepThought().findOrCreateSubCategoryForName(periodicalsCategory, newspaperName);

    if(isOnlineArticle == false)
      creationResult.addCategory(newspaperCategory);
    else {
      Category newspaperOnlineCategory = Application.getDeepThought().findOrCreateSubCategoryForName(newspaperCategory, newspaperName + " " + Localization.getLocalizedString("online"));
      creationResult.addCategory(newspaperOnlineCategory);
    }
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

  protected String formatDateToDeepThoughtDateString(Date parsedDate) {
    return DateFormat.getDateInstance(DateFormat.MEDIUM, Localization.getLanguageLocale()).format(parsedDate);
  }

}
