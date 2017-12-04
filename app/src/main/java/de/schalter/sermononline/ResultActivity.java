package de.schalter.sermononline;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.schalter.sermononline.dialogs.ErrorDialog;
import de.schalter.sermononline.objects.SermonListElement;
import de.schalter.sermononline.parser.JsoupResultParser;
import de.schalter.sermononline.parser.NoDataFoundException;
import de.schalter.sermononline.settings.Settings;
import de.schalter.sermononline.views.SermonView;

public class ResultActivity extends AppCompatActivity {

    static private final int COUNTELEMENTS = 25;
    static private final int ELEMENTS_LEFT_TO_LOAD = 2;

    static public final String SEARCHTEXT = "search";
    static public final String LANGUAGE = "language";
    static public final String CATEGORY = "category";
    static public final String AUTHOR = "author";
    static public final String MEDIATYPES = "mediaTypes";

    private ListView listViewContent;
    private ListAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private CoordinatorLayout coordinatorLayout;

    private String searchText;
    private int language;
    private int category;
    private int author;
    private int mediaTypes;
    private int lastIndex;
    private boolean loading;
    private boolean error;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Get extras
        Intent intent = getIntent();
        searchText = intent.getStringExtra(SEARCHTEXT);
        language = intent.getIntExtra(LANGUAGE, 0);
        category = intent.getIntExtra(CATEGORY, 0);
        author = intent.getIntExtra(AUTHOR, 0);
        mediaTypes = intent.getIntExtra(MEDIATYPES, 0);
        getSupportActionBar().setTitle(searchText);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        swipeRefreshLayout = findViewById(R.id.refresh_result);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadDataAsynchron(searchUrl(searchText, Settings.getSystemLanguageCode(), 1), true);
                lastIndex = COUNTELEMENTS;
            }
        });

        coordinatorLayout = findViewById(R.id.coordinator_result);
        listViewContent = findViewById(R.id.listView_content);
        listViewContent.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SermonView sermonView = (SermonView) view;
                sermonView.clickStartActivity();
            }
        });

        loading = true;
        error = false;

        listViewContent.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if(loading) {
                    //this is before the first data was loaded
                    //or data is already loading in background
                    return;
                }

                if(error) {
                    if(firstVisibleItem + visibleItemCount <= totalItemCount - ELEMENTS_LEFT_TO_LOAD * 2) {
                        error = false;
                    } else {
                        return;
                    }
                }

                //load new contents when at bottom
                if(firstVisibleItem + visibleItemCount >= totalItemCount - ELEMENTS_LEFT_TO_LOAD) {
                    loadNewData();
                }
            }
        });

        loadData();
    }

    public void snackbar(int message, int duration) {
        Snackbar.make(coordinatorLayout, message, duration).show();
    }

    public void snackbarOnUI(final int message, final int duration) {
        Utils.runOnUiThread(this, new Runnable() {
            @Override
            public void run() {
                snackbar(message, duration);
            }
        });
    }

    private void loadData() {
        loading = true;
        List<SermonView> views = new ArrayList<>();
        adapter = new ListAdapter(this, views);

        listViewContent.setAdapter(adapter);

        String url = searchUrl(searchText, Settings.getSystemLanguageCode(), 1);
        lastIndex = COUNTELEMENTS;
        loadDataAsynchron(url, true);
    }

    private void loadNewData() {
        loading = true;
        String url = searchUrl(searchText, Settings.getSystemLanguageCode(), lastIndex + 1);
        lastIndex += COUNTELEMENTS;
        loadDataAsynchron(url, false);
    }

    /**
     * cenerates a url with search request and starts the resultActivity
     * @param searchText searchText
     */
    private String searchUrl(String searchText, String languageString, int startIndex) {
        String searchEncoded = Uri.encode(searchText);
        String url = "http://sermon-online.com/search.pl?" +
                "lang=" + languageString +
                "&id=0" +
                "&start=" + startIndex +
                "&searchstring=" + searchEncoded +
                "&author=" + author +
                "&language=" + language +
                "&category=" + category +
                "&mediatype=" + mediaTypes +
                "&order=12" +
                "&count=" + COUNTELEMENTS +
                "&x=0" +
                "&y=0";

        Log.d("SermonOnline", "Result-Url: " + url);
        return url;
    }

    /**
     * Connect to sermon-online.com, parse website and show data
     */
    private void loadDataAsynchron(final String url, final boolean clearList) {
        swipeRefreshLayout.setRefreshing(true);
        if(clearList)
            adapter.clear();
        snackbarOnUI(R.string.connecting, Snackbar.LENGTH_INDEFINITE);

        Thread loadInBackground = new Thread(new Runnable() {
            @Override
            public void run() {
                final List<SermonView> views = new ArrayList<>();

                Looper.prepare();

                JsoupResultParser parser = new JsoupResultParser();
                try {
                    parser.connect(url);

                    snackbarOnUI(R.string.parsing, Snackbar.LENGTH_INDEFINITE);

                    parser.parse();

                    snackbarOnUI(R.string.buildingScreen, Snackbar.LENGTH_INDEFINITE);

                    List<SermonListElement> elements = parser.getSermonElements();
                    for(SermonListElement element : elements) {
                        views.add(new SermonView(ResultActivity.this, element));
                    }

                    ResultActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            adapter.addAll(views);
                            adapter.notifyDataSetChanged();

                            swipeRefreshLayout.setRefreshing(false);
                            snackbarOnUI(R.string.finished, Snackbar.LENGTH_SHORT);
                            loading = false;
                        }
                    });
                } catch (IOException e) {
                    if(clearList) {
                        e.printStackTrace();
                        snackbarOnUI(R.string.network_error, Snackbar.LENGTH_LONG);
                        new ErrorDialog(ResultActivity.this, getString(R.string.error), getString(R.string.network_error) + "\n\n" + e.getMessage())
                                .setOnClickListener(getOnClickListener())
                                .showOnMainThread();
                    } else {
                        //data was loaded after scrolled to bottom
                        snackbarOnUI(R.string.network_error, Snackbar.LENGTH_LONG);
                        Utils.runOnUiThread(ResultActivity.this, new Runnable() {
                            @Override
                            public void run() {
                                swipeRefreshLayout.setRefreshing(false);
                                loading = false;
                                error = true;
                                lastIndex -= COUNTELEMENTS;
                            }
                        });
                    }
                } catch (NoDataFoundException e) {
                    if(clearList) {
                        e.printStackTrace();
                        snackbarOnUI(R.string.data_error, Snackbar.LENGTH_LONG);
                        new ErrorDialog(ResultActivity.this, getString(R.string.error), getString(R.string.data_error) + "\n\n" + e.getMessage())
                                .setOnClickListener(getOnClickListener())
                                .showOnMainThread();
                    } else {
                        //data was loaded after scrolled to bottom
                        snackbarOnUI(R.string.no_more_data, Snackbar.LENGTH_LONG);
                        Utils.runOnUiThread(ResultActivity.this, new Runnable() {
                            @Override
                            public void run() {
                                swipeRefreshLayout.setRefreshing(false);
                                loading = false;
                                error = true;
                                lastIndex -= COUNTELEMENTS;
                            }
                        });
                    }
                }
            }
        });
        loadInBackground.start();
    }

    private DialogInterface.OnClickListener getOnClickListener() {
        return new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                ResultActivity.this.finish();
            }
        };
    }

    private class ListAdapter extends ArrayAdapter<SermonView> {

        ListAdapter(@NonNull Context context, List<SermonView> elements) {
            super(context, 0, elements);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = getItem(position);

            return convertView;
        }
    }

}
