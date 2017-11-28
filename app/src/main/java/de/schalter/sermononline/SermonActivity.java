package de.schalter.sermononline;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.support.design.widget.FloatingActionButton;
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

    private SermonElement sermonElement;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sermon);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        table = (TableLayout) findViewById(R.id.table_sermonData);
        FloatingActionButton floatingActionButton = (FloatingActionButton) findViewById(R.id.floatingButton_sermon);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                download();
            }
        });

        Intent intent = getIntent();
        url = intent.getStringExtra(URL);

        loadDataAsynchron();
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
        final WaitDialog waitDialog = new WaitDialog(this, R.string.searching);
        waitDialog.show();

        waitDialog.updateMessage(R.string.connecting);

        Thread loadInBackground = new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();

                JsoupSermonParser parser = new JsoupSermonParser();
                try {
                    parser.connect(url);

                    waitDialog.updateMessageOnMainThread(R.string.parsing);

                    parser.parse();

                    waitDialog.updateMessageOnMainThread(R.string.buildingScreen);

                    final SermonElement sermonElement = parser.getSermonElement();
                    SermonActivity.this.sermonElement = sermonElement;

                    Utils.runOnUiThread(SermonActivity.this, new Runnable() {
                        @Override
                        public void run() {
                            loadSermon(sermonElement);
                            waitDialog.close();
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                    waitDialog.closeOnMainThread();
                    new ErrorDialog(SermonActivity.this, getString(R.string.error), getString(R.string.network_error) + "\n\n" + e.getMessage())
                            .setOnClickListener(getOnClickListener())
                            .showOnMainThread();
                } catch (NoDataFoundException e) {
                    e.printStackTrace();
                    waitDialog.closeOnMainThread();
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
