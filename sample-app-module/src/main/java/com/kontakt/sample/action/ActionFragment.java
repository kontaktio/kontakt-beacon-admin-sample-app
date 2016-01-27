package com.kontakt.sample.action;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;

import com.kontakt.sample.R;
import com.kontakt.sample.ui.fragment.BaseFragment;
import com.kontakt.sdk.android.ble.configuration.ActivityCheckConfiguration;
import com.kontakt.sdk.android.ble.configuration.ForceScanConfiguration;
import com.kontakt.sdk.android.ble.configuration.ScanPeriod;
import com.kontakt.sdk.android.ble.configuration.scan.IBeaconScanContext;
import com.kontakt.sdk.android.ble.configuration.scan.ScanContext;
import com.kontakt.sdk.android.ble.discovery.EventType;
import com.kontakt.sdk.android.ble.manager.ProximityManager;
import com.kontakt.sdk.android.ble.rssi.RssiCalculators;
import com.kontakt.sdk.android.common.model.IAction;
import com.kontakt.sdk.android.common.model.IBrowserAction;
import com.kontakt.sdk.android.common.model.IContentAction;
import com.kontakt.sdk.android.common.profile.RemoteBluetoothDevice;
import com.kontakt.sdk.android.http.HttpResult;
import com.kontakt.sdk.android.http.KontaktApiClient;
import com.kontakt.sdk.android.http.exception.ClientException;

import java.util.Arrays;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class ActionFragment extends BaseFragment {

    public static final String TAG = ActionFragment.class.getSimpleName();

    public static ActionFragment newInstance() {
        Bundle args = new Bundle();

        ActionFragment fragment = new ActionFragment();
        fragment.setArguments(args);
        return fragment;
    }

    protected IBeaconScanContext beaconScanContext = new IBeaconScanContext.Builder()
            .setEventTypes(Arrays.asList(EventType.DEVICE_DISCOVERED))
            .setRssiCalculator(RssiCalculators.newLimitedMeanRssiCalculator(5))
            .build();

    private RxProximityManager rxProximityManager;

    private CompositeSubscription compositeSubscription;

    private KontaktApiClient kontaktApiClient;

    @InjectView(R.id.action_device_unique_id)
    EditText deviceIdEditText;

    @InjectView(R.id.action_web)
    RadioButton browserActions;

    @Override
    public int getTitle() {
        return R.string.actions;
    }

    @Override
    public String getFragmentTag() {
        return TAG;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.action_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.inject(this, view);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        kontaktApiClient = new KontaktApiClient();
        rxProximityManager = new RxProximityManager(getContext());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        compositeSubscription = new CompositeSubscription();
    }

    @Override
    public void onStop() {
        super.onStop();
        compositeSubscription.clear();
    }

    @Override
    public void onStart() {
        super.onStart();

        ScanContext scanContext = getScanContext();

        Subscription subscription = rxProximityManager.scan(scanContext)
                .filter(new Func1<RxBeaconEvent, Boolean>() {
                    @Override
                    public Boolean call(RxBeaconEvent rxBeaconEvent) {
                        return filter(rxBeaconEvent);
                    }
                })
                .observeOn(Schedulers.newThread())
                .map(new Func1<RxBeaconEvent, List<IAction>>() {
                    @Override
                    public List<IAction> call(RxBeaconEvent rxBeaconEvent) {
                        return getActions(rxBeaconEvent);
                    }
                })
                .filter(new Func1<List<IAction>, Boolean>() {
                    @Override
                    public Boolean call(List<IAction> actionList) {
                        return actionList != null;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<IAction>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(List<IAction> actionList) {
                        displayActions(actionList);
                    }
                });

        compositeSubscription.add(subscription);
    }

    @Nullable
    private List<IAction> getActions(RxBeaconEvent rxBeaconEvent) {
        RemoteBluetoothDevice remoteBluetoothDevice = rxBeaconEvent.getBluetoothDeviceEvent().getDeviceList().get(0);
        try {
            HttpResult<List<IAction>> actionsForDevice = kontaktApiClient.getActionsForDevice(remoteBluetoothDevice.getUniqueId());
            if (actionsForDevice.isPresent()) {
                return actionsForDevice.get();
            } else {
                return null;
            }
        } catch (ClientException e) {
            return null;
        }
    }


    @NonNull
    private Boolean filter(RxBeaconEvent rxBeaconEvent) {
        if (!rxBeaconEvent.hasBluetoothDeviceEvent()) {
            return false;
        }
        List<? extends RemoteBluetoothDevice> deviceList = rxBeaconEvent.getBluetoothDeviceEvent().getDeviceList();
        if (deviceList.size() > 1) {
            return false;
        }
        String targetDeviceUniqueId = deviceIdEditText.getText().toString();
        if (TextUtils.isEmpty(targetDeviceUniqueId)) {
            return false;
        }
        RemoteBluetoothDevice remoteBluetoothDevice = deviceList.get(0);
        if (targetDeviceUniqueId.equals(remoteBluetoothDevice.getUniqueId())) {
            return true;
        }
        return false;
    }

    private ScanContext getScanContext() {
        return new ScanContext.Builder()
                .setScanMode(ProximityManager.SCAN_MODE_BALANCED)
                .setIBeaconScanContext(beaconScanContext)
                .setScanPeriod(new ScanPeriod(5000, 5000))
                .setActivityCheckConfiguration(ActivityCheckConfiguration.MINIMAL)
                .setForceScanConfiguration(ForceScanConfiguration.MINIMAL)
                .build();
    }

    private void displayActions(List<IAction> actionList) {
        IAction.ActionType targetType;
        if (browserActions.isChecked()) {
            targetType = IAction.ActionType.BROWSER;
        } else {
            targetType = IAction.ActionType.CONTENT;
        }
        IAction targetAction = null;
        for (IAction action : actionList) {
            if (action.getType() == targetType) {
                targetAction = action;
                break;
            }
        }

        String url = null;
        if (targetAction instanceof IContentAction) {
            url = ((IContentAction) targetAction).getContentUrl();
        }
        if (targetAction instanceof IBrowserAction) {
            url = ((IBrowserAction) targetAction).getUrl();
        }
        if (TextUtils.isEmpty(url)) {
            return;
        }
        deviceIdEditText.setText(null);

        ActionPopup.newInstance(url).show(getFragmentManager(), TAG);
    }

}
