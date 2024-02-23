package com.example.accessibilityt;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

public class XDataTools {
    private static final String CLASSNAME_TAG="CLASSNAME_TAG";
    private static final String JAVABOYE_TAG="ISEND_TAG";
    public static Handler handler=new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            Bundle bundle=null;
            String packageName=null;
            String fileName=null;
            String urlPath=null;
            String apkPath=null;
            super.handleMessage(msg);
            switch (msg.what){
                case 0:
                    break;
                case 1:
                    packageName= (String) msg.obj;
                    Log.d(ApkFindXpose.TAG,"network requset end");
                    if (ApkFindXpose.zipFile!=null) {
                        try {
                            ApkFindXpose.zipFile.close();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    //为了防止thread的覆盖
                    if (thread!=null){
                        isInterrupt=true;
                        thread=null;
                    }
                    if (ApkFindXpose.mContext==null&&thread==null){
                        //初始化
                        if (isInterrupt)
                            isInterrupt=false;
                        if (isIsFinishContext)
                            isIsFinishContext=false;
                        Log.d(ApkFindXpose.TAG,"context is null (updateViewContent)");
                        lock=new Object();
                        if (lock!=null)
                            loopSetViewProvider();
                        return;
                    }
                    //数据库存储
                    ApkFindXpose.updateViewContent(packageName);
                    break;
                case 2:
                    break;
                case 9:
                    //如果请求失败重新请求（以弃用）
                    Log.d(ApkFindXpose.TAG,"handler 9 of what");
                    bundle=msg.getData();
                    fileName=bundle.getString("fileName");
                    packageName=bundle.getString("packageName");
                    urlPath=bundle.getString("urlPath");
                    apkPath=bundle.getString("apkPath");
                    isStatuLoop=true;
                    //判断有木有context有就直接执行ageinRequsetDex没有就在 11执行
                    if (executeLoop()) {
                        isStatuLoop=false;
                        return;
                    }
                    Log.d(ApkFindXpose.TAG,"handler 9 of what context exist");
                    ageinRequsetDex(ApkFindXpose.mContext,urlPath,fileName,packageName,apkPath);
                    break;
                case 10:
                    ApkFindXpose.updateViewContent(ApkFindXpose.mContext.getPackageName());
                    break;
                case 11:
                    Log.d(ApkFindXpose.TAG,"handler 11 of what");
                    bundle=msg.getData();
                    fileName=bundle.getString("fileName");
                    packageName=bundle.getString("packageName");
                    urlPath=bundle.getString("urlPath");
                    apkPath=bundle.getString("apkPath");
                    ageinRequsetDex(ApkFindXpose.mContext,urlPath,fileName,packageName,apkPath);
                    break;
            }
        }
    };
    private static boolean executeLoop(){
        if (thread!=null){
            isInterrupt=true;
            thread=null;
        }
        if (ApkFindXpose.mContext==null&&thread==null){
            if (isInterrupt)
                isInterrupt=false;
            if (isIsFinishContext)
                isIsFinishContext=false;
            Log.d(ApkFindXpose.TAG,"context is null (updateViewContent)");
            lock=new Object();
            if (lock!=null){
                loopSetViewProvider();
                return true;
            }
        }
        return false;
    }
    public static void extraDoPost(String urlPath, String fileName, String packageName, InputStream inputStream,boolean isEnd,String apkPath,String mark){
        new Thread(){
            @Override
            public void run() {
                super.run();
                doPost(urlPath,fileName,packageName,inputStream,isEnd,apkPath,mark);
            }
        }.start();
    }
    private static void doPost(String urlPath,String fileName,String packageName,InputStream inputStream,boolean isEnd,String apkPath,String mark){
        Log.d(ApkFindXpose.TAG,urlPath+" "+fileName+" "+packageName);
        final String LINE_FEED = "\r\n";
        final String BOUNDARY = "#";
        try {
//            Log.d(ApkFindXpose.TAG,"----------------------1");
            URL url = new URL(urlPath);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
            OutputStream outputStream = connection.getOutputStream();
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, "UTF-8"), true);
//            Log.d(ApkFindXpose.TAG,"----------------------2");
            // 添加表单字段参数
            writer.append("--" + BOUNDARY).append(LINE_FEED);
            writer.append("Content-Disposition: form-data; name=\"packageName\"").append(LINE_FEED);
            writer.append(LINE_FEED);
            writer.append(packageName).append(LINE_FEED);
//            Log.d(ApkFindXpose.TAG,"----------------------3");
            // 添加文件参数
            writer.append("--" + BOUNDARY).append(LINE_FEED);
            writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"" + fileName + "\"").append(LINE_FEED);
            writer.append("Content-Type: " + URLConnection.guessContentTypeFromName(fileName)).append(LINE_FEED);
            writer.append(LINE_FEED);
            writer.flush();
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.flush();
            inputStream.close();
            writer.append(LINE_FEED);
            writer.append("--" + BOUNDARY + "--").append(LINE_FEED);
            writer.close();

            int responseCode = connection.getResponseCode();
//            Log.d(ApkFindXpose.TAG,responseCode+" ----------------------------4");
            if (responseCode!=200){
                Log.d(ApkFindXpose.TAG,packageName+" "+fileName+" requstion error");
            }
            InputStream read=null;
            if (responseCode>=HttpURLConnection.HTTP_BAD_REQUEST)
                read=connection.getErrorStream();
            else
                read=connection.getInputStream();

            BufferedReader reader=new BufferedReader(new InputStreamReader(read));
            String line;
            StringBuilder response=new StringBuilder();
            while ((line=reader.readLine())!=null){
                response.append(line);
            }
            reader.close();
            Log.d(ApkFindXpose.TAG,response.toString());

            Message msg=new Message();
            msg.obj=packageName;
            if (!isEnd)
                msg.what=1;
            else
                msg.what=0;
            handler.sendMessage(msg);
            // 处理响应...
        } catch (IOException e) {
            Log.e(ApkFindXpose.TAG,"this error:"+mark+" urlPath:"+urlPath+" fileName:"+fileName+" packageName:"+packageName);
//            Message msg=new Message();
//            Bundle bundle=new Bundle();
//            bundle.putString("urlPath",urlPath);
//            bundle.putString("fileName",fileName);
//            bundle.putString("packageName",packageName);
//            bundle.putString("apkPath",apkPath);
//            msg.setData(bundle);
//            msg.what=9;
//            handler.sendMessage(msg);
        }
    }
    //向无障碍再次请求代码
    private static void ageinRequsetDex(Context context,String urlPath, String fileName, String packageName,String apkPath){
        Log.d(ApkFindXpose.TAG,"ageinRequsetDex-----------urlPath:"+urlPath+" fileName"+fileName+" packageName:"+packageName+"  context:"+context.toString());
        if (context==null){
            Log.d(ApkFindXpose.TAG,"ageinRequsetDex-----------end");
            return;
        }
        Intent intent = new Intent();
        intent.setAction("com.example.accessibilityt.XposedReceiver");
        try {
            intent.putExtra("urlPath",urlPath);
            intent.putExtra("fileName",fileName);
            intent.putExtra("packageName",packageName);
            intent.putExtra("apkPath",apkPath);
            intent.setClass(context,Class.forName("com.example.accessibilityt.XposedReceiver"));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        context.sendBroadcast(intent);
    }
    public static String displayApkPath(String text){
        String prefix="/data/app/";
        String suffix=".apk";
        int s=text.indexOf(prefix);
        if (s>=text.length()){
            return null;
        }
        String a=text.substring(text.indexOf(prefix)).split("\"")[0];
        if(a.endsWith(suffix)){
            return a;
        }
        return null;
    }
    static boolean isIsFinishContext=false;
    static boolean isFinishIcon=false;
    private static Thread thread=null;
    private static volatile boolean isInterrupt=false;

    public static Object lock=null;
    private static volatile boolean isStatuLoop=false;
    //判断有木有context
    public static void loopSetViewProvider(){
         thread=new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (lock){
                    while (!isIsFinishContext){
                        try {
                            if (isInterrupt)
                                return;
                            lock.wait();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
                Message msg=new Message();
                if (!isStatuLoop){
                    //当context被hook到时调用
                    msg.what=10;
                }else {
                    msg.what=11;
                }
                handler.sendMessage(msg);

            }
        });
         thread.start();
    }
}
