package net.deepthought.data.persistence;

import net.deepthought.data.model.DeepThought;

/**
 * Created by ganymed on 01/10/14.
 */
public interface IDeepThoughtPersistenceManager {

  public DeepThought deserializeDeepThought() throws Exception;
  public void deserializeDeepThoughtAsync(DeserializeDeepThoughtResult result);

  public boolean persistDeepThought(DeepThought deepThought) throws Exception;

//  public List<EntryTemplate> getAllEntryTemplates();
//  public boolean addEntryTemplate(EntryTemplate entryTemplate);


  public interface DeserializeDeepThoughtResult {
    public void result(boolean successful, Exception error, DeepThought deepThought);
  }

}
