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
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.nfc.Tag;
import android.util.Log;

import com.example.accessibilityt.xposed.ConstantX;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import android.util.Base64;
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
                    String key=it.next();
                    Log.d(TAG,"ApkFindXposed_create_json_key : "+key);
                    if (key.equals(loadPackageParam.packageName)||it.next().contains(loadPackageParam.packageName))
                        return;
                }
            }
        }



        Log.d(TAG,"ApkFindXposed_create");
        String clzloader = loadPackageParam.classLoader.toString();
        if (lastPackageName.equals(loadPackageParam.packageName)){
            hookTime++;
            if (hookTime!=3){
                hookTime=0;
                return;
            }
        }
        getApplicationIcon(loadPackageParam);
        getContext(loadPackageParam);
//        getApkFile(loadPackageParam,clzloader,"main");
    }

    static String base64Icon;
    //将img转为bit给json文件
    private void getApplicationIcon(XC_LoadPackage.LoadPackageParam param){
        XposedHelpers.findAndHookMethod(ApplicationInfo.class, "loadIcon", PackageManager.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                Drawable icon= (Drawable) param.getResult();
                Bitmap bitmap=((BitmapDrawable)icon).getBitmap();
                ByteArrayOutputStream stream=new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG,100,stream);
                byte[] byteArray= stream.toByteArray();

                //将 Drawable 转换为 Base64 编码的字符串
                base64Icon = Base64.encodeToString(byteArray, Base64.DEFAULT);

                if (base64Icon!=null){
                    Log.d(TAG,"img finish");
                }
            }
        });
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

    public static void updateViewContent(String packagheName){
        if (mContext==null){
            Log.e(TAG,"context not find in apkxposed");
            return;
        }
        String appName=getAppName(mContext);
        if (appName==null||appName.equals("")){
            Log.e(TAG,"appName is null!");
            return;
        }
//        Log.e(TAG,"appName is"+appName+"  packageName: "+packagheName);
        ContentResolver mContentResolver=mContext.getContentResolver();
        ContentValues values=new ContentValues();
        values.put(ConstantX.KEY_PACKAGENAME,packagheName);
        values.put(ConstantX.KET_APPNAME,appName);
        mContentResolver.insert(ConstantX.CONTENT_URI,values);
    }
}
