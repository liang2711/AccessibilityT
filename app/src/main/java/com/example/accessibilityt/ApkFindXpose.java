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
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.nfc.Tag;
import android.os.Build;
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
        Log.d(ApkFindXpose.TAG,loadPackageParam.packageName);
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
                    if (key.equals(loadPackageParam.packageName)||key.contains(loadPackageParam.packageName)){
                        Log.d(TAG,"ApkFindXposed_create_json_key  contains: "+key);
                    }
                        return;
                }
            }
        }

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
    private void getApkFile(XC_LoadPackage.LoadPackageParam loadPackageParam,String clzloader,String mark){
        String apkPath = XDataTools.displayApkPath(clzloader);
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
        Log.d(ApkFindXpose.TAG,loadPackageParam.packageName+"    getApkFile");
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
            XDataTools.extraDoPost( ConstantX.SERVICER_IP+"/outDex",entryName,loadPackageParam.packageName,inputStream,apkPath,mark);
        }
        //updateViewContent调用
        XDataTools.extraDoPost( null,null,loadPackageParam.packageName,null,null,null);
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
        String icon=getApplicationIcon(mContext);
        if (appName==null||appName.equals("")){
            Log.e(TAG,"appName is null!");
            return;
        }
        Log.e(TAG,"updateViewContent   appName is"+appName+"  packageName: "+packagheName);
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
}
