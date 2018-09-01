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
    private int borderSpace = 0;        //边框间距
    private int borderWidth = 5;        //边框粗细
    private int statusBarHeight = 0;    //状态栏高度
    private long startTime = -1;
    private long duration = 200;        //位移动画时间
    private int canvasStatus = 0;       //画布绘制状态 0 未绘制;1 正在绘制; 2 绘制完毕
    private boolean stopCanvas = false;
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
                stopCanvas = true;
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
                attachFocusBox(oldFocus, newFocus);
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

        //如果borderview当前位置不为null，且和要位移的位置相同，直接return不处理。
        if (currentRect != null) {
            if (currentRect.contains(toRect)) {
                Log.d(TAG, "toRect = currentRect");
                return;
            }
        }

        startInvalidate();
    }

    /**
     * 将View的位置转化成Rect
     *
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
     *
     * @param rect
     * @return
     */
    private Rect setBorderRect(Rect rect) {
        rect.left = rect.left - borderSpace;
        rect.top = rect.top - borderSpace;
        rect.right = rect.right + borderSpace;
        rect.bottom = rect.bottom + borderSpace;
        return rect;
    }

    /**
     * 设置画笔
     *
     * @return Paint
     */
    public Paint getPaint() {
        if (paint == null) {
            paint = new Paint();
            paint.setStyle(Paint.Style.STROKE);//设置空心
            paint.setColor(Color.BLUE);
            paint.setStrokeWidth(borderWidth);//设置画笔粗细
        }

        return paint;
    }

    /**
     * 开始绘制borderview
     */
    private void startInvalidate() {
        Log.i(TAG, "startInvalidate");
        // 重新绘制之前，初始化时间值，重新计算位移动画
        startTime = SystemClock.uptimeMillis();
        stopCanvas = false;
        invalidate();
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        if (toRect == null) {
            return;
        }

        //如果目标view不为空，但是fromRect 和 currentRect 为空的话，直接绘制到
        if (toRect != null
                && fromRect == null && currentRect == null) {
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

        boolean done = true;
        Log.i(TAG, "t : " + t);
        if (0 <= t && t < 1) {
            canvasStatus = 1;
            done = false;

            // 保存画布，方便下次绘制
            canvas.save();
            currentRect = getTranslateRect(currentRect, toRect, t);
            canvas.drawRect(currentRect, getPaint());
            canvas.restore();
        } else {
            currentRect = toRect;
            canvasStatus = 2;
            canvas.drawRect(toRect, getPaint());
        }

        Log.i(TAG, "done :" + done + " ,stopCanvas : " + stopCanvas);
        if (!done && !stopCanvas) {
            invalidate();
        }
    }

    /**
     * 根据当前的时间差值，得到将要绘制到画布上的borderview的位置
     * @param rect
     * @param toRect
     * @param t
     */
    private Rect getTranslateRect(Rect rect, Rect toRect, float t) {
        if (translateRect == null) {
            translateRect = new Rect();
        }
        int left = (int) lerp(rect.left, toRect.left, t);
        int top = (int) lerp(rect.top, toRect.top, t);
        int right = (int) lerp(rect.right, toRect.right, t);
        int bottom = (int) lerp(rect.bottom, toRect.bottom, t);
        translateRect.left = left;
        translateRect.top = top;
        translateRect.right = right;
        translateRect.bottom = bottom;
        return translateRect;
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

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (borderViewWR == null && borderViewWR.get() == null) {
                return;
            }

            switch (msg.what) {
                case 0:
//                    borderViewWR.get().attachFocusBox(oldFocus, newFocus);
                    Log.i(TAG, "handleMessage");
                    borderViewWR.get().attachFocusBox(null, borderViewWR.get().contentView.findFocus());
                    break;
            }
        }
    }
}
