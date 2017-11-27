package de.schalter.sermononline.parser;

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

    public String sermonUrlPage;

    public List<String> headers;
    public List<String> data;

    //<index in headers (or data), link (url)>
    public HashMap<Integer, String> links;

    SermonElement() {
        headers = new ArrayList<>();
        data = new ArrayList<>();
        links = new HashMap<>();
    }

    /**
     * converts a sermonElement to a sermonListElement
     * @return new sermonListElement
     */
    public SermonListElement toSermonListElement() {
        SermonListElement sermonListElement = new SermonListElement();
        for(int i = 0; i < SermonListElement.SIZE; i++) {
            switch (i) {
                case SermonListElement.TITLE:
                    addToSermonElement(sermonListElement, TITLES);
                    sermonListElement.links.put(i, sermonUrlPage);
                    break;
                case SermonListElement.AUTHOR:
                    addToSermonElement(sermonListElement, AUTHORS);
                    break;
                case SermonListElement.PASSAGE:
                    addToSermonElement(sermonListElement, PASSAGES);
                    break;
                case SermonListElement.LANGUAGE:
                    addToSermonElement(sermonListElement, LANGUAGES);
                    break;
                case SermonListElement.CATEGORY:
                    addToSermonElement(sermonListElement, CATEGORIES);
                    break;
                case SermonListElement.DATE:
                    addToSermonElement(sermonListElement, DATES);
                    break;
                case SermonListElement.DURATION:
                    addToSermonElement(sermonListElement, DURATIONS);
                    break;
                case SermonListElement.PAGES:
                    addToSermonElement(sermonListElement, PAGES);
                    break;
            }
        }

        return sermonListElement;
    }

    private void addToSermonElement(SermonListElement sermonListElement, String[] headerStrings) {
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
