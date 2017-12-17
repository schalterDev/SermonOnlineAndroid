package de.schalter.sermononline.objects;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.support.v4.content.FileProvider;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.net.URI;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import de.schalter.sermononline.DBHelper;
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
    private List<String> headers;

    //data of the table of the page
    private List<String> data;

    //<index in headers (or data), link (url)>
    private HashMap<Integer, String> links;

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

    public SermonElement(List<String> headers, List<String> data) {
        if(headers != null)
            this.headers = headers;
        else
            this.headers = new ArrayList<>();

        if(data != null)
            this.data = data;
        else
            this.data = new ArrayList<>();

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
     * @param url url of the sermon page
     */
    public static void startSermonActivity(Context context, String url) {
        Intent intent = new Intent(context, SermonActivity.class);
        intent.putExtra(SermonActivity.URL, url);
        context.startActivity(intent);
    }

    /**
     * Open activity to show notes / write notes
     * @param context
     */
    public void openNoteActivity(Context context) {
        Intent intent = new Intent(context, OpenSermonActivity.class);
        intent.putExtra(OpenSermonActivity.SERMON_ID, id);
        context.startActivity(intent);
    }

    /**
     * Open the sermon with an installed app
     * @param activity activity
     * @throws FileNotFoundException file does not exist
     * @throws ActivityNotFoundException could not load SermonElement from Database
     */
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

    public String getDataForHeaders(String[] headersString) {
        int index = Utils.indexOf(headers, headersString);
        if(index == -1) {
            //not found
            return null;
        } else {
            return data.get(index);
            //Data: data.get(index)
            //Link: links.get(index)
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

    public void addData(String toAdd) {
        data.add(toAdd);
    }

    public void addHeader(String toAdd) {
        headers.add(toAdd);
    }

    public void addLink(int index, String link) {
        links.put(index, link);
    }

    /**
     * @param linkContaining only add the link when the link contains the string
     * @return Hasmap containing description (as key) an link (as data)<description, link>
     */
    public HashMap<String, String> getLinksWithDescription(String linkContaining) {
        HashMap<String, String> linksReturn = new HashMap<>(); //<description, link>

        for(int indexData : links.keySet()) {
            String link = links.get(indexData);
            if(link.contains(linkContaining)) {
                linksReturn.put(data.get(indexData), link);
            }
        }

        return linksReturn;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getTitle() {
        return getDataForHeaders(TITLES);
    }

    public String getAuthor() {
        return getDataForHeaders(AUTHORS);
    }

    public String getPassage() {
        return getDataForHeaders(PASSAGES);
    }

    public String getLanguage() {
        return getDataForHeaders(LANGUAGES);
    }

    public String getCategory() {
        return getDataForHeaders(CATEGORIES);
    }

    public Date getDate() {
        String dateString = getDataForHeaders(DATES);

        if(dateString != null) {
            //date can have format dd.mm.yyy or yyyy
            try {
                if(dateString.contains(".")) {
                    DateFormat df = new SimpleDateFormat("dd.mm.yyyy");
                    return df.parse(dateString);
                } else {
                    DateFormat df = new SimpleDateFormat("yyyy");
                    return df.parse(dateString);
                }
            } catch(ParseException e) {
                return null;
            }
        } else {
            return null;
        }
    }

    public String getDateAsString() {
        return getDataForHeaders(DATES);
    }

    /**
     * Get duration in seconds
     * @return duration in seconds (0 if media does not have a duration)
     */
    public int getDuration() {
        String durationString = getDataForHeaders(DURATIONS);

        //Duration is splitted by :
        //hours:minutes:seconds (01:05:33)

        if(durationString != null) {
            int seconds = 0;
            int minutes = 0;
            int hours = 0;

            String[] durations = durationString.split(":");

            try {
                switch (durations.length) {
                    case 1:
                        seconds = Integer.parseInt(durations[0]);
                        break;
                    case 2:
                        seconds = Integer.parseInt(durations[1]);
                        minutes = Integer.parseInt(durations[0]);
                        break;
                    case 3:
                        seconds = Integer.parseInt(durations[2]);
                        minutes = Integer.parseInt(durations[1]);
                        hours = Integer.parseInt(durations[0]);
                        break;
                }
            } catch(NumberFormatException e) {
                return 0;
            }

            return seconds + minutes * 60 + hours * 60 * 60;
        } else {
            return 0;
        }
    }

    public String getDurationAsString() {
        return getDataForHeaders(DURATIONS);
    }

    public int getPages() {
        String pagesString = getDataForHeaders(PAGES);
        if(pagesString != null) {
            try {
                return Integer.parseInt(pagesString);
            } catch(NumberFormatException e) {
                return 0;
            }
        } else {
            return 0;
        }
    }

    public List<String> getHeaders() {
        return headers;
    }

    public List<String> getData() {
        return data;
    }
}
