package com.example.accessibilityt;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.accessibilityt.dao.MyCursor;
import com.example.accessibilityt.view.ConstantV;
import com.example.accessibilityt.view.FloadWindowService;
import com.example.accessibilityt.view.ProviderTool;
import com.example.accessibilityt.view.VDataTools;

import org.json.JSONObject;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

public class InfoProvider extends ContentProvider {
    private static final UriMatcher mMatcher;
    static {
        mMatcher=new UriMatcher(UriMatcher.NO_MATCH);

        mMatcher.addURI(ConstantV.AUTOHORITY, ConstantV.KEY_NAME, ConstantV.ITEM);
        mMatcher.addURI(ConstantV.AUTOHORITY, ConstantV.KEY_NAME, ConstantV.ITEM_DEFAULT);
    }
    @Override
    public boolean onCreate() {
        return false;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        Log.d(MainActivity.TAG,"InfoProvider query"+projection[0]+" "+uri);
        if (!isRun(projection[0])||FloadWindowService.mContext==null){
            return new MyCursor(false,VDataTools.getServiceUri());
        }
        Thread thread= ProviderTool.loopSetViewProvider();
        thread.start();
        ProviderTool.getApkFile(projection[0],projection[1],VDataTools.getServiceUri());
        try {
            thread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return new MyCursor(true,VDataTools.getServiceUri());
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        VDataTools.mapClassCode=null;
        String packageName=values.get(ConstantV.KEY_PACKAGENAME)+"";
        String appName=values.get(ConstantV.KET_APPNAME)+"";
        String icon=values.get(ConstantV.KEY_ICON)+"";
        Log.d(MainActivity.TAG,"InfoProvider insert "+packageName+"  "+uri.toString());
        if (!setAJson(appName,packageName,icon)){
            Log.e(MainActivity.TAG,"the provide error");
            return null;
        }
        Log.d(MainActivity.TAG,"provider module:"+VDataTools.setMJson());
        if (VDataTools.setMJson()){
            //本地
//            VDataTools.jarFileZipResponse(VDataTools.getServiceUri()+"/dlzip",
//                    packageName,packageName.replace('.','/'),appName,".jar",getContext()).start();
        }else {
            //远程
//            VDataTools.all(VDataTools.getServiceUri()+"/all",packageName,getContext()).start();
        }
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    boolean setAJson(String className,String packageName,String icon){
        if (className==null||packageName==null||icon==null){
            Log.e(MainActivity.TAG,"setJson data is null");
            return false;
        }
        if (FloadWindowService.mContext==null){
            Log.e(MainActivity.TAG,"not find context in DAO");
            return false;
        }
        JSONObject jsonObject=null;
        try {
            if (new File(ConstantV.APPLICATION_JSON).exists()){
                jsonObject=new JSONObject(new String(Files.readAllBytes(Paths.get(ConstantV.APPLICATION_JSON))));
            }else {
                jsonObject=new JSONObject();
            }
            JSONObject value=new JSONObject();
            value.put(className,icon);
            jsonObject.put(packageName,value);
            Files.write(Paths.get(ConstantV.APPLICATION_JSON),jsonObject.toString().getBytes());
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        Log.d(MainActivity.TAG,"json data success write");
        return true;
    }
    private boolean isRun(String packageName){
        if (packageName==null || packageName.equals("")|| packageName.equals("com.example.accessibilityt"))
            return false;
        if (VDataTools.isPK(packageName)){

        }else if (packageName.contains("android")) {
            return false;
        }
        if (VDataTools.isUse(packageName)){
            return false;
        }
        return true;
    }
}
