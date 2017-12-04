package de.schalter.sermononline.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashMap;

/**
 * Created by martin on 30.11.17.
 */

public class JsoupStartPageParser extends JsoupParser {

    private static final String AUTHOR = "author";
    private static final String LANGUAGE = "language";
    private static final String CATEGORY = "category";
    private static final String MEDIATYPE = "mediatype";

    private HashMap<String, Integer> authors;
    private HashMap<String, Integer> languages;
    private HashMap<String, Integer> categorys;
    private HashMap<String, Integer> mediatypes;

    @Override
    public void parse() throws NoDataFoundException {
        if(html == null || html.equals(""))
            throw new NoDataFoundException();

        Document doc = Jsoup.parse(html);

        authors = parseName(doc, AUTHOR);
        languages = parseName(doc, LANGUAGE);
        categorys = parseName(doc, CATEGORY);
        mediatypes = parseName(doc, MEDIATYPE);
    }

    private HashMap<String, Integer> parseName(Document doc, String name) throws NoDataFoundException {
        HashMap<String, Integer> elements = new HashMap<>();
        Element element = doc.selectFirst("select[name=" + name + "]");

        if(element == null)
            throw new NoDataFoundException();

        Elements valueElements = element.select("option");
        for(Element valueElement : valueElements) {
            String key = valueElement.text();
            Integer code = Integer.valueOf(valueElement.attr("value"));

            elements.put(key, code);
        }

        return elements;
    }

    public HashMap<String, Integer> getAuthors() {
        return authors;
    }

    public HashMap<String, Integer> getLanguages() {
        return languages;
    }

    public HashMap<String, Integer> getCategorys() {
        return categorys;
    }

    public HashMap<String, Integer> getMediatypes() {
        return mediatypes;
    }
}
