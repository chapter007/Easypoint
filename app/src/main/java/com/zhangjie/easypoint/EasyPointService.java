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

public class EasyPointService extends AccessibilityService {
    private AccessibilityService service;
    private Vibrator vibrator;
    private SharedPreferences setting;
    private MyWindowManager myWindowManager;

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }


    public EasyPointService() {
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
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        setting = getApplicationContext().getSharedPreferences("setting", MODE_PRIVATE);

        //创建小圆点
        handler.post(new Runnable() {
            @Override
            public void run() {
                myWindowManager.createEasyPoint(getApplicationContext(), service, vibrator);
                MyWindowManager.updateEasyPoint(getApplicationContext(),
                        setting.getInt("vibrate", 0) + 1, setting.getInt("alpha", 0), setting.getInt("size", 0));
            }
        });
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //计时器也销毁
        timer.cancel();
        timer = null;
        //关闭圆点
        handler.post(new Runnable() {
            @Override
            public void run() {
                myWindowManager.removeEasyPoint(getApplicationContext());
            }
        });
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        Log.d("zhangjie", "onAccessibilityEvent: " + accessibilityEvent.toString());
    }

    @Override
    public void onInterrupt() {

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //切换为竖屏
        this.getResources().getConfiguration();
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            myWindowManager.removeHidePoint(getApplicationContext());
//            myWindowManager.removeEasyPoint(getApplicationContext());
            myWindowManager.createEasyPoint(getApplicationContext(),this,vibrator);
        }
        //切换为横屏
        else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            //todo 横屏的时候不是完全隐藏，而是收缩起来，类似小米那样
//            myWindowManager.removeHidePoint(getApplicationContext());
            myWindowManager.removeEasyPoint(getApplicationContext());
            myWindowManager.createHidePoint(getApplicationContext(),this,vibrator);
        }

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

}
