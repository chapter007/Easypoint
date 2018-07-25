package com.zhangjie.easypoint;

import android.Manifest;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.accessibility.AccessibilityManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private boolean isEnabled;
    private SharedPreferences setting;
    private MyWindowManager myWindowManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final Button start= (Button) findViewById(R.id.start_point);
        Button stop= (Button) findViewById(R.id.stop_point);
        Button set_vibrate= (Button) findViewById(R.id.set_vibrate);
        Button set_alpha= (Button) findViewById(R.id.set_alpha);
        Button set_size= (Button) findViewById(R.id.set_size);
        final LinearLayout tip= (LinearLayout) findViewById(R.id.tip);
        setting=getSharedPreferences("setting",MODE_PRIVATE);
        myWindowManager=new MyWindowManager();
        Intent intent = new Intent(MainActivity.this, EasyPoint.class);
        startService(intent);
        checkService();


        if(isEnabled){tip.setVisibility(View.VISIBLE);}
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isEnabled) {
                    Intent intent = new Intent(MainActivity.this, EasyPoint.class);
                    startService(intent);
                    tip.setVisibility(View.VISIBLE);
                } else {
                    showDialog(MainActivity.this, "激活圆点", "您还没有激活圆点。" + "在设置中：系统 → 辅助功能 → 服务 中激活" + getResources().getString(R.string.app_name)
                            + "后，便可使用圆点", "去激活", "取消");
                }
            }
        });
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isEnabled) {
                    showDialog(MainActivity.this, "取消激活圆点", "您激活了圆点。" + "在设置中：系统 → 辅助功能 → 服务 中取消激活" + getResources().getString(R.string.app_name)
                            + "后，便可停止使用圆点", "去关闭激活", "取消");
                } else {
                    Intent intent = new Intent(MainActivity.this, EasyPoint.class);
                    stopService(intent);
                    finish();
                }

            }
        });
        set_vibrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSetDialog(MainActivity.this, "设置震动", 0);
            }
        });
        set_alpha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSetDialog(MainActivity.this,"设置透明度",1);
            }
        });
        set_size.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSetDialog(MainActivity.this,"设置圆点大小",2);
            }
        });
        if(Build.VERSION.SDK_INT>22&&!Settings.canDrawOverlays(this)){
            requestAlertWindowPermission();
        }


    }
    //6.0权限需要
    private static final int REQUEST_CODE = 1;
    private  void requestAlertWindowPermission() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            if(Build.VERSION.SDK_INT>22){
                if (Settings.canDrawOverlays(this)) {
                    Toast.makeText(this,"已为6.0设备获得相关权限",Toast.LENGTH_SHORT).show();
                }
            }

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void showDialog(Context mContext, String title, String msg, String positiveMsg, String cancelMsg) {

        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(mContext);

        builder.setTitle(title)
                .setPositiveButton(positiveMsg, new positiveListener())
                .setNegativeButton(cancelMsg, null)
                .setCancelable(false);// 设置点击空白处，不能消除该对话框

        builder.setMessage(msg).create().show();
    }

    public void showSetDialog(Context mContext,String title, final int type){
        final AlertDialog.Builder builder= new AlertDialog.Builder(mContext);

        LayoutInflater inflater= (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
        View setLayout=inflater.inflate(R.layout.setting_dialog, (ViewGroup) findViewById(R.id.my_setting_dialog));

        builder.setView(setLayout);
        builder.setCancelable(false);
        builder.setTitle(title);
        SeekBar set_bar= (SeekBar) setLayout.findViewById(R.id.set_bar);
        final TextView seek_value= (TextView) setLayout.findViewById(R.id.seek_value);
        builder.setNegativeButton("取消", null)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (type) {
                            case 0:
                                myWindowManager.updateEasyPoint(MainActivity.this, setting.getInt("vibrate", 0) + 1, 0, 0);
                                break;
                            case 1:
                                myWindowManager.updateEasyPoint(MainActivity.this, 0, setting.getInt("alpha", 0), 0);
                                break;
                            case 2:
                                myWindowManager.updateEasyPoint(MainActivity.this, 0, 0, setting.getInt("size", 0));
                                break;
                            default:
                                break;
                        }
                    }
                });
        switch (type){
            case 0:
                set_bar.setProgress(setting.getInt("vibrate", 0));
                seek_value.setText(""+setting.getInt("vibrate", 0));
                break;
            case 1:
                set_bar.setProgress(setting.getInt("alpha", 100)/2);
                seek_value.setText(""+setting.getInt("alpha", 100)/2);
                break;
            case 2:
                set_bar.setProgress(setting.getInt("size", 50));
                seek_value.setText(""+setting.getInt("size", 50));
                break;
            default:
                break;
        }
        set_bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                seek_value.setText("" + i);
                switch (type) {
                    case 0:
                        setting.edit().putInt("vibrate", i).commit();
                        break;
                    case 1:
                        setting.edit().putInt("alpha", i * 2).commit();
                        break;
                    case 2:
                        setting.edit().putInt("size", i).commit();
                        break;
                    default:
                        break;
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        builder.create().show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkService();
    }

    private class positiveListener implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            startActivity(new Intent("android.settings.ACCESSIBILITY_SETTINGS"));
        }
    }



    public void checkService(){
        AccessibilityManager manager = (AccessibilityManager) getSystemService(ACCESSIBILITY_SERVICE);
        assert manager != null;
        List<AccessibilityServiceInfo> list = manager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK);

        //System.out.println("list.size = " + list.size());
        if (list.size()==0) isEnabled=false;
        for (int i = 0; i < list.size(); i++) {
            //System.out.println("已经可用的服务列表 = " + list.get(i).getId());
            if ("com.zhangjie.easypoint/.EasyPoint".equals(list.get(i).getId())) {
                //System.out.println("已启用");
                isEnabled = true;
                break;
            }
        }
    }
}
