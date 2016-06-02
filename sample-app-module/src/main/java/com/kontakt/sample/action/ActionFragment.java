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
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.kontakt.sample.R;
import com.kontakt.sample.ui.fragment.BaseFragment;
import com.kontakt.sdk.android.cloud.IKontaktCloud;
import com.kontakt.sdk.android.cloud.KontaktCloud;
import com.kontakt.sdk.android.cloud.exception.KontaktCloudException;
import com.kontakt.sdk.android.cloud.response.paginated.Actions;
import com.kontakt.sdk.android.common.model.Action;
import com.kontakt.sdk.android.common.profile.RemoteBluetoothDevice;
import java.io.IOException;
import java.util.List;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class ActionFragment extends BaseFragment {

  public static final String TAG = ActionFragment.class.getSimpleName();

  @InjectView(R.id.action_device_unique_id) EditText deviceIdEditText;
  @InjectView(R.id.action_web) RadioButton browserActions;

  private RxProximityManager rxProximityManager;
  private CompositeSubscription compositeSubscription;
  private IKontaktCloud kontaktCloud;

  public static ActionFragment newInstance() {
    Bundle args = new Bundle();

    ActionFragment fragment = new ActionFragment();
    fragment.setArguments(args);
    return fragment;
  }

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
    ButterKnife.reset(this);
    super.onDestroyView();
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    kontaktCloud = KontaktCloud.newInstance();
    rxProximityManager = new RxProximityManager(getContext());
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    compositeSubscription = new CompositeSubscription();
  }

  @Override
  public void onStop() {
    compositeSubscription.clear();
    super.onStop();
  }

  @Override
  public void onStart() {
    super.onStart();

    Subscription subscription = rxProximityManager.scan().filter(new Func1<RxBeaconEvent, Boolean>() {
      @Override
      public Boolean call(RxBeaconEvent rxBeaconEvent) {
        return filter(rxBeaconEvent);
      }
    }).observeOn(Schedulers.newThread()).map(new Func1<RxBeaconEvent, List<Action>>() {
      @Override
      public List<Action> call(RxBeaconEvent rxBeaconEvent) {
        return getActions(rxBeaconEvent);
      }
    }).filter(new Func1<List<Action>, Boolean>() {
      @Override
      public Boolean call(List<Action> actionList) {
        return actionList != null;
      }
    }).observeOn(AndroidSchedulers.mainThread()).subscribe(new Subscriber<List<Action>>() {
      @Override
      public void onCompleted() {

      }

      @Override
      public void onError(Throwable e) {
        e.printStackTrace();
      }

      @Override
      public void onNext(List<Action> actionList) {
        displayActions(actionList);
      }
    });

    compositeSubscription.add(subscription);
  }

  @Nullable
  private List<Action> getActions(RxBeaconEvent rxBeaconEvent) {
    String uniqueId = rxBeaconEvent.getDevice().getUniqueId();
    try {
      Actions actions = kontaktCloud.actions().fetch().forDevices(uniqueId).execute();
      return actions.getContent();
    } catch (IOException | KontaktCloudException e) {
      return null;
    }
  }

  @NonNull
  private Boolean filter(RxBeaconEvent rxBeaconEvent) {
    if (!rxBeaconEvent.hasDevice()) {
      return false;
    }
    RemoteBluetoothDevice device = rxBeaconEvent.getDevice();
    if (device == null) {
      return false;
    }
    String targetDeviceUniqueId = deviceIdEditText.getText().toString();
    return !TextUtils.isEmpty(targetDeviceUniqueId) && targetDeviceUniqueId.equals(device.getUniqueId());
  }

  private void displayActions(List<Action> actionList) {
    Action.Type targetType;
    if (browserActions.isChecked()) {
      targetType = Action.Type.BROWSER;
    } else {
      targetType = Action.Type.CONTENT;
    }
    Action targetAction = null;
    for (Action action : actionList) {
      if (action.getType() == targetType) {
        targetAction = action;
        break;
      }
    }

    if (targetAction == null) {
      return;
    }

    String url = null;
    if (targetAction.getType() == Action.Type.CONTENT) {
      url = targetAction.getContent().getContentUrl();
    }
    if (targetAction.getType() == Action.Type.BROWSER) {
      url = targetAction.getUrl();
    }
    if (TextUtils.isEmpty(url)) {
      return;
    }

    deviceIdEditText.setText(null);
    ActionPopup.newInstance(url).show(getFragmentManager(), TAG);
  }
}
