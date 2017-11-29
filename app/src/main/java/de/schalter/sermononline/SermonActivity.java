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
import de.schalter.sermononline.dialogs.WaitDialog;
import de.schalter.sermononline.parser.JsoupSermonParser;
import de.schalter.sermononline.parser.NoDataFoundException;
import de.schalter.sermononline.objects.SermonElement;

public class SermonActivity extends AppCompatActivity {

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
        final String linkContains = "dyndns.org";
        HashMap<String, String> links = new HashMap<>();

        for(int indexData : sermonElement.links.keySet()) {
            String link = sermonElement.links.get(indexData);
            if(link.contains(linkContains)) {
                links.put(sermonElement.data.get(indexData), link);
            }
        }

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
        table.setColumnShrinkable(1, true);
        for(int i = 0; i < sermonElement.data.size(); i++) {
            TableRow row = new TableRow(this);
            TextView title = new TextView(this);
            title.setText(sermonElement.headers.get(i));
            title.setMaxWidth((int) Utils.convertDpToPixel(120));

            TextView content = new TextView(this);
            content.setText(sermonElement.data.get(i));

            content.setPadding(dpToPx(3), 0, 0, 0);

            row.addView(title);
            row.addView(content);

            table.addView(row);
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
