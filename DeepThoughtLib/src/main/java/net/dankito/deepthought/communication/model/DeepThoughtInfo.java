package net.dankito.deepthought.communication.model;

import net.dankito.deepthought.data.model.DeepThought;

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


  public static DeepThoughtInfo fromDeepThought(DeepThought deepThought) {
    return new DeepThoughtInfo(deepThought.getId(), deepThought.getCountEntries(), deepThought.getCountTags(), deepThought.getCountCategories(),
        (deepThought.getCountSeriesTitles() + deepThought.getCountReferences() + deepThought.getCountReferenceSubDivisions()), deepThought.getCountPersons(), deepThought.getCountFiles());
  }
}
