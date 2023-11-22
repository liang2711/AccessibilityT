package com.example.accessibilityt;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.accessibilityt.view.FloadWindowService;
import com.example.accessibilityt.view.PermissionUtil;

public class MainActivity extends AppCompatActivity {
    Intent serviceIn=null;
    public static final String TAG="ACCESSIBILITY";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PermissionUtil.requsetFloatWindowPermission(this);
    }
    public void startServcie(View view){
        if (PermissionUtil.requsetFloatWindowPermission(this)){
            return;
        }else if (!PermissionUtil.isStartFloadWindowService(this)){
            serviceIn=new Intent(this, FloadWindowService.class);
            startService(serviceIn);
            return;
        }

    }
    public void stopService(View view){
        if(PermissionUtil.isAccessibilityServiceEnabled(this,HongBaoService.class)){
            HongBaoService.mService.disableSelf();
            Toast.makeText(this,"关闭悬浮窗",Toast.LENGTH_SHORT).show();
        }
        if (PermissionUtil.isStartFloadWindowService(this)){
            if (serviceIn==null)
                serviceIn=new Intent(this,FloadWindowService.class);
            stopService(serviceIn);
        }
    }
    public void startAccessibilityService(View view){
        if (PermissionUtil.isStartFloadWindowService(this)){
            if (serviceIn==null)
                serviceIn=new Intent(this,FloadWindowService.class);
            stopService(serviceIn);
        }
        if (!PermissionUtil.isAccessibilityServiceEnabled(this, HongBaoService.class)){
            Toast.makeText(this,"请打开无障碍服务",Toast.LENGTH_SHORT).show();
            PermissionUtil.requestAccessibilityServicePermission(this,HongBaoService.class);
            return;
        }
    }
}