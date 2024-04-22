package com.example.accessibilityt.view;


import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.accessibilityt.MainActivity;
import com.example.accessibilityt.dao.AppCodeInfo;
import com.example.accessibilityt.dao.AppCodeInfoDao;
import com.example.accessibilityt.dao.RDatabase;
import com.example.accessibilityt.xposed.ConstantX;

import org.json.JSONArray;
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
import java.util.Iterator;
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

import de.robv.android.xposed.callbacks.XC_LoadPackage;
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

    public volatile static ThreadPoolExecutor threadPoolExecutor=new ThreadPoolExecutor(5, 5,
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
    public static Thread jarFileZipResponse(String urlPath, String filePackageName, String appPath,String appName,String fileType,Context context){
        return new Thread(){
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
                        c2j2(bytes,appName,filePackageName,appPath);
//                        c2j(bytes.subList(0,splitIndex),appName,filePackageName,appPath);
//                        c2j(bytes.subList(splitIndex,bytes.size()),appName,filePackageName,appPath);
                        zipInputStream.close();
                    }
                } catch (IOException e) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (context==null){
                                Log.e(MainActivity.TAG,"not find context,network error!");
                                return;
                            }
                            Toast.makeText(context,"请检查网络问题",Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        };
    }
    static Object lock=new Object();
    private static void c2j(List<byte[]> bytes,String appName,String filePackageName,String appPath) throws IOException {
        new Thread(){
            @Override
            public void run() {
                super.run();
//                Log.d(MainActivity.TAG,"JarFileZipResponse c2j bytes:"+bytes.size());
                synchronized (lock){
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
                                doPostPool(new C2JA(VDataTools.getServiceUri()+"/outClassToJava",filename[filename.length-1],jarInputStream,appName,filePackageName));
                            }
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
                }
        }.start();
    }

    private static void c2j2(List<byte[]> bytes,String appName,String filePackageName,String appPath) throws IOException {
        for (byte[] b:bytes){
            Log.d(MainActivity.TAG,"JarFileZipResponse c2j2 b:"+b.length);
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
                    doPostPool(new C2JA(VDataTools.getServiceUri()+"/outClassToJava",filename[filename.length-1],jarInputStream,appName,filePackageName));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    static int doPost(String urlPath,String className,byte[] inputStream,String appName,String packageName){
        Log.d(MainActivity.TAG,"executorService------------doPost:"+urlPath+" "+className+" "+"  "+appName);
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
            bundle.putString(ConstantV.KEY_CLASSNAME,c2Name);
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
                Log.e(MainActivity.TAG,"doPostPool uri+"+c2JA.uri+" b:"+c2JA.bytes.length);
                doPost(c2JA.uri,c2JA.className,c2JA.bytes,c2JA.appName,c2JA.packageName);
            }
        });
        return 0;
    }

    public static Thread all(String urlPath,String packageName,Context context){
        return new Thread(){
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
                    Log.d(MainActivity.TAG,"all codeState:"+response.code());
                } catch (IOException e) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (context==null){
                                Log.e(MainActivity.TAG,"not find context,network error!");
                                return;
                            }
                            Toast.makeText(context,"请检查网络问题",Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        };
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
                    Log.d(MainActivity.TAG,"javaFileZipResponse codeState:"+response.code());
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

                            AppCodeInfo info=new AppCodeInfo();
                            info.className=zipEntry.getName().split("\\.")[0];
                            info.code=stringBuilder.toString();
                            info.appName=appName;
                            infos.add(info);
                        }
                        zipInputStream.close();
                    }
                    List<AppCodeInfo> p=new ArrayList<>();
                    for (AppCodeInfo a:infos){
                        boolean isStory=true;
                        for (AppCodeInfo b:list){
//                            Log.d(MainActivity.TAG,"compare AclassName and BclassName:"+a.className+"  "+b.className);
                            if (a.className.equals(b.className)){
//                                Log.d(MainActivity.TAG,"compare AclassName and BclassName  than is equals");
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
//                    Log.d(MainActivity.TAG,"inquiry java file to list object size:"+list.size());
                    Message message=Message.obtain();
                    Bundle bundle=new Bundle();
                    bundle.putParcelableArrayList("list",new ArrayList<>(list));
                    message.setData(bundle);
                    CcodeShowView.handler.sendMessage(message);
                } catch (IOException e) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (context==null){
                                Log.e(MainActivity.TAG,"not find context,network error!");
                                return;
                            }
                            Toast.makeText(context,"请检查网络问题",Toast.LENGTH_SHORT).show();
                        }
                    });
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

    //json文件操作
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
    public static boolean putPK(List<String> list){
        JSONObject jsonObject=null;
        try {
            if (new File(ConstantV.MODULE_JSON).exists()){
                jsonObject=new JSONObject(new String(Files.readAllBytes(Paths.get(ConstantV.MODULE_JSON))));
            }else {
                jsonObject=new JSONObject();
            }
            JSONArray array=new JSONArray();
            for (String str:list)
                array.put(str);
            jsonObject.put(ConstantV.KEY_PACKAGENAME,array);
            Files.write(Paths.get(ConstantV.MODULE_JSON),jsonObject.toString().getBytes());
            return true;
        }catch (Exception e){
            return false;
        }
    }
    public static List<String> getPK(){
        if (new File(ConstantV.MODULE_JSON).exists()){
            JSONObject jsonObject=null;
            List<String> list;
            try {
                jsonObject=new JSONObject(new String(Files.readAllBytes(Paths.get(ConstantV.MODULE_JSON))));
                JSONArray array=jsonObject.getJSONArray(ConstantV.KEY_PACKAGENAME);
                if (array.length()==0) return null;
                list=new ArrayList<>();
                for (int i=0;i<array.length();i++){
                    list.add(array.get(i).toString());
                }
                return list;
            } catch (JSONException e) {
                return null;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }
    public static boolean putURI(String uri){
        JSONObject jsonObject=null;
        try {
            if (new File(ConstantV.MODULE_JSON).exists()){
                jsonObject=new JSONObject(new String(Files.readAllBytes(Paths.get(ConstantV.MODULE_JSON))));
            }else {
                jsonObject=new JSONObject();
            }
            jsonObject.put(ConstantV.KET_URI,uri);
            Files.write(Paths.get(ConstantV.MODULE_JSON),jsonObject.toString().getBytes());
            return true;
        }catch (Exception e){
            return false;
        }
    }
    public static String getURI(){
        if (new File(ConstantV.MODULE_JSON).exists()){
            JSONObject jsonObject=null;
            try {
                jsonObject=new JSONObject(new String(Files.readAllBytes(Paths.get(ConstantV.MODULE_JSON))));
                return jsonObject.getString(ConstantV.KET_URI);
            } catch (JSONException e) {
                return null;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
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
    public static String getServiceUri(){
        String str=getURI();
        if (str==null || str.equals("")){
            return ConstantV.SERVICER_IP;
        }
        String uri="http://"+str+":8088/dex2java";
        return uri;
    }
    public static boolean isUse(String packageName){
        JSONObject jsonObject;
        if (new File(ConstantV.APPLICATION_JSON).exists()){
            //解析json文件
            try {
                jsonObject=new JSONObject(new String(Files.readAllBytes(Paths.get(ConstantV.APPLICATION_JSON))));
            } catch (JSONException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if (jsonObject!=null) {
                Iterator<String> it=jsonObject.keys();
                while (it.hasNext()){
                    String key=it.next();
                    if (key.equals(packageName)||key.contains(packageName)){
//                        Log.d(MainActivity.TAG,"ApkFindXposed_create_json_key  contains: "+key);
                        return true;
                    }
                }
            }
        }
        return false;
    }
    public static boolean isPK(String packageName){
        List<String> list=getPK();
        if (list==null||list.size()==0)
            return false;
//        Log.e(MainActivity.TAG,"isPK packageName:"+packageName+" contains:"+list.contains(packageName)+"   ");
        for (String str:list){
            Log.e(MainActivity.TAG,"list: "+str+"   "+str.equals(packageName));
            if (str.equals(packageName))return true;
        }
        if (list.contains(packageName))
            return true;
        return false;
    }
}
