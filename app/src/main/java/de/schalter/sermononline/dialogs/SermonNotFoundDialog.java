package de.schalter.sermononline.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;

import de.schalter.sermononline.DBHelper;
import de.schalter.sermononline.R;
import de.schalter.sermononline.SermonActivity;
import de.schalter.sermononline.Utils;
import de.schalter.sermononline.objects.SermonElement;

/**
 * Created by martin on 27.11.17.
 */

public class SermonNotFoundDialog {

    private AlertDialog.Builder builder;

    private Activity activity;

    private int sermonId;

    /**
     *
     * @param activity activity
     * @param sermonId id (from database) of the sermon
     */
    public SermonNotFoundDialog(final Activity activity, int sermonId) {
        this.activity = activity;
        this.sermonId = sermonId;

        builder = new AlertDialog.Builder(this.activity);
        builder.setTitle(R.string.fileNotExists);

        builder.setPositiveButton(R.string.redownload, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                download();
            }
        });

        builder.setNeutralButton(R.string.open_sermon_page, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                openSermonPage();
            }
        });

        builder.setNegativeButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                delete();
            }
        });
    }

    public void show() {
        Dialog dialog = builder.create();

        dialog.show();
    }

    /**
     * redownload the sermon
     */
    private void download() {
        DBHelper dbHelper = DBHelper.getInstance(activity);
        Utils.deleteDataFromDownloadManager(activity, dbHelper.getDownloadId(sermonId));
        String fileUrl = dbHelper.getFileUrl(sermonId);
        SermonElement sermonElement = dbHelper.getSermonElement(sermonId);
        Utils.downloadSermon(activity, sermonElement, fileUrl);
    }

    /**
     * Open the sermon page in the app
     */
    private void openSermonPage() {
        DBHelper dbHelper = DBHelper.getInstance(activity);
        Intent intent = new Intent(activity, SermonActivity.class);
        intent.putExtra(SermonActivity.URL, dbHelper.getSermonPageUrl(sermonId));
        activity.startActivity(intent);
    }

    /**
     * deletes the entry from database
     */
    private void delete() {
        DBHelper dbHelper = DBHelper.getInstance(activity);
        long downloadId = dbHelper.getDownloadId(sermonId);
        Utils.deleteDataFromDownloadManager(activity, downloadId);
        dbHelper.removeDownload(sermonId);
    }

}
