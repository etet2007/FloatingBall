package com.chenyee.stephenlau.floatingball.floatingBall;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

import com.chenyee.stephenlau.floatingball.App;
import com.chenyee.stephenlau.floatingball.floatingBall.styleFlyme.FloatingBallDrawer;
import com.chenyee.stephenlau.floatingball.repository.BallSettingRepo;
import com.chenyee.stephenlau.floatingball.util.BitmapUtils;
import com.chenyee.stephenlau.floatingball.util.InputMethodDetector;
import com.chenyee.stephenlau.floatingball.util.SharedPrefsUtils;

import java.util.ArrayList;

import static com.chenyee.stephenlau.floatingball.util.StaticStringUtil.OPACITY_NONE;
import static com.chenyee.stephenlau.floatingball.util.StaticStringUtil.PREF_BALL_THEME;
import static com.chenyee.stephenlau.floatingball.util.StaticStringUtil.PREF_DOUBLE_CLICK_EVENT;
import static com.chenyee.stephenlau.floatingball.util.StaticStringUtil.PREF_DOWN_SWIPE_EVENT;
import static com.chenyee.stephenlau.floatingball.util.StaticStringUtil.PREF_IS_VIBRATE;
import static com.chenyee.stephenlau.floatingball.util.StaticStringUtil.PREF_LEFT_SWIPE_EVENT;
import static com.chenyee.stephenlau.floatingball.util.StaticStringUtil.PREF_MOVE_UP_DISTANCE;
import static com.chenyee.stephenlau.floatingball.util.StaticStringUtil.PREF_OPACITY;
import static com.chenyee.stephenlau.floatingball.util.StaticStringUtil.PREF_OPACITY_MODE;
import static com.chenyee.stephenlau.floatingball.util.StaticStringUtil.PREF_RIGHT_SWIPE_EVENT;
import static com.chenyee.stephenlau.floatingball.util.StaticStringUtil.PREF_SINGLE_TAP_EVENT;
import static com.chenyee.stephenlau.floatingball.util.StaticStringUtil.PREF_SIZE;
import static com.chenyee.stephenlau.floatingball.util.StaticStringUtil.PREF_UP_SWIPE_EVENT;
import static com.chenyee.stephenlau.floatingball.util.StaticStringUtil.PREF_USE_BACKGROUND;
import static com.chenyee.stephenlau.floatingball.util.StaticStringUtil.PREF_USE_GRAY_BACKGROUND;

/**
 * 管理FloatingBall的单例
 */
public class FloatingBallController {
    private static final String TAG = "FloatingBallController";

    //Singleton
    private static FloatingBallController sFloatingBallController = new FloatingBallController();

    private ArrayList<FloatingBallView> floatingBallViewList = new ArrayList<>();

    //内存中变量
    private boolean isStartedBallView = false;
    private boolean isSoftKeyboardShow = false;
    private boolean isHideBecauseRotate = false;

    private WindowManager windowManager = (WindowManager) App.getApplication().getApplicationContext()
            .getSystemService(Context.WINDOW_SERVICE);

    private FloatingBallController() {
    }

    public static FloatingBallController getInstance() {
        return sFloatingBallController;
    }

    private SharedPreferences.OnSharedPreferenceChangeListener mOnSharedPreferenceChangeListener = (sharedPreferences, key) -> {
        Log.d(TAG, "onSharedPreferenceChanged: ");
        updateSpecificParameter(key);
    };

    public void registerOnDataChangeListener() {
        SharedPrefsUtils.getSharedPreferences().registerOnSharedPreferenceChangeListener(mOnSharedPreferenceChangeListener);
    }

    public void unregisterOnDataChangeListener() {
        SharedPrefsUtils.getSharedPreferences().unregisterOnSharedPreferenceChangeListener(mOnSharedPreferenceChangeListener);
    }

    public boolean isHideBecauseRotate() {
        return isHideBecauseRotate;
    }

    /**
     * new floatingBallView and add to WindowManager
     */
    public void startBallView(Context context) {
        if (isStartedBallView) {//已经开启
            return;
        }
        isStartedBallView = true;

        int amount = BallSettingRepo.amount();
        for (int id = 0; id < amount; id++) {
            addFloatingBallView(context, id);
        }
        updateViewsParameter();

        //        辅助设置里打开，等于在设置中打开。
        BallSettingRepo.setIsAddedBallInSetting(true);
    }

    public void addFloatingBallView(Context context, int id) {

        FloatingBallView floatingBallView = new FloatingBallView(context, id);
        floatingBallViewList.add(floatingBallView);

        // use code to initialize layout parameters
        LayoutParams params = new LayoutParams();
        params.width = LayoutParams.WRAP_CONTENT;
        params.height = LayoutParams.WRAP_CONTENT;
        params.gravity = Gravity.START | Gravity.TOP;
        params.format = PixelFormat.RGBA_8888;//默认为不透明 会有黑色背景
        //窗口类型
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //https://developer.android.com/about/versions/oreo/android-8.0-changes?hl=zh-cn#all-aw
            params.type = LayoutParams.TYPE_APPLICATION_OVERLAY; //适配Android 8.0
        } else {
            params.type = LayoutParams.TYPE_SYSTEM_ALERT;
        }
        params.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL
                | LayoutParams.FLAG_NOT_FOCUSABLE
                //                | LayoutParams.FLAG_LAYOUT_NO_LIMITS;//没用
                | LayoutParams.FLAG_LAYOUT_IN_SCREEN
                | LayoutParams.FLAG_LAYOUT_INSET_DECOR;
        //FLAG_LAYOUT_NO_LIMITS 没有限制绘制的区域
        //FLAG_LAYOUT_IN_SCREEN 将window放置在整个屏幕之内,无视其他的装饰(比如状态栏)window要在考虑到屏幕的其他装饰来定位其中的内容
        //FLAG_LAYOUT_INSET_DECOR .当在屏幕中请求layout时,window可能在一些装饰物(如状态栏)之上或者之后当使用这个flag时,window manager会报告插入window的矩形大小,来确保你的内容不会被装饰物(如状态栏)掩盖..
        floatingBallView.setBallViewLayoutParams(params);
        windowManager.addView(floatingBallView, params);

        updateViewsParameter();
    }

    /**
     * 开关中的关闭
     */
    public void removeBallView() {
        BallSettingRepo.setIsAddedBallInSetting(false);
        removeAll();
    }

    /**
     * 旋转屏幕时隐藏
     */
    public void hideWhenRotate() {
        //    BallSettingRepo.setIsBallHideBecauseRotate(true);
        isHideBecauseRotate = true;
        removeAll();
    }

    /**
     * 旋转屏幕后出现
     */
    public void startWhenRotateBack(Context context) {
        startBallView(context);
        //    BallSettingRepo.setIsBallHideBecauseRotate(false);
        isHideBecauseRotate = false;
    }

    //暂时与屏幕旋转隐藏冲突！
    private void hideWhenKeyboardShow() {
        removeAll();
    }

    public void removeLastFloatingBall() {
        if (floatingBallViewList.size() > 1) {//最小数量为1
            FloatingBallView floatingBallView = floatingBallViewList.get(floatingBallViewList.size() - 1);
            floatingBallView.removeBallWithAnimation();
            floatingBallViewList.remove(floatingBallView);
        }
    }

    private void removeAll() {
        isStartedBallView = false;
        for (FloatingBallView floatingBallView : floatingBallViewList) {
            floatingBallView.removeBallWithAnimation();
        }
        floatingBallViewList.clear();
    }

    /**
     * 设置背景图 复制外部路径的图片到目录中去，更新bitmapRead，再进行裁剪
     *
     * @param imagePath 外部图片地址
     */
    public void setBackgroundImage(String imagePath) {
        BitmapUtils.copyBackgroundImageToAppFolder(imagePath);

        FloatingBallDrawer.BackgroundImageHelper.setupBitmapRead();
        FloatingBallDrawer.BackgroundImageHelper.createBitmapCropFromBitmapRead();

        for (FloatingBallView floatingBallView : floatingBallViewList) {
            floatingBallView.invalidate();
        }
    }

    private void updateSpecificParameter(String key) {
        for (FloatingBallView floatingBallView : floatingBallViewList) {
            switch (key) {
                case PREF_OPACITY:
                    floatingBallView.setOpacity(BallSettingRepo.opacity());
                    break;
                case PREF_OPACITY_MODE:
                    floatingBallView.setOpacityMode(SharedPrefsUtils.getIntegerPreference(PREF_OPACITY_MODE, OPACITY_NONE));
                    break;
                case PREF_SIZE:
                    floatingBallView.changeFloatBallSizeWithRadius(BallSettingRepo.size());
                    break;
                case PREF_USE_BACKGROUND:
                    floatingBallView.setUseBackgroundImage(BallSettingRepo.isUseBackground());
                    break;
                case PREF_BALL_THEME:
                    floatingBallView.setTheme(BallSettingRepo.themeMode());
                    updateSingleBallView(floatingBallView);
                    break;
                case PREF_USE_GRAY_BACKGROUND:
                case PREF_IS_VIBRATE:
                case PREF_MOVE_UP_DISTANCE:
                    floatingBallView.updateModelData();
                    break;
            }
            //refresh the layout and draw the view
            floatingBallView.requestLayout();
            floatingBallView.invalidate();

            switch (key) {
                case PREF_DOUBLE_CLICK_EVENT:
                    floatingBallView.setDoubleClickEventType(BallSettingRepo.doubleClickEvent());
                    break;
                case PREF_LEFT_SWIPE_EVENT:
                    floatingBallView.setLeftFunctionListener(BallSettingRepo.leftSlideEvent());
                    break;
                case PREF_RIGHT_SWIPE_EVENT:
                    floatingBallView.setRightFunctionListener(BallSettingRepo.rightSlideEvent());
                    break;
                case PREF_UP_SWIPE_EVENT:
                    floatingBallView.setUpFunctionListener(BallSettingRepo.upSlideEvent());
                    break;
                case PREF_DOWN_SWIPE_EVENT:
                    floatingBallView.setDownFunctionListener(BallSettingRepo.downSlideEvent());
                    break;
                case PREF_SINGLE_TAP_EVENT:
                    floatingBallView.setSingleTapFunctionListener(BallSettingRepo.singleTapEvent());
                    break;
            }
        }
    }

    /**
     * Use sharedPreference data to update all parameter
     */
    private void updateViewsParameter() {
        for (FloatingBallView floatingBallView : floatingBallViewList) {

            updateSingleBallView(floatingBallView);
        }
    }

    private void updateSingleBallView(FloatingBallView floatingBallView) {
        floatingBallView.updateLayoutParamsWithOrientation();
        /* View */
        floatingBallView.setOpacity(BallSettingRepo.opacity());
        floatingBallView.setOpacityMode(SharedPrefsUtils.getIntegerPreference(PREF_OPACITY_MODE, OPACITY_NONE));
        floatingBallView.changeFloatBallSizeWithRadius(BallSettingRepo.size());
        floatingBallView.setUseBackgroundImage(BallSettingRepo.isUseBackground());

        floatingBallView.updateModelData();

        floatingBallView.requestLayout();
        floatingBallView.invalidate();

        /* Function */
        floatingBallView.setDoubleClickEventType(BallSettingRepo.doubleClickEvent());
        floatingBallView.setLeftFunctionListener(BallSettingRepo.leftSlideEvent());
        floatingBallView.setRightFunctionListener(BallSettingRepo.rightSlideEvent());
        floatingBallView.setUpFunctionListener(BallSettingRepo.upSlideEvent());
        floatingBallView.setDownFunctionListener(BallSettingRepo.downSlideEvent());
        floatingBallView.setSingleTapFunctionListener(BallSettingRepo.singleTapEvent());
    }

    /**
     * According to the state of input method, move the floatingBall view.
     */
    public void inputMethodDetect(Context context) {
        if (InputMethodDetector.detectIsInputingWithHeight(100)) {
            onKeyboardShow();
        } else {
            onKeyboardDisappear(context);
        }
    }

    /**
     * 键盘检查，可能引起隐藏或躲避
     */
    private void onKeyboardShow() {
        if (!isSoftKeyboardShow) {

            if (BallSettingRepo.isHideWhenKeyboardShow()) {
                hideWhenKeyboardShow();
            } else if (BallSettingRepo.isAvoidKeyboard()) {
                for (FloatingBallView floatingBallView : floatingBallViewList) {
                    floatingBallView.moveToKeyboardTop();
                }
            }

        }

        isSoftKeyboardShow = true;
    }

    private void onKeyboardDisappear(Context context) {
        if (isSoftKeyboardShow) {

            if (BallSettingRepo.isHideWhenKeyboardShow()) {
                if (!isHideBecauseRotate) {
                    startBallView(context);
                }
            } else if (BallSettingRepo.isAvoidKeyboard()) {
                for (FloatingBallView floatingBallView : floatingBallViewList) {
                    floatingBallView.moveBackWhenKeyboardDisappear();
                }
            }

        }
        isSoftKeyboardShow = false;
    }

    public void recycleBitmapMemory() {
        FloatingBallDrawer.BackgroundImageHelper.recycleBitmapRead();
    }

    /**
     * screenshot时暂时隐藏
     */
    public void setBallViewIsHide(boolean isHide) {
        for (FloatingBallView floatingBallView : floatingBallViewList) {
            floatingBallView.setVisibility(isHide ? View.INVISIBLE : View.VISIBLE);
            floatingBallView.invalidate();
        }
    }


    public void updateBallViewLayout() {
        for (FloatingBallView floatingBallView : floatingBallViewList) {
            floatingBallView.updateLayoutParamsWithOrientation();
        }
    }

    public void postRunnable(Runnable r) {
        if (!floatingBallViewList.isEmpty()) {
            View view = floatingBallViewList.get(0);
            view.post(r);
        }
    }
}
