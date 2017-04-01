package com.zhangjie.easypoint;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;

import java.lang.reflect.Field;

/**
 * Created by zhangjie on 2016/1/30.
 */
public class PointView extends LinearLayout{
	private Context mContext;
    /**
     * 记录小圆点的宽度
     */
    public int viewWidth;
    /**
     * 记录小圆点的高度
     */
    public int viewHeight;
    /**
     * 记录系统状态栏的高度
     */
    private int statusBarHeight;
    /**
     * 用于更新小圆点的位置
     */
    private WindowManager windowManager;
    /**
     * 小圆点的参数
     */
    private WindowManager.LayoutParams mParams;
    /**
     * 记录当前手指位置在屏幕上的横坐标值
     */
    private float xInScreen;
    /**
     * 记录当前手指位置在屏幕上的纵坐标值
     */
    private float yInScreen;
    /**
     * 记录手指按下时在屏幕上的横坐标的值
     */
    private float xDownInScreen;
    /**
     * 记录手指按下时在屏幕上的纵坐标的值
     */
    private float yDownInScreen;
    /**
     * 记录手指按下时在小圆点的View上的横坐标的值
     */
    private float xInView;
    //按下时间
    private long time;
    /**
     * 记录手指按下时在小圆点的View上的纵坐标的值
     */
    private float yInView;
    
    private AccessibilityService mService;
    private View mView;
    private Vibrator mVibrator;
    private int vibrator_val,screenwidth;
    private SharedPreferences sharedPreferences;
    private boolean isHide;

    public PointView(Context context,AccessibilityService service,Vibrator vibrator) {
        super(context);
        mContext=context;
        mService=service;
        mVibrator=vibrator;
        windowManager= (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        LayoutInflater.from(context).inflate(R.layout.point_simple,this);
        mView=findViewById(R.id.point_view);
        screenwidth=windowManager.getDefaultDisplay().getWidth();
        viewWidth=mView.getLayoutParams().width;
        viewHeight=mView.getLayoutParams().height;
        sharedPreferences=context.getSharedPreferences("setting", Context.MODE_PRIVATE);
        vibrator_val=sharedPreferences.getInt("vibrate", 0);
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
                time=event.getDownTime()-lasTime;
                lasTime=event.getDownTime();

                mView.setBackgroundResource(R.drawable.clickshape);
                mView.getBackground().setAlpha(255);
                AlphaAnimation alphaAnimation= new AlphaAnimation(0.5f,1.0f);
                alphaAnimation.setDuration(1000);
                ScaleAnimation scaleAnimation=new ScaleAnimation(0.8f, 1.0f, 0.8f, 1.0f,
                        Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                scaleAnimation.setDuration(300);
                mView.startAnimation(alphaAnimation);
                mView.startAnimation(scaleAnimation);
                break;
            case MotionEvent.ACTION_MOVE:
                xInScreen = event.getRawX();
                yInScreen = event.getRawY() - getStatusBarHeight();
                // 手指移动的时候更新小悬浮窗的位置
                float pressTime=event.getEventTime()-event.getDownTime();
                mView.getBackground().setAlpha(255);
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
                if (wLength<200&&yLength>0&&hLength>40&&!isMove){
                    Log.i("上划,y", "" + wLength + "/" + yLength);
                    mService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_RECENTS);
                    mVibrator.vibrate(vibrator_val);
                }else if(wLength<200&&yLength<0&&hLength>40&&!isMove){
                    Log.i("下划,y", ""+wLength+"/" +yLength);
                    // 模拟HOME键
                    Intent i = new Intent(Intent.ACTION_MAIN);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // 如果是服务里调用，必须加入new task标识
                    i.addCategory(Intent.CATEGORY_HOME);
                    mContext.startActivity(i);
                    mVibrator.vibrate(vibrator_val);
                }else if(wLength<10&&hLength<40&&!isMove){
                    Log.i("点击,y", "" + wLength + "/" + yLength);
                    mService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
                    mVibrator.vibrate(vibrator_val);
                }else if(xLength>10&&hLength<80&&!isMove){
                    Log.i("左滑,y", "" + xLength + "/" + yLength);
                    if (isHide){
                        showView();
                    }else {
                        mService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS);
                        mVibrator.vibrate(vibrator_val);
                    }

                }else if(!isMove){
                    //Log.i("右滑,y", "" + xLength + "/" + yLength);右划现在不处理
                    /*if (!isHide){
                        hideView();
                        mVibrator.vibrate(vibrator_val);
                    }*/
                }else {
                    Log.i("x,y", "" +xLength+"/"+ wLength + "/" + yLength);
                }
                mView.setBackgroundResource(R.drawable.shape);
                int alpha=sharedPreferences.getInt("alpha",50);
                //Log.i("set alpha",""+alpha);
                mView.getBackground().setAlpha(alpha);
                break;
            default:
                break;
        }
        return false;
    }

    /**
     * 将小圆点的参数传入，用于更新小圆点的位置。
     *
     * @param params
     *            小圆点的参数
     */
    public void setParams(WindowManager.LayoutParams params) {
        mParams = params;
    }

    public void setVibrator_val(int value){
        vibrator_val=value;
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

    private void hideView() {
        mParams.width=viewWidth-30;
        //mView.getLayoutParams().width=viewWidth-30;
        isHide=true;
        mParams.x = (int) (xInScreen - xInView);
        windowManager.updateViewLayout(this, mParams);
    }

    private void showView() {
        Log.i("width",""+viewWidth);
        isHide=false;
        mParams.width=viewWidth;
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


}
