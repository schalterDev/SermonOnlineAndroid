package de.schalter.sermononline.objects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.schalter.sermononline.Utils;

/**
 * Element of a sermon
 * Created by martin on 22.11.17.
 */

public class SermonElement implements Serializable {

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

}
