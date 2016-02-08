package net.deepthought;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import net.deepthought.activities.ActivityManager;
import net.deepthought.activities.EditEntryActivity;
import net.deepthought.adapter.OnlineArticleContentExtractorsWithArticleOverviewAdapter;
import net.deepthought.communication.listener.ConnectedDevicesListener;
import net.deepthought.communication.listener.ImportFilesOrDoOcrListener;
import net.deepthought.communication.listener.ResponseListener;
import net.deepthought.communication.messages.request.DoOcrRequest;
import net.deepthought.communication.messages.request.ImportFilesRequest;
import net.deepthought.communication.messages.request.Request;
import net.deepthought.communication.messages.request.RequestWithAsynchronousResponse;
import net.deepthought.communication.messages.request.StopRequestWithAsynchronousResponse;
import net.deepthought.communication.messages.response.Response;
import net.deepthought.communication.messages.response.ResponseCode;
import net.deepthought.communication.messages.response.ScanBarcodeResult;
import net.deepthought.communication.model.ConnectedDevice;
import net.deepthought.communication.model.ImportFilesConfiguration;
import net.deepthought.communication.model.ImportFilesSource;
import net.deepthought.controls.html.AndroidHtmlEditorPool;
import net.deepthought.data.contentextractor.IOnlineArticleContentExtractor;
import net.deepthought.data.contentextractor.ocr.ImportFilesResult;
import net.deepthought.data.contentextractor.ocr.RecognizeTextListener;
import net.deepthought.data.contentextractor.ocr.TextRecognitionResult;
import net.deepthought.data.listener.ApplicationListener;
import net.deepthought.data.model.DeepThought;
import net.deepthought.data.model.FileLink;
import net.deepthought.dialogs.RegisterUserDevicesDialog;
import net.deepthought.fragments.EntriesFragment;
import net.deepthought.fragments.SearchFragment;
import net.deepthought.fragments.TagsFragment;
import net.deepthought.helper.AlertHelper;
import net.deepthought.util.DeepThoughtError;
import net.deepthought.util.Notification;
import net.deepthought.util.NotificationType;
import net.deepthought.util.file.FileUtils;
import net.deepthought.util.localization.Localization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity implements TabLayout.OnTabSelectedListener {

  private static final Logger log = LoggerFactory.getLogger(MainActivity.class);


  protected static final int CaptureImageForConnectPeerRequestCode = 7;

  protected final static int SelectImageFromGalleryForConnectPeerRequestCode = 8;

  protected static final int ScanBarCodeRequestCode = 49374;


  protected static boolean hasDeepThoughtBeenSetup = false;

  // make them static otherwise the will be cleaned up when starting TakePhoto Activity
  protected static FileLink temporaryImageFile = null;
  protected static RequestWithAsynchronousResponse captureImageRequest = null;

  protected static ImportFilesRequest importFilesRequest = null;

  protected static RequestWithAsynchronousResponse scanBarcodeRequest = null;


  protected ProgressDialog loadingDataProgressDialog = null;

  protected Toolbar toolbar;

  protected ActionBarDrawerToggle mDrawerToggle;

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    protected TabLayout tabLayout;

  @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      if(hasDeepThoughtBeenSetup == false) {
        setupDeepThought();

//        ActivityManager.createInstance();
        hasDeepThoughtBeenSetup = true;
      }

      setupUi();
    }

  protected void setupDeepThought() {
    // TODO: unregister listeners again to avoid memory leaks
    Application.addApplicationListener(new ApplicationListener() {
      @Override
      public void deepThoughtChanged(final DeepThought deepThought) {
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            setControlsEnabledState(deepThought != null);
          }
        });
      }

      @Override
      public void notification(final Notification notification) {
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            notifyUser(notification);
          }
        });
      }
    });

    setControlsEnabledState(Application.getDeepThought() != null);
  }

  protected void notifyUser(Notification notification) {
    if(notification instanceof DeepThoughtError)
      AlertHelper.showErrorMessage(this, (DeepThoughtError) notification);
    else if(notification.getType() == NotificationType.Info)
      AlertHelper.showInfoMessage(this, notification);
    else if(notification.getType() == NotificationType.ApplicationInstantiated) {
      AndroidHtmlEditorPool.getInstance().preloadHtmlEditors(this, 2);
    }
    else if(notification.getType() == NotificationType.PluginLoaded) {
      if(notification.getParameter() instanceof IOnlineArticleContentExtractor && ((IOnlineArticleContentExtractor)notification.getParameter()).hasArticlesOverview())
        invalidateOptionsMenu(); // now there may are some Article Overview Providers to show -> invalidate its Action
//      AlertHelper.showInfoMessage(notification); // TODO: show info in same way to user
    }
    else if(notification.getType() == NotificationType.DeepThoughtsConnectorStarted) {
      Application.getDeepThoughtsConnector().addConnectedDevicesListener(connectedDevicesListener);
      Application.getDeepThoughtsConnector().addImportFilesOrDoOcrListener(importFilesOrDoOcrListener);
    }
  }

  protected void setupUi() {
    try {
      setContentView(R.layout.activity_main);

      // Set up the action bar.
      toolbar = (Toolbar) findViewById(R.id.toolbar);
      setSupportActionBar(toolbar);

      // Create the adapter that will return a fragment for each of the three
      // primary sections of the activity.
      mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

      // Set up the ViewPager with the sections adapter.
      mViewPager = (ViewPager) findViewById(R.id.pager);
      mViewPager.setAdapter(mSectionsPagerAdapter);

      tabLayout = (TabLayout) findViewById(R.id.tabLayout);
      tabLayout.setupWithViewPager(mViewPager);

      // When swiping between different sections, select the corresponding
      // tab. We can also use ActionBar.Tab#select() to do this if we have
      // a reference to the Tab.
      mViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
        @Override
        public void onPageSelected(int position) {
//                actionBar.setSelectedNavigationItem(position);
        }
      });
      tabLayout.setOnTabSelectedListener(this);

//      // For each of the sections in the app, add a tab to the action bar.
//      for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
//        // Create a tab with text corresponding to the page title defined by
//        // the adapter. Also specify this Activity object, which implements
//        // the TabListener interface, as the callback (listener) for when
//        // this tab is selected.
//          tabLayout.addTab(
//                  tabLayout.newTab()
//                          .setText(mSectionsPagerAdapter.getPageTitle(i)));
//      }

      initNavigationDrawer();
    } catch(Exception ex) {
      log.error("Could not setup UI", ex);
    }
  }

  private void initNavigationDrawer() {
//    final ListView drawer = (ListView) findViewById(R.id.left_drawer);
//    DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
//    mDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,  R.string.drawer_open, R.string.drawer_close) {
//
//      /** Called when a drawer has settled in a completely closed state. */
//      public void onDrawerClosed(View view) {
//      }
//
//      /** Called when a drawer has settled in a completely open state. */
//      public void onDrawerOpened(View drawerView) {
//      }
//    };
//
//    // Set the drawer toggle as the DrawerListener
//    drawerLayout.setDrawerListener(mDrawerToggle);
//    // Set the adapter for the list view
//    drawer.setAdapter(new NavigationDrawerAdapter(this));
//    drawer.setOnItemClickListener(drawerItemSelectedListener);
//
//    // Enable ActionBar app icon to behave as action to toggle nav drawer
//    getSupportActionBar().setHomeButtonEnabled(true);
//    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//    mDrawerToggle.setHomeAsUpIndicator(R.drawable.ic_drawer);
//    mDrawerToggle.setDrawerIndicatorEnabled(true);
  }

//  protected AdapterView.OnItemClickListener drawerItemSelectedListener = new AdapterView.OnItemClickListener() {
//    @Override
//    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//      switch(position){
//        case 0:
//          showRegisterUserDevicesDialog();
//          break;
////        case 1:
////          Intent i = new Intent(DocumentGridActivity.this, OCRLanguageActivity.class);
////          startActivity(i);
////          overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
////          break;
////        case 2:
////          startActivity(new Intent(DocumentGridActivity.this,HelpActivity.class));
////          overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
////          break;
////        case 3:
////          startActivity(new Intent(DocumentGridActivity.this,ContributeActivity.class));
////          overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
////          break;
////        case 4:
////          startActivity(new Intent(DocumentGridActivity.this,AboutActivity.class));
////          overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
////          break;
////        case 5:
////          //TODO start product tour
////          break;
//      }
//
//    }
//  };

  protected void showRegisterUserDevicesDialog() {
    FragmentManager fragmentManager = getSupportFragmentManager();
    new RegisterUserDevicesDialog().show(fragmentManager, RegisterUserDevicesDialog.TAG);
  }

  protected void showArticlesOverview() {
    final List<IOnlineArticleContentExtractor> onlineArticleContentExtractors = Application.getContentExtractorManager().getOnlineArticleContentExtractorsWithArticleOverview();

    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder = builder.setAdapter(new OnlineArticleContentExtractorsWithArticleOverviewAdapter(this, onlineArticleContentExtractors), new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        IOnlineArticleContentExtractor clickedExtractor = onlineArticleContentExtractors.get(which);
        ActivityManager.getInstance().showArticlesOverviewActivity(MainActivity.this, clickedExtractor);
      }
    });

    builder.setNegativeButton(R.string.cancel, null);

    builder.create().show();
  }

  protected void setControlsEnabledState(boolean enable) {
      if(enable) {
        if(loadingDataProgressDialog != null) {
          loadingDataProgressDialog.hide();
          loadingDataProgressDialog = null;
        }
      }
      else {
        loadingDataProgressDialog = new ProgressDialog(this);
        loadingDataProgressDialog.setMessage(getString(R.string.loading_data_wait_message));
        loadingDataProgressDialog.setIndeterminate(true);
        loadingDataProgressDialog.setCancelable(false);
        loadingDataProgressDialog.show();
      }
  }


  @Override
  protected void onStop() {
    super.onStop();
  }

  @Override
  protected void onPause() {
    super.onPause();
  }

  @Override
  protected void onDestroy() {
    loadingDataProgressDialog = null;
    super.onDestroy();
  }

  @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        menu.findItem(R.id.action_device_registration).setTitle(Localization.getLocalizedString("device.registration"));

    MenuItem articlesOverviewItem = menu.findItem(R.id.action_articles_overview);
    articlesOverviewItem.setVisible(Application.getContentExtractorManager().hasOnlineArticleContentExtractorsWithArticleOverview());

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_device_registration) {
          showRegisterUserDevicesDialog();
          return true;
        }
      else if (id == R.id.action_articles_overview) {
        showArticlesOverview();
        return true;
      }

        return super.onOptionsItemSelected(item);
    }


  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    switch(requestCode) {
      case EditEntryActivity.RequestCode:
        if(resultCode == RESULT_OK && data != null) {
          // Entry has been updated
          ActivityManager.getInstance().resetEditEntryActivityCachedData();
        }
        break;
      case CaptureImageForConnectPeerRequestCode:
        handleCaptureImageResult(resultCode);
        break;
      case SelectImageFromGalleryForConnectPeerRequestCode:
        handleSelectImageFromGalleryResult(resultCode, data);
        break;
      case ScanBarCodeRequestCode: // TODO: is this really always == 49374
        handleScanBarCodeResult(requestCode, resultCode, data);
        break;
    }

    super.onActivityResult(requestCode, resultCode, data);
  }

  protected void handleCaptureImageResult(int resultCode) {
    if(resultCode == RESULT_OK) {
      if (captureImageRequest != null && temporaryImageFile != null) {
        File imageFile = new File(temporaryImageFile.getUriString());
        try {
          byte[] imageData = FileUtils.readFile(imageFile);
          Application.getDeepThoughtsConnector().getCommunicator().respondToImportFilesRequest(captureImageRequest, new ImportFilesResult(imageData, true), null);
        } catch (Exception ex) {
          log.error("Could not read captured photo from temp file " + temporaryImageFile.getUriString(), ex);
          // TODO: send error response
        }

        imageFile.delete();
      }
    }

    temporaryImageFile = null;
    captureImageRequest = null;
  }

  protected void handleSelectImageFromGalleryResult(int resultCode, Intent data) {
    if(resultCode == RESULT_OK) { // TODO: what to do in error case? Send Error Message?
      if(importFilesRequest != null) {
        String uri = data.getDataString();

        try {
          InputStream selectedFileStream = getContentResolver().openInputStream(data.getData());
          byte[] fileData = readDataFromInputStream(selectedFileStream);
          Application.getDeepThoughtsConnector().getCommunicator().respondToImportFilesRequest(importFilesRequest, new ImportFilesResult(fileData, true), null);
        } catch (Exception ex) {
          log.error("Could not read select file from uri " + uri, ex);
          // TODO: send error response
        }
      }
    }

    importFilesRequest = null;
  }

  protected byte[] readDataFromInputStream(InputStream inputStream) throws Exception{
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    int nRead;
    byte[] data = new byte[16384];

    while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
      buffer.write(data, 0, nRead);
    }

    buffer.flush();

    return buffer.toByteArray();
  }

  protected void handleScanBarCodeResult(int requestCode, int resultCode, Intent data) {
    if(scanBarcodeRequest != null) {
      IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
      if (result != null) {
        Application.getDeepThoughtsConnector().getCommunicator().respondToScanBarcodeRequest(scanBarcodeRequest,
            new ScanBarcodeResult(result.getContents(), result.getFormatName()), null);
      }
    }

    scanBarcodeRequest = null;
  }

  @Override
  public void onTabSelected(TabLayout.Tab tab) {
    mViewPager.setCurrentItem(tab.getPosition());
  }

  @Override
  public void onTabUnselected(TabLayout.Tab tab) {

  }

  @Override
  public void onTabReselected(TabLayout.Tab tab) {

  }


  protected ConnectedDevicesListener connectedDevicesListener = new ConnectedDevicesListener() {
    @Override
    public void registeredDeviceConnected(ConnectedDevice device) {
      // TODO
    }

    @Override
    public void registeredDeviceDisconnected(ConnectedDevice device) {

    }
  };

  protected ImportFilesOrDoOcrListener importFilesOrDoOcrListener = new ImportFilesOrDoOcrListener() {

    @Override
    public void importFiles(ImportFilesRequest request) {
      exportFilesToRemoteDevice(request);
    }

    @Override
    public void doOcr(DoOcrRequest request) {
      doOcrAndSendToCaller(request);
    }

    @Override
    public void scanBarcode(RequestWithAsynchronousResponse request) {
      MainActivity.this.scanBarCode(request);
    }

    @Override
    public void stopCaptureImageOrDoOcr(StopRequestWithAsynchronousResponse request) {
      // TODO: implement
    }
  };

  protected void exportFilesToRemoteDevice(ImportFilesRequest request) {
    ImportFilesConfiguration configuration = request.getConfiguration();

    if(configuration.getSource() == ImportFilesSource.CaptureImage) {
      capturePhotoAndSendToCaller(request);
    }
    else if(configuration.getSource() == ImportFilesSource.SelectFromExistingFiles) {
      selectImagesFromGalleryAndSendToCaller(request);
    }
  }

  protected void capturePhotoAndSendToCaller(ImportFilesRequest request) {
    temporaryImageFile = AndroidHelper.takePhoto(this, CaptureImageForConnectPeerRequestCode);
    if(temporaryImageFile != null)
      this.captureImageRequest = request; // TODO: in this way only the last of several simultaneous Requests can be send back to caller
  }

  protected void selectImagesFromGalleryAndSendToCaller(ImportFilesRequest request) {
    this.importFilesRequest = request;

    Intent i = new Intent(Intent.ACTION_GET_CONTENT, null);

    // TODO: set Files types either to Html compatible types or that ones in request parameter
    if (Build.VERSION.SDK_INT >= 19) {
      i.setType("image/*");
      i.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/png", "image/jpg", "image/jpeg"});
    } else {
      i.setType("image/png,image/jpg, image/jpeg");
    }

    Intent chooser = Intent.createChooser(i, getString(R.string.image_source));
    startActivityForResult(chooser, SelectImageFromGalleryForConnectPeerRequestCode);
  }

  protected void doOcrAndSendToCaller(final DoOcrRequest request) {
    if(Application.getContentExtractorManager().hasOcrContentExtractors()) {
      Application.getContentExtractorManager().getPreferredOcrContentExtractor().recognizeTextAsync(request.getConfiguration(), new RecognizeTextListener() {
        @Override
        public void textRecognized(TextRecognitionResult result) {
          Application.getDeepThoughtsConnector().getCommunicator().respondToDoOcrRequest(request, result, new ResponseListener() {
            @Override
            public void responseReceived(Request request, Response response) {
              if (response.getResponseCode() == ResponseCode.Error) {
                // TODO: stop process then
              }
            }
          });
        }
      });
    }
  }

  protected void scanBarCode(RequestWithAsynchronousResponse request) {
    scanBarcodeRequest = request;

    new IntentIntegrator(this).setOrientationLocked(false).initiateScan();
  }


  @Override
  public void onBackPressed() {
    // a bit hacky but i don't know how to solve it otherwise to reliably get informed of Back Button pressed in TagsFragment
    // (tip in https://stackoverflow.com/a/7992472 doesn't work as fragment's view needs to stay focused which is not always provided e.g. when displaying Search Bar)
    int selectedTabPosition = tabLayout.getSelectedTabPosition();
    Fragment selectedFragment = mSectionsPagerAdapter.getItem(selectedTabPosition);
    if(selectedFragment instanceof TagsFragment)
      ((TagsFragment)selectedFragment).backButtonPressed();

    super.onBackPressed();
  }

  /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        protected android.support.v4.app.FragmentManager fragmentManager = null;

        protected EntriesFragment entriesFragment = null;
        protected TagsFragment tagsFragment = null;

        public SectionsPagerAdapter(android.support.v4.app.FragmentManager fm) {
            super(fm);
            this.fragmentManager = fm;
        }

        @Override
        public int getCount() {
          return 2; // don't show SearchFragment right now
        }

        @Override
        public Fragment getItem(int position) {
          if(position == 0) {
            if(entriesFragment == null)
              entriesFragment = new EntriesFragment();
            return entriesFragment;
          }
          else if(position == 1) {
            if(tagsFragment == null)
              tagsFragment = new TagsFragment();
            return tagsFragment;
          }
          else if(position == 2) {
            return new SearchFragment();
          }

          return null;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_section_entries).toUpperCase(l);
                case 1:
                    return getString(R.string.title_section_tags).toUpperCase(l);
                case 2:
                  return getString(R.string.title_section_search).toUpperCase(l);
            }
            return null;
        }
  }

}
