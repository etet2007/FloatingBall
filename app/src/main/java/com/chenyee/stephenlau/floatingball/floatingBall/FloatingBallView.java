package com.chenyee.stephenlau.floatingball.floatingBall;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Vibrator;
import android.support.annotation.Keep;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Toast;

import com.chenyee.stephenlau.floatingball.R;
import com.chenyee.stephenlau.floatingball.util.BitmapUtils;
import com.chenyee.stephenlau.floatingball.util.FunctionInterfaceUtils;
import com.chenyee.stephenlau.floatingball.util.SharedPrefsUtils;

import static com.chenyee.stephenlau.floatingball.App.gScreenHeight;
import static com.chenyee.stephenlau.floatingball.util.StaticStringUtil.*;

/**
 * Created by stephenlau on 2017/12/5.
 */

public class FloatingBallView extends View {

  private static final String TAG = FloatingBallView.class.getSimpleName();

  public static final int STATE_UP = 1;
  public static final int STATE_DOWN = 2;
  public static final int STATE_LEFT = 3;
  public static final int STATE_RIGHT = 4;
  public static final int STATE_NONE = 5;

  private FloatingBallDrawer floatingBallDrawer;

  private FloatingBallAnimator floatingBallAnimator;

  private FloatingBallPaint floatingBallPaint;


  public FloatingBallDrawer getFloatingBallDrawer() {
    return floatingBallDrawer;
  }

  public void setFloatingBallDrawer(FloatingBallDrawer floatingBallDrawer) {
    this.floatingBallDrawer = floatingBallDrawer;
  }

  public FloatingBallPaint getFloatingBallPaint() {
    return floatingBallPaint;
  }

  public void setFloatingBallPaint(FloatingBallPaint floatingBallPaint) {
    this.floatingBallPaint = floatingBallPaint;
  }

  private int currentGestureState;
  private int lastGestureState = STATE_NONE;

  //function list
  private FunctionListener singleTapFunctionListener;
  private FunctionListener downFunctionListener;
  private FunctionListener upFunctionListener;
  private FunctionListener leftFunctionListener;
  private FunctionListener rightFunctionListener;
  private FunctionListener doubleTapFunctionListener;

  //标志位
  private boolean isFirstEvent = false;
  private boolean isScrolling = false;
  private boolean isLongPress = false;

  private boolean useBackground = false;
  private boolean isUseDoubleClick = false;

  private boolean isVibrate = true;

  //上次touchEvent的位置
  private float lastTouchEventPositionX;
  private float lastTouchEventPositionY;

  private GestureDetector mGestureDetector;

  private WindowManager windowManager;

  private Vibrator mVibrator;

  private Bitmap mBitmapRead;
  private Bitmap mBitmapScaled;

  private int userSetOpacity = 125;

  private int opacityMode;


  public LayoutParams getBallViewLayoutParams() {
    return ballViewLayoutParams;
  }

  public void setBallViewLayoutParams(LayoutParams ballViewLayoutParams) {
    this.ballViewLayoutParams = ballViewLayoutParams;
  }

  private WindowManager.LayoutParams ballViewLayoutParams;

  private int lastLayoutParamsY;

  private int gestureStateChangeCount;


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
    doubleTapFunctionListener = FunctionInterfaceUtils.getListener(doubleClickEventType);

    isUseDoubleClick = doubleTapFunctionListener != FunctionInterfaceUtils.getListener(NONE);

    if (isUseDoubleClick) {
      mGestureDetector.setOnDoubleTapListener(new DoubleTapGestureListener());
    } else {
      mGestureDetector.setOnDoubleTapListener(null);
    }
  }

  private int layoutParamsY;//动画用到

  private int getLayoutParamsY() {
    return ballViewLayoutParams.y;
  }

  @Keep
  private void setLayoutParamsY(int y) {
    ballViewLayoutParams.y = y;
  }


  public void setIsVibrate(boolean isVibrate) {
    this.isVibrate = isVibrate;
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

  public int getOpacityMode() {
    return opacityMode;
  }


  public void setOpacity(int opacity) {
    floatingBallPaint.setPaintAlpha(opacity);

    userSetOpacity = opacity;

    refreshOpacityMode();
  }


  //功能
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

  /**
   * 改变悬浮球大小，需要改变所有与Size相关的东西
   */
  public void changeFloatBallSizeWithRadius(int ballRadius) {

    floatingBallDrawer.calculateBackgroundRadiusAndMeasureSideLength(ballRadius);

    if (useBackground) {
      createBitmapCropFromBitmapRead();
    }

    floatingBallAnimator.setUpTouchAnimator(ballRadius);
  }

  /**
   * 构造函数
   */
  public FloatingBallView(Context context) {
    super(context);

    floatingBallPaint = new FloatingBallPaint();

    floatingBallDrawer = new FloatingBallDrawer(floatingBallPaint);

    floatingBallAnimator = new FloatingBallAnimator(this,floatingBallDrawer);

    floatingBallAnimator.performAddAnimator();

    changeFloatBallSizeWithRadius(25);

    windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);

    mGestureDetector = new GestureDetector(context, new FloatingBallGestureListener());

    mVibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);

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
    if (floatingBallDrawer.ballRadius <= 0) {
      return;
    }
    //bitmapRead可能已被回收
    if (mBitmapRead == null || mBitmapRead.isRecycled()) {
      refreshBitmapRead();
    }

    int edge = (int) floatingBallDrawer.ballRadius * 2;

    Bitmap scaledBitmap = Bitmap.createScaledBitmap(mBitmapRead, edge, edge, true);
    //进行裁切后的bitmapCrop
    mBitmapScaled = Bitmap.createBitmap(edge, edge, Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(mBitmapScaled);
    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    //x y r
    canvas.drawCircle(floatingBallDrawer.ballRadius, floatingBallDrawer.ballRadius, floatingBallDrawer.ballRadius, paint);
    paint.reset();
    paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
    canvas.drawBitmap(scaledBitmap, 0, 0, paint);

    scaledBitmap.recycle();
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
      windowManager.removeView(FloatingBallView.this);
    }
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

    int ballBottomYPlusGap =
        getLayoutParamsY() + floatingBallDrawer.measuredSideLength + floatingBallDrawer.moveUpDistance;
    if (ballBottomYPlusGap < keyboardTopY) {
      return;
    }
    lastLayoutParamsY = getLayoutParamsY();

    startParamsYAnimationTo(keyboardTopY - floatingBallDrawer.measuredSideLength - floatingBallDrawer.moveUpDistance);
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
        .ofInt(this, "layoutParamsY", getLayoutParamsY(), paramsY
        );
    animation.setDuration(200);
    animation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
      @Override
      public void onAnimationUpdate(ValueAnimator animation) {
        windowManager.updateViewLayout(FloatingBallView.this, ballViewLayoutParams);
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

    floatingBallDrawer.drawBallWithThisModel(canvas);

    Paint ballPaint = floatingBallPaint.getBallPaint();

    if (useBackground) {
      canvas.drawBitmap(mBitmapScaled, -mBitmapScaled.getWidth() / 2 + floatingBallDrawer.ballCenterX,
          -mBitmapScaled.getHeight() / 2 + floatingBallDrawer.ballCenterY, ballPaint);
    }
  }

  /**
   * 布局，改变View的大小
   */
  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    setMeasuredDimension(floatingBallDrawer.measuredSideLength, floatingBallDrawer.measuredSideLength);
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    mGestureDetector.onTouchEvent(event);    //使用Detector处理一部分手势

    switch (event.getAction()) {
      //任何接触，球都放大。 Opacity reset.
      case MotionEvent.ACTION_DOWN:
        floatingBallAnimator.startOnTouchAnimator();

        if (opacityMode == OPACITY_REDUCE) {
          floatingBallPaint.setPaintAlpha(userSetOpacity);
        }

      case MotionEvent.ACTION_MOVE:
        // 长按移动模式
        if (isLongPress) {
          //getX()、getY()返回的则是触摸点相对于View的位置。
          //getRawX()、getRawY()返回的是触摸点相对于屏幕的位置
          if (!isFirstEvent) {
            isFirstEvent = true;
            lastTouchEventPositionX = event.getX();
            lastTouchEventPositionY = event.getY();
          }
          ballViewLayoutParams.x = (int) (event.getRawX() - lastTouchEventPositionX);
          ballViewLayoutParams.y = (int) (event.getRawY() - lastTouchEventPositionY);

          SharedPrefsUtils.setIntegerPreference(PREF_PARAM_X, ballViewLayoutParams.x);
          SharedPrefsUtils.setIntegerPreference(PREF_PARAM_Y, ballViewLayoutParams.y);
          windowManager.updateViewLayout(FloatingBallView.this, ballViewLayoutParams);
        }
        break;
      case MotionEvent.ACTION_UP:
        floatingBallAnimator.startUnTouchAnimator();
        //滑动操作
        if (isScrolling) {
          onFunctionWithCurrentGestureState();
          //球移动动画
          floatingBallAnimator.moveFloatBallBack();

          currentGestureState = STATE_NONE;
          lastGestureState = STATE_NONE;
          isScrolling = false;
        }
        if (opacityMode == OPACITY_REDUCE) {
          floatingBallAnimator.startReduceOpacityAnimator();
        }
        //reset flag value.
        if (isLongPress) {
          isLongPress = false;
          if (isKeyboardShow) {
            //键盘弹起，长按移动到键盘下要避开
            int keyboardTopY = gScreenHeight - inputMethodWindowHeight;

            int ballBottomYPlusGap =
                getLayoutParamsY() + floatingBallDrawer.measuredSideLength + floatingBallDrawer.moveUpDistance;
            if (ballBottomYPlusGap >= keyboardTopY) {
              lastLayoutParamsY = getLayoutParamsY();
              startParamsYAnimationTo(
                  keyboardTopY - floatingBallDrawer.measuredSideLength - floatingBallDrawer.moveUpDistance);
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
        if (upFunctionListener != null) {
          upFunctionListener.onFunction();
        }
        break;
      case STATE_DOWN:
        if (upFunctionListener != null) {
          downFunctionListener.onFunction();
        }
        break;
      case STATE_LEFT:
        if (leftFunctionListener != null) {
          leftFunctionListener.onFunction();
        }
        break;
      case STATE_RIGHT:
        if (rightFunctionListener != null) {
          rightFunctionListener.onFunction();
        }
        break;
      case STATE_NONE:
        break;
    }
  }

  public void updateModuleData() {
    floatingBallDrawer.updateFieldBySingleDataManager();

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
        if (singleTapFunctionListener != null) {
          singleTapFunctionListener.onFunction();
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
        Log.d(TAG, "onScroll: gestureStateChangeCount" + gestureStateChangeCount);
        gestureStateChangeCount++;
        floatingBallAnimator.moveBallViewWithCurrentGestureState(currentGestureState);
        lastGestureState = currentGestureState;
      }
      return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
      if (isVibrate) {
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
      if (singleTapFunctionListener != null) {
        singleTapFunctionListener.onFunction();
      }
      return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
      if (doubleTapFunctionListener != null) {
        doubleTapFunctionListener.onFunction();
      }
      return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
      return false;
    }
  }
}
