package de.schalter.sermononline.parser;

/**
 * Created by martin on 21.11.17.
 */

import android.util.SparseArray;

import java.util.ArrayList;
import java.util.List;

public class SermonListElement {

    public List<String> elementsText;
    public SparseArray<String> links;

    public SermonListElement() {
        elementsText = new ArrayList<>();
        links = new SparseArray<>();
    }

    public String toString() {
        return "Elements: " + elementsText + "\n";
    }
}