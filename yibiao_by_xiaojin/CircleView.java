package com.jin.yibiao;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

/**
 * @author Administrator
 *         Date 2016/11/2
 */
public class CircleView extends View {
    private static final int FLING_MIN_DISTANCE = 5;
    private static final int FLING_MIN_VELOCITY = 0;
    private Paint paint;
    private GestureDetectorCompat gestureDetectorCompat;
    private float cy;
    private float radius;
    private float cx;
    private float rotatAngle;
    private float currentAngle, startAngle, endAngle ; // 当前的角度
    private float scaleAngleShowFloat;
    private final float arcLength = 120; //中间的圆弧长度
    private final float totalRotate = 60; // 剩余的旋转角度是60
    private boolean scrollleft = false; // true就是向左 false 向右
    private final float factor = 0.3f;
    private Rect rect;
    private float centerRaidus = 42; // 中间显示数字圆的半径
    public CircleView(Context context) {
        super(context);
        init();
    }

    public CircleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CircleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    private boolean isInit = false;
    private void init() {
        paint = new Paint();
        currentAngle = 0f;
        startAngle = 30f; // 这里是初始化开始的角度
        endAngle = 30f + arcLength; // 这里是结束的角度
        rect = new Rect();
        gestureDetectorCompat = new GestureDetectorCompat(getContext(), new GestureDetector.OnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {

                return true;
            }

            @Override
            public void onShowPress(MotionEvent e) {

            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return false;
            }

            @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
            @Override
            public boolean onScroll(final MotionEvent e1, final MotionEvent e2, float distanceX, float distanceY) {
                post(new Runnable() {
                    @Override
                    public void run() {
                        if (e1.getX() - e2.getX() > FLING_MIN_DISTANCE ) {
                            // 向左滑动
                            float moveX =  (e1.getX() - e2.getX()) * factor;
                            rotatAngle = (Math.abs(moveX)/ radius ) * totalRotate ;
                            scrollleft = true;
                        }else if (e2.getX()-e1.getX() > FLING_MIN_DISTANCE) {
                            // 向右滑动
                            float moveX =  (e2.getX() - e1.getX()) * factor;
                            rotatAngle = (Math.abs(moveX) / radius) * totalRotate;
                            scrollleft = false;
                        }
                        // 属性动画
                        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0f, rotatAngle);

                        valueAnimator.setDuration(100);
                        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator animation) {
                                float animatedValue = (float) animation.getAnimatedValue();
                                // 这里更新画布实现动画
                                synchronized (CircleView.class) {
                                    setAngle(animatedValue);
                                }

                                Log.i("CircleView", "animatedValue="+animatedValue+ ",rotatAngle=" + rotatAngle);


                            }
                        });
                        valueAnimator.start();
                    }
                });

                return true;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

                return true;
            }
            @Override
            public void onLongPress(MotionEvent e) {

            }


        });
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        cy = getBottom();
        // 半径
        radius = (getRight() - getLeft() - getPaddingRight() - getPaddingLeft())/2;
        cx = getWidth()/2;
        paint.setColor(Color.GRAY);
        // 先绘制一个灰色背景
        canvas.drawCircle(cx, cy, radius, paint);

        Log.i("CircleView", "getRight"+getRight() + ", getBottom=" + getBottom());

        canvas.save();

        // 这里是显示中间的 放大的倍数什么的
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(5);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawCircle(cx, cy - radius, centerRaidus, paint);

//        Region region = new Region((int) (cx - radius), (int) (cy - radius), (int) (cx + radius), (int) (cy + radius));

        rect.set((int)(cx- centerRaidus), (int)(cy - radius - centerRaidus), (int)(cx + centerRaidus), (int)(cy - radius + centerRaidus));

        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(20);

        // 显示倍数文字
        canvas.drawText(" X "+ (float)Math.round(scaleAngleShowFloat*100)/100, cx - 20 , cy - radius, paint);

        canvas.restore();

        for (float a = startAngle; a < endAngle; a += 2) {
            // 这里是画那一圈的点点
            float smallCx = (float) (cx + radius*Math.cos(a * Math.PI/180)); // 这里sin的角度是弧度制。
            float smallCy = (float) (cy - radius*Math.sin(a * Math.PI/180));
            paint.setStyle(Paint.Style.FILL);
            if (!rect.contains((int)smallCx, (int)smallCy))
                canvas.drawCircle(smallCx, smallCy, 4, paint);

            Log.i("CircleView", "Math.sin(a)="+Math.sin(a * Math.PI/180) + ", Math.cos(a)=" + Math.cos(a*Math.PI/180));
            Log.i("CircleView", "smallCx="+smallCx + ", smallCy=" + smallCy + ", 角度=" + a);
        }


    }

    // 缩放的大小
    private void setAngle(float scaleAngle) {
        // scaleAngle缩放的倍数 ( 1 ~8 之间的float的大小)

        if (scaleAngle > 8.0f)
            scaleAngle = 8.0f;
        if (scaleAngle < 1.0f)
            scaleAngle = 0.0f;
        this.scaleAngleShowFloat = scaleAngle;
        float startAngleTemp, endAngleTemp = 0;
        // rotatAngle/8 把剩余60 度 分成八分，然后计算最后要旋转到怎么样的一个角度
        if (scrollleft) { // 向左滚动 ,在原有的地方继续添加角度，进行重新绘制，就出动画效果
            startAngleTemp = startAngle + (scaleAngle * (rotatAngle/8));
            endAngleTemp = startAngle + arcLength; //
        } else {// 向右滚动
            startAngleTemp = startAngle - (scaleAngle * (rotatAngle/8));
            endAngleTemp = startAngle + arcLength; //
        }
        // 开始结束位置都有限制
        if (startAngleTemp >= 0f && startAngleTemp <= 60f && endAngleTemp <= 180f && endAngleTemp >= 120f) {
            // 只有真正移动了，才去更新真正的开始位置
            startAngle = startAngleTemp;
            endAngle = endAngleTemp;

            invalidate();
        }


    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetectorCompat.onTouchEvent(event);

    }
}
