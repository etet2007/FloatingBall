package com.chenyee.stephenlau.floatingball.floatingBall;

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

import com.chenyee.stephenlau.floatingball.R;
import com.chenyee.stephenlau.floatingball.util.BitmapUtils;
import com.chenyee.stephenlau.floatingball.util.FunctionInterfaceUtils;
import com.chenyee.stephenlau.floatingball.util.SharedPrefsUtils;

import static com.chenyee.stephenlau.floatingball.App.gScreenHeight;
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

  private static final int STATE_UP = 1;
  private static final int STATE_DOWN = 2;
  private static final int STATE_LEFT = 3;
  private static final int STATE_RIGHT = 4;
  private static final int STATE_NONE = 5;

  private int currentGestureState;
  private int lastGestureState = STATE_NONE;

  //灰色背景长度
  private final float edge = dip2px(getContext(), 4);
  //ballRadius在动画变大的值
  private final float ballRadiusDeltaMax = 7;
  private final int gestureMoveDistance = 18;
  //白球半径
  private float ballRadius; //不能改名字，用了反射
  //灰色背景半径 = ballRadius + edge
  private float mBackgroundRadius;

  private int measuredSideLength;

  public void setMoveUpDistance(int moveUpDistance) {
    this.moveUpDistance = moveUpDistance;
  }

  //移动距离 可以设置
  private int moveUpDistance;

  private float ballCenterY = 0;
  private float ballCenterX = 0;

  //function list
  private FunctionListener mSingleTapFunctionListener;
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
  private boolean isUseDoubleClick = false;

  private boolean mIsVibrate = true;

  //上次touchEvent的位置
  private float mLastTouchEventPositionX;
  private float mLastTouchEventPositionY;

  private WindowManager.LayoutParams mViewLayoutParams;

  private GestureDetector mGestureDetector;

  private WindowManager mWindowManager;

  private ObjectAnimator onTouchAnimator;
  private ObjectAnimator unTouchAnimator;

  private Vibrator mVibrator;

  private Bitmap mBitmapRead;
  private Bitmap mBitmapScaled;

  //基础透明度值
  private int mBaseOpacity;

  private int mOpacityMode;
  private ObjectAnimator breathingOpacityAnimator;

  private ObjectAnimator reduceOpacityAnimator;

  private int lastLayoutParamsY;
  private int gestureStateChangeCount;

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

  public void setDoubleClickEventType(int doubleClickEventType) {
    mDoubleTapFunctionListener = FunctionInterfaceUtils.getListener(doubleClickEventType);

    isUseDoubleClick =  mDoubleTapFunctionListener != FunctionInterfaceUtils.getListener(STATE_NONE);

    if (isUseDoubleClick) {
      mGestureDetector.setOnDoubleTapListener(new DoubleTapGestureListener());
    } else {
      mGestureDetector.setOnDoubleTapListener(null);
    }
  }

  private int mLayoutParamsY;//动画用到

  private int getMLayoutParamsY() {
    return mViewLayoutParams.y;
  }

  private void setMLayoutParamsY(int y) {
    mViewLayoutParams.y = y;
  }

  public void setLayoutParams(WindowManager.LayoutParams params) {
    mViewLayoutParams = params;
  }

  public WindowManager.LayoutParams getLayoutParams() {
    return mViewLayoutParams;
  }

  public float getBallRadius() {
    return ballRadius;
  }

  public void setBallRadius(float ballRadius) {
    this.ballRadius = ballRadius;
  }


  public void setIsVibrate(boolean isVibrate) {
    mIsVibrate = isVibrate;
  }

  /**
   * 设置透明度的模式，设置完后需要立刻刷新起效。
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
      setUpReduceAnimator(mBaseOpacity);
      if (reduceOpacityAnimator != null) {
        reduceOpacityAnimator.start();
      }
    }

    if (mOpacityMode == OPACITY_BREATHING) {
      setUpBreathingAnimator(mBaseOpacity);
      breathingOpacityAnimator.start();
    } else {
      if (breathingOpacityAnimator != null) {
        breathingOpacityAnimator.cancel();
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

  public void setOpacity(int opacity) {
    setPaintAlpha(opacity);
    //记录下用户设置的透明度
    mBaseOpacity = opacity;

    refreshOpacityMode();
  }


  private void setUpReduceAnimator(int opacity) {
    Keyframe kf1 = Keyframe.ofInt(0f, opacity);
    Keyframe kf2 = Keyframe.ofInt(0.5f, opacity);
    Keyframe kf3 = Keyframe.ofInt(1f, (int) (opacity * 0.6));
    PropertyValuesHolder pVH = PropertyValuesHolder.ofKeyframe("paintAlpha", kf1, kf2, kf3);
    reduceOpacityAnimator = ObjectAnimator.ofPropertyValuesHolder(this, pVH);
    reduceOpacityAnimator.setDuration(3000);
    reduceOpacityAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
      @Override
      public void onAnimationUpdate(ValueAnimator animation) {
        invalidate();
      }
    });
  }

  private void setUpBreathingAnimator(int opacity) {
    Keyframe kf1 = Keyframe.ofInt(0f, (int) (opacity * 0.4));
    Keyframe kf2 = Keyframe.ofInt(0.35f, opacity);
    Keyframe kf3 = Keyframe.ofInt(0.5f, opacity);
    Keyframe kf4 = Keyframe.ofInt(0.65f, opacity);
    Keyframe kf5 = Keyframe.ofInt(1f, (int) (opacity * 0.4));
    PropertyValuesHolder pVH = PropertyValuesHolder
        .ofKeyframe("paintAlpha", kf1, kf2, kf3, kf4, kf5);
    breathingOpacityAnimator = ObjectAnimator.ofPropertyValuesHolder(this, pVH);
    breathingOpacityAnimator.setRepeatCount(ValueAnimator.INFINITE);
    breathingOpacityAnimator.setRepeatMode(ValueAnimator.RESTART);
    breathingOpacityAnimator.setDuration(4000);
    breathingOpacityAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
      @Override
      public void onAnimationUpdate(ValueAnimator animation) {
        invalidate();
      }
    });
  }

  public int getOpacity() {
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

  public void setmSingleTapFunctionListener(FunctionListener singleTapFunctionListener) {
    mSingleTapFunctionListener = singleTapFunctionListener;
  }

  /**
   * 改变悬浮球大小，需要改变所有与Size相关的东西
   */
  public void changeFloatBallSizeWithRadius(int ballRadius) {
    this.ballRadius = ballRadius;
    this.mBackgroundRadius = ballRadius + edge;

    //View宽高 r + moveDistance + r在动画变大的值 = r + edge + gap
    int frameGap = (int) (gestureMoveDistance + ballRadiusDeltaMax - edge);

    measuredSideLength = (int) (mBackgroundRadius + frameGap) * 2;

    if (useBackground) {
      createBitmapCropFromBitmapRead();
    }
    //动画的参数也需计算
    setUpTouchAnimator();
  }

  /**
   * 构造函数
   */
  public FloatingBallView(Context context) {
    super(context);
    changeFloatBallSizeWithRadius(25);

    performAddAnimator();

    mWindowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
    mGestureDetector = new GestureDetector(context, new FloatingBallGestureListener());
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
    mBitmapRead = BitmapFactory.decodeFile(path + "/ballBackground.png");

    //读取不成功就取默认图片
    if (mBitmapRead == null) {
      Resources res = getResources();
      mBitmapRead = BitmapFactory.decodeResource(res, R.drawable.joe_big);
    }
  }

  /**
   * 复制图片置软件文件夹内。
   */
  public void copyBackgroundImage(String imagePath) {
    Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
    if (bitmap == null) {
      Toast.makeText(getContext(), "Read image error", Toast.LENGTH_LONG).show();
      return;
    }
    //copy source image
    String path = getContext().getFilesDir().toString();
    BitmapUtils.copyImage(bitmap, path, "ballBackground.png");
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

  private void setUpTouchAnimator() {
    Keyframe kf0 = Keyframe.ofFloat(0f, ballRadius);
    Keyframe kf1 = Keyframe.ofFloat(.7f, ballRadius + ballRadiusDeltaMax - 1);
    Keyframe kf2 = Keyframe.ofFloat(1f, ballRadius + ballRadiusDeltaMax);
    PropertyValuesHolder onTouch = PropertyValuesHolder.ofKeyframe("ballRadius", kf0, kf1, kf2);
    onTouchAnimator = ObjectAnimator.ofPropertyValuesHolder(this, onTouch);
    onTouchAnimator.setDuration(300);
    onTouchAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
      @Override
      public void onAnimationUpdate(ValueAnimator animation) {
        invalidate();
      }
    });

    Keyframe kf3 = Keyframe.ofFloat(0f, ballRadius + ballRadiusDeltaMax);
    Keyframe kf4 = Keyframe.ofFloat(0.3f, ballRadius + ballRadiusDeltaMax);
    Keyframe kf5 = Keyframe.ofFloat(1f, ballRadius);
    PropertyValuesHolder unTouch = PropertyValuesHolder.ofKeyframe("ballRadius", kf3, kf4, kf5);
    unTouchAnimator = ObjectAnimator.ofPropertyValuesHolder(this, unTouch);
    unTouchAnimator.setDuration(400);
    unTouchAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
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
            WindowManager windowManager = (WindowManager) getContext()
                .getSystemService(Context.WINDOW_SERVICE);
            if (windowManager != null) {
              windowManager.removeView(FloatingBallView.this);
            }
          }
        })
        .start();
  }


  public int inputMethodWindowHeight;
  private boolean isMoveUp = false;
  private boolean isKeyboardShow = false;

  /**
   * 移动到键盘顶部，移动前记录
   */
  public void moveToKeyboardTop() {
    isKeyboardShow = true;

    int keyboardTopY = gScreenHeight - inputMethodWindowHeight;

    int ballBottomYPlusGap = getMLayoutParamsY() + measuredSideLength + moveUpDistance;
    if (ballBottomYPlusGap < keyboardTopY) {
      return;
    }
    lastLayoutParamsY = getMLayoutParamsY();

    startParamsYAnimationTo(keyboardTopY - measuredSideLength - moveUpDistance);
    isMoveUp = true;

  }

  public void moveBackWhenKeyboardDisappear() {
    isKeyboardShow = false;

    if (isMoveUp) {
      isMoveUp = false;

      startParamsYAnimationTo(lastLayoutParamsY);
    }

  }

  private void startParamsYAnimationTo(int paramsY) {
    ObjectAnimator animation = ObjectAnimator
        .ofInt(this, "mLayoutParamsY", getMLayoutParamsY(), paramsY
        );
    animation.setDuration(200);
    animation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
      @Override
      public void onAnimationUpdate(ValueAnimator animation) {
        mWindowManager.updateViewLayout(FloatingBallView.this, mViewLayoutParams);
      }
    });
    animation.start();
  }

  /**
   * 绘制
   */
  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    //移动到View的中心进行绘制
    canvas.translate(measuredSideLength / 2, measuredSideLength / 2);
    //draw gray background
    if (useGrayBackground) {
      canvas.drawCircle(0, 0, mBackgroundRadius, mBackgroundPaint);
    }

    //clear ball
    canvas.drawCircle(ballCenterX, ballCenterY, ballRadius, mBallEmptyPaint);
    //draw ball
    canvas.drawCircle(ballCenterX, ballCenterY, ballRadius, mBallPaint);

    //draw imageBackground
    if (useBackground) {
      canvas.drawBitmap(mBitmapScaled, -mBitmapScaled.getWidth() / 2 + ballCenterX,
          -mBitmapScaled.getHeight() / 2 + ballCenterY, mBallPaint);
    }
  }

  /**
   * 布局，改变View的大小
   */
  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    setMeasuredDimension(measuredSideLength, measuredSideLength);
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    mGestureDetector.onTouchEvent(event);    //使用Detector处理一部分手势

    switch (event.getAction()) {
      //任何接触，球都放大。 Opacity reset.
      case MotionEvent.ACTION_DOWN:
        onTouchAnimator.start();
        if (mOpacityMode == OPACITY_REDUCE) {
          setPaintAlpha(mBaseOpacity);
        }

      case MotionEvent.ACTION_MOVE:
        // 长按移动模式
        if (isLongPress) {
          //getX()、getY()返回的则是触摸点相对于View的位置。
          //getRawX()、getRawY()返回的是触摸点相对于屏幕的位置
          if (!isFirstEvent) {
            isFirstEvent = true;
            mLastTouchEventPositionX = event.getX();
            mLastTouchEventPositionY = event.getY();
          }
          mViewLayoutParams.x = (int) (event.getRawX() - mLastTouchEventPositionX);
          mViewLayoutParams.y = (int) (event.getRawY() - mLastTouchEventPositionY);

          SharedPrefsUtils.setIntegerPreference(PREF_PARAM_X, mViewLayoutParams.x);
          SharedPrefsUtils.setIntegerPreference(PREF_PARAM_Y, mViewLayoutParams.y);
          mWindowManager.updateViewLayout(FloatingBallView.this, mViewLayoutParams);
        }
        break;
      case MotionEvent.ACTION_UP:
        unTouchAnimator.start();
        //滑动操作
        if (isScrolling) {
          onFunctionWithCurrentGestureState();
          //球移动动画
          moveFloatBallBack();
          currentGestureState = STATE_NONE;
          lastGestureState = STATE_NONE;
          isScrolling = false;
        }
        if (mOpacityMode == OPACITY_REDUCE) {
          if (reduceOpacityAnimator != null) {
            reduceOpacityAnimator.start();
          }
        }
        //reset flag value.
        if (isLongPress) {
          isLongPress = false;
          if (isKeyboardShow) {
            //键盘弹起，长按移动到键盘下要避开
            int keyboardTopY = gScreenHeight - inputMethodWindowHeight;

            int ballBottomYPlusGap = getMLayoutParamsY() + measuredSideLength + moveUpDistance;
            if (ballBottomYPlusGap >= keyboardTopY) {
              lastLayoutParamsY = getMLayoutParamsY();
              startParamsYAnimationTo(keyboardTopY - measuredSideLength - moveUpDistance);
            } else {
              isMoveUp = false;
            }

          }

        }
        isFirstEvent = false;
        break;
    }
    return true;
  }

  private void onFunctionWithCurrentGestureState() {
    if (gestureStateChangeCount > 1) {
      gestureStateChangeCount = 0;
      return;
    }
    gestureStateChangeCount = 0;

    switch (currentGestureState) {
      case STATE_UP:
        if (mUpFunctionListener != null) {
          mUpFunctionListener.onFunction();
        }
        break;
      case STATE_DOWN:
        if (mUpFunctionListener != null) {
          mDownFunctionListener.onFunction();
        }
        break;
      case STATE_LEFT:
        if (mLeftFunctionListener != null) {
          mLeftFunctionListener.onFunction();
        }
        break;
      case STATE_RIGHT:
        if (mRightFunctionListener != null) {
          mRightFunctionListener.onFunction();
        }
        break;
      case STATE_NONE:
        break;
    }
  }

  private void moveBallViewWithCurrentGestureState() {
    switch (currentGestureState) {
      case STATE_UP:
        ballCenterX = 0;
        ballCenterY = - gestureMoveDistance;
        break;
      case STATE_DOWN:
        ballCenterY = gestureMoveDistance;
        ballCenterX = 0;
        break;
      case STATE_LEFT:
        ballCenterX = - gestureMoveDistance;
        ballCenterY = 0;
        break;
      case STATE_RIGHT:
        ballCenterX = gestureMoveDistance;
        ballCenterY = 0;
        break;
      case STATE_NONE:
        ballCenterX = 0;
        ballCenterY = 0;
        break;
    }
    invalidate();
  }

  private void moveFloatBallBack() {
    PropertyValuesHolder pvh1 = PropertyValuesHolder.ofFloat("ballCenterX", 0);
    PropertyValuesHolder pvh2 = PropertyValuesHolder.ofFloat("ballCenterY", 0);
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
      if (!isUseDoubleClick) {
        if (mSingleTapFunctionListener != null) {
          mSingleTapFunctionListener.onFunction();
        }

      }
      return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
      isScrolling = true;

      float firstScrollX = e1.getX();
      float firstScrollY = e1.getY();

      float lastScrollX = e2.getX();
      float lastScrollY = e2.getY();

      float deltaX = lastScrollX - firstScrollX;
      float deltaY = lastScrollY - firstScrollY;

      double angle = Math.atan2(deltaY, deltaX);
      //判断currentGestureSTATE
      if (angle > -Math.PI / 4 && angle < Math.PI / 4) {
        currentGestureState = STATE_RIGHT;
      } else if (angle > Math.PI / 4 && angle < Math.PI * 3 / 4) {
        currentGestureState = STATE_DOWN;
      } else if (angle > -Math.PI * 3 / 4 && angle < -Math.PI / 4) {
        currentGestureState = STATE_UP;
      } else {
        currentGestureState = STATE_LEFT;
      }
      if (currentGestureState != lastGestureState) {
        Log.d(TAG, "onScroll: gestureStateChangeCount"+gestureStateChangeCount);
        gestureStateChangeCount++;
        moveBallViewWithCurrentGestureState();
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


  private class DoubleTapGestureListener implements GestureDetector.OnDoubleTapListener {

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
      if (mSingleTapFunctionListener != null) {
        mSingleTapFunctionListener.onFunction();
      }
      return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
      if (mDoubleTapFunctionListener != null) {
        mDoubleTapFunctionListener.onFunction();
      }
      return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
      return false;
    }
  }
}
