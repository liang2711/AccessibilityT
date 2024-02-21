package com.example.accessibilityt;

import android.accessibilityservice.AccessibilityService;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.widget.LinearLayout;

import com.example.accessibilityt.view.SideBarHideReceiver;
import com.example.accessibilityt.view.VDataTools;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Queue;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HongBaoService extends AccessibilityService {
    private static final String ACTION_HIDE="com.example.accessibilityt.ACTION_HIDE";
    private SideBarHideReceiver mReceiver;
    private SideBarArrow mLeftArrowBar;
    public static HongBaoService mService;

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.d("InitApp","AccessibilityService_connected");
        Log.d(MainActivity.TAG,"onServiceConnected");
        mService=this;
        IntentFilter intentFilter=new IntentFilter("com.example.accessibilityt.XposedReceiver.ACTION_AGAIN");
        registerReceiver(responseXposed,intentFilter);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createToucher();
    }

    @SuppressLint({"RtlHardcoded", "InflateParams"})
    private void createToucher() {
        Log.d(MainActivity.TAG,"AccessibilityService onCreate");
        Log.d("InitApp","AccessibilityService_create");
        // get window manager
        WindowManager windowManager = (WindowManager) getApplication().getSystemService(Context.WINDOW_SERVICE);
        // left arrow
        mLeftArrowBar = new SideBarArrow();
        LinearLayout mArrowLeft = mLeftArrowBar.getView(this, windowManager, this);
    }
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.d("InitApp","AccessibilityService_event");
        new Thread(){
            @Override
            public void run() {
                super.run();
                OkHttpClient client=new OkHttpClient();
                Request request=new Request.Builder()
                        .url("http://192.168.1.43:8088/dex2java/test")
                        .build();
                try {
                    Response response=client.newCall(request).execute();
                    Log.d("InitAppA",response.body().string());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }.start();
        if (sendRequestThead==null)
            sendRequest();
    }

    @Override
    public void onInterrupt() {
        Log.d("InitApp","onInterrupt");
        mService=null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mLeftArrowBar.clearAll();
        unregisterReceiver(responseXposed);
        mService=null;
    }
    public static boolean isStart(){
        return mService!=null;
    }
    public Queue<XposedRequestData> queue=new LinkedList<>();
    private BroadcastReceiver responseXposed=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String urlPath=intent.getStringExtra("urlPath");
            String fileName=intent.getStringExtra("fileName");
            String packageName=intent.getStringExtra("packageName");
            String apkPath=intent.getStringExtra("apkPath");
            Log.i("InitApp",urlPath+" "+fileName+" "+packageName+" "+apkPath);
            queue.add(new XposedRequestData(urlPath,fileName,packageName,apkPath));
            loopSetViewProvider(urlPath,fileName,packageName);
        }
    };

    private static volatile boolean isInterrupt=false;
    private static volatile boolean isOnDestroy=false;
    private void loopSetViewProvider(String urlPath,String fileName,String packageName){
        Thread thread=new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){
                    if (isInterrupt)
                        break;
                }
                if (queue==null||queue.size()<=0)
                    return;
                InputStream inputStream;
                XposedRequestData data;
                ByteArrayOutputStream buffer;
                try {
                    data=queue.poll();
                    buffer=new ByteArrayOutputStream();
                    inputStream=getApkFile(data.apkPath,data.fileName);
                    int bytesRead;
                    byte[] arrayByte=new byte[4096];
                    while ((bytesRead=inputStream.read(arrayByte,0,arrayByte.length))!=-1){
                        buffer.write(arrayByte,0,bytesRead);
                    }
                    buffer.flush();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                if (inputStream==null){
                    Log.d("InitAppA","inputstream is null");
                    return;
                }
                Log.i("InitAppA",urlPath+" "+fileName+" "+packageName+" "+buffer.toByteArray());
//                VDataTools.extraDoPost(urlPath,fileName,packageName,inputStream,"second");

            }
        });
        thread.start();
    }
    private String getApkPath(String packageName){
        PackageManager packageManager=getPackageManager();
        try {
            ApplicationInfo applicationInfo=packageManager.getApplicationInfo(packageName,0);
            if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.P){
                return applicationInfo.sourceDir;
            }else {
                return applicationInfo.sourceDir;
            }
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    private ZipFile zipFile;
    private InputStream getApkFile(String apkPath, String fileName) throws IOException {
        File file=new File(apkPath);
        if (file==null||apkPath==null)
            return null;
        zipFile=new ZipFile(file);
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements()){
            ZipEntry entry=entries.nextElement();
            String entryName=entry.getName();
            if (entryName.equals(fileName)){
                Log.d("InitAppA",entryName+"   "+fileName);
                return zipFile.getInputStream(entry);
            }
        }
        zipFile.close();
        return null;
    }
    private Thread sendRequestThead;
    private void sendRequest(){
        sendRequestThead=new Thread(new Runnable() {
            @Override
            public void run() {
                while (!isOnDestroy){
                    try {
                        if (isInterrupt)
                            Thread.sleep(5000);
                        isInterrupt=!isInterrupt;
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
        sendRequestThead.start();
    }
    private class XposedRequestData{
        public String urlPath;
        public String fileName;
        public String packageName;
        public String apkPath;
        public XposedRequestData(String urlPath,String fileName,String packageName,String apkPath){
            this.urlPath=urlPath;
            this.fileName=fileName;
            this.packageName=packageName;
            this.apkPath=apkPath;
        }
    }
}
