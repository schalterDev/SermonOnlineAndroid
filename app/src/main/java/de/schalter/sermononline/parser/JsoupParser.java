package de.schalter.sermononline.parser;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by martin on 30.11.17.
 */

public abstract class JsoupParser {

    protected String html;
    protected String url;

    /**
     * Connect to the given url and download the html
     * @param urlString url as String
     * @throws IOException when the url is not an URL
     */
    public void connect(String urlString) throws IOException {
        this.url = urlString;

        URL url = new URL(urlString);
        HttpURLConnection ucon = (HttpURLConnection) url.openConnection();

        ucon.setRequestProperty("User-Agent", "Mozilla/5.0...");

        InputStream is = ucon.getInputStream();
        BufferedInputStream bis = new BufferedInputStream(is);

        /*
         * Read bytes to the Buffer until there is nothing more to read(-1).
         */
        byte[] contents = new byte[1024];

        int bytesRead;
        StringBuilder stringBuilder = new StringBuilder();
        while( (bytesRead = bis.read(contents)) != -1){
            stringBuilder.append(new String(contents, 0, bytesRead));
        }

        html = stringBuilder.toString();
    }

    /**
     * parse the downloaded html file
     * @throws NoDataFoundException when the downloaded html is empty (or not downloaded) or there are no results
     */
    public abstract void parse() throws NoDataFoundException;

}
