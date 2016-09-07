package com.example;

import net.dankito.deepthought.Application;
import net.dankito.deepthought.data.contentextractor.EntryCreationResult;
import net.dankito.deepthought.data.contentextractor.SpiegelContentExtractor;
import net.dankito.deepthought.data.contentextractor.SueddeutscheContentExtractor;
import net.dankito.deepthought.data.model.Category;
import net.dankito.deepthought.data.model.Entry;
import net.dankito.deepthought.data.model.FileLink;
import net.dankito.deepthought.data.model.Person;
import net.dankito.deepthought.data.model.Reference;
import net.dankito.deepthought.data.model.ReferenceBase;
import net.dankito.deepthought.data.model.ReferenceSubDivision;
import net.dankito.deepthought.data.model.SeriesTitle;
import net.dankito.deepthought.data.model.Tag;
import net.dankito.deepthought.data.persistence.db.UserDataEntity;
import net.dankito.deepthought.util.StringUtils;
import net.dankito.deepthought.util.file.FileUtils;

import org.apache.xerces.dom.TextImpl;
import org.odftoolkit.odfdom.dom.element.draw.DrawFrameElement;
import org.odftoolkit.odfdom.dom.element.style.StyleTextPropertiesElement;
import org.odftoolkit.odfdom.dom.element.table.TableTableCellElement;
import org.odftoolkit.odfdom.dom.element.table.TableTableElement;
import org.odftoolkit.odfdom.dom.element.table.TableTableRowElement;
import org.odftoolkit.odfdom.dom.element.text.TextAElement;
import org.odftoolkit.odfdom.dom.element.text.TextLineBreakElement;
import org.odftoolkit.odfdom.dom.element.text.TextListElement;
import org.odftoolkit.odfdom.dom.element.text.TextListItemElement;
import org.odftoolkit.odfdom.dom.element.text.TextParagraphElementBase;
import org.odftoolkit.odfdom.dom.element.text.TextSoftPageBreakElement;
import org.odftoolkit.odfdom.dom.element.text.TextSpanElement;
import org.odftoolkit.odfdom.dom.element.text.TextTabElement;
import org.odftoolkit.odfdom.incubator.doc.draw.OdfDrawFrame;
import org.odftoolkit.odfdom.incubator.doc.draw.OdfDrawImage;
import org.odftoolkit.odfdom.incubator.doc.style.OdfStyle;
import org.odftoolkit.odfdom.incubator.doc.text.OdfTextList;
import org.odftoolkit.odfdom.incubator.doc.text.OdfTextSpan;
import org.odftoolkit.simple.TextDocument;
import org.odftoolkit.simple.draw.Image;
import org.odftoolkit.simple.text.Paragraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class OpenOfficeDocumentsImporterExporter {

  private final static Logger log = LoggerFactory.getLogger(OpenOfficeDocumentsImporterExporter.class);


  protected SpiegelContentExtractor spiegelContentExtractor = new SpiegelContentExtractor();
  protected SueddeutscheContentExtractor sueddeutscheContentExtractor = new SueddeutscheContentExtractor();


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
        extractedText += paragraph.getTextContent() + Application.getPlatformConfiguration().getLineSeparator();
      }

      if(extractedText.length() > 0)
        extractedText = extractedText.substring(0, extractedText.length() - Application.getPlatformConfiguration().getLineSeparator().length());
    } catch (Exception ex) {
      log.error("Could not load text document " + documentPath, ex);
    }

    if(textDocument != null)
      textDocument.close();
    return extractedText;
  }


  public List<Entry> importDankitosZitate(String documentPath) {
    List<Entry> extractedEntries = new ArrayList<>();
    TextDocument textDocument = null;
    try {
      textDocument = TextDocument.loadDocument(documentPath);
      if(textDocument == null) // Media Type not supported by Simple Odf
        return extractedEntries;

      Iterator<Paragraph> paragraphs = textDocument.getParagraphIterator();
      extractQuotationsFromParagraphs(paragraphs, extractedEntries);
    } catch (Exception ex) {
      log.error("Could not load text document " + documentPath, ex);
    }

    if(textDocument != null)
      textDocument.close();
    return extractedEntries;
  }

  protected UserDataEntity lastPersonOrReference = null;

  protected void extractQuotationsFromParagraphs(Iterator<Paragraph> paragraphs, List<Entry> extractedEntries) {
    List<Paragraph> entryParagraphs = null;
    for(entryParagraphs = getEntryParagraphs(paragraphs); entryParagraphs.size() > 0; entryParagraphs = getEntryParagraphs(paragraphs)) {
      UserDataEntity entryPersonOrReference = extractPersonOrReferenceFromIntroducingParagraph(entryParagraphs);
      if(entryParagraphs.size() == 0) // entryParagraphs contained only the reference for the next Quotations
        continue;

      Entry extractedEntry = extractQuotationEntryFromEntryParagraphs(entryParagraphs);

      if(extractedEntry != null) {
        Application.getDeepThought().addEntry(extractedEntry);
        extractedEntries.add(extractedEntry);
        addTagsToQuotationEntry(extractedEntry);

        if(entryPersonOrReference != null)
          setEntryPersonOrReference(extractedEntry, entryPersonOrReference);
        else if(extractedEntry.getReference() == null && lastPersonOrReference != null)
          setEntryPersonOrReference(extractedEntry, lastPersonOrReference);
      }
    }
  }

  protected void addTagsToQuotationEntry(Entry extractedEntry) {
    Tag quotationTag = Application.getEntitiesSearcherAndCreator().findOrCreateTagForName("Zitat");
    extractedEntry.addTag(quotationTag);
  }

  protected void setEntryPersonOrReference(Entry entry, UserDataEntity entryPersonOrReference) {
    if(entryPersonOrReference instanceof Person) {
      Person person = (Person)entryPersonOrReference;
      entry.addPerson(person);
      addTagToEntryFromPerson(entry, person);
    }
    else if(entryPersonOrReference instanceof Reference) {
      entry.setReference((Reference) entryPersonOrReference);
      addTagsToEntryFromReference(entry);
    }
  }

  protected void addTagsToEntryFromReference(Entry entry) {
    if(entry.getReference() != null) {
      entry.addTag(Application.getEntitiesSearcherAndCreator().findOrCreateTagForName(entry.getReference().getTitle()));
      for (Person person : entry.getReference().getPersons())
        addTagToEntryFromPerson(entry, person);
    }
  }

  protected void addTagToEntryFromPerson(Entry entry, Person person) {
    if(StringUtils.isNotNullOrEmpty(person.getFirstName()))
      entry.addTag(Application.getEntitiesSearcherAndCreator().findOrCreateTagForName(person.getFirstName() + " " + person.getLastName()));
    else
      entry.addTag(Application.getEntitiesSearcherAndCreator().findOrCreateTagForName(person.getLastName()));
  }

  protected Entry extractQuotationEntryFromEntryParagraphs(List<Paragraph> entryParagraphs) {
    Entry entry = new Entry();
    tryToFindQuotationPersonOrReferenceFromLastParagraph(entry, entryParagraphs);

    String content = "";
    for(Paragraph paragraph : entryParagraphs) {
      content += "<p>" + extractHtmlFormattedContentFromParagraph(paragraph) + "</p>";
    }
    entry.setContent(content);

    return entry;
  }

  protected class QuotationReference {
    protected Person author = null;
    protected Reference reference = null;
    protected String indication = null;

    public Person getAuthor() {
      return author;
    }

    public Reference getReference() {
      return reference;
    }

    public String getIndication() {
      return indication;
    }
  }

  protected UserDataEntity extractPersonOrReferenceFromIntroducingParagraph(List<Paragraph> entryParagraphs) {
    Paragraph firstParagraph = entryParagraphs.get(0);
    String firstParagraphText = firstParagraph.getTextContent().trim();
    if(firstParagraphText.endsWith(":") && firstParagraphText.length() < 128) {
      entryParagraphs.remove(0);
      return extractPersonOrReferenceFromIntroducingParagraph(firstParagraph);
    }

    return null;
  }

  protected UserDataEntity extractPersonOrReferenceFromIntroducingParagraph(Paragraph firstParagraph) {
    String firstParagraphText = firstParagraph.getTextContent();
    if(firstParagraphText.endsWith(":"))
      firstParagraphText = firstParagraphText.substring(0, firstParagraphText.length() - 1);

    if(firstParagraphText.contains(" - ") || firstParagraphText.contains(" – ")) { // Author and Book title
      lastPersonOrReference = getReference(firstParagraphText);
    }
    else {
      lastPersonOrReference = findOrCreatePersonForFullName(firstParagraphText);
    }

    return lastPersonOrReference;
  }

  protected Reference getReference(String firstParagraphText) {
    firstParagraphText = firstParagraphText.replace(" – ", " - ");
    String[] parts = firstParagraphText.split(" - ");
    List<Person> authors = findOrCreatePersonsFromAuthorString(parts[0]);
    Reference book = findOrCreateReferenceForTitle(parts[1]);
    for(Person author : authors)
      book.addPerson(author);

    return book;
  }

  protected List<Person> findOrCreatePersonsFromAuthorString(String authorString) {
    List<Person> persons = new ArrayList<>();

    if(authorString.contains(", ")) { // multiple persons are separated by comma
      for(String singlePersonFullName : authorString.split(", "))
        persons.add(findOrCreatePersonForFullName(singlePersonFullName));
    }
    else
      persons.add(findOrCreatePersonForFullName(authorString));

    return persons;
  }

  protected Person findOrCreatePersonForFullName(String personFullName) {
    if(personFullName.contains(" ")) {
      String[] parts = personFullName.split(" ");
      String lastName = parts[parts.length - 1].trim();
      String firstName = "";
      for(int i = 0; i < parts.length - 1; i++)
        firstName += parts[i].trim() + " ";
      firstName = firstName.substring(0, firstName.length() - 1);
      return Application.getEntitiesSearcherAndCreator().findOrCreatePerson(lastName, firstName);
    }
    else {
      return Application.getEntitiesSearcherAndCreator().findOrCreatePerson(personFullName.trim(), "");
    }
  }

  protected Reference findOrCreateReferenceForTitle(String title) {
    return Application.getEntitiesSearcherAndCreator().findOrCreateReferenceForTitle(title);
  }

  protected void tryToFindQuotationPersonOrReferenceFromLastParagraph(Entry entry, List<Paragraph> entryParagraphs) {
    Paragraph lastParagraph = entryParagraphs.get(entryParagraphs.size() - 1);
    TextParagraphElementBase paragraphElement = lastParagraph.getOdfElement();
    NodeList children = paragraphElement.getChildNodes();
    int length = children.getLength();
    if(paragraphElement.getTextContent().startsWith("(S. ") && paragraphElement.getTextContent().endsWith(")")) {
      setEntryIndication(entry, paragraphElement.getTextContent().substring(1, paragraphElement.getTextContent().length() - 1));
      entryParagraphs.remove(lastParagraph);
    }
    else if(length == 1 || (length == 2 && paragraphElement.getFirstChild() instanceof TextSoftPageBreakElement)) {
      NodeList subChildren = children.item(length - 1).getChildNodes();
      if(subChildren instanceof OdfTextSpan && subChildren.getLength() > 2 && subChildren instanceof OdfTextSpan) {
        if(subChildren.item(0) instanceof TextTabElement && subChildren.item(1) instanceof TextTabElement) {
          String referenceText = subChildren.item(2).getTextContent();
          entryParagraphs.remove(lastParagraph);
          if(referenceText.contains(" - ") || referenceText.contains(" – ")) { // Author and Book title
            entry.setReference(getReference(referenceText));
            addTagsToEntryFromReference(entry);
            lastPersonOrReference = null;
          }
          else if(referenceText.contains("S. ")) {
            setEntryIndication(entry, referenceText);
          }
          else {
            Person person = findOrCreatePersonForFullName(referenceText);
            entry.addPerson(person);
            addTagToEntryFromPerson(entry, person);
            lastPersonOrReference = null;
          }
        }
      }
    }
    else {
      Node lastChild = children.item(children.getLength() - 1);
      String lastChildText = lastChild.getTextContent();
      if(lastChildText.startsWith("(S. ") && lastChildText.endsWith(")")) {
        setEntryIndication(entry, lastChildText.substring(1, lastChildText.length() - 1));
        paragraphElement.removeChild(lastChild);
        if(children.item(children.getLength() - 1) instanceof TextLineBreakElement)
          paragraphElement.removeChild(children.item(children.getLength() - 1));
      }
      else if(lastChildText.equals("ebd.")) {

      }
    }
  }

  protected void setEntryIndication(Entry entry, String indicationText) {
    entry.setIndication(indicationText);
    setEntryPersonOrReference(entry, lastPersonOrReference);
  }


  public List<Entry> extractEntriesFromDankitosSchneisenImWald(String documentPath) {
    List<Entry> extractedEntries = new ArrayList<>();
    TextDocument textDocument = null;
    try {
      textDocument = TextDocument.loadDocument(documentPath);
      if(textDocument == null) // Media Type not supported by Simple Odf
        return extractedEntries;

      Iterator<Paragraph> paragraphs = textDocument.getParagraphIterator();
      extractEntriesFromParagraphs(paragraphs, extractedEntries);
    } catch (Exception ex) {
      log.error("Could not load text document " + documentPath, ex);
    }

    if(textDocument != null)
      textDocument.close();
    return extractedEntries;
  }

  protected void extractEntriesFromParagraphs(Iterator<Paragraph> paragraphs, List<Entry> extractedEntries) throws URISyntaxException {
    List<Paragraph> entryParagraphs = null;
    for(entryParagraphs = getEntryParagraphs(paragraphs); entryParagraphs.size() > 0; entryParagraphs = getEntryParagraphs(paragraphs)) {
      Entry extractedEntry = extractEntryFromEntryParagraphs(entryParagraphs);
      if(extractedEntry != null)
        extractedEntries.add(extractedEntry);
    }

//    return extractedEntries;
  }

  protected Entry extractEntryFromEntryParagraphs(List<Paragraph> entryParagraphs) {
    try {
      Entry currentEntry = null;

      if(entryParagraphs.size() == 1) {
        if(sueddeutscheContentExtractor.canCreateEntryFromUrl(entryParagraphs.get(0).getOdfElement().getTextContent())) {
          EntryCreationResult result = sueddeutscheContentExtractor.createEntryFromArticle(entryParagraphs.get(0).getOdfElement().getTextContent());
          // TODO: this doesn't work this way anymore, Entry's Tags, Categories, ... are now in EntryCreationResult
          if (result.successful())
            currentEntry = result.getCreatedEntry();
        }
        else if(spiegelContentExtractor.canCreateEntryFromUrl(entryParagraphs.get(0).getOdfElement().getTextContent())) {
          EntryCreationResult result = spiegelContentExtractor.createEntryFromArticle(entryParagraphs.get(0).getOdfElement().getTextContent());
          // TODO: this doesn't work this way anymore, Entry's Tags, Categories, ... are now in EntryCreationResult
          if (result.successful())
            currentEntry = result.getCreatedEntry();
        }
      }

      if(currentEntry == null) {
        currentEntry = new Entry();
        ReferenceBase currentReference = tryToExtractReference(entryParagraphs);
        if (currentReference instanceof ReferenceSubDivision)
          currentEntry.setReferenceSubDivision((ReferenceSubDivision) currentReference);
        else if (currentReference instanceof Reference)
          currentEntry.setReference((Reference) currentReference);

        checkIfFirstParagraphContainsAbstract(currentEntry, entryParagraphs);
        currentEntry.setContent(extractEntryContent(entryParagraphs));
      }

      Application.getDeepThought().addEntry(currentEntry);
      addTagsToEntry(currentEntry);
      return currentEntry;
    } catch(Exception ex) {
      log.error("Could not extract Entry from entryParagraphs", ex);
    }

    return null;
  }

  protected void checkIfFirstParagraphContainsAbstract(Entry entry, List<Paragraph> entryParagraphs) {
    if(entryParagraphs.size() == 0)
      return;

    Paragraph firstParagraph = entryParagraphs.get(0);
    if(firstParagraph.getTextContent().length() < HeadingMaxLength && firstParagraph.getOdfElement().getNextSibling() instanceof TextListElement) {
      entry.setAbstract(extractHtmlTextFromList((OdfTextList)firstParagraph.getOdfElement().getNextSibling(), firstParagraph));
      entryParagraphs.remove(0);
    }
  }

  protected String extractEntryContent(List<Paragraph> entryParagraphs) {
    String entryContent = "";
    
    for(Paragraph paragraph : entryParagraphs) {
      String paragraphHtml = extractHtmlFormattedContentFromParagraph(paragraph);

      if(paragraph.isHeading())
        entryContent += "<h" + paragraph.getHeadingLevel() + ">" + paragraphHtml + "</h" + paragraph.getHeadingLevel() + ">";
      else
        entryContent += "<p>" + paragraphHtml + "</p>";
    }

    return entryContent;
  }

  protected String extractHtmlFormattedContentFromParagraph(Paragraph paragraph) {
    String paragraphHtml = "";

    NodeList paragraphChildren = paragraph.getOdfElement().getChildNodes();
    for(int i = 0; i < paragraphChildren.getLength(); i++) {
      Node block = paragraphChildren.item(i);
      paragraphHtml += extractHtmlTextFromNode(block, paragraph.getOdfElement().getAutomaticStyle());
    }

    if(paragraph.getOdfElement().getNextSibling() instanceof OdfTextList)
      paragraphHtml += extractHtmlTextFromList((OdfTextList)paragraph.getOdfElement().getNextSibling(), paragraph);

    if(paragraph.getOdfElement().getNextSibling() instanceof TableTableElement)
      paragraphHtml += extractHtmlTextFromTableElement((TableTableElement)paragraph.getOdfElement().getNextElementSibling(), paragraph);

    return paragraphHtml;
  }

  protected String extractHtmlTextFromNode(Node node, OdfStyle parentStyle) {
    if(node instanceof TextSoftPageBreakElement) // a manual page break on text overflow, not needed in HTML
      return "";
    else if(node instanceof TextLineBreakElement)
      return  "<br />";
    else if(node instanceof TextSpanElement) {
      TextSpanElement element = (TextSpanElement)node;
      if(element.hasAutomaticStyle() || element.hasDocumentStyle()) {
        return extractStyledBlockContent(element);
      }
      else
        return node.getTextContent();
    }
    else if(node instanceof TextAElement) {
      TextAElement textHyperlink = (TextAElement)node;
      return  "<a href=\"" + textHyperlink.getXlinkHrefAttribute() + "\">" + textHyperlink.getTextContent() + "</a>";
    }
    else if(node instanceof DrawFrameElement)
      return extractDrawFrameElementContent((DrawFrameElement)node);
    else if(node instanceof TextListItemElement) {
      TextListItemElement listItemElement = (TextListItemElement)node;
      String itemText = "<li>";
      for(int i = 0; i < listItemElement.getLength(); i++) {
        Node listItemChild = listItemElement.item(i);
        itemText += extractHtmlTextFromNode(listItemChild, null);
      }

      return itemText + "</li>";
    }
    else if(parentStyle != null) {
      return extractStyledBlockContent(node, parentStyle);
    }
    else
      return node.getTextContent();
  }

  protected String extractStyledBlockContent(TextSpanElement element) {
    OdfStyle style = element.hasAutomaticStyle() ? element.getAutomaticStyle() : element.getDocumentStyle();
    return extractStyledBlockContent(element, style);
  }

  protected String extractStyledBlockContent(Node node, OdfStyle style) {
    if(style.getFirstChild() instanceof StyleTextPropertiesElement) { // TODO: also check other children for StyleTextPropertiesElement
      StyleTextPropertiesElement textPropertiesElement = (StyleTextPropertiesElement)style.getFirstChild();
      // TODO: also check for: underline, strike through, text fore- and background color, ...
      boolean isBold = textPropertiesElement.getFoFontWeightAttribute() != null && textPropertiesElement.getFoFontWeightAttribute().contains("bold");
      boolean isItalic = textPropertiesElement.getFoFontStyleAttribute() != null && textPropertiesElement.getFoFontStyleAttribute().contains("italic");

      String blockHtml = "";
      if(isBold) blockHtml += "<b>";
      if(isItalic) blockHtml += "<i>";

      blockHtml += node.getTextContent();

      if(isItalic) blockHtml += "</i>";
      if(isBold) blockHtml += "</b>";

      return blockHtml;
    }
    else
      return node.getTextContent();
  }

  protected String extractDrawFrameElementContent(DrawFrameElement drawFrameElement) {
    for(int i = 0; i < drawFrameElement.getLength(); i++) {
      Node drawChild = drawFrameElement.item(i);
      if(drawChild instanceof OdfDrawImage) {
        OdfDrawImage imageElement = (OdfDrawImage)drawChild;
        String href = imageElement.getXlinkHrefAttribute();
        Image extractedImage = Image.getInstanceof(imageElement);

        InputStream imageInputStream = extractedImage.getImageInputStream();
        if(imageInputStream != null) { // a local image
          FileLink imageFile = new FileLink(href);
          String folderPath = FileUtils.getUserDataFolderForFile(imageFile);
          File destinationFile = new File(folderPath, imageFile.getName());
          try {
            FileUtils.writeToFile(imageInputStream, destinationFile);
          } catch (Exception ex) {
            return "";
          }
          try { imageInputStream.close(); } catch (Exception ex) { }

          return "<p><img src=\"file://" + destinationFile.getAbsolutePath() + "\" /></p>";
        }
        else if(StringUtils.isNotNullOrEmpty(href)) // an image referenced from internet
          return "<p><img src=\"" + href + "\" /></p>"; // TODO: download image
      }
    }

    return "";
  }

  protected String extractHtmlTextFromList(OdfTextList textList, Paragraph paragraph) {
    String listHtml = "";
    if(textList.hasChildNodes()) {
      listHtml += "<ul>";
      NodeList listChildren = textList.getChildNodes();

      for (int i = 0; i < listChildren.getLength(); i++) {
        Node listElement = listChildren.item(i);
        listHtml += extractHtmlTextFromNode(listElement, paragraph.getOdfElement().getAutomaticStyle());
      }

      listHtml += "</ul>";
    }
    return listHtml;
  }

  protected String extractHtmlTextFromTableElement(TableTableElement tableElement, Paragraph paragraph) {
    String tableHtml = "";
    if(tableElement.hasChildNodes()) {
      tableHtml += "<table>";

      for (int i = 0; i < tableElement.getLength(); i++) {
        Node tableChildElement = tableElement.item(i);
        // TODO: extract table header
        if(tableChildElement instanceof TableTableRowElement) {
          TableTableRowElement rowElement = (TableTableRowElement) tableChildElement;
          tableHtml += "<tr>";

          for(int j = 0; j < rowElement.getLength(); j++) {
            Node rowChild = rowElement.item(j);
            if(rowChild instanceof TableTableCellElement) {
              TableTableCellElement cellElement = (TableTableCellElement) rowChild;
              tableHtml += "<td>";
              for(int k = 0; k < cellElement.getLength(); k++)
                tableHtml += extractHtmlTextFromNode(cellElement.item(k), cellElement.getAutomaticStyle());
              tableHtml += "</td>";
            }
          }

          tableHtml += "</tr>";
        }
      }

      tableHtml += "</table>";
    }
    return tableHtml;
  }

  protected ReferenceBase tryToExtractReference(List<Paragraph> entryParagraphs) {
    String referenceText = tryToFindReferenceText(entryParagraphs);
    if(referenceText == null || referenceText.length() > 255) // no reference specified in this case
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
          lineBreakElementIndex = -1;

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

    for(int i = length - 1; i >= (lineBreakElementIndex >= 0 ? lineBreakElementIndex : 0); i--)
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

  protected final static int HeadingMaxLength = 128;

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
        if(Reference.tryToParseIssueOrPublishingDateToDate(publishingDate) == null)
          publishingDate = null;
        else {
          subTitle = firstParagraphText.substring(0, indexOpeningSquareBracket - 1).trim();
          if (referenceText.contains("//www.der-postillon.com/") == false)
            entryParagraphs.remove(firstParagraph); // first paragraph is now handled, avoid that text extraction re-handles it
          else {
            firstParagraph.getOdfElement().setTextContent(subTitle);
            title = subTitle;
            subTitle = null;
          }
        }
      }
    }
    else if(firstParagraph.isHeading()) {
      title = firstParagraphText;
      entryParagraphs.remove(firstParagraph); // first paragraph is now handled, avoid that text extraction re-handles it
    }
    else if(firstParagraphText.length() < HeadingMaxLength && StringUtils.isNotNullOrEmpty(referenceText) && entryParagraphs.size() > 2) {
      subTitle = firstParagraphText;
      entryParagraphs.remove(firstParagraph);
    }

    if(subTitle != null && entryParagraphs.size() > 0) { // first paragraph contained SubTitle and PublishingDate, the second paragraph then contains the Title
      Paragraph secondParagraph = entryParagraphs.get(0);
      title = secondParagraph.getTextContent();
      if(title.length() > HeadingMaxLength || title.startsWith("• ")) { // not a title but already the content, Entry has only a Title but no SubTitle
        title = subTitle;
        subTitle = "";
      }
      else if(secondParagraph.getOdfElement().getNextSibling() instanceof OdfTextList == false) // Sueddeutsche articles sometimes have lists as Abstract on second paragraph
        entryParagraphs.remove(secondParagraph); // second paragraph is now handled, avoid that text extraction re-handles it
    }

    // sometimes i indicate reference date and publishing date together on last paragraph like 'Titanic Online, 09.12.2014'
    if(publishingDate == null && referenceText.contains(", ")) {
      publishingDate = referenceText.substring(referenceText.lastIndexOf(", ") + 1).trim();
      Date parsedDate = Reference.tryToParseIssueOrPublishingDateToDate(publishingDate);

      if(parsedDate != null)
        referenceText = referenceText.substring(0, referenceText.lastIndexOf(','));
      else
        publishingDate = null;
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
      return createReferenceFromReferenceText(referenceText, title, subTitle, publishingDate);
  }

  protected ReferenceBase createReferenceFromUrl(String url, String title, String subTitle, String publishingDate) {
    // a Spiegel article which meta data hasn't specified currectly by me
    if(spiegelContentExtractor.canCreateEntryFromUrl(url) &&
        (StringUtils.isNullOrEmpty(title) || StringUtils.isNullOrEmpty(subTitle) || StringUtils.isNullOrEmpty(publishingDate))) {
      EntryCreationResult creationResult = spiegelContentExtractor.createEntryFromArticle(url);
      // TODO: this doesn't work this way anymore, Entry's Tags, Categories, ... are now in EntryCreationResult
      if(creationResult.successful())
        return creationResult.getCreatedEntry().getReferenceSubDivision();
      else
        log.error("Could not read Spiegel Online article from Url " + url, creationResult.getError());
    }

    SeriesTitle seriesForUrl = findSeriesForUrl(url);
    if(seriesForUrl != null) {
      if(url.contains("www.youtube.com") == false) {
        Reference reference = findOrCreateReferenceForSeriesTitleToPublishingDate(seriesForUrl, publishingDate);

        ReferenceSubDivision article = new ReferenceSubDivision(title, subTitle);
        article.setOnlineAddressAndLastAccessToCurrentDateTime(url);
        reference.addSubDivision(article);
        Application.getDeepThought().addReferenceSubDivision(article);

        return article;
      }
      else {
        Reference newYouTubeLink = new Reference(title, subTitle);
        newYouTubeLink.setOnlineAddressAndLastAccessToCurrentDateTime(url);
        seriesForUrl.addSerialPart(newYouTubeLink);
        Application.getDeepThought().addReference(newYouTubeLink);
        return newYouTubeLink;
      }
    }

    Reference newReference = new Reference(title, subTitle);
    newReference.setIssueOrPublishingDate(publishingDate);
    newReference.setOnlineAddressAndLastAccessToCurrentDateTime(url);
    if(StringUtils.isNullOrEmpty(title) && StringUtils.isNullOrEmpty(subTitle))
      newReference.setTitle(url);
    Application.getDeepThought().addReference(newReference);
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
      seriesTitle.addSerialPart(newReference);
      Application.getDeepThought().addReference(newReference);
      return newReference;
    }
  }

  protected ReferenceBase createReferenceFromReferenceText(String referenceText, String title, String subTitle, String publishingDate) {
    if(referenceText.toLowerCase().contains("titanic")) {
      SeriesTitle series = findOrCreateSeriesTitleFor("Titanic");
      if(publishingDate == null)
        return series;
      else {
        Reference reference = findOrCreateReferenceForSeriesTitleToPublishingDate(series, publishingDate);
        return reference;
      }
    }
    else if(StringUtils.isNullOrEmpty(title)) {
      Reference reference = new Reference(referenceText);
      Application.getDeepThought().addReference(reference);
      return reference;
    }
    else {
      Reference reference = new Reference(referenceText);
      if(StringUtils.isNotNullOrEmpty(publishingDate))
        reference.setIssueOrPublishingDate(publishingDate);
      Application.getDeepThought().addReference(reference);

      ReferenceSubDivision subDivision = new ReferenceSubDivision(title, subTitle);
      reference.addSubDivision(subDivision);
      Application.getDeepThought().addReferenceSubDivision(subDivision);

      return subDivision;
    }
  }

  protected SeriesTitle findSeriesForUrl(String url) {
    if(url.contains("//www.sueddeutsche.de/"))
      return findOrCreateSeriesTitleFor("SZ");
    else if(url.contains(".spiegel.de/"))
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
    else if(url.contains("//www.theguardian.com/") || url.contains("//www.guardian.co.uk/"))
      return findOrCreateSeriesTitleFor("The Guardian");
    else if(url.contains("//www.youtube.com/"))
      return findOrCreateSeriesTitleFor("YouTube");
    else if(url.contains(".tagesschau.de/"))
      return findOrCreateSeriesTitleFor("Tagesschau");
    else if(url.contains("//www.der-postillon.com/"))
      return findOrCreateSeriesTitleFor("Postillon");

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
    newReference.setSeries(series);
    Application.getDeepThought().addReference(newReference);

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
    Application.getDeepThought().addReferenceSubDivision(newSubDivision);
    return newSubDivision;
  }

  protected List<Paragraph> getEntryParagraphs(Iterator<Paragraph> paragraphs) {
    List<Paragraph> entryParagraphs = new ArrayList<>();

    while(paragraphs.hasNext()) {
      Paragraph paragraph = paragraphs.next();
      if(StringUtils.isNullOrEmpty(paragraph.getTextContent()) && paragraph.getOdfElement().getFirstChild() instanceof OdfDrawFrame == false) { // usually an empty line means
      // end of Entry
        if (entryParagraphs.size() == 0 && paragraphs.hasNext()) // but sometimes there are two empty lines between two Entries, so ignore these
          continue;

        break;
      }

      entryParagraphs.add(paragraph);
    }

    return entryParagraphs;
  }

  protected Category findOrCreateCategoryForSeriesTitle(String seriesTitleTitle) {
    if(seriesTitleCategories.containsKey(seriesTitleTitle))
      return seriesTitleCategories.get(seriesTitleTitle);

    if("SZ".equals(seriesTitleTitle) || "The Guardian".equals(seriesTitleTitle) || "TAZ".equals(seriesTitleTitle) || "NZZ".equals(seriesTitleTitle) ||
        "Der Freitag".equals(seriesTitleTitle) || "Spiegel".equals(seriesTitleTitle) || "Zeit".equals(seriesTitleTitle) || "FAZ".equals(seriesTitleTitle) ||
        "Titanic".equals(seriesTitleTitle) || "Postillion".equals(seriesTitleTitle) || "Handelsblatt".equals(seriesTitleTitle)) {
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
      if(periodicalSubCategory.getName().contains("Online"))
        return periodicalSubCategory;
    }

    Category periodicalSubCategory = new Category(categoryTitle + " Online");
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


//  protected LuceneSearchEngine luceneSearchEngine = new LuceneSearchEngine();

  protected void addTagsToEntry(Entry entry) {
    if(entry.getSeries() != null) {
      String seriesTitleTitle = entry.getSeries().getTitle();
      Tag tag = Application.getEntitiesSearcherAndCreator().findOrCreateTagForName(seriesTitleTitle);
      if(tag != null)
        entry.addTag(tag);
    }

//    if(entry.getReference() != null && StringUtils.isNotNullOrEmpty(entry.getReference().getIssueOrPublishingDate()))
//      entry.addTag(Application.getDeepThought().findOrCreateTagForName(entry.getReference().getIssueOrPublishingDate()));

//    List<String> entryTerms = luceneSearchEngine.extractTermsFromEntry(entry);
//
//    for(int i = 0; i < (entryTerms.size() < 5 ? entryTerms.size() : 5); i++) { // add at maximum the five most relevant terms as tags
//      Tag tag = findOrCreateTagForTagName(entryTerms.get(i));
//      entry.addTag(tag);
//    }
  }

}
