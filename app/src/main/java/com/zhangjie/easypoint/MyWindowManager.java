package com.zhangjie.easypoint;

import android.accessibilityservice.AccessibilityService;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Vibrator;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;

/**
 * Created by zhangjie on 2016/1/30.
 */
public class MyWindowManager {
    private static final String TAG = "MyWindowManager";
    /**
     * 小悬浮窗View的实例
     */
    private static PointView smallWindow;
    private static PointHideView hideWindow;

    /**
     * 小悬浮窗View的参数
     */
    private static WindowManager.LayoutParams smallWindowParams;
    private static WindowManager.LayoutParams hideWindowParams;

    /**
     * 用于控制在屏幕上添加或移除悬浮窗
     */
    private static WindowManager mWindowManager;

    private static SharedPreferences sharedPreferences;

    /**
     * 创建一个小悬浮窗。初始位置为屏幕的右部中间位置。
     *
     * @param context 必须为应用程序的Context.
     */
    void createEasyPoint(Context context, AccessibilityService service, Vibrator vibrator) {
        WindowManager windowManager = getWindowManager(context);
        sharedPreferences = context.getSharedPreferences("setting", Context.MODE_PRIVATE);
        int screenWidth = windowManager.getDefaultDisplay().getWidth();
        int screenHeight = windowManager.getDefaultDisplay().getHeight();
        if (smallWindow == null) {
            smallWindow = new PointView(context, service, vibrator);
            View point = smallWindow.findViewById(R.id.point_view);
            if (smallWindowParams == null) {
                smallWindowParams = new WindowManager.LayoutParams();
                smallWindowParams.type = WindowManager.LayoutParams.TYPE_PHONE;
                smallWindowParams.format = PixelFormat.RGBA_8888;
                smallWindowParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
                smallWindowParams.gravity = Gravity.LEFT | Gravity.TOP;
                smallWindowParams.width = smallWindow.viewWidth;
                smallWindowParams.height = smallWindow.viewHeight;
                smallWindowParams.x = screenWidth;
                smallWindowParams.y = screenHeight / 2;
            }else {
                smallWindowParams.width = smallWindow.viewWidth;
                smallWindowParams.height = smallWindow.viewHeight;
                smallWindowParams.x = screenWidth;
                smallWindowParams.y = screenHeight / 2;
            }
            point.getLayoutParams().width = smallWindow.viewWidth;
            point.getLayoutParams().height= smallWindow.viewHeight;
            sharedPreferences.edit().putInt("origin", 48).apply();
            smallWindow.setParams(smallWindowParams);
            windowManager.addView(smallWindow, smallWindowParams);
        }
    }

    void createHidePoint(Context context, AccessibilityService service,Vibrator vibrator) {
        WindowManager windowManager = getWindowManager(context);
        int screenWidth = windowManager.getDefaultDisplay().getWidth();
        int screenHeight = windowManager.getDefaultDisplay().getHeight();
        if (hideWindow == null) {
            hideWindow = new PointHideView(context,service,vibrator);
            if (hideWindowParams == null) {
                hideWindowParams = new WindowManager.LayoutParams();
                hideWindowParams.type = WindowManager.LayoutParams.TYPE_PHONE;
                hideWindowParams.format = PixelFormat.RGBA_8888;
                hideWindowParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
                hideWindowParams.gravity = Gravity.LEFT | Gravity.TOP;
                hideWindowParams.width = hideWindow.viewWidth;
                hideWindowParams.height = hideWindow.viewHeight;
                hideWindowParams.x = screenWidth;
                hideWindowParams.y = screenHeight / 2;
                Log.i(TAG, "createHidePoint: x: "+hideWindowParams.x+" y: "+hideWindowParams.y);
            }else {
                hideWindowParams.width = hideWindow.viewWidth;
                hideWindowParams.height = hideWindow.viewHeight;
                hideWindowParams.x = screenWidth;
                hideWindowParams.y = screenHeight / 2;
            }
            sharedPreferences = context.getSharedPreferences("setting", Context.MODE_PRIVATE);
            sharedPreferences.edit().putInt("origin", hideWindowParams.width).apply();
            hideWindow.setParams(hideWindowParams);
            windowManager.addView(hideWindow, hideWindowParams);
        }
    }

    void updateEasyPoint(final Context context, int vib, int alpha, int size) {
        int origin_width = sharedPreferences.getInt("origin", 0);
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        float width = size / 50.0f;
        int screenWidth = manager.getDefaultDisplay().getWidth();
        int screenHeight = manager.getDefaultDisplay().getHeight();
        if (smallWindow != null) {
            View point = smallWindow.findViewById(R.id.point_view);
            if (vib != 0) {
                smallWindow.setVibrator_val(vib - 1);
            }
            if (alpha != 0) {
                point.getBackground().setAlpha(alpha - 1);
            }
            if (size != 0) {
                smallWindowParams.width = (int) (width*origin_width);
                smallWindowParams.height = (int) (width*origin_width);
                point.getLayoutParams().width = (int) (width*origin_width);
                point.getLayoutParams().height= (int) (width*origin_width);
                smallWindow.setParams(smallWindowParams);
                smallWindowParams.x = screenWidth;
                smallWindowParams.y = screenHeight / 2;
                manager.updateViewLayout(smallWindow, smallWindowParams);
            }
        }
    }

    static void resumeEasyPoint(final Context context) {
        float size = sharedPreferences.getInt("size", 56);
        int origin_width = sharedPreferences.getInt("origin", 0);
        float width = size / 50.0f;
        WindowManager windowManager = getWindowManager(context);
        int screenWidth = windowManager.getDefaultDisplay().getWidth();
        int screenHeight = windowManager.getDefaultDisplay().getHeight();

        if (smallWindow != null) {
            View point = smallWindow.findViewById(R.id.point_view);
            smallWindowParams.width = (int) (width);
            smallWindowParams.height = (int) (width*origin_width);
            point.getLayoutParams().width = (int) (width);
            point.getLayoutParams().height= (int) (width*origin_width);
            smallWindowParams.x = (int) (screenWidth);
            smallWindowParams.y = (int) (screenHeight);
            smallWindow.setParams(smallWindowParams);
            WindowManager manager = (WindowManager) context.getSystemService(context.WINDOW_SERVICE);
            manager.updateViewLayout(smallWindow, smallWindowParams);
        }
    }

    public static void updateEasyPointPosition(final Context context, int xy) {
        WindowManager windowManager = getWindowManager(context);
        float size = sharedPreferences.getInt("size", 56);
        int screenWidth = windowManager.getDefaultDisplay().getWidth();
        int screenHeight = windowManager.getDefaultDisplay().getHeight();
        int origin_width = sharedPreferences.getInt("origin", 0);
        float width = size / 50.0f;

        if (smallWindow != null) {
            View point = smallWindow.findViewById(R.id.point_view);
            //smallWindowParams.x = (int) (-1);
            smallWindowParams.width = xy;
            //smallWindowParams.height=10;
            //point.getLayoutParams().height=10;
            point.getLayoutParams().width = xy;//横屏时修改了宽，竖屏后改回来
            point.getBackground().setAlpha(0);//使圆点变小并且透明
            smallWindowParams.y = (int) (screenHeight);
            point.getLayoutParams().width= (int) (width*origin_width);
            point.getLayoutParams().height= (int) (width*origin_width);
            Log.i("width",origin_width+"//"+smallWindowParams.width+"//"+width*origin_width);
            smallWindow.setParams(smallWindowParams);
            WindowManager manager = (WindowManager) context.getSystemService(context.WINDOW_SERVICE);
            manager.updateViewLayout(smallWindow, smallWindowParams);
        }
    }


    /**
     * 如果WindowManager还未创建，则创建一个新的WindowManager返回。否则返回当前已创建的WindowManager。
     *
     * @param context 必须为应用程序的Context.
     * @return WindowManager的实例，用于控制在屏幕上添加或移除悬浮窗。
     */
    private static WindowManager getWindowManager(Context context) {
        if (mWindowManager == null) {
            mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        }
        return mWindowManager;
    }

    /**
     * 是否有悬浮窗(包括小悬浮窗和大悬浮窗)显示在屏幕上。
     *
     * @return 有悬浮窗显示在桌面上返回true，没有的话返回false。
     */
    public static boolean isWindowShowing() {
        return smallWindow != null;
    }

    /**
     * 将小悬浮窗从屏幕上移除。
     *
     * @param context 必须为应用程序的Context.
     */
    public void removeEasyPoint(Context context) {
        if (smallWindow != null) {
            WindowManager windowManager = getWindowManager(context);
            windowManager.removeViewImmediate(smallWindow);
//            windowManager.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            smallWindow = null;
        }
    }

    public void removeHidePoint(Context context) {
        if (hideWindow != null) {
            WindowManager windowManager = getWindowManager(context);
            windowManager.removeViewImmediate(hideWindow);
//            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            hideWindow = null;
        }
    }

}
