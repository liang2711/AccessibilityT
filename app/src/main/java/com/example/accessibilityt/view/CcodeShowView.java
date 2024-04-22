package com.example.accessibilityt.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;

import com.example.accessibilityt.AppNameListActivity;
import com.example.accessibilityt.CNameDrawerFragment;
import com.example.accessibilityt.MainActivity;
import com.example.accessibilityt.R;
import com.example.accessibilityt.dao.AppCodeInfo;
import com.example.accessibilityt.dao.AppCodeInfoDao;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CcodeShowView extends AppCompatActivity {
    public static DrawerLayout drawerLayout;
    public static ListView listView;
    static CcodeAdapter ccodeAdapter=null;
    public static Handler handler=new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            List<AppCodeInfo> list
                =msg.getData().getParcelableArrayList("list");
            if (list!=null){
                ccodeAdapter.setInfos(list);
            }else{
                Log.d(MainActivity.TAG,"ccodeshowview handle list is null");
                return;
            }
//            for (AppCodeInfo a:list){
//                Log.d(MainActivity.TAG+" CcodeShowView",a.appName+"  "+a.className);
//            }
            ccodeAdapter.notifyDataSetChanged();
            drawerLayout.openDrawer(listView);
        }
    };
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.class_list);
        drawerLayout=findViewById(R.id.c_dl);
        Bundle bundle = getIntent().getExtras();
        String appName=null;
        String packageName=null;
        if (bundle != null) {
            appName = bundle.getString("appName"); // 获取传递的数据
            packageName=bundle.getString("packageName");
        }
        CNameDrawerFragment contentFragment = new CNameDrawerFragment();
        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction().replace(R.id.c_fl,contentFragment).commit();
        listView=findViewById(R.id.c_lv);
        List<AppCodeInfo> list=new ArrayList<>();
        ccodeAdapter=new CcodeAdapter(this,list);
        listView.setAdapter(ccodeAdapter);
        Log.d(MainActivity.TAG,"ccodeshowview onCreate appName "+appName);
        getAppListData(appName,packageName);
    }
    private void getAppListData(String appName,String packageName){
        if (VDataTools.rDatabase==null){
            VDataTools.daoInit(FloadWindowService.mContext);
        }
        new Thread(){
            @Override
            public void run() {
                super.run();
                List<AppCodeInfo> list=null;
                AppCodeInfoDao dao=null;
                try {
                    dao=VDataTools.rDatabase.getDao();
                    list=dao.getAppNameAll(appName);
                }catch (Exception e){
                    Log.d(AppNameListActivity.TAG,"database table Query fail :"+appName+" in ccodeshowview");
                }
                boolean isState=VDataTools.setMJson();
                Log.d(AppNameListActivity.TAG,"getAppListData CcodeShowView:"+appName+"   isState"+isState);
                Message message=Message.obtain();
                Bundle bundle=new Bundle();
                bundle.putParcelableArrayList("list",new ArrayList<>(list));
                message.setData(bundle);
                handler.sendMessage(message);
                if (!isState){
                    if (list==null) list=new ArrayList<>();
                    Thread thread=VDataTools.all(VDataTools.getServiceUri()+"/all",packageName,FloadWindowService.mContext);
                    thread.start();
                    try {
                        thread.join();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    VDataTools.javaFileZipResponse(VDataTools.getServiceUri()+"/dlzip",packageName,".java",appName,list);
                }else{
                    Thread thread=VDataTools.jarFileZipResponse(VDataTools.getServiceUri()+"/dlzip",
                            packageName,packageName.replace('.','/'),appName,".jar",FloadWindowService.mContext);
                    thread.start();
                    try {
                        thread.join();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    Message message1=Message.obtain();
                    Bundle bundle1=new Bundle();
                    bundle1.putParcelableArrayList("list",new ArrayList<>(dao.getAppNameAll(appName)));
                    message1.setData(bundle1);
                    handler.sendMessage(message1);
                }
            }
        }.start();
    }
    class CcodeAdapter extends BaseAdapter{

        List<AppCodeInfo> infos;
        Context mContext;

        public CcodeAdapter(Context context,List<AppCodeInfo> list){
            mContext=context;
            this.infos=list;
        }

        @Override
        public int getCount() {
            return infos.size();
        }

        public void setInfos(List<AppCodeInfo> infos) {
            List<AppCodeInfo> list=new ArrayList<>();
            Set set=new HashSet();
            for (AppCodeInfo str:infos){
                if (set.add(str))
                    list.add(str);
            }
            this.infos = list;
        }

        public List<AppCodeInfo> getInfos() {
            return infos;
        }

        @Override
        public Object getItem(int position) {
            return infos.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            AppCodeInfo info=infos.get(position);
            ViewHolder viewHolder=null;
            if (convertView==null){
                convertView= LayoutInflater.from(mContext).
                        inflate(R.layout.a_list_item,null);
                viewHolder=new ViewHolder(convertView);
                viewHolder.textView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        CNameDrawerFragment.title.setText(info.className);
                        CNameDrawerFragment.code_Context.setText(info.code);
                        drawerLayout.closeDrawer(listView);
                    }
                });
                convertView.setTag(viewHolder);
            }else
                viewHolder= (ViewHolder) convertView.getTag();
            viewHolder.textView.setText(info.className);

            return convertView;
        }

        class ViewHolder{
            TextView textView;
            ViewHolder(View view){
                textView=view.findViewById(R.id.a_list_item_tv);
            }
        }
    }
}
