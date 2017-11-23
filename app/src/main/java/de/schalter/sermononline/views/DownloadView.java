package de.schalter.sermononline.views;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import de.schalter.sermononline.R;
import de.schalter.sermononline.SermonActivity;

/**
 * View to be shown in the listview
 * Created by martin on 23.11.17.
 */

public class DownloadView extends RelativeLayout {

    private String url;
    private Activity activity;

    public DownloadView(Activity activity, DownloadElement downloadElement) {
        super(activity);
        this.activity = activity;

        LayoutInflater mInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View root = mInflater.inflate(R.layout.download_view, this, true);

        TextView textView = (TextView) root.findViewById(R.id.textView);
        textView.setText(downloadElement.path);
    }

    public void click() {
        Intent intent = new Intent(activity, SermonActivity.class);
        intent.putExtra(SermonActivity.URL, url);
        activity.startActivity(intent);
    }
}
