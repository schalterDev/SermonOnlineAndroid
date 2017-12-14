package de.schalter.sermononline;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.IOException;
import java.io.InvalidClassException;
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
    private static final int VERSION = 2;

    private static final String KEY_ID = "id";
    private static final String KEY_DOWNLOADURL = "downloadUrl";
    private static final String KEY_DOWNLOADURL_FILE = "downloadUrlFile";
    private static final String KEY_PATH = "path";
    private static final String KEY_SERMONOBJECT = "sermonObject";
    private static final String KEY_DOWNLOAD_ID = "downloadId";
    private static final String KEY_NOTES = "notes";
    private static final String KEY_TIMELASTOPENED = "lastTimeOpened";
    private static final String KEY_LASTAUDIOPOSITION = "lastAudioPosition";

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
                KEY_DOWNLOADURL_FILE + " TEXT, " +
                KEY_SERMONOBJECT + " TEXT, " +
                KEY_PATH + " TEXT, " +
                KEY_NOTES + " TEXT, " +
                KEY_TIMELASTOPENED + " INTEGER, " +
                KEY_LASTAUDIOPOSITION + " INTEGER" +
                ");";

        db.execSQL(sqlCreate);
    }

    private static final String DATABASE_ALTER_ADD_NOTES = "ALTER TABLE "
            + T_DOWNLOADS +
            " ADD COLUMN " + KEY_NOTES + " TEXT;";

    private static final String DATABASE_ALTER_ADD_TIMELASTOPENED = "ALTER TABLE "
            + T_DOWNLOADS +
            " ADD COLUMN " + KEY_TIMELASTOPENED + " INTEGER;";

    private static final String DATABASE_ALTER_ADD_LASTAUDIOPOSITION = "ALTER TABLE "
            + T_DOWNLOADS +
            " ADD COLUMN " + KEY_LASTAUDIOPOSITION + " INTEGER;";

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        switch(oldVersion) {
            case 1:
                //upgrade logic from version 1 to 2
                db.execSQL(DATABASE_ALTER_ADD_NOTES);
                db.execSQL(DATABASE_ALTER_ADD_TIMELASTOPENED);
                db.execSQL(DATABASE_ALTER_ADD_LASTAUDIOPOSITION);
            case 2:
                //upgrade logic from version 2 to 3
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
        try {
            values.put(KEY_SERMONOBJECT, Utils.toString(sermonElement));
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        Cursor cursor = db.rawQuery("select " + KEY_ID + ", " + KEY_SERMONOBJECT + ", " +
                KEY_NOTES + ", " + KEY_TIMELASTOPENED + ", " + KEY_LASTAUDIOPOSITION + " from " + T_DOWNLOADS
                + " ORDER BY " + KEY_ID + " DESC",null);

        List<SermonElement> downloadElements = new ArrayList<>();
        List<Integer> removeElements = new ArrayList<>();

        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                try {
                    SermonElement sermonElement = (SermonElement) Utils.fromString(cursor.getString(1));
                    sermonElement.id = cursor.getInt(0);
                    if(!cursor.isNull(2))
                        sermonElement.setNotes(cursor.getString(2));
                    if(!cursor.isNull(3))
                        sermonElement.setTimeLastOpened(cursor.getLong(3));
                    if(!cursor.isNull(4))
                        sermonElement.setLastAudioPosition(cursor.getInt(4));
                    downloadElements.add(sermonElement);
                } catch (InvalidClassException e) {
                    //Delete this entry
                    removeElements.add(cursor.getInt(0));
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }

                cursor.moveToNext();
            }
        }

        cursor.close();
        db.close();

        for(Integer id : removeElements) {
            removeDownload(id);
        }

        return downloadElements;
    }

    public SermonElement getSermonElement(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select " + KEY_SERMONOBJECT + ", " +
                        KEY_NOTES + ", " + KEY_TIMELASTOPENED + ", " + KEY_LASTAUDIOPOSITION +
                        " from " + T_DOWNLOADS + " WHERE " + KEY_ID + " = " + id,null);

        SermonElement sermonElement = null;

        if (cursor.moveToFirst()) {
            try {
                sermonElement = (SermonElement) Utils.fromString(cursor.getString(0));
                sermonElement.id = id;
                if(!cursor.isNull(1))
                    sermonElement.setNotes(cursor.getString(1));
                if(!cursor.isNull(2))
                    sermonElement.setTimeLastOpened(cursor.getLong(2));
                if(!cursor.isNull(3))
                    sermonElement.setLastAudioPosition(cursor.getInt(3));
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
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
