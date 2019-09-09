package com.chenyee.stephenlau.floatingball.ui.fragment;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chenyee.stephenlau.floatingball.R;
import com.chenyee.stephenlau.floatingball.floatingBall.service.FloatingBallService;
import com.chenyee.stephenlau.floatingball.repository.BallSettingRepo;
import com.chenyee.stephenlau.floatingball.ui.activity.MainActivity;
import com.chenyee.stephenlau.floatingball.util.SharedPrefsUtils;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;
import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar.OnProgressChangeListener;

import java.util.ArrayDeque;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static com.chenyee.stephenlau.floatingball.util.StaticStringUtil.EXTRAS_COMMAND;
import static com.chenyee.stephenlau.floatingball.util.StaticStringUtil.OPACITY_NONE;
import static com.chenyee.stephenlau.floatingball.util.StaticStringUtil.PREF_DOUBLE_CLICK_EVENT;
import static com.chenyee.stephenlau.floatingball.util.StaticStringUtil.PREF_DOWN_SWIPE_EVENT;
import static com.chenyee.stephenlau.floatingball.util.StaticStringUtil.PREF_LEFT_SWIPE_EVENT;
import static com.chenyee.stephenlau.floatingball.util.StaticStringUtil.PREF_OPACITY_MODE;
import static com.chenyee.stephenlau.floatingball.util.StaticStringUtil.PREF_RIGHT_SWIPE_EVENT;
import static com.chenyee.stephenlau.floatingball.util.StaticStringUtil.PREF_SINGLE_TAP_EVENT;
import static com.chenyee.stephenlau.floatingball.util.StaticStringUtil.PREF_UP_SWIPE_EVENT;
import static com.chenyee.stephenlau.floatingball.util.StaticStringUtil.PREF_USE_BACKGROUND;


public class SettingFragment extends Fragment {
    private static final String TAG = SettingFragment.class.getSimpleName();
    // 调用系统相册-选择图片
    private static final int REQUEST_CODE_BACKGROUND_IMAGE = 1;
    private static final int REQUEST_EXTERNAL_STORAGE = 2;
    // Widgets
    @BindView(R.id.root_linearLayout) LinearLayoutCompat rootLinearLayout;
    @BindView(R.id.opacity_seekbar) DiscreteSeekBar opacitySeekBar;
    @BindView(R.id.size_seekbar) DiscreteSeekBar sizeSeekBar;
    @BindView(R.id.minusFloatingActionButton) FloatingActionButton minusButton;
    @BindView(R.id.amountTextView) AppCompatTextView amountTextView;
    @BindView(R.id.plusFloatingActionButton) FloatingActionButton plusButton;
    @BindView(R.id.opacity_mode_textView) AppCompatTextView opacityModeTextView;
    @BindView(R.id.choosePic_button) AppCompatButton choosePicButton;
    @BindView(R.id.background_switch) SwitchCompat backgroundSwitch;
    @BindView(R.id.use_gray_background_switch) SwitchCompat useGrayBackgroundSwitch;
    @BindView(R.id.single_tap_textView) AppCompatTextView singleTapTextView;
    @BindView(R.id.double_click_textView) AppCompatTextView doubleClickTextView;
    @BindView(R.id.left_swipe_textView) AppCompatTextView leftSlideTextView;
    @BindView(R.id.up_swipe_textView) AppCompatTextView upSlideTextView;
    @BindView(R.id.down_swipe_textView) AppCompatTextView downSlideTextView;
    @BindView(R.id.swipe_right_textView) AppCompatTextView rightSlideTextView;
    @BindView(R.id.is_rotate_hide) SwitchCompat isRotateHideSwitch;
    @BindView(R.id.vibrate_switch) SwitchCompat vibrateSwitch;
    @BindView(R.id.avoid_keyboard_switch) SwitchCompat avoidKeyboardSwitch;
    @BindView(R.id.upDistance_seekbar) DiscreteSeekBar upDistanceSeekBar;
    private Unbinder butterKnifeUnBinder;

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
    public void onStart() {
        super.onStart();

        Activity activity = getActivity();
        if (activity instanceof MainActivity) {
            refreshViews(((MainActivity) activity).isBallSwitchIsChecked());
        }
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

        butterKnifeUnBinder = ButterKnife.bind(this, view);

        // 初始化view
        initContentViews();
    }

    @Override
    public void onStop() {
        super.onStop();
        sendClearIntentToService();
    }

    @Override
    public void onDestroy() {
        // ButterKnife unbind
        butterKnifeUnBinder.unbind();
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: SettingFragment");

        //选取图片的回调
        if (requestCode == REQUEST_CODE_BACKGROUND_IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            if (selectedImage == null) {
                return;
            }

            String[] filePathColumns = {MediaStore.Images.Media.DATA};
            Cursor c = getActivity().getContentResolver()
                    .query(selectedImage, filePathColumns, null, null, null);
            if (c == null) {
                return;
            }

            c.moveToFirst();
            int columnIndex = c.getColumnIndex(filePathColumns[0]);
            String imagePath = c.getString(columnIndex);

            //发送Intent给FloatingBallService
            Bundle bundle = new Bundle();
            bundle.putInt(EXTRAS_COMMAND, FloatingBallService.TYPE_IMAGE_PATH);
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
    }

    private void initContentViews() {
        opacitySeekBar.setProgress(BallSettingRepo.opacity());
        sizeSeekBar.setProgress(BallSettingRepo.size());
        amountTextView.setText(String.valueOf(BallSettingRepo.amount()));
        backgroundSwitch.setChecked(BallSettingRepo.isUseBackground());
        useGrayBackgroundSwitch.setChecked(BallSettingRepo.isUseGrayBackground());
        vibrateSwitch.setChecked(BallSettingRepo.isVibrate());
        avoidKeyboardSwitch.setChecked(BallSettingRepo.isAvoidKeyboard());
        isRotateHideSwitch.setChecked(BallSettingRepo.isRotateHideSetting());
        upDistanceSeekBar.setProgress(BallSettingRepo.moveUpDistance());

        updateFunctionListView();
        updateOpacityModeView();

        //        boolean hasAddedBall = SharedPrefsUtils.getBooleanPreference(PREF_IS_ADDED_BALL_IN_SETTING, false);
        //        //hasAddedBall代表两种状态
        //        refreshViews(hasAddedBall);

        opacitySeekBar.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {
            @Override
            public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
                BallSettingRepo.setOpacity(value);
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
                BallSettingRepo.setSize(value);
            }

            @Override
            public void onStartTrackingTouch(DiscreteSeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(DiscreteSeekBar seekBar) {
            }
        });

        plusButton.setOnClickListener(v -> {
            BallSettingRepo.setAmount(BallSettingRepo.amount() + 1);

            //动态变化的需要通过intent ShardPref无法区分是增还是减。
            Intent intent = new Intent(getActivity(), FloatingBallService.class);
            Bundle data = new Bundle();
            data.putInt(EXTRAS_COMMAND, FloatingBallService.TYPE_ADD);
            intent.putExtras(data);
            getActivity().startService(intent);

            amountTextView.setText(String.valueOf(BallSettingRepo.amount()));
            refreshMinusButton();
        });

        refreshMinusButton();
        minusButton.setOnClickListener(v -> {
            int amount = BallSettingRepo.amount();

            if (amount >= 2) {
                BallSettingRepo.setAmount(amount - 1);
                Intent intent = new Intent(getActivity(), FloatingBallService.class);

                Bundle data = new Bundle();
                data.putInt(EXTRAS_COMMAND, FloatingBallService.TYPE_REMOVE_LAST);
                intent.putExtras(data);
                getActivity().startService(intent);

                amountTextView.setText(String.valueOf(BallSettingRepo.amount()));
            }

            refreshMinusButton();
        });

        backgroundSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> SharedPrefsUtils.setBooleanPreference(PREF_USE_BACKGROUND, isChecked));

        choosePicButton.setOnClickListener(v -> {
            //检查权限 请求权限 选图片
            requestStoragePermission();

            Intent intent = new Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, REQUEST_CODE_BACKGROUND_IMAGE);//onActivityResult
        });

        useGrayBackgroundSwitch
                .setOnCheckedChangeListener((buttonView, isChecked) -> BallSettingRepo.setIsUseGrayBackground(isChecked));

        isRotateHideSwitch.setOnCheckedChangeListener((compoundButton, isChecked) -> BallSettingRepo.setIsRotateHideSetting(isChecked));

        vibrateSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> BallSettingRepo.setIsVibrate(isChecked));

        avoidKeyboardSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> BallSettingRepo.setIsAvoidKeyboard(isChecked));

        upDistanceSeekBar.setOnProgressChangeListener(new OnProgressChangeListener() {
            @Override
            public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
                BallSettingRepo.setMoveUpDistance(value);
            }

            @Override
            public void onStartTrackingTouch(DiscreteSeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(DiscreteSeekBar seekBar) {

            }
        });
    }

    private void refreshMinusButton() {
        int amountInit = BallSettingRepo.amount();
        if (amountInit == 1) {
            minusButton.setEnabled(false);
        } else {
            minusButton.setEnabled(true);
        }
    }

    private void updateFunctionListView() {
        String[] functionList = getResources().getStringArray(R.array.function_array);

        int singleTapEvent = BallSettingRepo.singleTapEvent();
        singleTapTextView.setText(functionList[singleTapEvent]);

        int doubleClickEvent = BallSettingRepo.doubleClickEvent();
        doubleClickTextView.setText(functionList[doubleClickEvent]);

        int rightSlideEvent = BallSettingRepo.rightSlideEvent();
        rightSlideTextView.setText(functionList[rightSlideEvent]);

        int leftSlideEvent = BallSettingRepo.leftSlideEvent();
        leftSlideTextView.setText(functionList[leftSlideEvent]);

        int upSlideEvent = BallSettingRepo.upSlideEvent();
        upSlideTextView.setText(functionList[upSlideEvent]);

        int downSlideEvent = BallSettingRepo.downSlideEvent();
        downSlideTextView.setText(functionList[downSlideEvent]);
    }

    private void updateOpacityModeView() {
        Resources res = getResources();
        String[] opacityModeList = res.getStringArray(R.array.opacity_mode);

        int opacityMode = SharedPrefsUtils.getIntegerPreference(PREF_OPACITY_MODE, OPACITY_NONE);
        opacityModeTextView.setText(opacityModeList[opacityMode]);
    }

    public void refreshViews(boolean hasAddedBall) {
        if (hasAddedBall) {
            enableAll(rootLinearLayout, true);
        } else {
            enableAll(rootLinearLayout, false);
        }
    }

    private void enableAll(View root, boolean enabled) {
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
            } else if (top != null) { //如果栈顶为View类型，输出
                top.setEnabled(enabled);
            }
        }
    }

    /**
     * 不再对悬浮球进行设置，悬浮球可以清不需要的模块的内存。
     */
    private void sendClearIntentToService() {
        Intent intent = new Intent(getActivity(), FloatingBallService.class);
        Bundle data = new Bundle();
        data.putInt(EXTRAS_COMMAND, FloatingBallService.TYPE_CLEAR);
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
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    @OnClick({
            R.id.single_tap_function,
            R.id.double_click_function,
            R.id.left_function,
            R.id.right_function,
            R.id.up_function,
            R.id.down_function
    })
    public void onFunctionClicked(View view) {
        if (view.getId() == R.id.double_click_function) {
            showFunctionDialog(R.string.double_click_title, PREF_DOUBLE_CLICK_EVENT);
        } else if (view.getId() == R.id.left_function) {
            showFunctionDialog(R.string.left_swipe_title, PREF_LEFT_SWIPE_EVENT);
        } else if (view.getId() == R.id.right_function) {
            showFunctionDialog(R.string.right_swipe_title, PREF_RIGHT_SWIPE_EVENT);
        } else if (view.getId() == R.id.up_function) {
            showFunctionDialog(R.string.up_swipe_title, PREF_UP_SWIPE_EVENT);
        } else if (view.getId() == R.id.down_function) {
            showFunctionDialog(R.string.down_swipe_title, PREF_DOWN_SWIPE_EVENT);
        } else if (view.getId() == R.id.single_tap_function) {
            showFunctionDialog(R.string.single_tap_title, PREF_SINGLE_TAP_EVENT);
        }
    }

    private void showFunctionDialog(int titleId, final String prefKey) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(titleId)
                .setItems(R.array.function_array, (dialog, which) -> {
                    SharedPrefsUtils.setIntegerPreference(prefKey, which);
                    updateFunctionListView();
                }).show();
    }

    @OnClick(R.id.opacity_mode_relativeLayout)
    public void onOpacityModeClicked(View view) {
        //showOpacityModeDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.opacity_mode)
                .setItems(R.array.opacity_mode, (dialog, which) -> {
                    SharedPrefsUtils.setIntegerPreference(PREF_OPACITY_MODE, which);
                    updateOpacityModeView();
                }).show();
    }
    @OnClick(R.id.style_relativeLayout)
    public void onStyleClicked(View view) {
        //showOpacityModeDialog
//        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//        builder.setTitle(R.string.ballStyle)
//                .setItems(R.array.opacity_mode, (dialog, which) -> {
//                    SharedPrefsUtils.setIntegerPreference(PREF_OPACITY_MODE, which);
//                    updateOpacityModeView();
//                }).show();
        BallSettingRepo.setThemeMode(0);

    }
}
