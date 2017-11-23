package de.schalter.sermononline.dialogs;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.IOException;
import java.util.HashMap;

import de.schalter.sermononline.DBHelper;
import de.schalter.sermononline.Utils;
import de.schalter.sermononline.parser.SermonElement;

/**
 * Created by martin on 22.11.17.
 */

public class SermonDownloadDialog {

    private AlertDialog.Builder builder;
    private Dialog dialog;

    private Activity activity;
    private final int PERMISSION_REQUEST_EXTERNAL_STORAGE = 0;

    public SermonDownloadDialog(final Activity activity, int ressource, final HashMap<String, String> links, final SermonElement sermonElement) {
        this.activity = activity;

        permission();

        builder = new AlertDialog.Builder(this.activity);
        builder.setTitle(ressource);

        ListView modeList = new ListView(this.activity);

        String[] stringArray = new String[links.keySet().size()];
        final String[] linksArray = new String[links.keySet().size()];
        int counter = 0;
        for(String key : links.keySet()) {
            stringArray[counter] = key;
            linksArray[counter] = links.get(key);
            counter++;
        }

        ArrayAdapter<String> modeAdapter = new ArrayAdapter<>(this.activity, android.R.layout.simple_list_item_1, android.R.id.text1, stringArray);
        modeList.setAdapter(modeAdapter);
        modeList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String title = sermonElement.data.get(0);
                String link = linksArray[position];
                int indexLastPoint = link.lastIndexOf(".");
                String fileEnding = link.substring(indexLastPoint);

                long downloadId = Utils.downloadData(SermonDownloadDialog.this.activity, Uri.parse(link),
                        title, title, title + "." + fileEnding);

                DBHelper dbHelper = DBHelper.getInstance(SermonDownloadDialog.this.activity);
                try {
                    dbHelper.downloadStarted(linksArray[position], sermonElement, downloadId);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Utils.runOnUiThread(activity, new Runnable() {
                    @Override
                    public void run() {
                        dialog.cancel();
                    }
                });
            }
        });

        builder.setView(modeList);
    }

    public void show() {
        dialog = builder.create();

        dialog.show();
    }

    private void permission() {
        int permissionCheck = ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if(permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_EXTERNAL_STORAGE);

        }
    }

}
