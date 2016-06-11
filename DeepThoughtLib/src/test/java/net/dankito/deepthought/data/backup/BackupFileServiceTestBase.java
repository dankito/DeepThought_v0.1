package net.dankito.deepthought.data.backup;

import net.dankito.deepthought.Application;
import net.dankito.deepthought.data.helper.TestBackupManager;
import net.dankito.deepthought.TestApplicationConfiguration;
import net.dankito.deepthought.TestEntityManagerConfiguration;
import net.dankito.deepthought.data.backup.enums.BackupRestoreType;
import net.dankito.deepthought.data.backup.enums.BackupStep;
import net.dankito.deepthought.data.backup.enums.CreateBackupResult;
import net.dankito.deepthought.data.backup.listener.CreateBackupListener;
import net.dankito.deepthought.data.backup.listener.RestoreBackupListener;
import net.dankito.deepthought.data.helper.AssertSetToFalse;
import net.dankito.deepthought.data.helper.AssertSetToTrue;
import net.dankito.deepthought.data.helper.DataHelper;
import net.dankito.deepthought.data.listener.ApplicationListener;
import net.dankito.deepthought.data.model.DeepThought;
import net.dankito.deepthought.data.model.DeepThoughtApplication;
import net.dankito.deepthought.data.persistence.EntityManagerConfiguration;
import net.dankito.deepthought.data.persistence.IEntityManager;
import net.dankito.deepthought.data.persistence.db.BaseEntity;
import net.dankito.deepthought.util.DeepThoughtError;
import net.dankito.deepthought.util.Notification;
import net.dankito.deepthought.util.file.FileUtils;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by ganymed on 05/01/15.
 */
@Ignore
public abstract class BackupFileServiceTestBase {

  protected IBackupManager backupManager = null;

  protected IBackupFileService backupFileService = null;


  @BeforeClass
  public static void suiteSetup() {
    FileUtils.copyFile(DataHelper.getLatestDataModelVersionDatabaseFile(), new File(new TestEntityManagerConfiguration().getDataCollectionPersistencePath()));
  }

  @Before
  public void setup() throws Exception {
    Application.instantiate(new TestApplicationConfiguration() {
      @Override
      public IEntityManager createEntityManager(EntityManagerConfiguration configuration) throws Exception {
        return BackupFileServiceTestBase.this.createTestEntityManager(configuration);
      }

      @Override
      public IBackupManager createBackupManager() {
        return BackupFileServiceTestBase.this.createBackupManager();
      }
    });

    backupManager = Application.getBackupManager();

    backupFileService = createBackupFileService();
  }

  @After
  public void tearDown() {
    FileUtils.deleteFile(backupManager.getRestoredBackupsFolder()); // delete all restored backups
    Application.shutdown();
  }


  protected abstract IEntityManager createTestEntityManager(EntityManagerConfiguration configuration) throws Exception;

  protected abstract IBackupFileService createBackupFileService();

  protected IBackupManager createBackupManager() {
    return new TestBackupManager();
  }


  @Test
  public void createBackup_FileGetsWritten() {
    DeepThoughtApplication application = DataHelper.createTestApplication();

    final AssertSetToTrue createBackupResult = new AssertSetToTrue();
    final List<BackupFile> backupFileContainer = new ArrayList<>();

    backupFileService.createBackup(new CreateBackupParams(application, new CreateBackupListener() {
      @Override
      public void createBackupDone(CreateBackupResult result, DeepThoughtError error, BackupFile file) {
        createBackupResult.setValue(result == CreateBackupResult.Successful);
        backupFileContainer.add(file);
      }
    }));

    Assert.assertTrue(createBackupResult.getValue());
    Assert.assertEquals(1, backupFileContainer.size());

    BackupFile backupFile = backupFileContainer.get(0);
    Assert.assertTrue(backupFile.getFile().exists());
  }

  @Test
  public void checkIfMaximumCountBackupsExceeded_SuperfluousBackupsGetDeleted() {
    DeepThoughtApplication application = Application.getApplication();
    int maxBackupsToKeep = Application.getSettings().getMaxBackupsToKeep();

    CreateBackupParams createBackupParams = new CreateBackupParams(application);

    for(int i = 0; i < maxBackupsToKeep + 3; i++) {
      backupFileService.createBackup(createBackupParams);
    }

    File[] filesInBackupFolder = new File(backupManager.getBackupsFolderPath()).listFiles(new FilenameFilter() {
      @Override
      public boolean accept(File dir, String name) {
        return name.endsWith(backupFileService.getFileTypeFileExtension());
      }
    });
    Assert.assertTrue(filesInBackupFolder.length == maxBackupsToKeep);
  }

  @Test
  public void restoreDatabaseBackup_FileDoesNotExist_RestoreGetAbortedCorrectly() {
    File backupFile = new File(DataHelper.getExampleDataFolder(), "adersfkyuaeedsf_i_sure_dont_exist." + backupFileService.getFileTypeFileExtension());

    final AssertSetToFalse readingFileFromFileSystemStepSuccess = new AssertSetToFalse();
    final List<BackupStep> backupStepsContainer = new ArrayList<>();
    final AssertSetToFalse restoreBackupResult = new AssertSetToFalse();

    backupFileService.restoreBackup(new RestoreBackupParams(new BackupFile(backupFile, backupFileService.getFileServiceType()), BackupRestoreType.TryToMergeWithExistingData,
        new RestoreBackupListener() {
          @Override
          public void beginStep(BackupFile file, BackupStep step) {

          }

          @Override
          public void stepDone(RestoreBackupStepResult stepResult) {
            readingFileFromFileSystemStepSuccess.setValue(stepResult.successful());
            backupStepsContainer.add(stepResult.getStepDone());
          }

          @Override
          public List<BaseEntity> selectEntitiesToRestore(BackupFile file, BaseEntity restoredData) {
            return null;
          }

          @Override
          public void restoreBackupDone(boolean successful, RestoreBackupResult result) {
            restoreBackupResult.setValue(successful);
          }
        }));

    Assert.assertFalse(readingFileFromFileSystemStepSuccess.getValue());
    Assert.assertTrue(backupStepsContainer.contains(BackupStep.DeserializeBackedUpDeepThought));
    Assert.assertEquals(backupStepsContainer.size(), backupStepsContainer.indexOf(BackupStep.DeserializeBackedUpDeepThought) + 1); // assert last Step was trying to deserialize
    // DeepThought
    Assert.assertFalse(restoreBackupResult.getValue());
  }

  @Test
  public void restoreBackup_InvalidBackupFile_RestoreGetAbortedCorrectly() {
    File backupFile = new File(DataHelper.getExampleDataFolder(), "invalid." + backupFileService.getFileTypeFileExtension());

    final AssertSetToFalse readingFileFromFileSystemStepSuccess = new AssertSetToFalse();
    final List<BackupStep> backupStepsContainer = new ArrayList<>();
    final AssertSetToFalse restoreBackupResult = new AssertSetToFalse();

    backupFileService.restoreBackup(new RestoreBackupParams(new BackupFile(backupFile, backupFileService.getFileServiceType()), BackupRestoreType.TryToMergeWithExistingData, new RestoreBackupListener() {
      @Override
      public void beginStep(BackupFile file, BackupStep step) {

      }

      @Override
      public void stepDone(RestoreBackupStepResult stepResult) {
        readingFileFromFileSystemStepSuccess.setValue(stepResult.successful);
        backupStepsContainer.add(stepResult.getStepDone());
      }

      @Override
      public List<BaseEntity> selectEntitiesToRestore(BackupFile file, BaseEntity restoredData) {
        return null;
      }

      @Override
      public void restoreBackupDone(boolean successful, RestoreBackupResult result) {
        restoreBackupResult.setValue(successful);
      }
    }));

    Assert.assertFalse(readingFileFromFileSystemStepSuccess.getValue());
    Assert.assertTrue(backupStepsContainer.contains(BackupStep.DeserializeBackedUpDeepThought));
    Assert.assertEquals(backupStepsContainer.size(), backupStepsContainer.indexOf(BackupStep.DeserializeBackedUpDeepThought) + 1); // assert last Step was trying to deserialize
    // DeepThought
    Assert.assertFalse(restoreBackupResult.getValue());
  }

  @Test
  public void restoreBackup_DataCollectionAndBackupFileGetCopiedToRestoredBackupsFolder() {
    FileUtils.deleteFile(backupManager.getRestoredBackupsFolder()); // ensure folder is empty before
    File backupFile = DataHelper.getLatestDataModelVersionFileForFileType(backupFileService);

    backupFileService.restoreBackup(new RestoreBackupParams(new BackupFile(backupFile, backupFileService.getFileServiceType()), BackupRestoreType.TryToMergeWithExistingData, null));

    File[] filesInRestoredBackupsFolder = backupManager.getRestoredBackupsFolder().listFiles();
    Assert.assertEquals(2, filesInRestoredBackupsFolder.length);
  }


  @Test
  public void restoreBackup_ReplaceExisting_DataListenerGetsCorrectlyCalled() {
    File backupFile = DataHelper.getLatestDataModelVersionFileForFileType(backupFileService);

    final List<Integer> countDeepThoughtChangedCalledContainer = new ArrayList<Integer>();

    Application.addApplicationListener(new ApplicationListener() {
      @Override
      public void deepThoughtChanged(DeepThought deepThought) {
        countDeepThoughtChangedCalledContainer.add(countDeepThoughtChangedCalledContainer.size() + 1);
      }

      @Override
      public void notification(Notification notification) {

      }
    });

    backupFileService.restoreBackup(new RestoreBackupParams(new BackupFile(backupFile, backupFileService.getFileServiceType()), BackupRestoreType.ReplaceExistingDataCollection, null));

    Assert.assertEquals(2, countDeepThoughtChangedCalledContainer.size()); // once for 'null' (before replacing existing collection) and once for the new one
  }

  @Test
  public void restoreBackup_ReplaceExisting_AllEntitiesGetSuccessfullyInserted() {
    File backupFile = DataHelper.getLatestDataModelVersionFileForFileType(backupFileService);

    final Set<Boolean> allStepsSucceededContainer = new HashSet<>();
    final AssertSetToTrue restoreBackupResult = new AssertSetToTrue();

    backupFileService.restoreBackup(new RestoreBackupParams(new BackupFile(backupFile, backupFileService.getFileServiceType()), BackupRestoreType.ReplaceExistingDataCollection,
        new RestoreBackupListener() {
          @Override
          public void beginStep(BackupFile file, BackupStep step) {

          }

          @Override
          public void stepDone(RestoreBackupStepResult stepResult) {
            allStepsSucceededContainer.add(stepResult.successful());
          }

          @Override
          public List<BaseEntity> selectEntitiesToRestore(BackupFile file, BaseEntity restoredData) {
            return null;
          }

          @Override
          public void restoreBackupDone(boolean successful, RestoreBackupResult result) {
            restoreBackupResult.setValue(successful);
          }
        }));

    Assert.assertTrue(restoreBackupResult.getValue());
    Assert.assertFalse(allStepsSucceededContainer.contains(false));
  }


  @Test
  public void restoreBackup_TryToMergeWithExistingData_AllEntitiesGetSuccessfullyInserted() {
    File backupFile = DataHelper.getLatestDataModelVersionFileForFileType(backupFileService);

    final Set<Boolean> allStepsSucceededContainer = new HashSet<>();
    final AssertSetToTrue restoreBackupResult = new AssertSetToTrue();

    backupFileService.restoreBackup(new RestoreBackupParams(new BackupFile(backupFile, backupFileService.getFileServiceType()), BackupRestoreType.TryToMergeWithExistingData, new RestoreBackupListener() {
      @Override
      public void beginStep(BackupFile file, BackupStep step) {

      }

      @Override
      public void stepDone(RestoreBackupStepResult stepResult) {
        allStepsSucceededContainer.add(stepResult.successful());
      }

      @Override
      public List<BaseEntity> selectEntitiesToRestore(BackupFile file, BaseEntity restoredData) {
        return null;
      }

      @Override
      public void restoreBackupDone(boolean successful, RestoreBackupResult result) {
        restoreBackupResult.setValue(successful);
      }
    }));

    Assert.assertTrue(restoreBackupResult.getValue());
    Assert.assertFalse(allStepsSucceededContainer.contains(false));
  }


  @Test
  public void restoreBackup_AddAsNewToExistingData_AllEntitiesGetSuccessfullyInserted() {
    File backupFile = DataHelper.getLatestDataModelVersionFileForFileType(backupFileService);

    final Set<Boolean> allStepsSucceededContainer = new HashSet<>();
    final AssertSetToTrue restoreBackupResult = new AssertSetToTrue();

    backupFileService.restoreBackup(new RestoreBackupParams(new BackupFile(backupFile, backupFileService.getFileServiceType()), BackupRestoreType.AddAsNewToExistingData,
        new RestoreBackupListener() {
          @Override
          public void beginStep(BackupFile file, BackupStep step) {

          }

          @Override
          public void stepDone(RestoreBackupStepResult stepResult) {
            allStepsSucceededContainer.add(stepResult.successful());
          }

          @Override
          public List<BaseEntity> selectEntitiesToRestore(BackupFile file, BaseEntity restoredData) {
            return null;
          }

          @Override
          public void restoreBackupDone(boolean successful, RestoreBackupResult result) {
            restoreBackupResult.setValue(successful);
          }
        }));

    Assert.assertTrue(restoreBackupResult.getValue());
    Assert.assertFalse(allStepsSucceededContainer.contains(false));
  }

}
