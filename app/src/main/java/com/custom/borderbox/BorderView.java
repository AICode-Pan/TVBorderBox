package com.custom.borderbox;

import android.animation.Animator;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

/**
 * <pre>
 *     author : panbeixing
 *     time : 2018/8/18
 *     desc :
 *     version : 1.0
 * </pre>
 */

public class BorderView extends View {
    private final static String TAG = "BorderView";
    private ViewTreeObserver rootView;
    private FrameLayout contentView;
    private Rect fromRect, toRect;
    private int borderWidth = 5;
    private int statusBarHeight = 0;

    public BorderView(Activity activity) {
        super(activity);

        View decorView = activity.getWindow().getDecorView();
        rootView = decorView.getViewTreeObserver();
        rootView.addOnGlobalFocusChangeListener(new ViewTreeObserver.OnGlobalFocusChangeListener() {
            @Override
            public void onGlobalFocusChanged(View oldFocus, View newFocus) {
                Log.d(TAG, "GlobalFocusChanged oldFocus : " + oldFocus + " ,newFocus : " + newFocus);
                attachFocusBox(oldFocus, newFocus);
            }
        });

        contentView = decorView.findViewById(android.R.id.content);
        contentView.addView(this);
        bringToFront();

        /* 获取状态栏高度——方法 **/
        //获取status_bar_height资源的ID
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            //根据资源ID获取响应的尺寸值
            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        }

    }

    private Rect lastRect;

    private void attachFocusBox(View oldFocus, View newFocus) {
        fromRect = findViewtoRect(oldFocus);
        toRect = findViewtoRect(newFocus);

        if (fromRect == null && toRect == null) {
            return;
        }

        if (fromRect != null) {
            fromRect.left = fromRect.left - borderWidth;
            fromRect.top = fromRect.top - borderWidth;
            fromRect.right = fromRect.right + borderWidth;
            fromRect.bottom = fromRect.bottom + borderWidth;
        }
        if (toRect != null) {
            toRect.left = toRect.left - borderWidth;
            toRect.top = toRect.top - borderWidth;
            toRect.right = toRect.right + borderWidth;
            toRect.bottom = toRect.bottom + borderWidth;
        }

        if (lastRect != null) {
            if (lastRect.contains(toRect)) {
                Log.e(TAG, "toRect = lastRect");
                return;
            }
        }

        // 重新绘制之前，初始化时间值，重新计算位移动画
        mStartTime = SystemClock.uptimeMillis();
        invalidate();
    }

    public Rect findViewtoRect(View view) {
        Rect rect = new Rect();
        if (view == null) {
            return null;
        }
        view.getGlobalVisibleRect(rect);
        return rect;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    private long mStartTime = -1;
    private long mDuration = 200;
    private int canvasStatus = 0; //画布绘制状态 0 未绘制;1 正在绘制; 2 绘制完毕
    private Rect translateRect = null;

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        Paint paint = new Paint();
        paint.setColor(Color.BLUE);
        paint.setStyle(Paint.Style.STROKE);//设置空心
        paint.setStrokeWidth(5);//设置画笔粗细

        if (toRect == null) {
            return;
        }

        if (toRect != null && fromRect == null) {
            lastRect = toRect;
            canvas.drawRect(toRect, paint);
            return;
        }

        long curTime = SystemClock.uptimeMillis();
        // t为一个0到1均匀变化的值
        float t = (curTime - mStartTime) / (float) mDuration;
        t = Math.max(0, Math.min(t, 1));

        if (canvasStatus == 1) {
            lastRect = translateRect;
        }
        if (lastRect == null) {
            if (fromRect != null) {
                lastRect = fromRect;
            } else {
                return;
            }
        }
        int left = (int) lerp(lastRect.left, toRect.left, t);
        int top = (int) lerp(lastRect.top, toRect.top, t);
        int right = (int) lerp(lastRect.right, toRect.right, t);
        int bottom = (int) lerp(lastRect.bottom, toRect.bottom, t);
        boolean done = true;
        if (t < 1) {
            done = false;
        }
        if (0 <= t && t < 1) {
            canvasStatus = 1;
            done = false;
            // 保存画布，方便下次绘制
            canvas.save();
//            canvas.translate(translateX, translateY);
            if (translateRect == null) {
                translateRect = new Rect();
            }
            translateRect.left = left;
            translateRect.top = top;
            translateRect.right = right;
            translateRect.bottom = bottom;
            canvas.drawRect(translateRect, paint);
            canvas.restore();
        } else {
            lastRect = toRect;
            canvasStatus = 2;
            canvas.drawRect(toRect, paint);
        }

        if (!done) {
            invalidate();
        }
    }

    // 数制差
    float lerp(float start, float end, float t) {
        return start + (end - start) * t;
    }

    private static class CanvasHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    }
}
