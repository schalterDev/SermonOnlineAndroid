package de.schalter.sermononline;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import de.schalter.sermononline.objects.SermonElement;

/**
 * Database to store downloads
 * Created by martin on 22.11.17.
 */

public class DBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "sermonOnline";
    private static final String T_DOWNLOADS = "t_downloads";
    private static final int VERSION = 3;

    private static final String KEY_ID = "id";
    private static final String KEY_DOWNLOADURL = "downloadUrl";
    private static final String KEY_DOWNLOADURL_FILE = "downloadUrlFile";
    private static final String KEY_PATH = "path";

    //dont use this in newer versions
    @Deprecated
    private static final String KEY_SERMONOBJECT = "sermonObject";
    //use this instead
    private static final String KEY_HEADERS = "sermonHeaders";
    private static final String KEY_DATA = "sermonData";

    private static final String KEY_DOWNLOAD_ID = "downloadId";
    private static final String KEY_NOTES = "notes";
    private static final String KEY_TIMELASTOPENED = "lastTimeOpened";
    private static final String KEY_LASTAUDIOPOSITION = "lastAudioPosition";
    private static final String KEY_MARKED = "marked";

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

    private static final String CREATE_TABLE_V_1 = "create table if not exists " + T_DOWNLOADS + "(" +
                                                    KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                                    KEY_DOWNLOAD_ID + " INTEGER, " +
                                                    KEY_DOWNLOADURL + " TEXT, " +
                                                    KEY_DOWNLOADURL_FILE + " TEXT, " +
                                                    KEY_SERMONOBJECT + " TEXT, " +
                                                    KEY_PATH + " TEXT" +
                                                            ");";

    private static final String CREATE_TABLE_V_2 = "create table if not exists " + T_DOWNLOADS + "(" +
                                                    KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                                    KEY_DOWNLOAD_ID + " INTEGER, " +
                                                    KEY_DOWNLOADURL + " TEXT, " +
                                                    KEY_DOWNLOADURL_FILE + " TEXT, " +
                                                    KEY_SERMONOBJECT + " TEXT, " +
                                                    KEY_PATH + " TEXT, " +
                                                    KEY_NOTES + " TEXT, " +
                                                    KEY_TIMELASTOPENED + " INTEGER, " +
                                                    KEY_LASTAUDIOPOSITION + " INTEGER" +
                                                        ");";

    private static final String CREATE_TABLE_V_3 = "create table if not exists " + T_DOWNLOADS + "(" +
                                                    KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                                    KEY_DOWNLOAD_ID + " INTEGER, " +
                                                    KEY_DOWNLOADURL + " TEXT, " +
                                                    KEY_DOWNLOADURL_FILE + " TEXT, " +
                                                    KEY_HEADERS + " TEXT, " +
                                                    KEY_DATA + " TEXT, " +
                                                    KEY_PATH + " TEXT, " +
                                                    KEY_NOTES + " TEXT, " +
                                                    KEY_TIMELASTOPENED + " INTEGER, " +
                                                    KEY_LASTAUDIOPOSITION + " INTEGER, " +
                                                    KEY_MARKED + " INTEGER" +
                                                            ");";

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sqlCreate = CREATE_TABLE_V_3;

        db.execSQL(sqlCreate);
    }

    private static final String DROP_TABLE = "DROP TABLE " + T_DOWNLOADS + ";";

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        switch(oldVersion) {
            case 2:
                //upgrade logic from version 2 to 3
                db.execSQL(DROP_TABLE);
                db.execSQL(CREATE_TABLE_V_3);

            case 3:
                //upgrade logic from version 3 to 4
                break;
            default:
                throw new IllegalStateException(
                        "onUpgrade() with unknown oldVersion " + oldVersion);
        }
    }

    /**
     * Save a download when it's started in the database.
     * @param downloadUrl remote url of the sermon info page
     * @param downloadUrlFile remove url of the file that was downloaded
     * @param sermonElement downloaded sermon element
     * @param downloadKeyId downloadId from DownloadManager
     */
    public void downloadStarted(String downloadUrl, String downloadUrlFile, SermonElement sermonElement, long downloadKeyId) {
        boolean newEntry = true;

        //First check if downloadUrl is already in the system
        SQLiteDatabase read = this.getReadableDatabase();

        String downloadUrlFileSql = DatabaseUtils.sqlEscapeString(downloadUrlFile);

        String checkIfExistsSql = "select " + KEY_DOWNLOAD_ID + " from " + T_DOWNLOADS +
                " where " + KEY_DOWNLOADURL_FILE + " = " + downloadUrlFileSql;

        Cursor cursor = read.rawQuery(checkIfExistsSql,null);

        if (cursor.moveToFirst()) {
            String url = cursor.getString(0);
            if(url != null && !url.equals("")) {
                newEntry = false;
            }
        }

        read.close();

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_DOWNLOADURL, downloadUrl);
        values.put(KEY_DOWNLOADURL_FILE, downloadUrlFile);
        values.put(KEY_HEADERS, Utils.convertListToString(sermonElement.getHeaders()));
        values.put(KEY_DATA, Utils.convertListToString(sermonElement.getData()));
        values.put(KEY_DOWNLOAD_ID, downloadKeyId);

        if(newEntry) {
            // Inserting Row
            db.insert(T_DOWNLOADS, null, values);
        } else {
            //updating row
            db.update(T_DOWNLOADS, values, KEY_DOWNLOADURL_FILE + " = " + downloadUrlFileSql, null);
        }

        db.close();
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

        db.close();
    }

    /**
     * Get all downloads which are started (and not completed) and the completed ones
     * @return all downloads
     */
    public List<SermonElement> getAllDownloads() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select " + KEY_ID + ", " + KEY_DATA + ", " + KEY_HEADERS + ", " +
                KEY_NOTES + ", " + KEY_TIMELASTOPENED + ", " + KEY_LASTAUDIOPOSITION + " from " + T_DOWNLOADS
                + " ORDER BY " + KEY_ID + " DESC",null);

        List<SermonElement> downloadElements = new ArrayList<>();

        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                List<String> data = null;
                if(!cursor.isNull(1))
                    data = Utils.convertStringToList(cursor.getString(1));

                List<String> headers = null;
                if(!cursor.isNull(1))
                    headers = Utils.convertStringToList(cursor.getString(2));

                SermonElement sermonElement = new SermonElement(headers, data);
                sermonElement.id = cursor.getInt(0);
                if(!cursor.isNull(3))
                    sermonElement.setNotes(cursor.getString(3));
                if(!cursor.isNull(4))
                    sermonElement.setTimeLastOpened(cursor.getLong(4));
                if(!cursor.isNull(5))
                    sermonElement.setLastAudioPosition(cursor.getInt(5));
                downloadElements.add(sermonElement);

                cursor.moveToNext();
            }
        }

        cursor.close();
        db.close();

        return downloadElements;
    }

    public SermonElement getSermonElement(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select " + KEY_DATA + ", " + KEY_HEADERS + ", " +
                        KEY_NOTES + ", " + KEY_TIMELASTOPENED + ", " + KEY_LASTAUDIOPOSITION +
                        " from " + T_DOWNLOADS + " WHERE " + KEY_ID + " = " + id,null);

        SermonElement sermonElement = null;

        if (cursor.moveToFirst()) {
            List<String> data = null;
            if(!cursor.isNull(1))
                data = Utils.convertStringToList(cursor.getString(0));

            List<String> headers = null;
            if(!cursor.isNull(1))
                headers = Utils.convertStringToList(cursor.getString(1));

            sermonElement = new SermonElement(headers, data);
            sermonElement.id = id;
            if(!cursor.isNull(2))
                sermonElement.setNotes(cursor.getString(2));
            if(!cursor.isNull(3))
                sermonElement.setTimeLastOpened(cursor.getLong(3));
            if(!cursor.isNull(4))
                sermonElement.setLastAudioPosition(cursor.getInt(4));
        }

        cursor.close();
        db.close();

        return sermonElement;
    }

    public String getRessourcePath(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select " + KEY_PATH + " from " + T_DOWNLOADS + " WHERE " + KEY_ID + " = " + id, null);

        if(cursor.moveToFirst()) {
            String path = cursor.getString(0);
            cursor.close();
            return path;
        }

        cursor.close();
        db.close();

        return null;
    }

    public void removeDownload(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        db.delete(T_DOWNLOADS, KEY_ID + " = " + id, null);
        db.close();
    }

    public long getDownloadId(int sermonId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select " + KEY_DOWNLOAD_ID + " from " + T_DOWNLOADS + " WHERE " + KEY_ID + " = " + sermonId, null);

        if(cursor.moveToFirst()) {
            long downloadId = cursor.getLong(0);
            cursor.close();
            return downloadId;
        }

        cursor.close();
        db.close();

        return -1;
    }

    private String getColumn(String column, int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select " + column + " from " + T_DOWNLOADS + " WHERE " + KEY_ID + " = " + id, null);

        if(cursor.moveToFirst()) {
            String columnResult = cursor.getString(0);
            cursor.close();
            return columnResult;
        }

        cursor.close();
        db.close();

        return null;
    }

    public void insertNote(int id, String note) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(KEY_NOTES, note);

        // update Row
        db.update(T_DOWNLOADS, cv, KEY_ID + "=" + id, null);

        db.close();
    }

    public String getSermonPageUrl(int id) {
        return getColumn(KEY_DOWNLOADURL, id);
    }

    public String getFileUrl(int id) {
        return getColumn(KEY_DOWNLOADURL_FILE, id);
    }

    public void updateDownloadManagerId(int id, long downloadManagerId) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(KEY_DOWNLOAD_ID, downloadManagerId);

        // update Row
        db.update(T_DOWNLOADS, cv, KEY_ID + "=" + id, null);

        db.close();
    }
}
