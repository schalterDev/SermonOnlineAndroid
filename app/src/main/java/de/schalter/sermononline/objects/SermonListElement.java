package de.schalter.sermononline.objects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * storing all data important for displaying in listview
 * Created by martin on 21.11.17.
 */
public class SermonListElement {

    //order is title, author, passage, language, category, date/time, duration, pages
    public static final int TITLE = 0;
    public static final int AUTHOR = 1;
    public static final int PASSAGE = 2;
    public static final int LANGUAGE = 3;
    public static final int CATEGORY = 4;
    public static final int DATE = 5;
    public static final int DURATION = 6;
    public static final int PAGES = 7;
    public static final int SIZE = 8;

    //same order as at the website
    public List<String> elementsText;

    //index is refering to elementsText
    public HashMap<Integer, String> links;

    public SermonListElement() {
        elementsText = new ArrayList<>();
        links = new HashMap<>();
    }

    public String toString() {
        return "Elements: " + elementsText + "\n";
    }
}