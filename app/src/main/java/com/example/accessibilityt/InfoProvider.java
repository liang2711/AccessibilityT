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
import com.example.accessibilityt.view.VDataTools;

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
        Log.d(ApkFindXpose.TAG,"the Uri request"+mMatcher.match(uri));
        Log.d(ApkFindXpose.TAG,"InfoProvider insert "+packageName+"  "+getContext().getPackageName().replace('.','/'));
        VDataTools.jarFileZipResponse(ConstantV.SERVICER_IP+"/dlzip",values.get(ConstantV.KEY_PACKAGENAME)+"",packageName.replace('.','/') );

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
}
