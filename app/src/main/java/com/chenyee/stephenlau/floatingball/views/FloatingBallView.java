package com.chenyee.stephenlau.floatingball.views;

import android.accessibilityservice.AccessibilityService;
import android.animation.Keyframe;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Vibrator;

import android.preference.PreferenceManager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.chenyee.stephenlau.floatingball.FunctionListener;
import com.chenyee.stephenlau.floatingball.util.AccessibilityUtil;
import com.chenyee.stephenlau.floatingball.util.LockScreenUtil;
import com.chenyee.stephenlau.floatingball.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static com.chenyee.stephenlau.floatingball.util.StaticStringUtil.HOME;
import static com.chenyee.stephenlau.floatingball.util.StaticStringUtil.LOCK_SCREEN;
import static com.chenyee.stephenlau.floatingball.util.StaticStringUtil.NONE;
import static com.chenyee.stephenlau.floatingball.util.StaticStringUtil.PREF_PARAM_X;
import static com.chenyee.stephenlau.floatingball.util.StaticStringUtil.PREF_PARAM_Y;

/**
 * Created by stephenlau on 2017/12/5.
 */

public class FloatingBallView extends View {
    private static final String TAG = FloatingBallView.class.getSimpleName();

    private Paint mBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mBallPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mBallEmptyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);


    //手势的状态，虽然官方不推荐使用enum
    private enum GESTURE_STATE {
        UP, DOWN, LEFT, RIGHT,NONE
    }
    private GESTURE_STATE currentGestureSTATE;
    private GESTURE_STATE lastGestureSTATE = GESTURE_STATE.NONE;

    //球半径、背景半径
    private float ballRadius=25;
    private float mBackgroundRadius=ballRadius+15;
    //View宽高
    private int measuredWidth= (int) (mBackgroundRadius*2+20);
    private int measuredHeight=measuredWidth;

    private int moveUpDistance;

    private float ballCenterY=0;
    private float ballCenterX=0;

    //function
    private int doubleClickEvent;

    private FunctionListener mDownFunctionListener;
    private FunctionListener mUpFunctionListener;
    private FunctionListener mLeftFunctionListener;
    private FunctionListener mRightFunctionListener;
    private FunctionListener mDoubleTapFunctionListener;


    //标志
    private boolean isFirstEvent=false;
    private boolean isScrolling=false;
    private boolean isLongPress=false;

    private boolean useBackground=false;
    private boolean useGrayBackground=true;
    private boolean useDoubleClick=false;
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

    private Bitmap bitmapRead;
    private Bitmap bitmapCrop;

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
        this.useBackground = useBackground;
    }

//    public void setDoubleClickEventType(int doubleClickEvent,FunctionListener doubleTapFunctionListener) {
//
//        this.doubleClickEvent = doubleClickEvent;
//        this.mDoubleTapFunctionListener=doubleTapFunctionListener;
//
//        if(doubleClickEvent!=NONE)
//            mDetector.setOnDoubleTapListener(new DoubleTapGestureListener());
//        else
//            mDetector.setOnDoubleTapListener(null);
//    }

    public void setDoubleClickEventType(boolean useDoubleClick,FunctionListener doubleTapFunctionListener) {

        this.useDoubleClick = useDoubleClick;
        this.mDoubleTapFunctionListener=doubleTapFunctionListener;

        if(useDoubleClick)
            mDetector.setOnDoubleTapListener(new DoubleTapGestureListener());
        else
            mDetector.setOnDoubleTapListener(null);
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
    public void setOpacity(int opacity){
        mBackgroundPaint.setAlpha(opacity);
        mBallPaint.setAlpha(opacity);
    }
    public int getOpacity(){
        return mBackgroundPaint.getAlpha();
    }


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

    //改变球的半径，同时需要改变view的宽高
    public void changeFloatBallSizeWithRadius(int ballRadius){
        this.ballRadius=ballRadius;
        this.mBackgroundRadius=ballRadius+15;
        //View宽高
        measuredWidth= (int) (mBackgroundRadius*2+20);
        measuredHeight=measuredWidth;

        //动画的参数也需要重新计算
        calcTouchAnimator();
    }

    public FloatingBallView(Context context) {
        super(context);
        performAddAnimator();

        mService = (AccessibilityService) context;
        mWindowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);

        mDetector =new GestureDetector(context,new SingleTapGestureListener());

        mVibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);

        mBackgroundPaint.setColor(Color.GRAY);
        mBackgroundPaint.setAlpha(80);

        mBallPaint.setColor(Color.WHITE);
        mBallPaint.setAlpha(150);

        PorterDuff.Mode mode = PorterDuff.Mode.CLEAR;
        mBallEmptyPaint.setXfermode(new PorterDuffXfermode(mode));

        //生成BitmapRead
        getBitmapRead();
    }

    /**
     * 更新bitmapRead的值
     */
    public void getBitmapRead() {
        //app内部目录。
        String path = getContext().getFilesDir().toString();
        bitmapRead = BitmapFactory.decodeFile(path+"/ballBackground.png");

        //读取不成功就取默认图片
        if(bitmapRead==null){
            Resources res=getResources();
            bitmapRead = BitmapFactory.decodeResource(res, R.drawable.joe_big);
        }
    }

    /**
     * 复制图片置软件文件夹内。
     * @param imagePath
     */
    public void copyBackgroundImage(String imagePath) {
        bitmapRead=BitmapFactory.decodeFile(imagePath);
        if(bitmapRead==null){
            Toast.makeText(getContext(), "Error", Toast.LENGTH_LONG).show();
            return;
        }
        //copy source image
        String path = getContext().getFilesDir().toString();
        File file = new File(path, "ballBackground.png");
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            boolean isSucceed=bitmapRead.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
            Log.d(TAG, "getBitmapRead: isSecceed:"+isSucceed);
            // PNG is a lossless format, the compression factor (100) is ignored
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void createBitmapCropFromBitmapRead() {
        if(bitmapRead==null||ballRadius<=0){
            return;
        }

        int width=(int)ballRadius*2;
        int height=(int)ballRadius*2;
//        bitmapRead为读取的原图，缩放为scaledBitmap
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmapRead, width, height, true);
        //进行裁切后的bitmapCrop
        bitmapCrop = Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmapCrop);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

//      canvas.drawCircle(width/2, height/2, ballRadius, paint);
        canvas.drawCircle(ballRadius, ballRadius, ballRadius, paint);
        paint.reset();
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(scaledBitmap, 0, 0, paint);
    }

    private void calcTouchAnimator() {
//        onTouchAnimate
        Keyframe kf0 = Keyframe.ofFloat(0f, ballRadius);
        Keyframe kf1 = Keyframe.ofFloat(.7f, ballRadius+6);
        Keyframe kf2 = Keyframe.ofFloat(1f, ballRadius+7);
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
        Keyframe kf3 = Keyframe.ofFloat(0f, ballRadius+7);
        Keyframe kf4 = Keyframe.ofFloat(0.3f, ballRadius+7);
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
        //可以设置更加细致的动画，这里只是修改圆的半径大小，背景图没处理，适用性低。
//        Keyframe kf0 = Keyframe.ofFloat(0f, 0);
//        Keyframe kf1 = Keyframe.ofFloat(1f, ballRadius);
//        PropertyValuesHolder ballRadiusValuesHolder = PropertyValuesHolder.ofKeyframe("ballRadius", kf0,kf1);
//        Keyframe kf2 = Keyframe.ofFloat(0f, 0);
//        Keyframe kf3 = Keyframe.ofFloat(1f, ballRadius+15);
//        PropertyValuesHolder backgroundRadiusValuesHolder = PropertyValuesHolder.ofKeyframe("mBackgroundRadius", kf2,kf3);
//
//        onAddAnimate = ObjectAnimator.ofPropertyValuesHolder(this, ballRadiusValuesHolder,backgroundRadiusValuesHolder);
//        onAddAnimate.setDuration(400);
//        onAddAnimate.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//                        @Override
//            public void onAnimationUpdate(ValueAnimator animation) {
//                invalidate();
//            }});
//        onAddAnimate.start();

        //View自带的动画。
        setScaleX(0);
        setScaleY(0);
        animate()
                .scaleY(1).scaleX(1)
                .setDuration(200)
                .start();
    }

    public void performRemoveAnimator() {
//        Keyframe kf0 = Keyframe.ofFloat(0f, ballRadius);
//        Keyframe kf1 = Keyframe.ofFloat(1f, 0);
//        PropertyValuesHolder ballRadiusValuesHolder = PropertyValuesHolder.ofKeyframe("ballRadius", kf0,kf1);
//        Keyframe kf2 = Keyframe.ofFloat(0f, mBackgroundRadius);
//        Keyframe kf3 = Keyframe.ofFloat(1f, 0);
//        PropertyValuesHolder backgroundRadiusValuesHolder = PropertyValuesHolder.ofKeyframe("mBackgroundRadius", kf2,kf3);
//
//
//        onRemoveAnimate = ObjectAnimator.ofPropertyValuesHolder(this, ballRadiusValuesHolder,backgroundRadiusValuesHolder);
//        onRemoveAnimate.addListener(new Animator.AnimatorListener() {
//            @Override
//            public void onAnimationStart(Animator animation) {
//            }
//
//            @Override
//            public void onAnimationEnd(Animator animation) {
//                WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
//                windowManager.removeView(BallView.this);
//            }
//
//            @Override
//            public void onAnimationCancel(Animator animation) {
//
//            }
//
//            @Override
//            public void onAnimationRepeat(Animator animation) {
//
//            }
//        });
//        onRemoveAnimate.setDuration(400);
//        onRemoveAnimate.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//            @Override
//            public void onAnimationUpdate(ValueAnimator animation) {
//                invalidate();
//            }
//        });
//        onRemoveAnimate.start();

        //View自带的动画。
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

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.translate(measuredWidth/2,measuredHeight/2);

        //draw gray background
        if(useGrayBackground)
            canvas.drawCircle(0, 0, mBackgroundRadius, mBackgroundPaint);

        //clear ball
        canvas.drawCircle(ballCenterX, ballCenterY, ballRadius, mBallEmptyPaint);
        //draw ball
        canvas.drawCircle(ballCenterX, ballCenterY, ballRadius, mBallPaint);

        //draw imageBackground
        if(useBackground)
            canvas.drawBitmap(bitmapCrop,-bitmapCrop.getWidth()/2+ballCenterX,-bitmapCrop.getHeight()/2+ballCenterY,mBallPaint);
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(measuredWidth, measuredHeight);
    }

    //核心方法
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //使用Detector处理一部分手势。
        mDetector.onTouchEvent(event);

        switch (event.getAction()) {
            //任何接触，球都放大。
            case MotionEvent.ACTION_DOWN:
                onTouchAnimate.start();//球放大动画
                //处理移动模式
            case MotionEvent.ACTION_MOVE:
                //移动模式
                if (isLongPress){
                    //getX()、getY()返回的则是触摸点相对于View的位置。
                    //getRawX()、getRawY()返回的是触摸点相对于屏幕的位置
                    if(!isFirstEvent){
                        isFirstEvent=true;
                        mLastTouchEventX = event.getX();
                        mLastTouchEventY = event.getY();
                    }
                    mLayoutParams.x = (int) (event.getRawX() - mLastTouchEventX);
                    mLayoutParams.y = (int) (event.getRawY() - mLastTouchEventY);

                    SharedPreferences defaultSharedPreferences =PreferenceManager.getDefaultSharedPreferences(getContext());
                    SharedPreferences.Editor editor = defaultSharedPreferences.edit();
                    editor.putInt(PREF_PARAM_X,mLayoutParams.x);
                    editor.putInt(PREF_PARAM_Y,mLayoutParams.y);
                    editor.apply();

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
                    currentGestureSTATE = GESTURE_STATE.NONE;
                    lastGestureSTATE = GESTURE_STATE.NONE;
                    isScrolling=false;
                }
                isLongPress=false;
                isFirstEvent=false;
                break;
        }
        return true;
    }

    private void doGesture() {
        AccessibilityUtil.checkAccessibilitySetting(getContext());
        switch (currentGestureSTATE) {
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


    private class DoubleTapGestureListener implements GestureDetector.OnDoubleTapListener{
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            Log.d(TAG, "onSingleTapConfirmed: "+e);
            AccessibilityUtil.doBack(mService);
            return false;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if(mDoubleTapFunctionListener!=null)
                mDoubleTapFunctionListener.onClick();

//            Log.d(TAG, "onDoubleTap: "+e);
//            if(doubleClickEvent==HOME){
//                AccessibilityUtil.doHome(mService);
//            }else if(doubleClickEvent==LOCK_SCREEN)
//                LockScreenUtil.lockScreen(mService);
            return false;
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            return false;
        }
    }

    /**
     * 处理 单击事件、滑动事件。
     */
    private class SingleTapGestureListener implements GestureDetector.OnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            return false;
        }

        @Override
        public void onShowPress(MotionEvent e) {

        }

        //单击
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            if(!useDoubleClick)
                AccessibilityUtil.doBack(mService);
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            isScrolling=true;
//            Log.d(TAG, "onScroll: distanceX:"+distanceX+" distanceY"+distanceY);
//            Log.d(TAG, "onScroll: e1:"+e1.toString()+" e2:"+e2.toString());
            float firstScrollX = e1.getX();
            float firstScrollY = e1.getY();

            float lastScrollX = e2.getX();
            float lastScrollY = e2.getY();

            float deltaX = lastScrollX - firstScrollX;
            float deltaY = lastScrollY - firstScrollY;

            double angle = Math.atan2(deltaY, deltaX);
            //判断currentGestureSTATE
            if (angle > -Math.PI/4 && angle < Math.PI/4) {
                currentGestureSTATE = GESTURE_STATE.RIGHT;
            } else if (angle > Math.PI/4 && angle < Math.PI*3/4) {
                currentGestureSTATE = GESTURE_STATE.DOWN;
            } else  if (angle > -Math.PI*3/4 && angle < -Math.PI/4) {
                currentGestureSTATE = GESTURE_STATE.UP;
            } else{
                currentGestureSTATE = GESTURE_STATE.LEFT;
            }
            if(currentGestureSTATE != lastGestureSTATE){
                moveFloatBall();
                lastGestureSTATE = currentGestureSTATE;
            }
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            long[] pattern = {0, 100};
            mVibrator.vibrate(pattern, -1);
            isLongPress=true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return false;
        }
    }

    /**
     * 根据currentGestureSTATE改变显示参数
     */
    private void moveFloatBall() {
        int gestureMoveDistance = 18;
        switch (currentGestureSTATE){
            case UP:
                ballCenterX=0;
                ballCenterY=-gestureMoveDistance;
                break;
            case DOWN:
                ballCenterY= gestureMoveDistance;
                ballCenterX=0;
                break;
            case LEFT:
                ballCenterX=-gestureMoveDistance;
                ballCenterY=0;
                break;
            case RIGHT:
                ballCenterX= gestureMoveDistance;
                ballCenterY=0;
                break;
            case NONE:
                ballCenterX=0;
                ballCenterY=0;
                break;
        }
        invalidate();
    }

    private void moveFloatBallBack() {
        PropertyValuesHolder pvh1=PropertyValuesHolder.ofFloat("ballCenterX",0);
        PropertyValuesHolder pvh2=PropertyValuesHolder.ofFloat("ballCenterY",0);
        ObjectAnimator.ofPropertyValuesHolder(this, pvh1, pvh2).setDuration(300).start();
    }


}
