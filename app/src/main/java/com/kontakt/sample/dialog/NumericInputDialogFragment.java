package com.kontakt.sample.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;

import com.kontakt.sample.util.Utils;
import com.kontakt.sdk.core.interfaces.BiConsumer;
import com.kontakt.sdk.core.interfaces.Predicate;

public class NumericInputDialogFragment extends InputDialogFragment {

    public static NumericInputDialogFragment newInstance(final String title,
                                                         final String message,
                                                         final String buttonText,
                                                         final Predicate<Integer> valuePredicate,
                                                         final BiConsumer submitBiConsumer) {
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

    private Predicate<Integer> valuePredicate;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Dialog dialog = super.onCreateDialog(savedInstanceState);
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
                            Utils.showToast(getActivity(), "Incorrect value typed");
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
    };
}
