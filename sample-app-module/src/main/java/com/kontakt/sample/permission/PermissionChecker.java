package com.kontakt.sample.permission;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.kontakt.sample.R;

import java.lang.ref.WeakReference;

public class PermissionChecker {

    private static final String TAG = PermissionChecker.class.getSimpleName();

    private static final int ACCESS_LOCATION_REQUEST_CODE = 1;
    private static final String ACCESS_COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;


    public PermissionChecker() {

    }

    public interface Callback {
        void onPermisionGranted();

        void onPermissionRejected();
    }


    private WeakReference<Callback> callback;
    private WeakReference<FragmentActivity> fragmentActivity;

    public void requestLocationPermission(FragmentActivity activity, Callback callback) {
        this.callback = new WeakReference<Callback>(callback);
        this.fragmentActivity = new WeakReference<FragmentActivity>(activity);

        int checkSelfPermission = ContextCompat.checkSelfPermission(activity, ACCESS_COARSE_LOCATION);

        if (PackageManager.PERMISSION_GRANTED != checkSelfPermission) {
            Log.d(TAG, "Requesting permission");
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, ACCESS_COARSE_LOCATION)) {
                showExplanationDialog();
            } else {
                requestPermission();
            }
        } else {
            callback.onPermisionGranted();
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (ACCESS_LOCATION_REQUEST_CODE != requestCode) {
            return;
        }
        if (callback.get() == null) {
            return;
        }
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            callback.get().onPermisionGranted();
        } else {
            callback.get().onPermissionRejected();
        }
    }

    private void showExplanationDialog() {
        if (fragmentActivity.get() == null) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(fragmentActivity.get());
        builder.setTitle(R.string.permission_request_title)
                .setMessage(R.string.permission_request_message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        requestPermission();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (callback.get() != null) {
                            callback.get().onPermissionRejected();
                        }
                    }
                })
                .show();
    }

    private void requestPermission() {
        if (fragmentActivity.get() == null) {
            return;
        }
        ActivityCompat.requestPermissions(fragmentActivity.get(),
                new String[]{ACCESS_COARSE_LOCATION},
                ACCESS_LOCATION_REQUEST_CODE);
    }
}
