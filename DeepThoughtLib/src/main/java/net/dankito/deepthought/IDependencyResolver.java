package net.dankito.deepthought;

import net.dankito.deepthought.application.IApplicationLifeCycleService;
import net.dankito.deepthought.clipboard.IClipboardHelper;
import net.dankito.deepthought.communication.ICommunicationConfigurationManager;
import net.dankito.deepthought.communication.IDeepThoughtConnector;
import net.dankito.deepthought.communication.IDevicesFinder;
import net.dankito.deepthought.communication.connected_device.IConnectedRegisteredDevicesListenerManager;
import net.dankito.deepthought.communication.connected_device.IDevicesFinderListenerManager;
import net.dankito.deepthought.controls.html.IHtmlEditorPool;
import net.dankito.deepthought.data.IDataManager;
import net.dankito.deepthought.data.backup.IBackupManager;
import net.dankito.deepthought.data.compare.IDataComparer;
import net.dankito.deepthought.data.contentextractor.IContentExtractorManager;
import net.dankito.deepthought.data.download.IFileDownloader;
import net.dankito.deepthought.data.html.IHtmlHelper;
import net.dankito.deepthought.data.merger.IDataMerger;
import net.dankito.deepthought.data.persistence.EntityManagerConfiguration;
import net.dankito.deepthought.data.persistence.IEntityManager;
import net.dankito.deepthought.data.search.IEntitiesSearcherAndCreator;
import net.dankito.deepthought.data.search.ISearchEngine;
import net.dankito.deepthought.data.sync.IDeepThoughtSyncManager;
import net.dankito.deepthought.language.ILanguageDetector;
import net.dankito.deepthought.platform.IPlatformTools;
import net.dankito.deepthought.plugin.IPluginManager;
import net.dankito.deepthought.util.IThreadPool;
import net.dankito.deepthought.util.isbn.IIsbnResolver;
import net.dankito.deepthought.util.web.IWebClient;

/**
 * Created by ganymed on 05/01/15.
 */
public interface IDependencyResolver<THtmlEditor> {

  IThreadPool createThreadPool();

  IEntityManager createEntityManager(EntityManagerConfiguration configuration) throws Exception;

  IDataManager createDataManager(IEntityManager entityManager);

  IPlatformTools createPlatformTools();

  IApplicationLifeCycleService createApplicationLifeCycleService();

  IBackupManager createBackupManager();

  IDataComparer createDataComparer();

  IDataMerger createDataMerger();

  ILanguageDetector createLanguageDetector();

  ISearchEngine createSearchEngine();

  IEntitiesSearcherAndCreator createEntitiesSearcherAndCreator();

  IWebClient createWebClient();

  IHtmlHelper createHtmlHelper(IWebClient webClient);

  IFileDownloader createDownloader();

  IPluginManager createPluginManager();

  IContentExtractorManager createContentExtractorManager();

  IDevicesFinder createDevicesFinder(IThreadPool threadPool);

  IDeepThoughtConnector createDeepThoughtConnector(IDevicesFinder devicesFinder, IThreadPool threadPool);

  IDeepThoughtSyncManager createSyncManager(IEntityManager entityManager, IConnectedRegisteredDevicesListenerManager connectedDevicesListenerManager,
                                            IDevicesFinderListenerManager devicesFinderListenerManager, ICommunicationConfigurationManager configurationManager, IThreadPool threadPool);

  IIsbnResolver createIsbnResolver(IHtmlHelper htmlHelper, IThreadPool threadPool);

  IHtmlEditorPool<THtmlEditor> createHtmlEditorPool();

  IClipboardHelper createClipboardHelper();

}
