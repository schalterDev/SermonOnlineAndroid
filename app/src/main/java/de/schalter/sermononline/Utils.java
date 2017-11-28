package de.schalter.sermononline;

import android.app.DownloadManager;
import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.util.Base64;
import android.util.DisplayMetrics;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.schalter.sermononline.objects.SermonElement;

/**
 * Utils to use everywhere
 * Created by martin on 22.11.17.
 */

public class Utils {

    /** Read the object from Base64 string. */
    public static Object fromString( String s ) throws IOException ,
            ClassNotFoundException {
        byte [] data = Base64.decode(s, Base64.DEFAULT);
        ObjectInputStream ois = new ObjectInputStream(
                new ByteArrayInputStream(  data ) );
        Object o  = ois.readObject();
        ois.close();
        return o;
    }

    /** Write the object to a Base64 string. */
    public static String toString( Serializable o ) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream( baos );
        oos.writeObject( o );
        oos.close();
        return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
    }

    private static Handler mainHandler;

    /**
     * Runs the runnable on main/ui thread
     * @param context context
     * @param run runnable to run
     */
    public static void runOnUiThread(Context context, final Runnable run) {
        if(mainHandler == null)
            mainHandler = new Handler(context.getMainLooper());

        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                run.run();
            }
        });
    }

    /**
     * Download data with android download manager. Will be stored in downloads
     * @param context context
     * @param uri uri
     * @param title title
     * @param description description
     * @param fileName filename
     * @return downloadId from android download manager
     */
    public static long downloadDataWithDownloadManager(Context context, Uri uri, String title, String description, String fileName) {
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);

        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
        request.setAllowedOverRoaming(false);
        request.setTitle(title);
        request.setDescription(description);
        request.setVisibleInDownloadsUi(true);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);

        return downloadManager.enqueue(request);
    }

    public static void downloadSermon(Context context, SermonElement sermonElement, String link) {
        String title = sermonElement.data.get(0);
        int indexLastPoint = link.lastIndexOf(".");
        String fileEnding = link.substring(indexLastPoint + 1);

        long downloadId = Utils.downloadDataWithDownloadManager(context, Uri.parse(link),
                title, title, title + "." + fileEnding);

        DBHelper dbHelper = DBHelper.getInstance(context);
        dbHelper.downloadStarted(sermonElement.sermonUrlPage, link, sermonElement, downloadId);
    }

    public static void deleteDataFromDownloadManager(Context context, long downloadId) {
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);

        if (downloadManager != null) {
            downloadManager.remove(downloadId);
        }
    }

    /**
     * checks if an array contains any element of an list
     * @param array array
     * @param containObjects array with all element to check if they are in the array
     * @param <T>
     * @return -1 if no element from containObjects is in array. Otherwise the first index
     */
    public static <T> int indexOf (T[] array, T[] containObjects) {
        return indexOf(Arrays.asList(array), containObjects);
    }

    /**
     * checks if an array contains any element of an list.
     * @param list list
     * @param containObjects array with all element to check if they are in the array
     * @param <T>
     * @return -1 if no element from containObjects is in array. Otherwise the first index
     */
    public static <T> int indexOf (List<T> list, T[] containObjects) {
        int counter = 0;
        for(T containObject : containObjects) {
            if(list.contains(containObject))
                return list.indexOf(containObject);
        }

        return -1;
    }

    /**
     * Get the file extension from an url or path
     * @param url url or path
     * @return file extension
     */
    public static String getFileExtension(String url) {
        if (url.contains("?")) {
            url = url.substring(0, url.indexOf("?"));
        }
        if (url.lastIndexOf(".") == -1) {
            return null;
        } else {
            String ext = url.substring(url.lastIndexOf(".") + 1);
            if (ext.contains("%")) {
                ext = ext.substring(0, ext.indexOf("%"));
            }
            if (ext.contains("/")) {
                ext = ext.substring(0, ext.indexOf("/"));
            }
            return ext.toLowerCase();

        }
    }

    /**
     * changes all elements in the list to a lowercase stirng
     * @param list list with strings
     * @return new list with only lowerCase strings
     */
    public static List<String> toLowerCase(List<String> list) {
        List<String> newList = new ArrayList<>();
        for(String string : list) {
            newList.add(string.toLowerCase());
        }

        return newList;
    }

    /**
     * This method converts dp unit to equivalent pixels, depending on device density.
     *
     * @param dp A value in dp (density independent pixels) unit. Which we need to convert into pixels
     * @return A float value to represent px equivalent to dp depending on device density
     */
    public static float convertDpToPixel(float dp){
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        float px = dp * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return px;
    }

    /**
     * This method converts device specific pixels to density independent pixels.
     *
     * @param px A value in px (pixels) unit. Which we need to convert into db
     * @return A float value to represent dp equivalent to px value
     */
    public static float convertPixelsToDp(float px){
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        float dp = px / ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return dp;
    }

}
