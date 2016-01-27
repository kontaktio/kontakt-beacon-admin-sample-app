package com.kontakt.sample.ui.fragment;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kontakt.sample.R;
import com.kontakt.sample.permission.PermissionChecker;
import com.kontakt.sample.receiver.AbstractBroadcastInterceptor;
import com.kontakt.sample.receiver.AbstractScanBroadcastReceiver;
import com.kontakt.sample.receiver.ForegroundBroadcastInterceptor;
import com.kontakt.sample.service.BackgroundScanService;
import com.kontakt.sample.util.Utils;
import com.kontakt.sdk.android.common.log.Logger;
import com.kontakt.sdk.android.common.util.SDKPreconditions;

import butterknife.ButterKnife;

public class BackgroundScanFragment extends BaseFragment {

    public static final String TAG = BackgroundScanFragment.class.getSimpleName();


    public static final int MESSAGE_START_SCAN = 16;
    public static final int MESSAGE_STOP_SCAN = 25;

    private static final IntentFilter SCAN_INTENT_FILTER;

    static {
        SCAN_INTENT_FILTER = new IntentFilter(BackgroundScanService.BROADCAST);
        SCAN_INTENT_FILTER.setPriority(2);
    }


    public static BackgroundScanFragment newInstance() {

        Bundle args = new Bundle();

        BackgroundScanFragment fragment = new BackgroundScanFragment();
        fragment.setArguments(args);
        return fragment;
    }


    private final BroadcastReceiver scanReceiver = new ForegrondScanReceiver();

    private ServiceConnection serviceConnection;

    private Messenger serviceMessenger;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.background_scan_activity, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        bindServiceAndStartMonitoring();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    @Override
    public int getTitle() {
        return R.string.foreground_background_scan;
    }

    @Override
    public String getFragmentTag() {
        return TAG;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unbindService(serviceConnection);
    }

    @Override
    public void onResume() {
        super.onResume();
        Utils.cancelNotifications(getContext(), BackgroundScanService.INFO_LIST);
        getActivity().registerReceiver(scanReceiver, SCAN_INTENT_FILTER);
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(scanReceiver);
    }

    private void bindServiceAndStartMonitoring() {
        serviceConnection = createServiceConnection();
        final Intent intent = new Intent(getContext(), BackgroundScanService.class);
        getActivity().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private ServiceConnection createServiceConnection() {
        return new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                serviceMessenger = new Messenger(service);

                startScan();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };
    }

    private void startScan() {
        permissionCheckerHoster.requestPermission(new PermissionChecker.Callback() {
            @Override
            public void onPermisionGranted() {
                sendMessage(Message.obtain(null, MESSAGE_START_SCAN));
            }

            @Override
            public void onPermissionRejected() {
                Snackbar.make(getView(), R.string.permission_rejected_message, Snackbar.LENGTH_SHORT).show();
            }
        });

    }

    private void sendMessage(final Message message) {
        SDKPreconditions.checkNotNull(serviceMessenger, "ServiceMessenger is null.");
        SDKPreconditions.checkNotNull(message, "Message is null");

        try {
            serviceMessenger.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
            Logger.d(": message not sent(" + message.toString() + ")");
        }
    }

    private static class ForegrondScanReceiver extends AbstractScanBroadcastReceiver {

        @Override
        protected AbstractBroadcastInterceptor createBroadcastHandler(Context context) {
            return new ForegroundBroadcastInterceptor(context);
        }
    }
}
