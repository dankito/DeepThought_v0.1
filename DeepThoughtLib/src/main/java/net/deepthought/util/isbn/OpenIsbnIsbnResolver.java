package net.deepthought.util.isbn;

import net.deepthought.data.html.IHtmlHelper;
import net.deepthought.data.model.FileLink;
import net.deepthought.data.model.Person;
import net.deepthought.data.model.Reference;
import net.deepthought.util.IThreadPool;
import net.deepthought.util.localization.Localization;
import net.deepthought.util.StringUtils;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * Created by ganymed on 05/12/15.
 */
public class OpenIsbnIsbnResolver extends IsbnResolverBase implements IIsbnResolver {

  private static final Logger log = LoggerFactory.getLogger(OpenIsbnIsbnResolver.class);


  protected static final String QueryIsbnBaseUrl = "http://openisbn.com/isbn/";


  protected DateFormat dateFormatter;


  public OpenIsbnIsbnResolver(IHtmlHelper htmlHelper, IThreadPool threadPool) {
    super(htmlHelper, threadPool);
    dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
  }


  protected String getQueryIsbnUrl(String isbn) {
    return QueryIsbnBaseUrl + isbn;
  }

  protected Reference parseResponseToReference(Document receivedResponse) {
    if(receivedResponse.title().contains("404 - Page Not Found") || receivedResponse.title().contains("404 - Page Not Found")) {
      throw new IllegalArgumentException(Localization.getLocalizedString("no.information.found.for.this.isbn"));
    }

    Element body = receivedResponse.body();

    String title = parseTitle(body);
    Reference reference = new Reference(title);

    Elements postContentCollection = receivedResponse.select("div .Article div .PostContent");
    if(postContentCollection.size() > 0) {
      parseContentElementValues(postContentCollection.get(0), reference);
    }

    return reference;
  }

  protected String parseTitle(Element body) {
    String title = "";
    Elements titleHeaderCollection = body.select("div.PostHead");

    if(titleHeaderCollection.size() > 0) {
      title = titleHeaderCollection.get(0).child(0).text();
    }

    return title;
  }

  protected void parseContentElementValues(Element content, Reference reference) {
    parseContentChildNodes(content, reference);

    parseCover(content, reference);
  }

  protected void parseContentChildNodes(Element content, Reference reference) {
    for(Node node : content.childNodes()) {
      if(node instanceof TextNode) {
        String text = ((TextNode)node).text();

        if (text.startsWith("Author:") || text.startsWith("Authors:")) {
          parseAuthors((TextNode)node, reference);
        }
        else if (text.startsWith("Pages:")) {
          reference.setLength(text.replace("Pages: ", ""));
        }
        else if (text.startsWith("Published:")) {
          String publishingDate = text.replace("Published: ", "");
          try {
            reference.setPublishingDate(dateFormatter.parse(publishingDate));
          } catch (Exception ex) {
            log.warn("Could not parse " + publishingDate + " to Publishing Date", ex);
          }
        }
        // also available: Publisher:, Keywords:, Language:, Binding:, List Price:
      }
      else if(node instanceof Element) {
        Element element = (Element)node;
        if("a".equals(element.tagName()) && element.attr("href").startsWith("/author/")) {
          parseAuthor(element.text(), reference);
        }
      }
    }
  }

  protected void parseAuthors(TextNode authorsNode, Reference reference) {
    String authorString = authorsNode.text().replace("Author: ", "").replace("Authors: ", "");
    if(StringUtils.isNotNullOrEmpty(authorString)) {
      parseAuthorString(reference, authorString);
    }
  }

  protected void parseAuthorString(Reference reference, String authorString) {
    if(authorString.contains(", ")) {
      String[] authors = authorString.split(", ");
      for(String author : authors) {
        parseAuthor(author, reference);
      }
    }
    else {
      parseAuthor(authorString, reference);
    }
  }

  protected boolean isAuthorLinkElement(Node node) {
    if(node instanceof Element) {
      Element element = (Element)node;
      return "a".equals(element.tagName()) && element.attr("href").startsWith("/author/");
    }
    else if(node instanceof TextNode && ", ".equals(((TextNode)node).text())) { // Authors are separated by ", "
      return isAuthorLinkElement(node.nextSibling());
    }

    return false;
  }

  protected void parseAuthor(String author, Reference reference) {
    int indexOfLastWhiteSpace = author.trim().lastIndexOf(' ');

    if (indexOfLastWhiteSpace > 0) {
      Person person = new Person(author.substring(0, indexOfLastWhiteSpace).trim(), author.substring(indexOfLastWhiteSpace).trim());
      reference.addPerson(person);
    }
    else if (StringUtils.isNotNullOrEmpty(author)) {
      reference.addPerson(new Person("", author));
    }
  }

  protected void parseCover(Element content, Reference reference) {
    Elements imgElements = content.select("img");
    if(imgElements.size() > 0) {
      Element previewImageElement = imgElements.get(0);
      String previewImageUrl = previewImageElement.attr("src");

      String name = reference.getTitle();
      if(previewImageElement.hasAttr("title") && StringUtils.isNotNullOrEmpty(previewImageElement.attr("title"))) {
        name = previewImageElement.attr("title");
      }

      reference.setPreviewImage(new FileLink(previewImageUrl, name));
    }
  }

}
