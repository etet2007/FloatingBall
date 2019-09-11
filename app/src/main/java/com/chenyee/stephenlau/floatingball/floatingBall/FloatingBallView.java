package com.chenyee.stephenlau.floatingball.floatingBall;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.support.annotation.Keep;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

import com.chenyee.stephenlau.floatingball.App;
import com.chenyee.stephenlau.floatingball.floatingBall.base.BallAnimator;
import com.chenyee.stephenlau.floatingball.floatingBall.base.BallDrawer;
import com.chenyee.stephenlau.floatingball.floatingBall.gesture.GestureProcessor;
import com.chenyee.stephenlau.floatingball.floatingBall.styleFlyme.FloatingBallAnimator;
import com.chenyee.stephenlau.floatingball.floatingBall.styleFlyme.FloatingBallDrawer;
import com.chenyee.stephenlau.floatingball.floatingBall.styleFlyme.FloatingBallEventListener;
import com.chenyee.stephenlau.floatingball.floatingBall.styleGradient.GradientBallAnimator;
import com.chenyee.stephenlau.floatingball.floatingBall.styleGradient.GradientBallDrawer;
import com.chenyee.stephenlau.floatingball.floatingBall.styleGradient.GradientBallEventListener;
import com.chenyee.stephenlau.floatingball.repository.BallSettingRepo;
import com.chenyee.stephenlau.floatingball.util.FunctionInterfaceUtils;
import com.chenyee.stephenlau.floatingball.util.InputMethodDetector;

import static com.chenyee.stephenlau.floatingball.App.getApplication;
import static com.chenyee.stephenlau.floatingball.util.DimensionUtils.dip2px;
import static com.chenyee.stephenlau.floatingball.util.DimensionUtils.gScreenHeight;
import static com.chenyee.stephenlau.floatingball.util.StaticStringUtil.FLYME;
import static com.chenyee.stephenlau.floatingball.util.StaticStringUtil.NONE;
import static com.chenyee.stephenlau.floatingball.util.StaticStringUtil.OPACITY_BREATHING;
import static com.chenyee.stephenlau.floatingball.util.StaticStringUtil.OPACITY_NONE;
import static com.chenyee.stephenlau.floatingball.util.StaticStringUtil.OPACITY_REDUCE;
import static com.chenyee.stephenlau.floatingball.util.StaticStringUtil.PLANTE;

/**
 * Created by stephenlau on 2017/12/5.
 */
@Keep
public class FloatingBallView extends View {
    private static final String TAG = FloatingBallView.class.getSimpleName();

    //function list
    public FunctionListener singleTapFunctionListener = () -> {
    };
    public FunctionListener doubleTapFunctionListener = () -> {
    };
    public FunctionListener downFunctionListener = () -> {
    };
    public FunctionListener upFunctionListener = () -> {
    };
    public FunctionListener leftFunctionListener = () -> {
    };
    public FunctionListener rightFunctionListener = () -> {
    };

    //Draw
    public BallDrawer ballDrawer;
    public BallAnimator ballAnimator;
    public int userSetOpacity = 125;
    public int opacityMode;
    public int lastLayoutParamsY;
    public int keyboardTopY;
    public int moveUpDistance;
    public boolean isBallMoveUp = false;
    public boolean isKeyboardShow = false;
    //Interact
    private GestureProcessor gestureProcessor;
    //ballView的Id
    private int idCode;
    private WindowManager windowManager;
    private WindowManager.LayoutParams ballViewLayoutParams;
    private int layoutParamsY;//用于使用反射

    /**
     * 构造函数
     */
    public FloatingBallView(Context context, int idCode) {
        super(context);
        this.idCode = idCode;

        init();
        setTheme(BallSettingRepo.themeMode());
    }

    /**
     * 用于在xml中测试
     *
     * @param context
     * @param attrs
     */
    public FloatingBallView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();

        setTheme(0);
        ((FloatingBallDrawer) ballDrawer).setUseBackgroundImage(false);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        setOpacity(90);
        setOpacityMode(NONE);
        changeFloatBallSizeWithRadius(dip2px(getApplication(), 10));
    }

    public FloatingBallView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs);
    }

    private void init() {
        windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        gestureProcessor = new GestureProcessor(this);
        ViewAnimator.performAddAnimator(this);
    }

    public int getIdCode() {
        return idCode;
    }

    public LayoutParams getBallViewLayoutParams() {
        return ballViewLayoutParams;
    }

    public void setBallViewLayoutParams(LayoutParams ballViewLayoutParams) {
        this.ballViewLayoutParams = ballViewLayoutParams;
    }

    public int getLayoutParamsY() {
        return ballViewLayoutParams.y;
    }

    public void setLayoutParamsY(int y) {
        ballViewLayoutParams.y = y;
        windowManager.updateViewLayout(FloatingBallView.this, ballViewLayoutParams);
    }

    public void setUseBackgroundImage(boolean useBackgroundImage) {
        if (ballDrawer instanceof FloatingBallDrawer) {
            ((FloatingBallDrawer) ballDrawer).setUseBackgroundImage(useBackgroundImage);
            FloatingBallDrawer.BackgroundImageHelper.setUseBackgroundImage(useBackgroundImage);
        } else {
            FloatingBallDrawer.BackgroundImageHelper.setUseBackgroundImage(false);
        }
    }

    /**
     * 设置透明度的模式，设置完后需要立刻刷新起效。
     */
    public void setOpacityMode(int mOpacityMode) {
        this.opacityMode = mOpacityMode;
        refreshOpacityMode();
    }

    private void refreshOpacityMode() {
        if (opacityMode == OPACITY_NONE) {
            ballDrawer.setPaintAlpha(userSetOpacity);
        }

        if (opacityMode == OPACITY_REDUCE) {
            ballAnimator.setUpReduceAnimator(userSetOpacity);
            ballAnimator.startReduceOpacityAnimator();
        }

        if (opacityMode == OPACITY_BREATHING) {
            ballAnimator.setUpBreathingAnimator(userSetOpacity);
            ballAnimator.startBreathingAnimator();
        } else {
            ballAnimator.cancelBreathingAnimator();
        }
    }

    public void setOpacity(int opacity) {
        ballDrawer.setPaintAlpha(opacity);
        userSetOpacity = opacity;

        refreshOpacityMode();
    }

    public void setDoubleClickEventType(int doubleClickEventType) {
        doubleTapFunctionListener = FunctionInterfaceUtils.getListener(doubleClickEventType);

        boolean isUseDoubleClick = doubleTapFunctionListener != FunctionInterfaceUtils.getListener(NONE);
        gestureProcessor.setUseDoubleClick(isUseDoubleClick);
    }

    public void setDownFunctionListener(int downFunctionListenerType) {
        this.downFunctionListener = FunctionInterfaceUtils.getListener(downFunctionListenerType);
    }

    public void setUpFunctionListener(int upFunctionListenerType) {
        this.upFunctionListener = FunctionInterfaceUtils.getListener(upFunctionListenerType);
    }

    public void setLeftFunctionListener(int leftFunctionListenerType) {
        this.leftFunctionListener = FunctionInterfaceUtils.getListener(leftFunctionListenerType);
    }

    public void setRightFunctionListener(int rightFunctionListenerType) {
        this.rightFunctionListener = FunctionInterfaceUtils.getListener(rightFunctionListenerType);
    }

    public void setSingleTapFunctionListener(int singleTapFunctionListenerType) {
        this.singleTapFunctionListener = FunctionInterfaceUtils.getListener(singleTapFunctionListenerType);
    }

    public int getMeasureLength() {
        if (ballDrawer != null) {
            return ballDrawer.getMeasuredSideLength();
        } else {
            return 0;
        }
    }

    public void setLayoutPositionParamsAndSave(int x, int y) {
        if (windowManager != null) {
            ballViewLayoutParams.x = x;
            ballViewLayoutParams.y = y;

            windowManager.updateViewLayout(FloatingBallView.this, ballViewLayoutParams);

            //不是很耗性能，直接保存。当然也可以长按结束的时候再进行保存。
            saveLayoutParams();
        }
    }

    private void saveLayoutParams() {
        if (idCode < 0) {
            return;
        }
        Configuration configuration = App.getApplication().getResources().getConfiguration(); //获取设置的配置信息
        if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            BallSettingRepo.setFloatingBallLandscapeX(ballViewLayoutParams.x, idCode);
            BallSettingRepo.setFloatingBallLandscapeY(ballViewLayoutParams.y, idCode);
        } else if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            BallSettingRepo.setFloatingBallPortraitX(ballViewLayoutParams.x, idCode);
            BallSettingRepo.setFloatingBallPortraitY(ballViewLayoutParams.y, idCode);
        }
    }

    /**
     * 取数据更新
     */
    public void updateLayoutParamsWithOrientation() {
        if (idCode < 0) {
            return;
        }
        if (windowManager != null) {

            Configuration configuration = App.getApplication().getResources().getConfiguration();
            int orientation = configuration.orientation;

            int x = 0, y = 0;
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                x = BallSettingRepo.floatingBallLandscapeX(idCode);
                y = BallSettingRepo.floatingBallLandscapeY(idCode);

            } else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                x = BallSettingRepo.floatingBallPortraitX(idCode);
                y = BallSettingRepo.floatingBallPortraitY(idCode);
            }

            ballViewLayoutParams.x = x;
            ballViewLayoutParams.y = y;
            windowManager.updateViewLayout(FloatingBallView.this, ballViewLayoutParams);
        }
    }
    public float ballRadius;

    /**
     * 改变悬浮球大小，需要改变所有与Size相关的东西
     */
    public void changeFloatBallSizeWithRadius(float ballRadius) {
        this.ballRadius = ballRadius;

        ballDrawer.calculateBackgroundRadiusAndMeasureSideLength(ballRadius);
        ballAnimator.setUpTouchAnimator(ballRadius);
    }

    public void removeBallWithAnimation() {
        ViewAnimator.performRemoveAnimatorWithEndAction(this, this::removeBallWithoutAnimation);
    }

    private void removeBallWithoutAnimation() {
        if (windowManager != null) {
            windowManager.removeViewImmediate(this);
        }
    }

    /**
     * 移动到键盘顶部，移动前记录
     */
    public void moveToKeyboardTop() {
        isKeyboardShow = true;

        // 计算键盘顶部Y坐标
        keyboardTopY = gScreenHeight - InputMethodDetector.inputMethodWindowHeight;

        int ballBottomYPlusGap = getLayoutParamsY() + getMeasureLength() + moveUpDistance;

        if (ballBottomYPlusGap >= keyboardTopY) {//球在键盘下方
            lastLayoutParamsY = getLayoutParamsY();

            ballAnimator.startParamsYAnimationTo(keyboardTopY - getMeasureLength() - moveUpDistance);
            isBallMoveUp = true;
        }
    }

    public void moveBackWhenKeyboardDisappear() {
        isKeyboardShow = false;

        if (isBallMoveUp) {
            isBallMoveUp = false;

            ballAnimator.startParamsYAnimationTo(lastLayoutParamsY);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        ballDrawer.drawBallWithThisModel(canvas);
    }

    /**
     * 布局，改变View的大小
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(getMeasureLength(), getMeasureLength());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureProcessor.onTouchEvent(event);
        return true;
    }

    public void updateModelData() {
        ballDrawer.updateFieldBySingleDataManager();
        gestureProcessor.updateFieldBySingleDataManager();
        moveUpDistance = BallSettingRepo.moveUpDistance();
    }

    /**
     * 设置完主题后需要重新设置大小和透明度
     * 整个类的替换导致父类共有的数据丢失
     * @param themeMode
     */
    public void setTheme(int themeMode) {
        if (themeMode == FLYME) {
            ballDrawer = new FloatingBallDrawer(this);
            ballAnimator = new FloatingBallAnimator(this, (FloatingBallDrawer) ballDrawer);
            gestureProcessor.setOnGestureEventListener(new FloatingBallEventListener(this));
        } else if (themeMode == PLANTE) {
            ballDrawer = new GradientBallDrawer(this);
            ballAnimator = new GradientBallAnimator(this, (GradientBallDrawer) ballDrawer);
            gestureProcessor.setOnGestureEventListener(new GradientBallEventListener(this));
        }

        ballDrawer.calculateBackgroundRadiusAndMeasureSideLength(ballRadius);
        ballAnimator.setUpTouchAnimator(ballRadius);

        invalidate();
    }
}
