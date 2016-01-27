package com.kontakt.sample.ui.dialog;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialog;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;

import com.kontakt.sdk.android.common.interfaces.SDKBiConsumer;
import com.kontakt.sdk.android.common.interfaces.SDKPredicate;

public class NumericInputDialogFragment extends InputDialogFragment {

    public static NumericInputDialogFragment newInstance(final String title,
                                                         final String message,
                                                         final String buttonText,
                                                         final SDKPredicate<Integer> valuePredicate,
                                                         final SDKBiConsumer submitBiConsumer) {
        NumericInputDialogFragment dialog = new NumericInputDialogFragment();
        Bundle args = new Bundle();
        args.putString(TITLE_TAG, title);
        args.putString(BUTTON_TEXT_TAG, buttonText);
        args.putString(MESSAGE_TAG, message);
        dialog.setArguments(args);
        dialog.biConsumer = submitBiConsumer;
        dialog.valuePredicate = valuePredicate;
        return dialog;
    }

    private SDKPredicate<Integer> valuePredicate;

    @Override
    public AppCompatDialog onCreateDialog(Bundle savedInstanceState) {
        final AppCompatDialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                getSubmitButton().setEnabled(false);
                inputText.setInputType(InputType.TYPE_CLASS_NUMBER);
                inputText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        try {
                            int value = Integer.parseInt(String.valueOf(s));
                            submitButton.setEnabled(valuePredicate.test(value));
                        } catch(NumberFormatException e) {
                            submitButton.setEnabled(false);
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
