package com.custom.borderbox;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import java.lang.ref.WeakReference;

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
    private int statusBarHeight = 0; //状态栏高度
    private long startTime = -1;
    private long duration = 200; //位移动画时间
    private int canvasStatus = 0; //画布绘制状态 0 未绘制;1 正在绘制; 2 绘制完毕
    private Rect translateRect = null;
    private Rect currentRect = null;
    private CanvasHandler canvasHandler;
    private Paint paint;

    public BorderView(final Activity activity) {
        super(activity);

        View decorView = activity.getWindow().getDecorView();
        contentView = decorView.findViewById(android.R.id.content);
        contentView.addView(this);
        bringToFront();//置于view的最顶部，防止被覆盖

        canvasHandler = new CanvasHandler(new WeakReference<BorderView>(this));
        rootView = decorView.getViewTreeObserver();
        rootView.addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                Log.e(TAG, "onScrollChanged " + contentView.findFocus());
                if (canvasHandler.hasMessages(0)) {
                    canvasHandler.removeMessages(0);
                }
                canvasHandler.sendEmptyMessageDelayed(0, 30);
            }
        });

        rootView.addOnGlobalFocusChangeListener(new ViewTreeObserver.OnGlobalFocusChangeListener() {
            @Override
            public void onGlobalFocusChanged(final View oldFocus, final View newFocus) {
                Log.d(TAG, "GlobalFocusChanged oldFocus : " + oldFocus + " ,newFocus : " + newFocus);
                canvasHandler.onFocusChange(oldFocus, newFocus);
                canvasHandler.sendEmptyMessageDelayed(0, 30);
            }
        });



//        TODO 状态栏的高度可能会影响view的绘制
//        /* 获取状态栏高度——方法 **/
//        //获取status_bar_height资源的ID
//        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
//        if (resourceId > 0) {
//            //根据资源ID获取响应的尺寸值
//            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
//        }

    }

    private void attachFocusBox(View oldFocus, View newFocus) {
        fromRect = findViewToRect(oldFocus);
        toRect = findViewToRect(newFocus);

        if (toRect == null) {
            return;
        }

        if (toRect.right >= getRight()
                || toRect.bottom >= getBottom()
                || toRect.left <= 0
                || toRect.top <= 0) {
            return;
        }

        if (currentRect != null) {
            if (currentRect.contains(toRect)) {
                Log.d(TAG, "toRect = currentRect");
                return;
            }
        }

        Log.i(TAG, "invalidate");
        // 重新绘制之前，初始化时间值，重新计算位移动画
        startTime = SystemClock.uptimeMillis();
        invalidate();
    }

    /**
     * 将View的位置转化成Rect
     * @param view
     * @return
     */
    public Rect findViewToRect(View view) {
        Rect rect = new Rect();
        if (view == null) {
            return null;
        }
        //获取rect相对于整个屏幕的位置
        view.getGlobalVisibleRect(rect);
        setBorderRect(rect);
        return rect;
    }

    /**
     * 设置BorderView的位置大小
     * @param rect
     * @return
     */
    private Rect setBorderRect(Rect rect) {
        rect.left = rect.left - borderWidth;
        rect.top = rect.top - borderWidth;
        rect.right = rect.right + borderWidth;
        rect.bottom = rect.bottom + borderWidth;
        return rect;
    }

    /**
     * 设置画笔
     * @return Paint
     */
    public Paint getPaint() {
        if (paint == null) {
            paint = new Paint();
            paint.setStyle(Paint.Style.STROKE);//设置空心
            paint.setColor(Color.BLUE);
            paint.setStrokeWidth(5);//设置画笔粗细
        }

        return paint;
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        if (toRect == null) {
            return;
        }

        if (toRect != null && fromRect == null) {
            currentRect = toRect;
            canvas.drawRect(toRect, getPaint());
            return;
        }

        long curTime = SystemClock.uptimeMillis();
        // t为一个0到1均匀变化的值
        float t = (curTime - startTime) / (float) duration;
        t = Math.max(0, Math.min(t, 1));

        if (currentRect == null) {
            if (fromRect != null) {
                currentRect = fromRect;
            } else {
                return;
            }
        }
        int left = (int) lerp(currentRect.left, toRect.left, t);
        int top = (int) lerp(currentRect.top, toRect.top, t);
        int right = (int) lerp(currentRect.right, toRect.right, t);
        int bottom = (int) lerp(currentRect.bottom, toRect.bottom, t);
        boolean done = true;
        if (0 <= t && t < 1) {
            canvasStatus = 1;
            done = false;

            // 保存画布，方便下次绘制
            canvas.save();
            if (translateRect == null) {
                translateRect = new Rect();
            }
            translateRect.left = left;
            translateRect.top = top;
            translateRect.right = right;
            translateRect.bottom = bottom;
            currentRect = translateRect;
            canvas.drawRect(translateRect, getPaint());
            canvas.restore();
        } else {
            currentRect = toRect;
            canvasStatus = 2;
            canvas.drawRect(toRect, getPaint());
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
        private WeakReference<BorderView> borderViewWR;
        public CanvasHandler(WeakReference<BorderView> borderViewWeakReference) {
            this.borderViewWR = borderViewWeakReference;
        }

        private View oldFocus, newFocus;
        public void onFocusChange(View oldFocus, View newFocus) {
            this.oldFocus = oldFocus;
            this.newFocus = newFocus;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (borderViewWR == null && borderViewWR.get() == null) {
                return;
            }

            switch (msg.what) {
                case 0:
                    borderViewWR.get().attachFocusBox(oldFocus, newFocus);
                    break;
            }
        }
    }
}
