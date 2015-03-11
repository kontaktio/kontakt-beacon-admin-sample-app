package com.kontakt.sample.ui.activity;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.kontakt.sample.adapter.ProfilesAdapter;
import com.kontakt.sdk.android.http.KontaktApiClient;
import com.kontakt.sdk.android.model.Profile;
import com.kontakt.sdk.core.exception.ClientException;
import com.kontakt.sdk.core.http.HttpResult;

import org.apache.http.HttpStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

public class ProfilesActivity extends ListActivity {

    public static final String EXTRA_PROFILE = "extra_profile";

    private ProfilesAdapter profilesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        profilesAdapter = new ProfilesAdapter(this, new ArrayList<Profile>());
        setListAdapter(profilesAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        final Profile profile = profilesAdapter.getItem(position);
        final Intent resultIntent = new Intent();
        resultIntent.putExtra(EXTRA_PROFILE, profile);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    private interface ProfilesListener {
        void onProfilesDelivered(final Set<Profile> profiles);
        void onProfilesAbsent();
    }
}
