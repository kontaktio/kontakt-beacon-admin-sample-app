package com.kontakt.sample.ui.dialog;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialog;
import android.support.v7.app.AppCompatDialogFragment;

import com.kontakt.sample.R;

public class ChoiceDialogFragment extends AppCompatDialogFragment {

    private static final String MESSAGE_TAG = "message of dialog";
    private static final String TITLE_TAG = "title of a dialog";

    private static final DialogInterface.OnClickListener defaultNegativeOnClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
        }
    };

    private DialogInterface.OnClickListener positiveOnClickListener;

    private DialogInterface.OnClickListener negativeOnClickListener;

    public static ChoiceDialogFragment newInstance(final String title,
                                                   final String message,
                                                   final DialogInterface.OnClickListener positiveOnClickListener) {
        return newInstance(title,
                           message,
                           positiveOnClickListener,
                           defaultNegativeOnClickListener);
    }

    public static ChoiceDialogFragment newInstance(final String title,
                                            final String message,
                                            final DialogInterface.OnClickListener positiveOnClickListener,
                                            final DialogInterface.OnClickListener negativeOnClickListener) {
        ChoiceDialogFragment dialog = new ChoiceDialogFragment();
        Bundle args = new Bundle();
        args.putString(MESSAGE_TAG, message);
        args.putString(TITLE_TAG, title);
        dialog.setArguments(args);
        dialog.positiveOnClickListener = positiveOnClickListener;
        dialog.negativeOnClickListener = negativeOnClickListener;
        return dialog;
    }

    @Override
    public AppCompatDialog onCreateDialog(Bundle savedInstanceState) {
        final Bundle arguments  = getArguments();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        return builder.setIcon(android.R.drawable.ic_dialog_alert)
                .setMessage(arguments.getString(MESSAGE_TAG))
                .setTitle(arguments.getString(TITLE_TAG))
                .setPositiveButton(getString(R.string.yes), positiveOnClickListener)
                .setNegativeButton(getString(R.string.no), negativeOnClickListener)
                .create();
    }
}
