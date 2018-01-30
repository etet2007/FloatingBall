package com.chenyee.stephenlau.floatingball;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.CardView;
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
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Toast;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

import static com.chenyee.stephenlau.floatingball.SharedPreferencesUtil.*;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,AppBarLayout.OnOffsetChangedListener {
    private static final String GITHUB_REPO_URL = "https://github.com/etet2007/FloatingBall";
    private static final String GITHUB_REPO_RELEASE_URL = "https://github.com/etet2007/FloatingBall/releases";


    private static final int PERCENTAGE_TO_ANIMATE_AVATAR = 40;
    private boolean mIsAvatarShown = true;

    private ImageView mProfileImage;
    private int mMaxScrollSize;

    public static final String TAG = "MainActivity";

    //控件
    private FloatingActionButton fab;
    private SwitchCompat ballSwitch;
    private DiscreteSeekBar opacitySeekBar;
    private DiscreteSeekBar sizeSeekBar;
    private Button choosePicButton;
    private SwitchCompat backgroundSwitch;
    //显示参数
    SharedPreferences prefs;

    //调用系统相册-选择图片
    private static final int IMAGE = 1;
    private final int mREQUEST_external_storage = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //初始化toolbar等
        initFrameViews();
        //初始化view
        initContentViews();
        //申请权限 DrawOverlays
        requestDrawOverlaysPermission();
    }

    private void initFrameViews() {
        // Set up the toolbar. 工具栏。
        Toolbar toolbar = (Toolbar) findViewById(R.id.materialup_toolbar);
        setSupportActionBar(toolbar);//顺序会有影响
        ActionBar ab = getSupportActionBar();
        ab.setHomeButtonEnabled(true);
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setDisplayShowTitleEnabled(false);

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override public boolean onMenuItemClick(MenuItem item) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(GITHUB_REPO_URL));
                startActivity(browserIntent);
                return true;
            }
        });

//        toolbar.setNavigationIcon(R.drawable.ic_menu_send);
//        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                onBackPressed();
//            }
//        });

        //Set up the appBarLayout.
        AppBarLayout appbarLayout = (AppBarLayout) findViewById(R.id.materialup_appbar);
        appbarLayout.addOnOffsetChangedListener(this);
        mMaxScrollSize = appbarLayout.getTotalScrollRange();

        // Set up the FloatingActionButton.
        fab = (FloatingActionButton) findViewById(R.id.logo_fab);
        fab.setUseCompatPadding(false);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ballSwitch.toggle();
            }
        });

        // Set up the ActionBarDrawerToggle.
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // Set up the navigation drawer.左侧滑出的菜单。
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

//        Intent intent = new Intent(MainActivity.this, FloatBallService.class);
//        Bundle data = new Bundle();
//        data.putInt("type", FloatBallService.TYPE_SAVE);
//        intent.putExtras(data);
//        startService(intent);
    }

    private void requestDrawOverlaysPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            //Setting :The Settings provider contains global system-level device preferences.
            //Checks if the specified context can draw on top of other apps. As of API level 23,
            // an app cannot draw on top of other apps unless it declares the SYSTEM_ALERT_WINDOW permission
            // in its manifest, and the user specifically grants the app this capability.
            // To prompt the user to grant this approval, the app must send an intent with the action
            // ACTION_MANAGE_OVERLAY_PERMISSION, which causes the system to display a permission management screen.
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivityForResult(intent, 1);
                Toast.makeText(this, "请先允许FloatBall出现在顶部", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int i) {
        if (mMaxScrollSize == 0)
            mMaxScrollSize = appBarLayout.getTotalScrollRange();

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
                    .start();
        }
    }
    private void initContentViews() {
        mProfileImage = (ImageView) findViewById(R.id.materialup_profile_image);
        opacitySeekBar = (DiscreteSeekBar) findViewById(R.id.opacity_seekbar);
        opacitySeekBar = (DiscreteSeekBar) findViewById(R.id.opacity_seekbar);
        ballSwitch = (SwitchCompat) findViewById(R.id.start_switch);
        sizeSeekBar = (DiscreteSeekBar) findViewById(R.id.size_seekbar);
        choosePicButton = (Button) findViewById(R.id.choosePic_button);
        backgroundSwitch = (SwitchCompat) findViewById(R.id.background_switch);

        //获取悬浮球参数
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean hasAddedBall = prefs.getBoolean(KEY_HAS_Added_Ball, false);
        Log.d(TAG, "hasAddedBall: "+hasAddedBall);
        int opacity = prefs.getInt(KEY_OPACITY, 125);
        int ballSize = prefs.getInt(KEY_SIZE, 25);
        boolean useBackground = prefs.getBoolean(KEY_USE_BACKGROUND, false);

        //根据数据进行初始化
        opacitySeekBar.setProgress(opacity);
        sizeSeekBar.setProgress(ballSize);
        backgroundSwitch.setChecked(useBackground);

        //hasAddedBall代表两种状态
        updateViewsState(hasAddedBall);

        ballSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    addFloatBall();
                    Snackbar.make(buttonView, "Add floating ball.", Snackbar.LENGTH_SHORT)
                            .setAction("Action", null).show();
                }else{
                    removeFloatBall();
                    Snackbar.make(buttonView, "Remove floating ball.", Snackbar.LENGTH_SHORT)
                            .setAction("Action", null).show();
                }
                updateViewsState(isChecked);
            }
        });

        opacitySeekBar.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {
            @Override
            public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt(KEY_OPACITY,value);
                editor.apply();

                sendUpdateIntentToService();
            }
            @Override
            public void onStartTrackingTouch(DiscreteSeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(DiscreteSeekBar seekBar) {
            }
        });
        sizeSeekBar.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {
            @Override
            public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt(KEY_SIZE,value);
                editor.apply();

                sendUpdateIntentToService();
            }

            @Override
            public void onStartTrackingTouch(DiscreteSeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(DiscreteSeekBar seekBar) {
            }
        });
        backgroundSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean(KEY_USE_BACKGROUND,isChecked);
                editor.apply();

                sendUpdateIntentToService();
            }
        });
        choosePicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //检查权限 请求权限 选图片
                requestStoragePermission();

                Intent intent = new Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, IMAGE);//onActivityResult
            }
        });
    }

    private void updateViewsState(boolean hasAddedBall) {
        if(hasAddedBall) {
            fab.setImageAlpha(255);
            ballSwitch.setChecked(true);
            opacitySeekBar.setEnabled(true);
            sizeSeekBar.setEnabled(true);
            choosePicButton.setEnabled(true);
            backgroundSwitch.setEnabled(true);
        }else{
            fab.setImageAlpha(40);
            ballSwitch.setChecked(false);
            opacitySeekBar.setEnabled(false);
            sizeSeekBar.setEnabled(false);
            choosePicButton.setEnabled(false);
            backgroundSwitch.setEnabled(false);
        }
    }

    private void sendUpdateIntentToService() {
        Intent intent = new Intent(MainActivity.this, FloatBallService.class);
        Bundle data = new Bundle();
        data.putInt("type", FloatBallService.TYPE_UPDATE_DATA);
        intent.putExtras(data);
        startService(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //选取图片的回调
        if (requestCode == IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            Uri selectedImage = data.getData();

            String[] filePathColumns = {MediaStore.Images.Media.DATA};
            Cursor c = getContentResolver().query(selectedImage, filePathColumns, null, null, null);

            if (c == null)
                return;
            c.moveToFirst();
            int columnIndex = c.getColumnIndex(filePathColumns[0]);
            String imagePath = c.getString(columnIndex);
            Log.d("lqt", "onActivityResult: "+imagePath);

            Intent intent = new Intent(MainActivity.this, FloatBallService.class);
            Bundle bundle = new Bundle();
            bundle.putInt("type", FloatBallService.TYPE_IMAGE);
            bundle.putString("imagePath", imagePath);
            intent.putExtras(bundle);
            startService(intent);

            c.close();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==mREQUEST_external_storage){
//判断是否成功
//            成功继续打开图片？
        }
    }


    private void requestStoragePermission() {
        String[] PERMISSIONS_STORAGE = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
        int permission = ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

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
        checkAccessibility();

        Intent intent = new Intent(MainActivity.this, FloatBallService.class);
        Bundle data = new Bundle();
        data.putInt("type", FloatBallService.TYPE_ADD);
        intent.putExtras(data);
        startService(intent);
    }

    private void removeFloatBall() {
        Intent intent = new Intent(MainActivity.this, FloatBallService.class);
        Bundle data = new Bundle();
        data.putInt("type", FloatBallService.TYPE_DEL);
        intent.putExtras(data);
        startService(intent);
    }


    private void checkAccessibility() {
        // 判断辅助功能是否开启
        if (!AccessibilityUtil.isAccessibilitySettingsOn(this)) {
            // 引导至辅助功能设置页面
            startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
            Toast.makeText(this, "请先开启FloatBall辅助功能", Toast.LENGTH_SHORT).show();
        }
    }

    // 模板的代码
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
//
//        if (id == R.id.nav_camera) {
//            // Handle the camera action
//        } else if (id == R.id.nav_gallery) {
//
//        } else if (id == R.id.nav_slideshow) {
//
//        } else if (id == R.id.nav_manage) {
//
//        } else
         if (id == R.id.nav_share) {
             Intent textIntent = new Intent(Intent.ACTION_SEND);
             textIntent.setType("text/plain");
             textIntent.putExtra(Intent.EXTRA_TEXT, GITHUB_REPO_RELEASE_URL);
             startActivity(Intent.createChooser(textIntent, "shared"));
             }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
