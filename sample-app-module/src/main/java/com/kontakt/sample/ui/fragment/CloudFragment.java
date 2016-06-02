package com.kontakt.sample.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import com.kontakt.sample.R;
import com.kontakt.sdk.android.cloud.IKontaktCloud;
import com.kontakt.sdk.android.cloud.KontaktCloud;
import com.kontakt.sdk.android.cloud.response.CloudCallback;
import com.kontakt.sdk.android.cloud.response.CloudError;
import com.kontakt.sdk.android.cloud.response.CloudHeaders;
import com.kontakt.sdk.android.cloud.response.paginated.Devices;
import com.kontakt.sdk.android.cloud.response.paginated.Presets;
import com.kontakt.sdk.android.common.model.Device;
import com.kontakt.sdk.android.common.model.Preset;

public class CloudFragment extends BaseFragment {

  public static final String TAG = CloudFragment.class.getSimpleName();

  @InjectView(R.id.fragment_cloud_top_Layout) ScrollView topLayout;
  @InjectView(R.id.fragment_cloud_label) TextView label;
  @InjectView(R.id.fragment_cloud_fetch_presets_button) Button fetchPresetsButton;
  @InjectView(R.id.fragment_cloud_fetch_devices_button) Button fetchDevicesButton;
  @InjectView(R.id.fragment_cloud_results_text) TextView results;
  @InjectView(R.id.fragment_cloud_progress) ProgressBar progressBar;

  private IKontaktCloud kontaktCloud;

  public static CloudFragment newInstance() {
    CloudFragment fragment = new CloudFragment();
    fragment.setHasOptionsMenu(false);
    return fragment;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.cloud_fragment, container, false);
    ButterKnife.inject(this, view);
    return view;
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    this.kontaktCloud = KontaktCloud.newInstance();
  }

  @Override
  public void onDestroyView() {
    ButterKnife.reset(this);
    super.onDestroyView();
  }

  @Override
  public int getTitle() {
    return R.string.cloud;
  }

  @Override
  public String getFragmentTag() {
    return TAG;
  }

  @OnClick(R.id.fragment_cloud_fetch_presets_button)
  public void onFetchPresetsButtonClicked() {
    showProgressBar(true);
    kontaktCloud.presets().fetch().execute(new CloudCallback<Presets>() {
      @Override
      public void onSuccess(Presets response, CloudHeaders headers) {
        showSuccessSnackbar();
        showProgressBar(false);
        results.setText("Presets: \n\n");
        for (Preset preset : response.getContent()) {
          results.append(String.format("Name: %s\nDescription: %s\n\n", preset.getName(), preset.getDescription()));
        }
      }

      @Override
      public void onError(CloudError error) {
        showErrorSnackbar();
        showProgressBar(false);
      }
    });
  }

  @OnClick(R.id.fragment_cloud_fetch_devices_button)
  public void onFetchDevicesButtonClicked() {
    showProgressBar(true);
    kontaktCloud.devices().fetch().execute(new CloudCallback<Devices>() {
      @Override
      public void onSuccess(Devices response, CloudHeaders headers) {
        showSuccessSnackbar();
        showProgressBar(false);
        results.setText("Devices: \n\n");
        for (Device device : response.getContent()) {
          results.append(String.format("Name: %s\nProximity: %s, Major: %d, Minor: %d\n\n", device.getConfig().getName(),
              device.getConfig().getProximity().toString(), device.getConfig().getMajor(), device.getConfig().getMinor()));
        }
      }

      @Override
      public void onError(CloudError error) {
        showErrorSnackbar();
        showProgressBar(false);
      }
    });
  }

  private void showProgressBar(boolean show) {
    progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
  }

  private void showSuccessSnackbar() {
    Snackbar.make(topLayout, "Success", Snackbar.LENGTH_SHORT).show();
  }

  private void showErrorSnackbar() {
    Snackbar.make(topLayout, "Failed to communicate with cloud", Snackbar.LENGTH_SHORT).show();
  }
}
