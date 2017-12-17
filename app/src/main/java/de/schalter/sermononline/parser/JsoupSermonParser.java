package de.schalter.sermononline.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import de.schalter.sermononline.objects.SermonElement;

/**
 * Parse a single sermon page
 * Created by martin on 22.11.17.
 */

public class JsoupSermonParser extends JsoupParser {

    private final int DATA_TABLE_COUNT = 2;

    private SermonElement sermonElement;

    public JsoupSermonParser() {
        sermonElement = new SermonElement();
    }

    @Override
    public void parse() throws NoDataFoundException {
        if(html == null || html.equals(""))
            throw new NoDataFoundException();

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
        sermonElement.sermonUrlPage = url;

        int counterData = 0;
        String lastHeader = "";

        Elements rows = table.select("> tbody > tr");
        for(Element row : rows) {
            Elements columns = row.select(">td");
            for(int i = 0; i < columns.size(); i++) {
                if(i == 0 && columns.size() == 2) { //new header
                    lastHeader = columns.get(i).text().toLowerCase().replace(":", "");
                    sermonElement.addHeader(lastHeader);
                } else if(i == 1) {
                    sermonElement.addData(getDataFromString(columns.get(i).text()));
                    checkForLinks(counterData, columns.get(i));
                    counterData++;
                } else if (i == 0 && columns.size() == 1) { //same header as before
                    sermonElement.addData(getDataFromString(columns.get(i).text()));
                    sermonElement.addHeader(lastHeader);
                    checkForLinks(counterData, columns.get(i));
                    counterData++;
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
     * Checks for a link in the given element.
     * When a link is found it will be added to the sermonElement at the given index
     * @param index column in the table
     * @param element tablerow element
     */
    private void checkForLinks(int index, Element element) {
        Element linkElement = element.selectFirst("a[href]");
        if(linkElement != null) {
            sermonElement.addLink(index, linkElement.attr("href"));
        }
    }

    public SermonElement getSermonElement() {
        return sermonElement;
    }
}
