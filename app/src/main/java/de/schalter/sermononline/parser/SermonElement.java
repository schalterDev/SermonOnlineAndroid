package de.schalter.sermononline.parser;

import android.util.SparseArray;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Element of on sermon
 * Created by martin on 22.11.17.
 */

public class SermonElement implements Serializable {

    public List<String> headers;
    public List<String> data;

    //<index in headers (or data), link (url)>
    public HashMap<Integer, String> links;

    public SermonElement() {
        headers = new ArrayList<>();
        data = new ArrayList<>();
        links = new HashMap<>();
    }

    public String toString() {
        return "Elements: " + data + "\n";
    }

}
