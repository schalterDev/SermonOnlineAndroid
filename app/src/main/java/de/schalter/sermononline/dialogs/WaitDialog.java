package de.schalter.sermononline.dialogs;

import android.content.Context;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import de.schalter.sermononline.R;
import de.schalter.sermononline.ResultActivity;
import de.schalter.sermononline.Utils;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

/**
 * Created by martin on 15.11.16.
 */

public class WaitDialog {

    private Context context;
    private AlertDialog wait;

    private TextView textViewMessage;

    public WaitDialog(Context context, String title) {
        this(context, title, "");
    }

    public WaitDialog(Context context, int titleResourceID) {
        this(context, context.getResources().getString(titleResourceID), "");
    }

    public WaitDialog(Context context, String title, String message) {
        this.context = context;

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);

        init(builder);
        updateMessage(message);

        wait = builder.create();
    }

    public WaitDialog(Context context, int titleResourceID, int messageResourceId) {
        this.context = context;

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(titleResourceID);

        init(builder);
        updateMessage(messageResourceId);

        wait = builder.create();
    }

    private void init(AlertDialog.Builder builder) {
        builder.setCancelable(false);

        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        View root = layoutInflater.inflate(R.layout.wait_dialog,null);

        textViewMessage = (TextView) root.findViewById(R.id.textView_waitInfo);

        builder.setView(root);
    }

    public void updateMessage(final String message) {
        textViewMessage.setText(message);
    }

    public void updateMessage(final int messageId) {
        textViewMessage.setText(messageId);
    }

    public void updateMessageOnMainThread(final String message) {
        Utils.runOnUiThread(context, new Runnable() {
            @Override
            public void run() {
                updateMessage(message);
            }
        });
    }

    public void updateMessageOnMainThread(final int messageId) {
        Utils.runOnUiThread(context, new Runnable() {
            @Override
            public void run() {
                updateMessage(messageId);
            }
        });
    }

    public void show() {
        wait.show();
    }

    public void showOnMainThread() {
        Utils.runOnUiThread(context, new Runnable() {
            @Override
            public void run() {
                show();
            }
        });
    }

    public void close() {
        wait.cancel();
    }

    public void closeOnMainThread() {
        Utils.runOnUiThread(context, new Runnable() {
            @Override
            public void run() {
                close();
            }
        });
    }
}