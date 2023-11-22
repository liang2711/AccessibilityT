package com.example.accessibilityt;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.accessibilityt.view.VDataTools;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class XposedReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("com.example.accessibilityt.XposedReceiver")) {
            String urlPath = intent.getStringExtra("urlPath");
            String fileName = intent.getStringExtra("fileName");
            String packageName = intent.getStringExtra("packageName");
            String apkPath=intent.getStringExtra("apkPath");
            Log.d(ApkFindXpose.TAG, "XposedReceiver- urlPath:"+urlPath+"  fileName:"+fileName+" packageName:"+packageName);
            Intent hongBaoServiceIntent=new Intent();
            hongBaoServiceIntent.setAction("com.example.accessibilityt.XposedReceiver.ACTION_AGAIN");
            hongBaoServiceIntent.putExtra("urlPath",urlPath);
            hongBaoServiceIntent.putExtra("fileName",fileName);
            hongBaoServiceIntent.putExtra("packageName",packageName);
            hongBaoServiceIntent.putExtra("apkPath",apkPath);
            context.sendBroadcast(hongBaoServiceIntent);
        }
    }
}
