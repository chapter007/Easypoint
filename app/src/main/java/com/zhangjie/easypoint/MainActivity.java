package com.zhangjie.easypoint;

import android.Manifest;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private boolean isEnabled;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final Button start= (Button) findViewById(R.id.start_point);
        Button stop= (Button) findViewById(R.id.stop_point);
        final LinearLayout tip= (LinearLayout) findViewById(R.id.tip);
        checkService();
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
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
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
        List<AccessibilityServiceInfo> list = AccessibilityManagerCompat.getEnabledAccessibilityServiceList(manager,
                AccessibilityServiceInfo.FEEDBACK_ALL_MASK);
        System.out.println("list.size = " + list.size());
        if (list.size()==0) isEnabled=false;
        for (int i = 0; i < list.size(); i++) {
            System.out.println("已经可用的服务列表 = " + list.get(i).getId());
            if ("com.zhangjie.easypoint/.EasyPoint".equals(list.get(i).getId())) {
                System.out.println("已启用");
                isEnabled = true;
                break;
            }
        }
    }
}
