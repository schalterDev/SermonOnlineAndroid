package de.schalter.sermononline.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import de.schalter.sermononline.R;
import de.schalter.sermononline.Utils;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

/**
 * An Dialog which shows an error
 * Created by martin on 22.11.17.
 */

public class ErrorDialog {

    private Context context;
    private AlertDialog error;
    private AlertDialog.Builder builder;

    private TextView textViewMessage;

    public ErrorDialog(Context context, String title) {
        this(context, title, "");
    }

    public ErrorDialog(Context context, int titleResourceID) {
        this(context, context.getResources().getString(titleResourceID), "");
    }

    public ErrorDialog(Context context, String title, String message) {
        this.context = context;

        builder = new AlertDialog.Builder(context);
        builder.setTitle(title);

        init(builder);
        updateMessage(message);
    }

    public ErrorDialog(Context context, int titleResourceID, int messageResourceId) {
        this.context = context;

        builder = new AlertDialog.Builder(context);
        builder.setTitle(titleResourceID);

        init(builder);
        updateMessage(messageResourceId);
    }

    private void init(AlertDialog.Builder builder) {
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                close();
            }
        });

        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        View root = layoutInflater.inflate(R.layout.error_dialog,null);

        textViewMessage = (TextView) root.findViewById(R.id.textView_errorInfo);

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
        error = builder.create();
        error.show();
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
        error.cancel();
    }

    public void closeOnMainThread() {
        Utils.runOnUiThread(context, new Runnable() {
            @Override
            public void run() {
                close();
            }
        });
    }

    /**
     * Set the onClickListener for the positive button
     * @param clickListener clickListener
     * @return the ErrorDialog
     */
    public ErrorDialog setOnClickListener(DialogInterface.OnClickListener clickListener) {
        builder.setPositiveButton(R.string.okay, clickListener);
        return this;
    }
}
