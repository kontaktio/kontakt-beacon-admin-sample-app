package com.kontakt.sample.action;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.kontakt.sample.R;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class ActionPopup extends AppCompatDialogFragment implements Dialog.OnClickListener {

    private static final String TAG = ActionPopup.class.getSimpleName();
    private static final String URL = "url";
    private String url;

    public static ActionPopup newInstance(String url) {

        Bundle args = new Bundle();
        args.putString(URL, url);
        ActionPopup fragment = new ActionPopup();
        fragment.setArguments(args);
        return fragment;
    }

    @InjectView(R.id.action_window)
    WebView webView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        url = getArguments().getString(URL);
    }

    @Override
    public AppCompatDialog onCreateDialog(Bundle savedInstanceState) {
        View view = View.inflate(getContext(), R.layout.action_popup, null);
        ButterKnife.inject(this, view);

        return new AlertDialog.Builder(getContext(), getTheme())
                .setPositiveButton(R.string.ok, this)
                .setNegativeButton(R.string.cancel, this)
                .setView(view)
                .create();
    }

    @Override
    public void setupDialog(Dialog dialog, int style) {
        super.setupDialog(dialog, style);
        ((AppCompatDialog) dialog).supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
    }


    @Override
    public void onClick(DialogInterface dialog, int which) {
        dialog.dismiss();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient());
        webView.loadUrl(url);
    }
}
