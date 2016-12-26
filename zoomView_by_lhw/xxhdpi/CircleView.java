package com.android.camera;

import com.android.camera.manager.ZoomManager;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

/**
 * zoom camera bottom circle view controller 
 * @author lhw
 *
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
    private float currentAngle, startAngle, endAngle ; 
    private float scaleAngleShowFloat;
    private final float arcLength = 70;//90; // 120  draw center circle length
    private final float totalRotate = 70;//90; // 60 The rest of the rotation Angle
    private boolean scrollleft = false; // true left  false right
    private final float factor =0.08f;
    private Rect rect;
    private float centerRaidus = 40; // center circle show number raidus
    private float downX=0f;
    private float downY=0f;
    private boolean downFlag=true;
    float moveRight=0f;
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
	private Canvas canvas;
	public float showZoomValue=0;
	public ZoomManager mZoom=null;
	public onCircleZoomListener mCircleZoomListener=null;
	  
	public void setZoomManger(ZoomManager zoom){
		this.mZoom=zoom;
	}
   public  void setCircleZoomListener(onCircleZoomListener listener){
	  	  this.mCircleZoomListener=listener;
	 }
   public interface onCircleZoomListener{
  	  void onTouchCircleZoom(int index);
  	  void showCircle(boolean flag);
   }
   private long startTime=0;
   private long endTime=0;
    private void init() {
        paint = new Paint();
        canvas = new Canvas();
        currentAngle = 0f;
        startAngle =20f;// 0f;//30f; // init start angle 
        endAngle =90f;// 30f + arcLength; // init end angle 
        rect = new Rect();
        gestureDetectorCompat = new GestureDetectorCompat(getContext(), new GestureDetector.OnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {//show zoom number circle onclick 
            	
            	startTime=System.currentTimeMillis();	
            	/*if(e.getX()>downX && e.getY()<=downY+20){
            		if(downFlag){
            		downFlag=false;
              		 scaleAngleShowFloat=20.0f;
              		startAngle =15f;
            	    endAngle =105f;
            		}else{
            		downFlag=true;	
            		scaleAngleShowFloat=0.0f;
            		startAngle = 0f;
            	    endAngle =90f;
            		}
            	   
            		invalidate();
            	}*/
            	invalidate();
                return true;//true
            }

            @Override
            public void onShowPress(MotionEvent e) {
            	

            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
            
            	endTime=System.currentTimeMillis();	
            		mCircleZoomListener.showCircle(true);


                return true;
            }

            @Override
            public boolean onScroll(final MotionEvent e1, final MotionEvent e2, float distanceX, float distanceY) {
                post(new Runnable() {
                    @Override
                    public void run() {
                    	
                        if (e1.getX() - e2.getX() > FLING_MIN_DISTANCE ) {
                            // left scroll 
                        	moveRight=  (e1.getX() - e2.getX()) * factor;
                            rotatAngle = (Math.abs(moveRight)/ radius ) * totalRotate ;
                            scrollleft = true;

                        }else if (e2.getX()-e1.getX() > FLING_MIN_DISTANCE) {
                            // right scroll 
                            moveRight=  (e2.getX() - e1.getX()) * factor;
                            rotatAngle = (Math.abs(moveRight) / radius) * totalRotate;
                            scrollleft = false;
                        }
                        ValueAnimator valueAnimator = ValueAnimator .ofFloat(0f, moveRight);//rotatAngle

                        valueAnimator.setDuration(100);
                        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator animation) {
                                float animatedValue = (Float) animation.getAnimatedValue();
                                // this update cancas animation refresh draw circle 
                                synchronized (CircleView.class) {
                                    setAngle(animatedValue);
                                }

                               // Log.i("CircleView", "animatedValue="+animatedValue+ ",moveRight=" + moveRight);


                            }
                        });
                       valueAnimator.start();
                    }
                });

                return true;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {//

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
        		
        this.canvas=canvas;
        this.canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG)); //去掉锯齿
        cy = getBottom();
        // Circle radius
        radius = (getRight() - getLeft() - getPaddingRight() - getPaddingLeft())/2+10;
        cx = getWidth()/2;
        paint.setColor(Color.GRAY);
        paint.setAlpha(90);
      //  Log.i("LHW", "cy"+cy+ ", cx=" +cx);// 1040 360
        // draw gray backgroud color
        this.canvas.drawCircle(cx, cy+220, radius, paint);//cy+220 Set the Y distances
   /*     RectF oval=new RectF();  
        oval.left=500;                              //左边  
        oval.top=100;                                   //上边  
        oval.right=400;                             //右边  
        oval.bottom=300;   
                                                 //下边  //RectF对象  
        this.canvas.drawArc(oval, 255, 90, false,  paint);*/
       // Log.i("CircleView", "getRight"+getRight() + ", getBottom=" + getBottom());

        this.canvas.save();

        //show zoom number circle
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(3);
        paint.setStyle(Paint.Style.STROKE);
        downX=cx;
        downY=cy - radius;
        this.canvas.drawCircle(cx, cy - radius+220, centerRaidus-3, paint);

//        Region region = new Region((int) (cx - radius), (int) (cy - radius), (int) (cx + radius), (int) (cy + radius));

        rect.set((int)(cx- centerRaidus), (int)(cy - radius - centerRaidus), (int)(cx + centerRaidus), (int)(cy - radius + centerRaidus));

        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(20);

       
        this.canvas.drawText( showZoomValue+"x", cx -20 , cy - radius+220, paint);
        
        //mZoom.performZoom((int)showZoomValue,true);
        
        this.canvas.restore();
       // Log.i("CircleView", "cx="+cx + ", cy=" + cy);
        
        
        drawCircle();
       

    }
     public float getShowZoomValue() {
		return showZoomValue;
	}

	/**
      * draw little  circle
      */
	private void drawCircle() {
		for (float a = startAngle; a < endAngle; a += 2) {
            float smallCx = (float) (cx + radius*Math.cos(a * Math.PI/180)); 
            float smallCy = (float) (cy - radius*Math.sin(a * Math.PI/180));
            paint.setStyle(Paint.Style.FILL);
           
            if (!rect.contains((int)smallCx, (int)smallCy)){
            	
            	 if(a==startAngle){
            		 this.canvas.drawText("10x",smallCx, smallCy+220, paint); 
            	 }else if(a==endAngle-2){
            		 this.canvas.drawText("1x",smallCx, smallCy+220, paint); 	 
            	 }else{
            		 this.canvas.drawCircle(smallCx, smallCy+220,5, paint);
            	 }
            	// this.canvas.drawCircle(smallCx, smallCy, 4, paint);
            }

           // Log.i("CircleView", "Math.sin(a)="+Math.sin(a * Math.PI/180) + ", Math.cos(a)=" + Math.cos(a*Math.PI/180));
           //Log.i("CircleView", "smallCx="+smallCx + ", smallCy=" + smallCy + ", 角度=" + a);
        }
	}

    // scale Angle
    private void setAngle(float scaleAngle) {
        // scaleAngle Zoom multiples
    	
        if (scaleAngle >=10.0f)
            scaleAngle = 10.0f;
        if (scaleAngle < 1.0f)
            scaleAngle = 0.0f;
       // this.scaleAngleShowFloat = scaleAngle;
     //  Log.i("CircleView", "scaleAngleShowFloat="+scaleAngleShowFloat +" arcLength="+arcLength);
     //  Log.i("CircleView", "startAngle="+startAngle +" rotatAngle="+rotatAngle);
    
        float startAngleTemp, endAngleTemp = 0;
        // rotatAngle/8  get circle rotate angle
        if (scrollleft) { // left  scorll ,on start angle add angle restart draw ,and animation is start  
            startAngleTemp = startAngle + (scaleAngle * (rotatAngle/8));
            endAngleTemp = startAngle + arcLength; //
        } else {// right scorll 
            startAngleTemp = startAngle - (scaleAngle * (rotatAngle/8));
            endAngleTemp = startAngle + arcLength; //
        }
       // Log.i("CircleView", "startAngleTemp="+startAngleTemp +" endAngleTemp="+endAngleTemp);
        // Limit the position of the start and end
        if (startAngleTemp >= 20f && startAngleTemp <= 90f && endAngleTemp <= 160f && endAngleTemp >= 90f) {
            startAngle = startAngleTemp;
            endAngle = endAngleTemp;
            
            this.scaleAngleShowFloat =startAngle>10?startAngle:0;
            // show zoom number
        	float showNum=((float)Math.round(scaleAngleShowFloat/10)%10);
        
            Log.i("LHW","scaleAngleShowFloat="+scaleAngleShowFloat+" showNum="+showNum);
        	showZoomValue = (showNum >=9.0f?showNum+1:showNum<=2?1.0f:showNum);
        	mCircleZoomListener.onTouchCircleZoom((int)showZoomValue);
          //  Log.e("LHW","setAngle scaleAngleShowFloat="+scaleAngleShowFloat);
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
