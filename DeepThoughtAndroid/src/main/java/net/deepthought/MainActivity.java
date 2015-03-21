package net.deepthought;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import net.deepthought.activities.ActivityManager;
import net.deepthought.activities.EditEntryActivity;
import net.deepthought.android.db.OrmLiteAndroidEntityManager;
import net.deepthought.data.AndroidDataManager;
import net.deepthought.data.DeepThoughtAndroidApplicationConfiguration;
import net.deepthought.data.IDataManager;
import net.deepthought.data.listener.ApplicationListener;
import net.deepthought.data.model.DeepThought;
import net.deepthought.data.model.Entry;
import net.deepthought.data.persistence.EntityManagerConfiguration;
import net.deepthought.data.persistence.IEntityManager;
import net.deepthought.fragments.EntriesOverviewFragment;
import net.deepthought.fragments.SearchFragment;
import net.deepthought.util.DeepThoughtError;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;


public class MainActivity extends ActionBarActivity implements ActionBar.TabListener {

  private final static Logger log = LoggerFactory.getLogger(MainActivity.class);


  protected ProgressDialog loadingDataProgressDialog = null;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
        public void errorOccurred(DeepThoughtError error) {
          // TODO: show error message
        }
      });

      // TODO: create Android Dependency Resolver
      Application.instantiateAsync(new DeepThoughtAndroidApplicationConfiguration(this), new DefaultDependencyResolver() {
        @Override
        public IEntityManager createEntityManager(EntityManagerConfiguration configuration) {
          try {
            return new OrmLiteAndroidEntityManager(MainActivity.this.getApplicationContext(), configuration);
          } catch(Exception ex) {
            Log.e("MainActivity", "Could not create OrmLiteAndroidEntityManager", ex);
          }

          return null; // TODO: what to do in this case?
        }

        @Override
        public IDataManager createDataManager(IEntityManager entityManager) {
          return new AndroidDataManager(entityManager);
        }
      });

      loadingDataProgressDialog = new ProgressDialog(this);
      loadingDataProgressDialog.setMessage(getString(R.string.loading_data_wait_message));
      loadingDataProgressDialog.setIndeterminate(true);
      loadingDataProgressDialog.setCancelable(false);
      loadingDataProgressDialog.show();

        setContentView(R.layout.activity_main);

        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }


      ActivityManager.createInstance(this);

//      testLucene();
    }

  private void setControlsEnabledState(boolean enable) {
    if(loadingDataProgressDialog != null) {
      if(enable)
        loadingDataProgressDialog.hide();
      else
        loadingDataProgressDialog.show();
    }
  }

  private void testLucene() {
//    KeywordExtractor extractor = new KeywordExtractor();
//    List<String> keywords = extractor.extractKeywords("Dann bin ich mal gespannt. Gewichtiges Wort. Harald Junke");
//    if(keywords.size() > 0) {
//
//    }
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
    Application.shutdown();
    super.onDestroy();
  }

  @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.mnitmActionAddEntry) {

        }
        return super.onOptionsItemSelected(item);
    }

  @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }


  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    switch(requestCode) {
      case EditEntryActivity.RequestCode:
        if(resultCode == RESULT_OK && data != null) {
          Entry entry = ActivityManager.getInstance().getEntryToBeEdited();

//          try {
//            DeepThought deepThought = Application.getDeepThought();
//
//            if(deepThought.containsEntry(entry) == false)
//              deepThought.addEntry(entry);
//          } catch(Exception ex) {
//            log.error("Could not persist DeepThought", ex);
//            // TODO: show alert dialog
//          }
        }
        break;
    }

    super.onActivityResult(requestCode, resultCode, data);
  }

  /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
          if(position == 0) {
            return new EntriesOverviewFragment();
          }
          else if(position == 1) {
            return new SearchFragment();
          }
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_section_entries_overview).toUpperCase(l);
                case 1:
                    return getString(R.string.title_section_search).toUpperCase(l);
                case 2:
                    return getString(R.string.title_section3).toUpperCase(l);
            }
            return null;
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }
    }

}
