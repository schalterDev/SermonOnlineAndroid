package de.schalter.sermononline.views;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.content.FileProvider;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;

import de.schalter.sermononline.DBHelper;
import de.schalter.sermononline.MainActivity;
import de.schalter.sermononline.OpenSermonActivity;
import de.schalter.sermononline.R;
import de.schalter.sermononline.SermonActivity;
import de.schalter.sermononline.dialogs.SermonNotFoundDialog;
import de.schalter.sermononline.objects.SermonListElement;

/**
 * View to be shown in the Sermon listview
 * Created by martin on 21.11.17.
 */

public class SermonView extends RelativeLayout {

    private String url;
    private Context context;
    private int id;

    private TextView title;

    public SermonView(Context context, SermonListElement sermonListElement) {
        super(context);
        this.context = context;

        LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mInflater.inflate(R.layout.sermon_view, this , true);

        TextView author = (TextView) findViewById(R.id.sermon_author);
        title = (TextView) findViewById(R.id.sermon_title);
        TextView category = (TextView) findViewById(R.id.sermon_categorie);
        TextView bible = (TextView) findViewById(R.id.sermon_bible);
        TextView date = (TextView) findViewById(R.id.sermon_date);
        TextView duration = (TextView) findViewById(R.id.sermon_duration);

        title.setText(sermonListElement.elementsText.get(SermonListElement.TITLE));
        author.setText(sermonListElement.elementsText.get(SermonListElement.AUTHOR));
        bible.setText(sermonListElement.elementsText.get(SermonListElement.PASSAGE));
        category.setText(sermonListElement.elementsText.get(SermonListElement.CATEGORY));
        date.setText(sermonListElement.elementsText.get(SermonListElement.DATE));
        duration.setText(sermonListElement.elementsText.get(SermonListElement.DURATION));

        url = sermonListElement.links.get(0);
    }

    public void addToTitle(String string) {
        title.setText(title.getText() + string);
    }

    public void setSelection(boolean selected) {
        if(selected)
            this.setBackgroundColor(Color.LTGRAY);
        else
            this.setBackgroundColor(Color.WHITE);
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSermonId() {
        return id;
    }

    public String getUrl() {
        return url;
    }
}
