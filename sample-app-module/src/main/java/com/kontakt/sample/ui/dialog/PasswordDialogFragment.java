package com.kontakt.sample.ui.dialog;

import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialog;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;

import com.kontakt.sdk.android.common.interfaces.SDKBiConsumer;

public class PasswordDialogFragment extends InputDialogFragment {

    private static final int KONTAKT_BEACON_PASSWORD_LENGTH = 4;

    public static PasswordDialogFragment newInstance(final String title,
                                                  final String message,
                                                  final String buttonText,
                                                  final SDKBiConsumer<DialogInterface, String> submitBiConsumer) {
        return newInstance(title, message, buttonText, null, submitBiConsumer);
    }

    public static PasswordDialogFragment newInstance(final String title,
                                                  final String message,
                                                  final String buttonText,
                                                  final Drawable icon,
                                                  final SDKBiConsumer<DialogInterface, String> submitBiConsumer) {
        PasswordDialogFragment dialog = new PasswordDialogFragment();
        Bundle args = new Bundle();
        args.putString(TITLE_TAG, title);
        args.putString(BUTTON_TEXT_TAG, buttonText);
        args.putString(MESSAGE_TAG, message);
        dialog.setArguments(args);
        dialog.icon = icon;
        dialog.biConsumer = submitBiConsumer;
        return dialog;
    }

    @Override
    public AppCompatDialog onCreateDialog(Bundle savedInstanceState) {
        final AppCompatDialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                getSubmitButton().setEnabled(false);
                inputText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                inputText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        submitButton.setEnabled(s.length() == KONTAKT_BEACON_PASSWORD_LENGTH);
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });
            }
        });

        return dialog;
    }
}
