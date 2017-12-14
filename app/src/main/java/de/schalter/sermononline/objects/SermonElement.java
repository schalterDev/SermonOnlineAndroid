package de.schalter.sermononline.objects;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.FileProvider;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.schalter.sermononline.DBHelper;
import de.schalter.sermononline.MainActivity;
import de.schalter.sermononline.OpenSermonActivity;
import de.schalter.sermononline.SermonActivity;
import de.schalter.sermononline.Utils;

/**
 * Element of a sermon
 * Created by martin on 22.11.17.
 */

public class SermonElement implements Serializable {

    private static final long serialVersionUID = 146862L;

    //possible names of the columns at sermon-online.com
    private static final String[] TITLES = {"title", "titel"};
    private static final String[] AUTHORS = {"author", "autor"};
    private static final String[] PASSAGES = {"passage", "bibelstelle"};
    private static final String[] LANGUAGES = {"sprache", "language"};
    private static final String[] CATEGORIES = {"category", "kategorie"};
    private static final String[] DATES = {"date", "datum"};
    private static final String[] DURATIONS = {"duration", "dauer"};
    private static final String[] PAGES = {"pages", "seiten"};

    //url of the page of the sermon
    public String sermonUrlPage;

    //headers of the table of the page
    public List<String> headers;

    //data of the table of the page
    public List<String> data;

    //<index in headers (or data), link (url)>
    public HashMap<Integer, String> links;

    //id in the database
    public int id = -1;

    private long timeLastOpened;
    private int lastAudioPosition;
    private String notes;

    public SermonElement() {
        headers = new ArrayList<>();
        data = new ArrayList<>();
        links = new HashMap<>();
    }

    /**
     * converts a sermonElement to a sermonListElement
     * @return new sermonListElement
     */
    public SermonListElement toSermonListElement() {
        headers = Utils.toLowerCase(this.headers);
        for(int i = 0; i < headers.size(); i++) {
            headers.set(i, headers.get(i).replaceAll(":", ""));
        }

        SermonListElement sermonListElement = new SermonListElement();
        for(int i = 0; i < SermonListElement.SIZE; i++) {
            switch (i) {
                case SermonListElement.TITLE:
                    addToSermonListElement(sermonListElement, TITLES);
                    sermonListElement.links.put(i, sermonUrlPage);
                    break;
                case SermonListElement.AUTHOR:
                    addToSermonListElement(sermonListElement, AUTHORS);
                    break;
                case SermonListElement.PASSAGE:
                    addToSermonListElement(sermonListElement, PASSAGES);
                    break;
                case SermonListElement.LANGUAGE:
                    addToSermonListElement(sermonListElement, LANGUAGES);
                    break;
                case SermonListElement.CATEGORY:
                    addToSermonListElement(sermonListElement, CATEGORIES);
                    break;
                case SermonListElement.DATE:
                    addToSermonListElement(sermonListElement, DATES);
                    break;
                case SermonListElement.DURATION:
                    addToSermonListElement(sermonListElement, DURATIONS);
                    break;
                case SermonListElement.PAGES:
                    addToSermonListElement(sermonListElement, PAGES);
                    break;
            }
        }

        return sermonListElement;
    }

    /**
     * Searches for the data given in headerStrings and if the data is stored in this element it will
     * be added to the sermonListView
     * @param sermonListElement sermonListElement to add data
     * @param headerStrings search for the column given in the array (multilanguage)
     */
    private void addToSermonListElement(SermonListElement sermonListElement, String[] headerStrings) {
        int index = Utils.indexOf(headers, headerStrings);
        if(index == -1) {
            sermonListElement.elementsText.add("");
        } else {
            sermonListElement.elementsText.add(data.get(index));
            String url = links.get(index);
            if(url != null)
                sermonListElement.links.put(index, url);
        }
    }

    public String toString() {
        return "Elements: " + data + "\n";
    }

    /**
     * starts a new SermonActivity
     */
    public static void startSermonActivity(Context context, String url) {
        Intent intent = new Intent(context, SermonActivity.class);
        intent.putExtra(SermonActivity.URL, url);
        context.startActivity(intent);
    }

    public void openNoteActivity(Context context) {
        Intent intent = new Intent(context, OpenSermonActivity.class);
        intent.putExtra(OpenSermonActivity.SERMON_ID, id);
        context.startActivity(intent);
    }

    public void openRessource(Activity activity) throws FileNotFoundException, ActivityNotFoundException {
        DBHelper dbHelper = DBHelper.getInstance(activity);

        /*
        long downloadId = dbHelper.getDownloadId(id);
        Utils.openWithDownloadManager(context, downloadId);
        */
        String path = dbHelper.getRessourcePath(id);

        //Check if path exists
        if (path == null || !(new File(URI.create(path)).exists())) {
            throw new FileNotFoundException("File: " + path + ", not found");
        } else {
            Intent newIntent = new Intent(Intent.ACTION_VIEW,
                    FileProvider.getUriForFile(activity, activity.getApplicationContext().getPackageName() + ".de.schalter.sermononline",
                            new File(URI.create(path))));
            newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            newIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            activity.startActivity(newIntent);
        }

    }

    public long getTimeLastOpened() {
        return timeLastOpened;
    }

    public void setTimeLastOpened(long timeLastOpened) {
        this.timeLastOpened = timeLastOpened;
    }

    public int getLastAudioPosition() {
        return lastAudioPosition;
    }

    public void setLastAudioPosition(int lastAudioPosition) {
        this.lastAudioPosition = lastAudioPosition;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
