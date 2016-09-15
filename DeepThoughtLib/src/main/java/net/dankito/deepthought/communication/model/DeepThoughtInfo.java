package net.dankito.deepthought.communication.model;

import net.dankito.deepthought.data.model.DeepThought;
import net.dankito.deepthought.data.model.enums.BackupFileServiceType;
import net.dankito.deepthought.data.model.enums.FileType;
import net.dankito.deepthought.data.model.enums.Language;
import net.dankito.deepthought.data.model.enums.NoteType;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ganymed on 07/09/16.
 */
public class DeepThoughtInfo {

  protected String databaseId = "";

  protected int countEntries;

  protected int countTags;

  protected int countCategories;

  protected int countReferenceBases;

  protected int countPersons;

  protected int countFiles;


  protected String topLevelEntryId;

  protected String topLevelCategoryId;

  protected Map<String, String> noteTypeIds = new HashMap<>();

  protected Map<String, String> fileTypeIds = new HashMap<>();

  protected Map<String, String> languageIds = new HashMap<>();

  protected Map<String, String> backupFileServiceTypesIds = new HashMap<>();


  public DeepThoughtInfo(String databaseId, int countEntries, int countTags, int countCategories, int countReferenceBases, int countPersons, int countFiles) {
    this.databaseId = databaseId;
    this.countEntries = countEntries;
    this.countTags = countTags;
    this.countCategories = countCategories;
    this.countReferenceBases = countReferenceBases;
    this.countPersons = countPersons;
    this.countFiles = countFiles;
  }


  public String getDatabaseId() {
    return databaseId;
  }

  public int getCountEntries() {
    return countEntries;
  }

  public int getCountTags() {
    return countTags;
  }

  public int getCountCategories() {
    return countCategories;
  }

  public int getCountReferenceBases() {
    return countReferenceBases;
  }

  public int getCountPersons() {
    return countPersons;
  }

  public int getCountFiles() {
    return countFiles;
  }


  public String getTopLevelEntryId() {
    return topLevelEntryId;
  }

  public String getTopLevelCategoryId() {
    return topLevelCategoryId;
  }

  public Map<String, String> getNoteTypeIds() {
    return noteTypeIds;
  }

  public Map<String, String> getFileTypeIds() {
    return fileTypeIds;
  }

  public Map<String, String> getLanguageIds() {
    return languageIds;
  }

  public Map<String, String> getBackupFileServiceTypesIds() {
    return backupFileServiceTypesIds;
  }


  public static DeepThoughtInfo fromDeepThought(DeepThought deepThought) {
    DeepThoughtInfo deepThoughtInfo = new DeepThoughtInfo(deepThought.getId(), deepThought.getCountEntries(), deepThought.getCountTags(), deepThought.getCountCategories(),
        (deepThought.getCountSeriesTitles() + deepThought.getCountReferences() + deepThought.getCountReferenceSubDivisions()), deepThought.getCountPersons(), deepThought.getCountFiles());

    deepThoughtInfo.topLevelEntryId = deepThought.getTopLevelEntry().getId();
    deepThoughtInfo.topLevelCategoryId = deepThought.getTopLevelCategory().getId();

    deepThoughtInfo.noteTypeIds = extractNoteTypeIds(deepThought);
    deepThoughtInfo.fileTypeIds = extractFileTypeIds(deepThought);
    deepThoughtInfo.languageIds = extractLanguageIds(deepThought);
    deepThoughtInfo.backupFileServiceTypesIds = extractBackupFileServiceTypeIds(deepThought);

    return deepThoughtInfo;
  }

  private static Map<String, String> extractNoteTypeIds(DeepThought deepThought) {
    Map<String, String> noteTypeIds = new HashMap<>();

    for(NoteType noteType : deepThought.getNoteTypes()) {
      noteTypeIds.put(noteType.getNameResourceKey(), noteType.getId());
    }

    return noteTypeIds;
  }

  private static Map<String, String> extractFileTypeIds(DeepThought deepThought) {
    Map<String, String> fileTypeIds = new HashMap<>();

    for(FileType fileType : deepThought.getFileTypes()) {
      fileTypeIds.put(fileType.getNameResourceKey(), fileType.getId());
    }

    return fileTypeIds;
  }

  private static Map<String, String> extractLanguageIds(DeepThought deepThought) {
    Map<String, String> languageIds = new HashMap<>();

    for(Language language : deepThought.getLanguages()) {
      languageIds.put(language.getNameResourceKey(), language.getId());
    }

    return languageIds;
  }

  private static Map<String, String> extractBackupFileServiceTypeIds(DeepThought deepThought) {
    Map<String, String> backupFileServiceTypeIds = new HashMap<>();

    for(BackupFileServiceType backupFileServiceType : deepThought.getBackupFileServiceTypes()) {
      backupFileServiceTypeIds.put(backupFileServiceType.getNameResourceKey(), backupFileServiceType.getId());
    }

    return backupFileServiceTypeIds;
  }

}
