package de.schalter.sermononline.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.schalter.sermononline.DBHelper;
import de.schalter.sermononline.R;
import de.schalter.sermononline.Utils;
import de.schalter.sermononline.views.DownloadElement;
import de.schalter.sermononline.views.DownloadView;
import de.schalter.sermononline.views.SermonView;

/**
 * Fragment to show all downloads stored in the database
 * Created by martin on 21.11.17.
 */

public class DownloadsFragment extends Fragment {

    private ListAdapter adapter;
    private Context context;

    private static DownloadsFragment instance;

    public static DownloadsFragment newInstance() {
        if(instance == null)
            instance = new DownloadsFragment();

        return instance;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        context = getContext();

        View rootView = inflater.inflate(R.layout.fragment_downloads, container, false);

        ListView listView = (ListView) rootView.findViewById(R.id.list_downloads);

        List<DownloadView> views = new ArrayList<>();
        adapter = new ListAdapter(context, views);
        listView.setAdapter(adapter);

        loadDataAsync();

        return rootView;
    }

    /**
     * Loads the downloads async from the database
     */
    private void loadDataAsync() {
        Thread background = new Thread(new Runnable() {
            @Override
            public void run() {
            DBHelper dbHelper = DBHelper.getInstance(context);
                List<DownloadElement> downloads = dbHelper.getAllDownloads();
                final List<DownloadView> views = new ArrayList<>();

                for(DownloadElement download : downloads) {
                    DownloadView downloadView = new DownloadView(getActivity(), download);
                    views.add(downloadView);
                }

                Utils.runOnUiThread(context, new Runnable() {
                    @Override
                    public void run() {
                        adapter.addAll(views);
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        });
        background.start();
    }

    private class ListAdapter extends ArrayAdapter<DownloadView> {

        ListAdapter(@NonNull Context context, List<DownloadView> elements) {
            super(context, 0, elements);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = getItem(position);

            return convertView;
        }
    }

}
