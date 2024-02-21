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
import android.util.Log;

import com.example.accessibilityt.xposed.ConstantX;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class ApkFindXpose implements IXposedHookLoadPackage {
    static final String TAG = "ApkFindXpose";
    String lastPackageName="";

    static String appName="";
    int hookTime=0;
    static Context mContext;
    static ZipFile zipFile;
    static JSONObject jsonObject=null;
    static boolean isExists=false;
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        if (loadPackageParam.packageName.contains("android")) {
            return;
        }
        if (new File(ConstantX.APPLICATION_JSON).exists()){
            //解析json文件
            jsonObject=new JSONObject(new String(Files.readAllBytes(Paths.get(ConstantX.APPLICATION_JSON))));
            if (jsonObject!=null) {
                Iterator<String> it=jsonObject.keys();
                while (it.hasNext()){
                    if (it.next().equals(loadPackageParam.packageName)||it.next().contains(loadPackageParam.packageName))
                        return;
                }
            }
        }

        Log.d("InitApp","ApkFindXposed_create");
        String clzloader = loadPackageParam.classLoader.toString();
        if (lastPackageName.equals(loadPackageParam.packageName)){
            hookTime++;
            if (hookTime!=3){
                hookTime=0;
                return;
            }
        }
        getContext(loadPackageParam);
        getApkFile(loadPackageParam,clzloader,"main");
    }

    private void getApkFile(XC_LoadPackage.LoadPackageParam loadPackageParam,String clzloader,String mark){
        String apkPath = XDataTools.displayApkPath(clzloader);
        Log.d(TAG, "clzloader:" + clzloader + "  apkPath:" + apkPath);
        Log.d(TAG, apkPath);
        if (!apkPath.endsWith(".apk")){
            Log.e(TAG,"this error apkPath not contain .apk");
            return;
        }
        File file = new File(apkPath);
        if (file == null || apkPath == null) {
            Log.d(TAG, "FILE ERROR");
            return;
        }
        try {
            zipFile = new ZipFile(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Enumeration<? extends ZipEntry> entries = zipFile.entries();

        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            String entryName = entry.getName();
            if (!entryName.contains(".dex"))
                continue;

            InputStream inputStream= null;
            try {
                inputStream = zipFile.getInputStream(entry);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            //this not Constant.url
            XDataTools.extraDoPost( ConstantX.SERVICER_IP+"/outDex",entryName,loadPackageParam.packageName,inputStream,entries.hasMoreElements(),apkPath,mark);
        }
    }

    private void getContext(XC_LoadPackage.LoadPackageParam param){
        Class<?> contextClass=XposedHelpers.findClass(
                "android.content.ContextWrapper",param.classLoader);
        ApplicationInfo info=param.appInfo;
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
                        getAppName(mContext);
                        XDataTools.isThreadActive=true;
                        XDataTools.lock.notify();
                    }
                }
            }
        });
    }

    private void getAppName(Context context){
        PackageManager packageManager=context.getPackageManager();
        ApplicationInfo info= null;
        try {
            info = packageManager.getApplicationInfo(context.getPackageName(),0);
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
        appName=(String) packageManager.getApplicationLabel(info);
    }

    public static void updateViewContent(String packagheName){
        //创建json文件或者增加内容
        if (jsonObject==null)
            jsonObject=new JSONObject();
        try {
            jsonObject.put(packagheName,appName);
            Files.write(Paths.get(ConstantX.APPLICATION_JSON),jsonObject.toString().getBytes());
        } catch (JSONException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        ContentResolver mContentResolver=mContext.getContentResolver();
        ContentValues values=new ContentValues();
        values.put(ConstantX.KEY_PACKAGENAME,packagheName);
        values.put(ConstantX.KET_APPNAME,appName);
        mContentResolver.insert(ConstantX.CONTENT_URI,values);
    }
}
