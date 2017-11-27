package de.schalter.sermononline.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import de.schalter.sermononline.DBHelper;
import de.schalter.sermononline.R;
import de.schalter.sermononline.Utils;
import de.schalter.sermononline.parser.SermonElement;
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

        List<SermonView> views = new ArrayList<>();
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
                List<SermonElement> downloads = dbHelper.getAllDownloads();
                final List<SermonView> views = new ArrayList<>();

                for(SermonElement download : downloads) {
                    SermonView downloadView = new SermonView(getActivity(), download.toSermonListElement());
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

    private class ListAdapter extends ArrayAdapter<SermonView> {

        ListAdapter(@NonNull Context context, List<SermonView> elements) {
            super(context, 0, elements);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = getItem(position);

            return convertView;
        }
    }

}
