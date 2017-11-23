package de.schalter.sermononline;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.schalter.sermononline.dialogs.ErrorDialog;
import de.schalter.sermononline.dialogs.WaitDialog;
import de.schalter.sermononline.parser.JsoupParser;
import de.schalter.sermononline.parser.NoDataFoundException;
import de.schalter.sermononline.parser.SermonListElement;
import de.schalter.sermononline.views.SermonView;

public class ResultActivity extends AppCompatActivity {

    static public final String SEARCH = "search";
    static public final String URL = "url";

    private ListView listViewContent;
    private ListAdapter adapter;

    private String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Get extras
        Intent intent = getIntent();
        url = intent.getStringExtra(URL);
        String searchString = intent.getStringExtra(SEARCH);
        getSupportActionBar().setTitle(searchString);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        listViewContent = (ListView) findViewById(R.id.listView_content);
        listViewContent.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SermonView sermonView = (SermonView) view;
                sermonView.click();
            }
        });

        loadData();
    }

    private void loadData() {
        List<SermonView> views = new ArrayList<>();
        adapter = new ListAdapter(this, views);

        listViewContent.setAdapter(adapter);

        loadDataAsynchron();
    }

    /**
     * Connect to sermon-online.com, parse website and show data
     */
    private void loadDataAsynchron() {
        final WaitDialog dialog = new WaitDialog(ResultActivity.this, R.string.searching);
        dialog.show();

        dialog.updateMessage(R.string.connecting);

        Thread loadInBackground = new Thread(new Runnable() {
            @Override
            public void run() {
                final List<SermonView> views = new ArrayList<>();

                Looper.prepare();

                JsoupParser parser = new JsoupParser();
                try {
                    parser.connect(url);

                    dialog.updateMessageOnMainThread(R.string.parsing);

                    parser.parse();

                    dialog.updateMessageOnMainThread(R.string.buildingScreen);

                    List<SermonListElement> elements = parser.getSermonElements();
                    for(SermonListElement element : elements) {
                        views.add(new SermonView(ResultActivity.this, element));
                    }

                    ResultActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            adapter.addAll(views);
                            adapter.notifyDataSetChanged();

                            dialog.close();
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                    dialog.closeOnMainThread();
                    new ErrorDialog(ResultActivity.this, getString(R.string.error), getString(R.string.network_error) + "\n\n" + e.getMessage())
                            .setOnClickListener(getOnClickListener())
                            .showOnMainThread();
                } catch (NoDataFoundException e) {
                    e.printStackTrace();
                    dialog.closeOnMainThread();
                    new ErrorDialog(ResultActivity.this, getString(R.string.error), getString(R.string.data_error) + "\n\n" + e.getMessage())
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
