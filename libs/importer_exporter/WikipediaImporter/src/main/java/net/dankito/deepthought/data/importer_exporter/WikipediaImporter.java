package net.dankito.deepthought.data.importer_exporter;


import net.dankito.deepthought.Application;
import net.dankito.deepthought.data.model.Category;
import net.dankito.deepthought.data.model.DeepThought;
import net.dankito.deepthought.data.model.Entry;
import net.dankito.deepthought.data.model.Reference;
import net.dankito.deepthought.data.model.SeriesTitle;
import net.dankito.deepthought.data.model.Tag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import info.bliki.api.Page;
import info.bliki.api.PageInfo;
import info.bliki.api.User;
import info.bliki.htmlcleaner.TagNode;
import info.bliki.wiki.filter.HTMLConverter;
import info.bliki.wiki.filter.ITextConverter;
import info.bliki.wiki.model.IWikiModel;
import info.bliki.wiki.model.ImageFormat;
import info.bliki.wiki.model.WikiModel;

public class WikipediaImporter {

  private final Logger log = LoggerFactory.getLogger(WikipediaImporter.class);


  protected User user = null;

  protected WikiModel wikiModel = null;

  protected ITextConverter textConverter = null;

  protected ExecutorService threadPool = null;

  protected Set<String> extractedArticles = new ConcurrentSkipListSet<>();

  protected DeepThought deepThought = null;

  protected SeriesTitle cachedWikipediaSeriesTitle = null;

  protected List<Tag> createdTags = new ArrayList<>();

  protected Map<String, List<String>> articleCategories = new HashMap<>();

  protected Map<String, Category> createdCategories = new HashMap<>();

  Random random = new Random(System.currentTimeMillis());


  public WikipediaImporter() {
    deepThought = Application.getDeepThought();
    cachedWikipediaSeriesTitle = findOrCreateWikipediaSeriesTitle();

//    threadPool = Executors.newCachedThreadPool();
    threadPool = Executors.newFixedThreadPool(12);

    textConverter = new HTMLConverter() {
      @Override
      public void imageNodeToText(TagNode imageTagNode, ImageFormat imageFormat, Appendable resultBuffer, IWikiModel model) throws IOException {
        Map<String, String> attributes = imageTagNode.getAttributes();
        if(attributes.containsKey("src")) {
          String src = attributes.get("src");

          try {
            if (src.contains("/wiki/File:") == false)
              src = src.replace("/wiki/", "/wiki/File:");
            if (src.contains("px-")) {
              int startIndex = src.indexOf("/File:") + "/File:".length();
              int endIndex = src.indexOf("px-", startIndex) + "px-".length();
              String pixelsToRemove = src.substring(startIndex, endIndex);
              if (pixelsToRemove.length() < 6)
                src = src.replace(pixelsToRemove, "");
            }
            attributes.put("src", src);
          } catch(Exception ex) { log.warn("Could not adjust image source " + src, ex); }
        }

        super.imageNodeToText(imageTagNode, imageFormat, resultBuffer, model);
      }
    };
  }


  protected User loginUser(String languageCode) {
    String wikiDomain = "http://" + languageCode + ".wikipedia.org";

    user = new User("", "", wikiDomain + "/w/api.php");
    user.login();

    wikiModel = new WikiModel(wikiDomain + "/wiki/${image}", wikiDomain + "/wiki/${title}");

    return user;
  }

  public void testImportWikipediaArticles() {
    info.bliki.api.User user = loginUser("en");

    String[] listOfTitleStrings = { "Web service", "The Legend of Zelda: Link's Awakening", "Super Mario 64", "Borobudur" };

    List<Page> listOfPages = user.queryContent(listOfTitleStrings);
    for (Page page : listOfPages) {
//      WikiModel wikiModel = new WikiModel("${image}", "${title}");
      String html = wikiModel.render(page.getCurrentContent());
      System.out.println(html);

      String htmlWithTemplateTopic = wikiModel.render(page.getCurrentContent(), true);
      System.out.println(htmlWithTemplateTopic);

      String test = wikiModel.render(textConverter, page.getCurrentContent());

      String cleanedHtml = cleanTextBody(html);
      System.out.println();
      System.out.println("Cleaned:");
      System.out.println(cleanedHtml);
    }

//    user = loginUser("de");

//    List<Page> listOfPortals = user.queryContent(new String[] { "Portal:Geschichte"} );
//    for (Page page : listOfPortals) {
//      WikiModel wikiModel = new WikiModel("${image}", "${title}");
//      String html = wikiModel.render(page.toString());
//      System.out.println(html);
//    }

//    List<Page> listOfPortals = user.queryContent(new String[] { "Portal:Geschichte"} );
//    for (Page page : listOfPortals) {
//      WikiModel wikiModel = new WikiModel("${image}", "${title}");
//      String html = wikiModel.render(page.toString());
//      System.out.println(html);
//    }

//    List<Page> listOfPortals = user.queryContent(new String[]{"Portal:Byzanz/Index"});
//    for (Page page : listOfPortals) {
//      WikiModel wikiModel = new WikiModel("${image}", "${title}");
//      String html = wikiModel.render(page.toString());
//      System.out.println(html);
//    }
//
//    List<Page> listOfCategories = user.queryContent(new String[]{"Portal:Rom/Archiv"});
//    for (Page page : listOfCategories) {
//      WikiModel wikiModel = new WikiModel("${image}", "${title}");
//      String html = wikiModel.render(page.toString());
//      System.out.println(html);
//    }
//
//    List<Page> listOfThemes = user.queryContent(new String[]{"Portal:Rom/Themenliste"});
//    for (Page page : listOfThemes) {
//      WikiModel wikiModel = new WikiModel("${image}", "${title}");
//      String html = wikiModel.render(page.toString());
//      System.out.println(html);
//    }
//
//    List<Page> listOfFeaturedArticles = user.queryContent(new String[] { "Liste ägyptischer Götter"} );
//    for (Page page : listOfFeaturedArticles) {
//      WikiModel wikiModel = new WikiModel("${image}", "${title}");
//      String html = wikiModel.render(page.toString());
//      System.out.println(html);
//    }
  }

  public void getEnglishFeaturedArticles() {
    User user = loginUser("en");

    Page featuredArticlesPage = queryContent(user, new String[]{"Wikipedia:Featured_articles"}).get(0);

    String content = featuredArticlesPage.getCurrentContent();
    List<String> articles = getAllArticles(content);

    extractEntriesFromArticles(user, articles);
  }

  public void getGermanGeschichtsPortalArticles() {
    User user = loginUser("de");

    Page historyPortalPage = queryContent(user, new String[]{"Portal:Geschichte"}).get(0);
    String content = historyPortalPage.getCurrentContent();
    List<String> articles = getAllArticles(content, true);

    extractEntriesFromArticles(user, articles);
  }

  protected List<Page> queryContent(User user, String[] articlesToQuery) {
    return queryContent(user, Arrays.asList(articlesToQuery));
  }

  protected List<Page> queryContent(User user, List<String> articlesToQuery) {
//    return user.queryContent(articlesToQuery);

    String[] valuePairs = { "prop", "revisions", "rvprop", "timestamp|user|comment|content", "categories", "info", "links", "imageinfo", "iiprop", "url" };
    return user.getConnector().query(user, articlesToQuery, valuePairs);
  }

  protected List<String> getAllArticles(String content) {
    return getAllArticles(content, false);
  }

  protected List<String> getAllArticles(String content, boolean includePortals) {
    List<String> articleNames = new ArrayList<>();
    int indexArticleNameBegin = 0, indexArticleNameEnd = -2;

    while((indexArticleNameBegin = content.indexOf("[[", indexArticleNameEnd + 2)) > 0) {
      indexArticleNameEnd = content.indexOf("]]", indexArticleNameBegin + 1);
      String articleName = content.substring(indexArticleNameBegin + 2, indexArticleNameEnd);

      if(articleName.startsWith("Wikipedia:") || articleName.startsWith("File:") || articleName.startsWith("Datei:") ||  articleName.startsWith("#") ||
          articleName.startsWith("Special:") || articleName.startsWith("User:") || articleName.startsWith("Benutzer:") || articleName.startsWith("Wikipedia Diskussion:")) // no Categories
        continue;
      if(articleName.startsWith("Category:") || articleName.startsWith(":Category:") || articleName.startsWith("Kategorie:") || articleName.startsWith(":Kategorie:")) // no Categories
        continue;
      if(articleName.startsWith(":Diskussion:") || articleName.startsWith("Diskussion:") || articleName.startsWith("Bild:") || articleName.startsWith("WP:NKA"))
        continue;
      if(includePortals == false && articleName.startsWith("Portal:"))
        continue;

      if(articleName.contains("|"))
        articleName = articleName.substring(0, articleName.indexOf('|'));
      articleNames.add(articleName);
    }

    return articleNames;
  }

  protected void extractEntriesFromArticles(User user, List<String> articles) {
//    for(String articleName : articles) {
//      if(articleName.startsWith("Portal:"))
//        extractArticlesFromPortal(user, articleName);
//      else {
//        extractEntryFromArticle(user, articleName);
////        extractEntryFromArticleAsync(user, articleName);
//      }
//    }
//
////    try { threadPool.awaitTermination(20, TimeUnit.MINUTES); } catch(Exception ex) { }

    do {
      List<String> articlesToQueryInABunch = articles.subList(0, articles.size() < 50 ? articles.size() : 50); // only 50 titles are allowed per query
      List<Page> queriedPages = queryContent(user, articlesToQueryInABunch);
      queryArticlesCategories(user, articlesToQueryInABunch);

      for (Page page : queriedPages) {
        if (page.getTitle().startsWith("Portal:"))
          extractArticlesFromPortalPage(user, page);
        else
//          extractEntryFromPage(page);
          extractEntryFromPageAsync(page);
      }

      articles.removeAll(articlesToQueryInABunch);
    } while(articles.size() > 0) ;
  }

  protected void queryArticlesCategories(User user, List<String> articlesToQueryInABunch) {
    List<Page> queriedCategories = user.queryCategories(articlesToQueryInABunch);
    for(Page articlePage : queriedCategories) {
      if(articlePage.sizeOfCategoryList() > 0) {
        List<String> categories = new ArrayList<>();
        for(int i = 0; i < articlePage.sizeOfCategoryList(); i++) {
          PageInfo category = articlePage.getCategory(i);
          categories.add(category.getTitle().replace("Category:", "").replace("Kategorie,", ""));
        }
        articleCategories.put(articlePage.getTitle(), categories);
      }
    }
  }

  protected void extractEntryFromArticleAsync(final User user, final String articleName) {
    threadPool.execute(new Runnable() {
      @Override
      public void run() {
        extractEntryFromArticle(user, articleName);
      }
    });
  }

  protected Entry extractEntryFromArticle(User user, String articleName) {
    if(extractedArticles.contains(articleName))
      return null;
    extractedArticles.add(articleName);

    List<Page> pages = user.queryContent(new String[] { articleName } );
    if(pages.size() > 0) {
      Page articlePage = pages.get(0);
      return extractEntryFromPage(articlePage);
    }

    return null;
  }

  protected void extractEntryFromPageAsync(final Page articlePage) {
    threadPool.execute(new Runnable() {
      @Override
      public void run() {
        extractEntryFromPage(articlePage);
      }
    });
  }

  protected Entry extractEntryFromPage(Page articlePage) {
    String articleName = articlePage.getTitle();
    if(extractedArticles.contains(articleName))
      return null;
    extractedArticles.add(articleName);

    Entry entry = new Entry();

    //    String content = articlePage.getCurrentContent();
    String articleHtml = getPageHtmlContent(articlePage);

    entry.setContent(articleHtml);
    synchronized(deepThought) {
      deepThought.addEntry(entry);
    }

    addArticleReferenceToEntry(articleName, entry);

    addTagsToEntry(articleName, entry);

    addCategoriesToEntry(articleName, entry);

    log.debug("DeepThought now contains " + deepThought.getEntries().size() + " Entries");
    return entry;
  }

  protected String getPageHtmlContent(Page articlePage) {
//    WikiModel wikiModel = new WikiModel("${image}", "${title}");

    String html = "";
    synchronized(wikiModel) {
      html = wikiModel.render(textConverter, articlePage.getCurrentContent());
    }

    html = cleanTextBody(html);

    return html;
  }

  // found at http://www.programcreek.com/java-api-examples/index.php?api=info.bliki.wiki.model.WikiModel
  protected String cleanTextBody(String htmlStr) {
    htmlStr = htmlStr.replaceAll("n\\s*={2,}.+={2,}", "n");
    htmlStr = htmlStr.replaceAll("(?i)(\\{{2})(As of\\|)(.*?)(\\}{2})", "As of $3");
    htmlStr = htmlStr.replaceFirst("(\\\\n)+", "");
    htmlStr = htmlStr.replaceAll("(\\\\t)+", "").replaceAll("(\\\\n)+", "\\\\n").replaceAll("( *\\\\n)+", "\\\\n");

    String cleanedStr = htmlStr;

    while (cleanedStr.contains("</div")) {
      int backIndex = cleanedStr.indexOf("</div>");
      String tmp = cleanedStr.substring(0, backIndex);
      int index = tmp.lastIndexOf("<div");
      cleanedStr = cleanedStr.substring(0, index) + cleanedStr.substring(backIndex + 6);
    }

    cleanedStr = cleanedStr.replaceAll("\\{{2}.*?\\}{2}", "");
    cleanedStr = cleanedStr.replaceAll("\\{\\| *class.*?\\|\\}", "Table is removed!");
    cleanedStr = cleanedStr.replaceAll("<sup[^>]*><a[^>]*>\\[\\d*\\]</a></sup>", "");
    cleanedStr = cleanedStr.replaceAll("\\\\\\&#34;", "&#34;");
    cleanedStr = cleanedStr.replaceAll("\\n", "");
    cleanedStr = cleanedStr.replaceAll("(\\\\n)+", "\\\\n");
    cleanedStr = cleanedStr.replaceFirst("<p>(\\\\n)+", "<p>");
    cleanedStr = cleanedStr.replaceAll("(\\\\n)+", "<br/>");
    cleanedStr = cleanedStr.replaceAll("(<br/>)+", "<br/>");
    cleanedStr = cleanedStr.replaceAll("(<br/>)+", "<p>");
    if (cleanedStr.equals("<p></p>")) {
      cleanedStr = "";
    }
    return cleanedStr;
  }


  protected void addArticleReferenceToEntry(String articleName, Entry entry) {
    SeriesTitle wikipedia = cachedWikipediaSeriesTitle;

    Reference articleReference = new Reference(articleName);
    synchronized(deepThought) {
      deepThought.addReference(articleReference);
    }

    if(articleReference.isPersisted()) {
      wikipedia.addSerialPart(articleReference);

      entry.setReference(articleReference);
    }
    else
      log.warn("Could not persist Reference " + articleReference);
  }

  protected void addTagsToEntry(String articleName, Entry entry) {
    if(entry.getEntryIndex() % 5 == 0) // every fifth entry does get any tag to test getAllEntriesWithoutTags functionality
      return;

    // add up to 20 random tags
    if(createdTags.size() > 0) {
      int numberOfRandomTags = random.nextInt(19); // up to 20 tags
      for (int i = 0; i <= numberOfRandomTags; i++) {
        int tagIndex = random.nextInt(createdTags.size());
        entry.addTag(createdTags.get(tagIndex));
      }
    }

    // add a new Tag named to article name
    Tag newTag = new Tag(articleName);
    synchronized(deepThought) {
      deepThought.addTag(newTag);
    }

    if(newTag.isPersisted() == false)
      log.warn("Could not persist Tag " + newTag);
    else
      createdTags.add(newTag);

    entry.addTag(newTag);
    if(newTag.getEntries().contains(entry) == false) {
      log.warn("Could not add Tag " + newTag + " to Entry " + entry);
    }
  }

  protected void addCategoriesToEntry(String articleName, Entry entry) {
    if(articleCategories.containsKey(articleName)) {
      List<String> categories = articleCategories.remove(articleName);
      for(String categoryName : categories) {
        if(createdCategories.containsKey(categoryName))
          entry.addCategory(createdCategories.get(categoryName));
        else {
          Category newCategory = new Category(categoryName);
          synchronized(deepThought) {
            deepThought.addCategory(newCategory);
            if(newCategory.isPersisted())
              entry.addCategory(newCategory);
          }

          if(newCategory.isPersisted())
            createdCategories.put(categoryName, newCategory);
          else
            log.warn("Could not persist new category " + newCategory);
        }
      }
    }
  }

  protected SeriesTitle findOrCreateWikipediaSeriesTitle() {
    if(cachedWikipediaSeriesTitle == null) {
      for(SeriesTitle seriesTitle : deepThought.getSeriesTitles()) {
        if("Wikipedia".equals(seriesTitle.getTitle())) {
          cachedWikipediaSeriesTitle = seriesTitle;
          break;
        }
      }

      if(cachedWikipediaSeriesTitle == null) {
        cachedWikipediaSeriesTitle = new SeriesTitle("Wikipedia");
        deepThought.addSeriesTitle(cachedWikipediaSeriesTitle);
      }
    }

    return cachedWikipediaSeriesTitle;
  }

  protected List<String> parsedPortals = new ArrayList<>();

  protected void extractArticlesFromPortal(User user, String portalName) {
    if(portalName.endsWith("Fehlende Artikel") || portalName.startsWith("Portal Diskussion:") || portalName.endsWith("/Kategorien") ||
        portalName.endsWith("Projekt"))
      return;

    if(parsedPortals.contains(portalName)) // don't parse Portal twice
      return;
    parsedPortals.add(portalName);

    Page portalPage = user.queryContent(new String[] { portalName } ).get(0);
    extractArticlesFromPortalPage(user, portalPage);
  }

  protected void extractArticlesFromPortalPage(User user, Page portalPage) {
    String portalName = portalPage.getTitle();
    if(portalName.endsWith("Fehlende Artikel") || portalName.startsWith("Portal Diskussion:") || portalName.endsWith("/Kategorien") ||
        portalName.endsWith("Projekt"))
      return;

    if(parsedPortals.contains(portalName)) // don't parse Portal twice
      return;
    parsedPortals.add(portalName);

    String content = portalPage.getCurrentContent();
    List<String> articles = getAllArticles(content, true);

    extractEntriesFromArticles(user, articles);
  }

}
