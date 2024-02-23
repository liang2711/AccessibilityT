package com.example.accessibilityt;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.accessibilityt.view.ConstantV;
import com.example.accessibilityt.view.FloadWindowService;
import com.example.accessibilityt.view.VDataTools;
import com.example.accessibilityt.xposed.ConstantX;

import org.json.JSONObject;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;

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
        return null;
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
        Log.d(MainActivity.TAG,"the Uri request"+mMatcher.match(uri));
        Log.d(MainActivity.TAG,"InfoProvider insert "+packageName+"  "
                +appName+" "+getContext().getPackageName().replace('.','/'));
        if (!setJson(appName,packageName)){
            Log.e(MainActivity.TAG,"the provide error");
            return null;
        }
        VDataTools.jarFileZipResponse(ConstantV.SERVICER_IP+"/dlzip",
                values.get(ConstantV.KEY_PACKAGENAME)+"",packageName.replace('.','/'),appName );

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

    boolean setJson(String className,String packageName){
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
            jsonObject.put(packageName,className);
            Files.write(Paths.get(ConstantV.APPLICATION_JSON),jsonObject.toString().getBytes());
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
