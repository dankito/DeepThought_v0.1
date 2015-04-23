package net.deepthought.data.importer_exporter;


import net.deepthought.Application;
import net.deepthought.data.model.Entry;
import net.deepthought.data.model.Reference;
import net.deepthought.data.model.SeriesTitle;
import net.deepthought.data.model.Tag;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import info.bliki.api.Page;
import info.bliki.api.User;
import info.bliki.wiki.model.WikiModel;

public class WikipediaImporter {

  protected ExecutorService threadPool = null;


  public WikipediaImporter() {
    threadPool = Executors.newCachedThreadPool();
  }


  public void importWikipediaArticles() {
//    String[] listOfTitleStrings = { "Web service" };
    info.bliki.api.User user = new info.bliki.api.User("", "", "http://de.wikipedia.org/w/api.php");
    user.login();

//    List<Page> listOfPages = user.queryContent(listOfTitleStrings);
//    for (Page page : listOfPages) {
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

//    List<Page> listOfPortals = user.queryContent(new String[] { "Portal:Geschichte"} );
//    for (Page page : listOfPortals) {
//      WikiModel wikiModel = new WikiModel("${image}", "${title}");
//      String html = wikiModel.render(page.toString());
//      System.out.println(html);
//    }

    List<Page> listOfPortals = user.queryContent(new String[]{"Portal:Byzanz/Index"});
    for (Page page : listOfPortals) {
      WikiModel wikiModel = new WikiModel("${image}", "${title}");
      String html = wikiModel.render(page.toString());
      System.out.println(html);
    }

    List<Page> listOfCategories = user.queryContent(new String[]{"Portal:Rom/Archiv"});
    for (Page page : listOfCategories) {
      WikiModel wikiModel = new WikiModel("${image}", "${title}");
      String html = wikiModel.render(page.toString());
      System.out.println(html);
    }

    List<Page> listOfThemes = user.queryContent(new String[]{"Portal:Rom/Themenliste"});
    for (Page page : listOfThemes) {
      WikiModel wikiModel = new WikiModel("${image}", "${title}");
      String html = wikiModel.render(page.toString());
      System.out.println(html);
    }

    List<Page> listOfFeaturedArticles = user.queryContent(new String[] { "Liste ägyptischer Götter"} );
    for (Page page : listOfFeaturedArticles) {
      WikiModel wikiModel = new WikiModel("${image}", "${title}");
      String html = wikiModel.render(page.toString());
      System.out.println(html);
    }
  }

  public void getEnglishFeaturedArticles() {
    User user = new User("", "", "http://en.wikipedia.org/w/api.php");
    user.login();

    Page featuredArticlesPage = user.queryContent(new String[] { "Wikipedia:Featured_articles" } ).get(0);
    String content = featuredArticlesPage.getCurrentContent();
    List<String> articles = getAllArticles(content);

    extractEntriesFromArticles(user, articles);
  }

  public void getGermanGeschichtsPortalArticles() {
    User user = new User("", "", "http://de.wikipedia.org/w/api.php");
    user.login();

    Page historyPortalPage = user.queryContent(new String[] { "Portal:Geschichte" } ).get(0);
    String content = historyPortalPage.getCurrentContent();
    List<String> articles = getAllArticles(content, true);

    extractEntriesFromArticles(user, articles);
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
    for(String articleName : articles) {
      if(articleName.startsWith("Portal:"))
        extractArticlesFromPortal(user, articleName);
      else {
        extractEntryFromArticleAsync(user, articleName);
      }
    }

    try { threadPool.awaitTermination(3, TimeUnit.HOURS); } catch(Exception ex) { }
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
    Entry entry = new Entry();

    Page articlePage = user.queryContent(new String[] { articleName } ).get(0);
//    String content = articlePage.getCurrentContent();
    WikiModel wikiModel = new WikiModel("${image}", "${title}");
    String html = wikiModel.render(articlePage.toString());

    entry.setContent(html);
    Application.getDeepThought().addEntry(entry);

    addArticleReferenceToEntry(articleName, entry);

    addTagsToEntry(articleName, entry);

    return entry;
  }

  protected void addArticleReferenceToEntry(String articleName, Entry entry) {
    SeriesTitle wikipedia = findOrCreateWikipediaSeriesTitle();

    Reference articleReference = new Reference(articleName);
    Application.getDeepThought().addReference(articleReference);

    wikipedia.addSerialPart(articleReference);

    entry.setReference(articleReference);
  }

  protected void addTagsToEntry(String articleName, Entry entry) {
    // add up to five random tags
    List<Tag> existingTags = new ArrayList<>(Application.getDeepThought().getTags());
    Random random = new Random(System.currentTimeMillis());
    int numberOfRandomTags = random.nextInt(4); // up to five tags

    if(existingTags.size() > 0) {
      for (int i = 0; i <= numberOfRandomTags; i++) {
        int tagIndex = random.nextInt(existingTags.size());
        entry.addTag(existingTags.get(tagIndex));
      }
    }

    // add a new Tag named to article name
    Tag newTag = new Tag(articleName);
    Application.getDeepThought().addTag(newTag);
    entry.addTag(newTag);
  }

  protected SeriesTitle cachedWikipediaSeriesTitle = null;

  protected SeriesTitle findOrCreateWikipediaSeriesTitle() {
    if(cachedWikipediaSeriesTitle == null) {
      for(SeriesTitle seriesTitle : Application.getDeepThought().getSeriesTitles()) {
        if("Wikipedia".equals(seriesTitle.getTitle())) {
          cachedWikipediaSeriesTitle = seriesTitle;
          break;
        }
      }

      if(cachedWikipediaSeriesTitle == null) {
        cachedWikipediaSeriesTitle = new SeriesTitle("Wikipedia");
        Application.getDeepThought().addSeriesTitle(cachedWikipediaSeriesTitle);
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

    Page featuredArticlesPage = user.queryContent(new String[] { portalName } ).get(0);
    String content = featuredArticlesPage.getCurrentContent();
    List<String> articles = getAllArticles(content, true);

    extractEntriesFromArticles(user, articles);
  }

}
