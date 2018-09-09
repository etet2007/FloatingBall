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
import android.support.annotation.Keep;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Toast;

import com.chenyee.stephenlau.floatingball.R;
import com.chenyee.stephenlau.floatingball.floatingBall.gesture.FloatingBallGestureListener;
import com.chenyee.stephenlau.floatingball.floatingBall.gesture.OnGestureEventListener;
import com.chenyee.stephenlau.floatingball.util.BitmapUtils;
import com.chenyee.stephenlau.floatingball.util.FunctionInterfaceUtils;

import static com.chenyee.stephenlau.floatingball.App.gScreenHeight;
import static com.chenyee.stephenlau.floatingball.util.StaticStringUtil.*;

/**
 * Created by stephenlau on 2017/12/5.
 */
@Keep
public class FloatingBallView extends View implements OnGestureEventListener {

  private static final String TAG = FloatingBallView.class.getSimpleName();

  private FloatingBallGestureListener floatingBallGestureListener;

  private FloatingBallPaint floatingBallPaint;

  private FloatingBallDrawer floatingBallDrawer;

  private FloatingBallAnimator floatingBallAnimator;


  //function list
  public FunctionListener singleTapFunctionListener;
  private FunctionListener downFunctionListener;
  private FunctionListener upFunctionListener;
  private FunctionListener leftFunctionListener;
  private FunctionListener rightFunctionListener;
  public FunctionListener doubleTapFunctionListener;

  //标志位
  private boolean useBackground = false;

  public boolean isUseDoubleClick = false;

  //上次touchEvent的位置


  private GestureDetector gestureDetector;

  private WindowManager windowManager;


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
      gestureDetector.setOnDoubleTapListener(floatingBallGestureListener);
    } else {
      gestureDetector.setOnDoubleTapListener(null);
    }
  }

  private int layoutParamsY;//动画用到

  private int getLayoutParamsY() {
    return ballViewLayoutParams.y;
  }

  private void setLayoutParamsY(int y) {
    ballViewLayoutParams.y = y;
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


  public FloatingBallAnimator getFloatingBallAnimator() {
    return floatingBallAnimator;
  }

  public void setOpacity(int opacity) {
    floatingBallPaint.setPaintAlpha(opacity);

    userSetOpacity = opacity;

    refreshOpacityMode();
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

  public void setFloatingBallDrawer(FloatingBallDrawer floatingBallDrawer) {
    this.floatingBallDrawer = floatingBallDrawer;
  }

  public FloatingBallPaint getFloatingBallPaint() {
    return floatingBallPaint;
  }

  public void setFloatingBallPaint(FloatingBallPaint floatingBallPaint) {
    this.floatingBallPaint = floatingBallPaint;
  }

  /**
   * 构造函数
   */
  public FloatingBallView(Context context) {
    super(context);

    floatingBallPaint = new FloatingBallPaint();

    floatingBallDrawer = new FloatingBallDrawer(this,floatingBallPaint);

    floatingBallAnimator = new FloatingBallAnimator(this,floatingBallDrawer);

    floatingBallAnimator.performAddAnimator();

    changeFloatBallSizeWithRadius(25);

    windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);

    floatingBallGestureListener = new FloatingBallGestureListener(this);
    floatingBallGestureListener.setOnGestureEventListener(this);

    gestureDetector = new GestureDetector(context, floatingBallGestureListener);


    refreshOpacityMode();
  }

  public void updateViewLayout(int x,int y) {
    if (windowManager != null) {
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

    if (useBackground) {
      createBitmapCropFromBitmapRead();
    }

    floatingBallAnimator.setUpTouchAnimator(ballRadius);
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
    gestureDetector.onTouchEvent(event);

    floatingBallGestureListener.processEvent(event);

    return true;
  }


  public void updateModuleData() {
    floatingBallDrawer.updateFieldBySingleDataManager();
    floatingBallGestureListener.updateFieldBySingleDataManager();
  }

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

  public void onFunctionWithCurrentGestureState(int currentGestureState) {
    switch (currentGestureState) {
      case FloatingBallGestureListener.STATE_UP:
        if (upFunctionListener != null) {
          upFunctionListener.onFunction();
        }
        break;
      case FloatingBallGestureListener.STATE_DOWN:
        if (upFunctionListener != null) {
          downFunctionListener.onFunction();
        }
        break;
      case FloatingBallGestureListener.STATE_LEFT:
        if (leftFunctionListener != null) {
          leftFunctionListener.onFunction();
        }
        break;
      case FloatingBallGestureListener.STATE_RIGHT:
        if (rightFunctionListener != null) {
          rightFunctionListener.onFunction();
        }
        break;
      case FloatingBallGestureListener.STATE_NONE:
        break;
    }
  }

  @Override
  public void onScrollEnd() {
    //球移动动画
    floatingBallAnimator.moveFloatBallBack();
  }

  @Override
  public void onLongPressEnd() {
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
}
