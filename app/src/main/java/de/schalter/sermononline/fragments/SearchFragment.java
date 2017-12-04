package de.schalter.sermononline.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import de.schalter.sermononline.MainActivity;
import de.schalter.sermononline.R;
import de.schalter.sermononline.ResultActivity;
import de.schalter.sermononline.Utils;
import de.schalter.sermononline.parser.JsoupStartPageParser;
import de.schalter.sermononline.parser.NoDataFoundException;
import de.schalter.sermononline.settings.Settings;

/**
 * Fragment to input search on sermon-online.com
 * Created by martin on 21.11.17.
 */

public class SearchFragment extends Fragment {

    private static SearchFragment instance;

    private HashMap<String, Integer> languages;
    private HashMap<String, Integer> categories;
    private HashMap<String, Integer> authors;

    private List<String> sortedLanguages;
    private List<String> sortedCategories;
    private List<String> sortedAuthors;

    private Spinner spinnerLanguage;
    private Spinner spinnerCategory;
    private Spinner spinnerAuthor;

    private boolean downloadFinished;

    public static SearchFragment newInstance() {
        if(instance == null)
            instance = new SearchFragment();

        return instance;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_search, container, false);

        downloadFinished = false;

        final EditText editText = rootView.findViewById(R.id.search_editText);
        spinnerLanguage = rootView.findViewById(R.id.spinner_language);
        spinnerCategory = rootView.findViewById(R.id.spinner_category);
        spinnerAuthor = rootView.findViewById(R.id.spinner_author);

        setupSpinners();

        Button searchButton = rootView.findViewById(R.id.search_btn);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(downloadFinished) {
                    search(editText.getText().toString(), getLanguageCode(), getCategroyCode(),
                            getAuthorCode());
                }
            }
        });

        return rootView;
    }

    /**
     * Loads the website and setups spinners
     */
    private void setupSpinners() {
        snackbar(R.string.connecting);

        Thread background = new Thread(new Runnable() {
            @Override
            public void run() {
                String searchUrl = "http://sermon-online.com/search.pl?" +
                        "lang=" + Settings.getSystemLanguageCode() +
                        "&extended=1";

                try {
                    JsoupStartPageParser parser = new JsoupStartPageParser();
                    parser.connect(searchUrl);

                    snackBarOnUI(R.string.parsing);
                    parser.parse();

                    languages = parser.getLanguages();
                    categories = parser.getCategorys();
                    authors = parser.getAuthors();

                    sortedLanguages = setupSpinner(languages, spinnerLanguage);
                    sortedCategories = setupSpinner(categories, spinnerCategory);
                    sortedAuthors = setupSpinner(authors, spinnerAuthor);

                    downloadFinished = true;

                    snackBarOnUI(R.string.finished);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (NoDataFoundException e) {
                    e.printStackTrace();
                }
            }
        }); background.start();
    }

    public void snackbar(int message) {
        ((MainActivity) getActivity()).snackbar(message);
    }

    public void snackBarOnUI(final int mesasge) {
        final MainActivity activity = (MainActivity) getActivity();
        Utils.runOnUiThread(activity, new Runnable() {
            @Override
            public void run() {
                activity.snackbar(mesasge);
            }
        });
    }

    private String[] allStrings = {"all authors", "all languages", "all categories",
        "all media types", "alle Autoren", "alle Sprachen", "alle Kategorien",
        "alle Medientypen"};

    private List<String> setupSpinner(HashMap<String, Integer> map, final Spinner spinner) {
        List<String> sortedList = new ArrayList<>(map.keySet());
        Collections.sort(sortedList);

        for(String replace : allStrings) {
            if(sortedList.contains(replace)) {
                sortedList.remove(replace);
                sortedList.add(0, replace);
            }
        }

        final ArrayAdapter<String> adapter = new ArrayAdapter<>(
                getContext(), android.R.layout.simple_spinner_item, sortedList);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        Utils.runOnUiThread(getContext(), new Runnable() {
            @Override
            public void run() {
                spinner.setAdapter(adapter);
            }
        });

        return sortedList;
    }

    private int getLanguageCode() {
        int position = spinnerLanguage.getSelectedItemPosition();
        String language = sortedLanguages.get(position);

        return languages.get(language);
    }

    private int getCategroyCode() {
        int position = spinnerCategory.getSelectedItemPosition();
        String category = sortedCategories.get(position);

        return categories.get(category);
    }

    private int getAuthorCode() {
        int position = spinnerAuthor.getSelectedItemPosition();
        String category = sortedAuthors.get(position);

        return authors.get(category);
    }

    private void search(String searchText, int languageCode, int categoryCode, int authorCode) {
        Intent intent = new Intent(this.getActivity(), ResultActivity.class);
        intent.putExtra(ResultActivity.SEARCHTEXT, searchText);
        intent.putExtra(ResultActivity.LANGUAGE, languageCode);
        intent.putExtra(ResultActivity.CATEGORY, categoryCode);
        intent.putExtra(ResultActivity.AUTHOR, authorCode);
        startActivity(intent);
    }


}
