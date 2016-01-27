package com.kontakt.sample.ui.fragment;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;

import com.kontakt.sample.permission.PermissionCheckerHoster;

public abstract class BaseFragment extends Fragment {

    @StringRes
    public abstract int getTitle();

    public abstract String getFragmentTag();

    @Nullable
    protected PermissionCheckerHoster permissionCheckerHoster;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof PermissionCheckerHoster) {
            permissionCheckerHoster = (PermissionCheckerHoster) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        permissionCheckerHoster = null;
    }
}
