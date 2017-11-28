package de.schalter.sermononline.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import de.schalter.sermononline.objects.SermonElement;

/**
 * Parse a single sermon page
 * Created by martin on 22.11.17.
 */

public class JsoupSermonParser {

    private final int DATA_TABLE_COUNT = 2;

    private String html;

    private SermonElement sermonElement;

    public JsoupSermonParser() {
        sermonElement = new SermonElement();
    }

    /**
     * Connect to the given url and download the html
     * @param urlString url as String
     * @throws IOException when the url is not an URL
     */
    public void connect(String urlString) throws IOException {
        sermonElement.sermonUrlPage = urlString;

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
        html = "";
        while( (bytesRead = bis.read(contents)) != -1){
            html += new String(contents, 0, bytesRead);
        }
    }

    /**
     * parse the downloaded html file
     * @throws NoDataFoundException when the downloaded html is empty (or not downloaded) or there are no results
     */
    public void parse() throws NoDataFoundException {
        Document doc = Jsoup.parse(html);

        try {

            Element tempTable = doc.select("body > table").get(DATA_TABLE_COUNT);
            Element resultTable = tempTable.select("> tbody table").get(0);

            parseData(resultTable);
        } catch (IndexOutOfBoundsException e) {
            throw new NoDataFoundException();
        }
    }

    private void parseData(Element table) {

        Elements rows = table.select("> tbody > tr");
        for(Element row : rows) {
            Elements columns = row.select(">td");
            for(int i = 0; i < columns.size(); i++) {
                if(i == 0 && columns.size() == 2) {
                    sermonElement.headers.add(columns.get(i).text());
                } else if(i == 1) {
                    sermonElement.data.add(getDataFromString(columns.get(i).text()));
                    checkForLinks(sermonElement.data.size() - 1, columns.get(i));
                } else if (i == 0 && columns.size() == 1) {
                    sermonElement.data.add(getDataFromString(columns.get(i).text()));
                    sermonElement.headers.add(sermonElement.headers.get(sermonElement.headers.size() - 1));
                    checkForLinks(sermonElement.data.size() - 1, columns.get(i));
                }
            }
        }
    }

    private final static String[] toRemove = {"Download mit rechter Maustaste & Ziel speichern unter (Internet Explorer) oder Link speichern unter... (Google Chrome)",
            "Right click here & Save target as (IE) or Save link... (Google Chrome)"};

    /**
     * Removes all unnecessari explanations
     * @param string to shorten
     * @return shorten string
     */
    private String getDataFromString(String string) {
        for(String remove : toRemove) {
            string = string.replace(remove, "");
        }

        return string;
    }

    /**
     * Chekcs for a link in the given element.
     * When a link is found it will be added to the sermonElement at the given index
     * @param index column in the table
     * @param element tablerow element
     */
    private void checkForLinks(int index, Element element) {
        Element linkElement = element.selectFirst("a[href]");
        if(linkElement != null) {
            sermonElement.links.put(index, linkElement.attr("href"));
        }
    }

    public SermonElement getSermonElement() {
        return sermonElement;
    }
}
