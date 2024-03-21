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
import com.example.accessibilityt.dao.AppCodeInfo;
import com.example.accessibilityt.dao.AppCodeInfoDao;
import com.example.accessibilityt.dao.RDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
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
//    public static ExecutorService executorService=Executors.newFixedThreadPool(5);

    public static ThreadPoolExecutor threadPoolExecutor=new ThreadPoolExecutor(5, 5,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>());
    public static Handler handler=new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 1:
                    if (FloadWindowService.mContext==null){
                        Log.e(MainActivity.TAG,"not find context in DAO");
                        return;
                    }
                    //如果没有先添加临时map对象（开机时）
                    if (mapClassCode==null){
                        mapClassCode=new HashMap<>();
                        daoInit(FloadWindowService.mContext);
                    }
                    Bundle bundle=msg.getData();
                    String className=bundle.getString(ConstantV.KEY_CLASSNAME);
                    String appName=bundle.getString(ConstantV.KET_APPNAME);
//                    Log.e(MainActivity.TAG,appName+"  "+className);
                    if (mapClassCode==null)
                        mapClassCode=new HashMap<>();
                    if (mapClassCode.containsKey(className)){
                        if (mapClassCode.get(className).equals(appName)){
                            //为了room框架insert方法不处罚异常（数据重复）
                            Log.e(MainActivity.TAG,"mapClassCode value duplicate！");
                            return;
                        }
                    }
                    mapClassCode.put(className,appName);
                    AppCodeInfo info=new AppCodeInfo();
                    info.className=bundle.getString(ConstantV.KEY_CLASSNAME);
                    info.code=bundle.getString(ConstantV.KEY_CONTEXT);
                    info.appName=bundle.getString(ConstantV.KET_APPNAME);
                    Log.d(MainActivity.TAG,className+" "+appName);
                    setDatabase(info);
                    break;
                case 2:
//                    C2JA c2JA= (C2JA) msg.obj;
//                    if (c2JA==null){
//                        Log.e(MainActivity.TAG,"C2JA = null");
//                        return;
//                    }
//                    Log.d(MainActivity.TAG,"C2JA = c2JA.uri="+c2JA.uri+"c2JA.className="+c2JA.className+"" +
//                            "c2JA.appName="+c2JA.appName+"c2JA.packageName="+c2JA.packageName);
//                    doPostPool(c2JA.uri,c2JA.className,c2JA.inputStream,c2JA.appName,c2JA.packageName);
                    break;
                case 10:
                    mapClassCode= (Map<String, String>) msg.obj;
                    break;
            }
        }
    };
    public static RDatabase rDatabase=null;

    public static void daoInit(Context context){
        new Thread(){
            @Override
            public void run() {
                super.run();
                rDatabase=RDatabase.getInstance(context);
                if (rDatabase!=null){
                    if (mapClassCode==null){
                        AppCodeInfoDao dao = rDatabase.getDao();
                        dao.deleteNullItem();
                        Message message=Message.obtain();
                        //好的数据库了的所以信息，给新增信息判断条件不能重复
                        message.obj= getDaoItemValue(dao.getAll());
                        message.what=10;
                        handler.sendMessage(message);
                    }
                }

            }
        }.start();
    }

    private static Map<String,String> getDaoItemValue(List<AppCodeInfo> list){
        Map<String,String> map=new HashMap<>();
        for (AppCodeInfo info : list){
            map.put(info.className,info.appName);
        }
        return map;
    }

    public static void setDatabase(AppCodeInfo info){
        new Thread(){
            @Override
            public void run() {
                super.run();
                if (info.appName.equals("")||info.appName==null){
                    Log.e(MainActivity.TAG+"dao","not find appName");
                    return;
                }
                if (info.code.equals("")||info.code==null){
                    Log.e(MainActivity.TAG+"dao","not find code");
                    return;
                }
                if (info.className.equals("")||info.className==null){
                    Log.e(MainActivity.TAG+"dao","not find className");
                    return;
                }
                if (FloadWindowService.mContext==null){
                    Log.e(MainActivity.TAG+"dao","not find context in DAO");
                    return;
                }
                try {

                    AppCodeInfoDao dao=rDatabase.getDao();
                    dao.insert(info);
                }catch (Exception e){
                    Log.e(MainActivity.TAG,"dao error! in insert");
                }
            }
        }.start();
    }
    public static void jarFileZipResponse(String urlPath, String filePackageName, String appPath,String appName,String fileType){
        new Thread(){
            @Override
            public void run() {
                super.run();

                List<byte[]> bytes=new ArrayList<>();
                OkHttpClient client=new OkHttpClient();

                HttpUrl.Builder urlBuilder=HttpUrl.parse(urlPath).newBuilder();
                urlBuilder.addQueryParameter("filePackageName",filePackageName);
                urlBuilder.addQueryParameter("fileType",fileType);

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
                            if (!zipEntry.getName().endsWith(".jar"))
                                continue;
                            byte[] b=b2Stream(zipInputStream);
//                            ByteArrayOutputStream bos=new ByteArrayOutputStream();
//                            byte[] buffer=new byte[4096];
//                            int bytesRead;
//                            while ((bytesRead=zipInputStream.read(buffer))!=-1){
//                                bos.write(buffer,0,bytesRead);
//                            }
//                            byte[] b=bos.toByteArray();
//                            Log.d(MainActivity.TAG,"JarFileZipResponse b:"+b.length);
                            bytes.add(b);
                        }
                        Log.d(MainActivity.TAG,"JarFileZipResponse bytes:"+bytes.size());
                        int splitIndex=bytes.size()/2;
                        c2j(bytes.subList(0,splitIndex),appName,filePackageName,appPath);
                        c2j(bytes.subList(splitIndex,bytes.size()),appName,filePackageName,appPath);
                        zipInputStream.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private static void c2j(List<byte[]> bytes,String appName,String filePackageName,String appPath) throws IOException {
        new Thread(){
            @Override
            public void run() {
                super.run();
//                Log.d(MainActivity.TAG,"JarFileZipResponse c2j bytes:"+bytes.size());
                for (byte[] b:bytes){
                    Log.d(MainActivity.TAG,"JarFileZipResponse c2j b:"+b.length);
                    ByteArrayInputStream byteArrayInputStream=new ByteArrayInputStream(b);
                    JarInputStream jarInputStream= null;
//                    Log.d(MainActivity.TAG,"JarFileZipResponse c2j for b:"+b.length);
                    try {
                        jarInputStream = new JarInputStream(byteArrayInputStream);
                        JarEntry classEntry=null;
                        while ((classEntry=jarInputStream.getNextJarEntry())!=null){
                            String className=classEntry.getName();
//                            Log.d(MainActivity.TAG,"fristclass:"+classEntry.getName());
                            if (className.contains("$")||!className.endsWith(".class")||!className.contains(appPath))
                                continue;
                            String[] filename=className.split("/");
//                          doPost( ConstantV.SERVICER_IP+"/outClassToJava",filename[filename.length-1],jarInputStream,appName,filePackageName);
                            doPostPool(new C2JA(ConstantV.SERVICER_IP+"/outClassToJava",filename[filename.length-1],jarInputStream,appName,filePackageName));
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }.start();
    }

    static int doPost(String urlPath,String className,byte[] inputStream,String appName,String packageName){
        Log.e(MainActivity.TAG,"executorService------------doPost:"+urlPath+" "+className+" "+"  "+appName);
        final String LINE_FEED = "\r\n";
        final String BOUNDARY = "#";
        String c2Name=className.split("\\.")[0];
        try {
            URL url = new URL(urlPath);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
            OutputStream outputStream = connection.getOutputStream();
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, "UTF-8"), true);

            // 添加表单字段参数
            writer.append("--" + BOUNDARY).append(LINE_FEED);
            writer.append("Content-Disposition: form-data; name=\"data\"").append(LINE_FEED);
            writer.append(LINE_FEED);
            writer.append(c2Name+";"+packageName).append(LINE_FEED);


            // 添加文件参数
            writer.append("--" + BOUNDARY).append(LINE_FEED);
            writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"" + className + "\"").append(LINE_FEED);
            writer.append("Content-Type: " + URLConnection.guessContentTypeFromName(className)).append(LINE_FEED);
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
            msg.what=1;
            Bundle bundle=new Bundle();
            bundle.putString(ConstantV.KET_APPNAME,appName);
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

    static int doPostPool(C2JA c2JA){
        if (threadPoolExecutor==null){
            Log.e(MainActivity.TAG,"executorService==null");
            threadPoolExecutor=new ThreadPoolExecutor(5, 5,
                    0L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<Runnable>());
        }
        threadPoolExecutor.execute(new Runnable() {
            @Override
            public void run() {
                doPost(c2JA.uri,c2JA.className,c2JA.bytes,c2JA.appName,c2JA.packageName);
            }
        });
        return 0;
    }

    public static void all(String urlPath,String packageName){
        new Thread(){
            @Override
            public void run() {
                super.run();
                OkHttpClient client=new OkHttpClient();

                HttpUrl.Builder urlBuilder=HttpUrl.parse(urlPath).newBuilder();
                urlBuilder.addQueryParameter("filePackageName",packageName);

                String url=urlBuilder.build().toString();

                Request getRequest=new Request.Builder().get()
                        .url(url)
                        .build();

                try {
                    Response response=client.newCall(getRequest).execute();
                    System.out.println(response.body());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }.start();
    }

    public static void javaFileZipResponse(String urlPath, String filePackageName,String fileType,String appName,List<AppCodeInfo> list){
        new Thread(){
            @Override
            public void run() {
                super.run();

                List<AppCodeInfo> infos=new ArrayList<>();
                OkHttpClient client=new OkHttpClient();

                HttpUrl.Builder urlBuilder=HttpUrl.parse(urlPath).newBuilder();
                urlBuilder.addQueryParameter("filePackageName",filePackageName);
                urlBuilder.addQueryParameter("fileType",fileType);

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
                            BufferedReader reader=new BufferedReader(new InputStreamReader(zipInputStream));
                            String line;
                            StringBuilder stringBuilder=new StringBuilder();
                            while ((line=reader.readLine())!=null){
                                stringBuilder.append(line);
                            }
                            Log.d(MainActivity.TAG,"get java file className:"+zipEntry.getName());
                            AppCodeInfo info=new AppCodeInfo();
                            info.className=zipEntry.getName();
                            info.code=stringBuilder.toString();
                            info.appName=appName;
                            infos.add(info);
                        }
                        zipInputStream.close();
                    }
                    List<AppCodeInfo> p=new ArrayList<>();
                    if (list.size()==0){
                        for (AppCodeInfo info:infos){
                            setDatabase(info);
                        }
                        list.addAll(infos);
                    }else {
                        for (AppCodeInfo a:infos){
                            boolean isStory=true;
                            for (AppCodeInfo b:list){
                                if (a.className.equals(b.className)){
                                    isStory=false;
                                    break;
                                }
                            }
                            if (isStory){
                                p.add(a);
                                setDatabase(a);
                            }
                        }
                        list.addAll(p);
                    }
                    Log.d(MainActivity.TAG,"inquiry java file to list object size:"+list.size());
                    Message message=Message.obtain();
                    Bundle bundle=new Bundle();
                    bundle.putParcelableArrayList("list",new ArrayList<>(list));
                    message.setData(bundle);
                    CcodeShowView.handler.sendMessage(message);


                } catch (IOException e) {
                    e.printStackTrace();
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
    public static byte[] b2Stream(InputStream jarInputStream) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();
        byte[] buffer=new byte[4096];
        int bytesRead;
        while ((bytesRead = jarInputStream.read(buffer)) != -1) {
            byteArrayOutputStream.write(buffer, 0, bytesRead);
        }
        return byteArrayOutputStream.toByteArray();
    }
    // 自定义 RequestBody 类来包含 ByteArrayInputStream 数据和参数
    private static void updateViewContent(String packagheName){
        ContentResolver mContentResolver=context.getContentResolver();
        ContentValues values=new ContentValues();
        values.put(ConstantV.KEY_PACKAGENAME,packagheName);
        mContentResolver.insert(ConstantV.CONTENT_URI,values);
    }
    public static boolean setMJson(){
        if (new File(ConstantV.MODULE_JSON).exists()){
            JSONObject jsonObject=null;
            try {
                jsonObject=new JSONObject(new String(Files.readAllBytes(Paths.get(ConstantV.MODULE_JSON))));
                return jsonObject.getBoolean(ConstantV.KET_MODULE);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return true;
    }
    private static class C2JA implements Serializable {
        private String uri;
        private String className;
        private byte[] bytes;
        private String appName;
        private String packageName;
        public C2JA(String uri,String className,JarInputStream inputStream,String appName,String packageName) throws IOException {
            this.uri=uri;
            this.packageName=packageName;
            this.appName=appName;
            this.className=className;

            bytes=b2Stream(inputStream);
        };
    }
}
