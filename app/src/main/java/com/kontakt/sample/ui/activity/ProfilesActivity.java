package com.kontakt.sample.ui.activity;

import android.app.ListActivity;
import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.kontakt.sample.adapter.ProfilesAdapter;
import com.kontakt.sample.loader.ProfilesLoader;
import com.kontakt.sdk.android.http.KontaktApiClient;
import com.kontakt.sdk.android.model.Profile;
import com.kontakt.sdk.core.exception.ClientException;
import com.kontakt.sdk.core.http.HttpResult;

import org.apache.http.HttpStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class ProfilesActivity extends ListActivity implements LoaderManager.LoaderCallbacks<List<Profile>> {

    public static final String EXTRA_PROFILE = "extra_profile";

    private ProfilesAdapter profilesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        profilesAdapter = new ProfilesAdapter(this, new ArrayList<Profile>());
        setListAdapter(profilesAdapter);
        getLoaderManager().initLoader(0, null, this);
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

    @Override
    public Loader<List<Profile>> onCreateLoader(int id, Bundle args) {
        return new ProfilesLoader(this);
    }

    @Override
    public void onLoadFinished(Loader<List<Profile>> loader, List<Profile> data) {
        profilesAdapter.replaceWith(data);
    }

    @Override
    public void onLoaderReset(Loader<List<Profile>> loader) {
        profilesAdapter.replaceWith(Collections.<Profile>emptyList());
    }
}
