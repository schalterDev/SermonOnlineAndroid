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

import de.schalter.sermononline.MainActivity;
import de.schalter.sermononline.R;
import de.schalter.sermononline.ResultActivity;

import static android.provider.AlarmClock.EXTRA_MESSAGE;

/**
 * Created by martin on 21.11.17.
 */

public class SearchFragment extends Fragment {

    public SearchFragment() {

    }

    private static SearchFragment instance;

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

        Button searchButton = (Button) rootView.findViewById(R.id.search_btn);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                search(editText.getText().toString());
            }
        });

        return rootView;
    }

    private void search(String text) {
        String searchEncoded = Uri.encode(text);
        String url = "http://sermon-online.com/search.pl?lang=de&id=0&start=1&searchstring=" + searchEncoded + "&author=0&language=0&category=0&mediatype=0&order=12&count=25&x=0&y=0";

        Intent intent = new Intent(this.getActivity(), ResultActivity.class);
        intent.putExtra(ResultActivity.URL, url);
        intent.putExtra(ResultActivity.SEARCH, text);
        startActivity(intent);

    }

}
