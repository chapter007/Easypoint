package com.zhangjie.easypoint;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.inputmethod.InputMethodManager;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by zhangjie on 2016/1/30.
 */
public class EasyPoint extends AccessibilityService {
    private AccessibilityService service;
    private Vibrator vibrator;
    private Configuration cf;
    private InputMethodManager manager;
    private SharedPreferences setting;
    private WindowManager windowManager;
    private MyWindowManager myWindowManager;

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }


    public EasyPoint() {
        service = this;
        myWindowManager=new MyWindowManager();
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
        if (timer == null) {
            timer = new Timer();
            //timer.scheduleAtFixedRate(new RefreshTask(), 0, 1000);
        }
        cf = getApplicationContext().getResources().getConfiguration(); //获取设置的配置信息
        manager = (InputMethodManager) getApplicationContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
        windowManager = (WindowManager) getApplicationContext().getSystemService(WINDOW_SERVICE);
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        setting = getApplicationContext().getSharedPreferences("setting", MODE_PRIVATE);

        //创建小圆点
        handler.post(new Runnable() {
            @Override
            public void run() {
                myWindowManager.createEasyPoint(getApplicationContext(), service, vibrator);
                myWindowManager.updateEasyPoint(getApplicationContext(),
                        setting.getInt("vibrate", 0) + 1, setting.getInt("alpha", 0), setting.getInt("size", 0));
            }
        });
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //计时器也销毁
        Log.i("destroy", "");
        timer.cancel();
        timer = null;
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


    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.packageNames = new String[]{"com.zhangjie.easypoint"}; //监听过滤的包名
        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK; //监听哪些行为
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_SPOKEN; //反馈
        info.notificationTimeout = 100; //通知的时间
        setServiceInfo(info);
    }


    class RefreshTask extends TimerTask {
        //检测横屏隐藏圆点
        @Override
        public void run() {
            int ori = cf.orientation; //获取屏幕方向
            //Log.i("timer task",""+ori);
            if (MyWindowManager.isWindowShowing()) {
                if (ori == cf.ORIENTATION_LANDSCAPE) {//虚拟机下就是横屏的
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            //MyWindowManager.updateEasyPointPosition(getApplicationContext(), 10);
                            //MyWindowManager.updateEasyPoint(getApplicationContext(), 0, 2, 0);
                            //横屏不再自动隐藏，改为手动隐藏
                        }
                    });

                } else if (ori == cf.ORIENTATION_PORTRAIT) {
                    //Log.i("竖屏",""+2);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            int alpha = getSharedPreferences("setting", MODE_PRIVATE).getInt("alpha", 50);
                            //MyWindowManager.resumeEasyPoint(getApplicationContext());
                            //MyWindowManager.updateEasyPoint(getApplicationContext(), 0, alpha, 0);
                        }
                    });
                }

                MyWindowManager.getViewInfo(getApplicationContext());

            } else {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        myWindowManager.createEasyPoint(getApplicationContext(), service, vibrator);
                    }
                });
            }


        }
    }
}
