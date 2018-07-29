package com.zhangjie.easypoint;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.LinearLayout;

import java.lang.reflect.Field;

/**
 * Created by zhangjie on 2018/7/28.
 */
public class PointHideView extends LinearLayout{
    private static final String TAG = "PointHideView";
    private Context mContext;
    private AccessibilityService mService;
    private Vibrator mVib;
    public int viewWidth;
    public int viewHeight;
    private int statusBarHeight;
    private WindowManager windowManager;
    private WindowManager.LayoutParams mParams;
    private float xInScreen;
    private float yInScreen;
    private float xDownInScreen;
    /**
     * 记录手指按下时在屏幕上的纵坐标的值
     */
    private float yDownInScreen;
    /**
     * 记录手指按下时在小圆点的View上的横坐标的值
     */
    private float xInView;
    /**
     * 记录手指按下时在小圆点的View上的纵坐标的值
     */
    private float yInView;

    private View mView;
    private SharedPreferences sharedPreferences;
    private Paint paint;
    private RectF oval;
    private MyWindowManager myWindowManager;

    public PointHideView(Context context,AccessibilityService service,Vibrator vibrator) {
        super(context);
        mContext=context;
        mService=service;
        mVib=vibrator;
        windowManager= (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        myWindowManager=new MyWindowManager();
        LayoutInflater.from(context).inflate(R.layout.point_hide,this);
        mView=findViewById(R.id.point_hide_view);
        viewWidth=mView.getLayoutParams().width;
        viewHeight=mView.getLayoutParams().height;
        sharedPreferences=context.getSharedPreferences("setting", Context.MODE_PRIVATE);

        initPaint();
    }

    private void initPaint() {
        paint = new Paint();
        oval = new RectF();
    }

    boolean isMove=false;
    long lasTime=0;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                xInView = event.getX();
                yInView = event.getY();
                xDownInScreen = event.getRawX();
                yDownInScreen = event.getRawY() - getStatusBarHeight();
                xInScreen = event.getRawX();
                yInScreen = event.getRawY() - getStatusBarHeight();

                //time=event.getDownTime();//按下时间
                lasTime=event.getDownTime();

//                mView.setBackgroundResource(R.drawable.hideshape);
//                mView.getBackground().setAlpha(255);
                AlphaAnimation alphaAnimation= new AlphaAnimation(0.5f,1.0f);
                alphaAnimation.setDuration(1000);
                ScaleAnimation scaleAnimation=new ScaleAnimation(0.8f, 1.0f, 0.8f, 1.0f,
                        Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                scaleAnimation.setDuration(300);
//                mView.startAnimation(alphaAnimation);
//                mView.startAnimation(scaleAnimation);
                break;
            case MotionEvent.ACTION_MOVE:
                xInScreen = event.getRawX();
                yInScreen = event.getRawY() - getStatusBarHeight();
                // 手指移动的时候更新小悬浮窗的位置
                float pressTime=event.getEventTime()-event.getDownTime();
//                mView.getBackground().setAlpha(255);
                if(pressTime>1000){
                    updateViewPosition();
                    isMove=true;
                    break;
                }else {
                    isMove=false;
                }
                break;
            case MotionEvent.ACTION_UP:
                // 如果手指离开屏幕时，xDownInScreen和xInScreen相等，且yDownInScreen和yInScreen相等，则视为触发了单击事件。
                float yLength=yDownInScreen-yInScreen;//y轴距离，有方向
                float xLength=xDownInScreen-xInScreen;//x轴距离，有方向
                float wLength=Math.abs(xDownInScreen-xInScreen);//x轴距离
                float hLength=Math.abs(yDownInScreen - yInScreen);//y轴距离
                //if(time<500) isDoubleClick=true;
                if(!isMove){
                    // todo 只要碰到就进入普通模式
                    Log.i("左滑,y", "" + xLength + "/" + yLength);
                    myWindowManager.removeHidePoint(mContext);
                    myWindowManager.createEasyPoint(mContext,mService,mVib);
                }else {
                    Log.i("x,y", "" +xLength+"/"+ wLength + "/" + yLength);
                }
//                mView.setBackgroundResource(R.drawable.shape);
                int alpha=sharedPreferences.getInt("alpha",50);
                //Log.i("set alpha",""+alpha);
                mView.getBackground().setAlpha(alpha);
                break;
            default:
                break;
        }
        return false;
    }

    public void setParams(WindowManager.LayoutParams params) {
        mParams = params;
    }
    /**
     * 更新小圆点在屏幕中的位置。
     */
    private void updateViewPosition() {
        mParams.x = (int) (xInScreen - xInView);
        mParams.y = (int) (yInScreen - yInView);
        //Log.i("width",""+mParams);
        windowManager.updateViewLayout(this, mParams);
    }

    /**
     * 用于获取状态栏的高度。
     *
     * @return 返回状态栏高度的像素值。
     */
    private int getStatusBarHeight() {
        if (statusBarHeight == 0) {
            try {
                Class<?> c = Class.forName("com.android.internal.R$dimen");
                Object o = c.newInstance();
                Field field = c.getField("status_bar_height");
                int x = (Integer) field.get(o);
                statusBarHeight = getResources().getDimensionPixelSize(x);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return statusBarHeight;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // draw ring
        super.onDraw(canvas);
        // this width height is defined in xml
        float width = (float) getWidth();
        float height = (float) getHeight();
        float radius;

        if (width > height) {
            radius = height / 3;
        } else {
            radius = width / 3;
        }

        paint.setAntiAlias(true);
        paint.setColor(Color.parseColor("#939393"));
        paint.setStrokeWidth(5);
        paint.setStyle(Paint.Style.FILL);

        float center_x, center_y;
        paint.setStyle(Paint.Style.STROKE);

        int screenWidth = windowManager.getDefaultDisplay().getWidth();
        Log.i(TAG, "onDraw: screenWidth"+ screenWidth);
        center_x = width / 2;
        center_y = height / 2;

        // left top right bottom
        oval.set(center_x + radius/2 ,
                center_y - radius,
                width + radius,
                center_y + radius);
        canvas.drawArc(oval, 90, 180, false, paint);
    }

}
