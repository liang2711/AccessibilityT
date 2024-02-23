package com.example.accessibilityt;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.accessibilityt.dao.AppCodeInfo;
import com.example.accessibilityt.dao.AppCodeInfoDao;
import com.example.accessibilityt.dao.RDatabase;
import com.example.accessibilityt.view.ConstantV;
import com.example.accessibilityt.view.FloadWindowService;
import com.example.accessibilityt.view.PermissionUtil;
import com.example.accessibilityt.view.VDataTools;
import com.example.accessibilityt.xposed.ConstantX;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;

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
        new Thread(){
            @Override
            public void run() {
                super.run();
                RDatabase database=null;
                if (VDataTools.rDatabase!=null)
                    database=VDataTools.rDatabase;
                else database=RDatabase.getInstance(FloadWindowService.mContext);
                AppCodeInfoDao dao=database.getDao();
                dao.deleteNullItem();
            }
        }.start();
    }

    RDatabase database=null;
    public void test(View view){

//        PackageManager packageManager=this.getPackageManager();
//        ApplicationInfo info= null;
//        try {
//            info = packageManager.getApplicationInfo(this.getPackageName(),0);
//        } catch (PackageManager.NameNotFoundException e) {
//            throw new RuntimeException(e);
//        }
        String appName="LinuxApiTest";
//        appName=(String) packageManager.getApplicationLabel(info);
//        Log.d(MainActivity.TAG,"dwaaaaaaaaaaaaaaaaaaaaaaaaaa   onclick  "+appName);

        new Thread(){
            @Override
            public void run() {
                super.run();
                if (database==null){
                    if (VDataTools.rDatabase!=null)
                        database=VDataTools.rDatabase;
                    else {
                        if (FloadWindowService.mContext!=null)
                            database=RDatabase.getInstance(FloadWindowService.mContext);
                        else database=RDatabase.getInstance(getApplicationContext());
                    }
                }
                AppCodeInfoDao dao=database.getDao();
                Log.d(MainActivity.TAG,"dwaaaaaaaaaaaaaaaaaaaaaaaaaa");
                dao.delete(appName);
            }
        }.start();
//        if (new File(ConstantV.APPLICATION_JSON).exists()){
//            Log.d(MainActivity.TAG,"dwaaaaaaaaaaaaaaaaaaaaaaaaaa   onclick  file exists");
//            try {
//                String packageName=getPackageName();
//                JSONObject jsonObject=new JSONObject(new String(Files.readAllBytes(Paths.get(ConstantV.APPLICATION_JSON))));
//                if (jsonObject!=null) {
//                    return;
//                }
//                Iterator<String> it=jsonObject.keys();
//                while (it.hasNext()){
//                    String key=it.next();
//                    Log.d(TAG,"ApkFindXposed_create_json_key : "+key);
//                }
//                appName=jsonObject.getString(packageName);
//                jsonObject.remove(packageName);
//
//                String key;
//                it=jsonObject.keys();
//                while (it.hasNext()){
//                    key=it.next();
//                    Log.d(TAG,"ApkFindXposed_create_json_key : "+key);
//                }
//
//            } catch (JSONException e) {
//                throw new RuntimeException(e);
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//
//        }
    }
    public void startAccessibilityService(View view){
//        if (PermissionUtil.isStartFloadWindowService(this)){
//            if (serviceIn==null)
//                serviceIn=new Intent(this,FloadWindowService.class);
//            stopService(serviceIn);
//        }
//        if (!PermissionUtil.isAccessibilityServiceEnabled(this, HongBaoService.class)){
//            Toast.makeText(this,"请打开无障碍服务",Toast.LENGTH_SHORT).show();
//            PermissionUtil.requestAccessibilityServicePermission(this,HongBaoService.class);
//            return;
//        }
        new Thread(){
            @Override
            public void run() {
                super.run();
                Log.d(MainActivity.TAG,"dwaaaaaaaaaaaaaaaaaaaaaaaaaa   startAccessibilityService  ");
                if (database==null){
                    if (VDataTools.rDatabase!=null)
                        database=VDataTools.rDatabase;
                    else {
                        if (FloadWindowService.mContext!=null)
                            database=RDatabase.getInstance(FloadWindowService.mContext);
                        else database=RDatabase.getInstance(getApplicationContext());
                    }
                }
                AppCodeInfoDao dao=database.getDao();
                if (dao==null) return;
                for (AppCodeInfo info:dao.getAll()){
                    Log.d(MainActivity.TAG+"main_dao",info.appName+"   "+info.className+"  "+info.code);
                }
            }
        }.start();

    }
}