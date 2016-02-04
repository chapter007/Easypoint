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
import android.widget.LinearLayout;

import java.lang.reflect.Field;

/**
 * Created by zhangjie on 2016/1/30.
 */
public class PointView extends LinearLayout{
    /**
     * 记录小圆点的宽度
     */
    public static int viewWidth;
    /**
     * 记录小圆点的高度
     */
    public static int viewHeight;
    /**
     * 记录系统状态栏的高度
     */
    private static int statusBarHeight;
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
    private Context mContext;
    private AccessibilityService mService;
    private View mView;
    private Vibrator mVibrator;
    private int vibrator_val;
    private SharedPreferences sharedPreferences;

    public PointView(Context context,AccessibilityService service,Vibrator vibrator) {
        super(context);
        mContext=context;
        mService=service;
        mVibrator=vibrator;
        windowManager= (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        LayoutInflater.from(context).inflate(R.layout.point_simple,this);
        mView=findViewById(R.id.point_view);
        viewWidth=mView.getLayoutParams().width;
        viewHeight=mView.getLayoutParams().height;
        sharedPreferences=context.getSharedPreferences("setting",Context.MODE_PRIVATE);
        vibrator_val=sharedPreferences.getInt("vibrate",0);
    }

    boolean isMove=false;

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
                time=event.getDownTime();
                mView.setBackgroundResource(R.drawable.clickshape);
                mView.getBackground().setAlpha(255);
                break;
            case MotionEvent.ACTION_MOVE:
                xInScreen = event.getRawX();
                yInScreen = event.getRawY() - getStatusBarHeight();
                // 手指移动的时候更新小悬浮窗的位置
                float pressTime=event.getEventTime()-time;
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
                float length=yDownInScreen-yInScreen;
                float wlength=Math.abs(xDownInScreen-xInScreen);
                float hlength=Math.abs(yDownInScreen - yInScreen);

                if (wlength<200&&length>0&&hlength>40&&!isMove){
                    Log.i("上划,y", "" + wlength + "/" + length);
                    mService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_RECENTS);
                    mVibrator.vibrate(vibrator_val);
                }else if(wlength<200&&length<0&&hlength>40&&!isMove){
                    Log.i("下划,y", ""+wlength+"/" +length);
                    // 模拟HOME键
                    Intent i = new Intent(Intent.ACTION_MAIN);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // 如果是服务里调用，必须加入new task标识
                    i.addCategory(Intent.CATEGORY_HOME);
                    mContext.startActivity(i);
                    mVibrator.vibrate(vibrator_val);
                }else if(wlength<40&&hlength<40&&!isMove){
                    Log.i("点击,x,y", "" + wlength + "/" + hlength);
                    mService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
                    mVibrator.vibrate(vibrator_val);
                }else {
                    Log.i("x,y", ""+wlength+"/"+length);
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
