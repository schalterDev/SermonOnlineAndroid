package de.schalter.sermononline.views;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

import de.schalter.sermononline.R;
import de.schalter.sermononline.SermonActivity;
import de.schalter.sermononline.parser.SermonListElement;

/**
 * View to be shown in the Sermon listview
 * Created by martin on 21.11.17.
 */

public class SermonView extends RelativeLayout {

    private String url;
    private Activity activity;

    public SermonView(Activity activity, SermonListElement sermonListElement) {
        super(activity);
        this.activity = activity;

        LayoutInflater mInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mInflater.inflate(R.layout.sermon_view, this , true);

        TextView author = (TextView) findViewById(R.id.sermon_author);
        TextView title = (TextView) findViewById(R.id.sermon_title);
        TextView category = (TextView) findViewById(R.id.sermon_categorie);
        TextView bible = (TextView) findViewById(R.id.sermon_bible);
        TextView date = (TextView) findViewById(R.id.sermon_date);
        TextView duration = (TextView) findViewById(R.id.sermon_duration);

        title.setText(sermonListElement.elementsText.get(0));
        author.setText(sermonListElement.elementsText.get(1));
        bible.setText(sermonListElement.elementsText.get(2));
        category.setText(sermonListElement.elementsText.get(4));
        date.setText(sermonListElement.elementsText.get(5));
        duration.setText(sermonListElement.elementsText.get(6));

        url = sermonListElement.links.get(0);
    }

    /**
     * starts a new SermonActivity
     */
    public void click() {
        Intent intent = new Intent(activity, SermonActivity.class);
        intent.putExtra(SermonActivity.URL, url);
        activity.startActivity(intent);
    }

}
