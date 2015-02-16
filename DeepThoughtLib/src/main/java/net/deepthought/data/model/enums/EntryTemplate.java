package net.deepthought.data.model.enums;

import net.deepthought.data.persistence.db.TableConfig;
import net.deepthought.util.Localization;

import java.util.Arrays;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 * Created by ganymed on 14/12/14.
 */
@Entity(name = TableConfig.EntryTemplateTableName)
public class EntryTemplate extends ExtensibleEnumeration {

  private static final long serialVersionUID = 6495166895657265252L;


  public final static String DefaultEntryTemplateKey = "Standard";
  public final static String ThoughtEntryTemplateKey = "Thought";
  public final static String QuotationEntryTemplateKey = "Quotation";
  public final static String WordDefinitionEntryTemplateKey = "WordDefinition";
  public final static String BookEntryTemplateKey = "Book";
  public final static String JournalArticleEntryTemplateKey = "JournalArticle";


  @Column(name = TableConfig.EntryTemplateKeyColumnName)
  protected String key = "";

  @Column(name = TableConfig.EntryTemplateHelpTextColumnName)
  protected String helpText = "";

  @Column(name = TableConfig.EntryTemplateHelpTextResourceKeyColumnName)
  protected String helpTextResourceKey = null;

  @Column(name = TableConfig.EntryTemplateIsSystemTemplateColumnName)
  protected boolean isSystemTemplate = false;


  @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
  @JoinColumn(name = TableConfig.EntryTemplateDefaultPersonRoleJoinColumnName)
  protected PersonRole defaultPersonRole;


  @Column(name = TableConfig.EntryTemplateShowTitleColumnName)
  protected boolean showTitle = true;

  @Column(name = TableConfig.EntryTemplateShowSubTitleColumnName)
  protected boolean showSubTitle = false;

  @Column(name = TableConfig.EntryTemplateShowAbstractColumnName)
  protected boolean showAbstract = false;

  @Column(name = TableConfig.EntryTemplateShowTableOfContentsColumnName)
  protected boolean showTableOfContents = false;

  @Column(name = TableConfig.EntryTemplateShowContentColumnName)
  protected boolean showContent = true;

  @Column(name = TableConfig.EntryTemplateShowSeriesTitleColumnName)
  protected boolean showSeriesTitle = false;

  @Column(name = TableConfig.EntryTemplateShowReferenceColumnName)
  protected boolean showReference = false;

  @Column(name = TableConfig.EntryTemplateShowReferenceSubDivisionColumnName)
  protected boolean showReferenceSubDivision = false;

  @Column(name = TableConfig.EntryTemplateShowStartPageColumnName)
  protected boolean showReferenceStart = false;

  @Column(name = TableConfig.EntryTemplateShowEndPageColumnName)
  protected boolean showReferenceEnd = false;

  @Column(name = TableConfig.EntryTemplateShowPersonsColumnName)
  protected boolean showPersons = false;

  @Column(name = TableConfig.EntryTemplateShowCategoriesColumnName)
  protected boolean showCategories = true;

  @Column(name = TableConfig.EntryTemplateShowTagsColumnName)
  protected boolean showTags = true;

  @Column(name = TableConfig.EntryTemplateShowIndexTermsColumnName)
  protected boolean showIndexTerms = false;

  @Column(name = TableConfig.EntryTemplateShowNotesColumnName)
  protected boolean showNotes = false;

  @Column(name = TableConfig.EntryTemplateShowEntryLinkGroupsColumnName)
  protected boolean showEntryLinkGroups = false;

  @Column(name = TableConfig.EntryTemplateShowSubEntriesColumnName)
  protected boolean showSubEntries = false;

  @Column(name = TableConfig.EntryTemplateShowFilesColumnName)
  protected boolean showFiles = false;

  @Column(name = TableConfig.EntryTemplateShowLanguageColumnName)
  protected boolean showLanguage = false;

  @Column(name = TableConfig.EntryTemplateShowPreviewImageColumnName)
  protected boolean showPreviewImage = false;

  @Column(name = TableConfig.EntryTemplateShowSpecificField01ColumnName)
  protected boolean showSpecificField01 = false;

  @Column(name = TableConfig.EntryTemplateSpecificField01DisplayNameResourceKeyColumnName)
  protected String specificField01DisplayNameResourceKey;

  @Column(name = TableConfig.EntryTemplateSpecificField01HelpTextResourceKeyColumnName)
  protected String specificField01HelpTextResourceKey;

  @Column(name = TableConfig.EntryTemplateShowSpecificField02ColumnName)
  protected boolean showSpecificField02 = false;

  @Column(name = TableConfig.EntryTemplateSpecificField02DisplayNameResourceKeyColumnName)
  protected String specificField02DisplayNameResourceKey;

  @Column(name = TableConfig.EntryTemplateSpecificField02HelpTextResourceKeyColumnName)
  protected String specificField02HelpTextResourceKey;

  @Column(name = TableConfig.EntryTemplateShowSpecificField03ColumnName)
  protected boolean showSpecificField03 = false;

  @Column(name = TableConfig.EntryTemplateSpecificField03DisplayNameResourceKeyColumnName)
  protected String specificField03DisplayNameResourceKey;

  @Column(name = TableConfig.EntryTemplateSpecificField03HelpTextResourceKeyColumnName)
  protected String specificField03HelpTextResourceKey;

  @Column(name = TableConfig.EntryTemplateShowSpecificField04ColumnName)
  protected boolean showSpecificField04 = false;

  @Column(name = TableConfig.EntryTemplateSpecificField04DisplayNameResourceKeyColumnName)
  protected String specificField04DisplayNameResourceKey;

  @Column(name = TableConfig.EntryTemplateSpecificField04HelpTextResourceKeyColumnName)
  protected String specificField04HelpTextResourceKey;

  @Column(name = TableConfig.EntryTemplateShowSpecificField05ColumnName)
  protected boolean showSpecificField05 = false;

  @Column(name = TableConfig.EntryTemplateSpecificField05DisplayNameResourceKeyColumnName)
  protected String specificField05DisplayNameResourceKey;

  @Column(name = TableConfig.EntryTemplateSpecificField05HelpTextResourceKeyColumnName)
  protected String specificField05HelpTextResourceKey;

  @Column(name = TableConfig.EntryTemplateShowSpecificField06ColumnName)
  protected boolean showSpecificField06 = false;

  @Column(name = TableConfig.EntryTemplateSpecificField06DisplayNameResourceKeyColumnName)
  protected String specificField06DisplayNameResourceKey;

  @Column(name = TableConfig.EntryTemplateSpecificField06HelpTextResourceKeyColumnName)
  protected String specificField06HelpTextResourceKey;

  @Column(name = TableConfig.EntryTemplateShowSpecificField07ColumnName)
  protected boolean showSpecificField07 = false;

  @Column(name = TableConfig.EntryTemplateSpecificField07DisplayNameResourceKeyColumnName)
  protected String specificField07DisplayNameResourceKey;

  @Column(name = TableConfig.EntryTemplateSpecificField07HelpTextResourceKeyColumnName)
  protected String specificField07HelpTextResourceKey;



  protected EntryTemplate() {

  }

  public EntryTemplate(String key, String nameResourceKey, String helpTextResourceKey) {
    this.key = key;
    this.nameResourceKey = nameResourceKey;
    this.helpTextResourceKey = helpTextResourceKey;
  }

  public EntryTemplate(String key, String nameResourceKey, String helpTextResourceKey, boolean isSystemTemplate) {
    this(key, nameResourceKey, helpTextResourceKey);
    this.isSystemTemplate = isSystemTemplate;
  }

  public EntryTemplate(String key, String nameResourceKey, String helpTextResourceKey, boolean isSystemTemplate, PersonRole defaultPersonRole) {
    this(key, nameResourceKey, helpTextResourceKey, isSystemTemplate);
    this.defaultPersonRole = defaultPersonRole;
  }


  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getHelpText() {
    if(helpText != null && helpText.isEmpty() == false)
      return helpText;
    else if(helpTextResourceKey != null)
      return Localization.getLocalizedStringForResourceKey(helpTextResourceKey);

    return helpText;
  }

  public void setHelpText(String helpText) {
    this.helpText = helpText;
  }

//  public String getHelpTextResourceKey() {
//    return helpTextResourceKey;
//  }

  public boolean isSystemTemplate() {
    return isSystemTemplate;
  }


  public PersonRole getDefaultPersonRole() {
    return defaultPersonRole;
  }

  public void setDefaultPersonRole(PersonRole defaultPersonRole) {
    Object previousValue = this.defaultPersonRole;
    this.defaultPersonRole = defaultPersonRole;
    callPropertyChangedListeners(TableConfig.EntryTemplateDefaultPersonRoleJoinColumnName, previousValue, defaultPersonRole);
  }


  public boolean showTitle() {
    return showTitle;
  }

  public boolean showContent() {
    return showContent;
  }

  public boolean showCategories() {
    return showCategories;
  }

  public boolean showTags() {
    return showTags;
  }

  public boolean showIndexTerms() {
    return showIndexTerms;
  }

  public boolean showNotes() {
    return showNotes;
  }

  public boolean showEntryLinkGroups() {
    return showEntryLinkGroups;
  }

  public boolean showSubEntries() {
    return showSubEntries;
  }

  public boolean showFiles() {
    return showFiles;
  }

  public boolean showPersons() {
    return showPersons;
  }

  public boolean showSubTitle() {
    return showSubTitle;
  }

  public boolean showAbstract() {
    return showAbstract;
  }

  public boolean showTableOfContents() {
    return showTableOfContents;
  }

  public boolean showSeriesTitle() {
    return showSeriesTitle;
  }

  public boolean showReference() {
    return showReference;
  }

  public boolean showReferenceSubDivision() {
    return showReferenceSubDivision;
  }

  public boolean showReferenceStart() {
    return showReferenceStart;
  }

  public boolean showReferenceEnd() {
    return showReferenceEnd;
  }

  public boolean showPreviewImage() {
    return showPreviewImage;
  }

  public boolean showLanguage() {
    return showLanguage;
  }

  public boolean showSpecificField01() {
    return showSpecificField01;
  }

  public String getSpecificField01DisplayNameResourceKey() {
    return specificField01DisplayNameResourceKey;
  }

  public String getSpecificField01HelpTextResourceKey() {
    return specificField01HelpTextResourceKey;
  }

  public boolean showSpecificField02() {
    return showSpecificField02;
  }

  public String getSpecificField02DisplayNameResourceKey() {
    return specificField02DisplayNameResourceKey;
  }

  public String getSpecificField02HelpTextResourceKey() {
    return specificField02HelpTextResourceKey;
  }

  public boolean showSpecificField03() {
    return showSpecificField03;
  }

  public String getSpecificField03DisplayNameResourceKey() {
    return specificField03DisplayNameResourceKey;
  }

  public String getSpecificField03HelpTextResourceKey() {
    return specificField03HelpTextResourceKey;
  }

  public boolean showSpecificField04() {
    return showSpecificField04;
  }

  public String getSpecificField04DisplayNameResourceKey() {
    return specificField04DisplayNameResourceKey;
  }

  public String getSpecificField04HelpTextResourceKey() {
    return specificField04HelpTextResourceKey;
  }

  public boolean showSpecificField05() {
    return showSpecificField05;
  }

  public String getSpecificField05DisplayNameResourceKey() {
    return specificField05DisplayNameResourceKey;
  }

  public String getSpecificField05HelpTextResourceKey() {
    return specificField05HelpTextResourceKey;
  }

  public boolean showSpecificField06() {
    return showSpecificField06;
  }

  public String getSpecificField06DisplayNameResourceKey() {
    return specificField06DisplayNameResourceKey;
  }

  public String getSpecificField06HelpTextResourceKey() {
    return specificField06HelpTextResourceKey;
  }

  public boolean showSpecificField07() {
    return showSpecificField07;
  }

  public String getSpecificField07DisplayNameResourceKey() {
    return specificField07DisplayNameResourceKey;
  }

  public String getSpecificField07HelpTextResourceKey() {
    return specificField07HelpTextResourceKey;
  }


  @Override
  public String getTextRepresentation() {
    return key;
  }

  @Override
  public String toString() {
    return "EntryTemplate " + getTextRepresentation();
  }



  public static List<EntryTemplate> createSystemEntryTemplates(PersonRole defaultPersonRole) {
    return Arrays.asList(createDefaultEntryTemplate(defaultPersonRole), createThoughtEntryTemplate(defaultPersonRole), createQuotationEntryTemplate(defaultPersonRole),
        createWordDefinitionEntryTemplate(defaultPersonRole), createJournalArticleEntryTemplate(defaultPersonRole), createBookEntryTemplate(defaultPersonRole));
  }

  protected static EntryTemplate createDefaultEntryTemplate(PersonRole defaultPersonRole) {
    EntryTemplate defaultEntryTemplate = createSystemEntryTemplate(EntryTemplate.DefaultEntryTemplateKey, "entry.template.default.display.name", "entry.template.default.help.text", defaultPersonRole);
    return defaultEntryTemplate;
  }

  protected static EntryTemplate createThoughtEntryTemplate(PersonRole defaultPersonRole) {
    EntryTemplate thoughtEntryTemplate = createSystemEntryTemplate(EntryTemplate.ThoughtEntryTemplateKey, "entry.template.thought.display.name", "entry.template.thought.help.text", defaultPersonRole);

    thoughtEntryTemplate.showAbstract = true;

    return thoughtEntryTemplate;
  }

  protected static EntryTemplate createQuotationEntryTemplate(PersonRole defaultPersonRole) {
    EntryTemplate quotationEntryTemplate = createSystemEntryTemplate(EntryTemplate.QuotationEntryTemplateKey, "entry.template.quotation.display.name", "entry.template.quotation.help.text", defaultPersonRole);

    quotationEntryTemplate.showTitle = false;
    quotationEntryTemplate.showPersons = true;
    quotationEntryTemplate.showReference = true;
//    quotationEntryTemplate.showReferenceStart = true;
//    quotationEntryTemplate.showReferenceEnd = true;

    return quotationEntryTemplate;
  }

  protected static EntryTemplate createWordDefinitionEntryTemplate(PersonRole defaultPersonRole) {
    EntryTemplate wordDefinitionEntryTemplate = createSystemEntryTemplate(EntryTemplate.WordDefinitionEntryTemplateKey, "entry.template.word.definition.display.name", "entry.template.word.definition.help.text", defaultPersonRole);

    wordDefinitionEntryTemplate.showReference = true;

    return wordDefinitionEntryTemplate;
  }

  protected static EntryTemplate createJournalArticleEntryTemplate(PersonRole defaultPersonRole) {
    EntryTemplate journalArticleEntryTemplate = createSystemEntryTemplate(EntryTemplate.JournalArticleEntryTemplateKey, "entry.template.journal.article.display.name",
        "entry.template.journal.article.help.text", defaultPersonRole);

    journalArticleEntryTemplate.showSubTitle = true;
    journalArticleEntryTemplate.showAbstract = true;
    journalArticleEntryTemplate.showPersons = true;
    journalArticleEntryTemplate.showReference = true;
    journalArticleEntryTemplate.showIndexTerms = true;

    return journalArticleEntryTemplate;
  }

  protected static EntryTemplate createBookEntryTemplate(PersonRole defaultPersonRole) {
    EntryTemplate bookEntryTemplate = createSystemEntryTemplate(EntryTemplate.BookEntryTemplateKey, "entry.template.book.display.name", "entry.template.book.help.text", defaultPersonRole);

    bookEntryTemplate.showSubTitle = true;
    bookEntryTemplate.showAbstract = true;
    bookEntryTemplate.showPersons = true;
    bookEntryTemplate.showReference = true;
    bookEntryTemplate.showIndexTerms = true;

    return bookEntryTemplate;
  }

  protected static EntryTemplate createSystemEntryTemplate(String key, String displayNameResourceKey, String helpTextResourceKey, PersonRole defaultPersonRole) {
    return new EntryTemplate(key, displayNameResourceKey, helpTextResourceKey, true, defaultPersonRole);
  }

}
