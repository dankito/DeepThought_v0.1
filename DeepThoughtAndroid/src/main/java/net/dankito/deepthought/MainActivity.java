package net.dankito.deepthought;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import net.dankito.deepthought.activities.EditEntryActivity;
import net.dankito.deepthought.fragments.TagsFragment;
import net.dankito.deepthought.listener.AndroidImportFilesOrDoOcrListener;
import net.dankito.deepthought.activities.ActivityManager;
import net.dankito.deepthought.adapter.OnlineArticleContentExtractorsWithArticleOverviewAdapter;
import net.dankito.deepthought.communication.connected_device.IConnectedDevicesListener;
import net.dankito.deepthought.communication.model.ConnectedDevice;
import net.dankito.deepthought.controls.html.AndroidHtmlEditorPool;
import net.dankito.deepthought.data.contentextractor.IOnlineArticleContentExtractor;
import net.dankito.deepthought.data.listener.ApplicationListener;
import net.dankito.deepthought.data.model.DeepThought;
import net.dankito.deepthought.data.model.Entry;
import net.dankito.deepthought.data.model.Tag;
import net.dankito.deepthought.dialogs.DeviceRegistrationHandler;
import net.dankito.deepthought.fragments.EntriesFragment;
import net.dankito.deepthought.helper.AlertHelper;
import net.dankito.deepthought.util.DeepThoughtError;
import net.dankito.deepthought.util.Notification;
import net.dankito.deepthought.util.NotificationType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity implements TabLayout.OnTabSelectedListener {

  private static final Logger log = LoggerFactory.getLogger(MainActivity.class);


  protected static boolean hasDeepThoughtBeenSetup = false;

  protected DeviceRegistrationHandler deviceRegistrationHandler = null;


  protected Toolbar toolbar;

  protected ProgressDialog loadingDataProgressDialog = null;

  protected FloatingActionMenu floatingActionMenu;
  protected FloatingActionButton floatingActionButtonAddNewspaperArticle;

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

  protected static AndroidImportFilesOrDoOcrListener importFilesOrDoOcrListener;


  @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      if(hasDeepThoughtBeenSetup == false) {
        setupDeepThought();

//        ActivityManager.createInstance();
        hasDeepThoughtBeenSetup = true;
      }

      setupUi();

      handleIntent(getIntent());
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
      applicationInstantiated();
    }
    else if(notification.getType() == NotificationType.PluginLoaded) {
      if(floatingActionButtonAddNewspaperArticle != null) { // on start floatingActionButtonAddNewspaperArticle can be null
        setFloatingActionButtonAddNewspaperArticleVisibility();
      }
//      AlertHelper.showInfoMessage(notification); // TODO: show info in same way to user
    }
    else if(notification.getType() == NotificationType.DeepThoughtsConnectorStarted) {
      importFilesOrDoOcrListener = new AndroidImportFilesOrDoOcrListener(this);
      Application.getDeepThoughtConnector().addConnectedDevicesListener(connectedDevicesListener);
      Application.getDeepThoughtConnector().addImportFilesOrDoOcrListener(importFilesOrDoOcrListener);
    }
  }

  protected void applicationInstantiated() {
    AndroidHtmlEditorPool.getInstance().preloadHtmlEditors(this, 2);

    deviceRegistrationHandler = new DeviceRegistrationHandler(this, Application.getDeepThoughtConnector());
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

      initNavigationDrawer();

      initFloatingActionMenu();
    } catch(Exception ex) {
      log.error("Could not setup UI", ex);
    }
  }

  protected void initNavigationDrawer() {
    DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
    // TODO: uncomment for showing 'hamburger' icon to activate Navigation Drawer
//    ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
//        this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
//    drawer.setDrawerListener(toggle);
//    toggle.syncState();

    NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
    navigationView.setNavigationItemSelectedListener(navigationItemSelectedListener);
  }

  protected void initFloatingActionMenu() {
    floatingActionMenu = (FloatingActionMenu)findViewById(R.id.fab_menu);

    FloatingActionButton addEntryButton = (FloatingActionButton)floatingActionMenu.findViewById(R.id.fab_add_entry);
    addEntryButton.setOnClickListener(floatingActionButtonAddEntryClickListener);

    FloatingActionButton addTagButton = (FloatingActionButton)floatingActionMenu.findViewById(R.id.fab_add_tag);
    addTagButton.setOnClickListener(floatingActionButtonAddTagClickListener);

    floatingActionButtonAddNewspaperArticle = (FloatingActionButton)floatingActionMenu.findViewById(R.id.fab_add_newspaper_article);
    floatingActionButtonAddNewspaperArticle.setOnClickListener(floatingActionButtonAddNewspaperArticleClickListener);
    setFloatingActionButtonAddNewspaperArticleVisibility();
  }

  protected void setFloatingActionButtonAddNewspaperArticleVisibility() {
    if(Application.getContentExtractorManager().hasOnlineArticleContentExtractorsWithArticleOverview() == false) {
      floatingActionButtonAddNewspaperArticle.setVisibility(View.GONE);
    }
    else if(floatingActionMenu.isOpened()) {
      floatingActionButtonAddNewspaperArticle.setVisibility(View.VISIBLE);
    }
    else {
      floatingActionButtonAddNewspaperArticle.setVisibility(View.INVISIBLE);
    }
  }

  public void showFloatingActionMenu() {
//    floatingActionMenu.setVisibility(View.VISIBLE);
    floatingActionMenu.setAlpha(1.0f);
  }

  public void hideFloatingActionMenu() {
//    floatingActionMenu.setVisibility(View.INVISIBLE);
    floatingActionMenu.setAlpha(0.25f);
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
        dismissLoadingDataProgressDialog();
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
    dismissLoadingDataProgressDialog();

    super.onDestroy();
  }

  protected void dismissLoadingDataProgressDialog() {
    if(loadingDataProgressDialog != null) {
      loadingDataProgressDialog.hide();
      loadingDataProgressDialog.dismiss();
      loadingDataProgressDialog = null;
    }
  }

  @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.options_menu_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

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
      case AndroidImportFilesOrDoOcrListener.CaptureImageForConnectPeerRequestCode:
        if(importFilesOrDoOcrListener != null) {
          importFilesOrDoOcrListener.handleCaptureImageResult(resultCode);
        }
        break;
      case AndroidImportFilesOrDoOcrListener.SelectImageFromGalleryForConnectPeerRequestCode:
        if(importFilesOrDoOcrListener != null) {
          importFilesOrDoOcrListener.handleSelectImageFromGalleryResult(resultCode, data);
        }
        break;
      case AndroidImportFilesOrDoOcrListener.ScanBarCodeRequestCode: // TODO: is this really always == 49374
        if(importFilesOrDoOcrListener != null) {
          importFilesOrDoOcrListener.handleScanBarCodeResult(requestCode, resultCode, data);
        }
        break;
    }

    super.onActivityResult(requestCode, resultCode, data);
  }


  @Override
  protected void onNewIntent(Intent intent) {
    handleIntent(intent);
  }

  protected void handleIntent(Intent intent) {
    if(intent == null) {
      return;
    }
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


  protected NavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener = new NavigationView.OnNavigationItemSelectedListener() {
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
      return false;
    }
  };


  View.OnClickListener floatingActionButtonAddEntryClickListener = new View.OnClickListener() {
    @Override
    public void onClick(View view) {
      Entry entry = new Entry();
      ActivityManager.getInstance().showEditEntryActivity(MainActivity.this, entry);
      closeFloatingActionMenu();
    }
  };

  View.OnClickListener floatingActionButtonAddTagClickListener = new View.OnClickListener() {
    @Override
    public void onClick(View view) {
      Tag tag = new Tag();
      ActivityManager.getInstance().showEditTagAlert(MainActivity.this, tag);
      closeFloatingActionMenu();
    }
  };

  View.OnClickListener floatingActionButtonAddNewspaperArticleClickListener = new View.OnClickListener() {
    @Override
    public void onClick(View view) {
      showArticlesOverview();
      closeFloatingActionMenu();
    }
  };

  protected void closeFloatingActionMenu() {
    floatingActionMenu.close(true);
  }


  protected IConnectedDevicesListener connectedDevicesListener = new IConnectedDevicesListener() {
    @Override
    public void registeredDeviceConnected(ConnectedDevice device) {
      // TODO
    }

    @Override
    public void registeredDeviceDisconnected(ConnectedDevice device) {

    }
  };


  @Override
  public void onBackPressed() {
    DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
    if (drawer.isDrawerOpen(GravityCompat.START)) {
      drawer.closeDrawer(GravityCompat.START);
    } else {
      // a bit hacky but i don't know how to solve it otherwise to reliably get informed of Back Button pressed in TagsFragment
      // (tip in https://stackoverflow.com/a/7992472 doesn't work as fragment's view needs to stay focused which is not always provided e.g. when displaying Search Bar)
      int selectedTabPosition = tabLayout.getSelectedTabPosition();
      Fragment selectedFragment = mSectionsPagerAdapter.getItem(selectedTabPosition);
      if (selectedFragment instanceof TagsFragment)
        ((TagsFragment) selectedFragment).backButtonPressed();

      super.onBackPressed();
    }
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
          return 2;
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
            }
            return null;
        }
  }

}