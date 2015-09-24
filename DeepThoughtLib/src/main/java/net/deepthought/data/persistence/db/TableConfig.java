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
  public final static String UserUsersDefaultGroupJoinColumnName = "default_group";
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
  public final static String DeviceIconColumnName = "device_icon";
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
  public final static String DeepThoughtTopLevelEntryJoinColumnName = "top_level_entry_id";
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


  /*          Entry Table Config        */

  public final static String EntryTableName = "entry";

  public final static String EntryParentEntryJoinColumnName = "parent_entry_id";
  public final static String EntryTitleColumnName = "title";
  public final static String EntryAbstractColumnName = "abstract";
  public final static String EntryContentColumnName = "content";
  public final static String EntrySeriesTitleJoinColumnName = "series_title_id";
  public final static String EntryReferenceJoinColumnName = "reference_id";
  public final static String EntryReferenceSubDivisionJoinColumnName = "reference_sub_division_id";
  public final static String EntryIndicationColumnName = "indication";
  public final static String EntryPreviewImageJoinColumnName = "preview_image_id";

  public final static String EntryEntryIndexColumnName = "entry_index";
  public final static String EntryLanguageJoinColumnName = "language_id";

  public final static String EntryDeepThoughtJoinColumnName = "deep_thought_id";

  public final static String EntryTagsPseudoColumnName = "tags";
  public final static String EntryCategoriesPseudoColumnName = "categories";
  public final static String EntryPersonsPseudoColumnName = "persons";


  /*          Entry Tag Join Table Config        */

  public final static String EntryTagJoinTableName = "entry_tag_join_table";

  public final static String EntryTagJoinTableEntryIdColumnName = "entry_id";
  public final static String EntryTagJoinTableTagIdColumnName = "tag_id";


  /*          Entry Category Join Table Config        */

  public final static String EntryCategoryJoinTableName = "entry_category_join_table";

  public final static String EntryCategoryJoinTableEntryIdColumnName = "entry_id";
  public final static String EntryCategoryJoinTableCategoryIdColumnName = "category_id";


  /*          Entry Attached Files Join Table Config        */

  public final static String EntryAttachedFilesJoinTableName = "entry_attached_files_join_table";

  public final static String EntryAttachedFilesJoinTableEntryIdColumnName = "entry_id";
  public final static String EntryAttachedFilesJoinTableFileLinkIdColumnName = "file_id";


  /*          Entry Embedded Files Join Table Config        */

  public final static String EntryEmbeddedFilesJoinTableName = "entry_embedded_files_join_table";

  public final static String EntryEmbeddedFilesJoinTableEntryIdColumnName = "entry_id";
  public final static String EntryEmbeddedFilesJoinTableFileLinkIdColumnName = "file_id";


  /*          Entry EntriesLinkGroup Join Table Config        */

  public final static String EntryEntriesLinkGroupJoinTableName = "entry_link_group_join_table";

  public final static String EntryEntriesLinkGroupJoinTableEntryIdColumnName = "entry_id";
  public final static String EntryEntriesLinkGroupJoinTableLinkGroupIdColumnName = "link_group_id";


  /*          EntriesLinkGroup Config        */

  public final static String EntriesLinkGroupTableName = "entries_link_group";

  public final static String EntriesLinkGroupGroupNameColumnName = "name";
  public final static String EntriesLinkGroupNotesColumnName = "notes";


  /*          Tag Table Config        */

  public final static String TagTableName = "tag";

  public final static String TagNameColumnName = "name";
  public final static String TagDescriptionColumnName = "description";
  public final static String TagDeepThoughtJoinColumnName = "deep_thought_id";


  /*          Category Table Config        */

  public final static String CategoryTableName = "category";

  public final static String CategoryNameColumnName = "name";
  public final static String CategoryDescriptionColumnName = "description";
  public final static String CategoryIsExpandedColumnName = "is_expanded";
  public final static String CategoryCategoryOrderColumnName = "category_order";
  public final static String CategoryParentCategoryJoinColumnName = "parent_category_id";
  public final static String CategoryDeepThoughtJoinColumnName = "deep_thought_id";


  /*          Person Table Config        */

  public final static String PersonTableName = "person";

  public final static String PersonFirstNameColumnName = "first_name";
  public final static String PersonLastNameColumnName = "last_name";
  public final static String PersonNotesColumnName = "notes";
  public final static String PersonDeepThoughtJoinColumnName = "deep_thought_id";


  /*          EntryPersonJoinTable Table Config        */

  public final static String EntryPersonAssociationTableName = "entry_person_association";

  public final static String EntryPersonAssociationEntryJoinColumnName = "entry_id";
  public final static String EntryPersonAssociationPersonJoinColumnName = "person_id";
  public final static String EntryPersonAssociationPersonOrderColumnName = "person_order";


  /*          Note Table Config        */

  public final static String NoteTableName = "notes";

  public final static String NoteNoteColumnName = "notes";
  public final static String NoteNoteTypeJoinColumnName = "note_type_id";
  public final static String NoteEntryJoinColumnName = "entry_id";
  public final static String NoteDeepThoughtJoinColumnName = "deep_thought_id";


  /*          FileLink Table Config        */

  public final static String FileLinkTableName = "file";

  public final static String FileLinkUriColumnName = "uri";
  public final static String FileLinkNameColumnName = "name";
  public final static String FileLinkIsFolderColumnName = "folder";
  public final static String FileLinkFileTypeColumnName = "file_type";
  public final static String FileLinkDescriptionColumnName = "description";
  public final static String FileLinkSourceUriColumnName = "source_uri";
  public final static String FileLinkDeepThoughtJoinColumnName = "deep_thought_id";


  /*          ReferenceBase Table Config        */

  public final static String ReferenceBaseTableName = "reference_base";

  public final static String ReferenceBaseDiscriminatorColumnName = "REF_TYPE";
  public final static String ReferenceBaseTitleColumnName = "title";
  public final static String ReferenceBaseSubTitleColumnName = "sub_title";
  public final static String ReferenceBaseAbstractColumnName = "abstract";
  public final static String ReferenceBaseOnlineAddressColumnName = "online_address";
  public final static String ReferenceBaseNotesColumnName = "notes";
  public final static String ReferenceBasePreviewImageJoinColumnName = "preview_image_id";


  /*          ReferenceBasePersonJoinTable Table Config        */

  public final static String ReferenceBasePersonAssociationTableName = "reference_base_person_association";

  public final static String ReferenceBasePersonAssociationReferenceBaseJoinColumnName = "reference_base_id";
  public final static String ReferenceBasePersonAssociationPersonJoinColumnName = "person_id";
  public final static String ReferenceBasePersonAssociationPersonOrderColumnName = "person_order";


  /*          ReferenceBase Attached Files Join Table Config        */

  public final static String ReferenceBaseAttachedFileJoinTableName = "reference_base_attached_files_join_table";

  public final static String ReferenceBaseAttachedFileJoinTableReferenceBaseIdColumnName = "reference_base_id";
  public final static String ReferenceBaseAttachedFileJoinTableFileLinkIdColumnName = "file_id";


  /*          ReferenceBase Embedded Files Join Table Config        */

  public final static String ReferenceBaseEmbeddedFileJoinTableName = "reference_base_embedded_files_join_table";

  public final static String ReferenceBaseEmbeddedFileJoinTableReferenceBaseIdColumnName = "reference_base_id";
  public final static String ReferenceBaseEmbeddedFileJoinTableFileLinkIdColumnName = "file_id";


  /*          SeriesTitle Table Config        */

  public final static String SeriesTitleTableName = "series_title";
  public final static String SeriesTitleDiscriminatorValue = "SERIES_TITLE";

  public final static String SeriesTitleTableOfContentsColumnName = "table_of_contents";
  public final static String SeriesTitleDeepThoughtJoinColumnName = "deep_thought_id";


  /*          Reference Table Config        */

  public final static String ReferenceTableName = "reference";
  public final static String ReferenceDiscriminatorValue = "REFERENCE";

  public final static String ReferenceSeriesTitleJoinColumnName = "series_title_id";
  public final static String ReferenceSeriesTitleOrderColumnName = "series_order";
  public final static String ReferenceTableOfContentsColumnName = "table_of_contents";
  public final static String ReferenceIssueOrPublishingDateColumnName = "issue_or_publishing_date";
  public final static String ReferencePublishingDateColumnName = "publishing_date";
  public final static String ReferenceDeepThoughtJoinColumnName = "deep_thought_id";


  /*          ReferenceSubDivision Table Config        */

  public final static String ReferenceSubDivisionTableName = "reference_sub_division";
  public final static String ReferenceSubDivisionDiscriminatorValue = "SUB_DIVISION";

  public final static String ReferenceSubDivisionReferenceJoinColumnName = "reference_id";
  public final static String ReferenceSubDivisionParentSubDivisionJoinColumnName = "parent_sub_division_id";
  public final static String ReferenceSubDivisionOrderColumnName = "sub_division_order"; // Derby also doesn't like 'order' as column name
  public final static String ReferenceSubDivisionDeepThoughtJoinColumnName = "deep_thought_id";



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

  public final static String LanguageLanguageKeyColumnName = "language_key";
  public final static String LanguageNameInLanguageColumnName = "name_in_language";


  /*          NoteType Table Config        */

  public final static String NoteTypeTableName = "note_type";


  /*          FileType Table Config        */

  public final static String FileTypeTableName = "file_type";

  public final static String FileTypeFolderNameColumnName = "folder";
  public final static String FileTypeIconColumnName = "icon";


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
