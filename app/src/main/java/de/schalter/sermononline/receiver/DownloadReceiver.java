package de.schalter.sermononline.receiver;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;

import de.schalter.sermononline.DBHelper;

/**
 * Receiver to be notified when a download is finished
 * Created by martin on 23.11.17.
 */

public class DownloadReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {
        String action = intent.getAction();
        if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);

            // get the DownloadManager instance
            DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);

            DownloadManager.Query q = new DownloadManager.Query();
            Cursor c = manager.query(q);

            String downloadPath = null;


            if(c.moveToFirst()) {
                do {
                    if(c.getLong(c.getColumnIndex(DownloadManager.COLUMN_ID)) == id) {
                        downloadPath = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                        break;
                    }
                } while (c.moveToNext());
            }

            c.close();


            DBHelper dbHelper = DBHelper.getInstance(context);
            dbHelper.downloadCompleted(id, downloadPath);
        }
    }
}
