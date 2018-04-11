package com.chenyee.stephenlau.floatingball.activity;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import com.artitk.licensefragment.ScrollViewLicenseFragment;
import com.artitk.licensefragment.model.License;
import com.artitk.licensefragment.model.LicenseType;
import com.chenyee.stephenlau.floatingball.floatBall.FloatingBallService;
import com.chenyee.stephenlau.floatingball.R;
import com.chenyee.stephenlau.floatingball.fragment.SettingFragment;
import com.chenyee.stephenlau.floatingball.util.ActivityUtils;


import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.chenyee.stephenlau.floatingball.util.StaticStringUtil.*;


public class MainActivity extends AppCompatActivity
        implements AppBarLayout.OnOffsetChangedListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    //头像
    private static final int PERCENTAGE_TO_ANIMATE_AVATAR = 40;
    private boolean mIsAvatarShown = true;
    private int mMaxScrollSize;

    //控件
    @BindView(R.id.logo_fab)
    FloatingActionButton fab;
    @BindView(R.id.start_switch)
    SwitchCompat ballSwitch;

    @BindView(R.id.materialup_profile_image)
    ImageView mProfileImage;

    //调用系统相册-选择图片
    private static final int IMAGE = 1;
    private final int mREQUEST_external_storage = 1;

    public static Intent getStartIntent(Context context) {
        return new Intent(context, MainActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //ButterKnife
        ButterKnife.bind(this);

        //初始化toolbar等
        initFrameViews();

        FragmentManager fragmentManager = getFragmentManager();
        SettingFragment settingFragment = SettingFragment.newInstance();
        ActivityUtils.addFragmentToActivity(fragmentManager, settingFragment, R.id.contentFrame);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: ");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.gc();
        System.runFinalization();

        Log.d(TAG, "onDestroy: ");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //选取图片的回调
        if (requestCode == IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            if(selectedImage==null)
                return;

            String[] filePathColumns = {MediaStore.Images.Media.DATA};
            Cursor c = getContentResolver().query(selectedImage, filePathColumns, null, null, null);
            if (c == null) return;

            c.moveToFirst();
            int columnIndex = c.getColumnIndex(filePathColumns[0]);
            String imagePath = c.getString(columnIndex);

            Bundle bundle = new Bundle();
            bundle.putInt(EXTRA_TYPE, FloatingBallService.TYPE_IMAGE);
            bundle.putString("imagePath", imagePath);
            Intent intent = new Intent(MainActivity.this, FloatingBallService.class)
                    .putExtras(bundle);
            startService(intent);

            c.close();
        }
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int i) {
        if (mMaxScrollSize == 0) {
            mMaxScrollSize = appBarLayout.getTotalScrollRange();
        }

        int percentage = (Math.abs(i)) * 100 / mMaxScrollSize;

        if (percentage >= PERCENTAGE_TO_ANIMATE_AVATAR && mIsAvatarShown) {
            mIsAvatarShown = false;

            mProfileImage.animate()
                    .scaleY(0).scaleX(0)
                    .setDuration(200)
                    .start();
        }

        if (percentage <= PERCENTAGE_TO_ANIMATE_AVATAR && !mIsAvatarShown) {
            mIsAvatarShown = true;

            mProfileImage.animate()
                    .scaleY(1).scaleX(1)
                    .setDuration(200)
                    .start();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == mREQUEST_external_storage) {
            //判断是否成功
            //            成功继续打开图片？
        }
    }

    // 模板的代码
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

//        if (id == R.id.action_settings) {
//            return true;
//        }
        return super.onOptionsItemSelected(item);
    }

    private void initFrameViews() {
        // Set up the toolbar. 工具栏。
        Toolbar toolbar = findViewById(R.id.materialup_toolbar);
        setSupportActionBar(toolbar);//顺序会有影响
        ActionBar ab = getSupportActionBar();
        ab.setHomeButtonEnabled(true);
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setDisplayShowTitleEnabled(false);

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                //只有一个，所以不用判断
                Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse(getString(R.string.GITHUB_REPO_URL)));
                startActivity(browserIntent);
                return true;
            }
        });

        //Set up the appBarLayout.
        AppBarLayout appbarLayout = findViewById(R.id.materialup_appbar);
        appbarLayout.addOnOffsetChangedListener(this);
        mMaxScrollSize = appbarLayout.getTotalScrollRange();

        // Set up the FloatingActionButton.
        fab.setUseCompatPadding(false);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ballSwitch.toggle();
            }
        });

        //悬浮球的开关
        ballSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    addFloatBall();
                    Snackbar.make(buttonView, R.string.add_ball_hint, Snackbar.LENGTH_SHORT)
                            .setAction("Action", null).show();
                } else {
                    removeFloatBall();
                    Snackbar.make(buttonView, R.string.remove_ball_hint, Snackbar.LENGTH_SHORT)
                            .setAction("Action", null).show();
                }
                updateViewsState(isChecked);
            }
        });

        // Set up the ActionBarDrawerToggle.
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // Set up the navigation drawer.左侧滑出的菜单。
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                // Handle navigation view item clicks here.
                int id = item.getItemId();
                if (id == R.id.nav_share) {
                    Intent textIntent = new Intent(Intent.ACTION_SEND)
                            .setType("text/plain")
                            .putExtra(Intent.EXTRA_TEXT, getString(R.string.GITHUB_REPO_RELEASE_URL));
                    startActivity(Intent.createChooser(textIntent, "shared"));
                } else if (id == R.id.nav_license) {
                    FragmentManager fragmentManager = getFragmentManager();
                    ScrollViewLicenseFragment recyclerViewLicenseFragment = ScrollViewLicenseFragment.newInstance();
                    ArrayList<License> customLicenses = new ArrayList<>();
                    customLicenses.add(new License(MainActivity.this, "Butter Knife", LicenseType.APACHE_LICENSE_20, "2013", "Jake Wharton"));
                    customLicenses.add(new License(MainActivity.this, "Material Animated Switch", LicenseType.APACHE_LICENSE_20,"2015","Adrián García Lomas"));
                    customLicenses.add(new License(MainActivity.this, "DiscreteSeekBar", LicenseType.APACHE_LICENSE_20,"2014","Gustavo Claramunt (Ander Webbs)"));
                    customLicenses.add(new License(MainActivity.this, "CircleImageView", LicenseType.APACHE_LICENSE_20,"2014-2018","Henning Dodenhof"));
                    recyclerViewLicenseFragment.addCustomLicense(customLicenses);

                    ActivityUtils.addFragmentToActivity(fragmentManager, recyclerViewLicenseFragment, R.id.contentFrame);
                } else if (id == R.id.setting) {
                    FragmentManager fragmentManager = getFragmentManager();
                    SettingFragment settingFragment = SettingFragment.newInstance();
                    ActivityUtils.addFragmentToActivity(fragmentManager, settingFragment, R.id.contentFrame);
                }

                DrawerLayout drawer = findViewById(R.id.drawer_layout);
                drawer.closeDrawer(GravityCompat.START);

                return true;
            }
        });
        // Update versionTextView
        try {
            PackageInfo pi = getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = pi.versionName;
            TextView versionTextView = navigationView.getHeaderView(0)
                    .findViewById(R.id.version_textView);
            versionTextView.setText(String.format(getString(R.string.version_textview), version));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        // Update views
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean hasAddedBall = prefs.getBoolean(PREF_HAS_ADDED_BALL, false);
        updateViewsState(hasAddedBall);

    }

    private void updateViewsState(boolean hasAddedBall) {
        if (hasAddedBall) {
            fab.setImageAlpha(255);
            ballSwitch.setChecked(true);
        } else {
            fab.setImageAlpha(40);
            ballSwitch.setChecked(false);
        }
        FragmentManager fragmentManager = getFragmentManager();
        Fragment f = fragmentManager.findFragmentById(R.id.contentFrame);
        if (f instanceof SettingFragment) {
            SettingFragment settingFragment = (SettingFragment) f;
            settingFragment.updateViewsState(hasAddedBall);
        }
    }

    private void requestStoragePermission() {
        String[] PERMISSIONS_STORAGE = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
        int permission = ActivityCompat
                .checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    MainActivity.this,
                    PERMISSIONS_STORAGE,
                    mREQUEST_external_storage
            );
        }
    }

    private void addFloatBall() {
        Intent intent = new Intent(MainActivity.this, FloatingBallService.class);
        Bundle data = new Bundle();
        data.putInt(EXTRA_TYPE, FloatingBallService.TYPE_ADD);
        intent.putExtras(data);
        startService(intent);
    }

    private void removeFloatBall() {
        Intent intent = new Intent(MainActivity.this, FloatingBallService.class);
        Bundle data = new Bundle();
        data.putInt(EXTRA_TYPE, FloatingBallService.TYPE_REMOVE);
        intent.putExtras(data);
        startService(intent);
    }

}
