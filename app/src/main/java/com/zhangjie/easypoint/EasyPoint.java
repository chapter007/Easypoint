package com.zhangjie.easypoint;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by zhangjie on 2016/1/30.
 */
public class EasyPoint extends AccessibilityService {

    private AccessibilityService service;
    private Vibrator vibrator;
    private Configuration cf;
    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }


    public EasyPoint(){
        service=this;
    }

    /**
     * 用于在线程中创建或移除悬浮窗。
     */
    private Handler handler = new Handler();

    /**
     * 定时器，定时进行检测当前悬浮窗透明度。
     */
    private Timer timer;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //开启定时器，每隔5秒检测一下
        if (timer==null){
            timer=new Timer();
            timer.scheduleAtFixedRate(new RefreshTask(),0,1000);
        }
        cf= getApplicationContext().getResources().getConfiguration(); //获取设置的配置信息

        vibrator= (Vibrator) getSystemService(VIBRATOR_SERVICE);
        //创建小圆点
        handler.post(new Runnable() {
            @Override
            public void run() {
                MyWindowManager.createEasyPoint(getApplicationContext(), service,vibrator);
            }
        });
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //计时器也销毁
        Log.i("destroy","");
        timer.cancel();
        timer=null;
        //关闭圆点
        handler.post(new Runnable() {
            @Override
            public void run() {
                MyWindowManager.removeEasyPoint(getApplicationContext());
            }
        });
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {

    }

    @Override
    public void onInterrupt() {

    }

    class RefreshTask extends TimerTask{
        //检测横屏隐藏圆点
        @Override
        public void run() {
            int ori = cf.orientation ; //获取屏幕方向
            //Log.i("timer task",""+ori);
            if(ori == cf.ORIENTATION_LANDSCAPE){
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        MyWindowManager.updateEasyPoint(getApplicationContext(), 0, 1, 0);
                    }
                });

            }else if(ori == cf.ORIENTATION_PORTRAIT){
                //Log.i("竖屏",""+2);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        int alpha=getSharedPreferences("setting",MODE_PRIVATE).getInt("alpha", 50);
                        MyWindowManager.updateEasyPoint(getApplicationContext(), 0, alpha, 0);
                    }
                });
            }

            /*if (!MyWindowManager.isWindowShowing()){
                //创建小圆点
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        MyWindowManager.createEasyPoint(getApplicationContext(), service,vibrator);
                    }
                });
            }else {
                //修改小圆点透明度
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        int vib=getSharedPreferences("setting",MODE_PRIVATE).getInt("vibrate",0);
                        int alpha=getSharedPreferences("setting",MODE_PRIVATE).getInt("alpha", 0);
                        int size=getSharedPreferences("setting",MODE_PRIVATE).getInt("size", 0);
                        MyWindowManager.updateEasyPoint(getApplicationContext(),vib,alpha,size);
                    }
                });
            }*/

        }
    }
}
