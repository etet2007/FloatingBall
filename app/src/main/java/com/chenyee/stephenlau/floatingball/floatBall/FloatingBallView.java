package com.chenyee.stephenlau.floatingball.floatBall;

import android.accessibilityservice.AccessibilityService;
import android.animation.Keyframe;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Vibrator;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.chenyee.stephenlau.floatingball.util.AccessibilityUtil;
import com.chenyee.stephenlau.floatingball.R;
import com.chenyee.stephenlau.floatingball.util.BitmapUtil;
import com.chenyee.stephenlau.floatingball.util.SharedPrefsUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static com.chenyee.stephenlau.floatingball.util.DimensionUtils.dip2px;
import static com.chenyee.stephenlau.floatingball.util.StaticStringUtil.OPACITY_BREATHING;
import static com.chenyee.stephenlau.floatingball.util.StaticStringUtil.OPACITY_NONE;
import static com.chenyee.stephenlau.floatingball.util.StaticStringUtil.PREF_PARAM_X;
import static com.chenyee.stephenlau.floatingball.util.StaticStringUtil.PREF_PARAM_Y;
import static com.chenyee.stephenlau.floatingball.util.StaticStringUtil.OPACITY_REDUCE;

/**
 * Created by stephenlau on 2017/12/5.
 */

public class FloatingBallView extends View {
    private static final String TAG = FloatingBallView.class.getSimpleName();

    private Paint mBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mBallPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mBallEmptyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    //手势的状态
    private static final int UP = 1;
    private static final int DOWN = 2;
    private static final int LEFT = 3;
    private static final int RIGHT = 4;
    private static final int NONE = 5;

    private int currentGestureState;
    private int lastGestureState = NONE;

    //球半径、背景半径
    private final float edge = dip2px(getContext(), 4);
    private final float ballRadiusDeltaMax = 7;
    private final int gestureMoveDistance = 18;

    //白球半径
    private float ballRadius; //不能改名字，用了反射
    //灰色背景半径
    private float mBackgroundRadius ;

    private int measuredSideLength;
    //移动距离 可以设置
    private int moveUpDistance;

    private float ballCenterY = 0;
    private float ballCenterX = 0;

    //function list
    private FunctionListener mDownFunctionListener;
    private FunctionListener mUpFunctionListener;
    private FunctionListener mLeftFunctionListener;
    private FunctionListener mRightFunctionListener;
    private FunctionListener mDoubleTapFunctionListener;

    //标志位
    private boolean isFirstEvent = false;
    private boolean isScrolling = false;
    private boolean isLongPress = false;

    private boolean useBackground = false;
    private boolean useGrayBackground = true;
    private boolean useDoubleClick = false;

    private boolean mIsVibrate =true;

    //上次touchEvent的位置
    private float mLastTouchEventX;
    private float mLastTouchEventY;

    private WindowManager.LayoutParams mLayoutParams;

    private GestureDetector mDetector;
    private AccessibilityService mService;

    private WindowManager mWindowManager;
    //接触时动画
    private ObjectAnimator onTouchAnimate;
    private ObjectAnimator unTouchAnimate;

    //Vibrator
    private Vibrator mVibrator;

    private Bitmap mBitmapRead;
    private Bitmap mBitmapScaled;

    //基础透明度值
    private int mBaseOpacity;
    //透明度模式
    private int mOpacityMode;
    //透明度呼吸动画
    private ObjectAnimator breathingAnimate;
    //透明度减少动画
    private ObjectAnimator reduceAnimate;

    public float getBallCenterY() {
        return ballCenterY;
    }
    public void setBallCenterY(float ballCenterY) {
        this.ballCenterY = ballCenterY;
    }
    public float getBallCenterX() {
        return ballCenterX;
    }
    public void setBallCenterX(float ballCenterX) {
        this.ballCenterX = ballCenterX;
    }
    public void setUseGrayBackground(boolean useGrayBackground) {
        this.useGrayBackground = useGrayBackground;
    }
    public void setUseBackground(boolean useBackground) {
        if (useBackground) {
            refreshBitmapRead();
            createBitmapCropFromBitmapRead();
        } else {
            recycleBitmap();
        }

        this.useBackground = useBackground;
    }

    public void recycleBitmap() {
        if (mBitmapRead != null && !mBitmapRead.isRecycled()) {
            mBitmapRead.recycle();
        }
        if (!useBackground && mBitmapScaled != null && !mBitmapScaled.isRecycled()) {
            mBitmapScaled.recycle();
        }
    }

    public void setDoubleClickEventType(boolean useDoubleClick,FunctionListener doubleTapFunctionListener) {
        this.useDoubleClick = useDoubleClick;
        this.mDoubleTapFunctionListener = doubleTapFunctionListener;

        if (useDoubleClick) {
            mDetector.setOnDoubleTapListener(new DoubleTapGestureListener());
        } else {
            mDetector.setOnDoubleTapListener(null);
        }
    }

    public void setMoveUpDistance(int moveUpDistance) {
        this.moveUpDistance = moveUpDistance;
    }

    private int mLayoutParamsY;//动画用到
    private int getMLayoutParamsY(){
        return mLayoutParams.y;
    }
    private void setMLayoutParamsY(int y){
        mLayoutParams.y=y;
    }

    public void setLayoutParams(WindowManager.LayoutParams params) {
        mLayoutParams = params;
    }
    public  WindowManager.LayoutParams getLayoutParams() {
        return mLayoutParams;
    }
    public float getBallRadius() {
        return ballRadius;
    }
    public void setBallRadius(float ballRadius) {
        this.ballRadius = ballRadius;
    }
    public float getMBackgroundRadius() {
        return mBackgroundRadius;
    }
    public void setMBackgroundRadius(float mBackgroundRadius) {
        this.mBackgroundRadius = mBackgroundRadius;
    }

    public void setIsVibrate(boolean isVibrate) {
        mIsVibrate = isVibrate;
    }

    /**
     * 设置透明度的模式，设置完后需要立刻刷新起效。
     * @param mOpacityMode
     */
    public void setOpacityMode(int mOpacityMode) {
        this.mOpacityMode = mOpacityMode;
        refreshOpacityMode();
    }

    private void refreshOpacityMode() {
        if (mOpacityMode == OPACITY_NONE) {
            setPaintAlpha(mBaseOpacity);
        }

        if (mOpacityMode == OPACITY_REDUCE) {
            calcReduceAnimation(mBaseOpacity);
            if (reduceAnimate != null) {
                reduceAnimate.start();
            }
        }

        if (mOpacityMode == OPACITY_BREATHING) {
            calcBreathingAnimation(mBaseOpacity);
            breathingAnimate.start();
        } else {
            if (breathingAnimate != null) {
                breathingAnimate.cancel();
            }
        }
    }

    public int getOpacityMode() {
        return mOpacityMode;
    }

    public void setPaintAlpha(int opacity) {
        mBackgroundPaint.setAlpha(opacity);
        mBallPaint.setAlpha(opacity);
    }

    public void setOpacity(int opacity){
        setPaintAlpha(opacity);
        //记录下用户设置的透明度
        mBaseOpacity = opacity;

        refreshOpacityMode();
    }

    /**
     *
     * @param opacity opacity正常值，会自动将为0.6倍
     */
    private void calcReduceAnimation(int opacity) {
        Keyframe kf1 = Keyframe.ofInt(0f, opacity);
        Keyframe kf2 = Keyframe.ofInt(0.5f, opacity);
        Keyframe kf3 = Keyframe.ofInt(1f, (int) (opacity * 0.6));
        PropertyValuesHolder pVH = PropertyValuesHolder.ofKeyframe("paintAlpha", kf1,kf2,kf3);
        reduceAnimate = ObjectAnimator.ofPropertyValuesHolder(this, pVH);
        reduceAnimate.setDuration(3000);
        reduceAnimate.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                invalidate();
            }
        });
    }
    private void calcBreathingAnimation(int opacity){
        Keyframe kf1 = Keyframe.ofInt(0f, (int) (opacity*0.4));
        Keyframe kf2 = Keyframe.ofInt(0.35f, opacity);
        Keyframe kf3 = Keyframe.ofInt(0.5f, opacity);
        Keyframe kf4 = Keyframe.ofInt(0.65f, opacity);
        Keyframe kf5 = Keyframe.ofInt(1f, (int) (opacity*0.4));
        PropertyValuesHolder pVH = PropertyValuesHolder.ofKeyframe("paintAlpha", kf1,kf2,kf3,kf4,kf5);
        breathingAnimate = ObjectAnimator.ofPropertyValuesHolder(this, pVH);
        breathingAnimate.setRepeatCount(ValueAnimator.INFINITE);
        breathingAnimate.setRepeatMode(ValueAnimator.RESTART);
        breathingAnimate.setDuration(4000);
        breathingAnimate.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                invalidate();
            }
        });
    }

    public int getOpacity(){
        return mBackgroundPaint.getAlpha();
    }
    //功能
    public void setDownFunctionListener(FunctionListener downFunctionListener) {
        mDownFunctionListener = downFunctionListener;
    }

    public void setUpFunctionListener(FunctionListener upFunctionListener) {
        mUpFunctionListener = upFunctionListener;
    }

    public void setLeftFunctionListener(FunctionListener leftFunctionListener) {
        mLeftFunctionListener = leftFunctionListener;
    }

    public void setRightFunctionListener(FunctionListener rightFunctionListener) {
        mRightFunctionListener = rightFunctionListener;
    }

    /**
     * 改变悬浮球大小，需要改变所有与Size相关的东西
     * @param ballRadius
     */
    public void changeFloatBallSizeWithRadius(int ballRadius) {
        this.ballRadius = ballRadius;
        this.mBackgroundRadius = ballRadius + edge;
//        final int frameGap = 10;
        //View宽高 r+moveDistance+r在动画变大的值=r+edge+gap
        int frameGap = (int) (gestureMoveDistance + ballRadiusDeltaMax - edge);



        measuredSideLength = (int) (mBackgroundRadius + frameGap) * 2;

        if (useBackground) {
            createBitmapCropFromBitmapRead();
        }
        //动画的参数也需计算
        calcTouchAnimator();
    }

    /**
     * 构造函数
     * @param context
     */
    public FloatingBallView(Context context) {
        super(context);
        changeFloatBallSizeWithRadius(25);

        performAddAnimator();
        mService = (AccessibilityService) context;
        mWindowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);

        mDetector =new GestureDetector(context,new FloatingBallGestureListener());

        mVibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);

        mBackgroundPaint.setColor(Color.GRAY);
        mBackgroundPaint.setAlpha(80);

        mBallPaint.setColor(Color.WHITE);
        mBallPaint.setAlpha(150);

        PorterDuff.Mode mode = PorterDuff.Mode.CLEAR;
        mBallEmptyPaint.setXfermode(new PorterDuffXfermode(mode));

        refreshOpacityMode();

    }

    /**
     * 更新bitmapRead的值
     */
    public void refreshBitmapRead() {
        //path为app内部目录
        String path = getContext().getFilesDir().toString();
        mBitmapRead = BitmapFactory.decodeFile(path+"/ballBackground.png");

        //读取不成功就取默认图片
        if (mBitmapRead == null) {
            Resources res = getResources();
            mBitmapRead = BitmapFactory.decodeResource(res, R.drawable.joe_big);
        }
    }

    /**
     * 复制图片置软件文件夹内。
     * @param imagePath
     */
    public void copyBackgroundImage(String imagePath) {
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
        if (bitmap == null) {
            Toast.makeText(getContext(), "Read image error", Toast.LENGTH_LONG).show();
            return;
        }
        //copy source image
        String path = getContext().getFilesDir().toString();
        BitmapUtil.copyImage(bitmap,path,"ballBackground.png");
        bitmap.recycle();
    }

    public void createBitmapCropFromBitmapRead() {
        if (ballRadius <= 0) {
            return;
        }
        //bitmapRead可能已被回收
        if (mBitmapRead == null || mBitmapRead.isRecycled()) {
            refreshBitmapRead();
        }

        int edge = (int) ballRadius * 2;

        Bitmap scaledBitmap = Bitmap.createScaledBitmap(mBitmapRead, edge, edge, true);
        //进行裁切后的bitmapCrop
        mBitmapScaled = Bitmap.createBitmap(edge, edge, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(mBitmapScaled);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        //x y r
        canvas.drawCircle(ballRadius, ballRadius, ballRadius, paint);
        paint.reset();
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(scaledBitmap, 0, 0, paint);

        scaledBitmap.recycle();
    }

    private void calcTouchAnimator() {
//        onTouchAnimate
        Keyframe kf0 = Keyframe.ofFloat(0f, ballRadius);
        Keyframe kf1 = Keyframe.ofFloat(.7f, ballRadius + ballRadiusDeltaMax - 1);
        Keyframe kf2 = Keyframe.ofFloat(1f, ballRadius + ballRadiusDeltaMax);
        PropertyValuesHolder onTouch = PropertyValuesHolder.ofKeyframe("ballRadius", kf0,kf1,kf2);
        onTouchAnimate = ObjectAnimator.ofPropertyValuesHolder(this, onTouch);
        onTouchAnimate.setDuration(300);
        onTouchAnimate.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                invalidate();
            }
        });

//        unTouchAnimate
        Keyframe kf3 = Keyframe.ofFloat(0f, ballRadius + ballRadiusDeltaMax);
        Keyframe kf4 = Keyframe.ofFloat(0.3f, ballRadius + ballRadiusDeltaMax);
        Keyframe kf5 = Keyframe.ofFloat(1f, ballRadius);
        PropertyValuesHolder unTouch = PropertyValuesHolder.ofKeyframe("ballRadius", kf3,kf4,kf5);
        unTouchAnimate = ObjectAnimator.ofPropertyValuesHolder(this, unTouch);
        unTouchAnimate.setDuration(400);
        unTouchAnimate.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                invalidate();
            }
        });
    }

    private void performAddAnimator() {
        setScaleX(0);
        setScaleY(0);
        animate()
                .scaleY(1).scaleX(1)
                .setDuration(200)
                .start();
    }

    public void performRemoveAnimator() {
        animate()
                .scaleY(0).scaleX(0)
                .setDuration(200)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
                        if (windowManager != null) {
                            windowManager.removeView(FloatingBallView.this);
                        }
                    }
                })
                .start();
    }

    /**
     * 向上移动动画
     */
    public void performMoveUpAnimator() {
        ObjectAnimator animation = ObjectAnimator.ofInt (this, "mLayoutParamsY", getMLayoutParamsY(), getMLayoutParamsY()- moveUpDistance); // see this max value coming back here, we animale towards that value
        animation.setDuration (200); //in milliseconds
        animation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mWindowManager.updateViewLayout(FloatingBallView.this, mLayoutParams);
            }
        });
        animation.start();
    }
    /**
     * 向下移动动画
     */
    public void performMoveDownAnimator() {
        ObjectAnimator animation = ObjectAnimator.ofInt (this, "mLayoutParamsY", getMLayoutParamsY(), getMLayoutParamsY()+moveUpDistance); // see this max value coming back here, we animale towards that value
        animation.setDuration (200); //in milliseconds
        animation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mWindowManager.updateViewLayout(FloatingBallView.this, mLayoutParams);
            }
        });
        animation.start();
    }
    int opacityStart=0;
    boolean breathingRise=true;
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.translate(measuredSideLength /2, measuredSideLength /2);
        Log.d(TAG, "onDraw: " + mOpacityMode);

        //draw gray background
        if (useGrayBackground) {
            canvas.drawCircle(0, 0, mBackgroundRadius, mBackgroundPaint);
        }

        //clear ball
        canvas.drawCircle(ballCenterX, ballCenterY, ballRadius, mBallEmptyPaint);
        //draw ball
        canvas.drawCircle(ballCenterX, ballCenterY, ballRadius, mBallPaint);

        //draw imageBackground
        if (useBackground)
            canvas.drawBitmap(mBitmapScaled, -mBitmapScaled.getWidth() / 2 + ballCenterX, -mBitmapScaled.getHeight() / 2 + ballCenterY, mBallPaint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(measuredSideLength, measuredSideLength);
    }

    //核心方法
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //使用Detector处理一部分手势。
        mDetector.onTouchEvent(event);

        switch (event.getAction()) {
            //任何接触，球都放大。 Opacity reset.
            case MotionEvent.ACTION_DOWN:
                onTouchAnimate.start();//球放大动画
                if(mOpacityMode == OPACITY_REDUCE){
                    // 自动降低透明度的逻辑
                    setPaintAlpha(mBaseOpacity);
                }

                // 处理移动模式
            case MotionEvent.ACTION_MOVE:
                // 移动模式
                if (isLongPress){
                    //getX()、getY()返回的则是触摸点相对于View的位置。
                    //getRawX()、getRawY()返回的是触摸点相对于屏幕的位置
                    if (!isFirstEvent) {
                        isFirstEvent = true;
                        mLastTouchEventX = event.getX();
                        mLastTouchEventY = event.getY();
                    }
                    mLayoutParams.x = (int) (event.getRawX() - mLastTouchEventX);
                    mLayoutParams.y = (int) (event.getRawY() - mLastTouchEventY);

                    SharedPrefsUtils.setIntegerPreference(PREF_PARAM_X, mLayoutParams.x);
                    SharedPrefsUtils.setIntegerPreference(PREF_PARAM_Y, mLayoutParams.y);
                    mWindowManager.updateViewLayout(FloatingBallView.this, mLayoutParams);
                }
                break;
            case MotionEvent.ACTION_UP:
                //球缩小动画
                unTouchAnimate.start();
                //滑动操作
                if(isScrolling){
                    //功能接口
                    doGesture();
                    //球移动动画
                    moveFloatBallBack();
                    currentGestureState = NONE;
                    lastGestureState = NONE;
                    isScrolling=false;
                }
                if (mOpacityMode == OPACITY_REDUCE) {
                    // 自动降低透明度的逻辑
                    if (reduceAnimate != null) {
                        reduceAnimate.start();
                    }
                }
                //reset flag value.
                isLongPress=false;
                isFirstEvent=false;
                break;
        }
        return true;
    }

    private void doGesture() {
        switch (currentGestureState) {
            case UP:
                if(mUpFunctionListener!=null)
                    mUpFunctionListener.onClick();
                break;
            case DOWN:
                if(mUpFunctionListener!=null)
                    mDownFunctionListener.onClick();
                break;
            case LEFT:
                if(mLeftFunctionListener!=null)
                    mLeftFunctionListener.onClick();
                break;
            case RIGHT:
                if(mRightFunctionListener!=null)
                    mRightFunctionListener.onClick();
                break;
            case NONE:
                break;
        }
    }

    /**
     * 根据currentGestureSTATE改变显示参数,瞬间移动
     */
    private void moveFloatingBall() {
        switch (currentGestureState){
            case UP:
                ballCenterX = 0;
                ballCenterY = -gestureMoveDistance;
                break;
            case DOWN:
                ballCenterY = gestureMoveDistance;
                ballCenterX = 0;
                break;
            case LEFT:
                ballCenterX = -gestureMoveDistance;
                ballCenterY = 0;
                break;
            case RIGHT:
                ballCenterX = gestureMoveDistance;
                ballCenterY = 0;
                break;
            case NONE:
                ballCenterX = 0;
                ballCenterY = 0;
                break;
        }
        invalidate();
    }

    private void moveFloatBallBack() {
        PropertyValuesHolder pvh1=PropertyValuesHolder.ofFloat("ballCenterX",0);
        PropertyValuesHolder pvh2=PropertyValuesHolder.ofFloat("ballCenterY",0);
        ObjectAnimator.ofPropertyValuesHolder(this, pvh1, pvh2).setDuration(300).start();
    }


    /**
     * 处理单击事件、滑动事件、长按。
     */
    private class FloatingBallGestureListener implements GestureDetector.OnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            return false;
        }

        @Override
        public void onShowPress(MotionEvent e) {

        }

        //单击抬起
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            if(!useDoubleClick)
                AccessibilityUtil.doBack(mService);
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            isScrolling=true;

            float firstScrollX = e1.getX();
            float firstScrollY = e1.getY();

            float lastScrollX = e2.getX();
            float lastScrollY = e2.getY();

            float deltaX = lastScrollX - firstScrollX;
            float deltaY = lastScrollY - firstScrollY;

            double angle = Math.atan2(deltaY, deltaX);
            //判断currentGestureSTATE
            if (angle > -Math.PI/4 && angle < Math.PI/4) {
                currentGestureState = RIGHT;
            } else if (angle > Math.PI/4 && angle < Math.PI*3/4) {
                currentGestureState = DOWN;
            } else  if (angle > -Math.PI*3/4 && angle < -Math.PI/4) {
                currentGestureState = UP;
            } else{
                currentGestureState = LEFT;
            }
            if (currentGestureState != lastGestureState) {
                moveFloatingBall();
                lastGestureState = currentGestureState;
            }
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            if (mIsVibrate) {
                long[] pattern = {0, 70};
                mVibrator.vibrate(pattern, -1);
            }
            isLongPress = true;

        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return false;
        }
    }

    private class DoubleTapGestureListener implements GestureDetector.OnDoubleTapListener{
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            Log.d(TAG, "onSingleTapConfirmed: " + e);
            AccessibilityUtil.doBack(mService);
            return false;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if (mDoubleTapFunctionListener != null) {
                mDoubleTapFunctionListener.onClick();
            }
            return false;
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            return false;
        }
    }
}
