package de.schalter.sermononline.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import de.schalter.sermononline.MainActivity;
import de.schalter.sermononline.R;
import de.schalter.sermononline.ResultActivity;

import static android.provider.AlarmClock.EXTRA_MESSAGE;

/**
 * Fragment to input search on sermon-online.com
 * Created by martin on 21.11.17.
 */

public class SearchFragment extends Fragment {

    private static SearchFragment instance;

    private Spinner spinnerLanguage;

    public static SearchFragment newInstance() {
        if(instance == null)
            instance = new SearchFragment();

        return instance;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_search, container, false);

        final EditText editText = (EditText) rootView.findViewById(R.id.search_editText);
        spinnerLanguage = (Spinner) rootView.findViewById(R.id.spinner_language);

        Button searchButton = (Button) rootView.findViewById(R.id.search_btn);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int[] languages = getResources().getIntArray(R.array.languages_codes);
                int languageCode = languages[spinnerLanguage.getSelectedItemPosition()];
                search(editText.getText().toString(), languageCode);
            }
        });

        return rootView;
    }

    private void search(String searchText, int languageCode) {
        Intent intent = new Intent(this.getActivity(), ResultActivity.class);
        intent.putExtra(ResultActivity.SEARCHTEXT, searchText);
        intent.putExtra(ResultActivity.LANGUAGE, languageCode);
        startActivity(intent);
    }


}
