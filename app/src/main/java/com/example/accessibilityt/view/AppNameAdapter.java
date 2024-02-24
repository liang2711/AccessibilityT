package com.example.accessibilityt.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.accessibilityt.AppNameListActivity;
import com.example.accessibilityt.MainActivity;
import com.example.accessibilityt.R;
import com.example.accessibilityt.dao.AppCodeInfo;
import com.example.accessibilityt.dao.AppCodeInfoDao;
import com.example.accessibilityt.xposed.ConstantX;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.DataTruncation;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AppNameAdapter extends RecyclerView.Adapter<AppNameAdapter.AppViewHoder>{
    Context mContext;
    private static List<AppListJSONData> infos;

    private static AppNameAdapter instance;
    public static AppNameAdapter getInstance(Context context){
        if (instance==null){
            instance=new AppNameAdapter(context);
        }
        return instance;
    }
    private AppNameAdapter(Context context){
        mContext=context;
        setListData();
        Log.d(AppNameListActivity.TAG,"list size:"+getDataListSize());
    }
    JSONObject
            jsonObject= null;
    public void setListData(){
        if (infos==null) infos=new ArrayList();
        infos.clear();
        if (new File(ConstantV.APPLICATION_JSON).exists()){
            Log.d(AppNameListActivity.TAG,"application is exist");

            try {
                jsonObject = new JSONObject(new String(Files.readAllBytes(Paths.get(ConstantV.APPLICATION_JSON))));
            } catch (JSONException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if (jsonObject!=null) {
                Iterator<String> it=jsonObject.keys();
                while (it.hasNext()){
                    String key=it.next();

                    try {
                        JSONObject object= (JSONObject) jsonObject.get(key);
                        Log.d(AppNameListActivity.TAG,"jsonObject  key "+key+"   "+object.toString());
                        if (object!=null){
                            Iterator<String> sonIt=object.keys();
                            while (sonIt.hasNext()){
                                String sonKey=sonIt.next();
                                Log.d(AppNameListActivity.TAG,"object  keys "+object.length()+"  "+sonKey);
                                infos.add(new AppListJSONData(sonKey,key,object.getString(sonKey)));
                            }
                        }
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    Log.d(MainActivity.TAG,"ApkFindXposed_create_json_key : "+key);

                }
            }
        }else Log.d(AppNameListActivity.TAG,"application is exist");
    }

    public List<AppListJSONData> getInfos() {
        return infos;
    }

    public void setInfos(List<AppListJSONData> infos) {
        this.infos = infos;
    }

    @NonNull
    @Override
    public AppViewHoder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view=View.inflate(mContext, R.layout.appname_item,null);
        AppViewHoder appViewHoder=new AppViewHoder(view);
        return appViewHoder;
    }

    @Override
    public void onBindViewHolder(@NonNull AppViewHoder holder, int position) {
        AppListJSONData info=infos.get(position);
        holder.textView.setText(info.appName);
        holder.textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(mContext,CcodeShowView.class);
                Bundle bundle = new Bundle();
                bundle.putString("appName", info.appName); // 设置要传递的数据
                intent.putExtras(bundle);
                Log.d("CcodeShowView","appNameList click appName:"+info.appName);
                if((Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)){
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                }
                mContext.startActivity(intent);
            }
        });
        holder.menuLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(AppNameListActivity.TAG,"menuLayout :"+info.appName);
                if (VDataTools.rDatabase==null){
                    VDataTools.daoInit(FloadWindowService.mContext);
                }
                new Thread(){
                    @Override
                    public void run() {
                        super.run();
                        try {
                            AppCodeInfoDao dao=VDataTools.rDatabase.getDao();
                            dao.delete(info.appName);
                        }catch (Exception e){
                            Log.d(AppNameListActivity.TAG,"database table delete fail :"+info.appName);
                        }
                    }
                }.start();

                if (jsonObject==null){
                    try {
                        jsonObject = new JSONObject(new String(Files.readAllBytes(Paths.get(ConstantV.APPLICATION_JSON))));
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }

                jsonObject.remove(info.packageName);
                try {
                    Files.write(Paths.get(ConstantV.APPLICATION_JSON),jsonObject.toString().getBytes());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                infos.remove(info);
                instance.notifyDataSetChanged();

            }
        });
        Bitmap bitmap=null;
        if (info.icon_default!=null){
            File file=new File(info.icon_default);
            if (file.exists()){
                bitmap=BitmapFactory.decodeFile(file.getAbsolutePath());
            }
        }else {
            byte[] byteArray= Base64.decode(info.icon,Base64.DEFAULT);
            bitmap= BitmapFactory.decodeByteArray(byteArray,0,byteArray.length);
        }
        if (bitmap==null) return;
        holder.imageView.setImageDrawable(new BitmapDrawable(bitmap));

    }
    public int getDataListSize(){
        return infos.size();
    }

    @Override
    public int getItemCount() {
        return infos.size();
    }


    class AppViewHoder extends RecyclerView.ViewHolder{
        ImageView imageView;
        TextView textView;
        TextView menuLayout;
        LinearLayout contentLayout;

        public AppViewHoder(@NonNull View itemView) {
            super(itemView);
            imageView=itemView.findViewById(R.id.a_item_image);
            textView=itemView.findViewById(R.id.a_item_tv);
            menuLayout=itemView.findViewById(R.id.menu_layout_delete);
            contentLayout=itemView.findViewById(R.id.content_layout);
        }
    }
    private class AppListJSONData{
        String appName;
        String packageName;
        String icon=null;
        String icon_default=null;
        public AppListJSONData(String appName,String packageName,String icon){
            this.appName=appName;
            this.packageName=packageName;

            String[] value=icon.split("=");
            if (value.length>=2){
                //使用默认图片
                if (value[0].equals("localIconPath1")||value[0].contains("localIconPath1")){
                    icon_default=ConstantV.APPLICATION_ICON_DEFAULT;
                    return;
                }
            }

            this.icon=icon;
        }
    }
}
