package de.schalter.sermononline;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.schalter.sermononline.parser.SermonElement;
import de.schalter.sermononline.views.DownloadElement;

/**
 * Database to store downloads
 * Created by martin on 22.11.17.
 */

public class DBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "sermonOnline";
    private static final String T_DOWNLOADS = "t_downloads";
    private static final int VERSION = 1;

    private static final String KEY_ID = "id";
    private static final String KEY_DOWNLOADURL = "downloadUrl";
    private static final String KEY_PATH = "path";
    private static final String KEY_SERMONOBJECT = "sermonObject";
    private static final String KEY_DOWNLOAD_ID = "downloadId";

    private static DBHelper instance;

    private Context context;

    private DBHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
        this.context = context;
    }

    public static DBHelper getInstance(Context context) {
        if(instance == null)
            instance = new DBHelper(context);

        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sqlCreate = "create table if not exists " + T_DOWNLOADS + "(" +
                KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                KEY_DOWNLOAD_ID + " INTEGER, " +
                KEY_DOWNLOADURL + " TEXT, " +
                KEY_SERMONOBJECT + " TEXT, " +
                KEY_PATH + " TEXT" +
                ");";

        db.execSQL(sqlCreate);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    /**
     * Save a download when it's started in the database.
     * @param downloadUrl remote url of the sermon info page
     * @param sermonElement downloaded sermon element
     * @param downloadKeyId downloadId from DownloadManager
     */
    public void downloadStarted(String downloadUrl, SermonElement sermonElement, long downloadKeyId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_DOWNLOADURL, downloadUrl);
        try {
            values.put(KEY_SERMONOBJECT, Utils.toString(sermonElement));
        } catch (IOException e) {
            e.printStackTrace();
        }
        values.put(KEY_DOWNLOAD_ID, downloadKeyId);

        // Inserting Row
        db.insert(T_DOWNLOADS, null, values);
    }

    /**
     * Save the path on file system for a download
     * @param downloadKeyId downloadId from DownloadManager
     * @param path filePath on the local file system
     */
    public void downloadCompleted(long downloadKeyId, String path) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(KEY_PATH, path);

        // update Row
        db.update(T_DOWNLOADS, cv, KEY_DOWNLOAD_ID + "=" + downloadKeyId, null);
    }

    /**
     * Get all downloads which are started (and not completed) and the completed ones
     * @return all downloads
     */
    public List<DownloadElement> getAllDownloads() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select " + KEY_PATH + ", " + KEY_SERMONOBJECT + " from " + T_DOWNLOADS,null);

        List<DownloadElement> downloadElements = new ArrayList<>();

        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                DownloadElement downloadElement = new DownloadElement();

                downloadElement.path = cursor.getString(0);
                try {
                    downloadElement.sermonElement = (SermonElement) Utils.fromString(cursor.getString(1));
                    downloadElements.add(downloadElement);
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }

                cursor.moveToNext();
            }
        }

        cursor.close();

        return downloadElements;
    }
}
