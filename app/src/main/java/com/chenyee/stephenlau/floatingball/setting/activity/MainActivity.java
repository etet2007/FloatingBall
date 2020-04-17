package com.chenyee.stephenlau.floatingball.setting.activity;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.didikee.donate.AlipayDonate;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.artitk.licensefragment.ScrollViewLicenseFragment;
import com.artitk.licensefragment.model.License;
import com.artitk.licensefragment.model.LicenseType;
import com.chenyee.stephenlau.floatingball.R;
import com.chenyee.stephenlau.floatingball.floatingBall.service.FloatingBallService;
import com.chenyee.stephenlau.floatingball.commonReceiver.LockRequestReceiver;
import com.chenyee.stephenlau.floatingball.repository.BallSettingRepo;
import com.chenyee.stephenlau.floatingball.setting.fragment.SettingFragment;
import com.chenyee.stephenlau.floatingball.util.ActivityUtils;

import java.util.ArrayList;
import butterknife.BindView;
import butterknife.ButterKnife;

import static com.chenyee.stephenlau.floatingball.quickSetting.QuickSettingService.QUICK_SETTING_REFRESH_MAIN_ACTIVITY_ACTION;
import static com.chenyee.stephenlau.floatingball.util.StaticStringUtil.EXTRAS_COMMAND;

public class MainActivity extends AppCompatActivity
        implements AppBarLayout.OnOffsetChangedListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    //App Icon
    private static final int PERCENTAGE_TO_ANIMATE_AVATAR = 40;
    //Widgets
    @BindView(R.id.floating_action_bt_logo) FloatingActionButton mFloatingActionButton;
    @BindView(R.id.start_switch) SwitchCompat ballSwitch;
    @BindView(R.id.iv_logo) ImageView logoImageView;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.drawer_layout) DrawerLayout drawer;

    private boolean isAvatarShown = true;
    private int maxScrollSize;

    private RefreshReceiver refreshReceiver;

    public static Intent getStartIntent(Context context) {
        return new Intent(context, MainActivity.class);
    }

    public boolean isBallSwitchIsChecked() {
        return ballSwitch.isChecked();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //ButterKnife
        ButterKnife.bind(this);

        initViews();

        FragmentManager fragmentManager = getFragmentManager();
        SettingFragment settingFragment = SettingFragment.newInstance();
        ActivityUtils.addFragmentToActivity(fragmentManager, settingFragment, R.id.contentFrame);

        //QuickSettingService refresh MainActivity
        refreshReceiver = new RefreshReceiver(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(QUICK_SETTING_REFRESH_MAIN_ACTIVITY_ACTION);
        LocalBroadcastManager.getInstance(MainActivity.this).registerReceiver(refreshReceiver, intentFilter);
    }

    private void initViews() {
        // Set up the toolbar. 工具栏。
        setupToolbar();

        // Set up the appBarLayout.
        AppBarLayout appbarLayout = findViewById(R.id.appbar_layout);
        appbarLayout.addOnOffsetChangedListener(this);
        maxScrollSize = appbarLayout.getTotalScrollRange();

        // Set up the FloatingActionButton.
        mFloatingActionButton.setUseCompatPadding(false);
        mFloatingActionButton.setOnClickListener(v -> ballSwitch.toggle());

        // Set up the ballSwitch.
        ballSwitch.setOnCheckedChangeListener((buttonView, isBallSwitchChecked) -> {
            if (isBallSwitchChecked) {
                sendAddFloatBallIntent();
                Snackbar.make(buttonView, R.string.add_ball_hint, Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show();
            } else {
                sendRemoveFloatBallIntent();
                Snackbar.make(buttonView, R.string.remove_ball_hint, Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show();
            }
            refreshActivityViews(isBallSwitchChecked);

            refreshSettingFragmentViews();
        });

        // Set up the ActionBarDrawerToggle. 工具栏抽屉的开关。
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // Set up the navigation drawer.导航抽屉。
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(
                item -> {
                    int id = item.getItemId();
                    if (id == R.id.nav_license) {
                        addLicenseFragment();
                    } else if (id == R.id.setting) {
                        FragmentManager fragmentManager = getFragmentManager();
                        SettingFragment settingFragment = SettingFragment.newInstance();
                        ActivityUtils.addFragmentToActivity(fragmentManager, settingFragment,
                                R.id.contentFrame);
                    } else if (id == R.id.donate) {
                        boolean hasInstalledAlipayClient = AlipayDonate.hasInstalledAlipayClient(MainActivity.this);
                        if (hasInstalledAlipayClient) {
                            AlipayDonate.startAlipayClient(MainActivity.this, "fkx01795bkxkfkuk5rfh79d");
                        } else {
                            Toast.makeText(getApplicationContext(), R.string.noAliPay, Toast.LENGTH_SHORT)
                                    .show();
                        }
                    } else if (id == R.id.uninstall) {
                        uninstall();
                    }

                    drawer.closeDrawer(GravityCompat.START);

                    //true to display the item as the selected item and false if the item should not be selected.
                    return true;
                });

        // Update versionTextView
        try {
            PackageInfo pi = getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = pi.versionName;
            TextView versionTextView = navigationView.getHeaderView(0)
                    .findViewById(R.id.version_textView);
            versionTextView.setText(String.format(getString(R.string.version_textView), version));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);//顺序会有影响

        ActionBar ab = getSupportActionBar();
        ab.setHomeButtonEnabled(true);
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setDisplayShowTitleEnabled(false);

        toolbar.setOnMenuItemClickListener(item -> {
            //只有一个，所以不用判断
            Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse(getString(R.string.GITHUB_REPO_URL)));
            startActivity(browserIntent);
            return true;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");

        boolean hasAddedBall = BallSettingRepo.isAddedBallInSetting();
        refreshActivityViews(hasAddedBall);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
        LocalBroadcastManager.getInstance(MainActivity.this).unregisterReceiver(refreshReceiver);

        System.gc();
        System.runFinalization();
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int i) {
        if (maxScrollSize == 0) {
            maxScrollSize = appBarLayout.getTotalScrollRange();
        }

        int percentage = (Math.abs(i)) * 100 / maxScrollSize;

        if (percentage >= PERCENTAGE_TO_ANIMATE_AVATAR && isAvatarShown) {
            isAvatarShown = false;

            logoImageView.animate()
                    .scaleY(0).scaleX(0)
                    .setDuration(200)
                    .start();
        }

        if (percentage <= PERCENTAGE_TO_ANIMATE_AVATAR && !isAvatarShown) {
            isAvatarShown = true;

            logoImageView.animate()
                    .scaleY(1).scaleX(1)
                    .setDuration(200)
                    .start();
        }
    }

    @Override
    public void onBackPressed() {
        //如果打开了drawer则关闭，未打开则调用父类。
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Option Menu 选项菜单
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    private void refreshActivityViews(boolean hasAddedBall) {
        if (hasAddedBall) {
            mFloatingActionButton.setImageAlpha(255);
            ballSwitch.setChecked(true);
        } else {
            mFloatingActionButton.setImageAlpha(40);
            ballSwitch.setChecked(false);
        }
    }

    private void refreshSettingFragmentViews() {
        FragmentManager fragmentManager = getFragmentManager();
        Fragment f = fragmentManager.findFragmentById(R.id.contentFrame);
        if (f instanceof SettingFragment) {
            SettingFragment settingFragment = (SettingFragment) f;
            settingFragment.refreshViews(ballSwitch.isChecked());
        }
    }

    /**
     * Sent intent to FloatingBallService
     */
    private void sendAddFloatBallIntent() {
        Intent intent = new Intent(MainActivity.this, FloatingBallService.class);
        Bundle data = new Bundle();
        data.putInt(EXTRAS_COMMAND, FloatingBallService.TYPE_SWITCH_ON);
        intent.putExtras(data);
        startService(intent);
    }

    private void sendRemoveFloatBallIntent() {
        Intent intent = new Intent(MainActivity.this, FloatingBallService.class);
        Bundle data = new Bundle();
        data.putInt(EXTRAS_COMMAND, FloatingBallService.TYPE_REMOVE_ALL);
        intent.putExtras(data);
        startService(intent);
    }

    // Function methods
    private void addLicenseFragment() {
        FragmentManager fragmentManager = getFragmentManager();
        ScrollViewLicenseFragment recyclerViewLicenseFragment =
                ScrollViewLicenseFragment.newInstance();
        ArrayList<License> customLicenses = new ArrayList<>();
        customLicenses.add(
                new License(MainActivity.this, "Butter Knife", LicenseType.APACHE_LICENSE_20,
                        "2013", "Jake Wharton"));
        customLicenses.add(
                new License(MainActivity.this, "DiscreteSeekBar", LicenseType.APACHE_LICENSE_20,
                        "2014", "Gustavo Claramunt (Ander Webbs)"));
        customLicenses.add(
                new License(MainActivity.this, "CircleImageView", LicenseType.APACHE_LICENSE_20,
                        "2014-2018", "Henning Dodenhof"));
        recyclerViewLicenseFragment.addCustomLicense(customLicenses);

        ActivityUtils.addFragmentToActivity(fragmentManager, recyclerViewLicenseFragment,
                R.id.contentFrame);
    }

    private void uninstall() {
        ComponentName componentName =
                new ComponentName(MainActivity.this, LockRequestReceiver.class);
        DevicePolicyManager devicePolicyManager =
                (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        if (devicePolicyManager != null) {
            devicePolicyManager.removeActiveAdmin(componentName);
        }

        Uri packageUri = Uri.parse("package:" + MainActivity.this.getPackageName());
        Intent intent = new Intent(Intent.ACTION_DELETE, packageUri);
        startActivity(intent);
    }

    public static class RefreshReceiver extends BroadcastReceiver {
        MainActivity mainActivity;

        public RefreshReceiver(MainActivity mainActivity) {
            super();
            this.mainActivity = mainActivity;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (mainActivity != null) {
                mainActivity.onResume();
            }
        }
    }
}
