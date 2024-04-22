package com.example.accessibilityt.view;

import android.util.Log;

import com.example.accessibilityt.ApkFindXpose;
import com.example.accessibilityt.MainActivity;
import com.example.accessibilityt.view.ConstantV;

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
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ProviderTool {
    static ZipFile zipFile;
    public static boolean getApkFile(String packageName,String clzloader,String uri){
        String apkPath = displayApkPath(clzloader);
        if (!apkPath.endsWith(".apk")){
            Log.e(MainActivity.TAG,"this error apkPath not contain .apk");
            return false;
        }
        File file = new File(apkPath);
        if (file == null || apkPath == null || !file.exists()) {
            Log.d(MainActivity.TAG, "FILE ERROR");
            return false;
        }
        try {
            zipFile = new ZipFile(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        Log.d(MainActivity.TAG,packageName+"    getApkFile");
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            String entryName = entry.getName();
            if (!entryName.contains(".dex"))
                continue;
            if (entryName.equals("classes.dex"))
                continue;

            InputStream inputStream= null;
            byte[] bytes=null;
            try {
                inputStream = zipFile.getInputStream(entry);
                bytes=b2Stream(inputStream);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            extraDoPost( VDataTools.getServiceUri()+"/outDex",entryName,packageName,bytes);
        }
        extraDoPost( null,null,packageName,null);
        return true;
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

    public static boolean extraDoPost(String urlPath, String fileName, String packageName, byte[] inputStream){
        if (urlPath==null){
            Log.d(MainActivity.TAG,"network requset end");
            if (lock!=null){
                synchronized (lock){
                    isIsFinishContext=true;
                    lock.notify();
                }
            }
            return true;
        }
        new Thread(){
            @Override
            public void run() {
                super.run();
                doPost(urlPath,fileName,packageName,inputStream);
            }
        }.start();
        return false;
    }

    private static void doPost(String urlPath,String fileName,String packageName,byte[] inputStream){
        final String LINE_FEED = "\r\n";
        final String BOUNDARY = "#";
        try {
            Log.d(MainActivity.TAG,"doPost "+inputStream.length);
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
                Log.e(MainActivity.TAG,"this error:  responseCode"+responseCode+" urlPath:"+urlPath+" fileName:"+fileName+" packageName:"+packageName);
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

            // 处理响应...
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(MainActivity.TAG,"this error: urlPath:"+urlPath+" fileName:"+fileName+" packageName:"+packageName);
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
    public static Object lock=new Object();
    private static volatile int threadTime=1;
    public static Thread loopSetViewProvider(){
        Thread thread=new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (lock){
                    while (!isIsFinishContext){
                        try {
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
                        if (threadTime>=15){
                            threadTime=1;
                            return;
                        }
                    }
                }
                Log.d(MainActivity.TAG,"loop end  ");
            }
        });
        return thread;
    }
}
