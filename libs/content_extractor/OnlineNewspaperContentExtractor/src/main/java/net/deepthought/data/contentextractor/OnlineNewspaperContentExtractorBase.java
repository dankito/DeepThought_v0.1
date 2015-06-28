package net.deepthought.data.contentextractor;

import net.deepthought.Application;
import net.deepthought.data.model.Category;
import net.deepthought.data.model.Entry;
import net.deepthought.data.model.Reference;
import net.deepthought.data.model.SeriesTitle;
import net.deepthought.data.model.Tag;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class OnlineNewspaperContentExtractorBase extends OnlineArticleContentExtractorBase {

  public final static String DefaultUserAgent = "Mozilla/5.0 (Windows NT 6.3; rv:36.0) Gecko/20100101 Firefox/36.0";


  private final static Logger log = LoggerFactory.getLogger(OnlineNewspaperContentExtractorBase.class);

  protected Reference findOrCreateReferenceForThatDate(String newspaperTitle, String articleDate) {
    SeriesTitle newspaperSeries = null;

    if(Application.getDeepThought() != null) {
      newspaperSeries = Application.getDeepThought().findOrCreateSeriesTitleForTitle(newspaperTitle);
      for (Reference reference : newspaperSeries.getSerialParts()) {
        if (articleDate.equals(reference.getIssueOrPublishingDate()))
          return reference;
      }
    }

    Reference newspaperDateReference = new Reference();
    newspaperDateReference.setIssueOrPublishingDate(articleDate);
//    Application.getDeepThought().addReference(newspaperDateReference);

    if(newspaperDateReference != null)
      newspaperSeries.addSerialPart(newspaperDateReference);

    return newspaperDateReference;
  }

  protected void addTags(Entry articleEntry, String newspaperName) {
    Tag newspaperTag = Application.getDeepThought().findOrCreateTagForName(newspaperName);
    articleEntry.addTag(newspaperTag);
  }

  protected void addCategory(Entry articleEntry, String newspaperName, boolean isOnlineArticle) {
    Category periodicalsCategory = Application.getDeepThought().findOrCreateTopLevelCategoryForName("Periodika");
    Category newspaperCategory = Application.getDeepThought().findOrCreateSubCategoryForName(periodicalsCategory, newspaperName);

    if(isOnlineArticle == false)
      newspaperCategory.addEntry(articleEntry);
    else {
      Category newspaperOnlineCategory = Application.getDeepThought().findOrCreateSubCategoryForName(newspaperCategory, newspaperName + " Online");
      newspaperOnlineCategory.addEntry(articleEntry);
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

}
