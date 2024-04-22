package com.example.accessibilityt;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.accessibilityt.view.AppListTouchListener;
import com.example.accessibilityt.view.AppNameAdapter;
import com.example.accessibilityt.view.ConstantV;
import com.example.accessibilityt.xposed.ConstantX;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;

public class AppNameListActivity extends AppCompatActivity {
    public static String TAG="AppNameListActivity";
    RecyclerView mRecyclerView;
    AppNameAdapter appNameAdapter;
    Context mContext;

    private static boolean isFirstEnter = true;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG," onCreate ");
        setContentView(R.layout.app_list);
        mRecyclerView=findViewById(R.id.a_list_rv);
        appNameAdapter=AppNameAdapter.getInstance(this);
        mRecyclerView.setAdapter(appNameAdapter);
        LinearLayoutManager layoutManager=new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);
        mContext=getApplicationContext();
        mRecyclerView.addOnItemTouchListener(new AppListTouchListener());
        RefreshLayout refreshLayout=findViewById(R.id.a_list_rl);
        if (isFirstEnter) {
            isFirstEnter = false;
            refreshLayout.autoRefresh();//第一次进入触发自动刷新，演示效果
        }

        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                if (new File(ConstantV.APPLICATION_JSON).exists()){
                    //解析json文件
                    JSONObject jsonObject= null;
                    try {
                        jsonObject = new JSONObject(new String(Files.readAllBytes(Paths.get(ConstantV.APPLICATION_JSON))));
                        if (jsonObject!=null) {
                            if(jsonObject.length()==appNameAdapter.getDataListSize()){
                                refreshLayout.finishRefresh(2000);
                                Toast.makeText(mContext,"已经是最新",Toast.LENGTH_SHORT).show();
                                return;
                            }
                            refreshLayout.finishRefresh(2000);
                            appNameAdapter.setListData();
                            appNameAdapter.notifyDataSetChanged();

                        }
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
    }
}
