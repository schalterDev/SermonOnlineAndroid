package de.schalter.sermononline.parser;

import android.util.SparseArray;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by martin on 22.11.17.
 */

public class JsoupSermonParser {

    private final int DATA_TABLE_COUNT = 2;

    private String html;

    private SermonElement sermonElement;

    public JsoupSermonParser() {
        sermonElement = new SermonElement();
    }

    public void connect(String urlString) throws IOException {
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

    private String getDataFromString(String string) {
        int indexOfBracket = string.indexOf(")");
        if(indexOfBracket == -1) {
            return string;
        } else {
            return string.substring(0, indexOfBracket + 1);
        }
    }

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
