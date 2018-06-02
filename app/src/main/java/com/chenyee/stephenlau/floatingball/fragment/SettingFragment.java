package com.chenyee.stephenlau.floatingball.fragment;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;

import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;

import com.chenyee.stephenlau.floatingball.R;
import com.chenyee.stephenlau.floatingball.floatBall.FloatingBallService;
import com.chenyee.stephenlau.floatingball.util.SharedPrefsUtils;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

import java.util.ArrayDeque;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static com.chenyee.stephenlau.floatingball.util.StaticStringUtil.*;


public class SettingFragment extends Fragment {
    private static final String TAG = SettingFragment.class.getSimpleName();

    private Unbinder mUnBinder;

    //控件
    @BindView(R.id.root_linearLayout)
    LinearLayoutCompat rootLinearLayout;
    @BindView(R.id.opacity_seekbar)
    DiscreteSeekBar opacitySeekBar;
    @BindView(R.id.size_seekbar)
    DiscreteSeekBar sizeSeekBar;
    @BindView(R.id.opacity_mode_textView)
    AppCompatTextView opacityModeTextView;
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
    @BindView(R.id.is_rotate_hide)
    SwitchCompat isRotateHideSwitch;
    @BindView(R.id.vibrate_switch)
    SwitchCompat vibrateSwitch;
    //参数
//    private SharedPreferences prefs;

    //调用系统相册-选择图片
    private static final int REQUEST_CODE_IMAGE = 1;
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
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onStop() {
        super.onStop();
        sendClearIntentToService();
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
        Log.d(TAG, "onActivityResult: SettingFragment");

        //选取图片的回调
        if (requestCode == REQUEST_CODE_IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            if (selectedImage == null) {
                return;
            }

            String[] filePathColumns = {MediaStore.Images.Media.DATA};
            Cursor c = getActivity().getContentResolver().query(selectedImage, filePathColumns, null, null, null);
            if (c == null) return;

            c.moveToFirst();
            int columnIndex = c.getColumnIndex(filePathColumns[0]);
            String imagePath = c.getString(columnIndex);

            //发送Intent给FloatingBallService
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

    private void initContentViews() {
        opacitySeekBar.setProgress(SharedPrefsUtils.getIntegerPreference(PREF_OPACITY, 125));
        sizeSeekBar.setProgress(SharedPrefsUtils.getIntegerPreference(PREF_SIZE, 25));
        backgroundSwitch.setChecked(SharedPrefsUtils.getBooleanPreference(PREF_USE_BACKGROUND, false));
        useGrayBackgroundSwitch.setChecked(SharedPrefsUtils.getBooleanPreference(PREF_USE_GRAY_BACKGROUND, true));
        vibrateSwitch.setChecked(SharedPrefsUtils.getBooleanPreference(PREF_IS_VIBRATE, true));
        isRotateHideSwitch.setChecked(SharedPrefsUtils.getBooleanPreference(PREF_IS_ROTATE_HIDE,true));
        updateFunctionListView();
        updateOpacityModeView();

//        boolean hasAddedBall = SharedPrefsUtils.getBooleanPreference(PREF_HAS_ADDED_BALL, false);
//        //hasAddedBall代表两种状态
//        updateViewsState(hasAddedBall);

        opacitySeekBar.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {
            @Override
            public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
                SharedPrefsUtils.setIntegerPreference( PREF_OPACITY, value);
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
                SharedPrefsUtils.setIntegerPreference( PREF_SIZE, value);
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
                SharedPrefsUtils.setIntegerPreference(PREF_MOVE_UP_DISTANCE, value);
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
                SharedPrefsUtils.setBooleanPreference(PREF_USE_BACKGROUND, isChecked);
            }
        });
        choosePicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //检查权限 请求权限 选图片
                requestStoragePermission();

                Intent intent = new Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, REQUEST_CODE_IMAGE);//onActivityResult
            }
        });
        useGrayBackgroundSwitch
                .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        SharedPrefsUtils.setBooleanPreference(PREF_USE_GRAY_BACKGROUND, isChecked);
                    }
                });
        isRotateHideSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                SharedPrefsUtils.setBooleanPreference(PREF_IS_ROTATE_HIDE, isChecked);
            }
        });
        vibrateSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPrefsUtils.setBooleanPreference(PREF_IS_VIBRATE, isChecked);
            }
        });
    }

    private void updateFunctionListView() {
        Resources res = getResources();
        String[] functionList = res.getStringArray(R.array.function_array);
        int doubleClickEvent = SharedPrefsUtils.getIntegerPreference(PREF_DOUBLE_CLICK_EVENT, NONE);
        doubleClickTextView.setText(functionList[doubleClickEvent]);

        int rightSlideEvent = SharedPrefsUtils.getIntegerPreference(PREF_RIGHT_SLIDE_EVENT, RECENT_APPS);
        rightSlideTextView.setText(functionList[rightSlideEvent]);

        int leftSlideEvent = SharedPrefsUtils.getIntegerPreference(PREF_LEFT_SLIDE_EVENT, RECENT_APPS);
        leftSlideTextView.setText(functionList[leftSlideEvent]);

        int upSlideEvent = SharedPrefsUtils.getIntegerPreference(PREF_UP_SLIDE_EVENT, HOME);
        upSlideTextView.setText(functionList[upSlideEvent]);

        int downSlideEvent = SharedPrefsUtils.getIntegerPreference(PREF_DOWN_SLIDE_EVENT, NOTIFICATION);
        downSlideTextView.setText(functionList[downSlideEvent]);
    }

    private void updateOpacityModeView() {
        Resources res = getResources();
        String[] opacityModeList = res.getStringArray(R.array.opacity_mode);
        int opacityMode = SharedPrefsUtils.getIntegerPreference(PREF_OPACITY_MODE,OPACITY_NONE);
        opacityModeTextView.setText(opacityModeList[opacityMode]);
    }

    public void updateViewsState(boolean hasAddedBall) {
        //应该改成遍历

        if (hasAddedBall) {
            enableAll(rootLinearLayout, true);
        } else {
            enableAll(rootLinearLayout, false);
        }
    }
    private void enableAll(View root,boolean enanbled) {
        ArrayDeque stack = new ArrayDeque();
        stack.addLast(root);
        while (!stack.isEmpty()) {
            //取得栈顶
            View top = (View) stack.getLast();
            //出栈
            stack.pollLast();
            //如果为viewGroup则使子节点入栈
            if (top instanceof ViewGroup) {
                int childCount = ((ViewGroup) top).getChildCount();
                for (int i = childCount - 1; i >= 0; i--) {
                    stack.addLast(((ViewGroup) top).getChildAt(i));
                }
            }
            //如果栈顶为View类型，输出
            else if (top instanceof View)
                top.setEnabled(enanbled);

        }
    }
    /**
     * 不再对悬浮球进行设置，悬浮球可以清不需要的模块的内存。
     */
    private void sendClearIntentToService() {
        Intent intent = new Intent(getActivity(), FloatingBallService.class);
        Bundle data = new Bundle();
        data.putInt(EXTRA_TYPE, FloatingBallService.TYPE_CLEAR);
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
                        SharedPrefsUtils.setIntegerPreference(prefKey, which);
                        updateFunctionListView();
                    }
                }).show();
    }

    @OnClick(R.id.opacity_mode_relativeLayout)
    public void onOpacityModeClicked(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.opacity_mode)
                .setItems(R.array.opacity_mode, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPrefsUtils.setIntegerPreference(PREF_OPACITY_MODE, which);
                        updateOpacityModeView();
                    }
                }).show();
    }
}
