package com.example.accessibilityt;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.example.accessibilityt.view.VDataTools;
import com.example.accessibilityt.xposed.ConstantX;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.Buffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import android.util.Base64;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class ApkFindXpose implements IXposedHookLoadPackage {
    static final String TAG = "ApkFindXpose";
    static Context mContext;
    static ZipFile zipFile;
    Object object=new Object();
    static ExecutorService executorService=Executors.newFixedThreadPool(3);
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        Log.d(ApkFindXpose.TAG,loadPackageParam.packageName);
        String clzloader = loadPackageParam.classLoader.toString();
        getContext(loadPackageParam);
//        getApkFile(loadPackageParam.packageName,clzloader,"");
        synchronized (object){
            XDataTools.executeLoop(loadPackageParam.packageName,clzloader);
        }
    }

    //将img转为bit给json文件
    private static String getApplicationIcon(Context context){
        PackageManager packageManager=context.getPackageManager();
        ApplicationInfo info=null;
        try {
            info=packageManager.getApplicationInfo(context.getPackageName(),0);
        } catch (PackageManager.NameNotFoundException e) {
            return "localIconPath1="+ConstantX.APPLICATION_ICON_DEFAULT;
        }
        Drawable icon=info.loadIcon(packageManager);

        if (info==null||icon==null){
            return "localIconPath1="+ConstantX.APPLICATION_ICON_DEFAULT;
        }
        Bitmap bitmap;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                icon instanceof AdaptiveIconDrawable){
            bitmap = Bitmap.createBitmap(icon.getIntrinsicWidth(), icon.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            icon.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            icon.draw(canvas);
        }else {
            bitmap=((BitmapDrawable) icon).getBitmap();
        }
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        // 或者将 Drawable 转换为 Base64 编码的字符串
        String base64String = Base64.encodeToString(byteArray, Base64.DEFAULT);
        if (base64String==null)
            return "localIconPath1="+ConstantX.APPLICATION_ICON_DEFAULT;
        return base64String;
    }
    public static void getApkFile(String packageName,String clzloader,String uri){
        String apkPath = XDataTools.displayApkPath(clzloader);
        if (!apkPath.endsWith(".apk")){
            Log.e(TAG,"this error apkPath not contain .apk");
            return;
        }
        File file = new File(apkPath);
        if (file == null || apkPath == null || !file.exists()) {
            Log.d(TAG, "FILE ERROR");
            return;
        }
        try {
            zipFile = new ZipFile(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        Log.d(ApkFindXpose.TAG,packageName+"    getApkFile");
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            String entryName = entry.getName();
            if (!entryName.contains(".dex"))
                continue;
            if (entryName.equals("classes.dex"))
                continue;

            InputStream inputStream= null;
            byte[] bytes=null;
            try {
                inputStream = zipFile.getInputStream(entry);
                bytes=b2Stream(inputStream);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            //this not Constant.url
//            XDataTools.extraDoPost( ConstantX.SERVICER_IP+"/outDex",entryName,loadPackageParam.packageName,bytes,apkPath,mark);
            XDataTools.extraDoPost( ConstantX.SERVICER_IP+"/outDex",entryName,packageName,bytes);
        }
        //updateViewContent调用
        XDataTools.extraDoPost( null,null,packageName,null);
    }

    private void getContext(XC_LoadPackage.LoadPackageParam loadPackageParam){
        Class<?> contextClass=XposedHelpers.findClass(
                "android.content.ContextWrapper",loadPackageParam.classLoader);
        ApplicationInfo info=loadPackageParam.appInfo;
        XposedHelpers.findAndHookMethod(contextClass, "getApplicationContext", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                if (mContext!=null)
                    return;
                mContext= (Context) param.getResult();
                Log.d(TAG,"GET CONTEXT ON XPOSED");
                if (XDataTools.lock!=null&&mContext!=null){
                    synchronized (XDataTools.lock){
                        XDataTools.isIsFinishContext=true;
                        XDataTools.lock.notify();
                    }
                }
            }
        });
    }

    private static String getAppName(Context context){
        PackageManager packageManager=context.getPackageManager();
        ApplicationInfo info= null;
        try {
            info = packageManager.getApplicationInfo(context.getPackageName(),0);
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
        return (String) packageManager.getApplicationLabel(info);
    }

    public static byte[] b2Stream(InputStream jarInputStream) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();
        byte[] buffer=new byte[4096];
        int bytesRead;
        while ((bytesRead = jarInputStream.read(buffer)) != -1) {
            byteArrayOutputStream.write(buffer, 0, bytesRead);
        }
        return byteArrayOutputStream.toByteArray();
    }

    public static void updateViewContent(String packagheName){
        if (mContext==null){
            Log.e(TAG,"context not find in apkxposed");
            return;
        }
        String appName=getAppName(mContext);
        String icon=getApplicationIcon(mContext);
        if (appName==null||appName.equals("")){
            Log.e(TAG,"appName is null!");
            return;
        }
        Log.i(TAG,"updateViewContent   appName is"+appName+"  packageName: "+packagheName);
        if (packagheName==null){
            packagheName=mContext.getPackageName();
        }
        ContentResolver mContentResolver=mContext.getContentResolver();
        ContentValues values=new ContentValues();
        values.put(ConstantX.KEY_PACKAGENAME,packagheName);
        values.put(ConstantX.KET_APPNAME,appName);
        values.put(ConstantX.KEY_ICON,icon);
        mContentResolver.insert(ConstantX.CONTENT_URI,values);
    }
    public static boolean isRun(String clzloader,Context context,String packageName){
        if (context==null){
            Log.e(TAG,"context not find in apkxposed");
            return false;
        }
        ContentResolver contentResolver=context.getContentResolver();
        Cursor c=contentResolver.query(ConstantX.CONTENT_URI,new String[]{packageName,clzloader},null,null);
        Bundle bundle=c.getExtras();
        if (bundle==null){
            Log.e(TAG,"bundle is null in apkxposed");
            return false;
        }
        Log.d(TAG,"isRun     "+bundle.getBoolean(ConstantX.KEY_RUN)+" "+bundle.getString(ConstantX.KET_URI));
        if (!bundle.getBoolean(ConstantX.KEY_RUN))
            return false;
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        updateViewContent(packageName);
//        getApkFile(packageName,clzloader,bundle.getString(ConstantX.KET_URI));
        return true;
    }
}
