package com.chenyee.stephenlau.floatingball.floatingBall;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.Keep;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

import com.chenyee.stephenlau.floatingball.App;
import com.chenyee.stephenlau.floatingball.floatingBall.gesture.FloatingBallGestureProcessor;
import com.chenyee.stephenlau.floatingball.floatingBall.gesture.OnGestureEventListener;
import com.chenyee.stephenlau.floatingball.repository.BallSettingRepo;
import com.chenyee.stephenlau.floatingball.util.FunctionInterfaceUtils;
import com.chenyee.stephenlau.floatingball.util.InputMethodDetector;

import static com.chenyee.stephenlau.floatingball.util.DimensionUtils.gScreenHeight;
import static com.chenyee.stephenlau.floatingball.util.StaticStringUtil.NONE;
import static com.chenyee.stephenlau.floatingball.util.StaticStringUtil.OPACITY_BREATHING;
import static com.chenyee.stephenlau.floatingball.util.StaticStringUtil.OPACITY_NONE;
import static com.chenyee.stephenlau.floatingball.util.StaticStringUtil.OPACITY_REDUCE;

/**
 * Created by stephenlau on 2017/12/5.
 */
@Keep
public class FloatingBallView extends View implements OnGestureEventListener {
    private static final String TAG = FloatingBallView.class.getSimpleName();

    //function list
    private FunctionListener singleTapFunctionListener;
    private FunctionListener doubleTapFunctionListener;
    private FunctionListener downFunctionListener;
    private FunctionListener upFunctionListener;
    private FunctionListener leftFunctionListener;
    private FunctionListener rightFunctionListener;

    //Draw
    private FloatingBallPaint floatingBallPaint;
    private FloatingBallDrawer floatingBallDrawer;
    private FloatingBallAnimator floatingBallAnimator;
    //Interact
    private FloatingBallGestureProcessor floatingBallGestureProcessor;

    //ballView的Id
    private int idCode;


    private WindowManager windowManager;

    private int userSetOpacity = 125;
    private int opacityMode;
    private WindowManager.LayoutParams ballViewLayoutParams;
    private int lastLayoutParamsY;
    private int layoutParamsY;//用于使用反射
    private int keyboardTopY;
    private int moveUpDistance;
    private boolean isBallMoveUp = false;
    private boolean isKeyboardShow = false;

    /**
     * 构造函数
     */
    public FloatingBallView(Context context, int idCode) {
        super(context);
        this.idCode = idCode;

        init(context);
    }

    /**
     * 用于在xml中测试
     *
     * @param context
     * @param attrs
     */
    public FloatingBallView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context);

        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        setOpacity(100);
        setOpacityMode(NONE);
    }

    public FloatingBallView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs);
    }

    private void init(Context context) {
        floatingBallPaint = new FloatingBallPaint();

        floatingBallDrawer = new FloatingBallDrawer(this, floatingBallPaint);

        floatingBallAnimator = new FloatingBallAnimator(this, floatingBallDrawer);
        floatingBallAnimator.performAddAnimator();

        changeFloatBallSizeWithRadius(50);

        windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);

        floatingBallGestureProcessor = new FloatingBallGestureProcessor(this, this);
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
        floatingBallDrawer.setUseBackgroundImage(useBackgroundImage);
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
            floatingBallPaint.setPaintAlpha(userSetOpacity);
        }

        if (opacityMode == OPACITY_REDUCE) {
            floatingBallAnimator.setUpReduceAnimator(userSetOpacity);
            floatingBallAnimator.startReduceOpacityAnimator();
        }

        if (opacityMode == OPACITY_BREATHING) {
            floatingBallAnimator.setUpBreathingAnimator(userSetOpacity);
            floatingBallAnimator.startBreathingAnimator();
        } else {
            floatingBallAnimator.cancelBreathingAnimator();
        }
    }

    public void setOpacity(int opacity) {
        floatingBallPaint.setPaintAlpha(opacity);
        userSetOpacity = opacity;

        refreshOpacityMode();
    }

    public void setDoubleClickEventType(int doubleClickEventType) {
        doubleTapFunctionListener = FunctionInterfaceUtils.getListener(doubleClickEventType);

        boolean isUseDoubleClick = doubleTapFunctionListener != FunctionInterfaceUtils.getListener(NONE);
        floatingBallGestureProcessor.setUseDoubleClick(isUseDoubleClick);
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

    public FloatingBallDrawer getFloatingBallDrawer() {
        return floatingBallDrawer;
    }

    public FloatingBallPaint getFloatingBallPaint() {
        return floatingBallPaint;
    }

    public int getMeasureLength() {
        if (floatingBallDrawer != null) {
            return floatingBallDrawer.measuredSideLength;
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

    /**
     * 改变悬浮球大小，需要改变所有与Size相关的东西
     */
    public void changeFloatBallSizeWithRadius(int ballRadius) {
        floatingBallDrawer.calculateBackgroundRadiusAndMeasureSideLength(ballRadius);

        floatingBallAnimator.setUpTouchAnimator(ballRadius);
    }


    public void removeBallWithAnimation() {
        floatingBallAnimator.performRemoveAnimatorWithEndAction(new Runnable() {
            @Override
            public void run() {
                removeBallWithoutAnimation();
            }
        });
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

            floatingBallAnimator.startParamsYAnimationTo(keyboardTopY - getMeasureLength() - moveUpDistance);
            isBallMoveUp = true;
        }
    }

    public void moveBackWhenKeyboardDisappear() {
        isKeyboardShow = false;

        if (isBallMoveUp) {
            isBallMoveUp = false;

            floatingBallAnimator.startParamsYAnimationTo(lastLayoutParamsY);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        floatingBallDrawer.drawBallWithThisModel(canvas);

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
        floatingBallGestureProcessor.onTouchEvent(event);
        return true;
    }


    public void updateModelData() {
        floatingBallDrawer.updateFieldBySingleDataManager();
        floatingBallGestureProcessor.updateFieldBySingleDataManager();
        moveUpDistance = BallSettingRepo.moveUpDistance();
    }

    //手势处理的回调函数
    @Override
    public void onActionDown() {
        floatingBallAnimator.startOnTouchAnimator();

        if (opacityMode == OPACITY_REDUCE) {
            floatingBallPaint.setPaintAlpha(userSetOpacity);
        }
    }

    @Override
    public void onActionUp() {
        floatingBallAnimator.startUnTouchAnimator();
        if (opacityMode == OPACITY_REDUCE) {
            floatingBallAnimator.startReduceOpacityAnimator();
        }
    }

    @Override
    public void onMove(int x, int y) {
        setLayoutPositionParamsAndSave(x, y);
    }

    @Override
    public void onSingeTap() {
        singleTapFunctionListener.onFunction();
    }

    @Override
    public void onDoubleTap() {
        doubleTapFunctionListener.onFunction();
    }

    @Override
    public void upGesture() {
        upFunctionListener.onFunction();
    }

    @Override
    public void downGesture() {
        downFunctionListener.onFunction();
    }

    @Override
    public void leftGesture() {
        leftFunctionListener.onFunction();
    }

    @Override
    public void rightGesture() {
        rightFunctionListener.onFunction();
    }

    @Override
    public void onScrollEnd() {
        //球移动动画
        floatingBallAnimator.moveFloatBallBack();
    }

    @Override
    public void onLongPressEnd() {
        if (isKeyboardShow) {

            int ballBottomYPlusGap = getLayoutParamsY() + getMeasureLength() + moveUpDistance;
            if (ballBottomYPlusGap >= keyboardTopY) { //球在键盘下方
                lastLayoutParamsY = getLayoutParamsY();
                floatingBallAnimator.startParamsYAnimationTo(keyboardTopY - getMeasureLength() - moveUpDistance);
            } else {
                isBallMoveUp = false;
            }
        }
    }
}
