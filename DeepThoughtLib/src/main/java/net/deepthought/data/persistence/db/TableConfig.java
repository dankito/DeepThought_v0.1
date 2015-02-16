package net.deepthought.data.persistence.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Created by ganymed on 13/12/14.
 */
public class TableConfig {


  /*          BaseEntity Column Names        */

  public final static String BaseEntityIdColumnName = "id";
  public final static String BaseEntityCreatedOnColumnName = "created_on";
  public final static String BaseEntityModifiedOnColumnName = "modified_on";
  public final static String BaseEntityVersionColumnName = "version";
  public final static String BaseEntityDeletedColumnName = "deleted";


  /*          UserDataEntity Column Names        */

  public final static String UserDataEntityCreatedByJoinColumnName = "created_by";
  public final static String UserDataEntityModifiedByJoinColumnName = "modified_by";
  public final static String UserDataEntityDeletedByJoinColumnName = "deleted_by";
  public final static String UserDataEntityOwnerJoinColumnName = "owner";


  /*          DeepThoughtApplication Table Config        */

  public final static String DeepThoughtApplicationTableName = "application";

  public final static String DeepThoughtApplicationAppSettingsJoinColumnName = "app_settings_id";
  public final static String DeepThoughtApplicationDataModelVersionColumnName = "data_model_version";
  public final static String DeepThoughtApplicationLastLoggedOnUserJoinColumnName = "last_logged_on_user_id";
  public final static String DeepThoughtApplicationAutoLogOnLastLoggedOnUserColumnName = "auto_log_on_last_logged_on_user";
  public final static String DeepThoughtApplicationLocalDeviceJoinColumnName = "local_device_id";


  /*          AppSettings Table Config        */

  public final static String AppSettingsTableName = "app_settings";

  public final static String AppSettingsDataModelVersionColumnName = "data_model_version";
  public final static String AppSettingsLastLoggedOnUserJoinColumnName = "last_logged_on_user_id";
  public final static String AppSettingsAutoLogOnLastLoggedOnUserColumnName = "auto_log_on_last_logged_on_user";
  public final static String AppSettingsAutoSaveChangesColumnName = "auto_save_changes";
  public final static String AppSettingsAutoSaveChangesAfterMillisecondsColumnName = "auto_save_changes_after_milliseconds";
  public final static String AppSettingsMaxBackupsToKeepColumnName = "max_backups_to_keep";


  /*          User Table Config        */
  
  public final static String UserTableName = "user_dt"; // 'user' is not allowed as table name as it's a system table, so i used user_dt (for _deep_thought)

  public final static String UserUniversallyUniqueIdColumnName = "universally_unique_id";
  public final static String UserUserNameColumnName = "user_name";
  public final static String UserFirstNameColumnName = "first_name";
  public final static String UserLastNameColumnName = "last_name";
  public final static String UserPasswordColumnName = "password";
  public final static String UserIsLocalUserColumnName = "is_local_user";
  public final static String UserUserDeviceSettingsColumnName = "settings";
  public final static String UserLastViewedDeepThoughtColumnName = "last_viewed_deep_thought";
  public final static String UserDeepThoughtApplicationJoinColumnName = "application_id";


  /*          User Device Join Table Config        */

  public final static String UserDeviceJoinTableName = "user_device_join_table";

  public final static String UserDeviceJoinTableUserIdColumnName = "user_id";
  public final static String UserDeviceJoinTableDeviceIdColumnName = "device_id";


  /*          User Group Join Table Config        */
  
  public final static String UserGroupJoinTableName = "user_group_join_table";

  public final static String UserGroupJoinTableUserIdColumnName = "user_id";
  public final static String UserGroupJoinTableGroupIdColumnName = "group_id";


  /*          Group Table Config        */

  public final static String GroupTableName = "group_dt"; // 'group' is not allowed as table name as it's a system table, so i used group_dt (for _deep_thought)

  public final static String GroupUniversallyUniqueIdColumnName = "universally_unique_id";
  public final static String GroupNameColumnName = "name";
  public final static String GroupDescriptionColumnName = "description";
  public final static String GroupDeepThoughtApplicationJoinColumnName = "application_id";


  /*          Device Table Config        */

  public final static String DeviceTableName = "device";

  public final static String DeviceUniversallyUniqueIdColumnName = "universally_unique_id";
  public final static String DeviceNameColumnName = "name";
  public final static String DeviceDescriptionColumnName = "description";
  public final static String DevicePlatformColumnName = "platform";
  public final static String DevicePlatformArchitectureColumnName = "platform_architecture";
  public final static String DeviceOsVersionColumnName = "os_version";
  public final static String DeviceLastKnownIpColumnName = "last_known_ip";
  public final static String DeviceUserRegionColumnName = "user_region";
  public final static String DeviceUserLanguageColumnName = "user_language";
  public final static String DeviceUserTimezoneColumnName = "user_timezone";
  public final static String DeviceJavaRuntimeVersionColumnName = "java_runtime_version";
  public final static String DeviceJavaClassVersionColumnName = "java_class_version";
  public final static String DeviceJavaSpecificationVersionColumnName = "java_specification_version";
  public final static String DeviceJavaVirtualMachineVersionColumnName = "java_vm_version";
  public final static String DeviceOwnerJoinColumnName = "owner_id";
  public final static String DeviceDeepThoughtApplicationJoinColumnName = "application_id";


  /*          Group Device Join Table Config        */

  public final static String GroupDeviceJoinTableName = "group_device_join_table";

  public final static String GroupDeviceJoinTableGroupIdColumnName = "group_id";
  public final static String GroupDeviceJoinTableDeviceIdColumnName = "device_id";


  /*          DeepThought Table Config        */

  public final static String DeepThoughtTableName = "deep_thought";

  public final static String DeepThoughtNextEntryIndexColumnName = "next_entry_index";
  public final static String DeepThoughtTopLevelCategoryJoinColumnName = "top_level_category_id";
  public final static String DeepThoughtDeepThoughtOwnerJoinColumnName = "owner_id";
  public final static String DeepThoughtDefaultEntryTemplateJoinColumnName = "default_entry_template_id";
  public final static String DeepThoughtDeepThoughtSettingsColumnName = "settings";
  public final static String DeepThoughtLastViewedCategoryJoinColumnName = "last_viewed_category_id";
  public final static String DeepThoughtLastViewedTagJoinColumnName = "last_viewed_tag_id";
  public final static String DeepThoughtLastViewedIndexTermJoinColumnName = "last_viewed_index_term_id";
  public final static String DeepThoughtLastViewedEntryJoinColumnName = "last_viewed_entry_id";
  public final static String DeepThoughtLastSelectedTabColumnName = "selected_tab";
  public final static String DeepThoughtLastSelectedAndroidTabColumnName = "selected_android_tab";


  /*          DeepThought FavoriteEntryTemplate Join Table Config        */

  public final static String DeepThoughtFavoriteEntryTemplateJoinTableName = "deep_thought_favorite_entry_template_join_table";

  public final static String DeepThoughtFavoriteEntryTemplateJoinTableDeepThoughtIdColumnName = "deep_thought_id";
  public final static String DeepThoughtFavoriteEntryTemplateJoinTableEntryTemplateIdColumnName = "favorite_entry_template_id";
  public final static String DeepThoughtFavoriteEntryTemplateJoinTableEntryTemplateKeyColumnName = "template_key";
  public final static String DeepThoughtFavoriteEntryTemplateJoinTableEntryTemplateIndexColumnName = "favorite_index";
  public final static String DeepThoughtFavoriteEntryTemplateJoinTableDeepThoughtJoinColumnName = "deep_thought_id";


  /*          Category Table Config        */

  public final static String CategoryTableName = "category";

  public final static String CategoryNameColumnName = "name";
  public final static String CategoryDescriptionColumnName = "description";
  public final static String CategoryIsExpandedColumnName = "is_expanded";
  public final static String CategoryCategoryOrderColumnName = "category_order";
  public final static String CategoryDefaultEntryTemplateJoinColumnName = "default_entry_template_id";
//  public final static String CategoryDefaultEntryTemplateKeyColumnName = "default_entry_template_key";
  public final static String CategoryParentCategoryJoinColumnName = "parent_category_id";
  public final static String CategoryDeepThoughtJoinColumnName = "deep_thought_id";


  /*          Category Entry Join Table Config        */

  public final static String CategoryEntryJoinTableName = "category_entry_join_table";

  public final static String CategoryEntryJoinTableCategoryIdColumnName = "category_id";
  public final static String CategoryEntryJoinTableEntryIdColumnName = "entry_id";


  /*          Entry Table Config        */

  public final static String EntryTableName = "entry";

  public final static String EntryParentEntryJoinColumnName = "parent_entry_id";
  public final static String EntryTitleColumnName = "title";
  public final static String EntrySubTitleColumnName = "sub_title";
  public final static String EntryAbstractColumnName = "abstract";
  public final static String EntryContentColumnName = "content";
  public final static String EntryContentFormatColumnName = "content_format";
  public final static String EntrySeriesTitleJoinColumnName = "series_title_id";
  public final static String EntryReferenceJoinColumnName = "reference_id";
  public final static String EntryReferenceSubDivisionJoinColumnName = "reference_sub_division_id";
  public final static String EntryReferenceStartColumnName = "reference_start";
  public final static String EntryReferenceStartUnitJoinColumnName = "reference_start_unit_id";
  public final static String EntryReferenceEndColumnName = "reference_end";
  public final static String EntryReferenceEndUnitJoinColumnName = "reference_end_unit_id";
  public final static String EntryPreviewImageJoinColumnName = "preview_image_id";
  public final static String EntryEvaluationColumnName = "evaluation";
  public final static String EntryRatingColumnName = "rating";

  public final static String EntryEntryIndexColumnName = "entry_index";
    public final static String EntryEntryTemplateJoinColumnName = "template_id";
//  public final static String EntryEntryTemplateKeyColumnName = "template";
  public final static String EntryLanguageJoinColumnName = "language_id";

  public final static String EntrySpecificField01ColumnName = "specific_field_01";
  public final static String EntrySpecificField02ColumnName = "specific_field_02";
  public final static String EntrySpecificField03ColumnName = "specific_field_03";
  public final static String EntrySpecificField04ColumnName = "specific_field_04";
  public final static String EntrySpecificField05ColumnName = "specific_field_05";
  public final static String EntrySpecificField06ColumnName = "specific_field_06";
  public final static String EntrySpecificField07ColumnName = "specific_field_07";
  public final static String EntryCustomField01ColumnName = "custom_field_01";
  public final static String EntryCustomField02ColumnName = "custom_field_02";
  public final static String EntryCustomField03ColumnName = "custom_field_03";
  public final static String EntryCustomField04ColumnName = "custom_field_04";
  public final static String EntryCustomField05ColumnName = "custom_field_05";
  public final static String EntryCustomField06ColumnName = "custom_field_06";
  public final static String EntryCustomField07ColumnName = "custom_field_07";
  public final static String EntryCustomField08ColumnName = "custom_field_08";
  public final static String EntryCustomField09ColumnName = "custom_field_09";
  public final static String EntryDeepThoughtJoinColumnName = "deep_thought_id";


  /*          Entry Tag Join Table Config        */

  public final static String EntryTagJoinTableName = "entry_tag_join_table";

  public final static String EntryTagJoinTableEntryIdColumnName = "entry_id";
  public final static String EntryTagJoinTableTagIdColumnName = "tag_id";


  /*          Entry IndexTerm Join Table Config        */

  public final static String EntryIndexTermJoinTableName = "entry_index_term_join_table";

  public final static String EntryIndexTermJoinTableEntryIdColumnName = "entry_id";
  public final static String EntryIndexTermJoinTableIndexTermIdColumnName = "index_term_id";


  /*          Entry Authors Join Table Config        */

  public final static String EntryAuthorsJoinTableName = "entry_authors_join_table";

  public final static String EntryAuthorsJoinTableEntryIdColumnName = "entry_id";
  public final static String EntryAuthorsJoinTablePersonIdColumnName = "person_id";


  /*          Entry Note Join Table Config        */

  public final static String EntryNoteJoinTableName = "entry_note_join_table";

  public final static String EntryNoteJoinTableEntryIdColumnName = "entry_id";
  public final static String EntryNoteJoinTableNoteIdColumnName = "note_id";


  /*          Entry EntriesLinkGroup Join Table Config        */

  public final static String EntryEntriesLinkGroupJoinTableName = "entry_link_group_join_table";

  public final static String EntryEntriesLinkGroupJoinTableEntryIdColumnName = "entry_id";
  public final static String EntryEntriesLinkGroupJoinTableLinkGroupIdColumnName = "link_group_id";


  /*          Entry FileLink Join Table Config        */

  public final static String EntryFileJoinTableName = "entry_file_join_table";

  public final static String EntryFileJoinTableEntryIdColumnName = "entry_id";
  public final static String EntryFileJoinTableFileIdColumnName = "file_id";


  /*          Entry Editors Join Table Config        */

  public final static String EntryEditorsJoinTableName = "entry_editors_join_table";

  public final static String EntryEditorsJoinTableEntryIdColumnName = "entry_id";
  public final static String EntryEditorsJoinTablePersonIdColumnName = "person_id";


  /*          Entry Collaborators Join Table Config        */

  public final static String EntryCollaboratorsJoinTableName = "entry_collaborators_join_table";

  public final static String EntryCollaboratorsJoinTableEntryIdColumnName = "entry_id";
  public final static String EntryCollaboratorsJoinTablePersonIdColumnName = "person_id";


  /*          Entry OthersInvolved Join Table Config        */

  public final static String EntryOthersInvolvedJoinTableName = "entry_others_involved_join_table";

  public final static String EntryOthersInvolvedJoinTableEntryIdColumnName = "entry_id";
  public final static String EntryOthersInvolvedJoinTablePersonIdColumnName = "person_id";


  /*          EntryTemplate Table Config        */

  public final static String EntryTemplateTableName = "entry_template";

  public final static String EntryTemplateKeyColumnName = "template_key"; // Derby doesn't like 'key' as column name
  public final static String EntryTemplateNameResourceKeyColumnName = "name_resource_key";
  public final static String EntryTemplateHelpTextColumnName = "help_text";
  public final static String EntryTemplateHelpTextResourceKeyColumnName = "help_text_resource_key";
  public final static String EntryTemplateIsSystemTemplateColumnName = "is_system_template";
  public final static String EntryTemplateSortOrderColumnName = "sort_order";

  public final static String EntryTemplateDefaultPersonRoleJoinColumnName = "default_person_role_id";
  public final static String EntryTemplateShowTitleColumnName = "show_title";
  public final static String EntryTemplateShowContentColumnName = "show_content";
  public final static String EntryTemplateShowCategoriesColumnName = "show_categories";
  public final static String EntryTemplateShowTagsColumnName = "show_tags";
  public final static String EntryTemplateShowIndexTermsColumnName = "show_index_terms";
  public final static String EntryTemplateShowNotesColumnName = "show_notes";
  public final static String EntryTemplateShowEntryLinkGroupsColumnName = "show_entry_link_groups";
  public final static String EntryTemplateShowSubEntriesColumnName = "show_sub_entries";
  public final static String EntryTemplateShowFilesColumnName = "show_files";
  public final static String EntryTemplateShowPersonsColumnName = "show_persons";
  public final static String EntryTemplateShowSubTitleColumnName = "show_sub_title";
  public final static String EntryTemplateShowAbstractColumnName = "show_abstract";
  public final static String EntryTemplateShowTableOfContentsColumnName = "show_table_of_contents";
  public final static String EntryTemplateShowSeriesTitleColumnName = "show_series_title";
  public final static String EntryTemplateShowReferenceColumnName = "show_reference";
  public final static String EntryTemplateShowReferenceSubDivisionColumnName = "show_reference_sub_division";
  public final static String EntryTemplateShowPublishingDateColumnName = "show_publishing_date";
  public final static String EntryTemplatePublishingDateDateFormatColumnName = "publishing_date_date_format";
  public final static String EntryTemplateShowIsbnOrIssnSupplementColumnName = "show_isbn_or_issn";
  public final static String EntryTemplateShowPeriodicalColumnName = "show_periodical";
  public final static String EntryTemplateShowIssueColumnName = "show_issue";
  public final static String EntryTemplateShowDoiSupplementColumnName = "show_doi";
  public final static String EntryTemplateShowEditionColumnName = "show_edition";
  public final static String EntryTemplateShowVolumeColumnName = "show_volume";
  public final static String EntryTemplateShowPublisherSupplementColumnName = "show_publisher";
  public final static String EntryTemplateShowPlaceOfPublicationColumnName = "show_place_of_publication";
  public final static String EntryTemplateShowPageCountColumnName = "show_page_count";
  public final static String EntryTemplateShowStartPageColumnName = "show_start_page";
  public final static String EntryTemplateShowEndPageColumnName = "show_end_page";
  public final static String EntryTemplateShowPriceSupplementColumnName = "show_price";
  public final static String EntryTemplateShowLanguageColumnName = "show_language";
  public final static String EntryTemplateShowPreviewImageColumnName = "show_preview_image";
  public final static String EntryTemplateShowOnlineAddressColumnName = "show_online_address";
  public final static String EntryTemplateShowAccessDateColumnName = "show_access_date";


  public final static String EntryTemplateShowSpecificField01ColumnName = "show_specific_field_01";
  public final static String EntryTemplateSpecificField01DisplayNameResourceKeyColumnName = "specific_field_01_display_name_resource_key";
  public final static String EntryTemplateSpecificField01HelpTextResourceKeyColumnName = "specific_field_01_help_text_resource_key";
  public final static String EntryTemplateShowSpecificField02ColumnName = "show_specific_field_02";
  public final static String EntryTemplateSpecificField02DisplayNameResourceKeyColumnName = "specific_field_02_display_name_resource_key";
  public final static String EntryTemplateSpecificField02HelpTextResourceKeyColumnName = "specific_field_02_help_text_resource_key";
  public final static String EntryTemplateShowSpecificField03ColumnName = "show_specific_field_03";
  public final static String EntryTemplateSpecificField03DisplayNameResourceKeyColumnName = "specific_field_03_display_name_resource_key";
  public final static String EntryTemplateSpecificField03HelpTextResourceKeyColumnName = "specific_field_03_help_text_resource_key";
  public final static String EntryTemplateShowSpecificField04ColumnName = "show_specific_field_04";
  public final static String EntryTemplateSpecificField04DisplayNameResourceKeyColumnName = "specific_field_04_display_name_resource_key";
  public final static String EntryTemplateSpecificField04HelpTextResourceKeyColumnName = "specific_field_04_help_text_resource_key";
  public final static String EntryTemplateShowSpecificField05ColumnName = "show_specific_field_05";
  public final static String EntryTemplateSpecificField05DisplayNameResourceKeyColumnName = "specific_field_05_display_name_resource_key";
  public final static String EntryTemplateSpecificField05HelpTextResourceKeyColumnName = "specific_field_05_help_text_resource_key";
  public final static String EntryTemplateShowSpecificField06ColumnName = "show_specific_field_06";
  public final static String EntryTemplateSpecificField06DisplayNameResourceKeyColumnName = "specific_field_06_display_name_resource_key";
  public final static String EntryTemplateSpecificField06HelpTextResourceKeyColumnName = "specific_field_06_help_text_resource_key";
  public final static String EntryTemplateShowSpecificField07ColumnName = "show_specific_field_07";
  public final static String EntryTemplateSpecificField07DisplayNameResourceKeyColumnName = "specific_field_07_display_name_resource_key";
  public final static String EntryTemplateSpecificField07HelpTextResourceKeyColumnName = "specific_field_07_help_text_resource_key";


  /*          EntriesLinkGroup Config        */

  public final static String EntriesLinkGroupTableName = "entries_link_group";

  public final static String EntriesLinkGroupGroupNameColumnName = "name";
  public final static String EntriesLinkGroupNotesColumnName = "notes";


  /*          Tag Table Config        */

  public final static String TagTableName = "tag";

  public final static String TagNameColumnName = "name";
  public final static String TagDescriptionColumnName = "description";
  public final static String TagDeepThoughtJoinColumnName = "deep_thought_id";


  /*          IndexTerm Table Config        */

  public final static String IndexTermTableName = "index_term";

  public final static String IndexTermNameColumnName = "name";
  public final static String IndexTermDescriptionColumnName = "description";
  public final static String IndexTermDeepThoughtJoinColumnName = "deep_thought_id";


  /*          Person Table Config        */

  public final static String PersonTableName = "person";

  public final static String PersonFirstNameColumnName = "first_name";
  public final static String PersonMiddleNamesColumnName = "middle_names";
  public final static String PersonLastNameColumnName = "last_name";
  public final static String PersonTitleColumnName = "title";
  public final static String PersonPrefixColumnName = "prefix";
  public final static String PersonSuffixColumnName = "suffix";
  public final static String PersonAbbreviationColumnName = "abbreviation";
  public final static String PersonGenderColumnName = "gender";
  public final static String PersonBirthDayColumnName = "birth_day";
  public final static String PersonNotesColumnName = "notes";
  public final static String PersonSortByColumnName = "sort_by";
  public final static String PersonDeepThoughtJoinColumnName = "deep_thought_id";


  /*          EntryPersonRoles Table Config        */

  public final static String EntryPersonRolesTableName = "entry_person_roles";

  public final static String EntryPersonRolesPersonRoleJoinColumnName = "person_role_id";
  public final static String EntryPersonRolesEntryJoinColumnName = "entry_id";


  /*          EntryPersonJoinTable Table Config        */

  public final static String EntryPersonAssociationTableName = "entry_person_association";

  public final static String EntryPersonAssociationEntryJoinColumnName = "entry_id";
  public final static String EntryPersonAssociationPersonJoinColumnName = "person_id";
  public final static String EntryPersonAssociationPersonRoleJoinColumnName = "person_role_id";
  public final static String EntryPersonAssociationPersonOrderColumnName = "person_order";


  /*          PersonRole Table Config        */

  public final static String PersonRoleTableName = "person_role";


  /*          Note Table Config        */

  public final static String NoteTableName = "notes";

  public final static String NoteNoteColumnName = "notes";
  public final static String NoteNoteTypeJoinColumnName = "note_type_id";
  public final static String NoteEntryJoinColumnName = "entry_id";


  /*          FileLink Table Config        */

  public final static String FileLinkTableName = "file";

  public final static String FileLinkUriColumnName = "uri";
  public final static String FileLinkNameColumnName = "name";
  public final static String FileLinkIsFolderColumnName = "folder";
  public final static String FileLinkNotesColumnName = "notes";
  public final static String FileLinkPreviewImageJoinColumnName = "preview_image_file_id";
  public final static String FileLinkEntryJoinColumnName = "entry_id";
  public final static String FileLinkReferenceBaseJoinColumnName = "reference_base_id";


  /*          ReferenceBase Table Config        */

  public final static String ReferenceBaseTableName = "reference_base";

  public final static String ReferenceBaseDiscriminatorColumnName = "REF_TYPE";
  public final static String ReferenceBaseTitleColumnName = "title";
  public final static String ReferenceBaseSubTitleColumnName = "sub_title";
  public final static String ReferenceBaseAbstractColumnName = "abstract";
  public final static String ReferenceBaseOnlineAddressColumnName = "online_address";
  public final static String ReferenceBaseLastAccessDateColumnName = "last_access_date";
  public final static String ReferenceBaseNotesColumnName = "notes";


  /*          ReferenceBasePersonJoinTable Table Config        */

  public final static String ReferenceBasePersonAssociationTableName = "reference_base_person_association";

  public final static String ReferenceBasePersonAssociationReferenceBaseJoinColumnName = "reference_base_id";
  public final static String ReferenceBasePersonAssociationPersonJoinColumnName = "person_id";
  public final static String ReferenceBasePersonAssociationPersonRoleJoinColumnName = "person_role_id";
  public final static String ReferenceBasePersonAssociationPersonOrderColumnName = "person_order";


  /*          Reference Table Config        */

  public final static String ReferenceTableName = "reference";
  public final static String ReferenceDiscriminatorValue = "REFERENCE";

  public final static String ReferenceSeriesTitleJoinColumnName = "series_title_id";
  public final static String ReferenceSeriesTitleOrderColumnName = "series_order";
  public final static String ReferenceCategoryJoinColumnName = "reference_category_id";
  public final static String ReferenceTitleSupplementColumnName = "title_supplement";
  public final static String ReferenceTableOfContentsColumnName = "table_of_contents";
  public final static String ReferencePublishingDateColumnName = "publishing_date";
  public final static String ReferenceIsbnOrIssnColumnName = "isbn_or_issn";
  public final static String ReferenceIssueColumnName = "issue";
  public final static String ReferenceYearColumnName = "year_ref"; // Derby doesn't like 'year' as column name
  public final static String ReferenceDoiColumnName = "doi";
  public final static String ReferenceEditionColumnName = "edition";
  public final static String ReferenceVolumeColumnName = "volume";
  public final static String ReferencePublisherJoinColumnName = "publisher_id";
  public final static String ReferencePlaceOfPublicationColumnName = "place_of_publication";
  public final static String ReferenceLengthColumnName = "length";
  public final static String ReferenceLengthUnitJoinColumnName = "length_unit_id";
  public final static String ReferencePriceColumnName = "price";
  public final static String ReferenceLanguageJoinColumnName = "language_id";


  /*          ReferenceCategory Table Config        */

  public final static String ReferenceCategoryTableName = "reference_category";

  public final static String ReferenceCategoryNameColumnName = "name";


  /*          ReferenceSubDivision Table Config        */

  public final static String ReferenceSubDivisionTableName = "reference_sub_division";
  public final static String ReferenceSubDivisionDiscriminatorValue = "SUB_DIVISION";

  public final static String ReferenceSubDivisionReferenceJoinColumnName = "reference_id";
  public final static String ReferenceSubDivisionCategoryJoinColumnName = "category_id";
  public final static String ReferenceSubDivisionParentSubDivisionJoinColumnName = "parent_sub_division_id";
  public final static String ReferenceSubDivisionOrderColumnName = "sub_division_order"; // Derby also doesn't like 'order' as column name
  public final static String ReferenceSubDivisionLengthColumnName = "length";
  public final static String ReferenceSubDivisionLengthUnitJoinColumnName = "length_unit_id";


  /*          ReferenceSubDivisionCategory Table Config        */

  public final static String ReferenceSubDivisionCategoryTableName = "reference_sub_division_category";

  public final static String ReferenceSubDivisionCategoryNameColumnName = "name";


  /*          SeriesTitle Table Config        */

  public final static String SeriesTitleTableName = "series_title";
  public final static String SeriesTitleDiscriminatorValue = "SERIES_TITLE";

  public final static String SeriesTitleCategoryJoinColumnName = "series_title_category_id";
  public final static String SeriesTitleTitleSupplementColumnName = "title_supplement";
  public final static String SeriesTitleTableOfContentsColumnName = "table_of_contents";
  public final static String SeriesTitleFirstDayOfPublicationColumnName = "first_day_of_publication";
  public final static String SeriesTitleLastDayOfPublicationColumnName = "last_day_of_publication";
  public final static String SeriesTitleStandardAbbreviationColumnName = "standard_abbreviation";
  public final static String SeriesTitleUserAbbreviation1ColumnName = "user_abbreviation_1";
  public final static String SeriesTitleUserAbbreviation2ColumnName = "user_abbreviation_2";
  public final static String SeriesTitlePublisherJoinColumnName = "publisher_id";


  /*          SeriesTitleCategory Table Config        */

  public final static String SeriesTitleCategoryTableName = "series_title_category";

  public final static String SeriesTitleCategoryNameColumnName = "name";



  /*          Publisher Table Config        */

  public final static String PublisherTableName = "publisher";

  public final static String PublisherNameColumnName = "name";
  public final static String PublisherNotesColumnName = "notes";
  public final static String PublisherDeepThoughtJoinColumnName = "deep_thought_id";



  /*          ExtensibleEnumeration Table Config        */

  public final static String ExtensibleEnumerationNameColumnName = "name";
  public final static String ExtensibleEnumerationNameResourceKeyColumnName = "name_resource_key";
  public final static String ExtensibleEnumerationDescriptionColumnName = "description";
  public final static String ExtensibleEnumerationSortOrderColumnName = "sort_order";
  public final static String ExtensibleEnumerationIsSystemValueColumnName = "is_system_value";
  public final static String ExtensibleEnumerationIsDeletableColumnName = "is_deletable";
  public final static String ExtensibleEnumerationDeepThoughtJoinColumnName = "deep_thought_id";


  /*          ApplicationLanguage Table Config        */

  public final static String ApplicationLanguageTableName = "application_language";

  public final static String ApplicationLanguageLanguageKeyColumnName = "language_key";
  public final static String ApplicationLanguageDeepThoughtApplicationJoinColumnName = "application_id";


  /*          Language Table Config        */

  public final static String LanguageTableName = "language";

  public final static String LanguageSortOrderColumnName = "sort_order";


  /*          LengthType Table Config        */

  public final static String ReferenceIndicationUnitTableName = "reference_indication_unit";

  public final static String ReferenceIndicationUnitSortOrderColumnName = "sort_order";


  /*          NoteType Table Config        */

  public final static String NoteTypeTableName = "note_type";


  /*          BackupFileServiceType Table Config        */

  public final static String BackupFileServiceTypeTableName = "backup_file_service_type";



  private final static Logger log = LoggerFactory.getLogger(TableConfig.class);

  public static String getTableNameForClass(Class<? extends BaseEntity> type) {
    if(type.isAnnotationPresent(Table.class)) {
      Table tableAnnotation = type.getAnnotation(Table.class);
      return tableAnnotation.name();
    }
    else if(type.isAnnotationPresent(Entity.class)) {
      Entity entityAnnotation = type.getAnnotation(Entity.class);
      return entityAnnotation.name();
    }

    log.error("Could not get Table name for Class " + type);
//    throw new Exception("Could not get Table name for Class " + type);

    return "";
  }
  
}
