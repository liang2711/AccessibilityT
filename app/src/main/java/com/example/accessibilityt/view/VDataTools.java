package com.example.accessibilityt.view;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.accessibilityt.MainActivity;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.Okio;
import okio.Source;

public class VDataTools {
    private static Context context;
    public VDataTools(Context context){
        this.context=context;
    }
    public VDataTools(){}
    public static Map<String,String> mapClassCode=null;
    public static Handler handler=new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 1:
                    if (mapClassCode==null)
                        mapClassCode=new HashMap<>();
                    Bundle bundle=msg.getData();
                    mapClassCode.put(bundle.getString(ConstantV.KEY_CLASSNAME),bundle.getString(ConstantV.KEY_CONTEXT));
                    break;
                case 2:
                    String packageName= (String) msg.obj;
                    updateViewContent(packageName);
                    break;
            }
        }
    };
    public static void jarFileZipResponse(String urlPath, String filePackageName, String appPackageName){
        new Thread(){
            @Override
            public void run() {
                super.run();
                Log.d(MainActivity.TAG,"JarFileZipResponse");
                OkHttpClient client=new OkHttpClient();

                HttpUrl.Builder urlBuilder=HttpUrl.parse(urlPath).newBuilder();
                urlBuilder.addQueryParameter("filePackageName",filePackageName);

                String url=urlBuilder.build().toString();

                Request getRequest=new Request.Builder().get()
                        .url(url)
                        .build();

                try {
                    Response response=client.newCall(getRequest).execute();
                    ResponseBody body=response.body();

                    if (body!=null){
                        InputStream inputStream=body.byteStream();
                        ZipInputStream zipInputStream=new ZipInputStream(new BufferedInputStream(inputStream));
                        ZipEntry zipEntry=null;
                        while ((zipEntry=zipInputStream.getNextEntry())!=null){
                            InputStream jarEntry=zipInputStream;
                            Log.d(MainActivity.TAG,"name: "+zipEntry.getName());
                            JarInputStream jarInputStream=new JarInputStream(new BufferedInputStream(jarEntry));
                            JarEntry classEntry=null;
                            while ((classEntry=jarInputStream.getNextJarEntry())!=null){
                                String className=classEntry.getName();
                                if (className.contains("$")||!className.endsWith(".class")||!className.contains(appPackageName))
                                    continue;
                                String[] filename=className.split("/");
                                Log.d(MainActivity.TAG,"class:"+classEntry.getName());
                                doPost( ConstantV.SERVICER_IP+"/outClassToJava",filename[filename.length-1],jarInputStream);

                            }
                        }
                        zipInputStream.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    static int doPost(String urlPath,String className,InputStream inputStream){
        final String LINE_FEED = "\r\n";
        final String BOUNDARY = "#";
        try {
            URL url = new URL(urlPath);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
            OutputStream outputStream = connection.getOutputStream();
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, "UTF-8"), true);

            // 添加文件参数
            writer.append("--" + BOUNDARY).append(LINE_FEED);
            writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"" + className + "\"").append(LINE_FEED);
            writer.append("Content-Type: " + URLConnection.guessContentTypeFromName(className)).append(LINE_FEED);
            writer.append(LINE_FEED);
            writer.flush();
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.flush();
            writer.append(LINE_FEED);
            writer.append("--" + BOUNDARY + "--").append(LINE_FEED);
            writer.close();

            int responseCode = connection.getResponseCode();

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
            Log.d(MainActivity.TAG,response.toString()+" codestate:"+responseCode);
            reader.close();


            Message msg=new Message();
            msg.what=1;
            Bundle bundle=new Bundle();
            bundle.putString(ConstantV.KEY_CLASSNAME,className);
            bundle.putString(ConstantV.KEY_CONTEXT,response.toString());
            msg.setData(bundle);
            handler.sendMessage(msg);
            // 处理响应...
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }
    public static void extraDoPost(String urlPath, String fileName, String packageName, InputStream inputStream,String mark){
        new Thread(){
            @Override
            public void run() {
                super.run();
                doPost(urlPath,fileName,packageName,inputStream,mark);
            }
        }.start();
    }
    private static void doPost(String urlPath, String fileName, String packageName, InputStream inputStream, String mark){
        Log.d(MainActivity.TAG,urlPath+" "+fileName+" "+packageName);
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
                Log.d(MainActivity.TAG,packageName+" "+fileName+" requstion error");
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
            Log.d(MainActivity.TAG,response.toString());

            Message msg=new Message();
            msg.obj=packageName;
            msg.what=2;
            handler.sendMessage(msg);
            // 处理响应...
        } catch (IOException e) {
            Log.d(MainActivity.TAG,"this error:"+mark+" urlPath:"+urlPath+" fileName:"+fileName+" packageName:"+packageName);
            e.printStackTrace();
        }
    }
    public static void okHttpRequest(String urlPath, String fileName, String packageName, byte[] buffer){
        new Thread(){
            @Override
            public void run() {
                super.run();
                MultipartBody.Builder builder=new MultipartBody.Builder();
                builder.setType(MultipartBody.FORM);
                builder.addFormDataPart("packageName",packageName);

                builder.addFormDataPart("file",fileName,createProgressRequestBoye(MediaType.parse("application/octet-stream"),buffer));

                OkHttpClient client=new OkHttpClient();
                RequestBody requestBody=builder.build();

                Request request=new Request.Builder()
                        .url(urlPath)
                        .post(requestBody)
                        .build();

                final Call call=client.newBuilder().writeTimeout(5000, TimeUnit.SECONDS).build().newCall(request);
                try {
                    Response response=call.execute();
                    Log.d("InitAppA","response:"+response.body().string());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }.start();
    }
    public static RequestBody createProgressRequestBoye(final  MediaType mediaType, final byte[] buffer){
        return new RequestBody() {
            @Override
            public MediaType contentType() {
                return mediaType;
            }

            @Override
            public long contentLength() throws IOException {
                return buffer.length;
            }

            @Override
            public void writeTo(BufferedSink bufferedSink) throws IOException {
                Source source= Okio.source(new ByteArrayInputStream(buffer));
                Buffer buffer=new Buffer();
                long remaining=contentLength();
                long current=0;

                for (long readCount;(readCount=source.read(buffer,2048))!=-1;){
                    bufferedSink.write(buffer,readCount);
                    current+=readCount;
                }
            }
        };
    }
    // 自定义 RequestBody 类来包含 ByteArrayInputStream 数据和参数
    private static void updateViewContent(String packagheName){
        ContentResolver mContentResolver=context.getContentResolver();
        ContentValues values=new ContentValues();
        values.put(ConstantV.KEY_PACKAGENAME,packagheName);
        mContentResolver.insert(ConstantV.CONTENT_URI,values);
    }
}
