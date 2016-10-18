package net.dankito.deepthought.dialogs;

import android.os.Bundle;

import net.dankito.deepthought.Application;
import net.dankito.deepthought.data.contentextractor.EntryCreationResult;
import net.dankito.deepthought.data.listener.ApplicationListener;
import net.dankito.deepthought.data.model.DeepThought;
import net.dankito.deepthought.data.model.Entry;
import net.dankito.deepthought.data.persistence.deserializer.DeserializationResult;
import net.dankito.deepthought.data.persistence.json.JsonIoJsonHelper;
import net.dankito.deepthought.data.persistence.serializer.SerializationResult;
import net.dankito.deepthought.util.Notification;
import net.dankito.deepthought.util.NotificationType;

/**
 * Created by ganymed on 18/10/16.
 */
public abstract class EntryDialogBase extends FullscreenDialog {

  protected static final String ENTRY_ID_BUNDLE_KEY = "EntryId";

  protected static final String ENTRY_CREATION_RESULT_BUNDLE_KEY = "EntryCreationResult";


  protected Entry entry = null;
  protected EntryCreationResult entryCreationResult = null;



  public void setEntry(Entry entry) {
    this.entry = entry;
  }

  public void setEntryCreationResult(EntryCreationResult entryCreationResult) {
    this.entryCreationResult = entryCreationResult;
  }


  @Override
  public void onSaveInstanceState(Bundle outState) {
    if(entry != null) {
      outState.putString(ENTRY_ID_BUNDLE_KEY, entry.getId());
    }

    if(entryCreationResult != null) {
      SerializationResult result = JsonIoJsonHelper.generateJsonString(entryCreationResult);
      if(result.successful()) {
        outState.putString(ENTRY_CREATION_RESULT_BUNDLE_KEY, result.getSerializationResult());
      }
    }

    super.onSaveInstanceState(outState);
  }

  @Override
  protected void restoreSavedInstance(Bundle savedInstanceState) {
    if(savedInstanceState != null) {
      tryToRestoreEntry(savedInstanceState);

      tryToRestoreEntryCreationResult(savedInstanceState);
    }

    super.restoreSavedInstance(savedInstanceState);
  }

  protected void tryToRestoreEntry(Bundle savedInstanceState) {
    final String entryId = savedInstanceState.getString(ENTRY_ID_BUNDLE_KEY);

    if(entryId != null) {
      if(Application.getEntityManager() != null) {
        Entry entry = Application.getEntityManager().getEntityById(Entry.class, entryId);
        setEntry(entry);
      }
      else {
        Application.addApplicationListener(new ApplicationListener() {
          @Override
          public void deepThoughtChanged(DeepThought deepThought) { }

          @Override
          public void notification(Notification notification) {
            if(notification.getType() == NotificationType.ApplicationInstantiated) {
              Application.removeApplicationListener(this);

              Entry entry = Application.getEntityManager().getEntityById(Entry.class, entryId);
              setEntry(entry);
            }
          }
        });
      }
    }
  }

  protected void tryToRestoreEntryCreationResult(Bundle savedInstanceState) {
    String entryCreationResultJsonString = savedInstanceState.getString(ENTRY_CREATION_RESULT_BUNDLE_KEY);

    if(entryCreationResultJsonString != null) {
      DeserializationResult<EntryCreationResult> result = JsonIoJsonHelper.parseJsonString(entryCreationResultJsonString, EntryCreationResult.class);
      if(result.successful()) {
        setEntryCreationResult(result.getResult());
      }
    }
  }

}
