package de.schalter.sermononline.views;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.webkit.MimeTypeMap;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;

import de.schalter.sermononline.DBHelper;
import de.schalter.sermononline.MainActivity;
import de.schalter.sermononline.R;
import de.schalter.sermononline.SermonActivity;
import de.schalter.sermononline.Utils;
import de.schalter.sermononline.dialogs.SermonNotFoundDialog;
import de.schalter.sermononline.objects.SermonListElement;

/**
 * View to be shown in the Sermon listview
 * Created by martin on 21.11.17.
 */

public class SermonView extends RelativeLayout {

    private String url;
    private Activity activity;
    private int id;

    private TextView title;

    public SermonView(Activity activity, SermonListElement sermonListElement) {
        super(activity);
        this.activity = activity;

        LayoutInflater mInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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

    /**
     * starts a new SermonActivity
     */
    public void clickStartActivity() {
        Intent intent = new Intent(activity, SermonActivity.class);
        intent.putExtra(SermonActivity.URL, url);
        activity.startActivity(intent);
    }

    public void clickOpenRessource(MainActivity activity) throws FileNotFoundException, ActivityNotFoundException {
        DBHelper dbHelper = DBHelper.getInstance(getContext());

        /*
        long downloadId = dbHelper.getDownloadId(id);
        Utils.openWithDownloadManager(activity, downloadId);
        */
        String path = dbHelper.getRessourcePath(id);

        //Check if path exists
        if (path == null || !(new File(URI.create(path)).exists())) {
            //activity.snackbar(R.string.fileNotExists);
            SermonNotFoundDialog sermonNotFoundDialog = new SermonNotFoundDialog(activity, id);
            sermonNotFoundDialog.show();
        } else {
            MimeTypeMap myMime = MimeTypeMap.getSingleton();
            Intent newIntent = new Intent(Intent.ACTION_VIEW);
            String mimeType = myMime.getMimeTypeFromExtension(Utils.getFileExtension(path));
            newIntent.setDataAndType(Uri.parse(path), mimeType);
            newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getContext().startActivity(newIntent);
        }

    }

}
