package com.kontakt.sample.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;

import com.kontakt.sdk.core.interfaces.BiConsumer;

public class PasswordDialogFragment extends InputDialogFragment {

    public static PasswordDialogFragment newInstance(final String title,
                                                  final String message,
                                                  final String buttonText,
                                                  final BiConsumer<DialogInterface, String> submitBiConsumer) {
        return newInstance(title, message, buttonText, null, submitBiConsumer);
    }

    public static PasswordDialogFragment newInstance(final String title,
                                                  final String message,
                                                  final String buttonText,
                                                  final Drawable icon,
                                                  final BiConsumer submitBiConsumer) {
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
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Dialog dialog = super.onCreateDialog(savedInstanceState);
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
                        if (s.length() != 4) {
                            submitButton.setEnabled(false);
                        } else {
                            submitButton.setEnabled(true);
                        }
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
