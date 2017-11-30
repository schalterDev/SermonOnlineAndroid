package de.schalter.sermononline.parser;

/**
 * Parses a search page from sermon-online.de
 * Created by martin on 21.11.17.
 */


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

import de.schalter.sermononline.objects.SermonListElement;

public class JsoupResultParser {

    private final int DATA_TABLE_COUNT = 2;
    private final int HEADERS_TD_COUNT = 1;
    private final int DATA_TDS_BEGINN = 2;

    private String html;

    private List<String> headers;
    private List<SermonListElement> content;

    /**
     * connects to the given url and downloads the html
     * @param urlString url as string
     * @throws IOException when the url is not an URL
     */
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

    /**
     * parse the downloaded html file
     * @throws NoDataFoundException when the downloaded html is empty (or not downloaded) or there are no results
     */
    public void parse() throws NoDataFoundException {
        if(html == null || html.equals(""))
            throw new NoDataFoundException();

        Document doc = Jsoup.parse(html);
        try {
            Element resultTable = doc.select("body > table").get(DATA_TABLE_COUNT);

            headers = parseHeaders(resultTable);
            content = parseSermonElements(resultTable);
        } catch (IndexOutOfBoundsException e) {
            throw new NoDataFoundException();
        }
    }

    /**
     * return headers of the result table
     * @return headers
     */
    public List<String> getHeaders() {
        return headers;
    }

    /**
     * return parses results
     * @return all results
     */
    public List<SermonListElement> getSermonElements() {
        return content;
    }

    private List<String> parseHeaders(Element table) {
        List<String> headers = new ArrayList<>();

        Element headerResultTable = table.select("tr").get(HEADERS_TD_COUNT);
        for (Element tdHeader : headerResultTable.select(">td")) {
            headers.add(tdHeader.text());
        }

        return headers;
    }

    private List<SermonListElement> parseSermonElements(Element table) {
        List<SermonListElement> sermonListElements = new ArrayList<>();

        //loop trough each row beginning from DATA_TDS_BEGINN (including)
        Elements rows = table.select("> tbody > tr");
        for(int rowCounter = DATA_TDS_BEGINN; rowCounter < rows.size(); rowCounter++) {
            Element row = rows.get(rowCounter);

            sermonListElements.add(parseSermonElement(row));
        }

        return sermonListElements;
    }

    private SermonListElement parseSermonElement(Element row) {
        SermonListElement sermonListElement = new SermonListElement();

        //loop trough each column
        Elements columns = row.select(">td");
        for(int columnCounter = 0; columnCounter < columns.size(); columnCounter++) {
            Element column = columns.get(columnCounter);
            sermonListElement.elementsText.add(column.text());

            //check if the text is a link and save it (only first link)
            Element linkElement = column.selectFirst("a[href]");
            if(linkElement != null) {
                sermonListElement.links.put(columnCounter, "http://sermon-online.com/" + linkElement.attr("href"));
            }
        }

        return sermonListElement;
    }

}

