package de.schalter.sermononline.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import de.schalter.sermononline.DBHelper;
import de.schalter.sermononline.MainActivity;
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
    private ActionBar toolbar;
    private SwipeRefreshLayout swipeRefreshLayout;

    private static DownloadsFragment instance;

    private boolean selectionMode = false;
    //stores the indexes of the selected items
    private List<Integer> selected = new ArrayList<>();

    private Menu menu;
    private MenuInflater menuInflater;

    public static DownloadsFragment newInstance() {
        if(instance == null)
            instance = new DownloadsFragment();

        return instance;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        //inflater.inflate(R.menu.fragment_menu, menu);
        this.menu = menu;
        this.menuInflater = inflater;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {

            case R.id.action_delete:
                DBHelper dbHelper = DBHelper.getInstance(context);
                for(int position : selected) {
                    long downloadId = dbHelper.getDownloadId(adapter.getItem(position).getSermonId());
                    if(downloadId != -1) {
                        Utils.deleteDataFromDownloadManager(context, downloadId);
                    }

                    dbHelper.removeDownload(adapter.getItem(position).getSermonId());
                }

                ((MainActivity) getActivity()).snackbar(R.string.deleted);

                setSelectionMode(false);
                adapter.clear();
                loadDataAsync();

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        context = getContext();

        View rootView = inflater.inflate(R.layout.fragment_downloads, container, false);
        toolbar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.refresh_download);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                update();
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        ListView listView = (ListView) rootView.findViewById(R.id.list_downloads);

        List<SermonView> views = new ArrayList<>();
        adapter = new ListAdapter(context, views);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(!selectionMode) {
                    SermonView sermonView = (SermonView) view;
                    sermonView.clickOpenRessource((MainActivity) getActivity());
                } else {
                    if(selected.contains(position)) {
                        removeSelection((SermonView) view, position);
                    } else {
                        addSelection((SermonView) view, position);
                    }
                }
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                setSelectionMode(true);
                if(!selected.contains(position)) {
                    addSelection((SermonView) view, position);
                }
                return true;
            }
        });
        loadDataAsync();

        return rootView;
    }

    public void update() {
        if(adapter != null) {
            adapter.clear();
            adapter.notifyDataSetChanged();
            loadDataAsync();
        }
    }

    private void setSelectionMode(boolean selectionMode) {
        this.selectionMode = selectionMode;
        if(selectionMode) {
            menu.clear();
            menuInflater.inflate(R.menu.selection_downloads, menu);
        } else {
            getActivity().invalidateOptionsMenu();
            selected.clear();
        }
    }

    private void addSelection(SermonView view, int position) {
        view.setSelection(true);
        selected.add(position);
    }

    private void removeSelection(SermonView view, int position) {
        view.setSelection(false);
        selected.remove(Integer.valueOf(position));
        if(selected.size() == 0)
            setSelectionMode(false);
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
                    downloadView.setId(download.id);
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
