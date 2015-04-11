package com.example;

import net.deepthought.Application;
import net.deepthought.data.model.Category;
import net.deepthought.data.model.Entry;
import net.deepthought.data.model.Reference;
import net.deepthought.data.model.ReferenceBase;
import net.deepthought.data.model.ReferenceSubDivision;
import net.deepthought.data.model.SeriesTitle;
import net.deepthought.util.StringUtils;

import org.apache.xerces.dom.TextImpl;
import org.odftoolkit.odfdom.dom.element.style.StyleTextPropertiesElement;
import org.odftoolkit.odfdom.dom.element.text.TextAElement;
import org.odftoolkit.odfdom.dom.element.text.TextLineBreakElement;
import org.odftoolkit.odfdom.dom.element.text.TextParagraphElementBase;
import org.odftoolkit.odfdom.dom.element.text.TextSoftPageBreakElement;
import org.odftoolkit.odfdom.dom.element.text.TextSpanElement;
import org.odftoolkit.odfdom.incubator.doc.style.OdfStyle;
import org.odftoolkit.odfdom.incubator.doc.text.OdfTextSpan;
import org.odftoolkit.simple.TextDocument;
import org.odftoolkit.simple.text.Paragraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class OpenOfficeDocumentsImporterExporter {

  private final static Logger log = LoggerFactory.getLogger(OpenOfficeDocumentsImporterExporter.class);


  public String extractPlainTextFromTextDocument(String documentPath) {
    String extractedText = "CouldNotExtractText";
    TextDocument textDocument = null;
    try {
      textDocument = TextDocument.loadDocument(documentPath);
      if(textDocument == null) // Media Type not supported by Simple Odf
        return extractedText;

      extractedText = "";
      Iterator<Paragraph> paragraphs = textDocument.getParagraphIterator();
      while(paragraphs.hasNext()) {
        Paragraph paragraph = paragraphs.next();
        extractedText += paragraph.getTextContent() + System.lineSeparator();
      }

      if(extractedText.length() > 0)
        extractedText = extractedText.substring(0, extractedText.length() - System.lineSeparator().length());
    } catch (Exception ex) {
      log.error("Could not load text document " + documentPath, ex);
    }

    if(textDocument != null)
      textDocument.close();
    return extractedText;
  }


  public List<Entry> extractEntriesFromDankitosSchneisenImWald(String documentPath) {
    List<Entry> extractedEntries = new ArrayList<>();
    TextDocument textDocument = null;
    try {
      textDocument = TextDocument.loadDocument(documentPath);
      if(textDocument == null) // Media Type not supported by Simple Odf
        return extractedEntries;

      Iterator<Paragraph> paragraphs = textDocument.getParagraphIterator();
      extractedEntries = extractEntriesFromParagraphs(paragraphs, extractedEntries);
    } catch (Exception ex) {
      log.error("Could not load text document " + documentPath, ex);
    }

    if(textDocument != null)
      textDocument.close();
    return extractedEntries;
  }

  protected List<Entry> extractEntriesFromParagraphs(Iterator<Paragraph> paragraphs, List<Entry> extractedEntries) throws URISyntaxException {
    List<Paragraph> entryParagraphs = null;
    for(entryParagraphs = getEntryParagraphs(paragraphs); entryParagraphs.size() > 0; entryParagraphs = getEntryParagraphs(paragraphs)) {
      try {
        Entry currentEntry = new Entry();
        ReferenceBase currentReference = tryToExtractReference(entryParagraphs);
        if(currentReference instanceof ReferenceSubDivision)
          currentEntry.setReferenceSubDivision((ReferenceSubDivision)currentReference);
        else if(currentReference instanceof Reference)
          currentEntry.setReference((Reference)currentReference);

        currentEntry.setContent(extractEntryContent(entryParagraphs));
        Application.getDeepThought().addEntry(currentEntry);
        addEntryToCategory(currentEntry);
        extractedEntries.add(currentEntry);
      } catch(Exception ex) {
        log.error("Could not extract Entry from entryParagraphs", ex);
      }
    }

    return extractedEntries;
  }

  protected String extractEntryContent(List<Paragraph> entryParagraphs) {
    String entryContent = "";
    
    for(Paragraph paragraph : entryParagraphs) {
      String paragraphHtml = extractHtmlFormattedContentFromParagraph(paragraph);
      entryContent += "<p>" + paragraphHtml + "</p>";
    }

    return entryContent;
  }

  protected String extractHtmlFormattedContentFromParagraph(Paragraph paragraph) {
    String paragraphHtml = "";

    NodeList paragraphChildren = paragraph.getOdfElement().getChildNodes();
    for(int i = 0; i < paragraphChildren.getLength(); i++) {
      Node block = paragraphChildren.item(i);

      if(block instanceof TextSoftPageBreakElement) // a manual page break on text overflow, not needed in HTML
        continue;
      else if(block instanceof TextLineBreakElement)
        paragraphHtml += "<br />";
      else if(block instanceof TextSpanElement) {
        TextSpanElement element = (TextSpanElement)block;
        if(element.hasAutomaticStyle() || element.hasDocumentStyle()) {
          paragraphHtml = extractStyledBlockContent(element);
        }
        else
          paragraphHtml += block.getTextContent();
      }
      else if(block instanceof TextAElement) {
        TextAElement textHyperlink = (TextAElement)block;
        paragraphHtml += "<a href=\"" + textHyperlink.getXlinkHrefAttribute() + "\">" + textHyperlink.getTextContent() + "</a>";
      }
      else
        paragraphHtml += block.getTextContent();
    }

    return paragraphHtml;
  }

  protected String extractStyledBlockContent(TextSpanElement element) {
    OdfStyle style = element.hasAutomaticStyle() ? element.getAutomaticStyle() : element.getDocumentStyle();
    if(style.getFirstChild() instanceof StyleTextPropertiesElement) { // TODO: also check other children for StyleTextPropertiesElement
      StyleTextPropertiesElement textPropertiesElement = (StyleTextPropertiesElement)style.getFirstChild();
      // TODO: also check for: underline, strike through, text fore- and background color, ...
      boolean isBold = textPropertiesElement.getFoFontWeightAttribute() != null && textPropertiesElement.getFoFontWeightAttribute().contains("bold");
      boolean isItalic = textPropertiesElement.getFoFontStyleAttribute() != null && textPropertiesElement.getFoFontStyleAttribute().contains("italic");

      String blockHtml = "";
      if(isBold) blockHtml += "<b>";
      if(isItalic) blockHtml += "<i>";

      blockHtml += element.getTextContent();

      if(isItalic) blockHtml += "</i>";
      if(isBold) blockHtml += "</b>";

      return blockHtml;
    }
    else
      return element.getTextContent();
  }

  protected ReferenceBase tryToExtractReference(List<Paragraph> entryParagraphs) {
    String referenceText = tryToFindReferenceText(entryParagraphs);
    if(referenceText == null) // no reference specified in this case
      return null;

    return tryToFindReferenceTitleSubtitleAndPublishingDate(entryParagraphs, referenceText);
  }

  protected String tryToFindReferenceText(List<Paragraph> entryParagraphs) {
    // i indicate a reference by a line break on last paragraph and then place the reference text between two square brackets
    Paragraph lastParagraph = entryParagraphs.get(entryParagraphs.size() - 1);
    TextParagraphElementBase paragraphElement = lastParagraph.getOdfElement();
    NodeList children = paragraphElement.getChildNodes();
    int length = children.getLength();
    if(length > 2) {
      Node lastChild = children.item(length - 1);
      if(/*lastChild instanceof TextImpl &&*/ /*"]".equals(lastChild.getTextContent())*/ lastChild.getTextContent().endsWith("]")) { // paragraph end with a closing square bracket
        Integer lineBreakElementIndex = findLineBreakElement(children, length - 2);
        if(lineBreakElementIndex == null)
          return null;

        Integer openingSquareBracketIndex = findOpeningSquareBracketIndex(children, lineBreakElementIndex + 1, length /*- 1*/);
        if(openingSquareBracketIndex == null)
          return null;

        return extractReferenceTextFromParagraph(paragraphElement, lineBreakElementIndex, openingSquareBracketIndex);
      }
    }

    return null;
  }

  protected String extractReferenceTextFromParagraph(TextParagraphElementBase paragraphElement, Integer lineBreakElementIndex, Integer openingSquareBracketIndex) {
    String referenceText = "";
    NodeList children = paragraphElement.getChildNodes();
    int length = children.getLength();

    for(int i = openingSquareBracketIndex; i < length; i++) {
      Node referenceNode = children.item(i);
      if(referenceNode instanceof TextSoftPageBreakElement == false)
        referenceText += referenceNode.getTextContent();
    }

    if(referenceText.startsWith("[")) referenceText = referenceText.substring(1);
    if(referenceText.endsWith("]")) referenceText = referenceText.substring(0, referenceText.length() - 1);

    for(int i = length - 1; i >= lineBreakElementIndex; i--)
      paragraphElement.removeChild(children.item(i));

    return referenceText;
  }

  private Integer findLineBreakElement(NodeList children, int searchStartIndex) {
    for(int i = searchStartIndex; i >= 0; i--) {
      Node node = children.item(i);
      if(node instanceof TextLineBreakElement)
        return i;
    }

    return null;
  }

  private Integer findOpeningSquareBracketIndex(NodeList children, int searchStartIndex, int searchEndIndex) {
    for(int i = searchStartIndex; i < searchEndIndex; i++) {
      Node node = children.item(i);
      if((node instanceof TextImpl || node instanceof OdfTextSpan) && node.getTextContent().startsWith("["))
        return i;
    }

    return null;
  }

  protected ReferenceBase tryToFindReferenceTitleSubtitleAndPublishingDate(List<Paragraph> entryParagraphs, String referenceText) {
    String title = null;
    String publishingDate = null;
    String subTitle = null;

    Paragraph firstParagraph = entryParagraphs.get(0);
    String firstParagraphText = firstParagraph.getTextContent();
    if(firstParagraphText.endsWith("]")) { // in my organisation first paragraph is the Sub Title of a Entry followed by Publishing Date in square brackets
      int indexOpeningSquareBracket = firstParagraphText.lastIndexOf('[');
      if(indexOpeningSquareBracket > 0) {
        publishingDate = firstParagraphText.substring(indexOpeningSquareBracket + 1, firstParagraphText.length() - 1);
        subTitle = firstParagraphText.substring(0, indexOpeningSquareBracket - 1).trim();
        entryParagraphs.remove(firstParagraph); // first paragraph is now handled, avoid that text extraction re-handles it
      }
    }
    else if(firstParagraph.isHeading()) {
      title = firstParagraphText;
      entryParagraphs.remove(firstParagraph); // first paragraph is now handled, avoid that text extraction re-handles it
    }

    if(subTitle != null && entryParagraphs.size() > 0) { // first paragraph contained SubTitle and PublishingDate, the second paragraph then contains the Title
      Paragraph secondParagraph = entryParagraphs.get(0);
      title = secondParagraph.getTextContent();
      if(title.length() > 255) { // not a title but already the content, Entry has only a Title but no SubTitle
        title = subTitle;
        subTitle = "";
      }
      else
        entryParagraphs.remove(secondParagraph); // second paragraph is now handled, avoid that text extraction re-handles it
    }

    return createReference(referenceText, title, subTitle, publishingDate);
  }

  protected ReferenceBase createReference(String referenceText, String title, String subTitle, String publishingDate) {
    if(referenceText.startsWith("http"))
      return createReferenceFromUrl(referenceText, title, subTitle, publishingDate);
    else if(referenceText.contains(", http")) {
      int indexOfCommaAndHttp = referenceText.indexOf(", http");
      title = referenceText.substring(0, indexOfCommaAndHttp);
      referenceText = referenceText.substring(indexOfCommaAndHttp + 2);
      return createReferenceFromUrl(referenceText, title, subTitle, publishingDate);
    }
    else if(referenceText.contains("agora42 ")) {
      return extractAgora42Reference(referenceText);
    }
    else
      return createReferenceFromReferenceText(referenceText);
  }

  protected ReferenceBase createReferenceFromUrl(String url, String title, String subTitle, String publishingDate) {
    SeriesTitle seriesForUrl = findSeriesForUrl(url);
    if(seriesForUrl != null) {
      if(url.contains("www.youtube.com") == false) {
        Reference reference = findOrCreateReferenceForSeriesTitleToPublishingDate(seriesForUrl, publishingDate);

        ReferenceSubDivision article = new ReferenceSubDivision(title, subTitle);
        article.setOnlineAddress(url);
        reference.addSubDivision(article);

        return article;
      }
      else {
        Reference newYouTubeLink = new Reference(title, subTitle);
        newYouTubeLink.setOnlineAddress(url);
        Application.getDeepThought().addReference(newYouTubeLink);
        seriesForUrl.addSerialPart(newYouTubeLink);
        return newYouTubeLink;
      }
    }

    Reference newReference = new Reference(title, subTitle);
    newReference.setIssueOrPublishingDate(publishingDate);
    newReference.setOnlineAddress(url);
    if(StringUtils.isNullOrEmpty(title) && StringUtils.isNotNullOrEmpty(subTitle))
      newReference.setTitle(url);
    return newReference;
  }

  protected ReferenceBase extractAgora42Reference(String referenceText) {
    String publishingDate;SeriesTitle seriesTitle = findOrCreateSeriesTitleFor("agora42");
    referenceText = referenceText.substring(referenceText.indexOf("agora42 ") + "agora42 ".length());
    if(referenceText.indexOf(", ") == 7 || referenceText.indexOf(" - ") == 7) {
      publishingDate = referenceText.substring(0, 7);
      Reference reference = findOrCreateReferenceForSeriesTitleToPublishingDate(seriesTitle, publishingDate);
      referenceText = referenceText.substring(9).trim();
      if(referenceText.startsWith("S. ")) {
        String indication = referenceText;
        return reference;
      }
      else {
        int indicationIndex = referenceText.indexOf(", S.");
        String articleTitle = referenceText.substring(0, indicationIndex);
        ReferenceSubDivision article = findOrCreateReferenceSubDivisionForReference(reference, articleTitle);
        return article;
      }
    }
    else {
      Reference newReference = new Reference(referenceText.substring(8));
      Application.getDeepThought().addReference(newReference);
      seriesTitle.addSerialPart(newReference);
      return newReference;
    }
  }

  protected ReferenceBase createReferenceFromReferenceText(String referenceText) {
    return new Reference(referenceText);
  }

  protected SeriesTitle findSeriesForUrl(String url) {
    if(url.contains("//www.sueddeutsche.de/"))
      return findOrCreateSeriesTitleFor("SZ");
    else if(url.contains("//www.spiegel.de/"))
      return findOrCreateSeriesTitleFor("Spiegel");
    else if(url.contains("//www.handelsblatt.com/"))
      return findOrCreateSeriesTitleFor("Handelsblatt");
    else if(url.contains("//www.faz.net/"))
      return findOrCreateSeriesTitleFor("FAZ");
    else if(url.contains("//www.zeit.de/"))
      return findOrCreateSeriesTitleFor("Zeit");
    else if(url.contains("//www.taz.de/"))
      return findOrCreateSeriesTitleFor("TAZ");
    else if(url.contains("//www.freitag.de/"))
      return findOrCreateSeriesTitleFor("Der Freitag");
    else if(url.contains("//www.nzz.ch/"))
      return findOrCreateSeriesTitleFor("NZZ");
    else if(url.contains("//www.theguardian.com/"))
      return findOrCreateSeriesTitleFor("The Guardian");
    else if(url.contains("//www.youtube.com/"))
      return findOrCreateSeriesTitleFor("YouTube");
    else if(url.contains(".tagesschau.de/"))
      return findOrCreateSeriesTitleFor("Tagesschau");

    return null;
  }

  protected Map<String, SeriesTitle> cachedSeriesForUrl = new HashMap<>();

  protected SeriesTitle findOrCreateSeriesTitleFor(String seriesTitleTitle) {
    if(cachedSeriesForUrl.containsKey(seriesTitleTitle))
      return cachedSeriesForUrl.get(seriesTitleTitle);

    for(SeriesTitle series : Application.getDeepThought().getSeriesTitles()) {
      if(seriesTitleTitle.equals(series.getTitle())) {
        cachedSeriesForUrl.put(seriesTitleTitle, series);
        return series;
      }
    }

    SeriesTitle newSeriesTitle = new SeriesTitle(seriesTitleTitle);
    Application.getDeepThought().addSeriesTitle(newSeriesTitle);
    cachedSeriesForUrl.put(seriesTitleTitle, newSeriesTitle);
    return newSeriesTitle;
  }

  protected Reference findOrCreateReferenceForSeriesTitleToPublishingDate(SeriesTitle series, String publishingDate) {
    if(publishingDate != null) {
      for(Reference reference : series.getSerialParts()) {
        if(publishingDate.equals(reference.getIssueOrPublishingDate()))
          return reference;
      }
    }

    Reference newReference = new Reference();
    newReference.setIssueOrPublishingDate(publishingDate);
    Application.getDeepThought().addReference(newReference);
    series.addSerialPart(newReference);

    return newReference;
  }

  protected ReferenceSubDivision findOrCreateReferenceSubDivisionForReference(Reference reference, String subDivisionTitle) {
    if(subDivisionTitle != null) {
      for(ReferenceSubDivision subDivision : reference.getSubDivisions()) {
        if(subDivisionTitle.equals(subDivision.getTitle()))
          return subDivision;
      }
    }

    ReferenceSubDivision newSubDivision = new ReferenceSubDivision(subDivisionTitle);
    reference.addSubDivision(newSubDivision);
    return newSubDivision;
  }

  protected List<Paragraph> getEntryParagraphs(Iterator<Paragraph> paragraphs) {
    List<Paragraph> entryParagraphs = new ArrayList<>();

    while(paragraphs.hasNext()) {
      Paragraph paragraph = paragraphs.next();
      if(StringUtils.isNullOrEmpty(paragraph.getTextContent())) { // usually an empty line means end of Entry
        if (entryParagraphs.size() == 0 && paragraphs.hasNext()) // but sometimes there are two empty lines between two Entries, so ignore these
          continue;

        break;
      }

      entryParagraphs.add(paragraph);
    }

    return entryParagraphs;
  }

  protected void addEntryToCategory(Entry entry) {
    if(entry.getSeries() != null) {
      String seriesTitleTitle = entry.getSeries().getTitle();
      Category category = findOrCreateCategoryForSeriesTitle(seriesTitleTitle);
      if(category != null)
        category.addEntry(entry);
    }
  }

  protected Category findOrCreateCategoryForSeriesTitle(String seriesTitleTitle) {
    if(seriesTitleCategories.containsKey(seriesTitleTitle))
      return seriesTitleCategories.get(seriesTitleTitle);

    if("SZ".equals(seriesTitleTitle) || "The Guardian".equals(seriesTitleTitle) || "TAZ".equals(seriesTitleTitle) || "NZZ".equals(seriesTitleTitle) ||
        "Der Freitag".equals(seriesTitleTitle) || "Spiegel".equals(seriesTitleTitle) || "Zeit".equals(seriesTitleTitle) || "FAZ".equals(seriesTitleTitle) || "Handelsblatt".equals(seriesTitleTitle)) {
      Category periodicalOnlineCategory = findOrCreateCategoryForOnlinePeriodical(seriesTitleTitle);
      seriesTitleCategories.put(seriesTitleTitle, periodicalOnlineCategory);
      return periodicalOnlineCategory;
    }
    else if("agora42".equals(seriesTitleTitle)) {
      Category periodicalCategory = findOrCreateCategoryForPeriodical(seriesTitleTitle);
      seriesTitleCategories.put(seriesTitleTitle, periodicalCategory);
      return periodicalCategory;
    }
    else if("YouTube".equals(seriesTitleTitle)) {
      Category videoCategory = findOrCreateVideoCategory(seriesTitleTitle);
      seriesTitleCategories.put(seriesTitleTitle, videoCategory);
      return videoCategory;
    }

    return null;
  }

  protected Map<String, Category> seriesTitleCategories = new HashMap<>();

  protected Category findOrCreateCategoryForPeriodical(String categoryTitle) {
    Category periodicalsCategory = findOrCreatePeriodicalsCategory();

    for(Category periodicalCategory : periodicalsCategory.getSubCategories()) {
      if(categoryTitle.equals(periodicalCategory.getName())) {
        return periodicalCategory;
      }
    }

    Category periodicalCategory = new Category(categoryTitle);
    periodicalsCategory.addSubCategory(periodicalCategory);
    return periodicalCategory;
  }

  protected Category findOrCreateCategoryForOnlinePeriodical(String categoryTitle) {
    Category periodicalCategory = findOrCreateCategoryForPeriodical(categoryTitle);

    for(Category periodicalSubCategory : periodicalCategory.getSubCategories()) {
      if("Online".equals(periodicalSubCategory.getName()))
        return periodicalSubCategory;
    }

    Category periodicalSubCategory = new Category("Online");
    periodicalCategory.addSubCategory(periodicalSubCategory);
    return periodicalSubCategory;
  }

  protected Category periodicalsCategory = null;

  protected Category findOrCreatePeriodicalsCategory() {
    if(periodicalsCategory != null)
      return periodicalsCategory;

    for(Category category : Application.getDeepThought().getTopLevelCategory().getSubCategories()) {
      if("Periodika".equals(category.getName())) {
        periodicalsCategory = category;
        return category;
      }
    }

    periodicalsCategory = new Category("Periodika");
    Application.getDeepThought().addCategory(periodicalsCategory);
    return periodicalsCategory;
  }

  protected Category findOrCreateVideoCategory(String categoryTitle) {
    Category videosCategory = findOrCreateVideoTopLevelCategory();

    for(Category videoCategory : videosCategory.getSubCategories()) {
      if(categoryTitle.equals(videoCategory.getName())) {
        return videoCategory;
      }
    }

    Category videoCategory = new Category(categoryTitle);
    videosCategory.addSubCategory(videoCategory);
    return videoCategory;
  }

  protected Category videosTopLevelCategory = null;

  protected Category findOrCreateVideoTopLevelCategory() {
    if(videosTopLevelCategory != null)
      return videosTopLevelCategory;

    for(Category category : Application.getDeepThought().getTopLevelCategory().getSubCategories()) {
      if("Videos".equals(category.getName())) {
        videosTopLevelCategory = category;
        return category;
      }
    }

    videosTopLevelCategory = new Category("Videos");
    Application.getDeepThought().addCategory(videosTopLevelCategory);
    return videosTopLevelCategory;
  }

}
