package com.example.accessibilityt;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.example.accessibilityt.xposed.ConstantX;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class ApkFindXpose implements IXposedHookLoadPackage {
    static final String TAG = "ApkFindXpose";
    String lastPackageName="";
    int hookTime=0;
    static Context mContext;
    static ZipFile zipFile;

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        if (loadPackageParam.packageName.contains("android")) {
            return;
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
                        XDataTools.isThreadActive=true;
                        XDataTools.lock.notify();
                    }
                }
            }
        });
    }

    public static void updateViewContent(String packagheName){
        ContentResolver mContentResolver=mContext.getContentResolver();
        ContentValues values=new ContentValues();
        values.put(ConstantX.KEY_PACKAGENAME,packagheName);
        mContentResolver.insert(ConstantX.CONTENT_URI,values);
    }
}
