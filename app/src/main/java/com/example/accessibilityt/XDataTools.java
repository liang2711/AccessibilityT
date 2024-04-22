package com.example.accessibilityt;

import static com.example.accessibilityt.ApkFindXpose.TAG;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.accessibilityt.view.ConstantV;
import com.example.accessibilityt.xposed.ConstantX;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class XDataTools {
    private static final String CLASSNAME_TAG="CLASSNAME_TAG";
    private static final String JAVABOYE_TAG="ISEND_TAG";
    public static Handler handler=new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 0:
                    Log.d(TAG,"network requset ing");
                    break;
                case 1:
                    if (!ApkFindXpose.isRun(msg.getData().getString("s1"),ApkFindXpose.mContext,msg.getData().getString("s2"))){
                        Log.e(TAG,"运行post请求失败");
                    }
                    break;
                case 10:
                    ApkFindXpose.updateViewContent(ApkFindXpose.mContext.getPackageName());
                    break;
            }
        }
    };

    public static boolean executeLoop(String packageName,String clzloader){
        //为了防止thread的覆盖 初始化
        if (thread!=null){
            isInterrupt=true;
            thread=null;
        }
        if (ApkFindXpose.mContext==null&&thread==null){
            if (isInterrupt)
                isInterrupt=false;
            if (isIsFinishContext)
                isIsFinishContext=false;
            Log.d(TAG,"context is null");
            lock=new Object();
            if (lock!=null){
                loopSetViewProvider(packageName,clzloader);
                return true;
            }
        }
        return false;
    }
    public static void extraDoPost(String urlPath, String fileName, String packageName, byte[] inputStream){
        if (urlPath==null){
            Log.d(TAG,"network requset end");
            if (ApkFindXpose.zipFile!=null) {
                try {
                    ApkFindXpose.zipFile.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
//            if (executeLoop()) return;
            //数据库存储
            ApkFindXpose.updateViewContent(packageName);
            return;
        }
//        doPost(urlPath,fileName,packageName,inputStream);
//        if (ApkFindXpose.executorService!=null){
            ApkFindXpose.executorService.execute(new Runnable() {
                @Override
                public void run() {
                    doPost(urlPath,fileName,packageName,inputStream);
                }
            });
    }
    private static void doPost(String urlPath,String fileName,String packageName,byte[] inputStream){
        final String LINE_FEED = "\r\n";
        final String BOUNDARY = "#";
        try {
            Log.d(TAG,"doPost "+inputStream.length);
            URL url = new URL(urlPath);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
            OutputStream outputStream = connection.getOutputStream();
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, "UTF-8"), true);
            // 添加表单字段参数
            writer.append("--" + BOUNDARY).append(LINE_FEED);
            writer.append("Content-Disposition: form-data; name=\"packageName\"").append(LINE_FEED);
            writer.append(LINE_FEED);
            writer.append(packageName).append(LINE_FEED);
            // 添加文件参数
            writer.append("--" + BOUNDARY).append(LINE_FEED);
            writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"" + fileName + "\"").append(LINE_FEED);
            writer.append("Content-Type: " + URLConnection.guessContentTypeFromName(fileName)).append(LINE_FEED);
            writer.append(LINE_FEED);
            writer.flush();
//            byte[] buffer = new byte[4096];
//            int bytesRead;
//            while ((bytesRead = inputStream.read(buffer)) != -1) {
//                outputStream.write(buffer, 0, bytesRead);
//            }
            outputStream.write(inputStream);
            outputStream.flush();
            writer.append(LINE_FEED);
            writer.append("--" + BOUNDARY + "--").append(LINE_FEED);
            writer.close();

            int responseCode = connection.getResponseCode();
            if (responseCode!=200){
//                Log.e(TAG,packageName+" "+fileName+" requstion error");
                Log.e(TAG,"this error:  responseCode"+responseCode+" urlPath:"+urlPath+" fileName:"+fileName+" packageName:"+packageName);
                return;
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


            Message msg=new Message();
            msg.what=0;
            handler.sendMessage(msg);
            // 处理响应...
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG,"this error: urlPath:"+urlPath+" fileName:"+fileName+" packageName:"+packageName);
        }
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
    static volatile boolean isIsFinishContext=false;
    private static Thread thread=null;
    private static volatile boolean isInterrupt=false;

    public static Object lock=null;
    private static volatile boolean isStatuLoop=false;
    private static int threadTime=1;
    //判断有木有context
    public static void loopSetViewProvider(String packageName,String clzloader){
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
                        try {
                            threadTime++;
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        if (threadTime>=20){
                            threadTime=1;
                            return;
                        }
                    }
                }
                Log.d(TAG,"loop end  "+(ApkFindXpose.mContext!=null));
                if (!ApkFindXpose.isRun(clzloader,ApkFindXpose.mContext, packageName)) {
                    Log.e(TAG, "运行post请求失败");
                }
            }
        });
         thread.start();
    }
    public static String getURI(){
        if (new File(ConstantX.MODULE_JSON).exists()){
            JSONObject jsonObject=null;
            try {
                jsonObject=new JSONObject(new String(Files.readAllBytes(Paths.get(ConstantX.MODULE_JSON))));
                return jsonObject.getString(ConstantX.KET_URI);
            } catch (JSONException e) {
                return null;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }
    public static String getServiceUri(){
        String str=getURI();
        if (str==null || str.equals("")){
            return ConstantX.SERVICER_IP;
        }
        String uri="http://"+str+":8088/dex2java";
        return uri;
    }
}
