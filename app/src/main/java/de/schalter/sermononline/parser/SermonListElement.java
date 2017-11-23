package de.schalter.sermononline.parser;

/**
 * storing all data important for displaying in listview
 * Created by martin on 21.11.17.
 */

import android.util.SparseArray;

import java.util.ArrayList;
import java.util.List;

public class SermonListElement {

    //same order as at the website
    public List<String> elementsText;

    //index is refering to elementsText
    public SparseArray<String> links;

    public SermonListElement() {
        elementsText = new ArrayList<>();
        links = new SparseArray<>();
    }

    public String toString() {
        return "Elements: " + elementsText + "\n";
    }
}