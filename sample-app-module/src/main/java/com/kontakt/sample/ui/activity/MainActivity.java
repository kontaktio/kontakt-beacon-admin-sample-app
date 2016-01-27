package com.kontakt.sample.ui.activity;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.kontakt.sample.R;
import com.kontakt.sample.permission.PermissionChecker;
import com.kontakt.sample.permission.PermissionCheckerHoster;
import com.kontakt.sample.ui.fragment.BaseFragment;
import com.kontakt.sample.ui.fragment.DrawerFragmentFactory;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class MainActivity extends BaseActivity implements PermissionCheckerHoster{

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int REQUEST_CODE_ENABLE_BLUETOOTH = 121;

    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.drawer_layout)
    DrawerLayout drawerLayout;
    @InjectView(R.id.navigation_view)
    NavigationView navigationView;

    private ActionBarDrawerToggle actionBarDrawerToggle;

    private DrawerFragmentFactory drawerFragmentFactory;

    private PermissionChecker permissionChecker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        ButterKnife.inject(this);
        drawerFragmentFactory = new DrawerFragmentFactory();
        drawerFragmentFactory.onCreate();
        permissionChecker = new PermissionChecker();
        setUpActionBar(toolbar);
        setUpActionBarTitle(getString(R.string.app_name));
        setUpNavigationView();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        actionBarDrawerToggle.syncState();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return actionBarDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        actionBarDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        permissionChecker.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void setUpNavigationView() {
        actionBarDrawerToggle = new ActionBarDrawerToggle(this,
                drawerLayout,
                toolbar,
                R.string.drawer_open,
                R.string.drawer_close);

        drawerLayout.setDrawerListener(actionBarDrawerToggle);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                menuItem.setChecked(true);
                drawerLayout.closeDrawers();
                openFragment(menuItem.getItemId());
                return true;
            }
        });

        openFragment(drawerFragmentFactory.getLastFragmentOrDefault());
    }

    private void openFragment(@IdRes int menuItemId) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        BaseFragment fragment = drawerFragmentFactory.getFragmentByMenuItemId(menuItemId);

        if (fragment != null) {
            fragmentManager.beginTransaction()
                    .replace(R.id.content, fragment)
                    .commit();
            setUpActionBarTitle(fragment.getTitle());
        }
    }

    @Override
    public void requestPermission(PermissionChecker.Callback callback) {
        permissionChecker.requestLocationPermission(this, callback);
    }

}
