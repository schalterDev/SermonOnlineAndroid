package de.schalter.sermononline.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
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

    public static String BASE_URL = "https://www.sermon-online.com";

    private static SearchFragment instance;

    private HashMap<String, Integer> languages;
    private HashMap<String, Integer> categories;
    private HashMap<String, Integer> authors;

    private List<String> sortedLanguages;
    private List<String> sortedCategories;
    private List<String> sortedAuthors;

    private EditText editText;
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

        editText = rootView.findViewById(R.id.search_editText);

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String text = editable.toString();
                if(text.contains(System.getProperty("line.separator"))) {
                    text = text.replace(System.getProperty("line.separator"), "");
                    editText.setText(text);
                    if(downloadFinished)
                        search();
                }
            }
        });

        spinnerLanguage = rootView.findViewById(R.id.spinner_language);
        spinnerCategory = rootView.findViewById(R.id.spinner_category);
        spinnerAuthor = rootView.findViewById(R.id.spinner_author);

        setupSpinners();

        Button searchButton = rootView.findViewById(R.id.search_btn);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(downloadFinished) {
                    search();
                }
            }
        });

        return rootView;
    }

    /**
     * Loads the website and setups spinners
     */
    private void setupSpinners() {
        snackbar(R.string.connecting, Snackbar.LENGTH_INDEFINITE);

        Thread background = new Thread(new Runnable() {
            @Override
            public void run() {
                downloadFinished = false;
                String searchUrl = BASE_URL + "/search.pl?" +
                        "lang=" + Settings.getSystemLanguageCode() +
                        "&extended=1";

                try {
                    JsoupStartPageParser parser = new JsoupStartPageParser();
                    parser.connect(searchUrl);

                    snackBarOnUI(R.string.parsing, Snackbar.LENGTH_INDEFINITE);
                    parser.parse();

                    languages = parser.getLanguages();
                    categories = parser.getCategorys();
                    authors = parser.getAuthors();

                    sortedLanguages = setupSpinner(languages, spinnerLanguage);
                    sortedCategories = setupSpinner(categories, spinnerCategory);
                    sortedAuthors = setupSpinner(authors, spinnerAuthor);

                    downloadFinished = true;

                    snackBarOnUI(R.string.finished, Snackbar.LENGTH_SHORT);
                } catch (IOException e) {
                    e.printStackTrace();
                    snackBarOnUIWithAction(R.string.network_error, Snackbar.LENGTH_INDEFINITE,
                            R.string.retry, new Runnable() {
                                @Override
                                public void run() {
                                    Utils.runOnUiThread(getContext(), new Runnable() {
                                        @Override
                                        public void run() {
                                            setupSpinners();
                                        }
                                    });
                                }
                            });
                } catch (NoDataFoundException e) {
                    e.printStackTrace();
                    snackBarOnUIWithAction(R.string.data_error, Snackbar.LENGTH_INDEFINITE,
                            R.string.retry, new Runnable() {
                                @Override
                                public void run() {
                                    Utils.runOnUiThread(getContext(), new Runnable() {
                                        @Override
                                        public void run() {
                                            setupSpinners();
                                        }
                                    });
                                }
                            });
                }
            }
        }); background.start();
    }

    private void snackbar(int message, int duration) {
        ((MainActivity) getActivity()).snackbar(message, duration);
    }

    private void snackBarOnUI(final int message, final int duration) {
        final MainActivity activity = (MainActivity) getActivity();
        Utils.runOnUiThread(activity, new Runnable() {
            @Override
            public void run() {
                activity.snackbar(message, duration);
            }
        });
    }

    private void snackBarOnUIWithAction(final int message, final int duration, final int messageAction, final Runnable action) {
        final MainActivity activity = (MainActivity) getActivity();
        Utils.runOnUiThread(activity, new Runnable() {
            @Override
            public void run() {
                activity.snackbarWithAction(message, duration, messageAction, action);
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

    private void search() {
        String searchText = editText.getText().toString();
        int languageCode = getLanguageCode();
        int categoryCode = getCategroyCode();
        int authorCode = getAuthorCode();

        Intent intent = new Intent(this.getActivity(), ResultActivity.class);
        intent.putExtra(ResultActivity.SEARCHTEXT, searchText);
        intent.putExtra(ResultActivity.LANGUAGE, languageCode);
        intent.putExtra(ResultActivity.CATEGORY, categoryCode);
        intent.putExtra(ResultActivity.AUTHOR, authorCode);
        startActivity(intent);
    }


}
