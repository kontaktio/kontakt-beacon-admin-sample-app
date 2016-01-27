package com.kontakt.sample.ui.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.kontakt.sample.R;

public class AlertDialogFragment extends DialogFragment {

    private static final String MESSAGE_TAG = "message of dialog";
    private static final String TITLE_TAG = "title of a dialog";

    private Drawable icon;

    private DialogInterface.OnClickListener onClickListener;

    public static AlertDialogFragment newInstance(final String title,
                                                  final String message,
                                                  final Drawable icon,
                                                  final DialogInterface.OnClickListener onClickListener) {
        AlertDialogFragment dialog = new AlertDialogFragment();
        Bundle args = new Bundle();
        args.putString(MESSAGE_TAG, message);
        args.putString(TITLE_TAG, title);
        dialog.icon = icon;
        dialog.onClickListener = onClickListener;
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Bundle arguments  = getArguments();
        return new AlertDialog.Builder(getActivity())
                              .setMessage(arguments.getString(MESSAGE_TAG))
                              .setTitle(arguments.getString(TITLE_TAG))
                              .setIcon(icon)
                              .setNeutralButton(getString(R.string.ok), onClickListener)
                              .create();
    }
}
