package de.schalter.sermononline;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import de.schalter.sermononline.fragments.DownloadsFragment;
import de.schalter.sermononline.fragments.SearchFragment;
import de.schalter.sermononline.settings.Settings;
import de.schalter.sermononline.settings.SettingsActivity;

public class MainActivity extends AppCompatActivity {

    private final int SEARCH = 0;
    private final int DOWNLOADS = 1;

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    private RelativeLayout relativeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Settings.initSettings(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        relativeLayout = findViewById(R.id.main_content);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        if(Settings.getBoolean(Settings.SHOW_ADS, Settings.SHOW_ADS_DEFAULT)) {
            AdView mAdView = findViewById(R.id.adView_main);
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
        } else {
            AdView mAdView = findViewById(R.id.adView_main);
            relativeLayout.removeView(mAdView);
        }

        if(Settings.getBoolean(Settings.FIRST_START, Settings.FIRST_START_DEFAULT)) {
            //show dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.show_ads);
            builder.setMessage(R.string.show_ads_message);
            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Settings.setBoolean(Settings.SHOW_ADS, false);
                }
            });
            builder.setPositiveButton(R.string.activate_ads, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Settings.setBoolean(Settings.SHOW_ADS, true);
                }
            });

            builder.show();

            Settings.setBoolean(Settings.FIRST_START, false);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        DownloadsFragment fragment = (DownloadsFragment) mSectionsPagerAdapter.getItem(DOWNLOADS);
        fragment.update();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            this.startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void snackbar(int message) {
        Snackbar.make(relativeLayout, message, Snackbar.LENGTH_LONG).show();
    }

    public void snackbar(int message, int duration) {
        Snackbar.make(relativeLayout, message, duration).show();
    }

    public void snackbarWithAction(int message, int duration, int actionText, final Runnable action) {
        Snackbar.make(relativeLayout, message, duration).setAction(actionText, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                action.run();
            }
        }).show();
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
            // getItem is called to instantiate the fragment for the given page.
            if(position == SEARCH)
                return SearchFragment.newInstance();
            else if(position == DOWNLOADS)
                return DownloadsFragment.newInstance();

            return null;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case SEARCH:
                    return getString(R.string.search);
                case DOWNLOADS:
                    return getString(R.string.downloads);
            }
            return null;
        }
    }
}