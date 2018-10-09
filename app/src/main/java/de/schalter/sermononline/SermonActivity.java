package de.schalter.sermononline;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.io.IOException;
import java.util.HashMap;

import de.schalter.sermononline.dialogs.ErrorDialog;
import de.schalter.sermononline.dialogs.SermonDownloadDialog;
import de.schalter.sermononline.objects.SermonElement;
import de.schalter.sermononline.parser.JsoupSermonParser;
import de.schalter.sermononline.parser.NoDataFoundException;

public class SermonActivity extends AppCompatActivity {

    static final String linkContains = "sermon-online.com";
    static public final String URL = "url";

    private String url;

    private TableLayout table;
    private SwipeRefreshLayout swipeRefreshLayout;
    private CoordinatorLayout coordinatorLayout;
    private FloatingActionButton floatingActionButton;

    private SermonElement sermonElement;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sermon);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        swipeRefreshLayout = findViewById(R.id.refresh_sermon);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadDataAsynchron();
            }
        });

        coordinatorLayout = findViewById(R.id.coordinator_sermon);
        floatingActionButton = findViewById(R.id.floatingButton_sermon);

        table = findViewById(R.id.table_sermonData);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                download();
            }
        });
        floatingActionButton.setEnabled(false);

        Intent intent = getIntent();
        url = intent.getStringExtra(URL);

        loadDataAsynchron();
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

    /**
     * download button was clicked
     */
    private void download() {
        HashMap<String, String> links = sermonElement.getLinksWithDescription(linkContains);

        new SermonDownloadDialog(this, R.string.available_downloads, links, sermonElement).show();
    }

    /**
     * connect to sermon-online.com, download, parse data and display it
     */
    private void loadDataAsynchron() {
        swipeRefreshLayout.setRefreshing(true);
        snackbarOnUI(R.string.connecting, Snackbar.LENGTH_INDEFINITE);

        Thread loadInBackground = new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();

                JsoupSermonParser parser = new JsoupSermonParser();
                try {
                    parser.connect(url);

                    snackbarOnUI(R.string.parsing, Snackbar.LENGTH_INDEFINITE);

                    parser.parse();

                    snackbarOnUI(R.string.buildingScreen, Snackbar.LENGTH_INDEFINITE);

                    final SermonElement sermonElement = parser.getSermonElement();
                    SermonActivity.this.sermonElement = sermonElement;

                    Utils.runOnUiThread(SermonActivity.this, new Runnable() {
                        @Override
                        public void run() {
                            loadSermon(sermonElement);
                            swipeRefreshLayout.setRefreshing(false);
                            floatingActionButton.setEnabled(true);
                            snackbar(R.string.finished, Snackbar.LENGTH_SHORT);
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                    snackbarOnUI(R.string.network_error, Snackbar.LENGTH_LONG);
                    new ErrorDialog(SermonActivity.this, getString(R.string.error), getString(R.string.network_error) + "\n\n" + e.getMessage())
                            .setOnClickListener(getOnClickListener())
                            .showOnMainThread();
                } catch (NoDataFoundException e) {
                    e.printStackTrace();
                    snackbarOnUI(R.string.data_error, Snackbar.LENGTH_LONG);
                    new ErrorDialog(SermonActivity.this, getString(R.string.error), getString(R.string.data_error) + "\n\n" + e.getMessage())
                            .setOnClickListener(getOnClickListener())
                            .showOnMainThread();
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
                SermonActivity.this.finish();
            }
        };
    }

    private void loadSermon(SermonElement sermonElement) {
        final int TITLE = 0;
        final int AUTHOR = 1;
        final int PASSAGES = 2;
        final int LANGUAGE = 3;
        final int CATEGORIE = 4;
        final int DATE = 5;
        final int DURATION = 6;
        final int PAGES = 7;
        final int COUNTROWS = 8;

        table.setColumnShrinkable(1, true);
        for(int i = 0; i < COUNTROWS; i++) {
            TableRow row = new TableRow(this);
            TextView title = new TextView(this);
            title.setMaxWidth((int) Utils.convertDpToPixel(120));

            TextView content = new TextView(this);
            content.setPadding(dpToPx(3), 0, 0, 0);

            boolean add = false;

            switch(i) {
                case TITLE:
                    if(sermonElement.getTitle() != null) {
                        title.setText(R.string.title);
                        content.setText(sermonElement.getTitle());
                        add = true;
                    }
                    break;
                case AUTHOR:
                    if(sermonElement.getAuthor() != null) {
                        title.setText(R.string.author);
                        content.setText(sermonElement.getAuthor());
                        add = true;
                    }
                    break;
                case PASSAGES:
                    if(sermonElement.getPassage() != null) {
                        title.setText(R.string.passages);
                        content.setText(sermonElement.getPassage());
                        add = true;
                    }
                    break;
                case LANGUAGE:
                    if(sermonElement.getLanguage() != null) {
                        title.setText(R.string.language);
                        content.setText(sermonElement.getLanguage());
                        add = true;
                    }
                    break;
                case CATEGORIE:
                    if(sermonElement.getCategory() != null) {
                        title.setText(R.string.category);
                        content.setText(sermonElement.getCategory());
                        add = true;
                    }
                    break;
                case DATE:
                    if(sermonElement.getDateAsString() != null) {
                        title.setText(R.string.date);
                        content.setText(sermonElement.getDateAsString());
                        add = true;
                    }
                    break;
                case DURATION:
                    if(sermonElement.getDurationAsString() != null) {
                        title.setText(R.string.duration);
                        content.setText(sermonElement.getDurationAsString());
                        add = true;
                    }
                    break;
                case PAGES:
                    if(sermonElement.getPages() != 0) {
                        title.setText(R.string.pages);
                        content.setText(String.valueOf(sermonElement.getPages()));
                        add = true;
                    }
                    break;
            }

            if(add) {
                row.addView(title);
                row.addView(content);
                table.addView(row);
            }
        }
    }

    private int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        if(id == android.R.id.home) {
            finish();
        }
        return true;

    }

}
