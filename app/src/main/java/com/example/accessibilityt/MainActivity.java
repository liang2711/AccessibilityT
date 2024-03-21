package com.example.accessibilityt;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.accessibilityt.dao.AppCodeInfo;
import com.example.accessibilityt.dao.AppCodeInfoDao;
import com.example.accessibilityt.dao.RDatabase;
import com.example.accessibilityt.view.ConstantV;
import com.example.accessibilityt.view.FloadWindowService;
import com.example.accessibilityt.view.PermissionUtil;
import com.example.accessibilityt.view.SPUtils;
import com.example.accessibilityt.view.VDataTools;
import com.example.accessibilityt.xposed.ConstantX;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

public class MainActivity extends AppCompatActivity {
    Intent serviceIn=null;
    public static final String TAG="ACCESSIBILITY";
    static SharedPreferences sharedPreferences;
    public CheckBox local,remote;
    @SuppressLint("WrongThread")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        //创建默认图片文件
        File icon=new File(ConstantV.APPLICATION_ICON_DEFAULT);
        if (!icon.exists()){
            Drawable drawable=getResources().getDrawable(R.drawable.youki);
            Bitmap bitmap=((BitmapDrawable)drawable).getBitmap();
            try {
                FileOutputStream fos=new FileOutputStream(icon);
                bitmap.compress(Bitmap.CompressFormat.PNG,100,fos);
                fos.flush();
                fos.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        PermissionUtil.requsetFloatWindowPermission(this);
    }
    private void putBoolean(Boolean isTure){
        JSONObject jsonObject=null;
        try {
            if (new File(ConstantV.MODULE_JSON).exists()){
                jsonObject=new JSONObject(new String(Files.readAllBytes(Paths.get(ConstantV.MODULE_JSON))));
            }else {
                jsonObject=new JSONObject();
            }
            jsonObject.put(ConstantV.KET_MODULE,isTure);
            Files.write(Paths.get(ConstantV.MODULE_JSON),jsonObject.toString().getBytes());
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public static boolean getBoolean(){
        JSONObject jsonObject=null;
        try {
            if (new File(ConstantV.MODULE_JSON).exists()){
                jsonObject=new JSONObject(new String(Files.readAllBytes(Paths.get(ConstantV.MODULE_JSON))));
                return jsonObject.getBoolean(ConstantV.KET_MODULE);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return true;
    }
    public void init(){
        local=findViewById(R.id.cb_local);
        remote=findViewById(R.id.cb_remote);
        if (new File(ConstantV.MODULE_JSON).exists()){
            if (getBoolean()){
                local.setChecked(true);
                remote.setChecked(false);
            } else {remote.setChecked(true);local.setChecked(false);}
        }else {
            local.setChecked(true);
            remote.setChecked(false);
        }

        local.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d(TAG,"local:"+isChecked);
                if (isChecked){
                    putBoolean(true);
                }
                remote.setChecked(!isChecked);
            }
        });

        remote.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d(TAG,"remote:"+isChecked);
                if (isChecked){
                    putBoolean(false);
                }
                local.setChecked(!isChecked);
            }
        });
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

    public void selectThreadPool(View view){
        Log.i(TAG,"selectThreadPool");
        if (FloadWindowService.mContext==null){
            Toast.makeText(this,"请先打开hook服务",Toast.LENGTH_SHORT).show();
            return;
        }
        ThreadPoolExecutor executor=VDataTools.threadPoolExecutor;
        if (executor==null){
            Toast.makeText(this,"线程池错误",Toast.LENGTH_SHORT).show();
        }
        Queue<Runnable> pendingTasks=  executor.getQueue();
        for (Runnable task:pendingTasks){
            if (task==null)
                continue;
            Log.i(TAG,task.toString());
        }

    }
    public void startDaoAll(View view){
        //这是请求无障碍的权限
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
        if (FloadWindowService.mContext==null){
            Toast.makeText(this,"请先打开hook服务",Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent=new Intent(this,DaoListActivity.class);
        startActivity(intent);
    }
}