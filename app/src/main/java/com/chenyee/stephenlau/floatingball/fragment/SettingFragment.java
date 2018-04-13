package com.chenyee.stephenlau.floatingball.fragment;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;

import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.chenyee.stephenlau.floatingball.R;
import com.chenyee.stephenlau.floatingball.activity.MainActivity;
import com.chenyee.stephenlau.floatingball.floatBall.FloatingBallService;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static com.chenyee.stephenlau.floatingball.util.StaticStringUtil.EXTRA_TYPE;
import static com.chenyee.stephenlau.floatingball.util.StaticStringUtil.HOME;
import static com.chenyee.stephenlau.floatingball.util.StaticStringUtil.NONE;
import static com.chenyee.stephenlau.floatingball.util.StaticStringUtil.NOTIFICATION;
import static com.chenyee.stephenlau.floatingball.util.StaticStringUtil.PREF_DOUBLE_CLICK_EVENT;
import static com.chenyee.stephenlau.floatingball.util.StaticStringUtil.PREF_DOWN_SLIDE_EVENT;
import static com.chenyee.stephenlau.floatingball.util.StaticStringUtil.PREF_HAS_ADDED_BALL;
import static com.chenyee.stephenlau.floatingball.util.StaticStringUtil.PREF_LEFT_SLIDE_EVENT;
import static com.chenyee.stephenlau.floatingball.util.StaticStringUtil.PREF_MOVE_UP_DISTANCE;
import static com.chenyee.stephenlau.floatingball.util.StaticStringUtil.PREF_OPACITY;
import static com.chenyee.stephenlau.floatingball.util.StaticStringUtil.PREF_RIGHT_SLIDE_EVENT;
import static com.chenyee.stephenlau.floatingball.util.StaticStringUtil.PREF_SIZE;
import static com.chenyee.stephenlau.floatingball.util.StaticStringUtil.PREF_UP_SLIDE_EVENT;
import static com.chenyee.stephenlau.floatingball.util.StaticStringUtil.PREF_USE_BACKGROUND;
import static com.chenyee.stephenlau.floatingball.util.StaticStringUtil.PREF_USE_GRAY_BACKGROUND;
import static com.chenyee.stephenlau.floatingball.util.StaticStringUtil.RECENT_APPS;

public class SettingFragment extends Fragment {
    private static final String TAG = SettingFragment.class.getSimpleName();

    private Unbinder mUnBinder;

    //控件
//    @BindView(R.id.logo_fab)
//    FloatingActionButton fab;
//    @BindView(R.id.start_switch)
//    SwitchCompat ballSwitch;
    @BindView(R.id.opacity_seekbar)
    DiscreteSeekBar opacitySeekBar;
    @BindView(R.id.size_seekbar)
    DiscreteSeekBar sizeSeekBar;
    @BindView(R.id.choosePic_button)
    Button choosePicButton;
    @BindView(R.id.background_switch)
    SwitchCompat backgroundSwitch;
    @BindView(R.id.upDistance_seekbar)
    DiscreteSeekBar upDistanceSeekBar;
    @BindView(R.id.use_gray_background_switch)
    SwitchCompat useGrayBackgroundSwitch;

    @BindView(R.id.double_click_textView)
    AppCompatTextView doubleClickTextView;
    @BindView(R.id.left_slide_textView)
    AppCompatTextView leftSlideTextView;
    @BindView(R.id.up_slide_textView)
    AppCompatTextView upSlideTextView;
    @BindView(R.id.down_slide_textView)
    AppCompatTextView downSlideTextView;
    @BindView(R.id.right_slide_textView)
    AppCompatTextView rightSlideTextView;
    //参数
    private SharedPreferences prefs;

    //调用系统相册-选择图片
    private static final int IMAGE = 1;
    private final int mREQUEST_external_storage = 1;

    public SettingFragment() {
        // Required empty public constructor
    }

    public static SettingFragment newInstance() {
        return new SettingFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_setting, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ButterKnife bind
        mUnBinder = ButterKnife.bind(this, view);

        //初始化view
        initContentViews();
        //申请DrawOverlays权限
        requestDrawOverlaysPermission();

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onResume() {
        super.onResume();
        boolean hasAddBall = prefs.getBoolean(PREF_HAS_ADDED_BALL, false);
        if (hasAddBall) {
            addFloatBall();
        }
        updateViewsState(hasAddBall);
    }

    @Override
    public void onDetach() {
        super.onDetach();

    }

    @Override
    public void onDestroy() {
        // ButterKnife unbind
        mUnBinder.unbind();

        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //选取图片的回调
        if (requestCode == IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            if(selectedImage==null)
                return;

            String[] filePathColumns = {MediaStore.Images.Media.DATA};
            Cursor c = getActivity().getContentResolver().query(selectedImage, filePathColumns, null, null, null);
            if (c == null) return;

            c.moveToFirst();
            int columnIndex = c.getColumnIndex(filePathColumns[0]);
            String imagePath = c.getString(columnIndex);

            Bundle bundle = new Bundle();
            bundle.putInt(EXTRA_TYPE, FloatingBallService.TYPE_IMAGE);
            bundle.putString("imagePath", imagePath);
            Intent intent = new Intent(getActivity(), FloatingBallService.class)
                    .putExtras(bundle);
            getActivity().startService(intent);

            c.close();
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

    private void requestDrawOverlaysPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            //Setting :The Settings provider contains global system-level device preferences.
            //Checks if the specified context can draw on top of other apps. As of API level 23,
            // an app cannot draw on top of other apps unless it declares the SYSTEM_ALERT_WINDOW permission
            // in its manifest, and the user specifically grants the app this capability.
            // To prompt the user to grant this approval, the app must send an intent with the action
            // ACTION_MANAGE_OVERLAY_PERMISSION, which causes the system to display a permission management screen.
            if (!Settings.canDrawOverlays(getActivity())) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivityForResult(intent, 1);
                Toast.makeText(getActivity(), "请先允许FloatBall出现在顶部", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void initContentViews() {
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        opacitySeekBar.setProgress(prefs.getInt(PREF_OPACITY, 125));
        sizeSeekBar.setProgress(prefs.getInt(PREF_SIZE, 25));
        backgroundSwitch.setChecked(prefs.getBoolean(PREF_USE_BACKGROUND, false));
        useGrayBackgroundSwitch.setChecked(prefs.getBoolean(PREF_USE_GRAY_BACKGROUND, true));

        updateFunctionList();

        boolean hasAddedBall = prefs.getBoolean(PREF_HAS_ADDED_BALL, false);
        //hasAddedBall代表两种状态
        updateViewsState(hasAddedBall);

        opacitySeekBar.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {
            @Override
            public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
                SharedPreferences.Editor editor = prefs.edit()
                        .putInt(PREF_OPACITY, value);
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
                editor.putInt(PREF_SIZE, value);
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

        upDistanceSeekBar.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {
            @Override
            public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt(PREF_MOVE_UP_DISTANCE, value);
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
                editor.putBoolean(PREF_USE_BACKGROUND, isChecked);
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
        useGrayBackgroundSwitch
                .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putBoolean(PREF_USE_GRAY_BACKGROUND, isChecked);
                        editor.apply();
                        sendUpdateIntentToService();
                    }
                });
    }

    private void updateFunctionList() {
        Resources res = getResources();
        String[] functionList = res.getStringArray(R.array.function_array);
        int doubleClickEvent = prefs.getInt(PREF_DOUBLE_CLICK_EVENT, NONE);
        doubleClickTextView.setText(functionList[doubleClickEvent]);

        int rightSlideEvent = prefs.getInt(PREF_RIGHT_SLIDE_EVENT, RECENT_APPS);
        rightSlideTextView.setText(functionList[rightSlideEvent]);

        int leftSlideEvent = prefs.getInt(PREF_LEFT_SLIDE_EVENT, RECENT_APPS);
        leftSlideTextView.setText(functionList[leftSlideEvent]);

        int upSlideEvent = prefs.getInt(PREF_UP_SLIDE_EVENT, HOME);
        upSlideTextView.setText(functionList[upSlideEvent]);

        int downSlideEvent = prefs.getInt(PREF_DOWN_SLIDE_EVENT, NOTIFICATION);
        downSlideTextView.setText(functionList[downSlideEvent]);
    }

    public void updateViewsState(boolean hasAddedBall) {
        if (hasAddedBall) {
            opacitySeekBar.setEnabled(true);
            sizeSeekBar.setEnabled(true);
            choosePicButton.setEnabled(true);
            backgroundSwitch.setEnabled(true);
            upDistanceSeekBar.setEnabled(true);
            useGrayBackgroundSwitch.setEnabled(true);
        } else {
            opacitySeekBar.setEnabled(false);
            sizeSeekBar.setEnabled(false);
            choosePicButton.setEnabled(false);
            backgroundSwitch.setEnabled(false);
            upDistanceSeekBar.setEnabled(false);
            useGrayBackgroundSwitch.setEnabled(false);
        }
    }

    private void sendUpdateIntentToService() {
        Intent intent = new Intent(getActivity(), FloatingBallService.class);
        Bundle data = new Bundle();
        data.putInt(EXTRA_TYPE, FloatingBallService.TYPE_UPDATE_DATA);
        intent.putExtras(data);
        getActivity().startService(intent);
    }

    private void requestStoragePermission() {
        String[] PERMISSIONS_STORAGE = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
        int permission = ActivityCompat
                .checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    getActivity(),
                    PERMISSIONS_STORAGE,
                    mREQUEST_external_storage
            );
        }
    }

    private void addFloatBall() {
        Intent intent = new Intent(getActivity(), FloatingBallService.class);
        Bundle data = new Bundle();
        data.putInt(EXTRA_TYPE, FloatingBallService.TYPE_ADD);
        intent.putExtras(data);
        getActivity().startService(intent);
    }

    @OnClick({R.id.double_click_function,
            R.id.left_function,
            R.id.right_function,
            R.id.up_function,
            R.id.down_function
    })
    public void onDoubleClickClicked(View view) {
        if (view.getId() == R.id.double_click_function) {
            showFunctionDialog(R.string.double_click_title, PREF_DOUBLE_CLICK_EVENT);
        } else if (view.getId() == R.id.left_function) {
            showFunctionDialog(R.string.left_slide_title, PREF_LEFT_SLIDE_EVENT);
        } else if (view.getId() == R.id.right_function) {
            showFunctionDialog(R.string.right_slide_title, PREF_RIGHT_SLIDE_EVENT);
        } else if (view.getId() == R.id.up_function) {
            showFunctionDialog(R.string.up_slide_title, PREF_UP_SLIDE_EVENT);
        } else if (view.getId() == R.id.down_function) {
            showFunctionDialog(R.string.down_slide_title, PREF_DOWN_SLIDE_EVENT);
        }
    }

    private void showFunctionDialog(int titleId, final String prefKey) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(titleId)
                .setItems(R.array.function_array, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putInt(prefKey, which);
                        editor.apply();

                        sendUpdateIntentToService();

                        updateFunctionList();
                    }
                }).show();
    }
}
