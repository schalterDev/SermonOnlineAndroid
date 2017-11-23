package de.schalter.sermononline.views;

import de.schalter.sermononline.parser.SermonElement;

/**
 * Data for a download
 * Created by martin on 23.11.17.
 */

public class DownloadElement {

    public String path;
    public SermonElement sermonElement;

    public DownloadElement() {

    }

    public DownloadElement(String path, SermonElement sermonElement) {
        this.path = path;
        this.sermonElement = sermonElement;
    }

}
